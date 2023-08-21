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

package com.banglalink.toffee

/** ArrowOrientation determines the orientation of the arrow. */
public enum class ArrowOrientation {
  BOTTOM,
  TOP,
  START,
  END;

  internal companion object {
    /** Return [ArrowOrientation] depending on the [isRtlLayout].] */
    internal fun ArrowOrientation.getRTLSupportOrientation(isRtlLayout: Boolean): ArrowOrientation {
      return if (!isRtlLayout) {
        this
      } else {
        when (this) {
          START -> END
          END -> START
          else -> this
        }
      }
    }
  }
}
