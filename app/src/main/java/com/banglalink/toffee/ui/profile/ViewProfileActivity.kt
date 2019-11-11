package com.banglalink.toffee.ui.profile

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityProfileBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog

class ViewProfileActivity : BaseAppCompatActivity() {

    lateinit var binding:ActivityProfileBinding

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(ViewProfileViewModel::class.java)
    }
    private val progressDialog by lazy {
        VelBoxProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile)
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        loadProfile()

    }

    private fun loadProfile(){
        progressDialog.show()
        observe(viewModel.profileLiveData){
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
        launchActivity<EditProfileActivity>()
    }

    fun onClickChangePassword(view:View){

    }
}
