package com.banglalink.toffee.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.banglalink.toffee.notification.PUBSUBMessageStatus
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.home.HomeActivity

class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ROW_ID = "id"
        const val DISMISS = 300
        const val WATCH_NOW = 100
        const val WATCH_LATER = 200
        const val CONTENT_VIEW = 400
        const val PUB_SUB_ID = "pub-sub_id"
        const val ACTION_NAME = "action_name"
        const val RESOURCE_URL = "resource_url"
        const val NOTIFICATION_ID = "notification_id"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        with(intent?.extras) {
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            val rowId = this?.getLong(ROW_ID) ?: 0L
            val pubSubId = this?.getString(PUB_SUB_ID) ?: "0"
            val resourceUrl = this?.getString(RESOURCE_URL)
            val notificationId = this?.getInt(NOTIFICATION_ID, -1) ?: -1
            val actionName = this?.getInt(ACTION_NAME, DISMISS) ?: DISMISS
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            
            context.sendBroadcast(closeIntent)
            if (actionName == WATCH_NOW && !resourceUrl.isNullOrBlank()) {
                val newIntent = Intent(context, HomeActivity::class.java).apply {
                    putExtra(ROW_ID, rowId)
                    data = Uri.parse(resourceUrl)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(newIntent)
                PubSubMessageUtil.sendNotificationStatus(pubSubId, PUBSUBMessageStatus.OPEN)
            } else if (actionName == CONTENT_VIEW && !resourceUrl.isNullOrBlank()) {
                val newIntent = Intent(Intent.ACTION_VIEW, Uri.parse(resourceUrl)).apply {
                    putExtra(ROW_ID, rowId)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(newIntent)
                PubSubMessageUtil.sendNotificationStatus(pubSubId, PUBSUBMessageStatus.OPEN)
            } else {
                PubSubMessageUtil.sendNotificationStatus(pubSubId, PUBSUBMessageStatus.LATER)
            }
        }
    }
}