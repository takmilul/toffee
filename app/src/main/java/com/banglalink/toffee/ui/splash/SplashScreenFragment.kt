package com.banglalink.toffee.ui.splash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.ads.identifier.AdvertisingIdClient
import androidx.ads.identifier.AdvertisingIdInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.data.exception.AppDeprecatedError
import com.banglalink.toffee.data.exception.CustomerNotFoundError
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentSplashScreenBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.usecase.AdvertisingIdLogData
import com.banglalink.toffee.usecase.HeaderEnrichmentLogData
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.today
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures.addCallback
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import java.util.concurrent.*
import javax.inject.Inject
import javax.net.ssl.SSLContext


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : BaseFragment() {
    private val binding get() = _binding !!
    private var logoGifDrawable: GifDrawable? = null
    private var isOperationCompleted: Boolean = false
    @Inject @ApplicationContext lateinit var appContext: Context
    @Inject lateinit var commonPreference: CommonPreference
    private var _binding: FragmentSplashScreenBinding? = null
    @Inject lateinit var connectionWatcher: ConnectionWatcher
    private val viewModel by activityViewModels<SplashViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = SplashScreenFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.reportAppLaunch()
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif = binding.splashLogoImageView.drawable ?: binding.splashLogoImageView.background
        if (gif != null && gif is GifDrawable) {
            logoGifDrawable = gif.apply {
                stop()
                seekToFrame(0)
            }
        }
        observeApiLogin()
        observeHeaderEnrichment()
        requestHeaderEnrichment()
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            if (it == R.id.firstEnd) {
                lifecycleScope.launch {
                    delay(1000)
                    with(binding.splashScreenMotionLayout) {
                        setTransition(R.id.firstEnd, R.id.secondEnd)
                        transitionToEnd()
                    }
                }
            }
            if (it == R.id.secondEnd) {
                logoGifDrawable?.start()
                if (isOperationCompleted) {
                    lifecycleScope.launch { 
                        delay(500)
                        launchHomePage()
                    }
                }
                isOperationCompleted = true
            }
        }
        if (!mPref.isPreviousDbDeleted){
            viewModel.deletePreviousDatabase()
        }
        ToffeeAnalytics.logEvent(ToffeeEvents.APP_LAUNCH)
        detectTlsVersion()
    }
    
    private fun detectTlsVersion() {
        try {
            val protocols = SSLContext.getDefault().defaultSSLParameters.protocols
            protocols?.let { 
                ToffeeAnalytics.logEvent(ToffeeEvents.SUPPORTED_TLS, bundleOf(
                    "TLS_version" to protocols.contentToString()
                ))
            }
        } catch (e: Exception) { }
    }
    
    private fun sendAdIdLog() {
        if (mPref.adIdUpdateDate != today) {
            lifecycleScope.launch(IO + Job()) {
                runCatching {
                    var adId: String?
    
                    if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(appContext)) {
                        val adIdInfoCallback = AdvertisingIdClient.getAdvertisingIdInfo(appContext)
                        
                        addCallback(
                            adIdInfoCallback,
                            object : FutureCallback<AdvertisingIdInfo> {
                                override fun onSuccess(adInfo: AdvertisingIdInfo?) {
                                    adId = adInfo?.id
                                    Log.i("AD_ID", "adId: $adId")
                                    adId?.let {
                                        viewModel.sendAdvertisingIdLogData(AdvertisingIdLogData(adId).also {
                                            it.phoneNumber = mPref.phoneNumber
                                            it.isBlNumber = mPref.isBanglalinkNumber
                                        })
                                        mPref.adIdUpdateDate = today
                                    } ?: run {
                                        ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                                    }
                                }
                                
                                override fun onFailure(t: Throwable) {
                                    Log.e("AD_ID", "Failed to connect to Advertising ID provider.")
                                    ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                                }
                            },
                            Executors.newSingleThreadExecutor()
                        )
                    } else {
                        adId = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(appContext).id
                        Log.i("AD_ID", "adId: $adId")
                        adId?.let {
                            viewModel.sendAdvertisingIdLogData(AdvertisingIdLogData(adId).also {
                                it.phoneNumber = mPref.phoneNumber
                                it.isBlNumber = mPref.isBanglalinkNumber
                            })
                            mPref.adIdUpdateDate = today
                        } ?: run {
                            ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                        }
                    }
                }.onFailure {
                    ToffeeAnalytics.logEvent(ToffeeEvents.FETCHING_AD_ID_FAILED)
                }
            }
        }
    }
    
    private fun requestHeaderEnrichment() {
        try {
            if (mPref.heUpdateDate != today && connectionWatcher.isOverCellular) {
                viewModel.getHeaderEnrichment()
            } else {
                requestAppLaunch()
            }
        } catch (e: Exception) {
            requestAppLaunch()
            e.printStackTrace()
        }
    }
    
    private fun observeHeaderEnrichment() {
        observe(viewModel.headerEnrichmentResponse) { response ->
            when (response) {
                is Resource.Success -> {
                    val data = response.data
                    mPref.heUpdateDate = today
                    if (data.isBanglalinkNumber && data.phoneNumber.isNotBlank()) {
                        mPref.latitude = data.lat ?: ""
                        mPref.longitude = data.lon ?: ""
                        mPref.userIp = data.userIp ?: ""
                        mPref.geoCity = data.geoCity ?: ""
                        mPref.geoLocation = data.geoLocation ?: ""
                        mPref.hePhoneNumber = data.phoneNumber
                        mPref.isHeBanglalinkNumber = data.isBanglalinkNumber
                        viewModel.sendHeLogData(HeaderEnrichmentLogData().also {
                            it.phoneNumber = mPref.hePhoneNumber
                            it.isBlNumber = mPref.isHeBanglalinkNumber.toString()
                        })
                    }
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to "HeaderEnrichment",
                            FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                            "error_code" to response.error.code,
                            "error_description" to response.error.msg)
                    )
                    mPref.hePhoneNumber = ""
                    mPref.isHeBanglalinkNumber = false
                }
            }
            requestAppLaunch()
        }
    }
    
    private fun requestAppLaunch() {
        if (mPref.customerId == 0 || mPref.password.isBlank()) {
            viewModel.credentialResponse()
        } else {
            viewModel.loginResponse()
        }
    }
    
    private fun observeApiLogin() {
        observe(viewModel.apiLoginResponse) {
            when (it) {
                is Resource.Success -> {
                    sendAdIdLog()
                    viewModel.sendLoginLogData()
                    viewModel.sendDrmUnavailableLogData()
                    
                    if (isOperationCompleted) {
                        launchHomePage()
                    }
                    isOperationCompleted = true
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.API_LOGIN_V2,
                            FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    when (it.error) {
                        is AppDeprecatedError -> {
                            (it.error as AppDeprecatedError).let { ade->
                                showUpdateDialog(ade.title, ade.updateMsg, ade.forceUpdate)
                            }
                        }
                        is CustomerNotFoundError -> {
                            mPref.clear()
                            requestAppLaunch()
                        }
                        else -> {
                            ToffeeAnalytics.logApiError("apiLoginV2", it.error.msg)
                            binding.root.snack(it.error.msg) {
                                action("Retry") {
                                    requestAppLaunch()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun launchHomePage() {
        ToffeeAnalytics.updateCustomerId(mPref.customerId)
        requireActivity().launchActivity<HomeActivity>()
        requireActivity().finish()
    }
    
    private fun showUpdateDialog(title: String, message: String, forceUpdate: Boolean) {
       val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            setPositiveButton("Update") { _, _ ->
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${requireActivity().packageName}")
                        )
                    )
                }
                catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")
                        )
                    )
                }
                requireActivity().finish()
            }
//            if (! forceUpdate) {
//                setNegativeButton("SKIP") { dialogInterface, _ ->
//                    dialogInterface.dismiss()
//                    viewModel.loginResponse(true)
//                }
//            }
        }
        val alert = builder.create()
        alert.show()
        val updateButton: Button = alert.getButton(DialogInterface.BUTTON_POSITIVE)
//        updateButton.setBackgroundColor(Color.parseColor("#FF3988"))
        updateButton.setTextColor(Color.parseColor("#FF3988"))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}