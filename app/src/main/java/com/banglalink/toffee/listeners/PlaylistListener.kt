package com.banglalink.toffee.listeners

interface PlaylistListener {
    fun hasPrevious(): Boolean
    fun hasNext(): Boolean
    fun playNext()
    fun playPrevious()
    fun isAutoplayEnabled(): Boolean
}