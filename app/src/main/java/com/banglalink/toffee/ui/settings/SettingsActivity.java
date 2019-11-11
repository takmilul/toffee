package com.banglalink.toffee.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.databinding.DataBindingUtil;

import com.banglalink.toffee.R;
import com.banglalink.toffee.data.storage.Preference;
import com.banglalink.toffee.databinding.ActivitySettingsBinding;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.suke.widget.SwitchButton;


public class SettingsActivity extends BaseAppCompatActivity {


    String[] wifiProfileDesc = {"160p", "240p", "320p", "576p", "720p", "Auto"};
    String[] wifiProfileRes = {"240x160", "320x240", "480x320", "720x576", "1280x720", "Auto"};
    int[] wifiProfileBWRequiredTxt = {R.string.profile_240x160,
            R.string.profile_320x240,
            R.string.profile_480x320,
            R.string.profile_720x576,
            R.string.profile_1280x720,
            R.string.profile_1920x1080};
    protected AppCompatSeekBar wifiStateProgressBar;

    String[] cellularProfileDesc = {"160p", "320p", "480p", "720p", "Auto"};
    String[] cellularProfileRes = {"240x160", "480x320", "720x480", "1280x720", "Auto"};
    int[] cellularProfileBWRequiredTxt = {R.string.profile_240x160,
            R.string.profile_480x320,
            R.string.profile_720x576,
            R.string.profile_1280x720,
            R.string.profile_1920x1080};
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_settings);

        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        wifiStateProgressBar = findViewById(R.id.wifi_profile_state_bar);
        binding.wifiProfileStateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Preference.Companion.getInstance().setWifiProfileStatus(progress+1);
                binding.wifiProfileStatusTv.setText("Current Status: " + wifiProfileRes[progress]);
                binding.wifiProfileDescTxt.setText(getString(wifiProfileBWRequiredTxt[progress]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.cellularProfileStateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Preference.Companion.getInstance().setCellularProfileStatus(progress+1);
                binding.cellularProfileDescTxt.setText(getString(cellularProfileBWRequiredTxt[progress]));
                binding.cellularProfileStatusTv.setText("Current Status: " + cellularProfileRes[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.dataQualityToggleBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                handleDefaultDataQualityToggleBtn();
            }
        });

        binding.watchOnlyWifiToggleBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                handleWatchOnlyWifiToggleBtn();
            }
        });

        initializeSettings();
    }

    private void initializeSettings() {
        boolean defaultDataQuality = Preference.Companion.getInstance().defaultDataQuality();
        binding.dataQualityToggleBtn.setChecked(defaultDataQuality);

        boolean watchWifiOnly = Preference.Companion.getInstance().watchOnlyWifi();
        binding.watchOnlyWifiToggleBtn.setChecked(watchWifiOnly);

        binding.profileLayout.setVisibility(defaultDataQuality ? LinearLayout.GONE : LinearLayout.VISIBLE);
        binding.cellularProfileLayout.setVisibility(watchWifiOnly ? LinearLayout.GONE : LinearLayout.VISIBLE);
        binding.cellularProfileDescTxt.setText(getString(cellularProfileBWRequiredTxt[Preference.Companion.getInstance().getCellularProfileStatus() - 1]));
        binding.cellularProfileStatusTv.setText("Current Status: " + cellularProfileRes[Preference.Companion.getInstance().getCellularProfileStatus() - 1]);
        binding.cellularProfileStateBar.setProgress(Preference.Companion.getInstance().getCellularProfileStatus() - 1);

        wifiStateProgressBar.setProgress(Preference.Companion.getInstance().getWifiProfileStatus()-1);
        binding.wifiProfileStatusTv.setText("Current Status: " + wifiProfileRes[Preference.Companion.getInstance().getWifiProfileStatus()-1]);
        binding.wifiProfileDescTxt.setText(getString(wifiProfileBWRequiredTxt[Preference.Companion.getInstance().getWifiProfileStatus()-1]));
    }

    public void handleDefaultDataQualityToggleBtn() {
        Preference.Companion.getInstance().setDefaultDataQuality(binding.dataQualityToggleBtn.isChecked());
        initializeSettings();
    }


    public void handleWatchOnlyWifiToggleBtn() {
        Preference.Companion.getInstance().setWatchOnlyWifi(binding.watchOnlyWifiToggleBtn.isChecked());
        initializeSettings();
    }
}
