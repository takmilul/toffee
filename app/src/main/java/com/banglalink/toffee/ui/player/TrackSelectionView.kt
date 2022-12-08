package com.banglalink.toffee.ui.player

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.banglalink.toffee.R
import com.banglalink.toffee.R.*
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.util.Assertions
import java.util.*
import kotlin.math.min

/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /** A view for making track selections.  */
class TrackSelectionView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    /** Listener for changes to the selected tracks.  */
    interface TrackSelectionListener {
        /**
         * Called when the selected tracks changed.
         *
         * @param isDisabled Whether the renderer is disabled.
         * @param overrides List of selected track selection overrides for the renderer.
         */
        fun onTrackSelectionChanged(isDisabled: Boolean, overrides: List<SelectionOverride>?)
    }
    
    private var maxBitRate = -1
    private var rendererIndex = 0
    private var isDisabled = false
    private val defaultView: TextView
    private val inflater: LayoutInflater
    private var trackGroups: TrackGroupArray
    private var allowMultipleOverrides = false
    private var allowAdaptiveSelections = false
    private val componentListener: ComponentListener
    private var trackViews: MutableList<TextView?> = mutableListOf()
    private var mappedTrackInfo: MappedTrackInfo? = null
    private var listener: TrackSelectionListener? = null
    private val overrides: SparseArray<SelectionOverride>
    
    init {
        orientation = VERTICAL
        overrides = SparseArray()
        
        // Don't save view hierarchy as it needs to be reinitialized with a call to init.
        isSaveFromParentEnabled = false
        inflater = LayoutInflater.from(context)
        componentListener = ComponentListener()
        trackGroups = TrackGroupArray.EMPTY
        
        // View for clearing the override to allow the selector to use its default selection logic.
        defaultView = inflater.inflate(layout.list_item_quality, this, false) as TextView
        defaultView.text = "Auto"
        defaultView.isEnabled = false
        defaultView.isFocusable = true
        defaultView.setOnClickListener(componentListener)
        addView(defaultView)
    }
    
    companion object {
        private fun getTracksAdding(tracks: IntArray, addedTrack: Int): IntArray {
            var newTracks = tracks
            newTracks = newTracks.copyOf(newTracks.size + 1)
            newTracks[newTracks.size - 1] = addedTrack
            return newTracks
        }
        
        private fun getTracksRemoving(tracks: IntArray, removedTrack: Int): IntArray {
            val newTracks = IntArray(tracks.size - 1)
            var trackCount = 0
            for (track in tracks) {
                if (track != removedTrack) {
                    newTracks[trackCount++] = track
                }
            }
            return newTracks
        }
    }
    /**
     * Sets whether adaptive selections (consisting of more than one track) can be made using this
     * selection view.
     *
     *
     * For the view to enable adaptive selection it is necessary both for this feature to be
     * enabled, and for the target renderer to support adaptation between the available tracks.
     *
     * @param allowAdaptiveSelections Whether adaptive selection is enabled.
     */
    fun setAllowAdaptiveSelections(allowAdaptiveSelections: Boolean) {
        if (this.allowAdaptiveSelections != allowAdaptiveSelections) {
            this.allowAdaptiveSelections = allowAdaptiveSelections
            updateViews()
        }
    }
    
    /**
     * Sets whether tracks from multiple track groups can be selected. This results in multiple [ ] to be returned by [.getOverrides].
     *
     * @param allowMultipleOverrides Whether multiple track selection overrides can be selected.
     */
    fun setAllowMultipleOverrides(allowMultipleOverrides: Boolean) {
        if (this.allowMultipleOverrides != allowMultipleOverrides) {
            this.allowMultipleOverrides = allowMultipleOverrides
            if (!allowMultipleOverrides && overrides.size() > 1) {
                for (i in overrides.size() - 1 downTo 1) {
                    overrides.remove(i)
                }
            }
            updateViews()
        }
    }
    
    /**
     * Initialize the view to select tracks for a specified renderer using [MappedTrackInfo] and
     * a set of [DefaultTrackSelector.Parameters].
     *
     * @param mappedTrackInfo The [MappedTrackInfo].
     * @param rendererIndex The index of the renderer.
     * @param isDisabled Whether the renderer should be initially shown as disabled.
     * @param overrides List of initial overrides to be shown for this renderer. There must be at most
     * one override for each track group. If [.setAllowMultipleOverrides] hasn't
     * been set to `true`, only the first override is used.
     * @param listener An optional listener for track selection updates.
     */
    fun init(mappedTrackInfo: MappedTrackInfo?, rendererIndex: Int, isDisabled: Boolean, overrides: List<SelectionOverride>, maxBitRate: Int, listener: TrackSelectionListener?) {
        this.mappedTrackInfo = mappedTrackInfo
        this.rendererIndex = rendererIndex
        this.isDisabled = isDisabled
        this.maxBitRate = maxBitRate
        this.listener = listener
        val maxOverrides = if (allowMultipleOverrides) overrides.size else min(overrides.size, 1)
        for (i in 0 until maxOverrides) {
            val override = overrides[i]
            this.overrides.put(override.groupIndex, override)
        }
        updateViews()
    }
    
    /**
     * Returns the list of selected track selection overrides. There will be at most one override for
     * each track group.
     */
    private fun getOverrides(): List<SelectionOverride> {
        val overrideList: MutableList<SelectionOverride> = ArrayList(overrides.size())
        for (i in 0 until overrides.size()) {
            overrideList.add(overrides.valueAt(i))
        }
        return overrideList
    }
    
    private fun updateViews() {
        // Remove previous per-track views.
        for (i in childCount - 1 downTo 3) {
            removeViewAt(i)
        }
        if (mappedTrackInfo == null) {
            // The view is not initialized.
            defaultView.isEnabled = false
            return
        }
        defaultView.isEnabled = true
        trackGroups = mappedTrackInfo!!.getTrackGroups(rendererIndex)
        var invisibleProfileCount = 0
        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups[groupIndex]
            for (trackIndex in 0 until group.length) {
                val trackView = inflater.inflate(layout.list_item_quality, this, false) as TextView
                val format = group.getFormat(trackIndex)
                if (maxBitRate > 0 && format.bitrate > maxBitRate) {
                    trackView.isVisible = false
                    invisibleProfileCount++
                }
                val profile = "${format.height}p"
                trackView.text = profile
                if (mappedTrackInfo!!.getTrackSupport(rendererIndex, groupIndex, trackIndex) == C.FORMAT_HANDLED) {
                    trackView.isFocusable = true
                    trackView.tag = Triple(groupIndex, trackIndex, format.bitrate)
                    trackView.setOnClickListener(componentListener)
                } else {
                    trackView.isFocusable = false
                    trackView.isEnabled = false
                }
                trackViews.add(trackView)
            }
            if (invisibleProfileCount == group.length) {
                trackViews.last()?.isVisible = true
            }
        }
        val sortedProfileList = trackViews.sortedByDescending {
            it?.let {
                if(it.tag is Triple<*, *, *>) (it.tag as Triple<Int, Int, Int>).third else null
            }
        }
        sortedProfileList.forEachIndexed { index, it ->
            if (it != null && it.tag is Triple<*, *, *> && index > 0) {
                // get the current profile of the list and the immediate top and bottom profile and compare
                val previousProfile: String = sortedProfileList[index-1]!!.text.toString()
                val previousProfileBitRate: Int = (sortedProfileList[index-1]?.tag as Triple<Int, Int, Int>).third
                val currentBitRate: Int = (it.tag as Triple<Int, Int, Int>).third
                val nextProfile = sortedProfileList.getOrNull(index + 1)?.text?.toString()
                val nextProfileBitRate = sortedProfileList.getOrNull(index + 1)?.tag?.run { (this as Triple<Int, Int, Int>).third } ?: 0
                val hasDuplicateNext = nextProfile != null && nextProfileBitRate < currentBitRate
                
                if (it.text == previousProfile && currentBitRate < previousProfileBitRate && !hasDuplicateNext) {
                    it.text = it.text.toString().plus(" (Data Saver)")
                }
            }
            addView(it)
        }
        updateViewStates()
    }
    
    private fun updateViewStates() {
        var flag = !isDisabled && overrides.size() == 0
        val defaultTextView = defaultView.findViewById<TextView>(R.id.quality_text)
        if (flag) {
            val image = ContextCompat.getDrawable(context, drawable.ic_check_video_quality)
            val h = image?.intrinsicHeight ?: 0
            val w = image?.intrinsicWidth ?: 0
            image?.setBounds(0, 0, w, h)
            defaultTextView.setCompoundDrawables(image, null, null, null)
            defaultTextView.setTextColor(ContextCompat.getColor(context, color.colorAccent2))
        } else {
            defaultTextView.setCompoundDrawables(null, null, null, null)
        }
        val override = overrides[rendererIndex]
        for (i in trackViews.indices) {
            flag = override != null && override.containsTrack(i)
            if (flag) {
                val image = ContextCompat.getDrawable(context, drawable.ic_check_video_quality)
                val h = image?.intrinsicHeight ?: 0
                val w = image?.intrinsicWidth ?: 0
                image?.setBounds(0, 0, w, h)
                trackViews[i]?.setCompoundDrawables(image, null, null, null)
                trackViews[i]?.setTextColor(ContextCompat.getColor(context, color.colorAccent2))
            } else {
                trackViews[i]?.setCompoundDrawables(null, null, null, null)
            }
        }
    }
    
    private fun onClick(view: View) {
        if (view === defaultView) {
            onDefaultViewClicked()
        } else {
            onTrackViewClicked(view)
        }
        updateViewStates()
        if (listener != null) {
            listener!!.onTrackSelectionChanged(isDisabled, getOverrides())
        }
    }
    
    private fun onDefaultViewClicked() {
        isDisabled = false
        overrides.clear()
    }
    
    private fun onTrackViewClicked(view: View) {
        isDisabled = false
        val tag = view.tag as Triple<Int, Int, Int>
        val groupIndex = tag.first
        val trackIndex = tag.second
        val override = overrides[groupIndex]
        Assertions.checkNotNull(mappedTrackInfo)
        if (override == null) {
            // Start new override.
            if (!allowMultipleOverrides && overrides.size() > 0) {
                // Removed other overrides if we don't allow multiple overrides.
                overrides.clear()
            }
            overrides.put(groupIndex, SelectionOverride(groupIndex, trackIndex))
        } else {
            // An existing override is being modified.
            val overrideLength = override.length
            val overrideTracks = override.tracks
            val isCurrentlySelected = (view as TextView).compoundDrawables[0] != null
            val isAdaptiveAllowed = shouldEnableAdaptiveSelection(groupIndex)
            val isUsingCheckBox = isAdaptiveAllowed || shouldEnableMultiGroupSelection()
            if (isCurrentlySelected && isUsingCheckBox) {
                // Remove the track from the override.
                if (overrideLength == 1) {
                    // The last track is being removed, so the override becomes empty.
                    overrides.remove(groupIndex)
                } else {
                    val tracks = getTracksRemoving(overrideTracks, trackIndex)
                    overrides.put(groupIndex, SelectionOverride(groupIndex, *tracks))
                }
            } else if (!isCurrentlySelected) {
                overrides.put(groupIndex, SelectionOverride(groupIndex, trackIndex))
            }
        }
    }
    
    private fun shouldEnableAdaptiveSelection(groupIndex: Int): Boolean {
        return (allowAdaptiveSelections && trackGroups[groupIndex].length > 1 && (mappedTrackInfo!!.getAdaptiveSupport(
            rendererIndex, groupIndex,  /* includeCapabilitiesExceededTracks= */false
        ) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED))
    }
    
    private fun shouldEnableMultiGroupSelection(): Boolean {
        return allowMultipleOverrides && trackGroups.length > 1
    }
    
    // Internal classes.
    private inner class ComponentListener : OnClickListener {
        override fun onClick(view: View) {
            this@TrackSelectionView.onClick(view)
        }
    }
}