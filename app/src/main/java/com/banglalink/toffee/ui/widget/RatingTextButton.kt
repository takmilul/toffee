package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class RatingTextButton @JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttrs: Int = 0): LinearLayout(mContext, attrs, defStyleAttrs) {

    /*private var starView: View
    private var subAmountView: TextView

    companion object {
        val subStatusText = listOf("SUBSCRIBE", "SUBSCRIBED")
    }

    init {
        View.inflate(context, R.layout.multi_text_button, this)
        subStatusView = findViewById(R.id.subscription_status)
        subAmountView = findViewById(R.id.subscription_amount)
        setSubscriptionInfo(false, null)
    }

    fun setSubscriptionInfo(status: Boolean, amount: String?) {
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
        subStatusView.setTextColor(ContextCompat.getColorStateList(context, R.color.subscribe_button_text_color))
        dispatchSetActivated(status)
    }*/
    
}