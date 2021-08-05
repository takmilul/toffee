package com.banglalink.toffee.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.databinding.FragmentSettingsBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    @Inject lateinit var userActivitiesRepository: UserActivitiesRepository
    var wifiProfileRes = arrayOf("240x160", "320x240", "480x320", "720x576", "1280x720", "Auto")
    var wifiProfileBWRequiredTxt = intArrayOf(R.string.profile_240x160,
        R.string.profile_320x240,
        R.string.profile_480x320,
        R.string.profile_720x576,
        R.string.profile_1280x720,
        R.string.profile_1920x1080)
    var cellularProfileDesc = arrayOf("160p", "320p", "480p", "720p", "Auto")
    var cellularProfileRes = arrayOf("240x160", "480x320", "720x480", "1280x720", "Auto")
    var cellularProfileBWRequiredTxt = intArrayOf(R.string.profile_240x160,
        R.string.profile_480x320,
        R.string.profile_720x576,
        R.string.profile_1280x720,
        R.string.profile_1920x1080)
    
    private var _binding: FragmentSettingsBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.prefClearWatch.isVisible = mPref.isVerifiedUser
        binding.clearWatchDivider.isVisible = mPref.isVerifiedUser
        binding.wifiProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPref.wifiProfileStatus = progress + 1
                ToffeeAnalytics.logEvent(ToffeeEvents.SETTINGS_VIDEO_RESOLUTION)
                binding.wifiProfileStatusTv.text = getString(R.string.txt_video_resolution, wifiProfileRes[progress])
                binding.wifiProfileDescTxt.text = getString(wifiProfileBWRequiredTxt[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.cellularProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPref.cellularProfileStatus = progress + 1
                ToffeeAnalytics.logEvent(ToffeeEvents.SETTINGS_VIDEO_RESOLUTION)
                binding.cellularProfileDescTxt.text = getString(cellularProfileBWRequiredTxt[progress])
                binding.cellularProfileStatusTv.text = getString(R.string.txt_video_resolution, cellularProfileRes[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.dataQualityToggleBtn.setOnCheckedChangeListener { _, _ -> handleDefaultDataQualityToggleBtn() }
        binding.watchOnlyWifiToggleBtn.setOnCheckedChangeListener { _, _ -> handleWatchOnlyWifiToggleBtn() }
        binding.notificationSwitch.setOnCheckedChangeListener { _, _ -> handleNotificationChange() }
        binding.prefFloatingWindow.setOnCheckedChangeListener { _, _ -> handleFloatingWindowPrefChange() }
        binding.prefAspectRatio.setOnCheckedChangeListener { _, _ -> handleAspectRatioPrefChange() }
        initializeSettings()

        setPrefItemListener()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun initializeSettings() {
        binding.defaultDataQuality = mPref.defaultDataQuality()
        binding.watchWifiOnly = mPref.watchOnlyWifi()
        binding.enableNotification = mPref.isNotificationEnabled()
        binding.enableFloatingWindow = mPref.isEnableFloatingWindow
        binding.keepAspectRatio = mPref.keepAspectRatio
        binding.cellularProfileDescTxt.text =
            getString(cellularProfileBWRequiredTxt[mPref.cellularProfileStatus - 1])
        binding.cellularProfileStatusTv.text =getString(R.string.txt_video_resolution, cellularProfileRes[mPref.cellularProfileStatus - 1])
        binding.cellularProfileStateBar.progress = mPref.cellularProfileStatus - 1
        binding.wifiProfileStateBar.progress = mPref.wifiProfileStatus - 1
        binding.wifiProfileStatusTv.text =getString(R.string.txt_video_resolution,  wifiProfileRes[mPref.wifiProfileStatus - 1])
        binding.wifiProfileDescTxt.text =
            getString(wifiProfileBWRequiredTxt[mPref.wifiProfileStatus - 1])
    }

    private fun handleNotificationChange() {
        mPref.setNotificationEnabled(binding.notificationSwitch.isChecked)
        binding.enableNotification = mPref.isNotificationEnabled()
    }

    private fun handleFloatingWindowPrefChange() {
        mPref.isEnableFloatingWindow = binding.prefFloatingWindow.isChecked
        binding.enableFloatingWindow = mPref.isEnableFloatingWindow
    }

    private fun handleAspectRatioPrefChange() {
        mPref.keepAspectRatio = binding.prefAspectRatio.isChecked
        binding.keepAspectRatio = mPref.keepAspectRatio
    }

    private fun handleDefaultDataQualityToggleBtn() {
        mPref.setDefaultDataQuality(binding.dataQualityToggleBtn.isChecked)
        binding.defaultDataQuality = mPref.defaultDataQuality()
    }

    private fun handleWatchOnlyWifiToggleBtn() {
        mPref.setWatchOnlyWifi(binding.watchOnlyWifiToggleBtn.isChecked)
        binding.watchWifiOnly = mPref.watchOnlyWifi()
    }
    
    private fun setPrefItemListener() {
        binding.prefClearWatch.setOnClickListener { onClickClearWatchHistory() }
        binding.prefPrivacy.setOnClickListener { onClickPrivacyPolicy() }
        binding.prefTerms.setOnClickListener { onClickTermsAndConditions() }

        binding.prefAbout.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.AboutFragment && findNavController().currentDestination?.id == R.id.menu_settings) {
                val action = SettingsFragmentDirections.actionSettingsFragmentToAboutFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun onClickClearWatchHistory() {
        VelBoxAlertDialogBuilder(requireContext()).apply {
            setTitle("Clear Activities")
            setText("Are you sure that you want to clear Activities?")
            setPositiveButtonListener("Clear") {
                lifecycleScope.launch {
                    userActivitiesRepository.deleteAll()
                    requireContext().showToast("Activities cleared")
                    it?.dismiss()
                }
            }
        }.create().show()
    }

    private fun onClickTermsAndConditions() {
        if (findNavController().currentDestination?.id != R.id.termsAndConditionFragment&& findNavController().currentDestination?.id == R.id.menu_settings) {
            val action = SettingsFragmentDirections.actionSettingsFragmentToTermsAndConditions("Terms & Conditions", mPref.termsAndConditionUrl)
            findNavController().navigate(action)
        }
    }

    private fun onClickPrivacyPolicy() {
        if (findNavController().currentDestination?.id != R.id.privacyPolicyFragment && findNavController().currentDestination?.id == R.id.menu_settings) {
            val action = SettingsFragmentDirections.actionSettingsFragmentToPrivacyPolicy("Privacy Policy", mPref.privacyPolicyUrl)
            findNavController().navigate(action)
        }
    }

    companion object {
        const val PREF_KEY_NOTIFICATION = "pref_key_notification"
    }
}