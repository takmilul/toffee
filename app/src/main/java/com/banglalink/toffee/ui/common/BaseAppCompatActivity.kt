package com.banglalink.toffee.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import kotlin.system.exitProcess


abstract class BaseAppCompatActivity : AppCompatActivity(), Thread.UncaughtExceptionHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    //we are catching the CustomerNotFoundException across the app so that we can goto login screen
    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        if (p1 is CustomerNotFoundException) {
            Preference.getInstance().clear()
            launchActivity<SigninByPhoneActivity>{
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            finish()
        } else {
            p1?.printStackTrace()
           exitProcess(1)
        }
    }

    override fun onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        super.onDestroy()
    }
}