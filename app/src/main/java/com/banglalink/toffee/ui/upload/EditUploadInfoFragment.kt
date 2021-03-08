package com.banglalink.toffee.ui.upload

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeSpinnerAdapter
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditUploadInfoFragment: BaseFragment() {
    
    private lateinit var binding: FragmentEditUploadInfoBinding
    private var progressDialog: VelBoxProgressDialog? = null
    @Inject lateinit var editUploadViewModelFactory: EditUploadInfoViewModel.AssistedFactory
    @Inject lateinit var uploadRepo: UploadInfoRepository
    @AppCoroutineScope @Inject lateinit var appScope: CoroutineScope
    private lateinit var uploadFileUri: String
    private val viewModel: EditUploadInfoViewModel by viewModels {
        EditUploadInfoViewModel.provideFactory(editUploadViewModelFactory, uploadFileUri)
    }

    companion object {
        const val UPLOAD_FILE_URI = "UPLOAD_FILE_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadFileUri = requireArguments().getString(UPLOAD_FILE_URI, "")

        requireActivity().onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(isEnabled) {
                    VelBoxAlertDialogBuilder(requireContext()).apply {
                        setTitle("Cancel Uploading")
                        setText("Are you sure that you want to\n" +
                                "cancel uploading video?")
                        setPositiveButtonListener("NO") {
                            it?.dismiss()
                        }
                        setNegativeButtonListener("YES") {
                            isEnabled = false
                            requireActivity().onBackPressed()
                            it?.dismiss()
                        }
                    }.create().show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

    override fun onDestroyView() {
        viewModel.tags.value = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.container.setOnClickListener {
            UtilsKt.hideSoftKeyboard(requireActivity())
        }

        binding.cancelButton.setOnClickListener {
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setTitle("Cancel Uploading")
                setText("Are you sure that you want to\n" +
                        "cancel uploading video?")
                setPositiveButtonListener("NO") {
                    it?.dismiss()
                }
                setNegativeButtonListener("YES") {
                    findNavController().popBackStack()
                    it?.dismiss()
                }
            }.create().show()
        }

        binding.submitButton.setOnClickListener {
            submitVideo()
        }

        binding.thumbEditButton.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.editUploadInfoFragment) {
                val action =
                    EditUploadInfoFragmentDirections.actionEditUploadInfoFragmentToThumbnailSelectionMethodFragment(
                        "Set Video Cover Photo",
                        false
                    )
                findNavController().navigate(action)
            }
        }

        setupSubcategorySpinner()

        setupTagView()
        observeStatus()
//        observeUpload()
        observeProgressDialog()
        observeThumbnailLoad()
        observeThumbnailChange()
        observeVideoDuration()

        observeExitFragment()

        binding.uploadTitle.requestFocus()
    }

    private fun observeExitFragment() {
        observe(viewModel.exitFragment) {
            if(it) {
                Toast.makeText(requireContext(), "Unable to load data.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun observeThumbnailLoad() {
        observe(viewModel.thumbnailData) {
            it?.let { thumb ->
                binding.videoThumb.loadBase64(thumb)
            }
        }
    }

    private fun observeVideoDuration() {
        observe(viewModel.durationData) {
            binding.duration.text = UtilsKt.getDurationLongToString(it)
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

    private fun setupSubcategorySpinner() {
        val mSubCategoryAdapter = ToffeeSpinnerAdapter<SubCategory>(requireContext(), "Select Sub Category")
        binding.subCategorySpinner.adapter = mSubCategoryAdapter
        binding.subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0 && viewModel.subCategoryPosition.value != position) {
                    viewModel.subCategoryPosition.value = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        observe(viewModel.subCategories) {
            mSubCategoryAdapter.setData(it)
            viewModel.subCategoryPosition.value = 1
        }

        observe(viewModel.subCategoryPosition) {
            mSubCategoryAdapter.selectedItemPosition = it
            binding.subCategorySpinner.setSelection(it)
        }
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
                        if(chipText.isNotEmpty()) {
                            binding.uploadTags.addChip(chipText, null)
                        }
                    }
                }
            }
        })
    }

    private fun observeProgressDialog() {
        observe(viewModel.progressDialog) {
            when(it) {
                true -> {
                    if(progressDialog != null) {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                    progressDialog = VelBoxProgressDialog(requireContext())
                    progressDialog?.show()
                }
                false -> {
                    progressDialog?.dismiss()
                    progressDialog = null
                }
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
//                        getUploadInfo()?.apply {
//                            status = UploadStatus.SUBMITTED.value
//                        }?.also {info ->
//                            uploadRepo.updateUploadInfo(info)
//                        }
                        progressDialog?.dismiss()
                        progressDialog = null
//                        val dialog = VelBoxAlertDialogBuilder(
//                            requireContext(),
//                            text = it.data.message,
//                            icon = R.drawable.subscription_success
//                        ).create()
//                        dialog.setOnDismissListener {
//                            findNavController().popBackStack()
//                        }
//                        dialog.show()
                        findNavController().navigate(R.id.upload_minimize, Bundle().apply {
                            putString(MinimizeUploadFragment.UPLOAD_ID, it.data.first)
                            putLong(MinimizeUploadFragment.CONTENT_ID, it.data.second)
                        })
                    }
                }
                else -> {
                    context?.showToast("Unable to submit the video.")
                }
            }
        }
    }

    private fun submitVideo() {
        val title = binding.uploadTitle.text.toString().trim()
        val description = binding.uploadDescription.text.toString().trim()
        val orientation = viewModel.orientationData.value ?: 1
        
        if(title.isBlank()){
            binding.uploadTitle.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorTitleTv.show()
        }
        else{
            binding.uploadTitle.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorTitleTv.hide()
        }
        if(description.isBlank()){
            binding.uploadDescription.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
            binding.errorDescriptionTv.show()
        }
        else{
            binding.uploadDescription.setBackgroundResource(R.drawable.multiline_input_text_bg)
            binding.errorDescriptionTv.hide()
        }

        if (viewModel.thumbnailData.value.isNullOrBlank()){
            context?.showToast("Missing thumbnail field")
            return
        }

        if (title.isNotBlank() and description.isNotBlank()) {
            lifecycleScope.launch {
                val categoryObj = binding.categorySpinner.selectedItem
                val categoryId = if (categoryObj is Category) {
                    categoryObj.id
                } else -1

                val subCategoryObj = binding.subCategorySpinner.selectedItem
                val subcategoryId = if (subCategoryObj is SubCategory) {
                    subCategoryObj.id
                } else -1

                val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }

                viewModel.saveUploadInfo(
                    tags,
                    categoryId,
                    subcategoryId,
                    viewModel.durationData.value ?: 0L,
                    orientation
                )
            }
        }
    }
}
