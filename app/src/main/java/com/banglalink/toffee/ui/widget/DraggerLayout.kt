package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics.logException
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.px
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class DraggerLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
):RelativeLayout(context, attrs, defStyle) {
    
    private var mTop = 0
    private var mLeft = 0
    private var lastAction = 0
    private var dragViewId = 0
    private var bottomViewId = 0
    private var mVerticalDragRange = 0
    private var mHorizontalDragRange = 0
    private lateinit var bottomView: View
    private val bottomMargin = (52 + 72).px
    @Inject lateinit var mPref: SessionPreference
    private lateinit var viewDragHelper: ViewDragHelper
    private lateinit var dragView: ToffeeStyledPlayerView
    private lateinit var draggableViewCallback: DraggableViewCallback
    private val onPositionChangedListenerList: MutableList<OnPositionChangedListener> = ArrayList()

    init {
        initializeAttributes(attrs)
    }

    private fun initializeAttributes(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.dragger_layout)
        dragViewId = attributes.getResourceId(R.styleable.dragger_layout_top_view_id, 0)
        bottomViewId = attributes.getResourceId(R.styleable.dragger_layout_bottom_view_id, 0)
        attributes.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        dragView = findViewById(dragViewId)
        bottomView = findViewById(bottomViewId)
        initializeViewDragHelper()
    }

    private fun initializeViewDragHelper() {
        draggableViewCallback = DraggableViewCallback(this)
        viewDragHelper = ViewDragHelper.create(this, 1f, draggableViewCallback)
    }

    fun minimize() {
        if(smoothSlideTo(1f)) {
            onPositionChangedListenerList.forEach {
                it.onPlayerMinimize()
            }
        }
    }

    fun resetImmediately() {
        draggableViewCallback.onViewPositionChanged(dragView, 0, 0, 0, 0)
        dragView.setBackgroundColor(Color.BLACK)
        requestLayout()
    }

    fun maximize() {
        if(smoothSlideTo(0f)) {
            onPositionChangedListenerList.forEach {
                it.onPlayerMaximize()
            }
        }
    }

    private fun smoothSlideTo(slideOffset: Float): Boolean {
        val topBound = paddingTop
        val x = 0 //(int) (slideOffset * (getWidth() - transformer.getMinWidthPlusMarginRight()));
        val y = (topBound + slideOffset * mVerticalDragRange).toInt()
        if (viewDragHelper.smoothSlideViewTo(dragView, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    fun getMaxScale() = 1.0f
    fun getMidScale() = 0.98f//(getMaxScale() + getMinScale()) / 2.0f
    fun getMinScale() = 0.5f//if(dragView.isVideoPortrait) 0.25f else 0.5f

    fun isMaximized() = dragView.scaleX == getMaxScale()
    fun isMinimize() = dragView.scaleX == getMinScale()
    fun isClamped() = dragView.isClamped()

    private fun shouldMaximize(): Boolean {
        return isMinimize() && !isHorizontalDragged()
    }

    fun isHorizontalDragged() = dragView.right != right - paddingRight

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var scrollDir = 0
    private var lastScrollY = 0f
    private var isScrollCaptured = false
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val ts = measureTimeMillis {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        }
//        Log.e("MEASURE_T", "onMeasure_DraggerLayout ->> $ts")
//    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if(dragView.isFullScreenPortrait()) return false

        val bottomHit = isBottomHit(ev)
        when(ev.actionMasked) {
            MotionEvent.ACTION_DOWN-> {
                lastScrollY = ev.y
//                Log.e("SCROLL", "ACTION_DOWN ->>> $bottomHit")
                dragView.handleTouchDown2(ev)
                isScrollCaptured = false
            }
            MotionEvent.ACTION_MOVE-> {
                if(isScrollCaptured) return true

                val scrollDiff = ev.y - lastScrollY
                if(scrollDiff > mTouchSlop) {
                    scrollDir = 1
                }
                if(scrollDiff < -mTouchSlop) {
                    scrollDir = -1
                }
//                Log.e("SCROLL", "ScrollDir ->> $scrollDir, ---->>> ${canScrollBottomPanel()}")
                if(bottomHit){
//                    if(dragView.isFullHeight() && scrollDir < 0) {
                    if(dragView.layoutParams.height > dragView.minBound && scrollDir < 0) {
                        isScrollCaptured = true
                        return true
                    }
                    if(dragView.layoutParams.height < dragView.maxBound && scrollDir > 0 && !canScrollBottomPanel()) {
                        isScrollCaptured = true
                        return true
                    }
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP-> {
//                Log.e("SCROLL", "ACTION_UP ->>> $bottomHit")
                lastScrollY = 0f
                scrollDir = 0
//                Log.e("CLAMP", "Clamp")
                dragView.clampOrFullHeight()
                isScrollCaptured = false
            }
        }

        if(bottomHit) return false

        dragView.getDebugOverLay()?.let {
            if(isViewHit(it, ev.x.toInt(), ev.y.toInt())) return false
        }

        if (mPref.isEnableFloatingWindow && viewDragHelper.shouldInterceptTouchEvent(ev) || isMinimize() && isViewHit(dragView, ev.x.toInt(), ev.y.toInt())) {
            return true
        }

        return super.onInterceptTouchEvent(ev)
    }

    private fun isBottomHit(ev: MotionEvent): Boolean {
        return isViewHit(bottomView, ev.x.toInt(), ev.y.toInt())
    }

    var duration: Long = 0
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if(dragView.isFullScreenPortrait()) return false
        
        try {
            if(isBottomHit(ev) && (isMinimize() || isMaximized()) && !isHorizontalDragged()) {
//                if(ev.actionMasked == MotionEvent.ACTION_UP) Log.e("SCROLL", "ACTION_UP2")
                return dragView.handleTouchEvent(ev)
            }
            if (isViewHit(dragView, ev.x.toInt(), ev.y.toInt()) || !isMaximized() && !isMinimize() || isHorizontalDragged()) {
                when (ev.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastAction = MotionEvent.ACTION_DOWN
                        duration = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_UP -> if (lastAction == MotionEvent.ACTION_DOWN && System.currentTimeMillis() - duration < 250 && shouldMaximize()) {
                        maximize()
                    }
                }
                if (isMaximized() && System.currentTimeMillis() - duration < 250) {
                    dragView.dispatchTouchEvent(ev)
                }
                viewDragHelper.processTouchEvent(ev)
                return true
            }
        } catch (e: IllegalArgumentException) {
            logException(e)
        }
        return false
    }

    private fun canScrollBottomPanel(): Boolean {
        findBottomRecycler()?.let {
            if(it.layoutManager is LinearLayoutManager) {
                (it.layoutManager as LinearLayoutManager).let { lm->
                    val pos = lm.findFirstVisibleItemPosition()
                    if(pos == 0 && lm.findViewByPosition(pos)?.top == 0) {
                        return false
                    }
                }
            } else if(it.layoutManager is StickyHeaderGridLayoutManager) {
                (it.layoutManager as StickyHeaderGridLayoutManager).let { lm->
                    val pos = lm.getFirstVisibleHeaderPosition(true)
                    if(pos == 0 && lm.findViewByPosition(pos)?.top == 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun findBottomRecycler(): RecyclerView? {
        return bottomView.findViewById(R.id.listview)
    }

    private fun isViewHit(view: View?, x: Int, y: Int): Boolean {
        val viewLocation = IntArray(2)
        view!!.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.width && screenY >= viewLocation[1] && screenY < viewLocation[1] + view.height
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mVerticalDragRange = height - dragView.minBound
        mHorizontalDragRange = width - dragView.width
        //        super.onLayout(changed,l,t,r,b);
        dragView.layout(mLeft, mTop, mLeft + dragView.measuredWidth, mTop + dragView.measuredHeight)
        bottomView.layout(mLeft, mTop + dragView.measuredHeight, mLeft + bottomView.measuredWidth, mTop + b)
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun destroyView() {
        if (viewDragHelper.smoothSlideViewTo(dragView, 0 - (right - paddingRight), 0)) {
            parent?.let {
                if(it is View) ViewCompat.postInvalidateOnAnimation(it)
            }
        }
        onPositionChangedListenerList.forEach {
            it.onPlayerDestroy()
        }
        dragView.scaleX = getMaxScale()
        dragView.scaleY = getMaxScale()
    }

    inner class DraggableViewCallback constructor(private val parent: View) :
        ViewDragHelper.Callback() {
        private var newtop = 0
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (isHorizontalDragged()) {
                if (right - paddingRight - dragView.right > right / 5) {
                    if (viewDragHelper.smoothSlideViewTo(dragView, 0 - (right - paddingRight), parent.height - dragView.height)) {
                        ViewCompat.postInvalidateOnAnimation(parent)
                    }
                    onPositionChangedListenerList.forEach {
                        it.onPlayerDestroy()
                    }
                    dragView.scaleX = getMinScale()
                    dragView.scaleY = getMinScale()
                } else {
                    minimize()
                }
            } else {
                if (dragView.scaleX > getMidScale() && dragView.scaleX <= getMaxScale()) {
                    maximize()
                } else if (dragView.scaleX in getMinScale() .. getMidScale()) {
                    minimize()
                }
            }
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return mVerticalDragRange
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return mHorizontalDragRange
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            mTop = top
            mLeft = left
            if (!isHorizontalDragged()) {
                val bottomBound = parent.height - dragView.minBound //dragView.height - parent.paddingBottom
                if (bottomBound != 0) {
                    val colorValue = 256 - top * 256 / bottomBound
                    parent.setBackgroundColor(
                        Color.argb(
                            colorValue,
                            colorValue,
                            colorValue,
                            colorValue
                        )
                    )
                    val scale = (top * (getMinScale() - getMaxScale())) / bottomBound + getMaxScale()
                    dragView.pivotX = (dragView.width - 38).toFloat()
                    dragView.pivotY = (dragView.height - bottomMargin).toFloat()
                    val padding = (20 - 20 * scale).toInt()
                    dragView.setPadding(padding, padding, padding, padding)
                    if (scale == getMinScale()) {
                        dragView.setBackgroundColor(Color.WHITE)
                    } else {
                        dragView.setBackgroundColor(Color.BLACK)
                    }

//                    val initX = 2 * dragView.minBound - capturedEnd
                    if(dragView.isVideoScalable) {
                        val heightDiff2 =
                            (scale - getMaxScale()) * (dragView.minBound - (capturedEnd
                                ?: dragView.minBound)) / (getMinScale() - getMaxScale()) + (capturedEnd
                                ?: dragView.minBound)//initX + scale * (capturedEnd - initX)
                        if (heightDiff2 > 0) {
                            dragView.setLayoutHeight(heightDiff2.toInt())
                        }
                    }
                    dragView.scaleX = scale
                    dragView.scaleY = scale
                    if(scale == getMaxScale() || scale == getMinScale()) {
                        onScaleToBoundary(scale)
                    }
                }
            } else {
                if(dragView.right < 5) {
                    dragView.scaleX = getMaxScale()
                    dragView.scaleY = getMaxScale()
                    dragView.left = -dragView.width
                    dragView.top = 0
                }
            }
            requestLayout()
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            if (isHorizontalDragged()) {
                return paddingTop + mVerticalDragRange
            }
            val topBound = paddingTop
            val bottomBound = height - dragView.height
            newtop = min(max(top, topBound), bottomBound)
            return newtop
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (isMinimize()) {
                if (left > 0) {
                    0
                } else {
                    left
                }
            } else 0
        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == dragView
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            super.onViewCaptured(capturedChild, activePointerId)
            capturedEnd = if(dragView.scaleX == 0.5f) {
                dragView.maxBound
            } else {
                dragView.layoutParams.height
            }
        }
    }

    private var capturedEnd: Int? = null

    fun onScaleToBoundary(scale: Float) {
        if(dragView.isVideoScalable
            && scale == getMaxScale()
            && dragView.layoutParams.height != dragView.maxBound
        ) {
//            dragView.setLayoutHeight(dragView.maxBound)
            dragView.setHeightWithAnim(dragView.maxBound, 100)
        }
    }

    fun addOnPositionChangedListener(onPositionChangedListener: OnPositionChangedListener) {
        onPositionChangedListenerList.add(onPositionChangedListener)
    }

    interface OnPositionChangedListener {
        fun onPlayerMinimize()
        fun onPlayerMaximize()
        fun onPlayerDestroy()
    }
}