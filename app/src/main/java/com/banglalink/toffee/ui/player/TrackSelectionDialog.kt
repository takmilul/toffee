package com.banglalink.toffee.ui.player;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.banglalink.toffee.R;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Collections;

/** Dialog to select tracks. */
public final class TrackSelectionDialog extends BottomSheetDialog implements LifecycleObserver {

    public TrackSelectionDialog(@NonNull Context context) {
        super(context);
    }

    public void init(DefaultTrackSelector defaultTrackSelector){
        TrackGroupArray trackGroupArray = defaultTrackSelector.getCurrentMappedTrackInfo().getTrackGroups(0);
        DefaultTrackSelector.SelectionOverride initialOverride  =  defaultTrackSelector.getParameters().getSelectionOverride(/* rendererIndex= */ 0, trackGroupArray);
        TrackSelectionView bottomView = (TrackSelectionView) getLayoutInflater().inflate(R.layout.track_selection_dialog,null);
        bottomView.init(defaultTrackSelector.getCurrentMappedTrackInfo(), 0, false, initialOverride == null
                ? Collections.emptyList()
                : Collections.singletonList(initialOverride), (isDisabled, overrides) -> {
            if(defaultTrackSelector.getCurrentMappedTrackInfo() != null){
                DefaultTrackSelector.ParametersBuilder builder = defaultTrackSelector.getParameters().buildUpon();

                builder.clearSelectionOverrides(0);
                builder.setRendererDisabled(0, isDisabled);
                if (!overrides.isEmpty()) {
                    builder.setSelectionOverride(0,
                            defaultTrackSelector.getCurrentMappedTrackInfo().getTrackGroups(0),
                            overrides.get(0));
                }
                defaultTrackSelector.setParameters(builder);
            }
            dismiss();

        });
        setContentView(bottomView);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void dismissDialog(){
        dismiss();
    }
}