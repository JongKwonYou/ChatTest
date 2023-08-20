package com.yjk.chattest.view.pharmacy.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.android.user.User
import com.yjk.chattest.data.UserType

class PharmacyViewModel : ViewModel() {

    lateinit var userType: UserType
    var user: User? = null

    var drawer : MutableLiveData<Boolean> = MutableLiveData(true)

    var setChannelData : MutableLiveData<GroupChannel> = MutableLiveData()



    fun setNickname() {
        val params = UserUpdateParams().apply {
            nickname = userType.name
        }

        SendbirdChat.updateCurrentUserInfo(params) { e ->
            if (e != null) {
//                Toast.makeText(this@ActivityPharmacy, "유저정보 업데이트 실패..", Toast.LENGTH_SHORT).show()
            }
        }
    }


}