package com.richarddewan.filelocker.ui.sharedpref

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.richarddewan.filelocker.util.AppHelper
import com.richarddewan.filelocker.data.AppPreference
import com.richarddewan.filelocker.data.UserRepository

class SharedPrefViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "SharedPrefViewModel"
    }


    private val preferences = AppHelper.getSharedPref(application)
    private val appPreferences = AppPreference(preferences)
    private val userRepository = UserRepository(appPreferences)

    val userIdField: MutableLiveData<String> = MutableLiveData()
    val userEmailField: MutableLiveData<String> = MutableLiveData()

    val message: MutableLiveData<String> = MutableLiveData()
    val snackbar_msg: MutableLiveData<String> = MutableLiveData()

    fun saveUserData(userId: String,email: String) {
        userRepository.saveUserData(userId,email)
        snackbar_msg.value = "user saved $userId"

    }

    fun getUserName() {
        userIdField.value = userRepository.getUserName()
    }

    fun getUserEmail() {
        userEmailField.value = userRepository.getUserEmail()
    }



}