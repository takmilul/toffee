package com.banglalink.toffee.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivitySettingsBinding
import com.banglalink.toffee.databinding.FragmentAboutBinding
import com.banglalink.toffee.databinding.FragmentAboutPointsBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.HtmlPageViewActivity

class AboutFragment : BaseFragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.versionTv.text = getVersionText()

        binding.termsConditionsTv.setOnClickListener {
            onClickTermsAndConditions()
        }

        binding.privacyPolicyTv.setOnClickListener {
            onClickPrivacyPolicy()
        }

        binding.checkUpdate.setOnClickListener {

            onClickCheckUpdateButton()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun onClickTermsAndConditions() {
        if (findNavController().currentDestination?.id != R.id.termsAndConditionFragment && findNavController().currentDestination?.id == R.id.AboutFragment) {
            val action = AboutFragmentDirections.actionAboutFragmentToTermsAndConditons("Terms & Conditions",
                requireContext().getString(R.string.terms_and_conditions_url))
            findNavController().navigate(action)
        }
    }

    fun onClickPrivacyPolicy() {

        if (findNavController().currentDestination?.id != R.id.privacyPolicyFragment && findNavController().currentDestination?.id == R.id.AboutFragment) {
            val action = AboutFragmentDirections.actionAboutFragmentToPrivacyPolicy("Privacy Policy",requireContext().getString(R.string.privacy_policy_url))
            findNavController().navigate(action)
        }
    }

    fun onClickCheckUpdateButton() {
        val packName= requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packName")))
        }
    }


    private fun getVersionText(): String =
        try {
            val pInfo =  requireContext().packageManager.getPackageInfo( requireContext().packageName, 0)
            "Version ${pInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

    companion object {
        const val PRIVACY_POLICY_URL = "https://www.banglalink.net/en/toffee-privacy-policy"
        const val TERMS_AND_CONDITION_URL = "https://www.banglalink.net/en/toffee-privacy-policy"
        const val PRIVACY_POLICY_URL_OLD = "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy"
        const val TERMS_AND_CONDITION_URL_OLD = "https://www.banglalink.net/en/personal/digital-services/toffee-privacy-policy"
    }

}