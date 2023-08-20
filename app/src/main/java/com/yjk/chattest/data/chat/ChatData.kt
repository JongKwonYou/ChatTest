package com.yjk.chattest.data.chat

import com.sendbird.android.message.BaseMessage

// mapping data
data class ChatData(val type: Int, val data: BaseMessage)