package com.banglalink.toffee.listeners

interface OnPlayerControllerChangedListener {
    fun onPlayButtonPressed(currentState: Int): Boolean
    fun onFullScreenButtonPressed(): Boolean
    fun onDrawerButtonPressed(): Boolean
    fun onMinimizeButtonPressed(): Boolean
    fun onOptionMenuPressed(): Boolean
    fun onShareButtonPressed(): Boolean
    fun onPlayerIdleDueToError()
    fun onRotationLock(isAutoRotationEnabled: Boolean)
    fun onSeekPosition(position: Int): Boolean
    fun onControllerVisible()
    fun onControllerInVisible()
    fun onMediaItemChanged(){}
}