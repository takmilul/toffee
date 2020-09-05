package com.banglalink.toffee.showcase_view

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.content.ContextCompat
import com.banglalink.toffee.showcase_view.IAnimationFactory.AnimationEndListener
import com.banglalink.toffee.showcase_view.IAnimationFactory.AnimationStartListener
import com.banglalink.toffee.showcase_view.shape.*
import com.banglalink.toffee.showcase_view.target.Target
import com.banglalink.toffee.showcase_view.target.ViewTarget
import java.util.*

/**
 * Helper class to show a sequence of showcase views.
 */
class MaterialShowcaseView : FrameLayout, OnTouchListener, View.OnClickListener {
    var DEFAULT_DELAY: Long = 0
    var DEFAULT_FADE_TIME: Long = 300
    private var mOldHeight = 0
    private var mOldWidth = 0
    private var mBitmap // = new WeakReference<>(null);
        : Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mEraser: Paint? = null
    private var mTarget: Target? = null
    private var mShape: Shape? = null
    private var mXPosition = 0
    private var mYPosition = 0
    private var mWasDismissed = false
    private var mWasSkipped = false
    private var mShapePadding = DEFAULT_SHAPE_PADDING
    private var tooltipMargin = DEFAULT_TOOLTIP_MARGIN
    private var mContentBox: View? = null
    private var mTitleTextView: TextView? = null
    private var mContentTextView: TextView? = null
    
    //private TextView mDismissButton;
    private var mHasCustomGravity = false
    private var mSkipButton: Button? = null
    private var mNextButton: Button? = null
    private var mBulletView: LinearLayout? = null
    private var mGravity = 0
    private var mContentBottomMargin = 0
    private var mContentTopMargin = 0
    private var mDismissOnTouch = false
    private var mShouldRender = false // flag to decide when we should actually render
    private var mRenderOverNav = false
    private var mMaskColour = 0
    private var mAnimationFactory: IAnimationFactory? = null
    private val mShouldAnimate = true
    private var mUseFadeAnimation = false
    private var mFadeDurationInMillis = DEFAULT_FADE_TIME
    private var mHandler: Handler? = null
    private var mDelayInMillis = DEFAULT_DELAY
    private var mBottomMargin = 0
    private var mSingleUse = false // should display only once
    private var mPrefsManager // used to store state doe single use mode
        : PrefsManager? = null
    var mListeners // external listeners who want to observe when we show and dismiss
        : MutableList<IShowcaseListener>? = null
    var mNextClickListener: () -> Unit? = {}
    private var mLayoutListener: UpdateOnGlobalLayout? = null
    private var mDetachedListener: IDetachedListener? = null
    private var mTargetTouchable = false
    private var mDismissOnTargetTouch = true
    private var isSequence = false
    private var toolTip: ShowcaseTooltip? = null
    private var toolTipShown = false

    private var mTotalItem: Int = 1
    private var mActiveItem: Int = 1
    
