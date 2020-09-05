package com.banglalink.toffee.showcase_view

import android.app.Activity
import android.view.View
import java.util.*

class MaterialShowcaseSequence(private var mActivity: Activity, private val mTotalItem: Int? = 1) : IDetachedListener {
    var mPrefsManager: PrefsManager? = null
    var mShowcaseQueue: Queue<MaterialShowcaseView>
    private var mActiveItem: Int = 0
    private var mSingleUse = false
    private var mConfig: ShowcaseConfig? = null
    private var mSequencePosition = 0
    private var mOnItemShownListener: OnSequenceItemShownListener? = null
    private var mOnItemDismissedListener: OnSequenceItemDismissedListener? = null

    constructor(activity: Activity, sequenceID: String?) : this(activity) {
        singleUse(sequenceID)
    }

    constructor(activity: Activity, sequenceID: String?, totalItem: Int? = 1) : this(activity, totalItem) {
        singleUse(sequenceID)
    }

    fun addSequenceItem(targetView: View?, content: String?, dismissText: String?): MaterialShowcaseSequence {
        addSequenceItem(targetView, "", content, dismissText)
        return this
    }

    fun addSequenceItem(targetView: View?, title: String?, content: String?, dismissText: String?): MaterialShowcaseSequence {
        val sequenceItem = MaterialShowcaseView.Builder(mActivity)
                .setTarget(targetView)
                .setTitleText(title!!)
                .setDismissText(dismissText!!)
                .setContentText(content!!)
                .setSequence(true)
                .build()
        if (mConfig != null) {
            sequenceItem.setConfig(mConfig!!)
        }
        sequenceItem.apply {
            setTotalItem(mTotalItem!!)
            setActiveItem(++mActiveItem)
            buildBullets()
        }
        mShowcaseQueue.add(sequenceItem)
        return this
    }

    fun addSequenceItem(sequenceItem: MaterialShowcaseView): MaterialShowcaseSequence {
        if (mConfig != null) {
            sequenceItem.setConfig(mConfig!!)
        }
        sequenceItem.apply {
            setTotalItem(mTotalItem!!)
            setActiveItem(++mActiveItem)
            buildBullets()
        }
        mShowcaseQueue.add(sequenceItem)
        return this
    }

    fun singleUse(sequenceID: String?): MaterialShowcaseSequence {
        mSingleUse = true
        mPrefsManager = PrefsManager(mActivity, sequenceID)
        return this
    }

    fun setOnItemShownListener(listener: OnSequenceItemShownListener?) {
        mOnItemShownListener = listener
    }

    fun setOnItemDismissedListener(listener: OnSequenceItemDismissedListener?) {
        mOnItemDismissedListener = listener
    }

    private fun hasFired(): Boolean {
        return mPrefsManager!!.sequenceStatus == PrefsManager.SEQUENCE_FINISHED
    }

    fun start() {
        /**
         * Check if we've already shot our bolt and bail out if so         *
         */
        if (mSingleUse) {
            if (hasFired()) {
                return
            }
            /**
             * See if we have started this sequence before, if so then skip to the point we reached before
             * instead of showing the user everything from the start
             */
            mSequencePosition = mPrefsManager!!.sequenceStatus
            if (mSequencePosition > 0) {
                for (i in 0 until mSequencePosition) {
                    mShowcaseQueue.poll()
                }
            }
        }


        // do start
        if (mShowcaseQueue.size > 0) showNextItem()
    }

    private fun showNextItem() {
        if (mShowcaseQueue.size > 0 && !mActivity.isFinishing) {
            val sequenceItem = mShowcaseQueue.remove()
            sequenceItem.setDetachedListener(this)
            sequenceItem.show(mActivity)
            if (mOnItemShownListener != null) {
                mOnItemShownListener!!.onShow(sequenceItem, mSequencePosition)
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mPrefsManager!!.setFired()
            }
        }
    }

    private fun skipTutorial() {
        mShowcaseQueue.clear()
        if (mShowcaseQueue.size > 0 && !mActivity.isFinishing) {
            val sequenceItem = mShowcaseQueue.remove()
            sequenceItem.setDetachedListener(this)
            sequenceItem.show(mActivity)
            if (mOnItemShownListener != null) {
                mOnItemShownListener!!.onShow(sequenceItem, mSequencePosition)
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mPrefsManager!!.setFired()
            }
        }
    }

    override fun onShowcaseDetached(showcaseView: MaterialShowcaseView?, wasDismissed: Boolean, wasSkipped: Boolean) {
        showcaseView!!.setDetachedListener(null)
        /**
         * We're only interested if the showcase was purposefully dismissed
         */
        if (wasDismissed) {
            if (mOnItemDismissedListener != null) {
                mOnItemDismissedListener!!.onDismiss(showcaseView, mSequencePosition)
            }
            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
            if (mPrefsManager != null) {
                mSequencePosition++
                mPrefsManager!!.sequenceStatus = mSequencePosition
            }
            showNextItem()
        }
        if (wasSkipped) {
            if (mOnItemDismissedListener != null) {
                mOnItemDismissedListener!!.onDismiss(showcaseView, mSequencePosition)
            }
            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
            if (mPrefsManager != null) {
                mSequencePosition++
                mPrefsManager!!.sequenceStatus = mSequencePosition
            }
            skipTutorial()
        }
    }

    fun setConfig(config: ShowcaseConfig?) {
        mConfig = config
    }

    interface OnSequenceItemShownListener {
        fun onShow(itemView: MaterialShowcaseView?, position: Int)
    }

    interface OnSequenceItemDismissedListener {
        fun onDismiss(itemView: MaterialShowcaseView?, position: Int)
    }

    init {
        mShowcaseQueue = LinkedList()
    }
}