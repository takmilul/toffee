package com.banglalink.toffee.ui.player

import android.content.Context
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

internal class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(appContext: Context?): CastOptions {
        val supportedNamespaces = mutableListOf<String>()
        supportedNamespaces.add(CUSTOM_NAMESPACE)
        return CastOptions.Builder().apply {
                SessionPreference.getInstance().castReceiverId.let {
//                    if (it.isNotBlank()) {
//                        setReceiverApplicationId(SessionPreference.getInstance().castReceiverId)
//                    } else {
                        setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
//                    }
                }
//            .setSupportedNamespaces(supportedNamespaces)
            }.build()
    }

    override fun getAdditionalSessionProviders(context: Context?): List<SessionProvider>? {
        return null
    }

    companion object {
        const val CUSTOM_NAMESPACE = "urn:x-cast:toffee"
    }
}