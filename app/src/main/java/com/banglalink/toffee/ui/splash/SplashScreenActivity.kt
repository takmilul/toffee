package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.ActivitySplashScreenBinding
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity

class SplashScreenActivity : BaseAppCompatActivity() {
    
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding!!
    private val splashViewModel by viewModels<SplashViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    
    fun observeApiLogin(skipUpdate:Boolean = false){
        observe(splashViewModel.apiLoginResponse){
            when(it){
                is Resource.Success ->{
                    ToffeeAnalytics.updateCustomerId(mPref.customerId)
                    this.launchActivity<HomeActivity>()
                    this.finish()
                }
                is Resource.Failure->{
                    Toast.makeText(this, it.error.msg, Toast.LENGTH_SHORT).show()
                    when(it.error){
                        is AppDeprecatedError -> {
                            showUpdateDialog(it.error.title,it.error.updateMsg,it.error.forceUpdate)
                        }
                        else->{
                            ToffeeAnalytics.logApiError("apiLogin",it.error.msg)
                            binding.root.snack(it.error.msg){
                                action("Retry") {
                                    splashViewModel.loginResponse(skipUpdate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)
        
        builder.setPositiveButton("Update") { _, _ ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${this.packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${this.packageName}")
                    )
                )
            }
            this.finish()
        }
        
        if (!forceUpdate) {
            builder.setNegativeButton("SKIP") { dialogInterface, _ ->
                dialogInterface.dismiss()
                splashViewModel.loginResponse(true)
            }
        }
        
        val alertDialog = builder.create()
        alertDialog.show()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}
