package com.banglalink.toffee.ui.widget

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
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
    alertView.findViewById<TextView>(R.id.ok_button)
        .setOnClickListener(View.OnClickListener { alertDialog.dismiss() })
    alertDialog.show()
}

fun showAlertDialog(context: Context, title: String, message: String) {
    showAlertDialog(context, title, message, true)
}

fun showAlertDialog(
    context: Context,
    title: String,
    message: String,
    listener: View.OnClickListener
) {
    val factory = LayoutInflater.from(context)
    val alertView = factory.inflate(R.layout.alert_dialog_layout, null)
    val alertDialog = AlertDialog.Builder(context).create()
    alertDialog.setView(alertView)
    alertDialog.setCancelable(false)
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    (alertView.findViewById(R.id.title_tv) as TextView).text = title
    if (TextUtils.isEmpty(message)) {
        alertView.findViewById<TextView>(R.id.message_tv).setVisibility(View.GONE)
    } else {
        (alertView.findViewById(R.id.message_tv) as TextView).text = message
    }
    alertView.findViewById<TextView>(R.id.ok_button).setOnClickListener(listener)
    alertDialog.show()
}