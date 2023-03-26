package com.banglalink.toffee.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.extension.isTestEnvironment
import com.banglalink.toffee.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : BaseFragment() {
    
    @Inject lateinit var toffeeConfig: ToffeeConfig
//    private var _binding: FragmentAboutBinding? = null
//    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AboutPageView()
            }
        }
//        _binding = FragmentAboutBinding.inflate(inflater, container, false)
//        return binding.root
    }
    
    @Composable
    private fun AboutPageView() {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AboutLogo()
            TitleText(
                text = stringResource(id = string.about_toffee_title),
                modifier = Modifier.padding(top = 24.dp),
            )
            BodyText(
                text = stringResource(id = string.about_toffee_text) ,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 18F,
                textAlign = TextAlign.Justify
            )
            TitleText(
                text = stringResource(id = string.about_feature_title),
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            resources.getStringArray(R.array.about_feature_text_list).forEach { 
                AboutFeatureTextItem(text = it)
            }
            TitleText(
                text = stringResource(id = string.about_message),
                modifier = Modifier.padding(top = 24.dp),
                textSize = 14f
            )
            TitleText(
                text = getVersionText(),
                modifier = Modifier.padding(top = 24.dp),
                textSize = 16f,
                textAlign = TextAlign.Center
            )
            UpdateButton {
                onClickCheckUpdateButton()
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "About"
//        _binding?.versionTv?.text = getVersionText()
//        _binding?.checkUpdate?.setOnClickListener {
//            onClickCheckUpdateButton()
//        }
    }
    
    private fun onClickCheckUpdateButton() {
        val packName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packName")))
        }
    }
    
    private fun getVersionText(): String =
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            "Version ${pInfo.versionName}" + if (toffeeConfig.toffeeBaseUrl.isTestEnvironment()) " (Test Environment)" else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}