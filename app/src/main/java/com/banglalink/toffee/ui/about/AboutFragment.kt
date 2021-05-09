package com.banglalink.toffee.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.databinding.FragmentAboutBinding
import com.banglalink.toffee.ui.common.BaseFragment

class AboutFragment : BaseFragment() {
    
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.versionTv.text = getVersionText()
        binding.checkUpdate.setOnClickListener {
            onClickCheckUpdateButton()
        }
    }
    
    private fun onClickCheckUpdateButton() {
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}