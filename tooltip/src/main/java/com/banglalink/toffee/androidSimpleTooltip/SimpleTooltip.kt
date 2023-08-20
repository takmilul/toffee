/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Douglas Nassif Roma Junior
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.banglalink.toffee.androidSimpleTooltip

import android.R.attr
import android.R.id
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import android.widget.PopupWindow.OnDismissListener
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.calculateRectInWindow
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.calculateRectOnScreen
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.findFrameLayout
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.getColor
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.getDrawable
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.pxFromDp
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.removeOnGlobalLayoutListener
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.setTextAppearance
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.setWidth
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.setX
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.setY
import com.banglalink.toffee.androidSimpleTooltip.SimpleTooltipUtils.tooltipGravityToArrowDirection
import com.banglalink.toffee.tooltip.R

/**
 *
 * Um tooltip que pode ser utilizado para exibição de dicas.
 *
 * A tooltip that can be used to display tips on the screen.
 *
 * @author Created by douglas on 05/05/16.
 * @see android.widget.PopupWindow
 */
class SimpleTooltip private constructor(builder: Builder) : OnDismissListener {
    private val mContext: Context?
    private var mOnDismissListener: OnDismissListener?
    private var mOnShowListener: OnShowListener?
    private var mPopupWindow: PopupWindow? = null
    private val mGravity: Int
    private val mArrowDirection: Int
    private val mDismissOnInsideTouch: Boolean
    private val mDismissOnOutsideTouch: Boolean
    private val mModal: Boolean
    private val mContentView: View?
    private var mContentLayout: View? = null
    @IdRes private val mTextViewId: Int
    private val mOverlayWindowBackgroundColor: Int
    private val mText: CharSequence
    private val mAnchorView: View?
    private val mTransparentOverlay: Boolean
    private val mOverlayOffset: Float
    private val mOverlayMatchParent: Boolean
    private val mMaxWidth: Float
    private var mOverlay: View? = null
    private var mRootView: ViewGroup?
    private val mShowArrow: Boolean
    private var mArrowView: ImageView? = null
    private val mArrowDrawable: Drawable?
    private val mAnimated: Boolean
    private var mAnimator: AnimatorSet? = null
    private val mMargin: Float
    private val mPadding: Float
    private val mAnimationPadding: Float
    private val mAnimationDuration: Long
    private val mArrowWidth: Float
    private val mArrowHeight: Float
    private val mFocusable: Boolean
    private var dismissed = false
    private val mHighlightShape: Int
    private val width: Int
    private val height: Int
    private val mIgnoreOverlay: Boolean
    private val cornerRadius: Float
    
    init {
        mContext = builder.context
        mGravity = builder.gravity
        mOverlayWindowBackgroundColor = builder.overlayWindowBackgroundColor
        mArrowDirection = builder.arrowDirection
        mDismissOnInsideTouch = builder.dismissOnInsideTouch
        mDismissOnOutsideTouch = builder.dismissOnOutsideTouch
        mModal = builder.modal
        mContentView = builder.contentView
        mTextViewId = builder.textViewId
        mText = builder.text
        mAnchorView = builder.anchorView
        mTransparentOverlay = builder.transparentOverlay
        mOverlayOffset = builder.overlayOffset
        mOverlayMatchParent = builder.overlayMatchParent
        mMaxWidth = builder.maxWidth
        mShowArrow = builder.showArrow
        mArrowWidth = builder.arrowWidth
        mArrowHeight = builder.arrowHeight
        mArrowDrawable = builder.arrowDrawable
        mAnimated = builder.animated
        mMargin = builder.margin
        mPadding = builder.padding
        mAnimationPadding = builder.animationPadding
        mAnimationDuration = builder.animationDuration
        mOnDismissListener = builder.onDismissListener
        mOnShowListener = builder.onShowListener
        mFocusable = builder.focusable
        mRootView = findFrameLayout(mAnchorView!!)
        mHighlightShape = builder.highlightShape
        mIgnoreOverlay = builder.ignoreOverlay
        width = builder.width
        height = builder.height
        cornerRadius = builder.cornerRadius
        init()
    }
    
    private fun init() {
        configPopupWindow()
        configContentView()
    }
    
