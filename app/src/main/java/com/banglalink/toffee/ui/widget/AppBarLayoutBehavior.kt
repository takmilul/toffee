package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior
import java.lang.reflect.Field

/**
 * https://github.com/yangchong211/YCBlogs/blob/master/android/%E5%A4%8D%E6%9D%82%E6%8E%A7%E4%BB%B6/14.%E8%87%AA%E5%AE%9A%E4%B9%89Behavior.md
 */

class AppBarLayoutBehavior(context: Context?, attrs: AttributeSet?) : Behavior(context, attrs) {
    
    private var isFlinging = false
    private var shouldBlockNestedScroll = false
    
    companion object {
        private const val TAG = "AppbarLayoutBehavior"
        private const val TYPE_FLING = 1
    }
    
    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: AppBarLayout, ev: MotionEvent): Boolean {
//        Log.d(TAG, "onInterceptTouchEvent:" + child.totalScrollRange)
        shouldBlockNestedScroll = isFlinging
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> stopAppbarLayoutFling(child) // Stop fling when your finger touches the screen
            else -> {}
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }// Possibly 28 or more versions// Support design 27 and the following version
    
    /**
     * Reflect to get private flingRunnable attributes, considering the problem of variable name modification after support 28
     * @return Field
     * @throws NoSuchFieldException
     */
    @get:Throws(NoSuchFieldException::class) private val flingRunnableField: Field?
        get() {
            val superclass: Class<*>? = this.javaClass.superclass
            return try {
                // Support design 27 and the following version
                var headerBehaviorType: Class<*>? = null
                if (superclass != null) {
                    headerBehaviorType = superclass.superclass.superclass
                }
                headerBehaviorType?.getDeclaredField("flingRunnable")
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
                // Possibly 28 or more versions
                val headerBehaviorType = superclass!!.superclass
                headerBehaviorType?.getDeclaredField("mFlingRunnable")
            }
        }// Possibly 28 or more versions// Support design 27 and the following version
    
    /**
     * Reflect to get private scroller attributes, considering the problem of variable name modification after support 28
     * @return Field
     * @throws NoSuchFieldException
     */
    @get:Throws(NoSuchFieldException::class) private val scrollerField: Field?
        get() {
            val superclass: Class<*>? = this.javaClass.superclass
            return try {
                // Support design 27 and the following version
                var headerBehaviorType: Class<*>? = null
                if (superclass != null) {
                    headerBehaviorType = superclass.superclass.superclass
                }
                headerBehaviorType?.getDeclaredField("scroller")
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
                // Possibly 28 or more versions
                val headerBehaviorType = superclass!!.superclass
                headerBehaviorType?.getDeclaredField("mScroller")
            }
        }
    
    /**
     * Stop appbarLayout's fling event
     * @param appBarLayout
     */
    private fun stopAppbarLayoutFling(appBarLayout: AppBarLayout) {
        // Get the flingRunnable variable in HeaderBehavior by reflection
        try {
            val flingRunnableField = flingRunnableField
            val scrollerField = scrollerField
            
            flingRunnableField?.let {
                it.isAccessible = true
                if(it[this] is Runnable) {
                    (it[this] as Runnable).let { flingRunnable ->
                        appBarLayout.removeCallbacks(flingRunnable)
                        it[this] = null
                    }
                }
            }
            scrollerField?.let {
                it.isAccessible = true
                if (it[this] is OverScroller) {
                    (it[this] as OverScroller).let { overScroller ->
                        if (!overScroller.isFinished) overScroller.abortAnimation()
                    }
                }
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
    
    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: AppBarLayout,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int
    ): Boolean {
//        Log.d(TAG, "onStartNestedScroll")
        stopAppbarLayoutFling(child)
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
    }
    
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
//        Log.d(TAG, "onNestedPreScroll:" + child.totalScrollRange + " ,dx:" + dx + " ,dy:" + dy + " ,type:" + type)
        // When type returns to 1, it indicates that the current target is in a non-touch sliding.
        // The bug is caused by the sliding of the NestedScrolling Child2 interface in Coordinator Layout when the AppBar is sliding
        // The subclass has not ended its own fling
        // So here we listen for non-touch sliding of subclasses, and then block the sliding event to AppBarLayout
        if (type == TYPE_FLING) {
            isFlinging = true
        }
        if (!shouldBlockNestedScroll) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }
    
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: AppBarLayout,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (!shouldBlockNestedScroll) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        }
    }
    
    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, abl: AppBarLayout, target: View, type: Int) {
        Log.d(TAG, "onStopNestedScroll")
        super.onStopNestedScroll(coordinatorLayout, abl, target, type)
        isFlinging = false
        shouldBlockNestedScroll = false
    }
    
    //    @Override
    //    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
    //                               View target, int dxConsumed, int dyConsumed, int
    //            dxUnconsumed, int dyUnconsumed, int type) {
    //        LogUtil.d(TAG, "onNestedScroll: target:" + target.getClass() + " ,"
    //                + child.getTotalScrollRange() + " ,dxConsumed:"
    //                + dxConsumed + " ,dyConsumed:" + dyConsumed + " " + ",type:" + type);
    //        if (!shouldBlockNestedScroll) {
    //            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed,
    //                    dyConsumed, dxUnconsumed, dyUnconsumed, type);
    //        }
    //    }
}