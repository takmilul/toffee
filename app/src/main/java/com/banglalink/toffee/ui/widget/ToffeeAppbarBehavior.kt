package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_catchup.view.*

class ToffeeAppbarBehavior @JvmOverloads constructor(ctx: Context, attrib: AttributeSet? = null): AppBarLayout.Behavior(ctx, attrib) {

    private var overScroller: OverScroller? = null

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout,
                                  child: AppBarLayout,
                                  target: View,
                                  velocityX: Float,
                                  velocityY: Float): Boolean {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        abl: AppBarLayout,
        target: View,
        type: Int
    ) {
        stopAppBarLayoutFling()
        if(target is NestedScrollView) {
            target.smoothScrollTo(0, 0)
        }
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
    }

    private fun stopAppBarLayoutFling() {
        if (overScroller == null) {
            val scrollerField = javaClass.superclass?.superclass?.superclass?.getDeclaredField("scroller")
            scrollerField?.isAccessible = true
            overScroller = scrollerField?.get(this) as? OverScroller
        }
        overScroller?.forceFinished(true)
    }

}