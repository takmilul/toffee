package com.banglalink.toffee.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.FilterQuality
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.banglalink.toffee.R
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
    
    @Composable
    fun getAsyncImagePainter(
        model: Any,
        @DrawableRes placeholder: Int = R.drawable.placeholder,
        filterQuality: FilterQuality = FilterQuality.Low,
    ): AsyncImagePainter {
        return rememberAsyncImagePainter(
            model = model,
            error = rememberAsyncImagePainter(placeholder),
            fallback = rememberAsyncImagePainter(placeholder),
            placeholder = rememberAsyncImagePainter(placeholder),
            filterQuality = filterQuality
        )
    }
}