package com.yjk.chattest.view.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.MessageTypeFilter
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.message.*
import com.sendbird.android.params.*
import com.sendbird.android.params.common.MessagePayloadFilter
import com.sendbird.android.user.User
import com.yjk.chattest.data.*
import com.yjk.chattest.data.chat.ChatData
import com.yjk.chattest.databinding.ActivityChatListBinding
import com.yjk.chattest.view.user.adapter.AdapterChatMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityUser : AppCompatActivity() {

    private lateinit var userType: UserType
    private lateinit var user: User
    private var channel: GroupChannel? = null
    private var collection : MessageCollection? = null

    private lateinit var mBinding: ActivityChatListBinding

    private lateinit var adapter : AdapterChatMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        getData()
        connect()

        setEvent()
        observing()
    }

    private fun getData() {
        userType = intent.getSerializableExtra(key_user_type) as UserType
        adapter = AdapterChatMessage(userType.id)
        mBinding.recyclerViewMessage.adapter = adapter
    }

    private fun setEvent(){

        mBinding.btnSend.setOnClickListener {
            if(mBinding.etMessage.text.toString().isEmpty()){
                Toast.makeText(this@ActivityUser, "텍스트를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendMessage(mBinding.etMessage.text.toString())
        }

        mBinding.etMessage.setOnFocusChangeListener { view, isFocus ->
            if(isFocus){
                scrollDown()
            }
        }
    }

    private fun observing() {

    }

    private fun connect() {
        loading(true)
        SendbirdChat.connect(userType.id) { user, e ->
            loading(false)
            if (user != null) {
                if (e != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Chatting")
                        .setMessage("채팅 연결에 실패했습니다.\n다시 시도하시겠습니까?")
                        .setPositiveButton("예") { dialog, _ ->
                            connect()
                            dialog.dismiss()
                        }
                        .setNegativeButton("아니오") { dialog, _ ->
                            finish()
                        }
                        .create()
                        .show()
                } else {
                    // Proceed in online mode.
                    Log.d("#####","채널 connect 완료!")
                    this.user = user
                    setNickname()
                    findChannel()
                }
            } else {
                // Handle error.
                Log.e("######", "[Chatting Error] 유저 없음..")
            }
        }
    }

    private fun findChannel() {
        val query = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams().apply {
                includeEmpty = true
                userIdsExactFilter = listOf(ChatConstants.PHARMACY_ID, ChatConstants.USER_ID)
            }
        )

        query.next { channels, e ->
            if (e != null) {
                Log.e("#####", e.localizedMessage)
                createChannel()
            }

            if(channels == null || channels.isEmpty()){
                Log.d("#####","채널이 없습니다. -> 채널 생성")
                createChannel()
            }else {
                this.channel = channels.last()
                observingMessage()
            }
        }
    }

    private fun createChannel() {

        val params = GroupChannelCreateParams().apply {
            name = ChatConstants.USER_NAME
            coverUrl = ""
            userIds = listOf(ChatConstants.PHARMACY_ID, ChatConstants.USER_ID)
            isDistinct = true
            customType = ""
        }

        GroupChannel.createChannel(params) { channel, e ->
            if (e != null) {
                MainScope().launch {
                    Toast.makeText(this@ActivityUser, "채널 생성 실패.. (2초후 재시도)", Toast.LENGTH_SHORT)
                        .show()
                    delay(2000)
                    findChannel()
                }
            }

            if (channel != null) {
                this.channel = channel
                observingMessage()
            }else {
                findChannel()
            }
        }
    }

    private fun observingMessage(){
        Log.d("#####","observingMessage")
        if(channel == null){
            Log.d("######","channel is null..")
            return
        }

        // title setting
        for(member in channel!!.members){
            if(member.userId != userType.id){
                mBinding.tvTitle.text = member.nickname
            }
        }

        val params = MessageListParams().apply {
            reverse = false
            inclusive = false
            messageTypeFilter = MessageTypeFilter.ALL
            messagePayloadFilter = MessagePayloadFilter(
                includeMetaArray = true,
                includeReactions = true
            )
        }

        this.collection = SendbirdChat.createMessageCollection(MessageCollectionCreateParams(channel!!, params))

        this.collection!!.initialize(
            MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
            object : MessageCollectionInitHandler {
                override fun onApiResult(apiResultList: List<BaseMessage>?, e: SendbirdException?) {
                    if(apiResultList == null) return
                    Log.d("#####","onApiResult size : ${apiResultList.size}")
                    val list = mutableListOf<ChatData>()
                    for (message in apiResultList.iterator()) {
                        Log.d("######","message : ${message.message}")
                        var type = ChatConstants.TYPE_NORMAL_MESSAGE
                        if (message.customType == "buy") {
                            type = ChatConstants.TYPE_BUY_OTC_MESSAGE
                        }
                        list.add(ChatData(type, message))
                    }
                    adapter.addList(list)
                    scrollDown()
                }

                override fun onCacheResult(cachedList: List<BaseMessage>?, e: SendbirdException?) {

                }

            }
        )

//        val params = MessageListParams().apply {
//            reverse = false
//        }
//
//        collection = SendbirdChat.createMessageCollection(
//            MessageCollectionCreateParams(channel!!, params).apply {
//                // todo
//                startingPoint = System.currentTimeMillis()
//            }
//        )
//
        collection?.messageCollectionHandler = object : MessageCollectionHandler {
            override fun onMessagesAdded(
                context: MessageContext,
                channel: GroupChannel,
                messages: List<BaseMessage>
            ) {
                Log.d("######", "메세지 받음 ${messages.size}")
                val list = mutableListOf<ChatData>()
                for(message in messages){
                    var type = ChatConstants.TYPE_NORMAL_MESSAGE
                    if(message.customType == "buy"){
                        type = ChatConstants.TYPE_BUY_OTC_MESSAGE
                    }
                    list.add(ChatData(type, message))
                }
                adapter.addList(list)
                scrollDown()
            }

            override fun onMessagesUpdated(
                context: MessageContext,
                channel: GroupChannel,
                messages: List<BaseMessage>
            ) {
                Log.d("######","onMessagesUpdated")
            }

            override fun onMessagesDeleted(
                context: MessageContext,
                channel: GroupChannel,
                messages: List<BaseMessage>
            ) {
                Log.d("######","onMessagesDeleted")
            }

            override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
                Log.d("######", "onChannelUpdated...")

            }

            override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
            }

            override fun onHugeGapDetected() {
            }
        }
    }

    private fun setNickname(){
        val params = UserUpdateParams().apply {
            nickname = userType.name
        }

        SendbirdChat.updateCurrentUserInfo(params) { e ->
            if(e != null){
                Toast.makeText(this@ActivityUser, "유저정보 업데이트 실패..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(message : String){
        val params = UserMessageCreateParams(message).apply {
            customType = "normal"
            pushNotificationDeliveryOption = PushNotificationDeliveryOption.DEFAULT
        }

        channel?.sendUserMessage(params) { message, e ->
            if(e != null){
                Toast.makeText(this@ActivityUser, "메세지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                return@sendUserMessage
            }
            mBinding.etMessage.setText("")
            Log.d("####", "메세지 전송 성공..")
        }

    }

    private fun scrollDown(){
        MainScope().launch {
            delay(200)
            mBinding.recyclerViewMessage.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun loading(isLoading: Boolean) {
        mBinding.loadingLayer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        collection?.dispose()
        collection?.messageCollectionHandler = null
    }
}