package com.banglalink.toffee.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_minimize_upload.*
import kotlinx.coroutines.flow.collectLatest
import net.gotev.uploadservice.UploadService
import javax.inject.Inject

@AndroidEntryPoint
class MinimizeUploadFragment: BaseFragment() {
    private lateinit var uploadId: String
    private var contentId: Long = -1
    private var uploadIdLong = -1L

    @Inject
    lateinit var uploadRepo: UploadInfoRepository

    companion object {
        const val UPLOAD_ID = "UPLOAD_ID"
        const val CONTENT_ID = "SERVER_CONTENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uploadId = requireArguments().getString(UPLOAD_ID)!!
        uploadIdLong = UtilsKt.stringToUploadId(uploadId)
        contentId = requireArguments().getLong(CONTENT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_minimize_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancel_button.setOnClickListener {
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setTitle("Cancel Uploading")
                setText("Are you sure that you want to\n" +
                        "cancel uploading video?")
                setPositiveButtonListener("NO") {
                    it?.dismiss()
                }
                setNegativeButtonListener("YES") {
                    UploadService.stopAllUploads()
                    findNavController().popBackStack(R.id.menu_feed, false)
                    it?.dismiss()
                }
            }.create().show()
        }

        minimize_button.setOnClickListener {
            findNavController().popBackStack(R.id.menu_feed, false)
        }

        observeUpload()
    }

    private fun observeUpload() {
        lifecycleScope.launchWhenStarted {
            uploadRepo.getUploadFlowById(uploadIdLong).collectLatest { uploadInfo ->
                when(uploadInfo?.status) {
                    UploadStatus.SUCCESS.value,
                    UploadStatus.SUBMITTED.value,
                    -> {
                        uploadPercent.text = "100%"
                        progressBar.progress = 100
                        uploadSize.text = "of ${Utils.readableFileSize(uploadInfo.fileSize)}"
                        findNavController().popBackStack(R.id.menu_feed, false)
                    }
                    UploadStatus.ADDED.value,
                    UploadStatus.STARTED.value -> {
                        uploadPercent.text = "${uploadInfo.completedPercent}%"
                        progressBar.progress = uploadInfo.completedPercent
                        uploadSize.text = "of ${Utils.readableFileSize(uploadInfo.fileSize)}"
                    }
                    UploadStatus.CANCELED.value,
                    UploadStatus.ERROR_CONFIRMED.value -> {
                        findNavController().popBackStack(R.id.menu_feed, false)
                    }
                }
            }
        }
    }
}