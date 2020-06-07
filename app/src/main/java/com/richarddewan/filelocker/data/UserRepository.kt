package com.richarddewan.filelocker.data

class UserRepository (private val appPreference: AppPreference){

    fun saveUserData(userId: String,email: String){
        appPreference.setUserName(userId)
        appPreference.setUserEmail(email)
    }

    fun getUserName(): String {
         return appPreference.getUserName().toString()
    }

    fun getUserEmail(): String {
        return appPreference.getUserEmail().toString()
    }

    fun setAccessToken(token: String?){
        appPreference.setAccessToken(token)
    }

    fun getAccessToken(): String {
        return appPreference.getAccessToken().toString()
    }

}