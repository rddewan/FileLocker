package com.richarddewan.filelocker.ui.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.richarddewan.filelocker.util.AppHelper
import com.richarddewan.filelocker.data.AppPreference
import com.richarddewan.filelocker.data.SecureFileEntity
import com.richarddewan.filelocker.data.UserRepository
import com.richarddewan.filelocker.ui.image.ImageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val imageDir = File(application.filesDir,"/image")
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val context = application
    private val preferences = AppHelper.getSharedPref(application)
    private val appPreferences = AppPreference(preferences)
    private val userRepository = UserRepository(appPreferences)

    val accessToken: MutableLiveData<String> = MutableLiveData()
    val snackbar_msg: MutableLiveData<String> = MutableLiveData()
    val fileListEntity: MutableLiveData<ArrayList<SecureFileEntity>> = MutableLiveData()
    val fileList = ArrayList<SecureFileEntity>()
    val bitmap: MutableLiveData<Bitmap> = MutableLiveData()
    val message: MutableLiveData<String> = MutableLiveData()
    val isBiometric: MutableLiveData<Boolean> = MutableLiveData(false)
    val isProgress: MutableLiveData<Boolean> = MutableLiveData()

    fun setAccessToken(token: String) {
        userRepository.setAccessToken(token)
        //get access token
        getAccessToken()
    }

    fun updateAccessToken(oldToken: String, newToken: String) {
        if (oldToken != userRepository.getAccessToken()) {
            snackbar_msg.value = "Token does not match"
        } else {
            userRepository.setAccessToken(newToken)
            //get access token
            getAccessToken()
            snackbar_msg.value = "Token set successfully"
        }
    }

    fun getAccessToken() {
        accessToken.value = userRepository.getAccessToken()
    }

    fun getFileList() {
        isProgress.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(context.filesDir.path)
            val list = dir.listFiles()
            list?.forEach { i ->
                if (i.isDirectory) {
                    fileList.addAll(i.listFiles()?.map { file ->
                        SecureFileEntity(file.name, file,"${file.length() / 1024} KB")
                    }?.sortedByDescending { it.fileName} ?: emptyList()
                    )

                    fileListEntity.postValue(fileList)
                }
                if (i.isFile) {
                    fileList.add(SecureFileEntity(i.name, i,"${i.length() / 1024} KB"))
                    fileListEntity.postValue(fileList)
                }

            }
            isProgress.postValue(false)
            /*fileList?.forEach { file->
                fileListEntity.add(SecureFileEntity(file.name))
                Log.d(TAG,"File: ${file.name}")
            }*/
        }

    }

    fun getEncryptedImage(fileName: String){
        viewModelScope.launch {
            val file = File(imageDir,fileName)
            val encryptedFile = AppHelper.getAdvanceEncryptedFile(file,context, isBiometric.value)

            launch(Dispatchers.IO){
                try {
                    encryptedFile.openFileInput().also { input->

                        val byteArrayInputStream = ByteArrayInputStream(input.readBytes())
                        bitmap.postValue(BitmapFactory.decodeStream(byteArrayInputStream))

                    }
                    snackbar_msg.postValue("Image decrypted successfully")

                }catch (e: Exception){
                    Log.e(TAG, e.toString())
                }
            }

        }


    }

    fun getDecryptFile(fileName: String){
        viewModelScope.launch {
            val dir = File(context.filesDir,"documents")
            val file = File(dir,fileName)
            val encryptedFile = AppHelper.getAdvanceEncryptedFile(file, context,isBiometric.value)

            launch(Dispatchers.IO){
                try {
                    encryptedFile.openFileInput().also {input->
                        message.postValue(String(input.readBytes(),Charsets.UTF_8))
                    }
                    snackbar_msg.postValue("File decrypted successfully")

                }
                catch (e: Exception){
                    Log.e(TAG, e.toString())
                }
            }

        }

    }


}