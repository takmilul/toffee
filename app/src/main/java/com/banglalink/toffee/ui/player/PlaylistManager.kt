package com.banglalink.toffee.ui.player

import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Log

class PlaylistManager {
    
    var playlistId: Long = -1L
    private val playList: MutableList<ChannelInfo> = mutableListOf()
    private var playlistIndex = -1
    
    fun getCurrentChannel(): ChannelInfo? {
        return playList.getOrNull(playlistIndex)
    }
    
    fun getNextChannel(): ChannelInfo? {
        return if (hasNext()) playList.getOrNull(playlistIndex + 1) else null
    }
    
    fun getPreviousChannel(): ChannelInfo? {
        return if (hasPrevious()) playList.getOrNull(playlistIndex - 1) else null
    }
    
    fun setPlaylist(channelInfo: ChannelInfo) {
        playList.clear()
        playList.add(channelInfo)
        playlistIndex = 0
        playlistId = -1
    }
    
    fun setPlayList(pdata: AddToPlaylistData) {
        if(pdata.playlistId == -1L) {
            playList.clear()
            playList.addAll(pdata.items)
            return
        }
        if(pdata.playlistId == playlistId) {
            Log.i("PLAYLIST_DEBUG", "Playlist ID ${pdata.playlistId} is same, prev size - ${playList.size}, loading size - ${pdata.items.size}")
            if(pdata.items.size != playList.size && pdata.items.isNotEmpty()) {
                val currentChannel = getCurrentChannel()
                if(!pdata.append) {
                    Log.i("PLAYLIST_DEBUG", "Reloading data with size - ${pdata.items.size}")
                    playList.clear()
                } else {
                    Log.i("PLAYLIST_DEBUG", "Appending data with size - ${pdata.items.size}")
                }
                playList.addAll(pdata.items)
                currentChannel?.let {
                    setChannelId(it.id.toInt())
                }
            }
        } else {
            Log.i("PLAYLIST_DEBUG", "Playlist ID is NOT same")
            if(pdata.replaceList) {
                Log.i("PLAYLIST_DEBUG", "Replacing list with ID - ${pdata.playlistId}")
                playList.clear()
                playList.addAll(pdata.items)
                playlistId = pdata.playlistId
            } else {
                Log.i("PLAYLIST_DEBUG", "Ignoring data from ${pdata.playlistId}")
            }
        }
    }
    
    fun clearPlaylist() {
        playList.clear()
        playlistIndex = -1
        playlistId = -1
    }
    
    fun hasPrevious() = if(playlistId < 0) false else playlistIndex > 0
    
    fun hasNext() = playlistIndex < playList.size - 1
    
    fun setIndex(index: Int) {
        playlistIndex = index
    }
    
    fun isNextChannelPremium(): Boolean {
        return hasNext() && playList.getOrNull(playlistIndex + 1)?.urlTypeExt == 1 
    }
    
    fun isPreviousChannelPremium(): Boolean {
        return hasPrevious() && playList.getOrNull(playlistIndex - 1)?.urlTypeExt == 1 
    }
    
    fun setChannelId(channelId: Int) {
        playlistIndex = playList.indexOfFirst { it.id.toInt() == channelId }
        if(playlistIndex < 0 && playList.isNotEmpty()) playlistIndex = 0
    }
    
    fun nextChannel() {
        if(playlistId < 0) {
            if(playList.size > 1) {
                playList.removeAt(0)
            }
            return
        }
        if(hasNext()) playlistIndex++
    }
    
    fun previousChannel() {
        if(hasPrevious()) playlistIndex--
    }
}