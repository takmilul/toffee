package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivitySplashScreenBinding
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import com.banglalink.toffee.util.unsafeLazy
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : BaseAppCompatActivity() {

    lateinit var binding:ActivitySplashScreenBinding

    private val TAG = "SplashScreen"

    private val viewModel by unsafeLazy {
        getViewModel<SplashViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash_screen)

        if(viewModel.isCustomerLoggedIn())
            initApp()
        else{
            lifecycleScope.launch {
                delay(2000)
                launchActivity<SigninByPhoneActivity>()
                finish()
            }
        }
        val appEventsLogger = AppEventsLogger.newLogger(this)
        appEventsLogger.logEvent("app_launch")
        appEventsLogger.flush()

    }

    private fun initApp(skipUpdate:Boolean = false){
        observe(viewModel.init(skipUpdate)){
            when(it){
                is Resource.Success ->{
                    ToffeeAnalytics.updateCustomerId(Preference.getInstance().customerId)
                    launchActivity<HomeActivity>()
                    finish()
                }
                is Resource.Failure->{
                    when(it.error){
                        is AppDeprecatedError->{
                            showUpdateDialog(it.error.title,it.error.updateMsg,it.error.forceUpdate)
                        }
                        else->{
//                            ToffeeAnalytics.apiLoginFailed(it.error.msg)
                            binding.root.snack(it.error.msg){
                                action("Retry") {
                                    initApp(skipUpdate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton(
            "Update"
        ) { _, _ ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }

            finish()
        }

        if (!forceUpdate) {
            builder.setNegativeButton(
                "SKIP"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                initApp(true)
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
}
