package com.banglalink.toffee.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityEditProfileBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.VelBoxFieldTextWatcher
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showAlertDialog

class EditProfileActivity : AppCompatActivity() {

    private lateinit var progressDialog: VelBoxProgressDialog
    lateinit var binding:ActivityEditProfileBinding

    companion object{
        const val PROFILE_INFO = "Profile"
    }
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(EditProfileViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_edit_profile)
        binding.profileForm = intent.getSerializableExtra(PROFILE_INFO) as EditProfileForm

        progressDialog = VelBoxProgressDialog(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.nameEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.nameEt,
            VelBoxFieldTextWatcher.FieldType.NAME_FIELD
        )
        binding.emailEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.emailEt,
            VelBoxFieldTextWatcher.FieldType.EMAIL_FIELD
        )
        binding.addressEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.addressEt,
            VelBoxFieldTextWatcher.FieldType.ADDRESS_FIELD
        )


        binding.saveButton.setOnClickListener{
            handleSaveButton()
        }

        observe(viewModel.updateProfileLiveData){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    val intent = intent
                    intent.putExtra(PROFILE_INFO,binding.profileForm)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    private fun handleSaveButton(){
        if (TextUtils.isEmpty(binding.profileForm!!.fullName)) {
            showAlertDialog(
                this,
                getString(R.string.full_name_required_title),
                getString(R.string.full_name_required_msg)
            )
            return
        }

        if (TextUtils.isEmpty(binding.profileForm!!.email)) {
            showAlertDialog(
                this,
                getString(R.string.email_required_title),
                getString(R.string.email_required_msg)
            )
            return
        }

        if (TextUtils.isEmpty(binding.profileForm!!.address)) {
            showAlertDialog(
                this,
                getString(R.string.address_required_title),
                getString(R.string.address_required_msg)
            )
            return
        }

        progressDialog.show()
        viewModel.updateProfile(binding.profileForm!!)
    }
}
