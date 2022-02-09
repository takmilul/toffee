package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import com.banglalink.toffee.ui.widget.StickyHeaderGridLayoutManager.HeaderState.*
import com.banglalink.toffee.util.Log.e
import com.banglalink.toffee.util.Log.i
import java.util.*
import kotlin.math.abs

/**
 * Created by Sergej Kravcenko on 4/24/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */
class StickyHeaderGridLayoutManager(private val mSpanCount: Int) : LayoutManager(), ScrollVectorProvider {
    private var mSpanSizeLookup: SpanSizeLookup? = DefaultSpanSizeLookup()
    private var mAdapter: StickyHeaderGridAdapter? = null
    private var mHeadersStartPosition = 0
    private var mFloatingHeaderView: View? = null
    private var mFloatingHeaderPosition = 0
    private var mStickOffset = 0
    private var mAverageHeaderHeight = 0
    private var mHeaderOverlapMargin: Int
    /**
     * Returns the current [StickyHeaderGridLayoutManager.HeaderStateChangeListener] used by the StickyHeaderGridLayoutManager.
     *
     * @return The current [StickyHeaderGridLayoutManager.HeaderStateChangeListener] used by the StickyHeaderGridLayoutManager.
     */
    /**
     * Sets the listener to receive header state changes.
     *
     * @param listener [StickyHeaderGridLayoutManager.HeaderStateChangeListener] instance to be used to receive header
     * state changes
     */
    private var headerStateChangeListener: HeaderStateChangeListener? = null
    private var mStickyHeaderSection = NO_POSITION
    private var mStickyHeaderView: View? = null
    private var mStickyHeadeState: HeaderState? = null
    private val mFillViewSet: Array<View?>
    private var mPendingSavedState: SavedState? = null
    private var mPendingScrollPosition = NO_POSITION
    private var mPendingScrollPositionOffset = 0
    private val mAnchor = AnchorPosition()
    private val mFillResult = FillResult()
    private val mLayoutRows = ArrayList<LayoutRow>(DEFAULT_ROW_COUNT)
    
    enum class HeaderState {
        NORMAL, STICKY, PUSHED
    }
    
    /**
     * The interface to be implemented by listeners to header events from this
     * LayoutManager.
     */
    interface HeaderStateChangeListener {
        /**
         * Called when a section header state changes. The position can be HeaderState.NORMAL,
         * HeaderState.STICKY, HeaderState.PUSHED.
         *
         *
         *
         *
         *  * NORMAL - the section header is invisible or has normal position
         *  * STICKY - the section header is sticky at the top of RecyclerView
         *  * PUSHED - the section header is sticky and pushed up by next header
         *
         */
        fun onHeaderStateChanged(section: Int, headerView: View?, state: HeaderState?, pushOffset: Int)
    }
    /**
     * Returns the current [StickyHeaderGridLayoutManager.SpanSizeLookup] used by the StickyHeaderGridLayoutManager.
     *
     * @return The current [StickyHeaderGridLayoutManager.SpanSizeLookup] used by the StickyHeaderGridLayoutManager.
     */
    /**
     * Sets the source to get the number of spans occupied by each item in the adapter.
     *
     * @param spanSizeLookup [StickyHeaderGridLayoutManager.SpanSizeLookup] instance to be used to query number of spans
     * occupied by each item
     */
    var spanSizeLookup: SpanSizeLookup?
        get() = mSpanSizeLookup
        set(spanSizeLookup) {
            mSpanSizeLookup = spanSizeLookup
            if (mSpanSizeLookup == null) {
                mSpanSizeLookup = DefaultSpanSizeLookup()
            }
        }
    
    /**
     * Sets the size of header bottom margin that overlaps first section item. Used to create header bottom edge shadows.
     *
     * @param bottomMargin Size of header bottom margin in pixels
     */
    fun setHeaderBottomOverlapMargin(bottomMargin: Int) {
        mHeaderOverlapMargin = bottomMargin
    }
    
