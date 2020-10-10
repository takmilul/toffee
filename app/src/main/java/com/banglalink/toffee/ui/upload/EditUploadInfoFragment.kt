package com.banglalink.toffee.ui.upload

//import com.bumptech.glide.Glide
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.UtilsKt
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.UploadService
import javax.inject.Inject

@AndroidEntryPoint
class EditUploadInfoFragment: BaseFragment() {
    private lateinit var binding: FragmentEditUploadInfoBinding

    private lateinit var uploadId: String

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_upload_info, container, false)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            uploadId = mPref.uploadId ?: return@launch
            val uploadInfo = uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId)) ?: return@launch
            uploadInfo.fileUri.let {
                // TODO: Fetch the thumbnail
//                val bmp = withContext(Dispatchers.IO) {
//                    val mmr = MediaMetadataRetriever()
//                    mmr.setDataSource(context, Uri.parse(it))
//                    mmr.frameAtTime
//                }
//                binding.videoThumb.setImageBitmap(bmp)
            }

            binding.cancelButton.setOnClickListener {
                lifecycleScope.launch {
                    UploadService.stopAllUploads()
                    mPref.uploadId = null
                    findNavController().popBackStack()
                }
            }

            binding.submitButton.setOnClickListener {
                VelBoxAlertDialogBuilder(requireContext(),
                    text="Your video has been submitted!\n" +
                            "You will be notified once its published.",
                    icon = R.drawable.subscription_success
                ).create().show()
//                mPref.uploadId = null
            }

            binding.thumbEditButton.setOnClickListener {
                findNavController().navigate(R.id.action_editUploadInfoFragment_to_thumbnailSelectionMethodFragment)
            }

            setupUI(uploadInfo)
            observeUpload()
            observeThumbnailChange()

            binding.uploadTitle.requestFocus()
        }
    }

    private fun setupUI(uploadInfo: UploadInfo) {
        viewModel.initUploadInfo(uploadInfo)

        setupTagView(uploadInfo.tags)
    }

    private fun observeThumbnailChange() {
        findNavController().
            currentBackStackEntry?.
            savedStateHandle?.
            getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)?.
            observe(viewLifecycleOwner, Observer {
                it?.let {
                    binding.videoThumb.load(it)
                }
            })
    }

    private fun setupTagView(tags: String?) {
        with(binding.uploadTags.editText) {
            gravity = Gravity.START or Gravity.TOP
            setLines(2)
            maxLines = 2
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val chippRecycler = binding.uploadTags.findViewById<RecyclerView>(R.id.chips_recycler)
        chippRecycler.setPadding(0)

        binding.uploadTags.addChipsListener(object: ChipsInput.ChipsListener {
            override fun onChipAdded(chip: ChipInterface?, newSize: Int) {

            }

            override fun onChipRemoved(chip: ChipInterface?, newSize: Int) {

            }

            override fun onTextChanged(text: CharSequence?) {
                Log.e("CHIPS", text.toString())
                if(text?.endsWith(" ") == true) {
                    val chipText = text.toString().trim().capitalize()
                    binding.uploadTags.addChip(chipText, null)
                }
            }
        })

        binding.uploadTags.editText.setText(tags)
        tags?.split(" ")?.forEach {
            Log.e("CHIPS", it)
//            binding.uploadTags.addChip(it, null)
        }
    }

    private fun observeUpload() {
        lifecycleScope.launchWhenStarted {
            uploadProgressViewModel.getActiveUploadList().collectLatest {uploadList->
                if(uploadList.isNotEmpty()) {
                    val uploadInfo = uploadList[0]
                    when(uploadInfo.status) {
                        UploadStatus.SUCCESS.value -> {
                            binding.uploadProgressGroup.isVisible = false
                        }
                        UploadStatus.ADDED.value, UploadStatus.STARTED.value -> {
                            binding.uploadProgressGroup.isVisible = true
                            viewModel.updateProgress(uploadInfo.completedPercent, uploadInfo.fileSize)
                        }
                    }
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroy() {
        appScope.launch {
            uploadRepo.getUploadById(UtilsKt.stringToUploadId(uploadId))?.apply {
                title = binding.uploadTitle.text.toString()
                description = binding.uploadDescription.text.toString()

                tags = binding.uploadTags.selectedChipList.joinToString(" ") { it.label }
                Log.e("TAG", "TAG - $tags, === ${binding.uploadTags.selectedChipList}")

                ageGroupIndex = binding.ageGroupSpinner.selectedItemPosition
                ageGroup = binding.ageGroupSpinner.selectedItem.toString()

                categoryIndex = binding.categorySpinner.selectedItemPosition
                category = binding.categorySpinner.selectedItem.toString()

                submitToChallengeIndex = binding.challengeSelectionSpinner.selectedItemPosition
                submitToChallenge = binding.challengeSelectionSpinner.selectedItem.toString()

                Log.e("UploadInfo", "$this")
            }?.let {
                uploadRepo.updateUploadInfo(it)
            }
        }
        super.onDestroy()
    }
}
