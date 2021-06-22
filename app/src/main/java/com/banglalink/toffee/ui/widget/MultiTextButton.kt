package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.dp

class MultiTextButton @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0):
    LinearLayout(mContext, attrs, defStyleAttrs) {

    private var subStatusView: TextView
    private var subAmountView: TextView

    companion object {
        val subStatusText = listOf("SUBSCRIBE", "SUBSCRIBED")
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiTextButton)
        val width = typedArray.getDimension(R.styleable.MultiTextButton_multitext_layout_width, resources.getDimension(R.dimen.multitext_button_width))
        val height = typedArray.getDimension(R.styleable.MultiTextButton_multitext_layout_height, resources.getDimension(R.dimen.multitext_button_height))
        val textSize = typedArray.getDimension(R.styleable.MultiTextButton_multitext_textSize, resources.getDimension(R.dimen.default_text_size))
        View.inflate(context, R.layout.multi_text_button, this)
        subStatusView = findViewById(R.id.subscription_status)
        subAmountView = findViewById(R.id.subscription_amount)
        subStatusView.layoutParams.apply { 
            this.width = width.toInt()
            this.height = height.toInt()
        }
        subStatusView.textSize = textSize.toInt().dp.toFloat()
        setSubscriptionInfo(false, null)
    }

    fun setMultitextButtonWidth(width: Int) {
        subStatusView.layoutParams.width = width
    }
    
    fun setMultitextButtonHeight(height: Int) {
        subStatusView.layoutParams.height = height
    }
    
    fun setMultitextButtonTextSize(textSize: Int) {
        subStatusView.textSize = textSize.toFloat()
    }
    
    fun setSubscriptionInfo(status: Boolean, amount: String? = null) {
        when(amount) {
            null -> {
                subAmountView.visibility = View.GONE
                subStatusView.text = subStatusText[if(status) 1 else 0]
                subStatusView.setBackgroundResource(R.drawable.subscribe_bg_round)
            }
            else -> {
                subAmountView.visibility = View.VISIBLE
                subStatusView.text = subStatusText[if(status) 1 else 0]
                subAmountView.text = amount
                subStatusView.setBackgroundResource(R.drawable.subscribe_bg_left)
                subAmountView.setBackgroundResource(R.drawable.subscribe_bg_right)
            }
        }
        subStatusView.setTextColor(ContextCompat.getColorStateList(context, R.color.button_text_color))
        isActivated = status
    }
}