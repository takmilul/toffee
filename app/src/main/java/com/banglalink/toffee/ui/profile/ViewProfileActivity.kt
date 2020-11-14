package com.banglalink.toffee.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivityProfileBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewProfileActivity : BaseAppCompatActivity() {

    lateinit var binding:ActivityProfileBinding

    private val viewModel by viewModels<ViewProfileViewModel>()

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile)
        binding.data = EditProfileForm().apply {
            fullName = mPref.customerName
            phoneNo = mPref.phoneNumber
            photoUrl = mPref.userImageUrl?:""
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        loadProfile()
        observe(mPref.profileImageUrlLiveData){
            binding.profileIv.loadProfileImage(it)
        }
    }

    private fun loadProfile(){
        progressDialog.show()
        observe(viewModel.loadCustomerProfile()){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    binding.data = it.data
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    fun onClickEditProfile(view: View){
        launchActivity<EditProfileActivity>(requestCode = 1000){
            putExtra(EditProfileActivity.PROFILE_INFO,binding.data)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1000 && resultCode == Activity.RESULT_OK){
            if(data!=null){
                binding.data = data.getSerializableExtra(EditProfileActivity.PROFILE_INFO) as EditProfileForm
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
