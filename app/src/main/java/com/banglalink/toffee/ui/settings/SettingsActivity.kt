package com.banglalink.toffee.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.ActivitySettingsBinding
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingsActivity : BaseAppCompatActivity() {
    var wifiProfileDesc = arrayOf("160p", "240p", "320p", "576p", "720p", "Auto")
    var wifiProfileRes = arrayOf("240x160", "320x240", "480x320", "720x576", "1280x720", "Auto")
    var wifiProfileBWRequiredTxt = intArrayOf(R.string.profile_240x160,
        R.string.profile_320x240,
        R.string.profile_480x320,
        R.string.profile_720x576,
        R.string.profile_1280x720,
        R.string.profile_1920x1080)
    private var wifiStateProgressBar: AppCompatSeekBar? = null
    var cellularProfileDesc = arrayOf("160p", "320p", "480p", "720p", "Auto")
    var cellularProfileRes = arrayOf("240x160", "480x320", "720x480", "1280x720", "Auto")
    var cellularProfileBWRequiredTxt = intArrayOf(R.string.profile_240x160,
        R.string.profile_480x320,
        R.string.profile_720x576,
        R.string.profile_1280x720,
        R.string.profile_1920x1080)
    
    @Inject lateinit var preference: Preference
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        wifiStateProgressBar = findViewById(R.id.wifi_profile_state_bar)
        binding.wifiProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                preference.wifiProfileStatus = progress + 1
                binding.wifiProfileStatusTv.text = "Current Status: " + wifiProfileRes[progress]
                binding.wifiProfileDescTxt.text = getString(wifiProfileBWRequiredTxt[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.cellularProfileStateBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                preference.cellularProfileStatus = progress + 1
                binding.cellularProfileDescTxt.text = getString(cellularProfileBWRequiredTxt[progress])
                binding.cellularProfileStatusTv.text = "Current Status: " + cellularProfileRes[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.dataQualityToggleBtn.setOnCheckedChangeListener { switch, isChecked -> handleDefaultDataQualityToggleBtn() }
        binding.watchOnlyWifiToggleBtn.setOnCheckedChangeListener { switch, isChecked -> handleWatchOnlyWifiToggleBtn() }
        binding.darkThemeToggleBtn.setOnCheckedChangeListener { switch, isChecked -> changeAppTheme(isChecked) }
        initializeSettings()
    }

    private fun initializeSettings() {
        binding.defaultDataQuality = preference.defaultDataQuality()
        binding.watchWifiOnly = preference.watchOnlyWifi()
        /*binding.watchOnlyWifiToggleBtn.isChecked = watchWifiOnly
        binding.dataQualityToggleBtn.isChecked = defaultDataQuality
        binding.cellularProfileLayout.visibility = if (watchWifiOnly) View.GONE else View.VISIBLE
        binding.profileLayout.visibility = if (defaultDataQuality) View.GONE else View.VISIBLE*/
        binding.cellularProfileDescTxt.text =
            getString(cellularProfileBWRequiredTxt[preference.cellularProfileStatus - 1])
        binding.cellularProfileStatusTv.text =
            "Current Status: " + cellularProfileRes[preference.cellularProfileStatus - 1]
        binding.cellularProfileStateBar.progress = preference.cellularProfileStatus - 1
        wifiStateProgressBar!!.progress = preference.wifiProfileStatus - 1
        binding.wifiProfileStatusTv.text =
            "Current Status: " + wifiProfileRes[preference.wifiProfileStatus - 1]
        binding.wifiProfileDescTxt.text =
            getString(wifiProfileBWRequiredTxt[preference.wifiProfileStatus - 1])
        val isDarkEnabled = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        binding.darkThemeToggleBtn.isChecked = isDarkEnabled
    }

    fun handleDefaultDataQualityToggleBtn() {
        preference.setDefaultDataQuality(binding.dataQualityToggleBtn.isChecked)
        binding.defaultDataQuality = preference.defaultDataQuality()
//        initializeSettings()
    }

    fun handleWatchOnlyWifiToggleBtn() {
        preference.setWatchOnlyWifi(binding.watchOnlyWifiToggleBtn.isChecked)
        binding.watchWifiOnly = preference.watchOnlyWifi()
//        initializeSettings()
    }
    
    private fun changeAppTheme(isDarkEnabled: Boolean){
        if (isDarkEnabled) {
            preference.appThemeMode = Configuration.UI_MODE_NIGHT_YES
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            preference.appThemeMode = Configuration.UI_MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}