    constructor(context: Context) : super(context) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }
    
    private fun init(context: Context) {
        setWillNotDraw(false)
        mListeners = ArrayList()
        
        // make sure we add a global layout listener so we can adapt to changes
        mLayoutListener = UpdateOnGlobalLayout()
        viewTreeObserver.addOnGlobalLayoutListener(mLayoutListener)
        
        // consume touch events
        setOnTouchListener(this)
        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR)
        visibility = INVISIBLE
        val contentView = LayoutInflater.from(getContext()).inflate(R.layout.custom_view, this, true)
        //contentView = (LinearLayout)((CardView)((LinearLayout)((MaterialShowcaseView) contentView).getChildAt(0)).getChildAt(0)).getChildAt(0);
        mContentBox = contentView.findViewById(R.id.content_box)
        mTitleTextView = contentView.findViewById(R.id.tv_title)
        mContentTextView = contentView.findViewById(R.id.tv_content)
        mBulletView = contentView.findViewById(R.id.bulletView)
        /*mDismissButton = contentView.findViewById(R.id.tv_dismiss);
        mDismissButton.setOnClickListener(this);*/
        mNextButton = contentView.findViewById(R.id.nextButton)
        mNextButton!!.setOnClickListener(this)
        mSkipButton = contentView.findViewById(R.id.tv_skip)
        mSkipButton!!.setOnClickListener(this)
    }
    
    fun setTotalItem(totalItem: Int){
        mTotalItem = totalItem
    }
    
    fun setActiveItem(activeItem: Int){
        mActiveItem = activeItem
    }
    
    fun buildBullets() {
        mBulletView!!.removeAllViews()
        if (mTotalItem > 1) {
            mBulletView!!.visibility = VISIBLE
            repeat(mTotalItem) {
                val imageView = ImageView(context)
                if (it < mActiveItem) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selected_dot))
                }
                else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.default_dot))
                }
                val layoutParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParam.setMargins(8, 0, 8, 0)
                mBulletView!!.addView(imageView, layoutParam)
            }
            if (mTotalItem == mActiveItem){
                mNextButton!!.text = "Finish"
            }
        }
    }
    
    /**
     * Interesting drawing stuff.
     * We draw a block of semi transparent colour to fill the whole screen then we draw of transparency
     * to create a circular "viewport" through to the underlying content
     *
     * @param canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // don't bother drawing if we're not ready
        if (!mShouldRender) return
        
        // get current dimensions
        val width = measuredWidth
        val height = measuredHeight
        
        // don't bother drawing if there is nothing to draw on
        if (width <= 0 || height <= 0) return
        
        // build a new canvas if needed i.e first pass or new dimensions
        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
            if (mBitmap != null) mBitmap!!.recycle()
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)
        }
        
        // save our 'old' dimensions
        mOldWidth = width
        mOldHeight = height
        
        // clear canvas
        mCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        
        // draw solid background
        mCanvas!!.drawColor(mMaskColour)
        
        // Prepare eraser Paint if needed
        if (mEraser == null) {
            mEraser = Paint()
            mEraser!!.color = -0x1
            mEraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            mEraser!!.flags = Paint.ANTI_ALIAS_FLAG
        }
        
        // draw (erase) shape
        mShape!!.draw(mCanvas, mEraser, mXPosition, mYPosition)
        
        // Draw the bitmap on our views  canvas.
        canvas.drawBitmap(mBitmap!!, 0f, 0f, null)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        /**
         * If we're being detached from the window without the mWasDismissed flag then we weren't purposefully dismissed
         * Probably due to an orientation change or user backed out of activity.
         * Ensure we reset the flag so the showcase display again.
         */
        if (!mWasDismissed && mSingleUse && mPrefsManager != null) {
            mPrefsManager!!.resetShowcase()
        }
        notifyOnDismissed()
    }
    
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mDismissOnTouch) {
            hide()
        }
        if (mTargetTouchable && mTarget!!.bounds.contains(event.x.toInt(), event.y.toInt())) {
            if (mDismissOnTargetTouch) {
                hide()
            }
            return false
        }
        return true
    }
    
    private fun notifyOnDisplayed() {
        if (mListeners != null) {
            for (listener in mListeners!!) {
                listener.onShowcaseDisplayed(this)
            }
        }
    }
    
    private fun notifyOnDismissed() {
        if (mListeners != null) {
            for (listener in mListeners!!) {
                listener.onShowcaseDismissed(this)
            }
            mListeners!!.clear()
            mListeners = null
        }
        /**
         * internal listener used by sequence for storing progress within the sequence
         */
        if (mDetachedListener != null) {
            mDetachedListener!!.onShowcaseDetached(this, mWasDismissed, mWasSkipped)
        }
    }
    
    /**
     * Dismiss button clicked
     *
     * @param v
     */
    override fun onClick(v: View) {
        when (v.id) {
            /*R.id.tv_dismiss -> {
                hide()
            }*/
            R.id.tv_skip -> {
                skip()
            }
            R.id.nextButton -> {
                hide()
                mNextClickListener()
            }
        }
    }
    
    fun setOnNextClickListener(listener: () -> Unit) {
        mNextClickListener = listener
    }
    
    /**
     * Overrides the automatic handling of gravity and sets it to a specific one. Due to this,
     * margins are also reset to zero.
     *
     * @param gravity
     */
    fun setGravity(gravity: Int) {
        mHasCustomGravity = Gravity.NO_GRAVITY != gravity
        if (mHasCustomGravity) {
            mGravity = gravity
            mContentBottomMargin = 0
            mContentTopMargin = mContentBottomMargin
        }
        applyLayoutParams()
    }
    
    /**
     * Tells us about the "Target" which is the view we want to anchor to.
     * We figure out where it is on screen and (optionally) how big it is.
     * We also figure out whether to place our content and dismiss button above or below it.
     *
     * @param target
     */
    fun setTarget(target: Target?) {
        mTarget = target
        
        // update dismiss button state
        updateDismissButton()
        if (mTarget != null) {
            /**
             * If we're on lollipop then make sure we don't draw over the nav bar
             */
            if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBottomMargin = softButtonsBarSizePort
                val contentLP: LayoutParams? = layoutParams as LayoutParams?
                if (contentLP != null && contentLP.bottomMargin != mBottomMargin) contentLP.bottomMargin = mBottomMargin
            }
            
            // apply the target position
            val targetPoint = mTarget!!.point
            val targetBounds = mTarget!!.bounds
            setPosition(targetPoint)
            
            // now figure out whether to put content above or below it
            val height = measuredHeight
            val midPoint = height / 2
            val yPos = targetPoint.y
            var radius = Math.max(targetBounds.height(), targetBounds.width()) / 2
            if (mShape != null) {
                mShape!!.updateTarget(mTarget)
                radius = mShape!!.height / 2
            }
            
            // If there's no custom gravity in place, we'll do automatic gravity calculation.
            if (!mHasCustomGravity) {
                if (yPos > midPoint) {
                    // target is in lower half of screen, we'll sit above it
                    mContentTopMargin = 0
                    mContentBottomMargin = height - yPos + radius + mShapePadding
                    mGravity = Gravity.BOTTOM
                } else {
                    // target is in upper half of screen, we'll sit below it
                    mContentTopMargin = yPos + radius + mShapePadding
                    mContentBottomMargin = 0
                    mGravity = Gravity.TOP
                }
            }
        }
        applyLayoutParams()
    }
    
    private fun applyLayoutParams() {
        if (mContentBox != null && mContentBox!!.layoutParams != null) {
            val contentLP = mContentBox!!.layoutParams as LayoutParams
            var layoutParamsChanged = false
            if (contentLP.bottomMargin != mContentBottomMargin) {
                contentLP.bottomMargin = mContentBottomMargin
                layoutParamsChanged = true
            }
            if (contentLP.topMargin != mContentTopMargin) {
                contentLP.topMargin = mContentTopMargin
                layoutParamsChanged = true
            }
            if (contentLP.gravity != mGravity) {
                contentLP.gravity = mGravity
                layoutParamsChanged = true
            }
            /**
             * Only apply the layout params if we've actually changed them, otherwise we'll get stuck in a layout loop
             */
            if (layoutParamsChanged) mContentBox!!.layoutParams = contentLP
            updateToolTip()
        }
    }
    
    fun updateToolTip() {
        /**
         * Adjust tooltip gravity if needed
         */
        if (toolTip != null) {
            if (!toolTipShown) {
                toolTipShown = true
                val shapeDiameter = mShape!!.totalRadius * 2
                var toolTipDistance = (shapeDiameter - mTarget!!.bounds.height()) / 2
                toolTipDistance += tooltipMargin
                toolTip!!.show(toolTipDistance)
            }
            if (mGravity == Gravity.BOTTOM) {
                toolTip!!.position(ShowcaseTooltip.Position.TOP)
            } else {
                toolTip!!.position(ShowcaseTooltip.Position.BOTTOM)
            }
        }
    }
    
    /**
     * SETTERS
     */
    fun setPosition(point: Point) {
        setPosition(point.x, point.y)
    }
    
    fun setPosition(x: Int, y: Int) {
        mXPosition = x
        mYPosition = y
    }
    
    private fun setTitleText(contentText: CharSequence) {
        if (mTitleTextView != null && contentText != "") {
            mContentTextView!!.alpha = 0.5f
            mTitleTextView!!.text = contentText
        }
    }
    
    private fun setContentText(contentText: CharSequence) {
        if (mContentTextView != null) {
            mContentTextView!!.text = contentText
        }
    }
    
    private fun setToolTip(toolTip: ShowcaseTooltip) {
        this.toolTip = toolTip
    }
    
    private fun setIsSequence(isSequenceB: Boolean) {
        isSequence = isSequenceB
    }
    
    private fun setDismissText(dismissText: CharSequence) {
        /*if (mDismissButton != null) {
            mDismissButton.setText(dismissText);
            updateDismissButton();
        }*/
    }
    
    private fun setSkipText(skipText: CharSequence) {
        if (mSkipButton != null) {
            mSkipButton!!.text = skipText
            updateSkipButton()
        }
    }
    
    private fun setDismissStyle(dismissStyle: Typeface) {
        /*if (mDismissButton != null) {
            mDismissButton.setTypeface(dismissStyle);
            updateDismissButton();
        }*/
    }
    
    private fun setSkipStyle(skipStyle: Typeface) {
        if (mSkipButton != null) {
            mSkipButton!!.typeface = skipStyle
            updateSkipButton()
        }
    }
    
    private fun setTitleTextColor(textColour: Int) {
        if (mTitleTextView != null) {
            mTitleTextView!!.setTextColor(textColour)
        }
    }
    
    private fun setContentTextColor(textColour: Int) {
        if (mContentTextView != null) {
            mContentTextView!!.setTextColor(textColour)
        }
    }
    
    private fun setDismissTextColor(textColour: Int) {
        /*if (mDismissButton != null) {
            mDismissButton.setTextColor(textColour);
        }*/
    }
    
    private fun setShapePadding(padding: Int) {
        mShapePadding = padding
    }
    
    private fun setTooltipMargin(margin: Int) {
        tooltipMargin = margin
    }
    
    private fun setDismissOnTouch(dismissOnTouch: Boolean) {
        mDismissOnTouch = dismissOnTouch
    }
    
    private fun setShouldRender(shouldRender: Boolean) {
        mShouldRender = shouldRender
    }
    
    private fun setMaskColour(maskColour: Int) {
        mMaskColour = maskColour
    }
    
    private fun setDelay(delayInMillis: Long) {
        mDelayInMillis = delayInMillis
    }
    
    private fun setFadeDuration(fadeDurationInMillis: Long) {
        mFadeDurationInMillis = fadeDurationInMillis
    }
    
    private fun setTargetTouchable(targetTouchable: Boolean) {
        mTargetTouchable = targetTouchable
    }
    
    private fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean) {
        mDismissOnTargetTouch = dismissOnTargetTouch
    }
    
    private fun setUseFadeAnimation(useFadeAnimation: Boolean) {
        mUseFadeAnimation = useFadeAnimation
    }
    
    fun addShowcaseListener(showcaseListener: IShowcaseListener) {
        if (mListeners != null) mListeners!!.add(showcaseListener)
    }
    
    fun removeShowcaseListener(showcaseListener: IShowcaseListener) {
        if (mListeners != null && mListeners!!.contains(showcaseListener)) {
            mListeners!!.remove(showcaseListener)
        }
    }
    
    fun setDetachedListener(detachedListener: IDetachedListener?) {
        mDetachedListener = detachedListener
    }
    
    fun setShape(mShape: Shape?) {
        this.mShape = mShape
    }
    
    fun setAnimationFactory(animationFactory: IAnimationFactory?) {
        mAnimationFactory = animationFactory
    }
    
    /**
     * Set properties based on a config object
     *
     * @param config
     */
    fun setConfig(config: ShowcaseConfig) {
        if (config.delay > -1) {
            setDelay(config.delay)
        }
        if (config.fadeDuration > 0) {
            setFadeDuration(config.fadeDuration)
        }
        if (config.contentTextColor > 0) {
            setContentTextColor(config.contentTextColor)
        }
        if (config.dismissTextColor > 0) {
            setDismissTextColor(config.dismissTextColor)
        }
        if (config.dismissTextStyle != null) {
            setDismissStyle(config.dismissTextStyle!!)
        }
        if (config.maskColor > 0) {
            setMaskColour(config.maskColor)
        }
        if (config.shape != null) {
            setShape(config.shape)
        }
        if (config.shapePadding > -1) {
            setShapePadding(config.shapePadding)
        }
        if (config.renderOverNavigationBar != null) {
            setRenderOverNavigationBar(config.renderOverNavigationBar!!)
        }
    }
    
    fun updateDismissButton() {
        // hide or show button
        /*if (mDismissButton != null) {
            if (TextUtils.isEmpty(mDismissButton.getText())) {
                mDismissButton.setVisibility(GONE);
            } else {
                mDismissButton.setVisibility(VISIBLE);
            }
        }*/
    }
    
    fun updateSkipButton() {
        // hide or show button
        if (mSkipButton != null) {
            if (TextUtils.isEmpty(mSkipButton!!.text)) {
                mSkipButton!!.visibility = GONE
            } else {
                mSkipButton!!.visibility = VISIBLE
            }
        }
    }
    
    fun hasFired(): Boolean {
        return mPrefsManager!!.hasFired()
    }
    
    /**
     * REDRAW LISTENER - this ensures we redraw after activity finishes laying out
     */
    private inner class UpdateOnGlobalLayout : OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            setTarget(mTarget)
        }
    }
    
    /**
     * BUILDER CLASS
     * Gives us a builder utility class with a fluent API for eaily configuring showcase views
     */
    class Builder(private val activity: Activity) {
        private var fullWidth = false
        private var shapeType = CIRCLE_SHAPE
        val showcaseView: MaterialShowcaseView
        
        /**
         * Enforces a user-specified gravity instead of relying on the library to do that.
         */
        fun setGravity(gravity: Int): Builder {
            showcaseView.setGravity(gravity)
            return this
        }
        
        fun setOnNextClickListener(listener: () -> Unit): Builder {
            showcaseView.setOnNextClickListener(listener)
            return this
        }
        
        /**
         * Set the title text shown on the ShowcaseView.
         */
        fun setTarget(target: View?): Builder {
            showcaseView.setTarget(ViewTarget(target!!))
            return this
        }
        
        fun setSequence(isSequence: Boolean): Builder {
            showcaseView.setIsSequence(isSequence)
            return this
        }
        
        /**
         * Set the dismiss button properties
         */
        fun setDismissText(resId: Int): Builder {
            return setDismissText(activity.getString(resId))
        }
        
        fun setDismissText(dismissText: CharSequence): Builder {
            showcaseView.setDismissText(dismissText)
            return this
        }
        
        fun setDismissStyle(dismissStyle: Typeface): Builder {
            showcaseView.setDismissStyle(dismissStyle)
            return this
        }
        
        /**
         * Set the skip button properties
         */
        fun setSkipText(resId: Int): Builder {
            return setSkipText(activity.getString(resId))
        }
        
        fun setSkipText(skipText: CharSequence): Builder {
            showcaseView.setSkipText(skipText)
            return this
        }
        
        fun setSkipStyle(skipStyle: Typeface): Builder {
            showcaseView.setSkipStyle(skipStyle)
            return this
        }
        
        /**
         * Set the content text shown on the ShowcaseView.
         */
        fun setContentText(resId: Int): Builder {
            return setContentText(activity.getString(resId))
        }
        
        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        fun setContentText(text: CharSequence): Builder {
            showcaseView.setContentText(text)
            return this
        }
        
        /**
         * Set the title text shown on the ShowcaseView.
         */
        fun setTitleText(resId: Int): Builder {
            return setTitleText(activity.getString(resId))
        }
        
        /**
         * Set the descriptive text shown on the ShowcaseView as the title.
         */
        fun setTitleText(text: CharSequence): Builder {
            showcaseView.setTitleText(text)
            return this
        }
        
        /**
         * Tooltip mode config options
         *
         * @param toolTip
         */
        fun setToolTip(toolTip: ShowcaseTooltip): Builder {
            showcaseView.setToolTip(toolTip)
            return this
        }
        
        /**
         * Set whether or not the target view can be touched while the showcase is visible.
         *
         *
         * False by default.
         */
        fun setTargetTouchable(targetTouchable: Boolean): Builder {
            showcaseView.setTargetTouchable(targetTouchable)
            return this
        }
        
        /**
         * Set whether or not the showcase should dismiss when the target is touched.
         *
         *
         * True by default.
         */
        fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean): Builder {
            showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch)
            return this
        }
        
        fun setDismissOnTouch(dismissOnTouch: Boolean): Builder {
            showcaseView.setDismissOnTouch(dismissOnTouch)
            return this
        }
        
        fun setMaskColour(maskColour: Int): Builder {
            showcaseView.setMaskColour(maskColour)
            return this
        }
        
        fun setTitleTextColor(textColour: Int): Builder {
            showcaseView.setTitleTextColor(textColour)
            return this
        }
        
        fun setContentTextColor(textColour: Int): Builder {
            showcaseView.setContentTextColor(textColour)
            return this
        }
        
        fun setDismissTextColor(textColour: Int): Builder {
            showcaseView.setDismissTextColor(textColour)
            return this
        }
        
        fun setDelay(delayInMillis: Int): Builder {
            showcaseView.setDelay(delayInMillis.toLong())
            return this
        }
        
        fun setFadeDuration(fadeDurationInMillis: Int): Builder {
            showcaseView.setFadeDuration(fadeDurationInMillis.toLong())
            return this
        }
        
        fun setListener(listener: IShowcaseListener): Builder {
            showcaseView.addShowcaseListener(listener)
            return this
        }
        
        fun singleUse(showcaseID: String): Builder {
            showcaseView.singleUse(showcaseID)
            return this
        }
        
        fun setShape(shape: Shape?): Builder {
            showcaseView.setShape(shape)
            return this
        }
        
        fun withCircleShape(): Builder {
            shapeType = CIRCLE_SHAPE
            return this
        }
        
        fun withOvalShape(): Builder {
            shapeType = OVAL_SHAPE
            return this
        }
        
        fun withoutShape(): Builder {
            shapeType = NO_SHAPE
            return this
        }
        
        fun setShapePadding(padding: Int): Builder {
            showcaseView.setShapePadding(padding)
            return this
        }
        
        fun setTooltipMargin(margin: Int): Builder {
            showcaseView.setTooltipMargin(margin)
            return this
        }
        
        @JvmOverloads
        fun withRectangleShape(fullWidth: Boolean = false): Builder {
            shapeType = RECTANGLE_SHAPE
            this.fullWidth = fullWidth
            return this
        }
        
        fun renderOverNavigationBar(): Builder {
            // Note: This only has an effect in Lollipop or above.
            showcaseView.setRenderOverNavigationBar(true)
            return this
        }
        
        fun useFadeAnimation(): Builder {
            showcaseView.setUseFadeAnimation(true)
            return this
        }
        
        fun build(): MaterialShowcaseView {
            if (showcaseView.mShape == null) {
                when (shapeType) {
                    RECTANGLE_SHAPE -> {
                        showcaseView.setShape(RectangleShape(showcaseView.mTarget!!.bounds, fullWidth))
                    }
                    CIRCLE_SHAPE -> {
                        showcaseView.setShape(CircleShape(showcaseView.mTarget!!))
                    }
                    NO_SHAPE -> {
                        showcaseView.setShape(NoShape())
                    }
                    OVAL_SHAPE -> {
                        showcaseView.setShape(OvalShape(showcaseView.mTarget!!))
                    }
                    else -> {
                        showcaseView.setShape(CircleShape(showcaseView.mTarget!!))
                    }
                }
            }
            if (showcaseView.mAnimationFactory == null) {
                // create our animation factory
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !showcaseView.mUseFadeAnimation) {
                    showcaseView.setAnimationFactory(CircularRevealAnimationFactory())
                } else {
                    showcaseView.setAnimationFactory(FadeAnimationFactory())
                }
            }
            showcaseView.mShape!!.setPadding(showcaseView.mShapePadding)
            return showcaseView
        }
        
        fun show(): MaterialShowcaseView {
            build().show(activity)
            return showcaseView
        }
        
        companion object {
            private const val CIRCLE_SHAPE = 0
            private const val RECTANGLE_SHAPE = 1
            private const val NO_SHAPE = 2
            private const val OVAL_SHAPE = 3
        }
        
        init {
            showcaseView = MaterialShowcaseView(activity)
        }
    }
    
    private fun singleUse(showcaseID: String) {
        mSingleUse = true
        mPrefsManager = PrefsManager(context, showcaseID)
    }
    
    fun removeFromWindow() {
        if (parent != null && parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
        if (mBitmap != null) {
            mBitmap!!.recycle()
            mBitmap = null
        }
        mEraser = null
        mAnimationFactory = null
        mCanvas = null
        mHandler = null
        viewTreeObserver.removeGlobalOnLayoutListener(mLayoutListener)
        mLayoutListener = null
        if (mPrefsManager != null) mPrefsManager!!.close()
        mPrefsManager = null
    }
    
    /**
     * Reveal the showcaseview. Returns a boolean telling us whether we actually did show anything
     *
     * @param activity
     * @return
     */
    fun show(activity: Activity): Boolean {
        /**
         * if we're in single use mode and have already shot our bolt then do nothing
         */
        if (mSingleUse) {
            if (mPrefsManager!!.hasFired()) {
                return false
            } else {
                mPrefsManager!!.setFired()
            }
        }
        (activity.window.decorView as ViewGroup).addView(this)
        setShouldRender(true)
        if (toolTip != null) {
            if (mTarget !is ViewTarget) {
                throw RuntimeException("The target must be of type: " + ViewTarget::class.java.canonicalName)
            }
            val viewTarget = mTarget as ViewTarget
            toolTip!!.configureTarget(this, viewTarget.view)
        }
        mHandler = Handler()
        mHandler!!.postDelayed({
            val attached: Boolean
            // taken from https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/core/src/main/java/androidx/core/view/ViewCompat.java#3310
            attached = if (Build.VERSION.SDK_INT >= 19) {
                isAttachedToWindow
            } else {
                windowToken != null
            }
            if (mShouldAnimate && attached) {
                fadeIn()
            } else {
                visibility = VISIBLE
                notifyOnDisplayed()
            }
        }, mDelayInMillis)
        updateDismissButton()
        return true
    }
    
    fun hide() {
        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was dismissed purposefully (by the user or programmatically)
         */
        mWasDismissed = true
        if (mShouldAnimate) {
            animateOut()
        } else {
            removeFromWindow()
        }
    }
    
    fun skip() {
        /**
         * This flag is used to indicate to onDetachedFromWindow that the showcase view was skipped purposefully (by the user or programmatically)
         */
        mWasSkipped = true
        if (mShouldAnimate) {
            animateOut()
        } else {
            removeFromWindow()
        }
    }
    
    fun fadeIn() {
        visibility = INVISIBLE
        mAnimationFactory!!.animateInView(this, mTarget!!.point, mFadeDurationInMillis,
            object : AnimationStartListener {
                override fun onAnimationStart() {
                    visibility = VISIBLE
                    notifyOnDisplayed()
                }
            }
        )
    }
    
    fun animateOut() {
        mAnimationFactory!!.animateOutView(this, mTarget!!.point, mFadeDurationInMillis, object : AnimationEndListener {
            override fun onAnimationEnd() {
                visibility = INVISIBLE
                removeFromWindow()
            }
        })
    }
    
    fun resetSingleUse() {
        if (mSingleUse && mPrefsManager != null) mPrefsManager!!.resetShowcase()
    }
    
    val softButtonsBarSizePort: Int
        get() {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }
    
    private fun setRenderOverNavigationBar(mRenderOverNav: Boolean) {
        this.mRenderOverNav = mRenderOverNav
    }
    
    companion object {
        const val DEFAULT_SHAPE_PADDING = 10
        const val DEFAULT_TOOLTIP_MARGIN = 10
        
        /**
         * Static helper method for resetting single use flag
         *
         * @param context
         * @param showcaseID
         */
        @JvmStatic
        fun resetSingleUse(context: Context?, showcaseID: String?) {
            PrefsManager.resetShowcase(context, showcaseID)
        }
        
        /**
         * Static helper method for resetting all single use flags
         *
         * @param context
         */
        @JvmStatic
        fun resetAll(context: Context?) {
            PrefsManager.resetAll(context!!)
        }
    }
}