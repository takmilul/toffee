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

package com.banglalink.toffee.animations

/**
 * BalloonRotateDirection decides the direction of the balloon rotation animation.
 * This class can be used with the [BalloonRotateAnimation].
 */
public enum class BalloonRotateDirection(public val value: Int) {
  /** Rotate in the right direction. */
  RIGHT(1),

  /** Rotate in the left direction. */
  LEFT(-1);
}
