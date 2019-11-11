package com.daimajia.slider.library;


import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;

/**
 * Created by tushar on 9/26/18.
 */

@GlideModule
public final class AppGlideModule extends com.bumptech.glide.module.AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        int bitmapPoolSizeBytes = 1024 * 1024 * 10; //10mb
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSizeBytes));
    }
}
