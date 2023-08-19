package com.yjk.chattest

import android.app.Application
import android.util.Log
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.yjk.chattest.data.ChatConstants

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initChat()
    }

    private fun initChat(){
        SendbirdChat.init(
            InitParams(ChatConstants.CHAT_APP_ID, applicationContext, useCaching = true),
            object : InitResultHandler {
                override fun onMigrationStarted() {
                    Log.i("#####", "Called when there's an update in Sendbird server.")
                }

                override fun onInitFailed(e: SendbirdException) {
                    Log.i("#####", "Called when initialize failed. SDK will still operate properly as if useLocalCaching is set to false.")
                    ChatConstants.isInitializeChat.value = false
                }

                override fun onInitSucceed() {
                    Log.i("#####", "Called when initialization is completed.")
                    ChatConstants.isInitializeChat.value = true
                }
            }
        )
    }

}