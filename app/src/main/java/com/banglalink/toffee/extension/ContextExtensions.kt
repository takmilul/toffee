package com.banglalink.toffee.extension

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.ui.common.BaseViewModel

fun Context.showToast(message: String, length: Int = Toast.LENGTH_LONG) {
    if(message.isNotBlank())
        Toast.makeText(this, message, length).show()
}

inline fun <reified T : Any> FragmentActivity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    when (requestCode) {
        -1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(intent, options)
            } else {
                startActivity(intent)
            }
        }
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivityForResult(intent, requestCode, options)
            } else {
                startActivityForResult(intent, requestCode)
            }
        }
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

inline fun <reified T: BaseViewModel> FragmentActivity.getViewModel():T{
    return ViewModelProviders.of(this).get(T::class.java)
}