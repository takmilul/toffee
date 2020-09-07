package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.banglalink.toffee.R

class HashTagEditText @JvmOverloads constructor(mContext: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle): AppCompatEditText(mContext, attrs, defStyleAttr) {
    var hashTagColor: Int = Color.parseColor("#000000")
        set(value) {
            field = value
            text = text
        }

    init {
        val a = context.obtainStyledAttributes(attrs, intArrayOf(R.attr.colorAccent))
        try {
            this.hashTagColor = a.getColor(0, Color.parseColor("#448aff"))
        } finally {
            a.recycle()
        }

        addTextChangedListener {
            val text = it?.toString() ?: return@addTextChangedListener
            if(text.isBlank()) return@addTextChangedListener
//            if(text.isBlank() || !text.last().isWhitespace()) return@addTextChangedListener

            var startIndex = 0
            val wordList = text.split("\\s+".toRegex())
//            Log.e("EDIT", wordList.toString())
            for(word in wordList) {
                if(word.startsWith("#")) {
                    val windex = text.indexOf(word, startIndex)
                    it.setSpan(
                        ForegroundColorSpan(this.hashTagColor),
                        windex,
                        windex + word.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    startIndex = windex + word.length
                }
            }
        }
    }
}