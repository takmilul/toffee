package com.banglalink.toffee.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/**
 * Provides a set of utility methods for working with incoming Intents that may contain App Link
 * data.
 */
object AppLinks {
    const val KEY_NAME_APPLINK_DATA = "al_applink_data"
    const val KEY_NAME_EXTRAS = "extras"
    const val KEY_NAME_TARGET = "target_url"

    /**
     * Gets the App Link data for an intent, if there is any.
     * This is the authorized function to check if an intent is AppLink. If null is returned it is not.
     *
     * @param intent the incoming intent.
     * @return a bundle containing the App Link data for the intent, or `null` if none
     * is specified.
     */
    private fun getAppLinkData(intent: Intent): Bundle? {
        return intent.getBundleExtra(KEY_NAME_APPLINK_DATA)
    }

    /**
     * Gets the App Link extras for an intent, if there is any.
     *
     * @param intent the incoming intent.
     * @return a bundle containing the App Link extras for the intent, or `null` if none is
     * specified.
     */
    fun getAppLinkExtras(intent: Intent): Bundle? {
        val appLinkData = getAppLinkData(intent)
            ?: return null
        return appLinkData.getBundle(KEY_NAME_EXTRAS)
    }

    /**
     * Gets the target URL for an intent, regardless of whether the intent is from an App Link. If the
     * intent is from an App Link, this will be the App Link target. Otherwise, it will be the data
     * Uri from the intent itself.
     *
     * @param intent the incoming intent.
     * @return the target URL for the intent.
     */
    fun getTargetUrl(intent: Intent): Uri? {
        val appLinkData = getAppLinkData(intent)
        if (appLinkData != null) {
            val targetString = appLinkData.getString(KEY_NAME_TARGET)
            if (targetString != null) {
                return Uri.parse(targetString)
            }
        }
        return intent.data
    }

    /**
     * Gets the target URL for an intent. If the intent is from an App Link, this will be the App Link target.
     * Otherwise, it return null; For app link intent, this function will broadcast APP_LINK_NAVIGATE_IN_EVENT_NAME event.
     *
     * @param context the context this function is called within.
     * @param intent the incoming intent.
     * @return the target URL for the intent if applink intent; null otherwise.
     */
    fun getTargetUrlFromInboundIntent(context: Context?, intent: Intent): Uri? {
        val appLinkData = getAppLinkData(intent)
        if (appLinkData != null) {
            val targetString = appLinkData.getString(KEY_NAME_TARGET)
            if (targetString != null) {
                return Uri.parse(targetString)
            }
        }
        return null
    }
}