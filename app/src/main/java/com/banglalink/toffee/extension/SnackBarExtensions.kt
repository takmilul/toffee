package com.banglalink.toffee.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar


fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(it) }
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_INDEFINITE, f: Snackbar.() -> Unit): Snackbar {
    return Snackbar.make(this, message, length).apply {
        f()
        show()
    }
}