    companion object {
        private val TAG = SimpleTooltip::class.java.simpleName
        // Default Resources
        private const val mDefaultPopupWindowStyleRes = attr.popupWindowStyle
        private val mDefaultTextAppearanceRes: Int = R.style.simpletooltip_default
        private val mDefaultBackgroundColorRes: Int = R.color.simpletooltip_background
        private val mDefaultTextColorRes: Int = R.color.simpletooltip_text
        private val mDefaultArrowColorRes: Int = R.color.simpletooltip_arrow
        private val mDefaultMarginRes: Int = R.dimen.simpletooltip_margin
        private val mDefaultPaddingRes: Int = R.dimen.simpletooltip_padding
        private val mDefaultAnimationPaddingRes: Int = R.dimen.simpletooltip_animation_padding
        private val mDefaultAnimationDurationRes: Int = R.integer.simpletooltip_animation_duration
        private val mDefaultArrowWidthRes: Int = R.dimen.simpletooltip_arrow_width
        private val mDefaultArrowHeightRes: Int = R.dimen.simpletooltip_arrow_height
        private val mDefaultOverlayOffsetRes: Int = R.dimen.simpletooltip_overlay_offset
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun configPopupWindow() {
        mPopupWindow = PopupWindow(mContext, null, mDefaultPopupWindowStyleRes)
        mPopupWindow!!.setOnDismissListener(this)
        mPopupWindow!!.width = width
        mPopupWindow!!.height = height
        mPopupWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mPopupWindow!!.isOutsideTouchable = true
        mPopupWindow!!.isTouchable = true
        mPopupWindow!!.setTouchInterceptor(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (!mDismissOnOutsideTouch && event.action == MotionEvent.ACTION_DOWN
                && (x < 0 || x >= mContentLayout!!.measuredWidth || y < 0 || y >= mContentLayout!!.measuredHeight)
            ) {
                return@OnTouchListener true
            } else if (!mDismissOnOutsideTouch && event.action == MotionEvent.ACTION_OUTSIDE) {
                return@OnTouchListener true
            } else if (event.action == MotionEvent.ACTION_DOWN && mDismissOnInsideTouch) {
                dismiss()
                return@OnTouchListener true
            }
            false
        })
        mPopupWindow!!.isClippingEnabled = false
        mPopupWindow!!.isFocusable = mFocusable
    }
    
    fun show() {
        verifyDismissed()
        mContentLayout!!.viewTreeObserver.addOnGlobalLayoutListener(mLocationLayoutListener)
        mContentLayout!!.viewTreeObserver.addOnGlobalLayoutListener(mAutoDismissLayoutListener)
        mRootView!!.post {
            if (mRootView!!.isShown) {
                mPopupWindow!!.showAtLocation(mRootView, Gravity.NO_GRAVITY, mRootView!!.width, mRootView!!.height)
                if (mFocusable) mContentLayout!!.requestFocus()
            } else {
                Log.e(TAG, "Tooltip cannot be shown, root view is invalid or has been closed.")
            }
        }
    }
    
    private fun verifyDismissed() {
        require(!dismissed) { "Tooltip has been dismissed." }
    }
    
