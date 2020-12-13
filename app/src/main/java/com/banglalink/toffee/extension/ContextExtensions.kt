package com.banglalink.toffee.extension

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.ui.common.BaseViewModel

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
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
            startActivity(intent, options)
        }
        else -> {
            startActivityForResult(intent, requestCode, options)
        }
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

inline fun <reified T: BaseViewModel> FragmentActivity.getViewModel():T{
    return ViewModelProviders.of(this).get(T::class.java)
}

fun View.setVisibility(isVisible: Boolean){
    this.visibility = if(isVisible) View.VISIBLE else View.GONE
}