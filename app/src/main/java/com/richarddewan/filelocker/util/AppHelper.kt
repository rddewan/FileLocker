package com.richarddewan.filelocker.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File

object AppHelper {
    private val ENCRYPTED_PREFS = "com.richarddewan.jetpacksecurity.pref"
    private val masterKeysAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)


    fun getSharedPref(context: Context): SharedPreferences {

        val keyEncryptedScheme = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
        val valueEncryptedScheme = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS,
            masterKeysAlias,
            context,
            keyEncryptedScheme,
            valueEncryptedScheme
        )
    }

    fun getEncryptedFile(file: File, context: Context): EncryptedFile {

        val fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

        return EncryptedFile.Builder(
            file,
            context,
            masterKeysAlias,
            fileEncryptionScheme
        ).build()
    }

    fun getAdvanceEncryptedFile(
        file: File,
        context: Context,
        isBiometric: Boolean? = false
    ): EncryptedFile {
        val advancedSpec = KeyGenParameterSpec.Builder(
            "master_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT

        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)

            isBiometric?.let {
                setUserAuthenticationRequired(isBiometric)
                setUserAuthenticationValidityDurationSeconds(15)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setUnlockedDeviceRequired(true)
                setIsStrongBoxBacked(true)
            }
        }.build()

        val masterKeysAlias = MasterKeys.getOrCreate(advancedSpec)
        val fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

        return EncryptedFile.Builder(
            file,
            context,
            masterKeysAlias,
            fileEncryptionScheme
        ).build()
    }

    fun hideSoftKeyboard(activity: Activity?) {
        if (activity?.currentFocus == null) {
            return
        }
        val inputMethodManager: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }
}