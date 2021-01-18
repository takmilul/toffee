package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout.Alignment.ALIGN_CENTER
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AlignmentSpan.Standard
import android.text.style.ClickableSpan
import android.text.style.MetricAffectingSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.banglalink.toffee.R

class ReadMoreTextView constructor(context: Context, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {
    private var mainText: CharSequence? = null
    private var bufferType: BufferType? = null
    private var readMore = true
    private var trimLength: Int
    private var trimCollapsedText: CharSequence
    private var trimExpandedText: CharSequence
    private val viewMoreSpan: ReadMoreClickableSpan
    private var colorClickableText: Int
    private var trimTextSize: Float
    private var trimFont: Typeface? = null
    private val showTrimExpandedText: Boolean
    private var trimMode: Int
    private var lineEndIndex = 0
    private var trimLines: Int

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
        trimLength = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreLength, DEFAULT_TRIM_LENGTH)
        val resourceIdTrimCollapsedText = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreCollapsedText, R.string.read_more)
        val resourceIdTrimExpandedText = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreExpandedText, R.string.read_less)
        trimCollapsedText = resources.getString(resourceIdTrimCollapsedText)
        trimExpandedText = resources.getString(resourceIdTrimExpandedText)
        trimLines = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreTextLines, DEFAULT_TRIM_LINES)
        colorClickableText = typedArray.getColor(R.styleable.ReadMoreTextView_readMoreColorClickableText, ContextCompat.getColor(context, R.color.colorAccent))
        trimTextSize = typedArray.getDimension(R.styleable.ReadMoreTextView_readMoreTextSize, resources.getDimension(R.dimen.default_text_size))
        showTrimExpandedText = typedArray.getBoolean(R.styleable.ReadMoreTextView_readMoreExpandedText, DEFAULT_SHOW_TRIM_EXPANDED_TEXT)
        trimMode = typedArray.getInt(R.styleable.ReadMoreTextView_readMoreMode, TRIM_MODE_LINES)
        val fontId = typedArray.getResourceId(R.styleable.ReadMoreTextView_readMoreFontFamily, 0)
        trimFont = try {
            ResourcesCompat.getFont(context, fontId)
        }
        catch (e: Exception) {
            Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        typedArray.recycle()
        viewMoreSpan = ReadMoreClickableSpan()
        onGlobalLayoutLineEndIndex()
        setText()
    }

    private fun onGlobalLayoutLineEndIndex() {
        if (trimMode == TRIM_MODE_LINES) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val obs = viewTreeObserver
                    if (lineCount > 0) {
                        obs.removeOnGlobalLayoutListener(this)
                    }
                    refreshLineEndIndex()
                    setText()
                }
            })
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
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setText() {
        val text = displayableText()
        super.setText(text, bufferType)
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = Color.TRANSPARENT
    }

    private fun displayableText() = getTrimmedText(mainText)

    override fun setText(text: CharSequence, type: BufferType) {
        this.mainText = text.toString().replace("\n".toRegex(), " ")
        bufferType = type
        setText()
    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        if (trimMode == TRIM_MODE_LENGTH) {
            if (text != null && text.length > trimLength) {
                return if (readMore) {
                    updateCollapsedText()
                }
                else {
                    updateExpandedText()
                }
            }
        }
        if (trimMode == TRIM_MODE_LINES) {
            if (text != null && lineEndIndex > 0) {
                if (readMore) {
                    val line = layout.lineCount
                    if (line >= trimLines) {
                        return updateCollapsedText()
                    }
                }
                else {
                    return updateExpandedText()
                }
            }
        }
        return text
    }

    private fun updateCollapsedText(): CharSequence {
        var trimEndIndex = mainText!!.length
        when (trimMode) {
            TRIM_MODE_LINES -> {
                trimEndIndex = lineEndIndex - (ELLIPSIZE.length + trimCollapsedText.length + 1)
                if (trimEndIndex < 0) {
                    trimEndIndex = trimLength + 1
                }
            }
            TRIM_MODE_LENGTH -> trimEndIndex = trimLength + 1
        }
        val s = SpannableStringBuilder(mainText, 0, trimEndIndex)
            .append(ELLIPSIZE)
            .append(trimCollapsedText)
        return addClickableSpan(s, trimCollapsedText)
    }

    private fun updateExpandedText(): CharSequence? {
        if (showTrimExpandedText) {
            val s = SpannableStringBuilder(mainText, 0, mainText!!.length).append("\n").append(trimExpandedText.toString())
            return addClickableSpan(s, trimExpandedText)
        }
        return mainText
    }

    private fun addClickableSpan(s: SpannableStringBuilder, trimText: CharSequence): CharSequence {
        s.setSpan(Standard(ALIGN_CENTER), s.length - trimText.length, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(CustomTypefaceSpan(trimFont), s.length - trimText.length, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(viewMoreSpan, s.length - trimText.length, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
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