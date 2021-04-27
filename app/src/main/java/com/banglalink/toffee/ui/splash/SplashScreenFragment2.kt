package com.banglalink.toffee.ui.splash

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentSplashScreen2Binding
import com.banglalink.toffee.exception.AppDeprecatedError
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.facebook.appevents.AppEventsLogger
import com.facebook.shimmer.Shimmer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment2 : BaseFragment() {
    private var isOperationCompleted: Boolean = false
    @Inject lateinit var commonPreference: CommonPreference
    private var _binding: FragmentSplashScreen2Binding? = null
    private val binding get() = _binding !!
    private val viewModel by activityViewModels<SplashViewModel>()
    
    companion object {
        @JvmStatic
        fun newInstance() = SplashScreenFragment2()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.reportAppLaunch()
        _binding = FragmentSplashScreen2Binding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (mPref.customerId != 0 && mPref.password.isNotEmpty()) {
            observeApiLogin()
            viewModel.loginResponse()
        }
        else {
            observeApiLogin()
            viewModel.credentialResponse()
        }
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            if (it == R.id.firstEnd) {
                lifecycleScope.launch {
                    delay(300)
                    with(binding.splashScreenMotionLayout) {
                        setTransition(R.id.firstEnd, R.id.secondEnd)
                        transitionToEnd()
                    }
                }
            }
            if (it == R.id.secondEnd) {
                val shimmer = Shimmer.AlphaHighlightBuilder()
                    .setBaseAlpha(1f)
                    .setHighlightAlpha(0.4f)
                    .setDuration(500L)
                
                binding.splashLogoImageView.setShimmer(shimmer.build())
                binding.splashLogoImageView.startShimmer()
                if (isOperationCompleted) {
                    launchHomePage()
                }
                isOperationCompleted = true
            }
        }
        if (!mPref.isPreviousDbDeleted){
            viewModel.deletePreviousDatabase()
        }
        val appEventsLogger = AppEventsLogger.newLogger(requireContext())
        appEventsLogger.logEvent("app_launch")
        appEventsLogger.flush()
    }
    
    private fun observeApiLogin(skipUpdate: Boolean = false) {
        observe(viewModel.apiLoginResponse) {
            when (it) {
                is Resource.Success -> {
                    if (isOperationCompleted) {
                        launchHomePage()
                    }
                    isOperationCompleted = true
                }
                is Resource.Failure -> {
                    when (it.error) {
                        is AppDeprecatedError -> {
                            showUpdateDialog(it.error.title, it.error.updateMsg, it.error.forceUpdate)
                        }
                        else -> {
                            ToffeeAnalytics.logApiError("apiLogin", it.error.msg)
                            binding.root.snack(it.error.msg) {
                                action("Retry") {
                                    viewModel.loginResponse(skipUpdate)
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)
        
        builder.setPositiveButton("Update") { _, _ ->
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
        
        if (! forceUpdate) {
            builder.setNegativeButton("SKIP") { dialogInterface, _ ->
                dialogInterface.dismiss()
                viewModel.loginResponse(true)
            }
        }
        
        val alertDialog = builder.create()
        alertDialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mPref.logout = "0"
    }
}