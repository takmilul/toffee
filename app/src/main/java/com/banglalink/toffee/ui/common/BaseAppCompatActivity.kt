package com.banglalink.toffee.ui.common

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


open class BaseAppCompatActivity : AppCompatActivity() {


    fun showAlertDialog(message: String, cancellable: Boolean) {

        AlertDialog.Builder(this)
            .setMessage(message)
            .setCancelable(cancellable)
            .setPositiveButton("Ok") { dialog, which -> dialog.dismiss()}
            .show()
        return
    }
}