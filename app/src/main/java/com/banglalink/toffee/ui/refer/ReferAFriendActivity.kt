package com.banglalink.toffee.ui.refer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityReferAFriendLayoutBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.util.unsafeLazy

class ReferAFriendActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivityReferAFriendLayoutBinding
    private val referViewModel by unsafeLazy {
        ViewModelProviders.of(this).get(ReferAFriendViewModel::class.java)
    }
    private val referCode = ReferCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_a_friend_layout)
        binding.referCode = referCode
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        observe(referViewModel.referralCode) {
//            referralCode.text = s
//            shareBtn.isEnabled = !TextUtils.isEmpty(s)
//            copyBtn.isEnabled = !TextUtils.isEmpty(s)
            referCode.referalCode = it
        }

//        shareBtn!!.setOnClickListener {
//            referViewModel?.share(
//                this@ReferAFriendActivity,
//                referralCode.text.toString(),
//                "Share with"
//            )
//        }
//
//        copyBtn!!.setOnClickListener {
//            referViewModel?.copy(this@ReferAFriendActivity, referralCode.text.toString())
//            Toast.makeText(
//                this@ReferAFriendActivity,
//                R.string.copy_to_clipboard,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

}