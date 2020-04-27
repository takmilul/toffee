package com.banglalink.toffee.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.banglalink.toffee.ui.home.HomeActivity

/**
 * @author tushar
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val ACTION_NAME = "action_name"
        const val RESOURCE_URL = "resource_url"
        const val WATCH_NOW = 100
        const val WATCH_LATER = 200
        const val DISMISS = 300
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val closeIntent =
            Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context?.sendBroadcast(closeIntent)

        val notificationId = intent?.getIntExtra(NOTIFICATION_ID, -1) ?: -1
        val actionName = intent?.getIntExtra(ACTION_NAME, DISMISS) ?: DISMISS
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        val resourceUrl = intent?.getStringExtra(RESOURCE_URL)
        if (actionName == WATCH_NOW && !resourceUrl.isNullOrBlank()) {
            val intent = Intent(context, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse(resourceUrl)
            context.startActivity(intent)
        }
    }

}