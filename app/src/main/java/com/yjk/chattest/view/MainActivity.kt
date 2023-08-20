package com.yjk.chattest.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.yjk.chattest.data.*
import com.yjk.chattest.databinding.ActivityMainBinding
import com.yjk.chattest.view.pharmacy.ActivityPharmacy
import com.yjk.chattest.view.user.ActivityUser

class MainActivity : AppCompatActivity() {

    private lateinit var userType : UserType

    private lateinit var mBinding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUserType()
    }

    // 유저 타입 셋팅 ( 약국 or 사용자 )
    private fun setUserType(){
        AlertDialog.Builder(this)
            .setTitle("User Type")
            .setMessage("약국으로 로그인하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                userType = UserType(PHARMACY_TYPE, ChatConstants.PHARMACY_ID, ChatConstants.PHARMACY_NAME)
                connectChat()
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                userType = UserType(USER_TYPE, ChatConstants.USER_ID, ChatConstants.USER_NAME)
                connectChat()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 채팅 연결
    private fun connectChat(){
        var i = Intent()
        if(userType.type == PHARMACY_TYPE) {
            i = Intent(this@MainActivity, ActivityPharmacy::class.java)
        }else {
            i = Intent(this@MainActivity, ActivityUser::class.java)
        }
        i.putExtra(key_user_type, userType)
        startActivity(i)
        finish()
    }

    private fun loading(isLoading : Boolean){
        mBinding.loadingLayer.visibility = if(isLoading) View.VISIBLE else View.GONE
    }
}