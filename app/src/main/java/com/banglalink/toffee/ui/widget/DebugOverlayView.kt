package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.PlayerOverlayData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DebugOverlayView(private val ctx: Context, val attrs: AttributeSet? = null, val defAttrStyle: Int = 0)
    :LinearLayout(ctx, attrs, defAttrStyle) {

    @Inject lateinit var mPref: Preference

    private var customTextView: TextView? = null
    private var debugTextView: TextView? = null
    private var contentId: String? = null

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
            val fontSize = data.params.fontSize.replace("\\w+".toRegex(), "").toIntOrNull()
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
                MSISDN -> sb.appendLine(mPref.phoneNumber)
                DEVICE_ID -> sb.appendLine(mPref.deviceId)
                USER_ID -> sb.appendLine(mPref.customerId)
                USER_NAME -> sb.appendLine(mPref.customerName)
//                PUBLIC_IP -> sb.appendLine(mPref.)
                LOCATION -> sb.appendLine("${mPref.latitude}, ${mPref.longitude}")
                DEVICE_TYPE -> sb.appendLine("Android")
                CONTENT_ID -> sb.appendLine(contentId)
            }
        }
        return sb.toString()
    }

    private var dX = 0f
    private var dY = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return overlayData?.params?.position == "floating"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = x - event.rawX
                dY = y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> animate()
                .x(event.rawX + dX)
                .y(event.rawY + dY)
                .setDuration(0)
                .start()
            else -> return false
        }
        return true
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