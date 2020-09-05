package com.banglalink.toffee.showcase_view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*


/**
 * Base on original code by florentchampigny
 * https://github.com/florent37/ViewTooltip
 */
class ShowcaseTooltip private constructor(context: Context) {
    private var rootView: View? = null
    private var view: View? = null
    private var tooltipView: TooltipView? = null
    
    companion object {
        @JvmStatic
        fun build(context: Context): ShowcaseTooltip {
            return ShowcaseTooltip(context)
        }
        
        private fun getActivityContext(context: Context): Activity? {
            var context: Context? = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }
    }
    
    init {
        val myContext = MyContext(getActivityContext(context))
        tooltipView = TooltipView(myContext.getContext())
    }
    
    fun configureTarget(rootView: ViewGroup?, view: View?) {
        this.rootView = rootView
        this.view = view
    }
    
    fun position(position: Position): ShowcaseTooltip {
        tooltipView!!.setPosition(position)
        return this
    }
    
    fun customView(customView: View): ShowcaseTooltip {
        tooltipView!!.setCustomView(customView)
        return this
    }
    
    fun customView(viewId: Int): ShowcaseTooltip {
        tooltipView!!.setCustomView((view!!.context as Activity).findViewById(viewId))
        return this
    }
    
    fun arrowWidth(arrowWidth: Int): ShowcaseTooltip {
        tooltipView!!.setArrowWidth(arrowWidth)
        return this
    }
    
    fun arrowHeight(arrowHeight: Int): ShowcaseTooltip {
        tooltipView!!.setArrowHeight(arrowHeight)
        return this
    }
    
    fun arrowSourceMargin(arrowSourceMargin: Int): ShowcaseTooltip {
        tooltipView!!.setArrowSourceMargin(arrowSourceMargin)
        return this
    }
    
    fun arrowTargetMargin(arrowTargetMargin: Int): ShowcaseTooltip {
        tooltipView!!.setArrowTargetMargin(arrowTargetMargin)
        return this
    }
    
    fun align(align: ALIGN?): ShowcaseTooltip {
        tooltipView!!.setAlign(align)
        return this
    }
    
