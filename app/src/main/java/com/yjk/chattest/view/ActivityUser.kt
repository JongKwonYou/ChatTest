package com.yjk.chattest.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.sendbird.android.SendbirdChat
import com.sendbird.android.user.User
import com.yjk.chattest.data.ChatConstants
import com.yjk.chattest.data.UserType
import com.yjk.chattest.data.key_user_type
import com.yjk.chattest.databinding.ActivityMainBinding

class ActivityUser : AppCompatActivity() {

    private lateinit var userType : UserType
    private lateinit var user : User

    private lateinit var mBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        getData()
    }

    private fun getData(){
        userType = intent.getSerializableExtra(key_user_type) as UserType
    }

    private fun loading(isLoading : Boolean){
        mBinding.loadingLayer.visibility = if(isLoading) View.VISIBLE else View.GONE
    }
}