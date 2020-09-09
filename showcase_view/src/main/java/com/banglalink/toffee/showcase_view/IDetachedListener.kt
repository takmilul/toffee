package com.banglalink.toffee.showcase_view


interface IDetachedListener {
    fun onShowcaseDetached(showcaseView: MaterialShowcaseView?, wasDismissed: Boolean, wasSkipped: Boolean)
}