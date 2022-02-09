package com.banglalink.toffee.ui.common

import android.content.Context
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.R.string
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder

object UnSubscribeDialog {
    
    fun show(context: Context, positiveButtonListener: () -> Unit){
        VelBoxAlertDialogBuilder(
            context,
            text = context.getString(string.text_unsubscribe_title),
            icon = drawable.ic_unsubscribe_alert,
            positiveButtonTitle = "Unsubscribe",
            positiveButtonListener = {
                positiveButtonListener()
                it?.dismiss()
            }
        ).create().show()
    }
}