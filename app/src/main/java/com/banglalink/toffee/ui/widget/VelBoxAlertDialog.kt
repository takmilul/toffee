package com.banglalink.toffee.ui.widget

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.banglalink.toffee.R

fun showAlertDialog(context: Context, title: String, message: String, cancellable: Boolean) {
    val factory = LayoutInflater.from(context)
    val alertView = factory.inflate(R.layout.alert_dialog_layout, null)
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setView(alertView)
    alertDialog.setCancelable(cancellable)
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    
    (alertView.findViewById(R.id.title_tv) as TextView).text = title
    
    if (TextUtils.isEmpty(message)) {
        alertView.findViewById<TextView>(R.id.message_tv).setVisibility(View.GONE)
    } else {
        (alertView.findViewById(R.id.message_tv) as TextView).text = message
    }
    alertView.findViewById<TextView>(R.id.ok_button).setOnClickListener { alertDialog.dismiss() }
    alertDialog.show()
}

fun showAlertDialog(context: Context, title: String, message: String) {
    showAlertDialog(context, title, message, true)
}

fun showDisplayMessageDialog(
    context: Context?,
    message: String?,
    callbacks: () -> Unit = {},
) {
    val factory = LayoutInflater.from(context)
    val alertView: View = factory.inflate(R.layout.alert_dialog_display_message_layout, null)
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setView(alertView)
    alertDialog.setCancelable(true)
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    (alertView.findViewById<View>(R.id.message_tv) as TextView).text = message
    alertView.findViewById<View>(R.id.ok_button)
        .setOnClickListener {
            alertDialog.dismiss()
            callbacks.invoke()
        }
    alertDialog.show()
}

fun showRedeemDisplayMessageDialog(
    context: Context?,
    title: String?,
    message: String?,
    bulletMessage: List<String>?,
    callbacks: () -> Unit = {},
) {
    val factory = LayoutInflater.from(context)
    val alertView: View = factory.inflate(R.layout.alert_dialog_redem_invalid_layout, null)
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setView(alertView)
    alertDialog.setCancelable(true)
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val bulletContainer = alertView.findViewById<LinearLayout>(R.id.bulletContainer)
    val titleTv = alertView.findViewById<TextView>(R.id.dialogTitleTextView)
    val messageTv = alertView.findViewById<TextView>(R.id.messageTitleTextView)
    
    title?.let {
        titleTv.text = it
    }
    message?.let {
        messageTv.text = it
    }
    bulletMessage?.let { bulletMsg ->
        bulletMsg.forEach { text ->
            bulletContainer.addView(context?.let {
                RedeemBulletCardView(it).apply {
                    setConfiguration(text)
                }
            })
        }
    }
    alertView.findViewById<View>(R.id.ok_button)
        .setOnClickListener {
            alertDialog.dismiss()
            callbacks.invoke()
        }
    alertDialog.show()
}

fun showSubscriptionDialog(
    context: Context?,
    okCallBack: () -> Unit,
) {
    val factory = LayoutInflater.from(context)
    val alertView: View = factory.inflate(R.layout.alert_dialog_subscribe_pack, null)
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setView(alertView)
    alertDialog.setCancelable(true)
    
    alertView.findViewById<View>(R.id.ok_button)
        .setOnClickListener {
            alertDialog.dismiss()
            okCallBack.invoke()
        }
    alertView.findViewById<View>(R.id.close_iv)
        .setOnClickListener {
            alertDialog.dismiss()
        }
    
    alertDialog.show()
}