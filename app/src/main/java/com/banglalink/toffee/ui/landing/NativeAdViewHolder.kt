package com.banglalink.toffee.ui.landing

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.show
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class NativeAdViewHolder(itemView: View, val mPref: SessionPreference) : RecyclerView.ViewHolder(itemView){
    
    private var adsCnt = 3
    private var adView: NativeAdView
    private var placeholder: ShimmerFrameLayout
    
    init {
        placeholder = itemView.findViewById(R.id.placeholder)
        adView = itemView.findViewById<View>(R.id.nativeAdview) as NativeAdView
    }
    
    suspend fun loadAds(adUnitId: String) {
        val adLoader = AdLoader.Builder(itemView.context, adUnitId)
            .forNativeAd { nativeAd ->
                // val template: TemplateView  = itemView.findViewById(R.id.my_template)
                // template.setNativeAd(ad)
//                val adView = itemView as NativeAdView
                Log.i("NAT_", "loadAds: $nativeAd")
                CoroutineScope(Main).launch {
                    populateUnifiedNativeAdView(nativeAd)
                }
//                val typedValue = TypedValue()
//                val theme: Resources.Theme = itemView.context.getTheme()
//                theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
//                @ColorInt val color: Int = typedValue.data
//                val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(color)).build()
//                val styles = NativeTemplateStyle.Builder().build()
                
//                val template: TemplateView = itemView.findViewById(R.id.my_template)
//                template.setStyles(styles)
//                template.setNativeAd(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    Log.i("NAT_", "onAdFailedToLoad: $loadAdError")
                    if (adsCnt > 0) {
                        adsCnt -= 1
                        CoroutineScope(IO).launch {
                            loadAds(adUnitId)
                        }
                    } else {
                        CoroutineScope(Main).launch {
                            adView.hide()
                            placeholder.hide()
                            placeholder.stopShimmer()
                        }
                    }
                }
                
                override fun onAdClicked() {
                    super.onAdClicked()
                    mPref.isDisablePip.postValue(true)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder().setVideoOptions(
                    VideoOptions.Builder().setStartMuted(true).setClickToExpandRequested(true).build()
                ).build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
    
    /**
     * Populates a [UnifiedNativeAdView] object with data from a given
     * [UnifiedNativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
    private fun populateUnifiedNativeAdView(nativeAd: NativeAd) {
        // Set the media view.
        
        adView.mediaView = adView.findViewById(R.id.ad_media)
        
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
//        adView.priceView = adView.findViewById(R.id.ad_price)
//        adView.starRatingView = adView.findViewById(R.id.ad_stars)
//        adView.storeView = adView.findViewById(R.id.ad_store)
//        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView?.mediaContent = nativeAd.mediaContent
        
        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            (adView.bodyView as TextView).visibility = View.INVISIBLE
        } else {
            (adView.bodyView as TextView).visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }
        
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }
        
//        if (nativeAd.price == null) {
//            adView.priceView.visibility = View.INVISIBLE
//        } else {
//            adView.priceView.visibility = View.VISIBLE
//            (adView.priceView as TextView).text = nativeAd.price
//        }
        
//        if (nativeAd.store == null) {
//            adView.storeView.visibility = View.INVISIBLE
//        } else {
//            adView.storeView.visibility = View.VISIBLE
//            (adView.storeView as TextView).text = nativeAd.store
//        }
        
//        if (nativeAd.starRating == null) {
//            adView.starRatingView.visibility = View.INVISIBLE
//        } else {
//            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
//            adView.starRatingView.visibility = View.VISIBLE
//        }
        
//        if (nativeAd.advertiser == null) {
//            adView.advertiserView.visibility = View.INVISIBLE
//        } else {
//            (adView.advertiserView as TextView).text = nativeAd.advertiser
//            adView.advertiserView.visibility = View.VISIBLE
//        }
        
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
        placeholder.hide()
        placeholder.stopShimmer()
        adView.show()
        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
//        val vc = nativeAd.videoController
//        
//        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc.hasVideoContent()) {
////            videostatus_text.text = String.format(
////                Locale.getDefault(),
////                "Video status: Ad contains a %.2f:1 video asset.",
////                vc.aspectRatio)
////
////            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
////            // VideoController will call methods on this object when events occur in the video
////            // lifecycle.
//            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
//                override fun onVideoEnd() {
//                    // Publishers should allow native ads to complete video playback before
//                    // refreshing or replacing them with another ad in the same UI location.
//                    //refresh_button.isEnabled = true
//                    // videostatus_text.text = "Video status: Video playback has ended."
//                    super.onVideoEnd()
//                }
//            }
//        }
//       else {
//            videostatus_text.text = "Video status: Ad does not contain a video asset."
//            refresh_button.isEnabled = true
//        }
    }
    
}