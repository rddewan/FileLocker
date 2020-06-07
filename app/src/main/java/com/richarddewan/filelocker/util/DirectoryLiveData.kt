package com.richarddewan.filelocker.util

import android.os.FileObserver
import androidx.lifecycle.LiveData
import com.richarddewan.filelocker.data.SecureFileEntity
import java.io.File

/*
file change observer
 */

class DirectoryLiveData(private val observeDir: File): LiveData<List<SecureFileEntity>>() {

    private val observer = object: FileObserver(observeDir.path) {
        override fun onEvent(event: Int, path: String?) {
            handelFileChange()
        }
    }

    private fun handelFileChange(){
        postValue(observeDir.listFiles()?.map {
            SecureFileEntity(it.name,it,"${it.length() / 1024} KB")
        }?.sortedBy { data->
            data.fileName
        }?.reversed()
            ?: emptyList())
    }

    override fun onActive() {
        super.onActive()
        handelFileChange()
        observer.startWatching()
    }

    override fun onInactive() {
        super.onInactive()
        handelFileChange()
        observer.stopWatching()
    }
}