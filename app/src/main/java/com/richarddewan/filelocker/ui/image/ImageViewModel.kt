package com.richarddewan.filelocker.ui.image

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.richarddewan.filelocker.util.AppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val imageDir = File(application.filesDir,"/image")
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val context = application

    val bitmap: MutableLiveData<Bitmap> = MutableLiveData()
    val snackbar_msg: MutableLiveData<String> = MutableLiveData()
    val isBiometric: MutableLiveData<Boolean> = MutableLiveData(false)

    companion object {
        const val TAG = "DashboardViewModel"

    }


    fun storeImage(bitmap: Bitmap){
        viewModelScope.launch{
            val scaleBitmap = Bitmap.createScaledBitmap(bitmap,1080,780,true)
            val now = Date()
            val fileName = "${sdf.format(now)}.jpg"

            if (!imageDir.exists()){
                imageDir.mkdir()
            }
            val file = File(imageDir,fileName)

            launch(Dispatchers.IO){
                try {
                    val fileOutputStream = FileOutputStream(file)
                    scaleBitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
                    //close and flush
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    this@ImageViewModel.bitmap.postValue(BitmapFactory.decodeFile(file.path))
                    snackbar_msg.postValue("Image saved successfully")

                }
                catch (e: Exception){
                    Log.e(TAG, e.toString())
                }
            }

        }

    }

    fun storeEncryptedImage(bitmap: Bitmap){
        viewModelScope.launch {

            val scaleBitmap = Bitmap.createScaledBitmap(bitmap,1080,780,true)
            val now = Date()
            val fileName = "${sdf.format(now)}.jpg"

            if (!imageDir.exists()){
                imageDir.mkdir()
            }
            val file = File(imageDir,fileName)

            launch(Dispatchers.IO) {
                try {
                    val encryptedFile = AppHelper.getAdvanceEncryptedFile(file,context,isBiometric.value)

                    val byteArrayOutputStream = ByteArrayOutputStream()
                    scaleBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream)

                    encryptedFile.openFileOutput().also { out->
                        out.write(byteArrayOutputStream.toByteArray())
                        out.flush()
                        out.close()
                        byteArrayOutputStream.flush()
                        byteArrayOutputStream.close()
                    }
                    snackbar_msg.postValue("Image encrypted successfully")

                    getEncryptedImage(fileName)

                }
                catch (e: Exception){
                    Log.e(TAG,e.toString())
                }
            }

        }

    }

    private fun getEncryptedImage(fileName: String){
        viewModelScope.launch {

            val file = File(imageDir,fileName)
            val encryptedFile = AppHelper.getAdvanceEncryptedFile(file, context, isBiometric.value)

            launch(Dispatchers.IO) {
                try {
                    encryptedFile.openFileInput().also { input->

                        val byteArrayInputStream = ByteArrayInputStream(input.readBytes())
                        this@ImageViewModel.bitmap.postValue(
                            BitmapFactory.decodeStream(byteArrayInputStream)
                        )

                    }
                    snackbar_msg.postValue( "Image decrypted successfully")

                }catch (e: Exception){
                    Log.e(TAG, e.toString())
                }
            }

        }

    }
}