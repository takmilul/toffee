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

package com.banglalink.toffee.radius

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Px
import com.banglalink.toffee.internals.viewProperty

/**
 * RadiusLayout clips four directions of inner layouts depending on the radius size.
 */
public class RadiusLayout @JvmOverloads constructor(
  context: Context,
  attr: AttributeSet? = null,
  defStyle: Int = 0
) : FrameLayout(context, attr, defStyle) {

  /** path for smoothing the container's corner. */
  private val path = Path()

  /** corner radius for the clipping corners. */
  @get:Px
  public var radius: Float by viewProperty(0f)

  override fun onSizeChanged(
    w: Int,
    h: Int,
    oldw: Int,
    oldh: Int
  ) {
    super.onSizeChanged(w, h, oldw, oldh)
    path.apply {
      addRoundRect(
        RectF(0f, 0f, w.toFloat(), h.toFloat()),
        radius,
        radius,
        Path.Direction.CW
      )
    }
  }

  override fun dispatchDraw(canvas: Canvas) {
    canvas.clipPath(path)
    super.dispatchDraw(canvas)
  }
}
