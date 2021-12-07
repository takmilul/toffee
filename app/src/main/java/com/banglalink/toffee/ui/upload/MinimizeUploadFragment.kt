package com.banglalink.toffee.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.FragmentMinimizeUploadBinding
import com.banglalink.toffee.enums.UploadStatus
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.gotev.uploadservice.UploadService
import javax.inject.Inject

@AndroidEntryPoint
class MinimizeUploadFragment: BaseFragment() {
    private lateinit var uploadId: String
    private var contentId: Long = -1
    private var uploadIdLong = -1L

    private var _binding: FragmentMinimizeUploadBinding ? = null
    private val binding get() = _binding!!

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
    ): View {
        _binding = FragmentMinimizeUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButton.setOnClickListener {
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setTitle("Cancel Uploading")
                setText("Are you sure that you want to\n" +
                        "cancel uploading video?")
                setPositiveButtonListener("NO") {
                    it?.dismiss()
                }
                setNegativeButtonListener("YES") {
                    UploadService.stopAllUploads()
                    parentFragment?.findNavController()?.popBackStack(R.id.menu_feed, false)
                    it?.dismiss()
                }
            }.create().show()
        }

        binding.minimizeButton.setOnClickListener {
            findNavController().popBackStack(R.id.menu_feed, false)
        }

        observeUpload()
    }

    private fun observeUpload() {
        viewLifecycleOwner.lifecycleScope.launch {
            uploadRepo.getUploadFlowById(uploadIdLong).collectLatest { uploadInfo ->
                when(uploadInfo?.status) {
                    UploadStatus.SUCCESS.value,
                    UploadStatus.SUBMITTED.value,
                    -> {
                        binding.uploadPercent.text = "100%"
                        binding.progressBar.progress = 100
                        binding.uploadSize.text = "of ${Utils.readableFileSize(uploadInfo.fileSize)}"
                        findNavController().popBackStack(R.id.menu_feed, false)
                    }
                    UploadStatus.ADDED.value,
                    UploadStatus.STARTED.value -> {
                        binding.uploadPercent.text = "${uploadInfo.completedPercent}%"
                        binding.progressBar.progress = uploadInfo.completedPercent
                        binding.uploadSize.text = "of ${Utils.readableFileSize(uploadInfo.fileSize)}"
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