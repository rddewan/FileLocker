package com.richarddewan.filelocker.ui.file

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.richarddewan.filelocker.data.AppPreference
import com.richarddewan.filelocker.data.UserRepository
import com.richarddewan.filelocker.ui.home.HomeViewModel
import com.richarddewan.filelocker.util.AppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FileViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "FileViewModel"
    }


    private val context = application

    val message: MutableLiveData<String> = MutableLiveData()
    val snackbar_msg: MutableLiveData<String> = MutableLiveData()
    val isBiometric: MutableLiveData<Boolean> = MutableLiveData(false)


    fun storeTextFile(body: String){

        viewModelScope.launch {
            val now = Date()
            val sdf = SimpleDateFormat("HH:mm:s", Locale.getDefault())
            val fileName = "${sdf.format(now)}.txt"

            val txtDir = File(context.filesDir,"documents")
            if (!txtDir.exists()){
                txtDir.mkdir()
            }
            val file = File(txtDir,fileName)

            launch(Dispatchers.IO){
                try {
                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(body.toByteArray())
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    snackbar_msg.postValue("Write file successfully")
                }
                catch (e: Exception){
                    Log.e(TAG,e.toString())
                }
            }

        }

    }

    fun readTextFile(fileName: String){
        viewModelScope.launch {
            val txtDir = File(context.filesDir,"documents")
            val file = File(txtDir,fileName)

            launch(Dispatchers.IO){
                try {
                    val fileInputStream = FileInputStream(file)
                    message.value = String(fileInputStream.readBytes(),Charsets.UTF_8)
                    snackbar_msg.postValue( "Read file successfully")

                }
                catch(e: Exception){
                    Log.e(TAG,e.toString())
                }
            }

        }

    }

    fun encryptFile(body: String){

        viewModelScope.launch {
            val now = Date()
            val sdf = SimpleDateFormat("HH:mm:s", Locale.getDefault())
            val fileName = "${sdf.format(now)}.txt"
            val dir  = File(context.filesDir,"documents")

            if (!dir.exists()){
                dir.mkdir()
            }
            val file = File(dir,fileName)

            launch(Dispatchers.IO){
                try {
                    val encryptedFile = AppHelper.getAdvanceEncryptedFile(file,context,isBiometric.value)

                    encryptedFile.openFileOutput().also { out->

                        out.write(body.toByteArray())

                        out.flush()
                        out.close()
                    }
                    snackbar_msg.postValue("File encrypted successfully")
                }
                catch (e: Exception){
                    Log.e(HomeViewModel.TAG, e.toString())
                }
            }

        }

    }

    fun decryptFile(fileName: String){

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