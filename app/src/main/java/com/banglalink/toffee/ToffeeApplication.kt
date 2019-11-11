package com.banglalink.toffee

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import coil.Coil
import com.banglalink.toffee.data.storage.Preference

class ToffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preference.init(this)

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Coil.loader().clearMemory()
    }
}