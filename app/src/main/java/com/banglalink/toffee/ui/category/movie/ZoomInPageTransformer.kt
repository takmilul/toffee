package com.banglalink.toffee.ui.category.movie

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.banglalink.toffee.util.Utils
import kotlin.math.abs

/**
 * https://stackoverflow.com/questions/10098040/android-viewpager-show-preview-of-page-on-left-and-right
 */

class ZoomInPageTransformer : ViewPager2.PageTransformer {
//    val nextItemVisiblePx = Utils.dpToPx(24)
//    val currentItemHorizontalMarginPx = Utils.dpToPx(24)
    private val pageTranslationX = Utils.dpToPx(48) //nextItemVisiblePx + currentItemHorizontalMarginPx
    private val DIFF_SCALE = 0.28f

    override fun transformPage(view: View, position: Float) {
        view.translationX = -pageTranslationX * position
        // Next line scales the item's height. You can remove it if you don't want this effect

        (1 - (DIFF_SCALE * abs(position))).let {
            view.scaleX = it
            view.scaleY = it
        }
    }
}