package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.upload.UploadProgressViewModel
import com.banglalink.toffee.ui.upload.UploadStatus
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_page2.*
import kotlinx.android.synthetic.main.home_mini_upload_progress.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LandingPageFragment : HomeBaseFragment(){

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
    }

    private val viewModel: LandingPageViewModel by activityViewModels()
    private val uploadViewModel by activityViewModels<UploadProgressViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_page2, container, false)
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.app_name_short)

        viewModel.loadMostPopularVideos()
        observeUpload2()
    }

    private fun observeUpload2() {
        add_upload_info_button.setOnClickListener {
            findNavController().navigate(R.id.editUploadInfoFragment)
        }

        lifecycleScope.launchWhenStarted {
            uploadViewModel.getActiveUploadList().collectLatest {
                Log.e("UPLOAD 2", "Collecting ->>> ${it.size}")
                if(it.isNotEmpty()) {
                    home_mini_progress_container.isVisible = true
                    val upInfo = it[0]
                    when(upInfo.status){
                        UploadStatus.SUCCESS.value -> {
                            mini_upload_progress.progress = 100
                            add_upload_info_button.isVisible = true
                            upload_size_text.isInvisible = true
                            mini_upload_progress_text.text = "Upload complete"
                        }
                        UploadStatus.ADDED.value,
                        UploadStatus.STARTED.value -> {
                            add_upload_info_button.isInvisible = true
                            upload_size_text.isVisible = true
                            mini_upload_progress_text.text = "Uploading - ${upInfo.completedPercent}%"
                            mini_upload_progress.progress = upInfo.completedPercent
                            upload_size_text.text = Utils.readableFileSize(upInfo.fileSize)
                        }
                    }
                } else {
                    home_mini_progress_container.isVisible = false
                }
            }
        }
    }
}