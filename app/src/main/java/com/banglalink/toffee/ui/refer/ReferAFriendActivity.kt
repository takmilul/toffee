package com.banglalink.toffee.ui.refer

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityReferAFriendLayoutBinding

class ReferAFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReferAFriendLayoutBinding
    private var referViewModel: ReferAFriendViewModel? = null
    private val mToolbar : Toolbar by lazy{
       binding.toolbar as Toolbar
    }
    private val referCode = ReferCode()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_a_friend_layout)
        binding.setVariable(BR.referCode, referCode)
        binding.executePendingBindings()
        referViewModel = ViewModelProviders.of(this).get(ReferAFriendViewModel::class.java)
        setSupportActionBar(mToolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        mToolbar.setNavigationOnClickListener { onBackPressed() }
        referViewModel?.referralCode?.observe(this, Observer<String> { s ->
//            referralCode.text = s
//            shareBtn.isEnabled = !TextUtils.isEmpty(s)
//            copyBtn.isEnabled = !TextUtils.isEmpty(s)
            referCode.referalCode = s
            binding.executePendingBindings()
        })

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