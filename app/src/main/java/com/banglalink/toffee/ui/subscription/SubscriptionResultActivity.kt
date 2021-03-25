package com.banglalink.toffee.ui.subscription

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivitySubscriptionResultBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionResultActivity : AppCompatActivity() {

    companion object{
        const val PACKAGE = "PACKAGE"
    }

    private lateinit var binding :ActivitySubscriptionResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPackage = intent.getSerializableExtra(PACKAGE) as Package
        binding = DataBindingUtil.setContentView(this,R.layout.activity_subscription_result)

        setUpToolbar()
        binding.packageNameTv.text = mPackage.packageName
        binding.backToHome.setOnClickListener{
            backToHome()
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { backToHome() }
    }

    private fun backToHome(){
        launchActivity<HomeActivity>{
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(HomeActivity.INTENT_PACKAGE_SUBSCRIBED,true)
        }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToHome()
    }
}
