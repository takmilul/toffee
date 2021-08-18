package com.banglalink.toffee.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.views.ChipsInputEditText

class ToffeeChipsInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): ChipsInput(context, attrs) {
    override fun getEditText(): ChipsInputEditText {
        return super.getEditText().apply {
            gravity = Gravity.START or Gravity.TOP
            setLines(2)
            textSize = 12f//.dp.toFloat()
            maxLines = 2
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
    }
}