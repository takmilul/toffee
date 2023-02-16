package com.banglalink.toffee.ui.player

import android.R.attr
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.banglalink.toffee.ui.player.TrackSelectionView.Companion
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Tracks.Group
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.ui.R.string
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.Assertions
import com.google.common.collect.ImmutableList

/** A view for making track selections.  */
class TrackSelectionView2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    /** Listener for changes to the selected tracks.  */
    interface TrackSelectionListener {
        /**
         * Called when the selected tracks changed.
         *
         * @param isDisabled Whether the disabled option is selected.
         * @param overrides The selected track overrides.
         */
        fun onTrackSelectionChanged(
            isDisabled: Boolean, overrides: Map<TrackGroup, TrackSelectionOverride>?
        )
    }
    
    private val selectableItemBackgroundResourceId: Int
    private val inflater: LayoutInflater
    private val disableView: TextView
    private val defaultView: TextView
    private val componentListener: ComponentListener
    private val trackGroups: MutableList<Group>
    private val overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    private var allowAdaptiveSelections = false
    private var allowMultipleOverrides = false
    private var trackNameProvider: TrackNameProvider
    private var trackViews: MutableList<TextView?> = mutableListOf()
    
    /** Returns whether the disabled option is selected.  */
    var isDisabled = false
        private set
    private var trackInfoComparator: Comparator<TrackInfo>? = null
    private var listener: TrackSelectionListener? = null
    private var maxBitRate = 66666660
    /** Creates a track selection view.  */
    /** Creates a track selection view.  */
    /** Creates a track selection view.  */
    init {
        orientation = VERTICAL
        // Don't save view hierarchy as it needs to be reinitialized with a call to init.
        isSaveFromParentEnabled = false
        val attributeArray = context.theme.obtainStyledAttributes(intArrayOf(attr.selectableItemBackground))
        selectableItemBackgroundResourceId = attributeArray.getResourceId(0, 0)
        attributeArray.recycle()
        inflater = LayoutInflater.from(context)
        componentListener = ComponentListener()
        trackNameProvider = DefaultTrackNameProvider(resources)
        trackGroups = ArrayList()
        overrides = HashMap()
        
        // View for disabling the renderer.
        disableView = inflater.inflate(android.R.layout.simple_list_item_single_choice, this, false) as CheckedTextView
        disableView.setBackgroundResource(selectableItemBackgroundResourceId)
        disableView.setText(string.exo_track_selection_none)
        disableView.isEnabled = false
        disableView.isFocusable = true
        disableView.setOnClickListener(componentListener)
        disableView.visibility = GONE
        addView(disableView)
        // Divider view.
        addView(inflater.inflate(R.layout.exo_list_divider, this, false))
        // View for clearing the override to allow the selector to use its default selection logic.
        defaultView = inflater.inflate(com.banglalink.toffee.R.layout.list_item_quality, this, false) as TextView
        defaultView.setBackgroundResource(selectableItemBackgroundResourceId)
        defaultView.setText(string.exo_track_selection_auto)
        defaultView.isEnabled = false
        defaultView.isFocusable = true
        defaultView.setOnClickListener(componentListener)
        addView(defaultView)
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
     * Sets whether tracks from multiple track groups can be selected. This results in multiple [ ] being returned by [.getOverrides].
     *
     * @param allowMultipleOverrides Whether tracks from multiple track groups can be selected.
     */
    fun setAllowMultipleOverrides(allowMultipleOverrides: Boolean) {
        if (this.allowMultipleOverrides != allowMultipleOverrides) {
            this.allowMultipleOverrides = allowMultipleOverrides
            if (!allowMultipleOverrides && overrides.size > 1) {
                // Re-filter the overrides to retain only one of them.
                val filteredOverrides = filterOverrides(overrides, trackGroups,  /* allowMultipleOverrides= */false)
                overrides.clear()
                overrides.putAll(filteredOverrides)
            }
            updateViews()
        }
    }
    
    /**
     * Sets whether the disabled option can be selected.
     *
     * @param showDisableOption Whether the disabled option can be selected.
     */
    fun setShowDisableOption(showDisableOption: Boolean) {
        disableView.visibility = if (showDisableOption) VISIBLE else GONE
    }
    
    /**
     * Sets the [TrackNameProvider] used to generate the user visible name of each track and
     * updates the view with track names queried from the specified provider.
     *
     * @param trackNameProvider The [TrackNameProvider] to use.
     */
    fun setTrackNameProvider(trackNameProvider: TrackNameProvider?) {
        this.trackNameProvider = Assertions.checkNotNull(trackNameProvider)
        updateViews()
    }
    
    fun init(
        trackGroups: List<Group>,
        isDisabled: Boolean,
        overrides: Map<TrackGroup, TrackSelectionOverride>,
        trackFormatComparator: Comparator<Format?>?,
        listener: TrackSelectionListener?
    ) {
        this.isDisabled = isDisabled
        trackInfoComparator = if (trackFormatComparator == null) null else Comparator<TrackInfo> { o1: TrackInfo, o2: TrackInfo ->
            trackFormatComparator.compare(
                o1.format, o2.format
            )
        }
        this.listener = listener
        this.trackGroups.clear()
        this.trackGroups.addAll(trackGroups)
        this.overrides.clear()
        this.overrides.putAll(filterOverrides(overrides, trackGroups, allowMultipleOverrides))
        updateViews()
    }
    
    /** Returns the selected track overrides.  */
    fun getOverrides(): Map<TrackGroup, TrackSelectionOverride> {
        return overrides
    }
    
    // Private methods.
//    private fun updateViews() {
//        // Remove previous per-track views.
//        for (i in childCount - 1 downTo 3) {
//            removeViewAt(i)
//        }
//        if (trackGroups.isEmpty()) {
//            // The view is not initialized.
//            disableView.isEnabled = false
//            defaultView.isEnabled = false
//            return
//        }
//        disableView.isEnabled = true
//        defaultView.isEnabled = true
//
//        // Add per-track views.
//        trackViews = arrayOfNulls(trackGroups.size)
//        val enableMultipleChoiceForMultipleOverrides = shouldEnableMultiGroupSelection()
//        for (trackGroupIndex in trackGroups.indices) {
//            val trackGroup = trackGroups[trackGroupIndex]
//            val enableMultipleChoiceForAdaptiveSelections = shouldEnableAdaptiveSelection(trackGroup)
//            trackViews[trackGroupIndex] = arrayOfNulls(trackGroup.length)
//            val trackInfos = arrayOfNulls<TrackInfo>(trackGroup.length)
//            for (trackIndex in 0 until trackGroup.length) {
//                trackInfos[trackIndex] = TrackInfo(trackGroup, trackIndex)
//            }
//            if (trackInfoComparator != null) {
//                Arrays.sort(trackInfos, trackInfoComparator)
//            }
//            for (trackIndex in trackInfos.indices) {
//                if (trackIndex == 0) {
//                    addView(inflater.inflate(R.layout.exo_list_divider, this, false))
//                }
//                val trackViewLayoutId =
//                    if (enableMultipleChoiceForAdaptiveSelections || enableMultipleChoiceForMultipleOverrides) layout.simple_list_item_multiple_choice else layout.simple_list_item_single_choice
//                val trackView = inflater.inflate(trackViewLayoutId, this, false) as CheckedTextView
//                trackView.setBackgroundResource(selectableItemBackgroundResourceId)
//                trackView.text = trackNameProvider.getTrackName(trackInfos[trackIndex]?.format)
//                trackView.tag = trackInfos[trackIndex]
//                if (trackGroup.isTrackSupported(trackIndex)) {
//                    trackView.isFocusable = true
//                    trackView.setOnClickListener(componentListener)
//                } else {
//                    trackView.isFocusable = false
//                    trackView.isEnabled = false
//                }
//                trackViews[trackGroupIndex][trackIndex] = trackView
//                addView(trackView)
//            }
//        }
//        updateViewStates()
//    }
    
    private fun updateViews() {
        // Remove previous per-track views.
        for (i in childCount - 1 downTo 3) {
            removeViewAt(i)
        }
        if (trackGroups.isEmpty()) {
            // The view is not initialized.
            defaultView.isEnabled = false
            return
        }
        defaultView.isEnabled = true
        var invisibleProfileCount = 0
        for (groupIndex in 0 until trackGroups.size) {
            val group = trackGroups[groupIndex]
            for (trackIndex in 0 until group.length) {
                val trackView = inflater.inflate(com.banglalink.toffee.R.layout.list_item_quality, this, false) as TextView
                val format = group.getTrackFormat(trackIndex)
                if (maxBitRate > 0 && format.bitrate > maxBitRate) {
                    trackView.isVisible = false
                    invisibleProfileCount++
                }
                val profile = "${format.height}p"
                trackView.text = profile

//                if (mappedTrackInfo!!.getTrackSupport(rendererIndex, groupIndex, trackIndex) == C.FORMAT_HANDLED) {
                trackView.isFocusable = true
                trackView.tag = Triple(groupIndex, trackIndex, format.bitrate)
                trackView.setOnClickListener(componentListener)
//                } else {
//                    trackView.isFocusable = false
//                    trackView.isEnabled = false
//                }
                trackViews.add(trackView)
            }
            if (invisibleProfileCount == group.length) {
                trackViews.last()?.isVisible = true
            }
        }
        val sortedProfileList = trackViews.sortedByDescending {
            it?.let {
                if (it.tag is Triple<*, *, *>) (it.tag as Triple<Int, Int, Int>).third else null
            }
        }
        sortedProfileList.forEachIndexed { index, it ->
            if (it != null && it.tag is Triple<*, *, *> && index > 0) {
                // get the current profile of the list and the immediate top and bottom profile and compare
                val previousProfile: String = sortedProfileList[index - 1]!!.text.toString()
                val previousProfileBitRate: Int = (sortedProfileList[index - 1]?.tag as Triple<Int, Int, Int>).third
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
//        updateViewStates()
    }
    
    private fun updateViewStates() {
        for (i in trackViews.indices) {
            val override = overrides[trackGroups[i].mediaTrackGroup]
            
            val image = ContextCompat.getDrawable(context, com.banglalink.toffee.R.drawable.ic_check_video_quality)
            val h = image?.intrinsicHeight ?: 0
            val w = image?.intrinsicWidth ?: 0
            image?.setBounds(0, 0, w, h)
            
            if (override != null) {
                val trackInfo = Assertions.checkNotNull(
                    trackViews[i]!!.tag
                ) as TrackInfo
                trackViews[i]?.setCompoundDrawables(image, null, null, null)
            } else {
                trackViews[i]?.setCompoundDrawables(null, null, null, null)
            }
        }
    }

//    private fun updateViewStates() {
//        var flag = !isDisabled && overrides.isEmpty()
//        val defaultTextView = defaultView.findViewById<TextView>(com.banglalink.toffee.R.id.quality_text)
//        if (flag) {
//            val image = ContextCompat.getDrawable(context, com.banglalink.toffee.R.drawable.ic_check_video_quality)
//            val h = image?.intrinsicHeight ?: 0
//            val w = image?.intrinsicWidth ?: 0
//            image?.setBounds(0, 0, w, h)
//            defaultTextView.setCompoundDrawables(image, null, null, null)
//            defaultTextView.setTextColor(ContextCompat.getColor(context, com.banglalink.toffee.R.color.colorAccent2))
//        } else {
//            defaultTextView.setCompoundDrawables(null, null, null, null)
//        }
//        for (i in trackViews.indices) {
//            val override = overrides[trackGroups[i].mediaTrackGroup]
//            flag = override != null
//            if (flag) {
//                val image = ContextCompat.getDrawable(context, com.banglalink.toffee.R.drawable.ic_check_video_quality)
//                val h = image?.intrinsicHeight ?: 0
//                val w = image?.intrinsicWidth ?: 0
//                image?.setBounds(0, 0, w, h)
//                trackViews[i]?.setCompoundDrawables(image, null, null, null)
//                trackViews[i]?.setTextColor(ContextCompat.getColor(context, com.banglalink.toffee.R.color.colorAccent2))
//            } else {
//                trackViews[i]?.setCompoundDrawables(null, null, null, null)
//            }
//        }
//    }
    
    private fun onClick(view: View) {
        if (view === disableView) {
            onDisableViewClicked()
        } else if (view === defaultView) {
            onDefaultViewClicked()
        } else {
            onTrackViewClicked(view)
        }
        updateViewStates()
        if (listener != null) {
            listener!!.onTrackSelectionChanged(isDisabled, getOverrides())
        }
    }
    
    private fun onDisableViewClicked() {
        isDisabled = true
        overrides.clear()
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
                    val tracks = TrackSelectionView.getTracksRemoving(overrideTracks, trackIndex)
                    overrides.put(groupIndex, SelectionOverride(groupIndex, *tracks))
                }
            } else if (!isCurrentlySelected) {
                overrides.put(groupIndex, SelectionOverride(groupIndex, trackIndex))
            }
        }
    }

