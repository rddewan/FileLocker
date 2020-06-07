package com.richarddewan.filelocker.ui.file

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.richarddewan.filelocker.R
import com.richarddewan.filelocker.ui.home.HomeFragment
import com.richarddewan.filelocker.util.AppHelper
import kotlinx.android.synthetic.main.fragment_file.*

class FileFragment : Fragment() {

    companion object {
        fun newInstance() = FileFragment()
    }
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt

    private val lockFileAuthCallback = object: BiometricPrompt.AuthenticationCallback(){

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            AppHelper.hideSoftKeyboard(activity)
            viewModel.encryptFile(txtBody.text.toString())

        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e(HomeFragment.TAG,errString.toString())
        }
    }

    private lateinit var viewModel: FileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FileViewModel::class.java)

        //setupBiometricPrompt
        setupBiometricPrompt()
        //observe
        observer()


        fab_save_msg.setOnClickListener {
            //AppHelper.hideSoftKeyboard(activity)
            //viewModel.encryptFile(txtBody.text.toString())
            //viewModel.storeTextFile(txtBody.text.toString())
            advanceEncryptFile()
        }

    }

    private fun setupBiometricPrompt(){
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Encrypt File")
            .setDescription("Scan Fingerprint")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            lockFileAuthCallback
        )
    }

    private fun advanceEncryptFile(){
        when(BiometricManager.from(requireContext()).canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                viewModel.isBiometric.value = true
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                AppHelper.hideSoftKeyboard(activity)
                viewModel.encryptFile(txtBody.text.toString())
            }
        }
    }


    private fun observer(){

        viewModel.snackbar_msg.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(),it, Snackbar.LENGTH_LONG).show()
        })
    }

}