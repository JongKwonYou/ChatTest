package com.yjk.chattest.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.sendbird.android.SendbirdChat
import com.yjk.chattest.data.*
import com.yjk.chattest.databinding.ActivityMainBinding

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
        loading(true)
        SendbirdChat.connect(userType.id) { user, e ->
            loading(false)
            if (user != null) {
                if (e != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Chatting")
                        .setMessage("채팅 연결에 실패했습니다.\n다시 시도하시겠습니까?")
                        .setPositiveButton("예") { dialog, _ ->
                            connectChat()
                            dialog.dismiss()
                        }
                        .setNegativeButton("아니오") { dialog, _ ->
                            connectChat()
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    // Proceed in online mode.
                    var i = Intent()
                    if(userType.type == PHARMACY_TYPE) {
                        i = Intent(this@MainActivity, ActivityPharmacy::class.java)
                    }else {
                        startActivity(Intent(this@MainActivity, ActivityUser::class.java))
                    }
                    i.putExtra(key_user_type, userType)
                    i.putExtra(key_user, user.serialize())
                    startActivity(i)
                    finish()
                }
            } else {
                // Handle error.
                Log.e("######","[Chatting Error] 유저 없음..")
            }
        }
    }

    private fun loading(isLoading : Boolean){
        mBinding.loadingLayer.visibility = if(isLoading) View.VISIBLE else View.GONE
    }
}