//    private fun onTrackViewClicked(view: View) {
//        isDisabled = false
//        val trackInfo = Assertions.checkNotNull(view.tag) as TrackInfo
//        val mediaTrackGroup = trackInfo.trackGroup.mediaTrackGroup
//        val trackIndex = trackInfo.trackIndex
//        val override = overrides[mediaTrackGroup]
//        if (override == null) {
//            // Start new override.
//            if (!allowMultipleOverrides && overrides.size > 0) {
//                // Removed other overrides if we don't allow multiple overrides.
//                overrides.clear()
//            }
//            overrides[mediaTrackGroup] = TrackSelectionOverride(
//                mediaTrackGroup, ImmutableList.of(trackIndex)
//            )
//        } else {
//            // An existing override is being modified.
//            val trackIndices = ArrayList(override.trackIndices)
//            val isCurrentlySelected = (view as CheckedTextView).isChecked
//            val isAdaptiveAllowed = shouldEnableAdaptiveSelection(trackInfo.trackGroup)
//            val isUsingCheckBox = isAdaptiveAllowed || shouldEnableMultiGroupSelection()
//            if (isCurrentlySelected && isUsingCheckBox) {
//                // Remove the track from the override.
//                trackIndices.remove(trackIndex)
//                if (trackIndices.isEmpty()) {
//                    // The last track has been removed, so remove the whole override.
//                    overrides.remove(mediaTrackGroup)
//                } else {
//                    overrides[mediaTrackGroup] = TrackSelectionOverride(mediaTrackGroup, trackIndices)
//                }
//            } else if (!isCurrentlySelected) {
//                if (isAdaptiveAllowed) {
//                    // Add new track to adaptive override.
//                    trackIndices.add(trackIndex)
//                    overrides[mediaTrackGroup] = TrackSelectionOverride(mediaTrackGroup, trackIndices)
//                } else {
//                    // Replace existing track in override.
//                    overrides[mediaTrackGroup] = TrackSelectionOverride(
//                        mediaTrackGroup, ImmutableList.of(trackIndex)
//                    )
//                }
//            }
//        }
//    }
    
    private fun shouldEnableAdaptiveSelection(trackGroup: Group): Boolean {
        return allowAdaptiveSelections && trackGroup.isAdaptiveSupported
    }
    
    private fun shouldEnableMultiGroupSelection(): Boolean {
        return allowMultipleOverrides && trackGroups.size > 1
    }
    
    // Internal classes.
    private inner class ComponentListener : OnClickListener {
        override fun onClick(view: View) {
            this@TrackSelectionView2.onClick(view)
        }
    }
    
    private class TrackInfo(val trackGroup: Group, val trackIndex: Int) {
        val format: Format
            get() = trackGroup.getTrackFormat(trackIndex)
    }
    
    companion object {
        /**
         * Returns the subset of `overrides` that apply to the specified `trackGroups`. If
         * `allowMultipleOverrides` is `` then at most one override is retained, which will be
         * the one whose track group is first in `trackGroups`.
         *
         * @param overrides The overrides to filter.
         * @param trackGroups The track groups whose overrides should be retained.
         * @param allowMultipleOverrides Whether more than one override can be retained.
         * @return The filtered overrides.
         */
        @JvmStatic
        fun filterOverrides(
            overrides: Map<TrackGroup, TrackSelectionOverride>, trackGroups: List<Group>, allowMultipleOverrides: Boolean
        ): Map<TrackGroup, TrackSelectionOverride> {
            val filteredOverrides = HashMap<TrackGroup, TrackSelectionOverride>()
            for (i in trackGroups.indices) {
                val trackGroup = trackGroups[i]
                val override = overrides[trackGroup.mediaTrackGroup]
                if (override != null && (allowMultipleOverrides || filteredOverrides.isEmpty())) {
                    filteredOverrides[override.mediaTrackGroup] = override
                }
            }
            return filteredOverrides
        }
    }
}