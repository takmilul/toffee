package com.banglalink.toffee.ui.splash

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.data.repository.NotificationInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.ActivitySplashScreenBinding
import com.banglalink.toffee.di.FirebaseInAppMessage
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var notificationInfoRepository: NotificationInfoRepository
    @Inject @FirebaseInAppMessage lateinit var inAppMessaging: FirebaseInAppMessaging
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        inAppMessaging.setMessagesSuppressed(true)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermission(POST_NOTIFICATIONS) {}.onDeclined {
                it.askAgain()
            }
        }
        
        MedalliaDigital.disableIntercept()
//        intent.getStringExtra("resourceUrl")?.let {
//            saveNotification(intent)
//            mPref.homeIntent.value = intent.setData(Uri.parse(it))
//        }
    }
    
    private fun saveNotification(intent: Intent?) {
        intent?.extras?.run {
            val pubSubId = getString("notificationId")
            val playNowUrl = getString("playNowUrl")
            val thumbnailUrl = getString("thumbnail")
            val resourceUrl = getString("resourceUrl")
            val notificationType = getString("notificationType")
            val watchLaterUrl = getString("watchLaterUrl")
            val customerId = getString("customerId")?.ifBlank { mPref.customerId }?.toString()?.toInt() ?: mPref.customerId
            val title = getString("notificationHeader")
            val content = getString("notificationText")
            val imageUrl = getString("image")
            
            val notificationInfo = NotificationInfo(null, customerId, notificationType, pubSubId, 0, 0, title, content, null, thumbnailUrl, imageUrl, resourceUrl, playNowUrl, watchLaterUrl, seenTime = System.currentTimeMillis(), isSeen = true)
            
            lifecycleScope.launch {
                runCatching {
                    notificationInfoRepository.insert(notificationInfo)
                }
            }
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}