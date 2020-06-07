package com.richarddewan.filelocker.ui.sharedpref

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.richarddewan.filelocker.R
import com.richarddewan.filelocker.util.AppHelper
import kotlinx.android.synthetic.main.fragment_shared_pref.*

class SharedPerfFragment : Fragment() {

    private lateinit var viewModel: SharedPrefViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProvider(this).get(SharedPrefViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_shared_pref, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //observe
        observer()
        //
        viewModel.getUserName()
        viewModel.getUserEmail()


        fab_save_pref.setOnClickListener {
            AppHelper.hideSoftKeyboard(activity)
            viewModel.saveUserData(
                txtUserId.text.toString(),
                txtEmail.text.toString()
            )
        }
    }

    private fun observer(){
        viewModel.userIdField.observe(viewLifecycleOwner, Observer {
            txtUserId.setText(it)
        })

        viewModel.userEmailField.observe(viewLifecycleOwner, Observer {
            txtEmail.setText(it)

        })

        viewModel.message.observe(viewLifecycleOwner, Observer {

        })

        viewModel.snackbar_msg.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(),it, Snackbar.LENGTH_LONG).show()
        })
    }
}
