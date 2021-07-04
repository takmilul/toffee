package com.banglalink.toffee.ui.widget

import android.content.Context
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.banglalink.toffee.R

class HashTagEditText @JvmOverloads constructor(mContext: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle): AppCompatEditText(mContext, attrs, defStyleAttr) {
    private var textWatcher: TextWatcher? = null

    private val hashTagColor: Int = ContextCompat.getColor(mContext, R.color.colorAccent2)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        textWatcher = addTextChangedListener {
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(textWatcher)
    }
}