    fun show(margin: Int): TooltipView {
        val activityContext = tooltipView!!.context
        if (activityContext != null && activityContext is Activity) {
            val decorView = if (rootView != null) rootView as ViewGroup else (activityContext.window.decorView as ViewGroup)
            view!!.postDelayed({
                val rect = Rect()
                view!!.getGlobalVisibleRect(rect)
                val rootGlobalRect = Rect()
                val rootGlobalOffset = Point()
                decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset)
                val location = IntArray(2)
                view!!.getLocationOnScreen(location)
                rect.left = location[0]
                if (rootGlobalOffset != null) {
                    rect.top -= rootGlobalOffset.y
                    rect.bottom -= rootGlobalOffset.y
                    rect.left -= rootGlobalOffset.x
                    rect.right -= rootGlobalOffset.x
                }
    
                // fixes bottom mode
                rect.top -= margin
    
                // fixes top mode
                rect.bottom += margin
                decorView.addView(tooltipView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                tooltipView!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        tooltipView!!.setup(rect, decorView.width)
                        tooltipView!!.viewTreeObserver.removeOnPreDrawListener(this)
                        return false
                    }
                })
            }, 100)
        }
        return tooltipView!!
    }
    
    fun color(color: Int): ShowcaseTooltip {
        tooltipView!!.setColor(color)
        return this
    }
    
    fun color(paint: Paint): ShowcaseTooltip {
        tooltipView!!.setPaint(paint)
        return this
    }
    
    fun onDisplay(listener: ListenerDisplay?): ShowcaseTooltip {
        tooltipView!!.setListenerDisplay(listener)
        return this
    }
    
    fun padding(left: Int, top: Int, right: Int, bottom: Int): ShowcaseTooltip {
        tooltipView!!.setViewPadding(left, top, right, bottom)
        return this
    }
    
    fun animation(tooltipAnimation: TooltipAnimation): ShowcaseTooltip {
        tooltipView!!.setTooltipAnimation(tooltipAnimation)
        return this
    }
    
    fun text(text: String?): ShowcaseTooltip {
        tooltipView!!.setText(text)
        return this
    }
    
    fun text(text: Int): ShowcaseTooltip {
        tooltipView!!.setText(text)
        return this
    }
    
    fun corner(corner: Int): ShowcaseTooltip {
        tooltipView!!.setCorner(corner)
        return this
    }
    
    fun textColor(textColor: Int): ShowcaseTooltip {
        tooltipView!!.setTextColor(textColor)
        return this
    }
    
    fun textTypeFace(typeface: Typeface?): ShowcaseTooltip {
        tooltipView!!.setTextTypeFace(typeface)
        return this
    }
    
    fun textSize(unit: Int, textSize: Float): ShowcaseTooltip {
        tooltipView!!.setTextSize(unit, textSize)
        return this
    }
    
    fun setTextGravity(textGravity: Int): ShowcaseTooltip {
        tooltipView!!.setTextGravity(textGravity)
        return this
    }
    
    fun distanceWithView(distance: Int): ShowcaseTooltip {
        tooltipView!!.setDistanceWithView(distance)
        return this
    }
    
    fun border(color: Int, width: Float): ShowcaseTooltip {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.color = color
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = width
        tooltipView!!.setBorderPaint(borderPaint)
        return this
    }
    
    enum class Position {
        LEFT, RIGHT, TOP, BOTTOM
    }
    
    enum class ALIGN {
        START, CENTER, END
    }
    
    interface TooltipAnimation {
        fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?)
        fun animateExit(view: View, animatorListener: Animator.AnimatorListener?)
    }
    
    interface ListenerDisplay {
        fun onDisplay(view: View?)
    }
    
    class FadeTooltipAnimation : TooltipAnimation {
        private var fadeDuration: Long = 400
        
        constructor() {}
        constructor(fadeDuration: Long) {
            this.fadeDuration = fadeDuration
        }
        
        override fun animateEnter(view: View, animatorListener: Animator.AnimatorListener?) {
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(fadeDuration).setListener(animatorListener)
        }
        
        override fun animateExit(view: View, animatorListener: Animator.AnimatorListener?) {
            view.animate().alpha(0f).setDuration(fadeDuration).setListener(animatorListener)
        }
    }
    
    class TooltipView(context: Context?) : FrameLayout(context!!) {
        private var arrowHeight = 15
        private var arrowWidth = 15
        private var arrowSourceMargin = 0
        private var arrowTargetMargin = 0
        protected var childView: View
        private var color = Color.parseColor("#FFFFFF")
        private var bubblePath: Path? = null
        private var bubblePaint: Paint
        private var borderPaint: Paint?
        private var position = Position.BOTTOM
        private var align: ALIGN? = ALIGN.CENTER
        private var listenerDisplay: ListenerDisplay? = null
        private var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()
        private var corner = 30
        private var mPaddingTop = 20
        private var mPaddingBottom = 30
        private var mPaddingRight = 60
        private var mPaddingLeft = 60
        private var viewRect: Rect? = null
        private var distanceWithView = 0
        fun setCustomView(customView: View) {
            removeView(childView)
            //this.removeView(customView);
            childView = customView
            //ViewGroup parent = ((ViewGroup)childView.getParent());
            //parent.removeView(childView);
            //ViewGroup parent2 = ((ViewGroup)customView.getParent());
            //parent2.removeView(customView);
            //this.removeAllViews();
            addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        fun setColor(color: Int) {
            this.color = color
            bubblePaint.color = color
            postInvalidate()
        }
        
        fun setPaint(paint: Paint) {
            bubblePaint = paint
            setLayerType(LAYER_TYPE_SOFTWARE, paint)
            postInvalidate()
        }
        
        fun setPosition(position: Position) {
            this.position = position
            when (position) {
                Position.TOP -> setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom + arrowHeight)
                Position.BOTTOM -> setPadding(mPaddingLeft, mPaddingTop + arrowHeight, mPaddingRight, mPaddingBottom)
                Position.LEFT -> setPadding(mPaddingLeft, mPaddingTop, mPaddingRight + arrowHeight, mPaddingBottom)
                Position.RIGHT -> setPadding(mPaddingLeft + arrowHeight, mPaddingTop, mPaddingRight, mPaddingBottom)
            }
            postInvalidate()
        }
        
        fun setAlign(align: ALIGN?) {
            this.align = align
            postInvalidate()
        }
        
        fun setText(text: String?) {
            if (childView is TextView) {
                (childView as TextView).text = Html.fromHtml(text)
            }
            postInvalidate()
        }
        
        fun setText(text: Int) {
            if (childView is TextView) {
                (childView as TextView).setText(text)
            }
            postInvalidate()
        }
        
        fun setTextColor(textColor: Int) {
            if (childView is TextView) {
                (childView as TextView).setTextColor(textColor)
            }
            postInvalidate()
        }
        
        fun getArrowHeight(): Int {
            return arrowHeight
        }
        
        fun setArrowHeight(arrowHeight: Int) {
            this.arrowHeight = arrowHeight
            postInvalidate()
        }
        
        fun getArrowWidth(): Int {
            return arrowWidth
        }
        
        fun setArrowWidth(arrowWidth: Int) {
            this.arrowWidth = arrowWidth
            postInvalidate()
        }
        
        fun getArrowSourceMargin(): Int {
            return arrowSourceMargin
        }
        
        fun setArrowSourceMargin(arrowSourceMargin: Int) {
            this.arrowSourceMargin = arrowSourceMargin
            postInvalidate()
        }
        
        fun getArrowTargetMargin(): Int {
            return arrowTargetMargin
        }
        
        fun setArrowTargetMargin(arrowTargetMargin: Int) {
            this.arrowTargetMargin = arrowTargetMargin
            postInvalidate()
        }
        
        fun setTextTypeFace(textTypeFace: Typeface?) {
            if (childView is TextView) {
                (childView as TextView).typeface = textTypeFace
            }
            postInvalidate()
        }
        
        fun setTextSize(unit: Int, size: Float) {
            if (childView is TextView) {
                (childView as TextView).setTextSize(unit, size)
            }
            postInvalidate()
        }
        
        fun setTextGravity(textGravity: Int) {
            if (childView is TextView) {
                (childView as TextView).gravity = textGravity
            }
            postInvalidate()
        }
        
        fun setViewPadding(left: Int, top: Int, right: Int, bottom: Int){
            mPaddingLeft = left
            mPaddingTop = top
            mPaddingRight = right
            mPaddingBottom = bottom
        }
        
        fun setCorner(corner: Int) {
            this.corner = corner
        }
        
        override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(width, height, oldw, oldh)
            bubblePath = drawBubble(RectF(0f, 0f, width.toFloat(), height.toFloat()), corner.toFloat(), corner.toFloat(), corner.toFloat(), corner.toFloat())
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (bubblePath != null) {
                canvas.drawPath(bubblePath!!, bubblePaint)
                if (borderPaint != null) {
                    canvas.drawPath(bubblePath!!, borderPaint!!)
                }
            }
        }
        
        fun setListenerDisplay(listener: ListenerDisplay?) {
            listenerDisplay = listener
        }
        
        fun setTooltipAnimation(tooltipAnimation: TooltipAnimation) {
            this.tooltipAnimation = tooltipAnimation
        }
        
        protected fun startEnterAnimation() {
            tooltipAnimation.animateEnter(this, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (listenerDisplay != null) {
                        listenerDisplay!!.onDisplay(this@TooltipView)
                    }
                }
            })
        }
        
        fun setupPosition(rect: Rect) {
            val x: Int
            val y: Int
            if (position == Position.LEFT || position == Position.RIGHT) {
                x = if (position == Position.LEFT) {
                    rect.left - width - distanceWithView
                } else {
                    rect.right + distanceWithView
                }
                y = rect.top + getAlignOffset(height, rect.height())
            } else {
                y = if (position == Position.BOTTOM) {
                    rect.bottom + distanceWithView
                } else { // top
                    rect.top - height - distanceWithView
                }
                x = rect.left + getAlignOffset(width, rect.width())
            }
            translationX = x.toFloat()
            translationY = y.toFloat()
        }
        
        private fun getAlignOffset(myLength: Int, hisLength: Int): Int {
            when (align) {
                ALIGN.END -> return hisLength - myLength
                ALIGN.CENTER -> return (hisLength - myLength) / 2
            }
            return 0
        }
        
        private fun drawBubble(myRect: RectF, topLeftDiameter: Float, topRightDiameter: Float, bottomRightDiameter: Float, bottomLeftDiameter: Float): Path {
            var topLeftDiameter = topLeftDiameter
            var topRightDiameter = topRightDiameter
            var bottomRightDiameter = bottomRightDiameter
            var bottomLeftDiameter = bottomLeftDiameter
            val path = Path()
            if (viewRect == null) return path
            topLeftDiameter = if (topLeftDiameter < 0) 0f else topLeftDiameter
            topRightDiameter = if (topRightDiameter < 0) 0f else topRightDiameter
            bottomLeftDiameter = if (bottomLeftDiameter < 0) 0f else bottomLeftDiameter
            bottomRightDiameter = if (bottomRightDiameter < 0) 0f else bottomRightDiameter
            val spacingLeft = 30f
            val spacingTop = if (position == Position.BOTTOM) arrowHeight.toFloat() else 0.toFloat()
            val spacingRight = 30f
            val spacingBottom = if (position == Position.TOP) arrowHeight.toFloat() else 0.toFloat()
            val left = spacingLeft + myRect.left
            val top = spacingTop + myRect.top
            val right = myRect.right - spacingRight
            val bottom = myRect.bottom - spacingBottom
            val centerX = viewRect!!.centerX() - x
            val arrowSourceX = if (Arrays.asList(Position.TOP, Position.BOTTOM).contains(position)) centerX + arrowSourceMargin else centerX
            val arrowTargetX = if (Arrays.asList(Position.TOP, Position.BOTTOM).contains(position)) centerX + arrowTargetMargin else centerX
            val arrowSourceY = if (Arrays.asList(Position.RIGHT, Position.LEFT).contains(position)) bottom / 2f - arrowSourceMargin else bottom / 2f
            val arrowTargetY = if (Arrays.asList(Position.RIGHT, Position.LEFT).contains(position)) bottom / 2f - arrowTargetMargin else bottom / 2f
            path.moveTo(left + topLeftDiameter / 2f, top)
            //LEFT, TOP
            if (position == Position.BOTTOM) {
                path.lineTo(arrowSourceX - arrowWidth, top)
                path.lineTo(arrowTargetX, myRect.top)
                path.lineTo(arrowSourceX + arrowWidth, top)
            }
            path.lineTo(right - topRightDiameter / 2f, top)
            path.quadTo(right, top, right, top + topRightDiameter / 2)
            //RIGHT, TOP
            if (position == Position.LEFT) {
                path.lineTo(right, arrowSourceY - arrowWidth)
                path.lineTo(myRect.right, arrowTargetY)
                path.lineTo(right, arrowSourceY + arrowWidth)
            }
            path.lineTo(right, bottom - bottomRightDiameter / 2)
            path.quadTo(right, bottom, right - bottomRightDiameter / 2, bottom)
            //RIGHT, BOTTOM
            if (position == Position.TOP) {
                path.lineTo(arrowSourceX + arrowWidth, bottom)
                path.lineTo(arrowTargetX, myRect.bottom)
                path.lineTo(arrowSourceX - arrowWidth, bottom)
            }
            path.lineTo(left + bottomLeftDiameter / 2, bottom)
            path.quadTo(left, bottom, left, bottom - bottomLeftDiameter / 2)
            //LEFT, BOTTOM
            if (position == Position.RIGHT) {
                path.lineTo(left, arrowSourceY + arrowWidth)
                path.lineTo(myRect.left, arrowTargetY)
                path.lineTo(left, arrowSourceY - arrowWidth)
            }
            path.lineTo(left, top + topLeftDiameter / 2)
            path.quadTo(left, top, left + topLeftDiameter / 2, top)
            path.close()
            return path
        }
        
        fun adjustSize(rect: Rect, screenWidth: Int): Boolean {
            val r = Rect()
            getGlobalVisibleRect(r)
            var changed = false
            val layoutParams = layoutParams
            if (position == Position.LEFT && width > rect.left) {
                layoutParams.width = rect.left - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView
                changed = true
            } else if (position == Position.RIGHT && rect.right + width > screenWidth) {
                layoutParams.width = screenWidth - rect.right - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView
                changed = true
            } else if (position == Position.TOP || position == Position.BOTTOM) {
                var adjustedLeft = rect.left
                var adjustedRight = rect.right
                if (rect.centerX() + width / 2f > screenWidth) {
                    val diff = rect.centerX() + width / 2f - screenWidth
                    adjustedLeft -= diff.toInt()
                    adjustedRight -= diff.toInt()
                    setAlign(ALIGN.CENTER)
                    changed = true
                } else if (rect.centerX() - width / 2f < 0) {
                    val diff = -(rect.centerX() - width / 2f)
                    adjustedLeft += diff.toInt()
                    adjustedRight += diff.toInt()
                    setAlign(ALIGN.CENTER)
                    changed = true
                }
                if (adjustedLeft < 0) {
                    adjustedLeft = 0
                }
                if (adjustedRight > screenWidth) {
                    adjustedRight = screenWidth
                }
                rect.left = adjustedLeft
                rect.right = adjustedRight
            }
            setLayoutParams(layoutParams)
            postInvalidate()
            return changed
        }
        
        private fun onSetup(myRect: Rect) {
            setupPosition(myRect)
            bubblePath = drawBubble(RectF(0f, 0f, width.toFloat(), height.toFloat()), corner.toFloat(), corner.toFloat(), corner.toFloat(), corner.toFloat())
            startEnterAnimation()
        }
        
        fun setup(viewRect: Rect?, screenWidth: Int) {
            this.viewRect = Rect(viewRect)
            val myRect = Rect(viewRect)
            val changed = adjustSize(myRect, screenWidth)
            if (!changed) {
                onSetup(myRect)
            } else {
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        onSetup(myRect)
                        viewTreeObserver.removeOnPreDrawListener(this)
                        return false
                    }
                })
            }
        }
        
        fun removeNow() {
            if (parent != null) {
                val parent = parent as ViewGroup
                parent.removeView(this@TooltipView)
            }
        }
        
        fun closeNow() {
            removeNow()
        }
        
        fun setDistanceWithView(distanceWithView: Int) {
            this.distanceWithView = distanceWithView
        }
        
        fun setBorderPaint(borderPaint: Paint?) {
            this.borderPaint = borderPaint
            postInvalidate()
        }
        
        companion object {
            private const val MARGIN_SCREEN_BORDER_TOOLTIP = 30
        }
        
        init {
            setWillNotDraw(false)
            childView = TextView(context)
            (childView as TextView).setTextColor(Color.BLACK)
            addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            childView.setPadding(0, 0, 0, 0)
            bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            bubblePaint.color = color
            bubblePaint.style = Paint.Style.FILL
            borderPaint = null
            setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint)
        }
    }
    
    class MyContext {
        private var fragment: Fragment? = null
        private var context: Context? = null
        private var activity: Activity? = null
        
        constructor(activity: Activity?) {
            this.activity = activity
        }
        
        constructor(fragment: Fragment?) {
            this.fragment = fragment
        }
        
        constructor(context: Context?) {
            this.context = context
        }
        
        fun getContext(): Context {
            return if (activity != null) {
                activity!!
            } else {
                fragment!!.activity as Context
            }
        }
        
        fun getActivity(): Activity {
            return if (activity != null) {
                activity!!
            } else {
                fragment!!.activity as Activity
            }
        }
        
        val window: Window
            get() = if (activity != null) {
                activity!!.window
            } else {
                if (fragment is DialogFragment) {
                    (fragment as DialogFragment).dialog!!.window!!
                } else fragment!!.activity!!.window
            }
    }
    
}