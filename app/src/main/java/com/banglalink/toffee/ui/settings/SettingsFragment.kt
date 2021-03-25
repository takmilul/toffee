package com.banglalink.toffee.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.databinding.ActivitySettingsBinding
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HtmlPageViewActivity
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var userActivitiesRepository: UserActivitiesRepository

    var wifiProfileDesc = arrayOf("160p", "240p", "320p", "576p", "720p", "Auto")
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
    
    private var _binding: ActivitySettingsBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.wifiProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPref.wifiProfileStatus = progress + 1
                binding.wifiProfileStatusTv.text = getString(R.string.txt_video_resolution, wifiProfileRes[progress])
                binding.wifiProfileDescTxt.text = getString(wifiProfileBWRequiredTxt[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.cellularProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPref.cellularProfileStatus = progress + 1
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
//        binding.darkThemeToggleBtn.setOnCheckedChangeListener { switch, isChecked -> changeAppTheme(isChecked) }
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
        binding.cellularProfileDescTxt.text =
            getString(cellularProfileBWRequiredTxt[mPref.cellularProfileStatus - 1])
        binding.cellularProfileStatusTv.text =getString(R.string.txt_video_resolution, cellularProfileRes[mPref.cellularProfileStatus - 1])
        binding.cellularProfileStateBar.progress = mPref.cellularProfileStatus - 1
        binding.wifiProfileStateBar.progress = mPref.wifiProfileStatus - 1
        binding.wifiProfileStatusTv.text =getString(R.string.txt_video_resolution,  wifiProfileRes[mPref.wifiProfileStatus - 1])
        binding.wifiProfileDescTxt.text =
            getString(wifiProfileBWRequiredTxt[mPref.wifiProfileStatus - 1])
//        val isDarkEnabled = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
//        binding.darkThemeToggleBtn.isChecked = isDarkEnabled
    }

    private fun handleNotificationChange() {
        mPref.setNotificationEnabled(binding.notificationSwitch.isChecked)
        binding.enableNotification = mPref.isNotificationEnabled()
    }

    private fun handleFloatingWindowPrefChange() {
        mPref.isEnableFloatingWindow = binding.prefFloatingWindow.isChecked
        binding.enableFloatingWindow = mPref.isEnableFloatingWindow
    }

    private fun handleDefaultDataQualityToggleBtn() {
        mPref.setDefaultDataQuality(binding.dataQualityToggleBtn.isChecked)
        binding.defaultDataQuality = mPref.defaultDataQuality()
    }

    private fun handleWatchOnlyWifiToggleBtn() {
        mPref.setWatchOnlyWifi(binding.watchOnlyWifiToggleBtn.isChecked)
        binding.watchWifiOnly = mPref.watchOnlyWifi()
    }
    
    private fun changeAppTheme(isDarkEnabled: Boolean){
        if (isDarkEnabled) {
            mPref.appThemeMode = Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            mPref.appThemeMode = Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun setPrefItemListener() {
        binding.prefClearWatch.setOnClickListener { onClickClearWatchHistory() }
        binding.prefPrivacy.setOnClickListener { onClickPrivacyPolicy() }
        binding.prefTerms.setOnClickListener { onClickTermsAndConditions() }

        binding.prefAbout.setOnClickListener {
            startActivity(Intent(requireActivity(), AboutActivity::class.java))
        }
    }

    private fun onClickClearWatchHistory() {
        VelBoxAlertDialogBuilder(requireContext()).apply {
            setTitle("Clear Activities")
            setText("Are you sure that you want to clear Activities?")
            setPositiveButtonListener("Clear") {
                lifecycleScope.launch {
                    userActivitiesRepository.deleteAll()
                    Toast.makeText(requireContext(), "Activities cleared", Toast.LENGTH_SHORT).show()
                    it?.dismiss()
                }
            }
        }.create().show()
    }

    private fun onClickTermsAndConditions() {
        val intent = Intent(requireActivity(), HtmlPageViewActivity::class.java).apply {
            putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.TERMS_AND_CONDITION_URL)
            putExtra(HtmlPageViewActivity.TITLE_KEY, "Terms and Conditions")
        }
        startActivity(intent)
    }

    private fun onClickPrivacyPolicy() {
        val intent = Intent(requireActivity(), HtmlPageViewActivity::class.java).apply {
            putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.PRIVACY_POLICY_URL)
            putExtra(HtmlPageViewActivity.TITLE_KEY, "Privacy Policy")
        }
        startActivity(intent)
    }

    companion object {
        const val PREF_KEY_NOTIFICATION = "pref_key_notification"
    }
}