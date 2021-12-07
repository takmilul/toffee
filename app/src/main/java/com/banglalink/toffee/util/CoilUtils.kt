package com.banglalink.toffee.util

import android.content.Context
import android.graphics.drawable.Drawable
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.banglalink.toffee.analytics.ToffeeAnalytics

object CoilUtils {
    suspend fun coilExecuteGet(ctx: Context, url: Any?): Drawable? = try{
        val request = ImageRequest.Builder(ctx)
            .data(url)
            .allowHardware(false) // Disable hardware bitmaps.
            .build()
        ctx.imageLoader.execute(request).let {
            if(it is SuccessResult) it.drawable
            else null
        }
    } catch (ex: Exception) {
        ToffeeAnalytics.logException(ex)
        null
    }
}