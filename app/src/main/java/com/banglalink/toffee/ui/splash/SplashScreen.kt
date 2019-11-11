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
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.exception.UpdateRequiredException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.login.SigninByPhoneActivity

class SplashScreen : BaseAppCompatActivity() {

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
            handler.postDelayed({
                launchActivity<HomeActivity>()
                finish()
            },2000)
        }
    }

    internal fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton(
            "Update"
        ) { dialogInterface, i ->
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
            builder.setNegativeButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    dialogInterface.dismiss()
                    viewModel.init(true)
                }
            })
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
}
