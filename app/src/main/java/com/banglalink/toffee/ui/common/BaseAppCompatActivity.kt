package com.banglalink.toffee.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.exception.CustomerNotFoundException
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.login.SigninByPhoneActivity
import com.banglalink.toffee.util.EventProvider


abstract class BaseAppCompatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observe(EventProvider.getEventLiveData()){
            when(it.getValue()){
                is CustomerNotFoundException->{
                    Preference.getInstance().clear()
                    launchActivity<SigninByPhoneActivity>{
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    finish()
                }
            }
        }
    }
}