package com.yjk.chattest.data

import androidx.lifecycle.MutableLiveData

object ChatConstants {

    val CHAT_APP_ID = "45928CE2-B778-4869-8697-7707C90A3C30"

    val PHARMACY_ID = "20221112222"
    val PHARMACY_NAME = "건식약국"

    val USER_ID = "11111"
    val USER_NAME = "유종권"


    var isInitializeChat : MutableLiveData<Boolean> = MutableLiveData(false)
    var isConnectedChat : MutableLiveData<Boolean> = MutableLiveData(false)
}