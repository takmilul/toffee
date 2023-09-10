package com.banglalink.toffee.extension

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.banglalink.toffee.ArrowPositionRules
import com.banglalink.toffee.Balloon
import com.banglalink.toffee.BalloonSizeSpec
import com.banglalink.toffee.R

fun Context.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) {
    if(!message.isNullOrBlank()) {
        Toast.makeText(this, message, length).show()
    }
}

inline fun <reified T : Any> FragmentActivity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    when (requestCode) {
        -1 -> startActivity(intent, options)
        else -> startActivityForResult(intent, requestCode, options)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

fun View.setVisibility(isVisible: Boolean){
    this.visibility = if(isVisible) View.VISIBLE else View.GONE
}

fun MotionLayout.onTransitionCompletedListener(onCompleted:(transitionId: Int) -> Unit){
    this.addTransitionListener(object : MotionLayout.TransitionListener{
        override fun onTransitionStarted(motion: MotionLayout?, startId: Int, endId: Int) { }
        override fun onTransitionChange(motion: MotionLayout?, startId: Int, endId: Int, progress: Float) { }
        override fun onTransitionTrigger(motion: MotionLayout?, startId: Int, endId: Boolean, progress: Float) { }
        override fun onTransitionCompleted(motion: MotionLayout?, transitionId: Int) {
            onCompleted(transitionId)
        }
    })
}

fun View.safeClick(action: View.OnClickListener, debounceTime: Long = 1000L) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return else action.onClick(v)
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

fun Context.getBalloon(tooltipText: String): Balloon {
    return Balloon.Builder(this)
        .setWidthRatio(0.90f)
        .setWidth(BalloonSizeSpec.WRAP)
        .setHeight(BalloonSizeSpec.WRAP)
        .setText(tooltipText)
        .setTextColorResource(com.banglalink.toffee.R.color.tooltip_text_color)
        .setTextSize(12f)
        .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        .setArrowSize(12)
        .setArrowPosition(0.5f)
        .setPaddingVertical(8)
        .setPaddingHorizontal(12)
        .setMarginHorizontal(16)
        .setTextTypeface(ResourcesCompat.getFont(this, com.banglalink.toffee.R.font.roboto_medium)!!)
        .setCornerRadius(8f)
        .setBackgroundColorResource(com.banglalink.toffee.R.color.tooltip_bg_color)
        .setTextGravity(Gravity.START)
        .build()
}
fun Context.showCustomToast(message: String?) {
    if (!message.isNullOrBlank()) {
        val inflater = LayoutInflater.from(this)
        val toastView = inflater.inflate(R.layout.custom_toast_layout, null)

        val toast = Toast(this)
        toast.view = toastView

        val toastText = toastView.findViewById<TextView>(R.id.customToastText)
        toastText.text = message

        // Set gravity to BOTTOM
        toast.setGravity(Gravity.BOTTOM, 0, 88)

        // Set duration and show the toast
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }
}