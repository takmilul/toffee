package com.banglalink.toffee.ui.refer

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityReferAFriendLayoutBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class ReferAFriendActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivityReferAFriendLayoutBinding
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(ReferAFriendViewModel::class.java)
    }
    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_a_friend_layout)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }


        observe(viewModel.referralCode) {
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    binding.referralCode.text = it.data
                    binding.shareBtn.isEnabled = true
                    binding.copyBtn.isEnabled = true
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                    binding.root.snack(it.error.msg){
                        action("Retry") {
                            progressDialog.show()
                            viewModel.getMyReferralCode()
                        }
                    }
                }
            }
        }

        binding.shareBtn.setOnClickListener {
            viewModel.share(
                this@ReferAFriendActivity,
                binding.referralCode.text.toString(),
                "Share with"
            )
        }

        binding.copyBtn.setOnClickListener {
            viewModel.copy(this@ReferAFriendActivity,  binding.referralCode.text.toString())
            showToast(getString( R.string.copy_to_clipboard))
        }
    }

}