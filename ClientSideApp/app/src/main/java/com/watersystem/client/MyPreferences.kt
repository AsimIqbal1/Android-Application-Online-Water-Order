package com.watersystem.client

import android.content.Context

class MyPreferences(context: Context){
    private val username = "username"

    private val autoLoginPref = context.getSharedPreferences(username, Context.MODE_PRIVATE)

    fun setUserName(username1: String){
        val editor = autoLoginPref.edit()
        editor.putString(username, username1)
        editor.apply()
    }

    fun getUserName(): String{
        return autoLoginPref.getString(username,"")
    }
}