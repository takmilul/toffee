package com.banglalink.toffee.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentAccountBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : BaseFragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var bindingUtil: BindingUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Account"
        setProfileInfo()
        setPrefItemListener()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun setPrefItemListener() {
        binding.prefMyProfile.setOnClickListener { onClickProfile() }
        binding.prefActivities.setOnClickListener { onClickActivities() }
        binding.prefPlaylist.setOnClickListener { onClickMyPlaylist() }
        binding.prefManagePaymentMethod.setOnClickListener{ onClickManagePaymentMehtods() }
        binding.prefFavorites.setOnClickListener { onClickFavorites() }
        binding.prefSubscriptions.setOnClickListener { onClickSubscriptions() }
    }

    private fun onClickProfile() {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to "Profile"))
            findNavController().navigate(R.id.profileFragment)
        }
    }
    
    private fun onClickActivities() {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to getString(R.string.menu_activities)))
            ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_ACTIVITIES)
            findNavController().navigate(R.id.menu_activities)
        }
    }

    private fun onClickMyPlaylist() {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to getString(R.string.menu_playlists)))
            findNavController().navigate(R.id.menu_playlist)
        }
    }

    private fun onClickManagePaymentMehtods(){
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to getString(R.string.menu_manage_payment_methods)))
            findNavController().navigate(R.id.menu_manage_payment_methods)
        }
    }

    private fun onClickFavorites() {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to getString(R.string.menu_favorites)))
            ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_FAVORITES)
            findNavController().navigate(R.id.menu_favorites)
        }
    }

    private fun onClickSubscriptions() {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "account",
                    "method" to "mobile"
                )
            )
        }
        activity?.checkVerification {
            ToffeeAnalytics.logEvent(ToffeeEvents.MENU_CLICK, bundleOf("selected_menu" to getString(R.string.menu_subscriptions)))
            findNavController().navigate(R.id.menu_subscriptions)
        }
    }

    private fun setProfileInfo() {
        if (mPref.isVerifiedUser) {
            if (mPref.customerName.isNotBlank()) binding.profileName.text = mPref.customerName
            if (!mPref.userImageUrl.isNullOrBlank()) bindingUtil.bindRoundImage(binding.profileImageView, mPref.userImageUrl)

           observe(mPref.customerNameLiveData) {
                when {
                    it.isBlank() -> binding.profileName.text = getString(R.string.profile)
                    else -> {
                        binding.profileName.text = mPref.customerName
                    }
                }
            }
            observe(mPref.profileImageUrlLiveData) {
                when (it) {
                    is String -> bindingUtil.bindRoundImage(binding.profileImageView, it)
                    is Int -> bindingUtil.loadImageFromResource(binding.profileImageView, it)
                }
            }
        }
    }
}