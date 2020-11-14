package com.banglalink.toffee.ui.upload

//import com.bumptech.glide.Glide

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.loadBase64
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.gotev.uploadservice.UploadService
import javax.inject.Inject


@AndroidEntryPoint
class EditUploadInfoFragment: BaseFragment() {
    private lateinit var binding: FragmentEditUploadInfoBinding

    private var progressDialog: VelBoxProgressDialog? = null

    @Inject
    lateinit var uploadRepo: UploadInfoRepository

    @AppCoroutineScope
    @Inject
    lateinit var appScope: CoroutineScope

    private val viewModel: EditUploadInfoViewModel by viewModels()
    private val uploadProgressViewModel by activityViewModels<UploadProgressViewModel>()

    companion object {
        fun newInstance(): EditUploadInfoFragment {
            return EditUploadInfoFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_upload_info,
            container,
            false
        )

        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.cancelButton.setOnClickListener {
            lifecycleScope.launch {
                if(UploadService.taskList.isNotEmpty()) {
                    UploadService.stopAllUploads()
                }
                getUploadInfo()?.apply {
                    status = UploadStatus.CANCELED.value
                }?.also {
                    uploadRepo.updateUploadInfo(it)
                }
                mPref.uploadId = null
                findNavController().popBackStack()
            }
        }

        binding.submitButton.setOnClickListener {
            submitVideo()
        }

        binding.thumbEditButton.setOnClickListener {
            val action = EditUploadInfoFragmentDirections.actionEditUploadInfoFragmentToThumbnailSelectionMethodFragment("Set Video Cover Photo")
            findNavController().navigate(action)
        }

        setupTagView()
        observeStatus()
        observeUpload()
        observeProgressDialog()
        observeThumbnailLoad()
        observeThumbnailChange()

        binding.uploadTitle.requestFocus()
    }

    private fun observeThumbnailLoad() {
        observe(viewModel.thumbnailData) {
            it?.let { thumb ->
                binding.videoThumb.loadBase64(thumb)
            }
        }
    }

    private fun observeThumbnailChange() {
        findNavController().
            currentBackStackEntry?.
            savedStateHandle?.
            getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)?.
            observe(viewLifecycleOwner, {
                viewModel.saveThumbnail(it)
            })
    }

    private fun setupTagView() {
        with(binding.uploadTags.editText) {
            gravity = Gravity.START or Gravity.TOP
            setLines(2)
            maxLines = 2
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val chippRecycler = binding.uploadTags.findViewById<RecyclerView>(R.id.chips_recycler)
        chippRecycler.setPadding(0)

        binding.uploadTags.addChipsListener(object : ChipsInput.ChipsListener {
            override fun onChipAdded(chip: ChipInterface?, newSize: Int) {

            }

            override fun onChipRemoved(chip: ChipInterface?, newSize: Int) {

            }

            override fun onTextChanged(text: CharSequence?) {
                if (text?.endsWith(" ") == true) {
                    text.let {
                        val chipText = it.toString().trim().capitalize()
                        binding.uploadTags.addChip(chipText, null)
                    }
                }
            }
        })
    }

    private fun observeUpload() {
        lifecycleScope.launchWhenStarted {
            uploadProgressViewModel.getActiveUploadList().collectLatest { uploadList->
                if(uploadList.isNotEmpty()) {
                    val uploadInfo = uploadList[0]
                    when(uploadInfo.status) {
                        UploadStatus.SUCCESS.value -> {
                            binding.uploadProgressGroup.isVisible = false
                            binding.submitButton.isEnabled = true
                        }
                        UploadStatus.ADDED.value, UploadStatus.STARTED.value -> {
                            binding.uploadProgressGroup.isVisible = true
                            viewModel.updateProgress(
                                uploadInfo.completedPercent,
                                uploadInfo.fileSize
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if(mPref.uploadId != null) {
            appScope.launch {
                saveInfo()
            }
        }
        super.onDestroy()
    }

    private fun observeProgressDialog() {
        observe(viewModel.progressDialog) {
            when(it) {
                true -> {
                    progressDialog = VelBoxProgressDialog(requireContext())
                    progressDialog?.show()
                }
                false -> progressDialog?.dismiss()
            }
        }
    }

    private fun observeStatus() {
        observe(viewModel.tags) { tags ->
            tags?.split(" | ")?.filter { it.isNotBlank() }?.forEach {
                binding.uploadTags.addChip(it, null)
            }
        }

        observe(viewModel.submitButtonStatus) {
            binding.submitButton.isEnabled = it
        }

        observe(viewModel.resultLiveData) {
            when(it){
                is Resource.Success -> {
                    lifecycleScope.launch {
                        getUploadInfo()?.apply {
                            status = UploadStatus.SUBMITTED.value
                        }?.also {info ->
                            uploadRepo.updateUploadInfo(info)
                        }
                        mPref.uploadId = null
                        progressDialog?.dismiss()
                        val dialog = VelBoxAlertDialogBuilder(
                            requireContext(),
                            text = it.data.message,
                            icon = R.drawable.subscription_success
                        ).create()
                        dialog.setOnDismissListener {
                            findNavController().popBackStack()
                        }
                        dialog.show()
                    }
                }
                else -> {
                    context?.showToast("Unable to submit the video.")
                }
            }
        }
    }

    private suspend fun getUploadInfo(): UploadInfo? {
        return mPref.uploadId?.let {
            uploadRepo.getUploadById(UtilsKt.stringToUploadId(it))
        }
    }

    private suspend fun saveInfo(): UploadInfo? {
        return getUploadInfo()?.apply {
            title = binding.uploadTitle.text.toString()
            description = binding.uploadDescription.text.toString()

            tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
            Log.e("TAG", "TAG - $tags, === ${binding.uploadTags.selectedChipList}")

            ageGroupIndex = binding.ageGroupSpinner.selectedItemPosition
            ageGroup = binding.ageGroupSpinner.selectedItem.toString()

            categoryIndex = binding.categorySpinner.selectedItemPosition
            category = binding.categorySpinner.selectedItem.toString()

//                submitToChallengeIndex = binding.challengeSelectionSpinner.selectedItemPosition
//                submitToChallenge = binding.challengeSelectionSpinner.selectedItem.toString()

            Log.e("UploadInfo", "$this")
        }?.also {
            uploadRepo.updateUploadInfo(it)
        }
    }

    private fun submitVideo() {
        val title = binding.uploadTitle.text.toString()
        val description = binding.uploadDescription.text.toString()
        if(title.isBlank() || description.isBlank()) {
            context?.showToast("Missing required field", Toast.LENGTH_SHORT)
            return
        }
        lifecycleScope.launch {
            val uploadInfo = saveInfo()
            val categoryObj = binding.categorySpinner.selectedItem
            val categoryId = if(categoryObj is UgcCategory) {
                categoryObj.id
            } else -1
            uploadInfo?.let {
                viewModel.saveUploadInfo(it.fileName, it.tags, categoryId)
            }
        }
    }
}
