package com.banglalink.toffee.ui.bubble

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Foreground service that pops a bubble on top of your screen
 */
abstract class BaseService : Service() {
    
    private var bubble: Bubble? = null
    
    companion object {
        lateinit var INSTANCE: BaseService
    }
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Allow only one Floatie to exist at a time
        if (startId == 1) {
            bubble = createFloatie()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        bubble?.removeViewFromWindow()
    }
    
    abstract fun createFloatie(): Bubble
}
