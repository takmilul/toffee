package com.banglalink.toffee.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.databinding.FragmentSplashScreenBinding
import com.banglalink.toffee.extension.onTransitionCompletedListener
import com.banglalink.toffee.ui.common.BaseFragment
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment:BaseFragment() {
    @Inject lateinit var commonPreference: CommonPreference
    private var _binding: FragmentSplashScreenBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<SplashViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = SplashScreenFragment()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        viewModel.reportAppLaunch()
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.splashScreenMotionLayout.onTransitionCompletedListener {
            if(mPref.logout.equals("1")){
                mPref.logout = "0"
                lifecycleScope.launch {
                    if(findNavController().currentDestination?.id != R.id.signInFragment && findNavController().currentDestination?.id == R.id.splashScreenFragment) {
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToSigninByPhoneFragment())
                    }
                }
            }
            else if (mPref.customerId != 0 && mPref.password.isNotEmpty()){
                (requireActivity() as SplashScreenActivity).observeApiLogin()
                viewModel.loginResponse()
            }
            else{
                (requireActivity() as SplashScreenActivity).observeApiLogin()
                viewModel.credentialResponse()
            }
            
            val appEventsLogger = AppEventsLogger.newLogger(requireContext())
            appEventsLogger.logEvent("app_launch")
            appEventsLogger.flush()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPref.logout="0"
    }
}