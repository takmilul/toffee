package com.banglalink.toffee.ui.redeem

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityRedeemCodeLayoutBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.ui.widget.showDisplayMessageDialog
import com.banglalink.toffee.util.unsafeLazy

class RedeemCodeActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivityRedeemCodeLayoutBinding
    private val viewModel by viewModels<RedeemCodeViewModel>()

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
            showDisplayMessageDialog(this, "Please enter valid referral code.")
            return
        }

        redeemReferralCode(binding.referralCode.text.toString())
    }

    private fun redeemReferralCode(redeemCode: String) {
        progressDialog.show()
        observe(viewModel.redeemReferralCode(redeemCode)) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    showDisplayMessageDialog(this@RedeemCodeActivity, it.data.referralStatusMessage) {
                        finish()
                    }
                }
                is Resource.Failure -> {
                    if (it.error.code == 100) {
                        showDisplayMessageDialog(this@RedeemCodeActivity, it.error.msg)
                    } else {
                        binding.root.snack(it.error.msg) {
                            action("Ok") {
                            }
                        }
                    }
                }
            }
        }
    }

}