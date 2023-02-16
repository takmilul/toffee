package com.banglalink.toffee.ui.player

import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.DialogInterface.OnDismissListener
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.ui.player.TrackSelectionView2.Companion.filterOverrides
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.Tracks.Group
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.collect.ImmutableList

/** Dialog to select tracks.  */
class TrackSelectionDialog2(context: Context) : BottomSheetDialog(context), DefaultLifecycleObserver {
    /** Called when tracks are selected.  */
    interface TrackSelectionListener {
        /**
         * Called when tracks are selected.
         *
         * @param trackSelectionParameters A [TrackSelectionParameters] representing the selected
         * tracks. Any manual selections are defined by [     ][TrackSelectionParameters.disabledTrackTypes] and [     ][TrackSelectionParameters.overrides].
         */
        fun onTracksSelected(trackSelectionParameters: TrackSelectionParameters?)
    }
    
    private val tabFragments: SparseArray<TrackSelectionViewFragment>
    private val tabTrackTypes: ArrayList<Int>
    private var onClickListener: OnClickListener? = null
    private var onDismissListener: OnDismissListener? = null
    
    init {
        tabFragments = SparseArray()
        tabTrackTypes = ArrayList()
    }
    
    private fun init(
        tracks: Tracks,
        trackSelectionParameters: TrackSelectionParameters,
        allowAdaptiveSelections: Boolean,
        allowMultipleOverrides: Boolean,
        onClickListener: OnClickListener,
        onDismissListener: OnDismissListener?
    ) {
        this.onClickListener = onClickListener
        this.onDismissListener = onDismissListener
        val trackType = SUPPORTED_TRACK_TYPES[0]
        val trackGroups = ArrayList<Group>()
        for (trackGroup in tracks.groups) {
            if (trackGroup.type == trackType) {
                trackGroups.add(trackGroup)
            }
        }
        if (trackGroups.isNotEmpty()) {
            val tabFragment = TrackSelectionViewFragment()
            tabFragment.init(
                trackGroups,
                trackSelectionParameters.disabledTrackTypes.contains(trackType),
                trackSelectionParameters.overrides,
                allowAdaptiveSelections,
                allowMultipleOverrides
            )
            tabFragments.put(trackType, tabFragment)
            tabTrackTypes.add(trackType)
        }
    }
    
    /**
     * Returns whether the disabled option is selected for the specified track type.
     *
     * @param trackType The track type.
     * @return Whether the disabled option is selected for the track type.
     */
    fun getIsDisabled(trackType: Int): Boolean {
        val trackView = tabFragments[trackType]
        return trackView != null && trackView.isDisabled
    }
    
    /**
     * Returns the selected track overrides for the specified track type.
     *
     * @param trackType The track type.
     * @return The track overrides for the track type.
     */
    fun getOverrides(trackType: Int): Map<TrackGroup, TrackSelectionOverride> {
        val trackView = tabFragments[trackType]
        return if (trackView == null) emptyMap() else trackView.overrides!!
    }
    
    inner class TrackSelectionViewFragment : Fragment(), TrackSelectionView2.TrackSelectionListener {
        private lateinit var trackGroups: List<Group>
        private var allowAdaptiveSelections = false
        private var allowMultipleOverrides = false
        
        var isDisabled = false
        var overrides: Map<TrackGroup, TrackSelectionOverride>? = null
        lateinit var rootView: View
        
        fun init(
            trackGroups: List<Group>,
            isDisabled: Boolean,
            overrides: Map<TrackGroup, TrackSelectionOverride>,
            allowAdaptiveSelections: Boolean,
            allowMultipleOverrides: Boolean
        ) {
            this.trackGroups = trackGroups
            this.isDisabled = isDisabled
            this.allowAdaptiveSelections = allowAdaptiveSelections
            this.allowMultipleOverrides = allowMultipleOverrides
            // TrackSelectionView2 does this filtering internally, but we need to do it here as well to
            // handle the case where the TrackSelectionView2 is never created.
            this.overrides = HashMap(
                filterOverrides(overrides, trackGroups, allowMultipleOverrides)
            )
        }
        
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            rootView = inflater.inflate(
                layout.exo_track_selection_dialog, container,  /* attachToRoot= */false
            )
            val trackSelectionView = rootView.findViewById<TrackSelectionView2>(R.id.exo_track_selection_view)
            trackSelectionView.setShowDisableOption(true)
            trackSelectionView.setAllowMultipleOverrides(allowMultipleOverrides)
            trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
            trackSelectionView.init(
                trackGroups, isDisabled, overrides!!,  /* trackFormatComparator= */
                null,  /* listener= */
                this
            )
            return rootView
        }
    
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        }
        
        override fun onTrackSelectionChanged(isDisabled: Boolean, overrides: Map<TrackGroup, TrackSelectionOverride>?) {
            this.isDisabled = isDisabled
            this.overrides = overrides
        }
    }
    
    fun initialize(player: Player, onDismissListener: OnDismissListener?) {
        val trackSelectionParameters = player.trackSelectionParameters
        val bottomView = layoutInflater.inflate(R.layout.exo_track_selection_dialog, null) as TrackSelectionView2
        val trackType = SUPPORTED_TRACK_TYPES[0]
        val trackGroups = ArrayList<Group>()
        for (trackGroup in player.currentTracks.groups) {
            if (trackGroup.type == trackType) {
                trackGroups.add(trackGroup)
            }
        }
        bottomView.init(trackGroups, player.trackSelectionParameters.disabledTrackTypes.contains(trackType),
            player.trackSelectionParameters.overrides,
            null,
            object : TrackSelectionView2.TrackSelectionListener{
                override fun onTrackSelectionChanged(isDisabled: Boolean, overrides: Map<TrackGroup, TrackSelectionOverride>?) {
                    val builder = trackSelectionParameters.buildUpon()
                    for (i in SUPPORTED_TRACK_TYPES.indices) {
                        val trackType = SUPPORTED_TRACK_TYPES[i]
                        builder.setTrackTypeDisabled(trackType, getIsDisabled(trackType))
                        builder.clearOverridesOfType(trackType)
                        val overrides = getOverrides(trackType)
                        for (override in overrides.values) {
                            builder.addOverride(override)
                        }
                    }
                    builder.build().let {
                        player.trackSelectionParameters = it
                    }
                    dismiss()
                }
            })
        
        setContentView(bottomView)
    }
    
    override fun onPause(owner: LifecycleOwner) {
        dismiss()
        super.onPause(owner)
    }
    
    companion object {
        val SUPPORTED_TRACK_TYPES = ImmutableList.of(C.TRACK_TYPE_VIDEO/*, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT*/)
        
    }
}