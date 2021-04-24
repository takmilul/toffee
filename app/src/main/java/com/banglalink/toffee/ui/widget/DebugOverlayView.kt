package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.PlayerOverlayData
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DebugOverlayView(ctx: Context, val attrs: AttributeSet? = null, val defAttrStyle: Int = 0)
    :LinearLayout(ctx, attrs, defAttrStyle) {

    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var commonPreference: CommonPreference
    private var customTextView: TextView? = null
    private var debugTextView: TextView? = null
    private var contentId: String? = null
    private var parentWidth: Int = 0
    private var parentHeight: Int = 0
    private val marginDp = Utils.dpToPx(10)
    private var margin: Rect = Rect(marginDp, marginDp, marginDp, marginDp)

    init {
        View.inflate(ctx, R.layout.debug_overlay_layout, this)
        customTextView = findViewById(R.id.custom_text)
        debugTextView = findViewById(R.id.debug_text)
    }

    private var overlayData: PlayerOverlayData? = null

    fun setPlayerOverlayData(data: PlayerOverlayData, cid: String) {
        overlayData = data
        contentId = cid
        resetState()
    }

    private fun resetState() {
        overlayData?.let { data ->
            resetViewState()
            customTextView?.text = data.params.customText
            customTextView?.isVisible = data.params.customText.isNotBlank()
            debugTextView?.text = getFormattedDebugText(data.params.displayParams)
        }
    }

    private fun resetViewState() {
        overlayData?.let { data->
            if(data.params.bgColorCode.isNotBlank()) {
                val bgColor = Color.parseColor(data.params.bgColorCode)
                setBackgroundColor(bgColor)
            }
            if(data.params.fontColorCode.isNotBlank()) {
                val textColor = Color.parseColor(data.params.fontColorCode)
                customTextView?.setTextColor(textColor)
                debugTextView?.setTextColor(textColor)
            }
            val fontSize = data.params.fontSize.replace("[^\\d.]".toRegex(), "").toIntOrNull()
            fontSize?.let {
                customTextView?.textSize = it.toFloat()
                debugTextView?.textSize = it.toFloat()
            }
        }
    }

    private fun getFormattedDebugText(displayParams: List<String>): String {
        val sb = StringBuilder()
        displayParams.forEach {
            when(it) {
                MSISDN -> if(mPref.phoneNumber.isNotBlank()) sb.appendLine(mPref.phoneNumber)
                DEVICE_ID -> if(commonPreference.deviceId.isNotBlank()) sb.appendLine(commonPreference.deviceId)
                USER_ID -> if(mPref.customerId != 0) sb.appendLine(mPref.customerId)
                USER_NAME -> if(mPref.customerName.isNotBlank()) sb.appendLine(mPref.customerName)
//                PUBLIC_IP -> if(mPref.phoneNumber.isNotBlank()) sb.appendLine(mPref.)
                LOCATION -> if(mPref.latitude.isNotBlank() || mPref.longitude.isNotBlank()) sb.appendLine("${mPref.latitude}, ${mPref.longitude}")
                DEVICE_TYPE -> sb.appendLine("Android")
                CONTENT_ID -> if(!contentId.isNullOrBlank()) sb.appendLine(contentId)
            }
        }
        sb.setLength(sb.length - 1)
        return sb.toString()
    }

    private var dX = 0f
    private var dY = 0f

    /*override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return overlayData?.params?.position == "floating"
    }

    // https://stackoverflow.com/questions/9398057/android-move-a-view-on-touch-move-action-move
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = x - event.rawX
                dY = y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val nextX = max(margin.left.toFloat(), min(event.rawX + dX, parentWidth - margin.right - width.toFloat()))
                val nextY = max(margin.top.toFloat(), min(event.rawY + dY, parentHeight - margin.bottom - height.toFloat()))

                animate()
                    .x(nextX)
                    .y(nextY)
                    .setDuration(0)
                    .start()
            }
            else -> return false
        }
        return true
    }*/

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        x = margin.left.toFloat()
        y = margin.right.toFloat()
        parentWidth = if(parent is FrameLayout) (parent as FrameLayout).width else 0
        parentHeight = if(parent is FrameLayout) (parent as FrameLayout).height else 0
    }

    companion object {
        private const val MSISDN = "msisdn"
        private const val USER_NAME = "user_name"
        private const val DEVICE_ID = "device_id"
        private const val USER_ID = "user_id"
        private const val DEVICE_TYPE = "device_type"
        private const val CONTENT_ID = "content_id"
        private const val PUBLIC_IP = "public_ip"
        private const val LOCATION = "location"
    }
}