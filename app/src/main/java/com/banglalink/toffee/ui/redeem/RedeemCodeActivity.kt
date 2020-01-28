package com.banglalink.toffee.ui.redeem

import android.os.Bundle
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityRedeemCodeLayoutBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivityRedeemCodeLayoutBinding
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(RedeemCodeViewModel::class.java)
    }
    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_redeem_code_layout)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.redeemBtn.setOnClickListener { _ -> handleRedeemCodeButton() }
    }

    private fun handleRedeemCodeButton() {
        if (TextUtils.isEmpty(binding.referralCode.text.toString())) {
            showToast("Please enter valid referral code.")
            return
        }

        redeemReferralCode(binding.referralCode.text.toString())
    }

    private fun redeemReferralCode(redeemCode: String) {
        observe(viewModel.redeemReferralCode(redeemCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    showToast("Your code has been successfully redeemed.")

                }
                is Resource.Failure -> {
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                        }
                    }
                }
            }
        }
    }

}