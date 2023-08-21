/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.banglalink.toffee.vectortext

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.banglalink.toffee.balloon.R
import com.banglalink.toffee.extensions.applyDrawable
import com.banglalink.toffee.internals.NO_INT_VALUE
import com.banglalink.toffee.internals.takeIfNotNoIntValue

/** VectorTextView is a customizable textView having a vector icon.  */
public class VectorTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
) : AppCompatTextView(context, attrs) {
    public var drawableTextViewParams: VectorTextViewParams? = null
        set(value) {
            field = value?.also { applyDrawable(it) }
        }
    
    init {
        initAttrs(context, attrs)
    }
    
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.VectorTextView)
            drawableTextViewParams = VectorTextViewParams(
                drawableStartRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableStart,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                drawableEndRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableEnd,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                drawableBottomRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableBottom,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                drawableTopRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableTop,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                compoundDrawablePaddingRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawablePadding,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                tintColor = attributeArray.getColor(
                    R.styleable.VectorTextView_balloon_drawableTintColor,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                widthRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableWidth,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                heightRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableHeight,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue(),
                squareSizeRes = attributeArray.getResourceId(
                    R.styleable.VectorTextView_balloon_drawableSquareSize,
                    NO_INT_VALUE
                ).takeIfNotNoIntValue()
            )
            attributeArray.recycle()
        }
    }
    
    public fun isRtlSupport(rtlLayout: Boolean) {
        drawableTextViewParams?.apply {
            isRtlLayout = rtlLayout
            applyDrawable(this)
        }
    }
}
