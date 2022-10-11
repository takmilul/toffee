package com.banglalink.toffee.ui.bubble.enums

import android.view.Gravity

/**
 * Enum class that represents the possible initial gravity placement of the draggable view when added to the window:
 *    TOP_LEFT: Gravitate towards the top-left corner
 *    TOP_RIGHT: Gravitate towards the top-right corner
 *    BOTTOM_LEFT: Gravitate towards the bottom-left corner
 *    BOTTOM_RIGHT: Gravitate towards the bottom-right corner
 */
enum class DraggableWindowItemGravity(val value: Int) {
    TOP_LEFT(Gravity.TOP or Gravity.LEFT),
    TOP_RIGHT(Gravity.TOP or Gravity.RIGHT),
    BOTTOM_LEFT(Gravity.BOTTOM or Gravity.LEFT),
    BOTTOM_RIGHT(Gravity.BOTTOM or Gravity.RIGHT),
    CENTER(Gravity.TOP or Gravity.CENTER)
}