package com.banglalink.toffee.ui.splash

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.banglalink.toffee.databinding.ActivitySplashScreenBinding
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.medallia.digital.mobilesdk.MedalliaDigital

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseAppCompatActivity() {
    
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
//        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MedalliaDigital.disableIntercept()
        intent.getStringExtra("resourceUrl")?.let {
            mPref.homeIntent.value = intent.setData(Uri.parse(it))
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}