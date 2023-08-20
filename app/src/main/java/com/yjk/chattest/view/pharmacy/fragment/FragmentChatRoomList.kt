package com.yjk.chattest.view.pharmacy.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.GroupChannelListQueryParams
import com.yjk.chattest.data.ChatConstants
import com.yjk.chattest.databinding.FragmentRoomListBinding
import com.yjk.chattest.view.pharmacy.ActivityPharmacy
import com.yjk.chattest.view.pharmacy.adapter.AdapterRoom
import com.yjk.chattest.view.pharmacy.viewmodel.PharmacyViewModel

class FragmentChatRoomList : Fragment(){

    private lateinit var viewModel : PharmacyViewModel
    private lateinit var mBinding : FragmentRoomListBinding

    private lateinit var adapter : AdapterRoom

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(activity as ActivityPharmacy)[PharmacyViewModel::class.java]
        mBinding = FragmentRoomListBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView(){
        if(viewModel.user == null) {
            Log.d("#######", "user 없음!")
            return
        }

        adapter = AdapterRoom(viewModel.user!!.userId) { channel ->
            viewModel.setChannelData.postValue(channel)
            viewModel.drawer.postValue(false)
        }
        mBinding.recyclerViewRoom.adapter = adapter

        // 약국 정보
        mBinding.tvName.text = viewModel.user!!.nickname

        findChannel()
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
                return@next
            }

            if(channels == null || channels.isEmpty()){
                Log.d("#####","채널이 없습니다.")
            }else {
                adapter.addList(channels.toMutableList())
            }
        }
    }

}