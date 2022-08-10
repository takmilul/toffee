package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Layout.Alignment.ALIGN_CENTER
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.AlignmentSpan.Standard
import android.text.style.ClickableSpan
import android.text.style.MetricAffectingSpan
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.home.CatchupDetailsFragment
import com.banglalink.toffee.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReadMoreTextView constructor(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    private var trimMode: Int
    private var trimLines: Int
    private var readMore = true
    private var trimLength: Int
    private var lineEndIndex = 0
    private var trimTextSize: Float
    private var colorClickableText: Int
    private var trimFont: Typeface? = null
    private val showTrimExpandedText: Boolean
    private var mainText: CharSequence? = null
    private var bufferType: BufferType? = null
    private var trimExpandedText: CharSequence
    private var trimCollapsedText: CharSequence
    private var textWatcher: TextWatcher? = null
    private val viewMoreSpan: ReadMoreClickableSpan
    
    val TAG = "READ_"
    
    companion object {
        private const val TRIM_MODE_LINES = 0
        private const val TRIM_MODE_LENGTH = 1
        private const val DEFAULT_TRIM_LENGTH = 240
        private const val DEFAULT_TRIM_LINES = 2
        private const val INVALID_END_INDEX = -1
        private const val DEFAULT_SHOW_TRIM_EXPANDED_TEXT = true
        private const val ELLIPSIZE = "... "
    }
    
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView)
        val fontId = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreFontFamily, 0)
        val resourceIdTrimCollapsedText = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreCollapsedText, R.string.read_more)
        val resourceIdTrimExpandedText = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreExpandedText, R.string.read_less)
        trimLength = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreLength, DEFAULT_TRIM_LENGTH)
        trimCollapsedText = resources.getString(resourceIdTrimCollapsedText)
        trimExpandedText = resources.getString(resourceIdTrimExpandedText)
        trimLines = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreTextLines, DEFAULT_TRIM_LINES)
        colorClickableText = typedArray.getColor(R.styleable.ReadMoreTextView_readMoreColorClickableText, ContextCompat.getColor(context, R.color.colorAccent))
        trimTextSize = typedArray.getDimension(R.styleable.ReadMoreTextView_readMoreTextSize, resources.getDimension(R.dimen.default_text_size))
        showTrimExpandedText = typedArray.getBoolean(R.styleable.ReadMoreTextView_readMoreExpandedText, DEFAULT_SHOW_TRIM_EXPANDED_TEXT)
        trimMode = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreMode, TRIM_MODE_LINES)
        trimFont = try {
            ResourcesCompat.getFont(context, fontId)
        } catch (e: Exception) {
            Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        typedArray.recycle()
        viewMoreSpan = ReadMoreClickableSpan()
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        Log.i(TAG, "onAttachedToWindow: ")
        textWatcher = addTextChangedListener {
            val text = it?.toString() ?: return@addTextChangedListener
            if (text.isBlank()) return@addTextChangedListener
//            if(text.isBlank() || !text.last().isWhitespace()) return@addTextChangedListener
            var startIndex = 0
            val wordList = text.split("\\s+".toRegex())
//            Log.e("EDIT", wordList.toString())
            for (word in wordList) {
                if (word.startsWith("#")) {
                    val windex = text.indexOf(word, startIndex)
                    val separated = word.split("#").toTypedArray()
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(textView: View) {
                            //Toast.makeText(context, separated[1], Toast.LENGTH_LONG).show()
                            val fragment = this@ReadMoreTextView.findFragment<Fragment>()
                            fragment.findNavController().navigate(R.id.searchFragment, Bundle().apply {
                                putString(SearchFragment.SEARCH_KEYWORD, separated[1])
                            })
                        }
                    }
                    it.setSpan(clickableSpan, windex, windex + word.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    startIndex = windex + word.length
                }
                if(Patterns.WEB_URL.matcher(word).matches()) {
                    val windex = text.indexOf(word, startIndex)
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(textView: View) {
                            //Toast.makeText(context, word, Toast.LENGTH_LONG).show()
                            val fragment = this@ReadMoreTextView.findFragment<Fragment>()
                            if (fragment is CatchupDetailsFragment) {
                                fragment.mPref.isWebViewDialogOpened.postValue(true)
                            }
                            fragment.findNavController().navigate(R.id.htmlPageViewDialogInApp, Bundle().apply {
                                putString("myTitle", word)
                                putString("url", word)
                            })
                        }
                    }
                    it.setSpan(clickableSpan, windex, windex + word.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        onGlobalLayoutLineEndIndex()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        Log.i(TAG, "onDetachedFromWindow: ")
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }
    
    private val globalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
//            Log.i(TAG, "onGlobalLayout: $lineCount")
            val obs = viewTreeObserver
            if (lineCount > 0 && measuredHeight > 0 && measuredWidth > 0) {
                obs.removeOnGlobalLayoutListener(this)
                refreshLineEndIndex()
                setText()
            }
        }
    }
    
    private fun onGlobalLayoutLineEndIndex() {
//        Log.i(TAG, "onGlobalLayoutLineEndIndex: ")
        if (trimMode == TRIM_MODE_LINES) {
            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }
    
    private fun refreshLineEndIndex() {
        try {
            lineEndIndex = when (trimLines) {
                0 -> {
                    layout.getLineEnd(0)
                }
                in 1 until lineCount -> {
                    layout.getLineEnd(trimLines - 1)
                }
                else -> {
                    INVALID_END_INDEX
                }
            }
//            Log.i(TAG, "refreshLineEndIndex: ${layout.lineCount}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setText() {
//        Log.i(TAG, "setText-main: ${getTrimmedText()}")
        getTrimmedText()?.let {
            super.setText(it, bufferType)
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.RED
        }
    }
    
    override fun setText(text: CharSequence, type: BufferType) {
//        Log.i(TAG, "setText: ")
        this.mainText = text.trim()
//        this.mainText = text.toString().trim()/*.replace("\n".toRegex(), " ")*/
        bufferType = type
        setText()
    }
    
    private fun getTrimmedText(): CharSequence? {
//        Log.i(TAG, "getTrimmedText: ")
        if (trimMode == TRIM_MODE_LENGTH) {
            if (!mainText.isNullOrBlank() && mainText?.length ?: 0 > trimLength) {
                return if (readMore) {
                    updateCollapsedText()
                } else {
                    updateExpandedText()
                }
            }
        }
        if (trimMode == TRIM_MODE_LINES) {
            if (!mainText.isNullOrBlank() && lineEndIndex > 0) {
                if (readMore) {
                    val line = lineCount
                    if (line >= trimLines) {
                        return updateCollapsedText()
                    }
                } else {
                    return updateExpandedText()
                }
            }
        }
        return mainText
    }

    private fun updateCollapsedText(): CharSequence? {
//        Log.i(TAG, "updateCollapsedText: ")
        var trimEndIndex = mainText?.length ?: 0
        when (trimMode) {
            TRIM_MODE_LINES -> {
                trimEndIndex = lineEndIndex - (ELLIPSIZE.length + trimCollapsedText.length + 1)
                if (trimEndIndex < 45) {
                    trimEndIndex = lineEndIndex - 1
                }
            }
            TRIM_MODE_LENGTH -> trimEndIndex = trimLength + 1
        }
        mainText?.let {
            if (it.length > trimEndIndex) {
                val spannableText = SpannableStringBuilder(it, 0, trimEndIndex)
                    .append(ELLIPSIZE)
                    .append(trimCollapsedText)
                return addClickableSpan(spannableText, trimCollapsedText)
            }
        }
        return mainText
    }
    
    private fun updateExpandedText(): CharSequence? {
//        Log.i(TAG, "updateExpandedText: ")
        if (showTrimExpandedText) {
            val spannableText = SpannableStringBuilder(mainText, 0, mainText?.length ?: 0)
                .append("\n\n")
                .append(trimExpandedText.toString())
            return addClickableSpan(spannableText, trimExpandedText)
        }
        return mainText
    }
    
    private fun addClickableSpan(spannableText: SpannableStringBuilder, trimText: CharSequence): CharSequence {
        val startIndex = spannableText.length - trimText.length
        spannableText.setSpan(Standard(if (trimText == trimExpandedText) ALIGN_CENTER else ALIGN_NORMAL), startIndex, spannableText.length, Spanned
            .SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableText.setSpan(CustomTypefaceSpan(trimFont), startIndex, spannableText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableText.setSpan(viewMoreSpan, startIndex, spannableText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableText
    }
    
    private inner class ReadMoreClickableSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            readMore = !readMore
            setText()
        }
        
        override fun updateDrawState(ds: TextPaint) {
            ds.textSize = trimTextSize
            ds.color = colorClickableText
        }
    }
    
    fun setTrimLength(trimLength: Int) {
        this.trimLength = trimLength
        setText()
    }
    
    fun setColorClickableText(colorClickableText: Int) {
        this.colorClickableText = colorClickableText
    }
    
    fun setTrimCollapsedText(trimCollapsedText: CharSequence) {
        this.trimCollapsedText = trimCollapsedText
    }
    
    fun setTrimExpandedText(trimExpandedText: CharSequence) {
        this.trimExpandedText = trimExpandedText
    }
    
    fun setTrimMode(trimMode: Int) {
        this.trimMode = trimMode
    }
    
    fun setTrimLines(trimLines: Int) {
        this.trimLines = trimLines
    }
    
    fun setTrimTextSize(textSize: Float) {
        trimTextSize = textSize
    }
    
    inner class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {
        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeFace(ds, typeface)
        }
        
        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeFace(paint, typeface)
        }
        
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface?) {
            paint.typeface = tf
        }
    }
}