package com.banglalink.toffee.ui.player

import android.content.Context
import com.banglalink.toffee.R
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

internal class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(appContext: Context?): CastOptions {
//        val supportedNamespaces = mutableListOf<String>()
//        supportedNamespaces.add(CUSTOM_NAMESPACE)
        return CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
//            .setReceiverApplicationId(appContext?.getString(R.string.app_id))
//            .setSupportedNamespaces(supportedNamespaces)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context?): List<SessionProvider>? {
        return null
    }

    companion object {
//        const val CUSTOM_NAMESPACE = "urn:x-cast:custom_namespace"
    }
}