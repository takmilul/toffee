package com.banglalink.toffee.ui.player

interface SeekListener {
    /**
     * Called when video start reached during rewinding
     */
    fun onVideoStartReached() {}

    /**
     * Called when video end reached during forwarding
     */
    fun onVideoEndReached() {}
}