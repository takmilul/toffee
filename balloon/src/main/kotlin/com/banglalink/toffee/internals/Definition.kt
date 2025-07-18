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

package com.banglalink.toffee.internals

/** definition of the non-value of Int type. */
@get:JvmSynthetic
internal const val NO_INT_VALUE: Int = Int.MIN_VALUE

/** definition of the non-value of Float type. */
@get:JvmSynthetic
internal const val NO_Float_VALUE: Float = 0f

/** definition of the non-value of Long type. */
@get:JvmSynthetic
internal const val NO_LONG_VALUE: Long = -1L

/** definition of the left-to-right value. */
@get:JvmSynthetic
internal const val LTR: Int = 1

/** definition of the boundary size between the content and the arrow. */
@get:JvmSynthetic
internal const val SIZE_ARROW_BOUNDARY: Int = 1

/** returns the negative of this value. */
@JvmSynthetic
internal fun Int.unaryMinus(predicate: Boolean): Int {
  return if (predicate) {
    unaryMinus()
  } else {
    this
  }
}

/** returns a nullable int value if the target int is not [NO_INT_VALUE]. */
@JvmSynthetic
internal fun Int.takeIfNotNoIntValue(): Int? {
  return this.takeIf { this != NO_INT_VALUE }
}
