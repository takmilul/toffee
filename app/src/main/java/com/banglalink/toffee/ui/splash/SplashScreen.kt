package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : BaseAppCompatActivity() {

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(SplashViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        observe(viewModel.customerLoginLiveData) {
            when (it) {
                is Resource.Success -> {
                    when(it.data){
                        true->{//User is logged in...so go to home
                            launchActivity<HomeActivity>()
                            finish()
                        }
                        false->{//user not logged in ....goto login screen
                            lifecycleScope.launch {
                                delay(2000)
                                launchActivity<SigninByPhoneActivity>()
                                finish()
                            }
                        }
                    }

                }
                is Resource.Failure->{
                    showToast(it.error.msg)

                }
            }
        }

        observe(viewModel.updateRequiredLiveData){
            showUpdateDialog(it.title,it.updateMsg,it.forceUpdate)
        }
    }

    fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {

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
                viewModel.init(true)
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
}
