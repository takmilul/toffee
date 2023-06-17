package com.banglalink.toffee.listeners

interface PlaylistListener {
    fun hasPrevious(): Boolean
    fun hasNext(): Boolean
    fun playNext(): Boolean
    fun playPrevious(): Boolean
    fun isAutoplayEnabled(): Boolean
}