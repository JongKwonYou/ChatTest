package com.yjk.chattest.data

import java.io.Serializable

val key_user = "user"
val key_user_type = "userType"
val PHARMACY_TYPE = 0
val USER_TYPE = 1

data class UserType (val type: Int, val id: String, val name: String) : Serializable