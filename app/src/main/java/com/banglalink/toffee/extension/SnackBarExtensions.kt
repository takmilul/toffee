package com.banglalink.toffee.extension

import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R.color
import com.banglalink.toffee.R.drawable
import com.google.android.material.R.id.snackbar_action
import com.google.android.material.snackbar.BaseTransientBottomBar
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

fun View.showTopSnackbar(
    message: String,
    topMargin: Int,
    length: Int = Snackbar.LENGTH_INDEFINITE,
    onActionClicked: (Snackbar.() -> Unit)? = null
): Snackbar {
    return Snackbar.make(this, message, length).apply {
        view.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = android.view.Gravity.TOP
            marginStart = 24
            marginEnd = 24
            this.topMargin = topMargin
        }
        view.background = (ContextCompat.getDrawable(this.context, drawable.rounded_snackbar_shape))
        animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        val textView = view.findViewById<TextView>(snackbar_action)
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable.ic_cross, 0)
        setAction(".") {
            dismiss()
            onActionClicked?.invoke(this)
        }
        setActionTextColor(ContextCompat.getColor(this.context, color.colorAccent2))
        show()
    }
}