    override fun onAdapterChanged(oldAdapter: Adapter<*>?, newAdapter: Adapter<*>?) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        mAdapter = try {
            newAdapter as StickyHeaderGridAdapter?
        } catch (e: ClassCastException) {
            throw ClassCastException("Adapter used with StickyHeaderGridLayoutManager must be kind of StickyHeaderGridAdapter")
        }
        removeAllViews()
        clearState()
    }
    
    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mAdapter = try {
            view.adapter as StickyHeaderGridAdapter?
        } catch (e: ClassCastException) {
            throw ClassCastException("Adapter used with StickyHeaderGridLayoutManager must be kind of StickyHeaderGridAdapter")
        }
    }
    
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    
    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return LayoutParams(c, attrs)
    }
    
    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return if (lp is MarginLayoutParams) {
            LayoutParams(lp)
        } else {
            LayoutParams(lp)
        }
    }
    
    override fun onSaveInstanceState(): Parcelable {
        if (mPendingSavedState != null) {
            return SavedState(mPendingSavedState!!)
        }
        val state = SavedState()
        if (childCount > 0) {
            state.mAnchorSection = mAnchor.section
            state.mAnchorItem = mAnchor.item
            state.mAnchorOffset = mAnchor.offset
        } else {
            state.invalidateAnchor()
        }
        return state
    }
    
    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            mPendingSavedState = state
            requestLayout()
        } else {
            i(TAG, "invalid saved state class")
        }
    }
    
    override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
        return lp is LayoutParams
    }
    
    override fun canScrollVertically(): Boolean {
        return true
    }
    
    /**
     *
     * Scroll the RecyclerView to make the position visible.
     *
     *
     * RecyclerView will scroll the minimum amount that is necessary to make the
     * target position visible.
     *
     *
     * Note that scroll position change will not be reflected until the next layout call.
     *
     * @param position Scroll to this adapter position
     */
    override fun scrollToPosition(position: Int) {
        if (position < 0 || position > itemCount) {
            throw IndexOutOfBoundsException("adapter position out of range")
        }
        mPendingScrollPosition = position
        mPendingScrollPositionOffset = 0
        if (mPendingSavedState != null) {
            mPendingSavedState!!.invalidateAnchor()
        }
        requestLayout()
    }
    
    private fun getExtraLayoutSpace(state: State): Int {
        return if (state.hasTargetScrollPosition()) {
            height
        } else {
            0
        }
    }
    
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: State, position: Int) {
        val linearSmoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
                val layoutManager = layoutManager
                if (layoutManager == null || !layoutManager.canScrollVertically()) {
                    return 0
                }
                val adapterPosition = getPosition(view)
                val topOffset = getPositionSectionHeaderHeight(adapterPosition)
                val top = layoutManager.getDecoratedTop(view)
                val bottom = layoutManager.getDecoratedBottom(view)
                val start = layoutManager.paddingTop + topOffset
                val end = layoutManager.height - layoutManager.paddingBottom
                return calculateDtToFit(top, bottom, start, end, snapPreference)
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
    
    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }
        val firstRow = firstVisibleRow ?: return null
        return PointF(0F, (targetPosition - firstRow.adapterPosition).toFloat())
    }
    
    private fun getAdapterPositionFromAnchor(anchor: AnchorPosition): Int {
        if (anchor.section < 0 || anchor.section >= mAdapter!!.sectionCount) {
            anchor.reset()
            return NO_POSITION
        } else if (anchor.item < 0 || anchor.item >= mAdapter!!.getSectionItemCount(anchor.section)) {
            anchor.offset = 0
            return mAdapter!!.getSectionHeaderPosition(anchor.section)
        }
        return mAdapter!!.getSectionItemPosition(anchor.section, anchor.item)
    }
    
    private fun getAdapterPositionChecked(section: Int, offset: Int): Int {
        if (section < 0 || section >= mAdapter!!.sectionCount) {
            return NO_POSITION
        } else if (offset < 0 || offset >= mAdapter!!.getSectionItemCount(section)) {
            return mAdapter!!.getSectionHeaderPosition(section)
        }
        return mAdapter!!.getSectionItemPosition(section, offset)
    }
    
    override fun onLayoutChildren(recycler: Recycler, state: State) {
        if (mAdapter == null || state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            clearState()
            return
        }
        var pendingAdapterPosition: Int
        var pendingAdapterOffset: Int
        if (mPendingScrollPosition >= 0) {
            pendingAdapterPosition = mPendingScrollPosition
            pendingAdapterOffset = mPendingScrollPositionOffset
        } else if (mPendingSavedState != null && mPendingSavedState!!.hasValidAnchor()) {
            pendingAdapterPosition = getAdapterPositionChecked(mPendingSavedState!!.mAnchorSection, mPendingSavedState!!.mAnchorItem)
            pendingAdapterOffset = mPendingSavedState!!.mAnchorOffset
            mPendingSavedState = null
        } else {
            pendingAdapterPosition = getAdapterPositionFromAnchor(mAnchor)
            pendingAdapterOffset = mAnchor.offset
        }
        if (pendingAdapterPosition < 0 || pendingAdapterPosition >= state.itemCount) {
            pendingAdapterPosition = 0
            pendingAdapterOffset = 0
            mPendingScrollPosition = NO_POSITION
        }
        if (pendingAdapterOffset > 0) {
            pendingAdapterOffset = 0
        }
        detachAndScrapAttachedViews(recycler)
        clearState()
        
        // Make sure mFirstViewPosition is the start of the row
        pendingAdapterPosition = findFirstRowItem(pendingAdapterPosition)
        val left = paddingLeft
        val right = width - paddingRight
        val recyclerBottom = height - paddingBottom
        val totalHeight = 0
        var adapterPosition = pendingAdapterPosition
        var top = paddingTop + pendingAdapterOffset
        while (true) {
            if (adapterPosition >= state.itemCount) {
                break
            }
            var bottom: Int
            val viewType = mAdapter!!.getItemViewInternalType(adapterPosition)
            if (viewType == StickyHeaderGridAdapter.TYPE_HEADER) {
                val v = recycler.getViewForPosition(adapterPosition)
                addView(v)
                measureChildWithMargins(v, 0, 0)
                var height = getDecoratedMeasuredHeight(v)
                val margin = if (height >= mHeaderOverlapMargin) mHeaderOverlapMargin else height
                bottom = top + height
                layoutDecorated(v, left, top, right, bottom)
                bottom -= margin
                height -= margin
                mLayoutRows.add(LayoutRow(v, adapterPosition, 1, top, bottom))
                adapterPosition++
                mAverageHeaderHeight = height
            } else {
                val result = fillBottomRow(recycler, state, adapterPosition, top)
                bottom = top + result.height
                mLayoutRows.add(LayoutRow(result.adapterPosition, result.length, top, bottom))
                adapterPosition += result.length
            }
            top = bottom
            if (bottom >= recyclerBottom + getExtraLayoutSpace(state)) {
                break
            }
        }
        if (bottomRow!!.bottom < recyclerBottom) {
            scrollVerticallyBy(bottomRow!!.bottom - recyclerBottom, recycler, state)
        } else {
            clearViewsAndStickHeaders(recycler, state, false)
        }
        
        // If layout was caused by the pending scroll, adjust top item position and move it under sticky header
        if (mPendingScrollPosition >= 0) {
            mPendingScrollPosition = NO_POSITION
            val topOffset = getPositionSectionHeaderHeight(pendingAdapterPosition)
            if (topOffset != 0) {
                scrollVerticallyBy(-topOffset, recycler, state)
            }
        }
    }
    
    override fun onLayoutCompleted(state: State) {
        super.onLayoutCompleted(state)
        mPendingSavedState = null
    }
    
    private fun getPositionSectionHeaderHeight(adapterPosition: Int): Int {
        val section = mAdapter!!.getAdapterPositionSection(adapterPosition)
        if (section >= 0 && mAdapter!!.isSectionHeaderSticky(section)) {
            val offset = mAdapter!!.getItemSectionOffset(section, adapterPosition)
            if (offset >= 0) {
                val headerAdapterPosition = mAdapter!!.getSectionHeaderPosition(section)
                return if (mFloatingHeaderView != null && headerAdapterPosition == mFloatingHeaderPosition) {
                    0.coerceAtLeast(getDecoratedMeasuredHeight(mFloatingHeaderView!!) - mHeaderOverlapMargin)
                } else {
                    val header = getHeaderRow(headerAdapterPosition)
                    header?.height ?: // Fall back to cached header size, can be incorrect
                    mAverageHeaderHeight
                }
            }
        }
        return 0
    }
    
    private fun findFirstRowItem(adapterPosition: Int): Int {
        var position = adapterPosition
        val section = mAdapter!!.getAdapterPositionSection(position)
        var sectionPosition = mAdapter!!.getItemSectionOffset(section, position)
        while (sectionPosition > 0 && mSpanSizeLookup!!.getSpanIndex(section, sectionPosition, mSpanCount) != 0) {
            sectionPosition--
            position--
        }
        return position
    }
    
    private fun getSpanWidth(recyclerWidth: Int, spanIndex: Int, spanSize: Int): Int {
        val spanWidth = recyclerWidth / mSpanCount
        val spanWidthReminder = recyclerWidth - spanWidth * mSpanCount
        val widthCorrection = 0.coerceAtLeast(spanWidthReminder - spanIndex).coerceAtMost(spanSize)
        return spanWidth * spanSize + widthCorrection
    }
    
    private fun getSpanLeft(recyclerWidth: Int, spanIndex: Int): Int {
        val spanWidth = recyclerWidth / mSpanCount
        val spanWidthReminder = recyclerWidth - spanWidth * mSpanCount
        val widthCorrection = spanWidthReminder.coerceAtMost(spanIndex)
        return spanWidth * spanIndex + widthCorrection
    }
    
    private fun fillBottomRow(recycler: Recycler, state: State, position: Int, top: Int): FillResult {
        val recyclerWidth = width - paddingLeft - paddingRight
        val section = mAdapter!!.getAdapterPositionSection(position)
        var adapterPosition = position
        var sectionPosition = mAdapter!!.getItemSectionOffset(section, adapterPosition)
        var spanSize = mSpanSizeLookup!!.getSpanSize(section, sectionPosition)
        var spanIndex = mSpanSizeLookup!!.getSpanIndex(section, sectionPosition, mSpanCount)
        var count = 0
        var maxHeight = 0
        
        // Create phase
        Arrays.fill(mFillViewSet, null)
        while (spanIndex + spanSize <= mSpanCount) {
            // Create view and fill layout params
            val spanWidth = getSpanWidth(recyclerWidth, spanIndex, spanSize)
            val v = recycler.getViewForPosition(adapterPosition)
            val params = v.layoutParams as LayoutParams
            params.spanIndex = spanIndex
            params.spanSize = spanSize
            addView(v, mHeadersStartPosition)
            mHeadersStartPosition++
            measureChildWithMargins(v, recyclerWidth - spanWidth, 0)
            mFillViewSet[count] = v
            count++
            val height = getDecoratedMeasuredHeight(v)
            if (maxHeight < height) {
                maxHeight = height
            }
            
            // Check next
            adapterPosition++
            sectionPosition++
            if (sectionPosition >= mAdapter!!.getSectionItemCount(section)) {
                break
            }
            spanIndex += spanSize
            spanSize = mSpanSizeLookup!!.getSpanSize(section, sectionPosition)
        }
        
        // Layout phase
        var left = paddingLeft
        for (i in 0 until count) {
            val v = mFillViewSet[i]
            val height = getDecoratedMeasuredHeight(v!!)
            val width = getDecoratedMeasuredWidth(v)
            layoutDecorated(v, left, top, left + width, top + height)
            left += width
        }
        mFillResult.edgeView = mFillViewSet[count - 1]
        mFillResult.adapterPosition = position
        mFillResult.length = count
        mFillResult.height = maxHeight
        return mFillResult
    }
    
    private fun fillTopRow(recycler: Recycler, state: State, position: Int, top: Int): FillResult {
        val recyclerWidth = width - paddingLeft - paddingRight
        val section = mAdapter!!.getAdapterPositionSection(position)
        var adapterPosition = position
        var sectionPosition = mAdapter!!.getItemSectionOffset(section, adapterPosition)
        var spanSize = mSpanSizeLookup!!.getSpanSize(section, sectionPosition)
        var spanIndex = mSpanSizeLookup!!.getSpanIndex(section, sectionPosition, mSpanCount)
        var count = 0
        var maxHeight = 0
        Arrays.fill(mFillViewSet, null)
        while (spanIndex >= 0) {
            // Create view and fill layout params
            val spanWidth = getSpanWidth(recyclerWidth, spanIndex, spanSize)
            val v = recycler.getViewForPosition(adapterPosition)
            val params = v.layoutParams as LayoutParams
            params.spanIndex = spanIndex
            params.spanSize = spanSize
            addView(v, 0)
            mHeadersStartPosition++
            measureChildWithMargins(v, recyclerWidth - spanWidth, 0)
            mFillViewSet[count] = v
            count++
            val height = getDecoratedMeasuredHeight(v)
            if (maxHeight < height) {
                maxHeight = height
            }
            
            // Check next
            adapterPosition--
            sectionPosition--
            if (sectionPosition < 0) {
                break
            }
            spanSize = mSpanSizeLookup!!.getSpanSize(section, sectionPosition)
            spanIndex -= spanSize
        }
        
        // Layout phase
        var left = paddingLeft
        for (i in count - 1 downTo 0) {
            val v = mFillViewSet[i]
            val height = getDecoratedMeasuredHeight(v!!)
            val width = getDecoratedMeasuredWidth(v)
            layoutDecorated(v, left, top - maxHeight, left + width, top - (maxHeight - height))
            left += width
        }
        mFillResult.edgeView = mFillViewSet[count - 1]
        mFillResult.adapterPosition = adapterPosition + 1
        mFillResult.length = count
        mFillResult.height = maxHeight
        return mFillResult
    }
    
    private fun clearHiddenRows(recycler: Recycler?, state: State, top: Boolean) {
        if (mLayoutRows.size <= 0) {
            return
        }
        try {
            val recyclerTop = paddingTop
            val recyclerBottom = height - paddingBottom
            if (top) {
                var row = topRow
                while (row.bottom < recyclerTop - getExtraLayoutSpace(state) || row.top > recyclerBottom) {
                    if (row.header) {
                        removeAndRecycleViewAt(mHeadersStartPosition + if (mFloatingHeaderView != null) 1 else 0, recycler!!)
                    } else {
                        for (i in 0 until row.length) {
                            if (recycler == null) return
                            removeAndRecycleViewAt(0, recycler)
                            mHeadersStartPosition--
                        }
                    }
                    if (mLayoutRows.isEmpty()) return
                    mLayoutRows.removeAt(0)
                    if (mLayoutRows.isNotEmpty()) row = topRow
                }
            } else {
                var row = bottomRow
                while (row != null && (row.bottom < recyclerTop || row.top > recyclerBottom + getExtraLayoutSpace(state))) {
                    if (row.header) {
                        removeAndRecycleViewAt(childCount - 1, recycler!!)
                    } else {
                        for (i in 0 until row.length) {
                            if (recycler == null) return
                            removeAndRecycleViewAt(mHeadersStartPosition - 1, recycler)
                            mHeadersStartPosition--
                        }
                    }
                    mLayoutRows.removeAt(mLayoutRows.size - 1)
                    row = bottomRow
                }
            }
        } catch (ne: NullPointerException) {
            e(TAG, "clearHiddenRows: " + ne.message, ne)
        } catch (ibe: IndexOutOfBoundsException) {
            e(TAG, "clearHiddenRows: " + ibe.message, ibe)
        }
    }
    
    private fun clearViewsAndStickHeaders(recycler: Recycler, state: State, top: Boolean) {
        clearHiddenRows(recycler, state, top)
        if (childCount > 0) {
            stickTopHeader(recycler)
        }
        updateTopPosition()
    }
    
    private val bottomRow: LayoutRow?
        private get() = if (mLayoutRows.size > 0) mLayoutRows[mLayoutRows.size - 1] else null
    private val topRow: LayoutRow
        private get() = mLayoutRows[0]
    
    private fun offsetRowsVertical(offset: Int) {
        for (row in mLayoutRows) {
            row.top += offset
            row.bottom += offset
        }
        offsetChildrenVertical(offset)
    }
    
    private fun addRow(recycler: Recycler, state: State, isTop: Boolean, adapterPosition: Int, top: Int) {
        val left = paddingLeft
        val right = width - paddingRight
        
        // Reattach floating header if needed
        if (isTop && mFloatingHeaderView != null && adapterPosition == mFloatingHeaderPosition) {
            removeFloatingHeader(recycler)
        }
        val viewType = mAdapter!!.getItemViewInternalType(adapterPosition)
        if (viewType == StickyHeaderGridAdapter.TYPE_HEADER) {
            val v = recycler.getViewForPosition(adapterPosition)
            if (isTop) {
                addView(v, mHeadersStartPosition)
            } else {
                addView(v)
            }
            measureChildWithMargins(v, 0, 0)
            val height = getDecoratedMeasuredHeight(v)
            val margin = if (height >= mHeaderOverlapMargin) mHeaderOverlapMargin else height
            if (isTop) {
                layoutDecorated(v, left, top - height + margin, right, top + margin)
                mLayoutRows.add(0, LayoutRow(v, adapterPosition, 1, top - height + margin, top))
            } else {
                layoutDecorated(v, left, top, right, top + height)
                mLayoutRows.add(LayoutRow(v, adapterPosition, 1, top, top + height - margin))
            }
            mAverageHeaderHeight = height - margin
        } else {
            if (isTop) {
                val result = fillTopRow(recycler, state, adapterPosition, top)
                mLayoutRows.add(0, LayoutRow(result.adapterPosition, result.length, top - result.height, top))
            } else {
                val result = fillBottomRow(recycler, state, adapterPosition, top)
                mLayoutRows.add(LayoutRow(result.adapterPosition, result.length, top, top + result.height))
            }
        }
    }
    
    private fun addOffScreenRows(recycler: Recycler, state: State, recyclerTop: Int, recyclerBottom: Int, bottom: Boolean) {
        if (bottom) {
            // Bottom
            while (true) {
                val bottomRow = bottomRow
                val adapterPosition = bottomRow!!.adapterPosition + bottomRow.length
                if (bottomRow.bottom >= recyclerBottom + getExtraLayoutSpace(state) || adapterPosition >= state.itemCount) {
                    break
                }
                addRow(recycler, state, false, adapterPosition, bottomRow.bottom)
            }
        } else {
            // Top
            while (true) {
                val topRow = topRow
                val adapterPosition = topRow.adapterPosition - 1
                if (topRow.top < recyclerTop - getExtraLayoutSpace(state) || adapterPosition < 0) {
                    break
                }
                addRow(recycler, state, true, adapterPosition, topRow.top)
            }
        }
    }
    
    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: State): Int {
        if (childCount == 0) {
            return 0
        }
        var scrolled = 0
        val left = paddingLeft
        val right = width - paddingRight
        val recyclerTop = paddingTop
        val recyclerBottom = height - paddingBottom
        try {
            // If we have simple header stick, offset it back
            val firstHeader = firstVisibleSectionHeader
            if (firstHeader != NO_POSITION) {
                mLayoutRows[firstHeader].headerView!!.offsetTopAndBottom(-mStickOffset)
            }
            if (dy >= 0) {
                // Up
                while (scrolled < dy) {
                    val bottomRow = bottomRow
                    val scrollChunk = -(bottomRow!!.bottom - recyclerBottom).coerceAtLeast(0).coerceAtMost(dy - scrolled)
                    offsetRowsVertical(scrollChunk)
                    scrolled -= scrollChunk
                    val adapterPosition = bottomRow.adapterPosition + bottomRow.length
                    if (scrolled >= dy || adapterPosition >= state.itemCount) {
                        break
                    }
                    addRow(recycler, state, false, adapterPosition, bottomRow.bottom)
                }
            } else {
                // Down
                while (scrolled > dy) {
                    val topRow = topRow
                    val scrollChunk = (-topRow.top + recyclerTop).coerceAtLeast(0).coerceAtMost(scrolled - dy)
                    offsetRowsVertical(scrollChunk)
                    scrolled -= scrollChunk
                    val adapterPosition = topRow.adapterPosition - 1
                    if (scrolled <= dy || adapterPosition >= state.itemCount || adapterPosition < 0) {
                        break
                    }
                    addRow(recycler, state, true, adapterPosition, topRow.top)
                }
            }
            
            // Fill extra offscreen rows for smooth scroll
            if (scrolled == dy) {
                addOffScreenRows(recycler, state, recyclerTop, recyclerBottom, dy >= 0)
            }
            clearViewsAndStickHeaders(recycler, state, dy >= 0)
        } catch (e: NullPointerException) {
            e(TAG, "scrollVerticallyBy: " + e.message, e)
        } catch (e: IndexOutOfBoundsException) {
            e(TAG, "scrollVerticallyBy: " + e.message, e)
        }
        return scrolled
    }
    
    /**
     * Returns first visible item excluding headers.
     *
     * @param visibleTop Whether item top edge should be visible or not
     * @return The first visible item adapter position closest to top of the layout.
     */
    fun getFirstVisibleItemPosition(visibleTop: Boolean): Int {
        return getFirstVisiblePosition(StickyHeaderGridAdapter.TYPE_ITEM, visibleTop)
    }
    
    /**
     * Returns last visible item excluding headers.
     *
     * @return The last visible item adapter position closest to bottom of the layout.
     */
    val lastVisibleItemPosition: Int
        get() = getLastVisiblePosition(StickyHeaderGridAdapter.TYPE_ITEM)
    
    /**
     * Returns first visible header.
     *
     * @param visibleTop Whether header top edge should be visible or not
     * @return The first visible header adapter position closest to top of the layout.
     */
    fun getFirstVisibleHeaderPosition(visibleTop: Boolean): Int {
        return getFirstVisiblePosition(StickyHeaderGridAdapter.TYPE_HEADER, visibleTop)
    }
    
    /**
     * Returns last visible header.
     *
     * @return The last visible header adapter position closest to bottom of the layout.
     */
    val lastVisibleHeaderPosition: Int
        get() = getLastVisiblePosition(StickyHeaderGridAdapter.TYPE_HEADER)
    
    private fun getFirstVisiblePosition(type: Int, visibleTop: Boolean): Int {
        if (type == StickyHeaderGridAdapter.TYPE_ITEM && mHeadersStartPosition <= 0) {
            return NO_POSITION
        } else if (type == StickyHeaderGridAdapter.TYPE_HEADER && mHeadersStartPosition >= childCount) {
            return NO_POSITION
        }
        val viewFrom = if (type == StickyHeaderGridAdapter.TYPE_ITEM) 0 else mHeadersStartPosition
        val viewTo = if (type == StickyHeaderGridAdapter.TYPE_ITEM) mHeadersStartPosition else childCount
        val recyclerTop = paddingTop
        for (i in viewFrom until viewTo) {
            val v = getChildAt(i)
            val adapterPosition = getPosition(v!!)
            val headerHeight = getPositionSectionHeaderHeight(adapterPosition)
            val top = getDecoratedTop(v)
            val bottom = getDecoratedBottom(v)
            if (visibleTop) {
                if (top >= recyclerTop + headerHeight) {
                    return adapterPosition
                }
            } else {
                if (bottom >= recyclerTop + headerHeight) {
                    return adapterPosition
                }
            }
        }
        return NO_POSITION
    }
    
    private fun getLastVisiblePosition(type: Int): Int {
        if (type == StickyHeaderGridAdapter.TYPE_ITEM && mHeadersStartPosition <= 0) {
            return NO_POSITION
        } else if (type == StickyHeaderGridAdapter.TYPE_HEADER && mHeadersStartPosition >= childCount) {
            return NO_POSITION
        }
        val viewFrom = if (type == StickyHeaderGridAdapter.TYPE_ITEM) mHeadersStartPosition - 1 else childCount - 1
        val viewTo = if (type == StickyHeaderGridAdapter.TYPE_ITEM) 0 else mHeadersStartPosition
        val recyclerBottom = height - paddingBottom
        for (i in viewFrom downTo viewTo) {
            val v = getChildAt(i)
            val top = getDecoratedTop(v!!)
            if (top < recyclerBottom) {
                return getPosition(v)
            }
        }
        return NO_POSITION
    }
    
    private val firstVisibleRow: LayoutRow?
        private get() {
            val recyclerTop = paddingTop
            for (row in mLayoutRows) {
                if (row.bottom > recyclerTop) {
                    return row
                }
            }
            return null
        }
    private val firstVisibleSectionHeader: Int
        private get() {
            val recyclerTop = paddingTop
            var header = NO_POSITION
            var i = 0
            val n = mLayoutRows.size
            while (i < n) {
                val row = mLayoutRows[i]
                if (row.header) {
                    header = i
                }
                if (row.bottom > recyclerTop) {
                    return header
                }
                ++i
            }
            return NO_POSITION
        }
    
    private fun getNextVisibleSectionHeader(headerFrom: Int): LayoutRow? {
        var i = headerFrom + 1
        val n = mLayoutRows.size
        while (i < n) {
            val row = mLayoutRows[i]
            if (row.header) {
                return row
            }
            ++i
        }
        return null
    }
    
    private fun getHeaderRow(adapterPosition: Int): LayoutRow? {
        var i = 0
        val n = mLayoutRows.size
        while (i < n) {
            val row = mLayoutRows[i]
            if (row.header && row.adapterPosition == adapterPosition) {
                return row
            }
            ++i
        }
        return null
    }
    
    private fun removeFloatingHeader(recycler: Recycler) {
        if (mFloatingHeaderView == null) {
            return
        }
        val view: View = mFloatingHeaderView as View
        mFloatingHeaderView = null
        mFloatingHeaderPosition = NO_POSITION
        removeAndRecycleView(view, recycler)
    }
    
    private fun onHeaderChanged(section: Int, view: View?, state: HeaderState, pushOffset: Int) {
        if (mStickyHeaderSection != NO_POSITION && section != mStickyHeaderSection) {
            onHeaderUnstick()
        }
        val headerStateChanged = mStickyHeaderSection != section || mStickyHeadeState != state || state == PUSHED
        mStickyHeaderSection = section
        mStickyHeaderView = view
        mStickyHeadeState = state
        if (headerStateChanged && headerStateChangeListener != null) {
            headerStateChangeListener!!.onHeaderStateChanged(section, view, state, pushOffset)
        }
    }
    
    private fun onHeaderUnstick() {
        if (mStickyHeaderSection != NO_POSITION) {
            if (headerStateChangeListener != null) {
                headerStateChangeListener!!.onHeaderStateChanged(mStickyHeaderSection, mStickyHeaderView, NORMAL, 0)
            }
            mStickyHeaderSection = NO_POSITION
            mStickyHeaderView = null
            mStickyHeadeState = NORMAL
        }
    }
    
    private fun stickTopHeader(recycler: Recycler) {
        val firstHeader = firstVisibleSectionHeader
        val top = paddingTop
        val left = paddingLeft
        val right = width - paddingRight
        val notifySection = NO_POSITION
        val notifyView: View? = null
        val notifyState = NORMAL
        val notifyOffset = 0
        if (firstHeader != NO_POSITION) {
            // Top row is header, floating header is not visible, remove
            removeFloatingHeader(recycler)
            val firstHeaderRow = mLayoutRows[firstHeader]
            val section = mAdapter!!.getAdapterPositionSection(firstHeaderRow.adapterPosition)
            if (mAdapter!!.isSectionHeaderSticky(section)) {
                val nextHeaderRow = getNextVisibleSectionHeader(firstHeader)
                var offset = 0
                if (nextHeaderRow != null) {
                    val height = firstHeaderRow.height
                    offset = ((top - nextHeaderRow.top).coerceAtLeast(-height) + height).coerceAtMost(height)
                }
                mStickOffset = top - firstHeaderRow.top - offset
                firstHeaderRow.headerView!!.offsetTopAndBottom(mStickOffset)
                onHeaderChanged(section, firstHeaderRow.headerView, if (offset == 0) STICKY else PUSHED, offset)
            } else {
                onHeaderUnstick()
                mStickOffset = 0
            }
        } else {
            // We don't have first visible sector header in layout, create floating
            val firstVisibleRow = firstVisibleRow
            if (firstVisibleRow != null) {
                val section = mAdapter!!.getAdapterPositionSection(firstVisibleRow.adapterPosition)
                if (mAdapter!!.isSectionHeaderSticky(section)) {
                    val headerPosition = mAdapter!!.getSectionHeaderPosition(section)
                    if (mFloatingHeaderView == null || mFloatingHeaderPosition != headerPosition) {
                        removeFloatingHeader(recycler)
                        
                        // Create floating header
                        val v = recycler.getViewForPosition(headerPosition)
                        addView(v, mHeadersStartPosition)
                        measureChildWithMargins(v, 0, 0)
                        mFloatingHeaderView = v
                        mFloatingHeaderPosition = headerPosition
                    }
                    
                    // Push floating header up, if needed
                    val height = getDecoratedMeasuredHeight(mFloatingHeaderView!!)
                    var offset = 0
                    if (childCount - mHeadersStartPosition > 1) {
                        val nextHeader = getChildAt(mHeadersStartPosition + 1)
                        val contentHeight = 0.coerceAtLeast(height - mHeaderOverlapMargin)
                        offset = (top - getDecoratedTop(nextHeader!!)).coerceAtLeast(-contentHeight) + contentHeight
                    }
                    layoutDecorated(mFloatingHeaderView!!, left, top - offset, right, top + height - offset)
                    onHeaderChanged(section, mFloatingHeaderView, if (offset == 0) STICKY else PUSHED, offset)
                } else {
                    onHeaderUnstick()
                }
            } else {
                onHeaderUnstick()
            }
        }
    }
    
    private fun updateTopPosition() {
        if (childCount == 0) {
            mAnchor.reset()
        }
        val firstVisibleRow = firstVisibleRow
        if (firstVisibleRow != null) {
            mAnchor.section = mAdapter!!.getAdapterPositionSection(firstVisibleRow.adapterPosition)
            mAnchor.item = mAdapter!!.getItemSectionOffset(mAnchor.section, firstVisibleRow.adapterPosition)
            mAnchor.offset = (firstVisibleRow.top - paddingTop).coerceAtMost(0)
        }
    }
    
    private fun getViewType(view: View): Int {
        return getItemViewType(view) and 0xFF
    }
    
    private fun getViewType(position: Int): Int {
        return mAdapter!!.getItemViewType(position) and 0xFF
    }
    
    private fun clearState() {
        mHeadersStartPosition = 0
        mStickOffset = 0
        mFloatingHeaderView = null
        mFloatingHeaderPosition = -1
        mAverageHeaderHeight = 0
        mLayoutRows.clear()
        if (mStickyHeaderSection != NO_POSITION) {
            if (headerStateChangeListener != null) {
                headerStateChangeListener!!.onHeaderStateChanged(mStickyHeaderSection, mStickyHeaderView, NORMAL, 0)
            }
            mStickyHeaderSection = NO_POSITION
            mStickyHeaderView = null
            mStickyHeadeState = NORMAL
        }
    }
    
    override fun computeVerticalScrollExtent(state: State): Int {
        if (mHeadersStartPosition == 0 || state.itemCount == 0) {
            return 0
        }
        val startChild = getChildAt(0)
        val endChild = getChildAt(mHeadersStartPosition - 1)
        return if (startChild == null || endChild == null) {
            0
        } else abs(getPosition(startChild) - getPosition(endChild)) + 1
    }
    
    override fun computeVerticalScrollOffset(state: State): Int {
        if (mHeadersStartPosition == 0 || state.itemCount == 0) {
            return 0
        }
        val startChild = getChildAt(0)
        val endChild = getChildAt(mHeadersStartPosition - 1)
        if (startChild == null || endChild == null) {
            return 0
        }
        val recyclerTop = paddingTop
        val topRow = topRow
        val scrollChunk = (-topRow.top + recyclerTop).coerceAtLeast(0)
        if (scrollChunk == 0) {
            return 0
        }
        val minPosition = getPosition(startChild).coerceAtMost(getPosition(endChild))
        val maxPosition = getPosition(startChild).coerceAtLeast(getPosition(endChild))
        return 0.coerceAtLeast(minPosition)
    }
    
    override fun computeVerticalScrollRange(state: State): Int {
        if (mHeadersStartPosition == 0 || state.itemCount == 0) {
            return 0
        }
        val startChild = getChildAt(0)
        val endChild = getChildAt(mHeadersStartPosition - 1)
        return if (startChild == null || endChild == null) {
            0
        } else state.itemCount
    }
    
    class LayoutParams : RecyclerView.LayoutParams {
        var spanIndex = INVALID_SPAN_ID
        var spanSize = 0
        
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: MarginLayoutParams?) : super(source)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: RecyclerView.LayoutParams?) : super(source)
        
        companion object {
            const val INVALID_SPAN_ID = -1
        }
    }
    
    class DefaultSpanSizeLookup : SpanSizeLookup() {
        override fun getSpanSize(section: Int, position: Int): Int {
            return 1
        }
        
        override fun getSpanIndex(section: Int, position: Int, spanCount: Int): Int {
            return position % spanCount
        }
    }
    
    /**
     * An interface to provide the number of spans each item occupies.
     *
     *
     * Default implementation sets each item to occupy exactly 1 span.
     *
     * @see StickyHeaderGridLayoutManager.setSpanSizeLookup
     */
    abstract class SpanSizeLookup {
        /**
         * Returns the number of span occupied by the item in `section` at `position`.
         *
         * @param section The adapter section of the item
         * @param position The adapter position of the item in section
         * @return The number of spans occupied by the item at the provided section and position
         */
        abstract fun getSpanSize(section: Int, position: Int): Int
        
        /**
         * Returns the final span index of the provided position.
         *
         *
         *
         * If you override this method, you need to make sure it is consistent with
         * [.getSpanSize]. StickyHeaderGridLayoutManager does not call this method for
         * each item. It is called only for the reference item and rest of the items
         * are assigned to spans based on the reference item. For example, you cannot assign a
         * position to span 2 while span 1 is empty.
         *
         *
         *
         * @param section The adapter section of the item
         * @param position  The adapter position of the item in section
         * @param spanCount The total number of spans in the grid
         * @return The final span position of the item. Should be between 0 (inclusive) and
         * `spanCount`(exclusive)
         */
        open fun getSpanIndex(section: Int, position: Int, spanCount: Int): Int {
            // TODO: cache them?
            val positionSpanSize = getSpanSize(section, position)
            if (positionSpanSize >= spanCount) {
                return 0
            }
            var spanIndex = 0
            for (i in 0 until position) {
                val spanSize = getSpanSize(section, i)
                spanIndex += spanSize
                if (spanIndex == spanCount) {
                    spanIndex = 0
                } else if (spanIndex > spanCount) {
                    spanIndex = spanSize
                }
            }
            return if (spanIndex + positionSpanSize <= spanCount) {
                spanIndex
            } else 0
        }
    }
    
    class SavedState : Parcelable {
        var mAnchorSection = 0
        var mAnchorItem = 0
        var mAnchorOffset = 0
        
        constructor()
        internal constructor(`in`: Parcel) {
            mAnchorSection = `in`.readInt()
            mAnchorItem = `in`.readInt()
            mAnchorOffset = `in`.readInt()
        }
        
        constructor(other: SavedState) {
            mAnchorSection = other.mAnchorSection
            mAnchorItem = other.mAnchorItem
            mAnchorOffset = other.mAnchorOffset
        }
        
        fun hasValidAnchor(): Boolean {
            return mAnchorSection >= 0
        }
        
        fun invalidateAnchor() {
            mAnchorSection = NO_POSITION
        }
        
        override fun describeContents(): Int {
            return 0
        }
        
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(mAnchorSection)
            dest.writeInt(mAnchorItem)
            dest.writeInt(mAnchorOffset)
        }
        
        companion object {
            @JvmField
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }
                
                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
    
    private class LayoutRow {
        var header: Boolean
        var headerView: View?
        var adapterPosition: Int
        var length: Int
        var top: Int
        var bottom: Int
        
        constructor(adapterPosition: Int, length: Int, top: Int, bottom: Int) {
            header = false
            headerView = null
            this.adapterPosition = adapterPosition
            this.length = length
            this.top = top
            this.bottom = bottom
        }
        
        constructor(headerView: View?, adapterPosition: Int, length: Int, top: Int, bottom: Int) {
            header = true
            this.headerView = headerView
            this.adapterPosition = adapterPosition
            this.length = length
            this.top = top
            this.bottom = bottom
        }
        
        val height: Int
            get() = bottom - top
    }
    
    private class FillResult {
        var edgeView: View? = null
        var adapterPosition = 0
        var length = 0
        var height = 0
    }
    
    private class AnchorPosition {
        var section = 0
        var item = 0
        var offset = 0
        fun reset() {
            section = NO_POSITION
            item = 0
            offset = 0
        }
        
        init {
            reset()
        }
    }
    
    companion object {
        const val TAG = "StickyLayoutManager"
        private const val DEFAULT_ROW_COUNT = 16
    }
    
    /**
     * Creates a vertical StickyHeaderGridLayoutManager
     *
     * @param spanCount The number of columns in the grid
     */
    init {
        mFillViewSet = arrayOfNulls(mSpanCount)
        mHeaderOverlapMargin = 0
        require(mSpanCount >= 1) { "Span count should be at least 1. Provided $mSpanCount" }
    }
}