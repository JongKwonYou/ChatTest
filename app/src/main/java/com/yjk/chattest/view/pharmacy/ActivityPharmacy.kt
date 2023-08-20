package com.yjk.chattest.view.pharmacy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
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
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.PushNotificationDeliveryOption
import com.sendbird.android.params.MessageCollectionCreateParams
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.android.params.common.MessagePayloadFilter
import com.sendbird.android.user.User
import com.yjk.chattest.R
import com.yjk.chattest.data.*
import com.yjk.chattest.data.chat.ChatData
import com.yjk.chattest.databinding.ActivityChatListForPharmacyBinding
import com.yjk.chattest.view.pharmacy.fragment.FragmentChatRoomList
import com.yjk.chattest.view.pharmacy.viewmodel.PharmacyViewModel
import com.yjk.chattest.view.user.adapter.AdapterChatMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityPharmacy : AppCompatActivity() {

    private lateinit var viewModel: PharmacyViewModel
    private lateinit var adapter: AdapterChatMessage

    private var collection: MessageCollection? = null

    private lateinit var mBinding: ActivityChatListForPharmacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatListForPharmacyBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        viewModel = ViewModelProvider(this)[PharmacyViewModel::class.java]

        getData()
        setEvent()

        observing()
        connect()
    }

    private fun getData() {
        viewModel.userType = intent.getSerializableExtra(key_user_type) as UserType
        adapter = AdapterChatMessage(viewModel.userType.id)
        mBinding.recyclerViewMessage.adapter = adapter
    }

    private fun setEvent() {

        mBinding.btnBack.setOnClickListener {
            openDrawer()
        }

        mBinding.btnSend.setOnClickListener {
            if (mBinding.etMessage.text.toString().isEmpty()) {
                Toast.makeText(this@ActivityPharmacy, "텍스트를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendMessage(mBinding.etMessage.text.toString())
        }

        mBinding.etMessage.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {

            }
        }
    }

    private fun observing() {

        viewModel.drawer.observe(this) { isOpen ->
            if (isOpen) {
                openDrawer()
            } else {
                closeDrawer()
            }
        }

        viewModel.setChannelData.observe(this) { channel ->
            adapter.clear()

            // title setting
            for(member in channel!!.members){
                if(member.userId != viewModel.userType.id){
                    mBinding.tvTitle.text = member.nickname
                }
            }

            observingMessage(channel)
        }

    }

    private fun observingMessage(channel: GroupChannel){
        val params = MessageListParams().apply {
            reverse = false
            inclusive = false
            messageTypeFilter = MessageTypeFilter.ALL
            messagePayloadFilter = MessagePayloadFilter(
                includeMetaArray = true,
                includeReactions = true
            )
        }

        this.collection = SendbirdChat.createMessageCollection(MessageCollectionCreateParams(channel, params))

        this.collection!!.initialize(
            MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
            object : MessageCollectionInitHandler {
                override fun onApiResult(apiResultList: List<BaseMessage>?, e: SendbirdException?) {
                    if(apiResultList == null) return
                    val list = mutableListOf<ChatData>()
                    for (message in apiResultList.iterator()) {
                        var type = ChatConstants.TYPE_NORMAL_MESSAGE
                        if (message.customType == "buy") {
                            type = ChatConstants.TYPE_BUY_OTC_MESSAGE
                        }
                        list.add(ChatData(type, message))
                        Log.d("#####","sender : ${message.sender?.userId}")
                    }
                    adapter.addList(list)
                    scrollDown()
                }

                override fun onCacheResult(cachedList: List<BaseMessage>?, e: SendbirdException?) {

                }

            }
        )

        collection?.messageCollectionHandler = object : MessageCollectionHandler {
            override fun onMessagesAdded(
                context: MessageContext,
                channel: GroupChannel,
                messages: List<BaseMessage>
            ) {
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

    private fun connect() {
        loading(true)
        SendbirdChat.connect(viewModel.userType.id) { user, e ->
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
                    viewModel.user = user
                    viewModel.setNickname()
                    openDrawer()
                }
            } else {
                // Handle error.
                Log.e("######", "[Chatting Error] 유저 없음..")
            }
        }
    }

    private fun sendMessage(message : String){
        val params = UserMessageCreateParams(message).apply {
            customType = "normal"
            pushNotificationDeliveryOption = PushNotificationDeliveryOption.DEFAULT
        }

        viewModel.setChannelData.value?.sendUserMessage(params) { message, e ->
            if(e != null){
                Log.d("######","e : ${e.localizedMessage}")
                Toast.makeText(this@ActivityPharmacy, "메세지 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                return@sendUserMessage
            }
            mBinding.etMessage.setText("")
        }
    }

    fun openDrawer() {
        mBinding.viewFragment.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.viewFragment, FragmentChatRoomList())
            .commit()
    }

    fun closeDrawer() {
        mBinding.viewFragment.visibility = View.GONE
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStack()
    }

    private fun scrollDown(){
        MainScope().launch {
            delay(200)
            mBinding.recyclerViewMessage.scrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onStop() {
        super.onStop()
        collection?.dispose()
        collection?.messageCollectionHandler = null
    }

    private fun loading(isLoading: Boolean) {
        mBinding.loadingLayer.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}