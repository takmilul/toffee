package com.banglalink.toffee.ui.widget

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.px
import com.loopnow.fireworklibrary.views.VideoFeedView
import kotlin.math.ceil
import kotlin.math.roundToInt

const val VISIBLE_ITEM_COUNT = 3.25

class FireworkCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
): ConstraintLayout(context, attrs, defAttrStyle) {
    
    private var titleTextView: TextView? = null
    private var feedView: VideoFeedView? = null
    private var feedFrameView: FrameLayout? = null
    
    init {
        View.inflate(context, R.layout.firework_card_view, this)
        titleTextView = findViewById(R.id.fireworkHeader)
        feedView = findViewById(R.id.feedView)
        feedFrameView = findViewById(R.id.feedFrameView)
        setFeedFrameDynamicHeight()
    }
    
    private fun setFeedFrameDynamicHeight() {
        val spaceBeforeItems = 8.px * ceil(VISIBLE_ITEM_COUNT)
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val paddingHorizontal = (feedFrameView?.paddingLeft ?: 0) + (feedFrameView?.paddingRight ?: 0)
        val calculatedWidth = (screenWidth - paddingHorizontal - spaceBeforeItems) / VISIBLE_ITEM_COUNT
        val calculatedHeight = ((calculatedWidth / 9) * 16).roundToInt()  // video item ratio -> 9:16
        feedFrameView?.layoutParams?.height = calculatedHeight
    }
    
    fun setConfiguration(cardTitle: String, channelId: String, playlistId: String) {
        titleTextView?.text = cardTitle
        feedView?.setChannel(channelId)
        feedView?.setPlaylist(channelId, playlistId)
    }
}