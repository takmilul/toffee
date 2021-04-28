package com.banglalink.toffee.ui.common

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.util.EventProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseAppCompatActivity : AppCompatActivity() {

    @Inject lateinit var mPref: SessionPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (mPref.appThemeMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        observe(EventProvider.getEventLiveData()){
            when(it.getValue()){
                is CustomerNotFoundException->{
                    if(this is HomeActivity) {
//                        recreate()
//                        overridePendingTransition(0, 0)
                    } else {
                        mPref.clear()
                        launchActivity<SplashScreenActivity> {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        finish()
                    }
                }
            }
        }
    }
}