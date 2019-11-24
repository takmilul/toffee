package com.banglalink.toffee.ui.refer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityReferAFriendLayoutBinding
import com.banglalink.toffee.extension.observe

class ReferAFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReferAFriendLayoutBinding
    private val referViewModel by lazy {
        ViewModelProviders.of(this).get(ReferAFriendViewModel::class.java)
    }
    private val referCode = ReferCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_a_friend_layout)
        binding.referCode = referCode
        binding.executePendingBindings()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        observe(referViewModel.referralCode) {
//            referralCode.text = s
//            shareBtn.isEnabled = !TextUtils.isEmpty(s)
//            copyBtn.isEnabled = !TextUtils.isEmpty(s)
            referCode.referalCode = it
            binding.executePendingBindings()
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