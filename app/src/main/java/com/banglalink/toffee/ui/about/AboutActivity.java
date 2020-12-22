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

import static com.banglalink.toffee.model.AppSettingsKt.PRIVACY_POLICY_URL;
import static com.banglalink.toffee.model.AppSettingsKt.TERMS_AND_CONDITION_URL;


public class AboutActivity extends BaseAppCompatActivity {

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
        intent.putExtra(HtmlPageViewActivity.CONTENT_KEY, TERMS_AND_CONDITION_URL);
        intent.putExtra(HtmlPageViewActivity.TITLE_KEY, "Terms and Conditions");
        startActivity(intent);

    }

    public void onClickPrivacyPolicy(View view) {
        Intent intent = new Intent(AboutActivity.this, HtmlPageViewActivity.class);
        intent.putExtra(HtmlPageViewActivity.CONTENT_KEY, PRIVACY_POLICY_URL);
        intent.putExtra(HtmlPageViewActivity.TITLE_KEY, "Privacy Policy");
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
