package com.richarddewan.filelocker.ui.image

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.richarddewan.filelocker.R
import com.richarddewan.filelocker.ui.home.HomeFragment
import com.richarddewan.filelocker.util.AppHelper
import com.richarddewan.filelocker.util.DirectoryLiveData
import kotlinx.android.synthetic.main.fragment_file.*
import kotlinx.android.synthetic.main.fragment_image.*
import java.io.File

class ImageFragment : Fragment() {

    companion object {
        const val TAG = "DashboardFragment"
        const val RC_CAPTURE_IMAGE = 100
    }
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt

    private val lockFileAuthCallback = object: BiometricPrompt.AuthenticationCallback(){

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            viewModel.storeEncryptedImage(bitmap)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e(HomeFragment.TAG,errString.toString())
        }
    }

    private lateinit var viewModel: ImageViewModel
    private lateinit var bitmap: Bitmap

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProvider(this).get(ImageViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_image, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setupBiometricPrompt
        setupBiometricPrompt()
        //observe
        observer()

        fb_capture_image.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent->
                intent.resolveActivity(requireContext().packageManager)?.also {
                    startActivityForResult(intent, RC_CAPTURE_IMAGE)
                }
            }
        }

        val imageDir = File(requireContext().filesDir,"/image")

        DirectoryLiveData(imageDir).observe(viewLifecycleOwner, Observer {
            Log.d(HomeFragment.TAG,it.toString())
        })
    }

    private fun setupBiometricPrompt(){
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Encrypt Image")
            .setDescription("Scan Fingerprint")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            lockFileAuthCallback
        )
    }

    private fun observer(){

        viewModel.bitmap.observe(viewLifecycleOwner, Observer {
            it?.let {
                imageView.setImageBitmap(it)
            }
        })

        viewModel.snackbar_msg.observe(viewLifecycleOwner, Observer {
           Snackbar.make(requireView(),it,Snackbar.LENGTH_LONG).show()
        })
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
                viewModel.storeEncryptedImage(bitmap)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CAPTURE_IMAGE){
            if (resultCode == Activity.RESULT_OK){
                bitmap = data?.extras?.get("data") as Bitmap

                //viewModel.storeEncryptedImage(bitmap)
                //viewModel.storeImage(bitmap)
                advanceEncryptFile()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class MainViewModelFactory(private val application: Application):
        ViewModelProvider.NewInstanceFactory(){

        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            (ImageViewModel(application) as T)
    }
}
