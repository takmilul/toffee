package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.exception.UpdateRequiredException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.login.SigninByPhoneActivity

class SplashScreen : BaseAppCompatActivity() {

    companion object {
        const val MULTI_DEVICE_LOGIN_ERROR_CODE = 109
    }
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(SplashViewModel::class.java)
    }
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        try {
            viewModel.init(true)
        } catch (ce: CustomerNotFoundException) {
            ce.printStackTrace()
            handler.postDelayed({
                launchActivity<SigninByPhoneActivity>()
                finish()
            }, 2000)
        } catch (e: UpdateRequiredException) {
            e.printStackTrace()
            showUpdateDialog(e.title, e.updateMsg, e.forceUpdate)
        }

        observe(viewModel.splashLiveData) {
            when(it){
                is Resource.Success->{
                    handler.postDelayed({
                        launchActivity<HomeActivity>()
                        finish()
                    },2000)
                }
                is Resource.Failure->{
                    if(it.error.code == MULTI_DEVICE_LOGIN_ERROR_CODE){
                        Preference.getInstance().clear()
                        launchActivity<SigninByPhoneActivity>()
                        finish()
                    }else{
                        showToast(it.error.msg)
                    }

                }
            }

        }
    }

    internal fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton(
            "Update"
        ) { _, i ->
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
            builder.setNegativeButton("OK"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.init(true)
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
}
