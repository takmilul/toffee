package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.banglalink.toffee.R

class RedeemBulletCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
): ConstraintLayout(context, attrs, defAttrStyle) {
    private var titleTextView: TextView? = null
    private var bulletImageView: ImageView? = null

    init {
        View.inflate(context, R.layout.list_item_redeemcode_bulletpoint, this)
        titleTextView = findViewById(R.id.bullet_title)
//        bulletImageView = findViewById(R.id.feedView)
    }

    fun setConfiguration(cardTitle: String) {
        titleTextView?.text = cardTitle
    }
}