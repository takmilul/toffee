package com.banglalink.toffee.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityAboutBinding
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.common.HtmlPageViewActivity

class AboutActivity : BaseAppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        binding.toolbar.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.setNavigationOnClickListener { onBackPressed() }
        }

        binding.versionTv.text = getVersionText()
    }

    fun onClickTermsAndConditions(view: View?) {
        val intent = Intent(this, HtmlPageViewActivity::class.java).apply {
            putExtra(HtmlPageViewActivity.CONTENT_KEY, TERMS_AND_CONDITION_URL)
            putExtra(HtmlPageViewActivity.TITLE_KEY, "Terms and Conditions")
        }
        startActivity(intent)
    }

    fun onClickPrivacyPolicy(view: View?) {
        val intent = Intent(this, HtmlPageViewActivity::class.java).apply {
            putExtra(HtmlPageViewActivity.CONTENT_KEY, PRIVACY_POLICY_URL)
            putExtra(HtmlPageViewActivity.TITLE_KEY, "Privacy Policy")
        }
        startActivity(intent)
    }

    fun onClickCheckUpdateButton(view: View?) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun getVersionText(): String =
        try {
            val pInfo = application.packageManager.getPackageInfo(application.packageName, 0)
            "Version ${pInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

    companion object {
        const val PRIVACY_POLICY_URL =
            "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy"
        const val TERMS_AND_CONDITION_URL =
            "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy"
    }
}