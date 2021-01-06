package com.banglalink.toffee.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

import java.lang.reflect.Field;

/**
 * https://github.com/yangchong211/YCBlogs/blob/master/android/%E5%A4%8D%E6%9D%82%E6%8E%A7%E4%BB%B6/14.%E8%87%AA%E5%AE%9A%E4%B9%89Behavior.md
 */

public class AppBarLayoutBehavior extends AppBarLayout.Behavior {

    private static final String TAG = "AppbarLayoutBehavior";
    private static final int TYPE_FLING = 1;
    private boolean isFlinging;
    private boolean shouldBlockNestedScroll;

    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        LogUtil.d(TAG, "onInterceptTouchEvent:" + child.getTotalScrollRange());
        shouldBlockNestedScroll = isFlinging;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Stop fling when your finger touches the screen
                stopAppbarLayoutFling(child);
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    /**
     * Reflect to get private flingRunnable attributes, considering the problem of variable name modification after support 28
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getFlingRunnableField() throws NoSuchFieldException {
        Class<?> superclass = this.getClass().getSuperclass();
        try {
            // Support design 27 and the following version
            Class<?> headerBehaviorType = null;
            if (superclass != null) {
                headerBehaviorType = superclass.getSuperclass().getSuperclass();
            }
            if (headerBehaviorType != null) {
                return headerBehaviorType.getDeclaredField("flingRunnable");
            }else {
                return null;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // Possibly 28 or more versions
            Class<?> headerBehaviorType = superclass.getSuperclass();
            if (headerBehaviorType != null) {
                return headerBehaviorType.getDeclaredField("mFlingRunnable");
            } else {
                return null;
            }
        }
    }

    /**
     * Reflect to get private scroller attributes, considering the problem of variable name modification after support 28
     * @return Field
     * @throws NoSuchFieldException
     */
    private Field getScrollerField() throws NoSuchFieldException {
        Class<?> superclass = this.getClass().getSuperclass();
        try {
            // Support design 27 and the following version
            Class<?> headerBehaviorType = null;
            if (superclass != null) {
                headerBehaviorType = superclass.getSuperclass().getSuperclass();
            }
            if (headerBehaviorType != null) {
                return headerBehaviorType.getDeclaredField("scroller");
            }else {
                return null;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            // Possibly 28 or more versions
            Class<?> headerBehaviorType = superclass.getSuperclass();
            if (headerBehaviorType != null) {
                return headerBehaviorType.getDeclaredField("mScroller");
            }else {
                return null;
            }
        }
    }

    /**
     * Stop appbarLayout's fling event
     * @param appBarLayout
     */
    private void stopAppbarLayoutFling(AppBarLayout appBarLayout) {
        // Get the flingRunnable variable in HeaderBehavior by reflection
        try {
            Field flingRunnableField = getFlingRunnableField();
            Field scrollerField = getScrollerField();
            if (flingRunnableField != null) {
                flingRunnableField.setAccessible(true);
            }
            if (scrollerField != null) {
                scrollerField.setAccessible(true);
            }
            Runnable flingRunnable = null;
            if (flingRunnableField != null) {
                flingRunnable = (Runnable) flingRunnableField.get(this);
            }
            OverScroller overScroller = (OverScroller) scrollerField.get(this);
            if (flingRunnable != null) {
                LogUtil.d (TAG,"Flying Runnable");
                appBarLayout.removeCallbacks(flingRunnable);
                flingRunnableField.set(this, null);
            }
            if (overScroller != null && !overScroller.isFinished()) {
                overScroller.abortAnimation();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child,
                                       View directTargetChild, View target,
                                       int nestedScrollAxes, int type) {
        LogUtil.d(TAG, "onStartNestedScroll");
        stopAppbarLayoutFling(child);
        return super.onStartNestedScroll(parent, child, directTargetChild, target,
                nestedScrollAxes, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout,
                                  AppBarLayout child, View target,
                                  int dx, int dy, int[] consumed, int type) {
        LogUtil.d(TAG, "onNestedPreScroll:" + child.getTotalScrollRange()
                + " ,dx:" + dx + " ,dy:" + dy + " ,type:" + type);
        // When type returns to 1, it indicates that the current target is in a non-touch sliding.
        // The bug is caused by the sliding of the NestedScrolling Child2 interface in Coordinator Layout when the AppBar is sliding
        // The subclass has not ended its own fling
        // So here we listen for non-touch sliding of subclasses, and then block the sliding event to AppBarLayout
        if (type == TYPE_FLING) {
            isFlinging = true;
        }
        if (!shouldBlockNestedScroll) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, @NonNull AppBarLayout child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed, int type, int[] consumed) {
        if(!shouldBlockNestedScroll) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        }
    }
//
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

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout abl,
                                   View target, int type) {
        LogUtil.d(TAG, "onStopNestedScroll");
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
        isFlinging = false;
        shouldBlockNestedScroll = false;
    }

    private static class LogUtil{
        static void d(String tag, String string){
//            Log.d(tag,string);
        }
    }

}