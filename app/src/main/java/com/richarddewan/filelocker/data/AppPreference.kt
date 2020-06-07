package com.richarddewan.filelocker.data

import android.content.SharedPreferences

class AppPreference (private val prefs: SharedPreferences){

    companion object {

        const val KEY_USER_NAME = "PREF_KEY_USER_NAME"
        const val KEY_USER_EMAIL = "PREF_KEY_USER_EMAIL"
        const val KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN"

    }


    fun setUserName(userName: String) {
        prefs.edit().putString(KEY_USER_NAME,userName).apply()
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME,"")

    fun setUserEmail(userEmail: String?) {
        prefs.edit().putString(KEY_USER_EMAIL,userEmail).apply()
    }

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL,"")

    fun setAccessToken(token: String?) {
        prefs.edit().putString(KEY_ACCESS_TOKEN,token).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN,"")


}