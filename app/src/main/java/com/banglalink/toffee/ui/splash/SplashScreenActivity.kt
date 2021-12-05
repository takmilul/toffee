package com.banglalink.toffee.ui.splash

import android.os.Bundle
import com.banglalink.toffee.databinding.ActivitySplashScreenBinding
import com.banglalink.toffee.ui.common.BaseAppCompatActivity

class SplashScreenActivity : BaseAppCompatActivity() {
    
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}