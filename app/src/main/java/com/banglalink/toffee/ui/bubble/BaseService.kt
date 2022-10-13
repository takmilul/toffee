package com.banglalink.toffee.ui.bubble

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import com.banglalink.toffee.data.repository.BubbleConfigRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service that pops a bubble on top of your screen
 */
@AndroidEntryPoint
abstract class BaseService : Service() {
    
    private var bubble: Bubble? = null
    var countDownTimer: CountDownTimer? = null
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var bubbleConfigRepository: BubbleConfigRepository
    
    companion object {
        lateinit var INSTANCE: BaseService
    }
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Allow only one Bubble to exist at a time
        if (startId == 1) {
            bubble = createBubble()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        bubble?.removeViewFromWindow()
    }
    
    abstract fun createBubble(): Bubble
}