    private fun createOverlay() {
        if (mIgnoreOverlay) {
            return
        }
        mOverlay = if (mTransparentOverlay) View(mContext) else OverlayView(
            mContext,
            mAnchorView!!,
            mHighlightShape,
            mOverlayOffset,
            mOverlayWindowBackgroundColor,
            cornerRadius
        )
        if (mOverlayMatchParent) mOverlay!!.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ) else mOverlay!!.layoutParams = ViewGroup.LayoutParams(
            mRootView!!.width, mRootView!!.height
        )
        mOverlay!!.setOnTouchListener(mOverlayTouchListener)
        mRootView!!.addView(mOverlay)
    }
    
    private fun calculatePopupLocation(): PointF {
        val location = PointF()
        val anchorRect = calculateRectInWindow(mAnchorView!!)
        val anchorCenter = PointF(anchorRect.centerX(), anchorRect.centerY())
        when (mGravity) {
            Gravity.START -> {
                location.x = anchorRect.left - mPopupWindow!!.contentView.width - mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.END -> {
                location.x = anchorRect.right + mMargin
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            Gravity.TOP -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorRect.top - mPopupWindow!!.contentView.height - mMargin
            }
            Gravity.BOTTOM -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorRect.bottom + mMargin
            }
            Gravity.CENTER -> {
                location.x = anchorCenter.x - mPopupWindow!!.contentView.width / 2f
                location.y = anchorCenter.y - mPopupWindow!!.contentView.height / 2f
            }
            else -> throw IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.")
        }
        return location
    }
    
    private fun configContentView() {
        if (mContentView is TextView) {
            mContentView.text = mText
        } else {
            val tv = mContentView!!.findViewById<View>(mTextViewId) as TextView
            tv.text = mText
        }
        mContentView.setPadding(mPadding.toInt(), mPadding.toInt(), mPadding.toInt(), mPadding.toInt())
        val linearLayout = LinearLayout(mContext)
        linearLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.orientation =
            if (mArrowDirection == ArrowDrawable.LEFT || mArrowDirection == ArrowDrawable.RIGHT) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        val layoutPadding = (if (mAnimated) mAnimationPadding else 0).toInt()
        linearLayout.setPadding(layoutPadding, layoutPadding, layoutPadding, layoutPadding)
        if (mShowArrow) {
            mArrowView = ImageView(mContext)
            mArrowView!!.setImageDrawable(mArrowDrawable)
            val arrowLayoutParams: LayoutParams = if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                LayoutParams(mArrowWidth.toInt(), mArrowHeight.toInt(), 0f)
            } else {
                LayoutParams(mArrowHeight.toInt(), mArrowWidth.toInt(), 0f)
            }
            arrowLayoutParams.gravity = Gravity.CENTER
            mArrowView!!.layoutParams = arrowLayoutParams
            if (mArrowDirection == ArrowDrawable.BOTTOM || mArrowDirection == ArrowDrawable.RIGHT) {
                linearLayout.addView(mContentView)
                linearLayout.addView(mArrowView)
            } else {
                linearLayout.addView(mArrowView)
                linearLayout.addView(mContentView)
            }
        } else {
            linearLayout.addView(mContentView)
        }
        val contentViewParams = LayoutParams(width, height, 0f)
        contentViewParams.gravity = Gravity.CENTER
        mContentView.layoutParams = contentViewParams
        mContentLayout = linearLayout
        mContentLayout!!.visibility = View.INVISIBLE
        if (mFocusable) {
            mContentLayout!!.isFocusableInTouchMode = true
            mContentLayout!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            dismiss()
                            return@OnKeyListener true
                        }
                    }
                }
                false
            })
        }
        mPopupWindow!!.contentView = mContentLayout
    }
    
    fun dismiss() {
        if (dismissed) return
        dismissed = true
        if (mPopupWindow != null) {
            mPopupWindow!!.dismiss()
        }
    }
    
    val isShowing: Boolean
        /**
         * <div class="pt">Indica se o tooltip está sendo exibido na tela.</div>
         * <div class=en">Indicate whether this tooltip is showing on screen.</div>
         *
         * @return <div class="pt"><tt>true</tt> se o tooltip estiver sendo exibido, <tt>false</tt> caso contrário</div>
         * <div class="en"><tt>true</tt> if the popup is showing, <tt>false</tt> otherwise</div>
         */
        get() = mPopupWindow != null && mPopupWindow!!.isShowing
    
    fun <T : View?> findViewById(id: Int): T {
        return mContentLayout!!.findViewById<View>(id) as T
    }
    
    override fun onDismiss() {
        dismissed = true
        if (mAnimator != null) {
            mAnimator!!.removeAllListeners()
            mAnimator!!.end()
            mAnimator!!.cancel()
            mAnimator = null
        }
        if (mRootView != null && mOverlay != null) {
            mRootView!!.removeView(mOverlay)
        }
        mRootView = null
        mOverlay = null
        if (mOnDismissListener != null) mOnDismissListener!!.onDismiss(this)
        mOnDismissListener = null
        removeOnGlobalLayoutListener(mPopupWindow!!.contentView, mLocationLayoutListener)
        removeOnGlobalLayoutListener(mPopupWindow!!.contentView, mArrowLayoutListener)
        removeOnGlobalLayoutListener(mPopupWindow!!.contentView, mShowLayoutListener)
        removeOnGlobalLayoutListener(mPopupWindow!!.contentView, mAnimationLayoutListener)
        removeOnGlobalLayoutListener(mPopupWindow!!.contentView, mAutoDismissLayoutListener)
        mPopupWindow = null
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private val mOverlayTouchListener = View.OnTouchListener { v, event -> mModal }
    
    private val mLocationLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            if (mMaxWidth > 0 && mContentView!!.width > mMaxWidth) {
                setWidth(mContentView, mMaxWidth)
                popup.update(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                return
            }
            removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mArrowLayoutListener)
            val location = calculatePopupLocation()
            popup.isClippingEnabled = true
            popup.update(location.x.toInt(), location.y.toInt(), popup.width, popup.height)
            popup.contentView.requestLayout()
            createOverlay()
        }
    }
    
    private val mArrowLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            removeOnGlobalLayoutListener(popup.contentView, this)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mAnimationLayoutListener)
            popup.contentView.viewTreeObserver.addOnGlobalLayoutListener(mShowLayoutListener)
            if (mShowArrow) {
                val achorRect = calculateRectOnScreen(
                    mAnchorView!!
                )
                val contentViewRect = calculateRectOnScreen(
                    mContentLayout!!
                )
                var x: Float
                var y: Float
                if (mArrowDirection == ArrowDrawable.TOP || mArrowDirection == ArrowDrawable.BOTTOM) {
                    x = mContentLayout!!.paddingLeft + pxFromDp(2f)
                    val centerX = contentViewRect.width() / 2f - mArrowView!!.width / 2f
                    val newX = centerX - (contentViewRect.centerX() - achorRect.centerX())
                    if (newX > x) {
                        x = if (newX + mArrowView!!.width + x > contentViewRect.width()) {
                            contentViewRect.width() - mArrowView!!.width - x
                        } else {
                            newX
                        }
                    }
                    y = mArrowView!!.top.toFloat()
                    y += if (mArrowDirection == ArrowDrawable.BOTTOM) -1 else +1
                } else {
                    y = mContentLayout!!.paddingTop + pxFromDp(2f)
                    val centerY = contentViewRect.height() / 2f - mArrowView!!.height / 2f
                    val newY = centerY - (contentViewRect.centerY() - achorRect.centerY())
                    if (newY > y) {
                        y = if (newY + mArrowView!!.height + y > contentViewRect.height()) {
                            contentViewRect.height() - mArrowView!!.height - y
                        } else {
                            newY
                        }
                    }
                    x = mArrowView!!.left.toFloat()
                    x += if (mArrowDirection == ArrowDrawable.RIGHT) -1 else +1
                }
                setX(mArrowView!!, x.toInt())
                setY(mArrowView!!, y.toInt())
            }
            popup.contentView.requestLayout()
        }
    }
    
    private val mShowLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            removeOnGlobalLayoutListener(popup.contentView, this)
            if (mOnShowListener != null) mOnShowListener!!.onShow(this@SimpleTooltip)
            mOnShowListener = null
            mContentLayout!!.visibility = View.VISIBLE
        }
    }
    
    private val mAnimationLayoutListener: OnGlobalLayoutListener = object : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val popup = mPopupWindow
            if (popup == null || dismissed) return
            removeOnGlobalLayoutListener(popup.contentView, this)
            if (mAnimated) startAnimation()
            popup.contentView.requestLayout()
        }
    }
    
    private fun startAnimation() {
        val property = if (mGravity == Gravity.TOP || mGravity == Gravity.BOTTOM) "translationY" else "translationX"
        val anim1 = ObjectAnimator.ofFloat(mContentLayout, property, -mAnimationPadding, mAnimationPadding)
        anim1.duration = mAnimationDuration
        anim1.interpolator = AccelerateDecelerateInterpolator()
        val anim2 = ObjectAnimator.ofFloat(mContentLayout, property, mAnimationPadding, -mAnimationPadding)
        anim2.duration = mAnimationDuration
        anim2.interpolator = AccelerateDecelerateInterpolator()
        mAnimator = AnimatorSet()
        mAnimator!!.playSequentially(anim1, anim2)
        mAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!dismissed && isShowing) {
                    animation.start()
                }
            }
        })
        mAnimator!!.start()
    }
    /**
     * <div class="pt">Listener utilizado para chamar o <tt>SimpleTooltip#dismiss()</tt> quando a <tt>View</tt> root é encerrada sem que a tooltip seja fechada.
     * Pode ocorrer quando a tooltip é utilizada dentro de Dialogs.</div>
     */
    private val mAutoDismissLayoutListener = OnGlobalLayoutListener {
        val popup = mPopupWindow
        if (popup == null || dismissed) return@OnGlobalLayoutListener
        if (!mRootView!!.isShown) dismiss()
    }
    
    interface OnDismissListener {
        fun onDismiss(tooltip: SimpleTooltip?)
    }
    
    interface OnShowListener {
        fun onShow(tooltip: SimpleTooltip?)
    }
    
    /**
     * <div class="pt">Classe responsável por facilitar a criação do objeto <tt>SimpleTooltip</tt>.</div>
     * <div class="en">Class responsible for making it easier to build the object <tt>SimpleTooltip</tt>.</div>
     *
     * @author Created by douglas on 05/05/16.
     */
    @Suppress("unused")
    class Builder(context: Context) {
        val context: Context?
        var dismissOnInsideTouch = true
        var dismissOnOutsideTouch = true
        var modal = false
        var contentView: View? = null
        @IdRes var textViewId = id.text1
        var text: CharSequence = ""
        var anchorView: View? = null
        var arrowDirection = ArrowDrawable.AUTO
        var gravity = Gravity.BOTTOM
        var transparentOverlay = true
        var overlayOffset = -1f
        var overlayMatchParent = true
        var maxWidth = 0f
        var showArrow = true
        var arrowDrawable: Drawable? = null
        var animated = false
        var margin = -1f
        var padding = -1f
        var animationPadding = -1f
        var onDismissListener: OnDismissListener? = null
        var onShowListener: OnShowListener? = null
        var animationDuration: Long = 0
        private var backgroundColor = 0
        private var textColor = 0
        private var arrowColor = 0
        var arrowHeight = 0f
        var arrowWidth = 0f
        var focusable: Boolean
        var cornerRadius = 0f
        var highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL
        var width = ViewGroup.LayoutParams.WRAP_CONTENT
        var height = ViewGroup.LayoutParams.WRAP_CONTENT
        var ignoreOverlay = false
        var overlayWindowBackgroundColor = 0
        
        init {
            this.context = context
            focusable = !context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        }
        
        @Throws(IllegalArgumentException::class)
        fun build(): SimpleTooltip {
            validateArguments()
            if (backgroundColor == 0) {
                backgroundColor = getColor(context!!, mDefaultBackgroundColorRes)
            }
            if (overlayWindowBackgroundColor == 0) {
                overlayWindowBackgroundColor = Color.BLACK
            }
            if (textColor == 0) {
                textColor = getColor(context!!, mDefaultTextColorRes)
            }
            if (contentView == null) {
                val tv = TextView(context)
                setTextAppearance(tv, mDefaultTextAppearanceRes)
                tv.setBackgroundColor(backgroundColor)
                tv.setTextColor(textColor)
                contentView = tv
            }
            if (arrowColor == 0) {
                arrowColor = getColor(context!!, mDefaultArrowColorRes)
            }
            if (margin < 0) {
                margin = context!!.resources.getDimension(mDefaultMarginRes)
            }
            if (padding < 0) {
                padding = context!!.resources.getDimension(mDefaultPaddingRes)
            }
            if (animationPadding < 0) {
                animationPadding = context!!.resources.getDimension(mDefaultAnimationPaddingRes)
            }
            if (animationDuration == 0L) {
                animationDuration = context!!.resources.getInteger(mDefaultAnimationDurationRes).toLong()
            }
            if (showArrow) {
                if (arrowDirection == ArrowDrawable.AUTO) arrowDirection = tooltipGravityToArrowDirection(gravity)
                if (arrowDrawable == null) arrowDrawable = ArrowDrawable(arrowColor, arrowDirection)
                if (arrowWidth == 0f) arrowWidth = context!!.resources.getDimension(mDefaultArrowWidthRes)
                if (arrowHeight == 0f) arrowHeight = context!!.resources.getDimension(mDefaultArrowHeightRes)
            }
            if (highlightShape < 0 || highlightShape > OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR_ROUNDED) {
                highlightShape = OverlayView.HIGHLIGHT_SHAPE_OVAL
            }
            if (overlayOffset < 0) {
                overlayOffset = context!!.resources.getDimension(mDefaultOverlayOffsetRes)
            }
            return SimpleTooltip(this)
        }
        
        @Throws(IllegalArgumentException::class)
        private fun validateArguments() {
            requireNotNull(context) { "Context not specified." }
            requireNotNull(anchorView) { "Anchor view not specified." }
        }
        
        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }
        
        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }
        
        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param textView <div class="pt">novo conteúdo para o tooltip.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(textView: TextView?): Builder {
            contentView = textView
            textViewId = 0
            return this
        }
        
        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentView <div class="pt">novo conteúdo para o tooltip, pode ser um <tt>[ViewGroup]</tt> ou qualquer componente customizado.</div>
         * @param textViewId  <div class="pt">resId para o <tt>[TextView]</tt> existente dentro do <tt>[Builder.contentView]</tt>. Padrão é <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(contentView: View?, @IdRes textViewId: Int): Builder {
            this.contentView = contentView
            this.textViewId = textViewId
            return this
        }
        
        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para o tooltip.</div>
         * @param textViewId    <div class="pt">resId para o <tt>[TextView]</tt> existente dentro do <tt>[Builder.contentView]</tt>. Padrão é <tt>android.R.id.text1</tt>.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(@LayoutRes contentViewId: Int, @IdRes textViewId: Int): Builder {
            val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            contentView = inflater.inflate(contentViewId, null, false)
            this.textViewId = textViewId
            return this
        }
        
        /**
         * <div class="pt">Define um novo conteúdo customizado para o tooltip.</div>
         *
         * @param contentViewId <div class="pt">layoutId que será inflado como o novo conteúdo para o tooltip.</div>
         * @return this
         * @see Builder.contentView
         * @see Builder.contentView
         * @see Builder.contentView
         */
        fun contentView(@LayoutRes contentViewId: Int): Builder {
            val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            contentView = inflater.inflate(contentViewId, null, false)
            textViewId = 0
            return this
        }
        
        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique dentro de sua área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnInsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o click dentro, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnOutsideTouch
         */
        fun dismissOnInsideTouch(dismissOnInsideTouch: Boolean): Builder {
            this.dismissOnInsideTouch = dismissOnInsideTouch
            return this
        }
        
        /**
         * <div class="pt">Define se o tooltip será fechado quando receber um clique fora de sua área. Padrão é <tt>true</tt>.</div>
         *
         * @param dismissOnOutsideTouch <div class="pt"><tt>true</tt> para fechar quando receber o click fora, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnInsideTouch
         */
        fun dismissOnOutsideTouch(dismissOnOutsideTouch: Boolean): Builder {
            this.dismissOnOutsideTouch = dismissOnOutsideTouch
            return this
        }
        
        /**
         * <div class="pt">Define se a tela fiacrá bloqueada enquanto o tooltip estiver aberto.
         * Esse parâmetro deve ser combinado com <tt>[Builder.dismissOnInsideTouch]</tt> e <tt>[Builder.dismissOnOutsideTouch]</tt>.
         * Padrão é <tt>false</tt>.</div>
         *
         * @param modal <div class="pt"><tt>true</tt> para bloquear a tela, <tt>false</tt> caso contrário.</div>
         * @return this
         * @see Builder.dismissOnInsideTouch
         * @see Builder.dismissOnOutsideTouch
         */
        fun modal(modal: Boolean): Builder {
            this.modal = modal
            return this
        }
        
        /**
         * <div class="pt">Define o texto que sera exibido no <tt>[TextView]</tt> dentro do tooltip.</div>
         *
         * @param text <div class="pt">texto que sera exibido.</div>
         * @return this
         */
        fun text(text: CharSequence): Builder {
            this.text = text
            return this
        }
        
        /**
         * <div class="pt">Define o texto que sera exibido no <tt>[TextView]</tt> dentro do tooltip.</div>
         *
         * @param textRes <div class="pt">id do resource da String.</div>
         * @return this
         */
        fun text(@StringRes textRes: Int): Builder {
            text = context!!.getString(textRes)
            return this
        }
        
        /**
         * <div class="pt">Define para qual <tt>[View]</tt> o tooltip deve apontar. Importante ter certeza que esta <tt>[View]</tt> já esteja pronta e exibida na tela.</div>
         * <div class="en">Set the target <tt>[View]</tt> that the tooltip will point. Make sure that the anchor <tt>[View]</tt> shold be showing in the screen.</div>
         *
         * @param anchorView <div class="pt"><tt>View</tt> para qual o tooltip deve apontar</div>
         * <div class="en"><tt>View</tt> that the tooltip will point</div>
         * @return this
         */
        fun anchorView(anchorView: View?): Builder {
            this.anchorView = anchorView
            return this
        }
        
        /**
         * <div class="pt">Define a para qual lado o tooltip será posicionado em relação ao <tt>anchorView</tt>.
         * As opções existentes são <tt>[Gravity.START]</tt>, <tt>[Gravity.END]</tt>, <tt>[Gravity.TOP]</tt> e <tt>[Gravity.BOTTOM]</tt>.
         * O padrão é <tt>[Gravity.BOTTOM]</tt>.</div>
         *
         * @param gravity <div class="pt">lado para qual o tooltip será posicionado.</div>
         * @return this
         */
        fun gravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }
        
        /**
         * <div class="pt">Define a direção em que a seta será criada.
         * As opções existentes são <tt>[ArrowDrawable.LEFT]</tt>, <tt>[ArrowDrawable.TOP]</tt>, <tt>[ArrowDrawable.RIGHT]</tt>,
         * <tt>[ArrowDrawable.BOTTOM]</tt> e <tt>[ArrowDrawable.AUTO]</tt>.
         * O padrão é <tt>[ArrowDrawable.AUTO]</tt>. <br></br>
         * Esta opção deve ser utilizada em conjunto com  <tt>Builder#showArrow(true)</tt>.</div>
         *
         * @param arrowDirection <div class="pt">direção em que a seta será criada.</div>
         * @return this
         */
        fun arrowDirection(arrowDirection: Int): Builder {
            this.arrowDirection = arrowDirection
            return this
        }
        
        /**
         * <div class="pt">Define se o fundo da tela será escurecido ou transparente enquanto o tooltip estiver aberto. Padrão é <tt>true</tt>.</div>
         *
         * @param transparentOverlay <div class="pt"><tt>true</tt> para o fundo transparente, <tt>false</tt> para escurecido.</div>
         * @return this
         */
        fun transparentOverlay(transparentOverlay: Boolean): Builder {
            this.transparentOverlay = transparentOverlay
            return this
        }
        
        /**
         * <div class="pt">Define a largura máxima do Tooltip.</div>
         *
         * @param maxWidthRes <div class="pt">resId da largura máxima.</div>
         * @return <tt>this</tt>
         * @see Builder.maxWidth
         */
        fun maxWidth(@DimenRes maxWidthRes: Int): Builder {
            maxWidth = context!!.resources.getDimension(maxWidthRes)
            return this
        }
        
        /**
         * <div class="pt">Define a largura máxima do Tooltip. Padrão é <tt>0</tt> (sem limite).</div>
         *
         * @param maxWidth <div class="pt">largura máxima em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.maxWidth
         */
        fun maxWidth(maxWidth: Float): Builder {
            this.maxWidth = maxWidth
            return this
        }
        
        /**
         * <div class="pt">Define se o tooltip será animado enquanto estiver aberto. Disponível a partir do Android API 11. Padrão é <tt>false</tt>.</div>
         *
         * @param animated <div class="pt"><tt>true</tt> para tooltip animado, <tt>false</tt> caso contrário.</div>
         * @return this
         */
        fun animated(animated: Boolean): Builder {
            this.animated = animated
            return this
        }
        
        /**
         * <div class="pt">Define o tamanho do deslocamento durante a animação. Padrão é o valor de <tt>[R.dimen.simpletooltip_animation_padding]</tt>.</div>
         *
         * @param animationPadding <div class="pt">tamanho do deslocamento em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.animationPadding
         */
        fun animationPadding(animationPadding: Float): Builder {
            this.animationPadding = animationPadding
            return this
        }
        
        /**
         * <div class="pt">Define o tamanho do deslocamento durante a animação. Padrão é <tt>[R.dimen.simpletooltip_animation_padding]</tt>.</div>
         *
         * @param animationPaddingRes <div class="pt">resId do tamanho do deslocamento.</div>
         * @return <tt>this</tt>
         * @see Builder.animationPadding
         */
        fun animationPadding(@DimenRes animationPaddingRes: Int): Builder {
            animationPadding = context!!.resources.getDimension(animationPaddingRes)
            return this
        }
        
        fun animationDuration(animationDuration: Long): Builder {
            this.animationDuration = animationDuration
            return this
        }
        
        /**
         * <div class="pt">Define o padding entre a borda do Tooltip e seu conteúdo. Padrão é o valor <tt>[R.dimen.simpletooltip_padding]</tt>.</div>
         *
         * @param padding <div class="pt">tamanho do padding em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.padding
         */
        fun padding(padding: Float): Builder {
            this.padding = padding
            return this
        }
        
        /**
         * <div class="pt">Define o padding entre a borda do Tooltip e seu conteúdo. Padrão é <tt>[R.dimen.simpletooltip_padding]</tt>.</div>
         *
         * @param paddingRes <div class="pt">resId do tamanho do padding.</div>
         * @return <tt>this</tt>
         * @see Builder.padding
         */
        fun padding(@DimenRes paddingRes: Int): Builder {
            padding = context!!.resources.getDimension(paddingRes)
            return this
        }
        
        /**
         * <div class="pt">Define a margem entre o Tooltip e o <tt>anchorView</tt>. Padrão é o valor de <tt>[R.dimen.simpletooltip_margin]</tt>.</div>
         *
         * @param margin <div class="pt">tamanho da margem em pixels.</div>
         * @return <tt>this</tt>
         * @see Builder.margin
         */
        fun margin(margin: Float): Builder {
            this.margin = margin
            return this
        }
        
        /**
         * <div class="pt">Define a margem entre o Tooltip e o <tt>anchorView</tt>. Padrão é <tt>[R.dimen.simpletooltip_margin]</tt>.</div>
         *
         * @param marginRes <div class="pt">resId do tamanho da margem.</div>
         * @return <tt>this</tt>
         * @see Builder.margin
         */
        fun margin(@DimenRes marginRes: Int): Builder {
            margin = context!!.resources.getDimension(marginRes)
            return this
        }
        
        fun textColor(textColor: Int): Builder {
            this.textColor = textColor
            return this
        }
        
        fun backgroundColor(@ColorInt backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }
        
        fun overlayWindowBackgroundColor(@ColorInt overlayWindowBackgroundColor: Int): Builder {
            this.overlayWindowBackgroundColor = overlayWindowBackgroundColor
            return this
        }
        
        /**
         * <div class="pt">Indica se deve ser gerada a seta indicativa. Padrão é <tt>true</tt>.</div>
         * <div class="en">Indicates whether to be generated indicative arrow. Default is <tt>true</tt>.</div>
         *
         * @param showArrow <div class="pt"><tt>true</tt> para exibir a seta, <tt>false</tt> caso contrário.</div>
         * <div class="en"><tt>true</tt> to show arrow, <tt>false</tt> otherwise.</div>
         * @return this
         */
        fun showArrow(showArrow: Boolean): Builder {
            this.showArrow = showArrow
            return this
        }
        
        fun arrowDrawable(arrowDrawable: Drawable?): Builder {
            this.arrowDrawable = arrowDrawable
            return this
        }
        
        fun arrowDrawable(@DrawableRes drawableRes: Int): Builder {
            arrowDrawable = getDrawable(context!!, drawableRes)
            return this
        }
        
        fun arrowColor(@ColorInt arrowColor: Int): Builder {
            this.arrowColor = arrowColor
            return this
        }
        
        /**
         * <div class="pt">Altura da seta indicativa. Esse valor é automaticamente definido em Largura ou Altura conforme a <tt>[android.view.Gravity]</tt> configurada.
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_arrow_height]</tt></div>
         * <div class="en">Height of the arrow. This value is automatically set in the Width or Height as the <tt>[android.view.Gravity]</tt>.</div>
         *
         * @param arrowHeight <div class="pt">Altura em pixels.</div>
         * <div class="en">Height in pixels.</div>
         * @return this
         * @see Builder.arrowWidth
         */
        fun arrowHeight(arrowHeight: Float): Builder {
            this.arrowHeight = arrowHeight
            return this
        }
        
        /**
         * <div class="pt">Largura da seta indicativa. Esse valor é automaticamente definido em Largura ou Altura conforme a <tt>[android.view.Gravity]</tt> configurada.
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_arrow_width]</tt></div>
         * <div class="en">Width of the arrow. This value is automatically set in the Width or Height as the <tt>[android.view.Gravity]</tt>.</div>
         *
         * @param arrowWidth <div class="pt">Largura em pixels.</div>
         * <div class="en">Width in pixels.</div>
         * @return this
         */
        fun arrowWidth(arrowWidth: Float): Builder {
            this.arrowWidth = arrowWidth
            return this
        }
        
        fun onDismissListener(onDismissListener: OnDismissListener?): Builder {
            this.onDismissListener = onDismissListener
            return this
        }
        
        fun onShowListener(onShowListener: OnShowListener?): Builder {
            this.onShowListener = onShowListener
            return this
        }
        
        /**
         * <div class="pt">Habilita o foco no conteúdo da tooltip. Padrão é <tt>false</tt> em dispositivos sensíveis ao toque e <tt>true</tt> em dispositivos não sensíveis ao toque.</div>
         * <div class="en">Enables focus in the tooltip content. Default is <tt>false</tt> on touch devices, and <tt>true</tt> on non-touch devices.</div>
         *
         * @param focusable <div class="pt">Pode receber o foco.</div>
         * <div class="en">Can receive focus.</div>
         * @return this
         */
        fun focusable(focusable: Boolean): Builder {
            this.focusable = focusable
            return this
        }
        
        /**
         * <div class="pt">Configura o formato do Shape em destaque. <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_OVAL]</tt> - Destaque oval (padrão). <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR]</tt> - Destaque retangular. <br></br>
        </div> *
         *
         *
         * <div class="en">Configure the the Shape type. <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_OVAL]</tt> - Shape oval (default). <br></br>
         * <tt>[OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR]</tt> - Shape rectangular. <br></br>
        </div> *
         *
         * @param highlightShape <div class="pt">Formato do Shape.</div>
         * <div class="en">Shape type.</div>
         * @return this
         * @see OverlayView.HIGHLIGHT_SHAPE_OVAL
         *
         * @see OverlayView.HIGHLIGHT_SHAPE_RECTANGULAR
         *
         * @see Builder.transparentOverlay
         */
        fun highlightShape(highlightShape: Int): Builder {
            this.highlightShape = highlightShape
            return this
        }
        
        fun cornerRadius(radius: Float): Builder {
            cornerRadius = radius
            return this
        }
        
        /**
         * <div class="pt">Tamanho da margem entre [Builder.anchorView] e a borda do Shape de destaque.
         * Este valor sobrescreve <tt>[R.dimen.simpletooltip_overlay_offset]</tt></div>
         * <div class="en">Margin between [Builder.anchorView] and highlight Shape border.
         * This value override <tt>[R.dimen.simpletooltip_overlay_offset]</tt></div>
         *
         * @param overlayOffset <div class="pt">Tamanho em pixels.</div>
         * <div class="en">Size in pixels.</div>
         * @return this
         * @see Builder.anchorView
         * @see Builder.transparentOverlay
         */
        fun overlayOffset(@Dimension overlayOffset: Float): Builder {
            this.overlayOffset = overlayOffset
            return this
        }
        
        /**
         * <div class="pt">Define o comportamento do overlay. Utilizado para casos onde a view de Overlay não pode ser MATCH_PARENT.
         * Como em uma Dialog ou DialogFragment.</div>
         * <div class="en">Sets the behavior of the overlay view. Used for cases where the Overlay view can not be MATCH_PARENT.
         * Like in a Dialog or DialogFragment.</div>
         *
         * @param overlayMatchParent <div class="pt">True se o overlay deve ser MATCH_PARENT. False se ele deve obter o mesmo tamanho do pai.</div>
         * <div class="en">True if the overlay should be MATCH_PARENT. False if it should get the same size as the parent.</div>
         * @return this
         */
        fun overlayMatchParent(overlayMatchParent: Boolean): Builder {
            this.overlayMatchParent = overlayMatchParent
            return this
        }
        
        /**
         * As some dialogs have a problem when displaying tooltip (like expand/subtract) container, this ignores overlay adding altogether.
         * @param ignoreOverlay flag to ignore overlay adding
         * @return this
         */
        fun ignoreOverlay(ignoreOverlay: Boolean): Builder {
            this.ignoreOverlay = ignoreOverlay
            return this
        }
    }
}