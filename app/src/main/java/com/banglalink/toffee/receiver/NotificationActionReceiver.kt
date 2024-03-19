package com.banglalink.toffee.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.banglalink.toffee.notification.PUBSUBMessageStatus
import com.banglalink.toffee.notification.ToffeeNotificationService.Companion.ACTION_NAME
import com.banglalink.toffee.notification.ToffeeNotificationService.Companion.DISMISS
import com.banglalink.toffee.notification.ToffeeNotificationService.Companion.NOTIFICATION_ID
import com.banglalink.toffee.notification.ToffeeNotificationService.Companion.PUB_SUB_ID
import com.banglalink.toffee.notification.ToffeeNotificationService.Companion.WATCH_LATER
import com.banglalink.toffee.usecase.SendNotificationStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext)
    @Inject lateinit var sendNotificationStatusEvent: SendNotificationStatus
    
    override fun onReceive(context: Context?, intent: Intent?) {
        with(intent?.extras) {
            val pubSubId = this?.getString(PUB_SUB_ID) ?: "0"
            val actionName = this?.getInt(ACTION_NAME, DISMISS)
            val notificationId = this?.getInt(NOTIFICATION_ID, -1) ?: -1
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            if (actionName == WATCH_LATER) {
                coroutineScope.launch {
                    sendNotificationStatusEvent.execute(pubSubId, PUBSUBMessageStatus.LATER)
                }
            }
        }
    }
}