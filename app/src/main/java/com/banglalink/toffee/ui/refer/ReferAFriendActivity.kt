package com.banglalink.toffee.ui.refer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ShareCompat
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

        getMyReferralCode()
    }

    private fun getMyReferralCode(){
        observe(viewModel.getMyReferralCode()) {
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    binding.referralCode.text = it.data.referralCode
                    binding.shareBtn.isEnabled = true
                    binding.copyBtn.isEnabled = true

                    setCopyBtnClick(it.data.referralCode)
                    setShareBtnClick(it.data.sharableText)
                }
                is Resource.Failure->{
                    binding.root.snack(it.error.msg){
                        action("Retry") {
                            progressDialog.show()
                            viewModel.getMyReferralCode()
                        }
                    }
                }
            }
        }
    }

    private fun setShareBtnClick(shareableText:String){
        binding.shareBtn.setOnClickListener {
            val shareIntent: Intent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plan")
                .setText(shareableText)
                .intent
            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(shareIntent)
            }
        }
    }

    private fun setCopyBtnClick(referralCode:String){
        binding.copyBtn.setOnClickListener {
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(referralCode, referralCode)
                clipboard.setPrimaryClip(clip)
                showToast(getString( R.string.copy_to_clipboard))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}