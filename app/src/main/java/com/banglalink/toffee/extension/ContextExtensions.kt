package com.banglalink.toffee.extension

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.banglalink.toffee.ArrowPositionRules
import com.banglalink.toffee.Balloon
import com.banglalink.toffee.BalloonSizeSpec
import com.banglalink.toffee.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Scanner

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
@RequiresApi(Build.VERSION_CODES.M)
suspend fun Context.vpnConnectivityStatus(): Pair<Boolean, String>? = withContext(Dispatchers.IO) {
    try {
        // https://blog.tarkalabs.com/the-ultimate-vpn-detection-guide-for-ios-and-android-313b521186cb
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isVPNConnected: Boolean = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false

        // https://medium.com/@ISKFaisal/android-get-public-ip-address-with-java-kotlin-4d0575d2847
        val url = URL("https://api.ipify.org")
        val connection = url.openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0") // Set a User-Agent to avoid HTTP 403 Forbidden error
        val inputStream = connection.getInputStream()
        val ip = Scanner(inputStream, "UTF-8").useDelimiter("\\A").next()
        inputStream.close()

        isVPNConnected to ip
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun LifecycleOwner.launchWithLifecycle(observe: suspend (LifecycleOwner) -> Unit): Job {
    val lifecycleOwner = if(this is Fragment && this !is DialogFragment) this.viewLifecycleOwner else this
    
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            observe(lifecycleOwner)
        }
    }
}