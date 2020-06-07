package com.richarddewan.filelocker.ui.home


import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.snackbar.Snackbar
import com.richarddewan.filelocker.R
import com.richarddewan.filelocker.data.SecureFileEntity
import com.richarddewan.filelocker.ui.home.adaptor.SecureFileAdaptor
import com.richarddewan.filelocker.util.AppHelper
import kotlinx.android.synthetic.main.key_setup_dialog.*
import kotlinx.android.synthetic.main.key_edit_dialog.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.detail_dialog.view.*
import java.io.File

class HomeFragment : Fragment() {

    companion object {
        const val TAG = "MainActivity"
    }
    private var fileListEntity = ArrayList<SecureFileEntity>()
    lateinit var secureFileAdaptor: SecureFileAdaptor
    private  var mPosition: Int = 0

    private val onClickListener: (Int) ->Unit = {
        mPosition = it
        //dialogAuth()
        dialogAdvanceAuth()
    }

    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfoLockFile: BiometricPrompt.PromptInfo
    private lateinit var biometricPromptLockFile: BiometricPrompt

    private val lockFileAuthCallback = object: BiometricPrompt.AuthenticationCallback(){

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            showImageDialog(fileListEntity[mPosition].file)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e(TAG,errString.toString())
        }
    }

    //
    private val authenticationCallback = object: BiometricPrompt.AuthenticationCallback(){

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            dialogEditToken()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e(TAG,errString.toString())
        }
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        //setupBiometricPrompt
        setupBiometricPrompt()
        //unlock file
        setupBiometricPromptFileLock()
        //observe live data
        observer()
        //get access token
        viewModel.getAccessToken()
        viewModel.getFileList()

    }

    private fun observer(){
        viewModel.snackbar_msg.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(),it, Snackbar.LENGTH_LONG).show()

        })

        viewModel.fileListEntity.observe(viewLifecycleOwner, Observer {
            fileListEntity = it
            setupRecyclerView()
        })

        viewModel.isProgress.observe(viewLifecycleOwner, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun setupBiometricPrompt(){
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock")
            .setDescription("Scan Fingerprint")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            authenticationCallback
        )
    }

    private fun setupBiometricPromptFileLock(){
        promptInfoLockFile = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock File")
            .setDescription("Scan Fingerprint")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPromptLockFile = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            lockFileAuthCallback
        )
    }

    private fun setupRecyclerView(){
        secureFileAdaptor = SecureFileAdaptor(fileListEntity,onClickListener)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = secureFileAdaptor
    }

    private fun setMasterKeyClicked(){
        when(BiometricManager.from(requireContext()).canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                dialogEditToken()
            }
        }
    }

    private fun dialogSetToken(){
        MaterialDialog(requireContext()).show {
            val view =   customView(R.layout.key_setup_dialog)

            positiveButton(R.string.dialog_ok) {
                val accessToken = view.txtApiKey.text.toString()
                viewModel.setAccessToken(accessToken)
                it.dismiss()
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    private fun dialogEditToken(){
        MaterialDialog(requireContext()).show {
            val view =   customView(R.layout.key_edit_dialog)

            positiveButton(R.string.dialog_ok) {
                val oldAccessToken = view.txtOldApiKey.text.toString()
                val newAccessToken = view.txtNewApiKey.text.toString()
                viewModel.updateAccessToken(oldAccessToken,newAccessToken)
                it.dismiss()
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    private fun dialogAuth(){
        MaterialDialog(requireContext()).show {
            val view =   customView(R.layout.auth_dialog)

            positiveButton(R.string.dialog_ok) {
                val accessToken = view.txtApiKey.text.toString()
                when {
                    viewModel.accessToken.value.isNullOrEmpty() -> {
                        showImageDialog(fileListEntity[mPosition].file)
                    }
                    viewModel.accessToken.value == accessToken -> {
                        showImageDialog(fileListEntity[mPosition].file)
                    }
                    else -> {
                        viewModel.snackbar_msg.value = "Auth failed"
                    }
                }

                it.dismiss()
            }
            negativeButton(R.string.dialog_close) {
                it.dismiss()
            }
        }
    }

    private fun dialogAdvanceAuth(){
        when(BiometricManager.from(requireContext()).canAuthenticate()){
            BiometricManager.BIOMETRIC_SUCCESS ->{
                viewModel.isBiometric.value = true
                biometricPromptLockFile.authenticate(promptInfoLockFile)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showImageDialog(fileListEntity[mPosition].file)
            }
        }
    }

    private fun showImageDialog(file: File){
        val extension = file.extension
        val dialog = MaterialDialog(requireContext())
            .customView(R.layout.detail_dialog)
            .cornerRadius(8f)
        val customView = dialog.getCustomView()

        /*val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        customView.dialog_imageView.setImageBitmap(bitmap)*/

        if (extension == "jpg"){
            customView.dialog_imageView.visibility = View.VISIBLE
            viewModel.bitmap.observe(viewLifecycleOwner, Observer {
                //customView.dialog_imageView.setImage(ImageSource.bitmap(it))
                customView.dialog_imageView.setImageBitmap(it)
            })

            viewModel.getEncryptedImage(file.name)
        }
        else {
            customView.dialog_textView.visibility = View.VISIBLE
            viewModel.message.observe(viewLifecycleOwner, Observer {
                customView.dialog_textView.text = it
            })

            viewModel.getDecryptFile(file.name)
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.navigation_master_key ->{
                AppHelper.hideSoftKeyboard(activity)
                if (viewModel.accessToken.value.isNullOrEmpty()){
                    dialogSetToken()
                }
                else {
                    setMasterKeyClicked()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    @Suppress("UNCHECKED_CAST")
    class MainViewModelFactory(private val application: Application): ViewModelProvider.NewInstanceFactory(){

        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            (HomeViewModel(application) as T)
    }
}
