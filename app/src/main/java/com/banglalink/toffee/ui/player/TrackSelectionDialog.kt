package com.banglalink.toffee.ui.player

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.banglalink.toffee.R
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.material.bottomsheet.BottomSheetDialog

class TrackSelectionDialog(context: Context) : BottomSheetDialog(context), LifecycleObserver {
    fun init(defaultTrackSelector: DefaultTrackSelector?) {
        val trackGroupArray = defaultTrackSelector?.currentMappedTrackInfo?.getTrackGroups(0)
        val initialOverride = trackGroupArray?.let { defaultTrackSelector.parameters.getSelectionOverride(0, it) }
        val bottomView = layoutInflater.inflate(R.layout.track_selection_dialog, null) as TrackSelectionView
        bottomView.init(defaultTrackSelector?.currentMappedTrackInfo,
            0,
            false,
            initialOverride?.let { listOf(it) } ?: emptyList()) { isDisabled: Boolean, overrides: List<SelectionOverride?> ->
            defaultTrackSelector?.currentMappedTrackInfo?.let {
                val builder = defaultTrackSelector.parameters.buildUpon()
                builder.clearSelectionOverrides(0)
                builder.setRendererDisabled(0, isDisabled)
                if (overrides.isNotEmpty()) {
                    builder.setSelectionOverride(0,
                        defaultTrackSelector.currentMappedTrackInfo!!.getTrackGroups(0),
                        overrides[0])
                }
                defaultTrackSelector.setParameters(builder)
            }
            dismiss()
        }
        setContentView(bottomView)
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun dismissDialog() {
        dismiss()
    }
}