package com.banglalink.toffee.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import com.banglalink.toffee.util.EventProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseAppCompatActivity : AppCompatActivity() {

    @Inject lateinit var mPref: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        observe(EventProvider.getEventLiveData()){
            when(it.getValue()){
                is CustomerNotFoundException->{
                    mPref.clear()
                    launchActivity<SigninByPhoneActivity>{
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    finish()
                }
            }
        }
    }
}