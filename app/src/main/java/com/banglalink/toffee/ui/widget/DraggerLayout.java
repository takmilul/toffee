package com.banglalink.toffee.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.banglalink.toffee.R;
import com.banglalink.toffee.analytics.ToffeeAnalytics;
import com.banglalink.toffee.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shantanu on 12/9/16.
 */

public class DraggerLayout extends RelativeLayout {
    private int dragViewId;
    private int bottomViewId;
    private View dragView;
    private View bottomView;
    private int lastAction;
    private float dY2;
    private float dY;

    private int bottomMargin = Utils.dpToPx(52 + 56*2);

    private ViewDragHelper viewDragHelper;
    private float scaleFactor = 2.5f;
    private static final int INVALID_POINTER = -1;
    private int activePointerId = INVALID_POINTER;
    private DraggableViewCallback dragableViewCallback;
    private int mVerticalDragRange;
    private int mHorizontalDragRange;
    private int mTop;
    private int mLeft;
    private List<OnPositionChangedListener> onPositionChangedListenerList = new ArrayList<>();

    public DraggerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeAttributes(attrs);
    }
    public DraggerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(attrs);
    }
    public DraggerLayout(Context context) {
        super(context);
    }

    private void initializeAttributes(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.dragger_layout);
        this.dragViewId =  attributes.getResourceId(R.styleable.dragger_layout_top_view_id, 0);
        this.bottomViewId = attributes.getResourceId(R.styleable.dragger_layout_bottom_view_id, 0);
        attributes.recycle();
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dragView = findViewById(dragViewId);
        bottomView = findViewById(bottomViewId);
        initializeViewDragHelper();
    }

    private void initializeViewDragHelper() {
        dragableViewCallback = new DraggableViewCallback(this);
        viewDragHelper = ViewDragHelper.create(this, 1f, dragableViewCallback);
    }

    public void minimize() {
        smoothSlideTo(1f);

        int height = getHeight();
        for(OnPositionChangedListener onPositionChangedListener : onPositionChangedListenerList){
            if(onPositionChangedListener != null){
                onPositionChangedListener.onViewMinimize();
            }
        }
    }

    public void resetImmediately() {
        dragableViewCallback.onViewPositionChanged(dragView,0,0,0,0);
        dragView.setBackgroundColor(Color.BLACK);
        requestLayout();
    }

    public void maximize() {
        smoothSlideTo(0f);

        for(OnPositionChangedListener onPositionChangedListener : onPositionChangedListenerList){
            if(onPositionChangedListener != null){
                onPositionChangedListener.onViewMaximize();
            }
        }
    }

    private boolean smoothSlideTo(float slideOffset) {
        final int topBound = getPaddingTop();
        int x = 0;//(int) (slideOffset * (getWidth() - transformer.getMinWidthPlusMarginRight()));
        int y = (int) (topBound + slideOffset * mVerticalDragRange);
        if (viewDragHelper.smoothSlideViewTo(dragView, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    public boolean isMaximized() {
        return (dragView.getScaleX() == 1.0f);
    }

    public boolean isMinimize() {
        return dragView.getScaleX() == 0.5f;
    }

    public boolean shouldMaximize(){
        return  isMinimize() && !isHorizontalDragged();
    }

    public boolean isHorizontalDragged(){
        return dragView.getRight() != (getRight() - getPaddingRight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(viewDragHelper.shouldInterceptTouchEvent(ev) || (isMinimize() && isViewHit(dragView,(int)ev.getX(),(int)ev.getY()))){
            Log.e("intercpting","true");
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
    long duration;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try{
            if(isViewHit(dragView,(int)ev.getX(),(int)ev.getY()) || ((!isMaximized() && !isMinimize()) || isHorizontalDragged())) {
                switch (ev.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastAction = MotionEvent.ACTION_DOWN;
                        duration = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN && (System.currentTimeMillis() - duration < 250) && shouldMaximize()) {
                            maximize();
                        }
                        break;
                }
                if (isMaximized() && (System.currentTimeMillis() - duration < 250)) {
                    dragView.dispatchTouchEvent(ev);
                }
                viewDragHelper.processTouchEvent(ev);
                return true;
            }
        }catch (IllegalArgumentException e){
            ToffeeAnalytics.INSTANCE.logException(e);
        }


        return false;
    }

    private boolean isViewHit(View view, int x, int y) {
        int[] viewLocation = new int[2];
        view.getLocationOnScreen(viewLocation);
        int[] parentLocation = new int[2];
        this.getLocationOnScreen(parentLocation);
        int screenX = parentLocation[0] + x;
        int screenY = parentLocation[1] + y;
        return screenX >= viewLocation[0]
                && screenX < viewLocation[0] + view.getWidth()
                && screenY >= viewLocation[1]
                && screenY < viewLocation[1] + view.getHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mVerticalDragRange = getHeight() - dragView.getHeight();
        mHorizontalDragRange = getWidth() - dragView.getWidth();
//        super.onLayout(changed,l,t,r,b);
        dragView.layout(
                mLeft,
                mTop,
                mLeft + dragView.getMeasuredWidth(),
                mTop + dragView.getMeasuredHeight());

        bottomView.layout(
                mLeft,
                mTop + dragView.getMeasuredHeight(),
                mLeft + bottomView.getMeasuredWidth(),
                mTop  + b);
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public class DraggableViewCallback extends ViewDragHelper.Callback{
        private final View parent;
        private int newtop = 0;
        DraggableViewCallback(View parent){
            this.parent = parent;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(isHorizontalDragged()){
                if((getRight() - getPaddingRight() - dragView.getRight()) > getRight() / 5){
                    if (viewDragHelper.smoothSlideViewTo(dragView, 0 - (getRight() - getPaddingRight()), newtop)) {
                        ViewCompat.postInvalidateOnAnimation(parent);
                    }

                    for(OnPositionChangedListener onPositionChangedListener : onPositionChangedListenerList){
                        if(onPositionChangedListener != null){
                            onPositionChangedListener.onViewDestroy();
                        }
                    }
                    dragView.setScaleX(1.0f);
                    dragView.setScaleY(1.0f);
                }
                else {
                    minimize();
                }
            }
            else{
                if(dragView.getScaleX() > .75 && dragView.getScaleX() <= 1.f){
                    maximize();
                }
                else if(dragView.getScaleX() <= .75 && dragView.getScaleX() >= .5){
                    minimize();
                }
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mVerticalDragRange;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mHorizontalDragRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mTop = top;
            mLeft = left;
            if(!isHorizontalDragged()) {
                int bottomBound = parent.getHeight() - dragView.getHeight() - parent.getPaddingBottom();
                if (bottomBound != 0) {
                    int colorValue = (256 - (top * 256 / bottomBound));
                    parent.setBackgroundColor(Color.argb(colorValue, colorValue, colorValue, colorValue));
                    float scale = 1.0f - (0.50f * (top * 100f / bottomBound) / 100.0f);
                    dragView.setPivotX(dragView.getWidth() - 38);
                    dragView.setPivotY(dragView.getHeight() - bottomMargin);
                    int padding = (int) (20 - 20 * scale);
                    dragView.setPadding(padding, padding, padding, padding);
                    if (scale == .5f) {
                        dragView.setBackgroundColor(Color.WHITE);
                    } else {
                        dragView.setBackgroundColor(Color.BLACK);
                    }
                    dragView.setScaleX(scale);
                    dragView.setScaleY(scale);
                }
            }
            requestLayout();
        }



        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if(isHorizontalDragged()){
                return getPaddingTop() + mVerticalDragRange;
            }
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - dragView.getHeight();
            newtop =  Math.min(Math.max(top, topBound), bottomBound);
            return newtop;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(isMinimize()){
                if(left > 0) {
                    return 0;
                }
                else {
                    return left;
                }
            }
            return 0;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child.equals(dragView);
        }
    }

    public void addOnPositionChangedListener(OnPositionChangedListener onPositionChangedListener){
        this.onPositionChangedListenerList.add(onPositionChangedListener);
    }

    public interface OnPositionChangedListener{
        public void onViewMinimize();
        public void onViewMaximize();
        public void onViewDestroy();
    }
}
