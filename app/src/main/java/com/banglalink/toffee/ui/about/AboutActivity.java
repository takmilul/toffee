package com.banglalink.toffee.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.banglalink.toffee.R;
import com.banglalink.toffee.databinding.ActivityAboutBinding;
import com.banglalink.toffee.ui.common.BaseAppCompatActivity;
import com.banglalink.toffee.ui.common.HtmlPageViewActivity;


public class AboutActivity extends BaseAppCompatActivity {

    public static final String PRIVACY_POLICY_URL = "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy";
    public static final String TERMS_AND_CONDITION_URL = "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy";

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_about);

        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        ((TextView) findViewById(R.id.version_tv)).setText(getVersionText());
    }


    public void onClickTermsAndConditions(View view) {
        Intent intent = new Intent(AboutActivity.this, HtmlPageViewActivity.class);
        intent.putExtra(HtmlPageViewActivity.Companion.getCONTENT_KEY(), TERMS_AND_CONDITION_URL);
        intent.putExtra(HtmlPageViewActivity.Companion.getTITLE_KEY(), "Terms and Conditions");
        startActivity(intent);

    }

    public void onClickPrivacyPolicy(View view) {
        Intent intent = new Intent(AboutActivity.this, HtmlPageViewActivity.class);
        intent.putExtra(HtmlPageViewActivity.Companion.getCONTENT_KEY(), PRIVACY_POLICY_URL);
        intent.putExtra(HtmlPageViewActivity.Companion.getTITLE_KEY(), "Privacy Policy");
        startActivity(intent);
    }

    public void onClickCheckUpdateButton(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public String getVersionText() {
        String version = "";
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            version = pInfo.versionName;

            version = "Version " + version;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }


}
