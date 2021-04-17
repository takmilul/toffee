package com.banglalink.toffee.ui.common

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPrivacyPolicyBinding
import com.banglalink.toffee.databinding.FragmentReferAFriendBinding
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.about.AboutFragment

class PrivacyPolicyFragment : BaseFragment() {

    private lateinit var _binding: FragmentPrivacyPolicyBinding
    private val binding get() = _binding!!

    private var htmlUrl: String? = AboutFragment.PRIVACY_POLICY_URL
    private var header: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        binding.webview.settings.javaScriptEnabled = true
        if(header.isNullOrEmpty()){
            binding.webview.loadUrl(htmlUrl)
        }else{
            val headerMap: MutableMap<String, String> = HashMap()
            headerMap["MSISDN"] = header!!
            binding.webview.loadUrl(htmlUrl,headerMap)
        }
    }

}