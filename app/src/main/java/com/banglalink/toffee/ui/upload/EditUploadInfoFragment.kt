package com.banglalink.toffee.ui.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeSpinnerAdapter
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Utils
import com.github.florent37.runtimepermission.kotlin.NoActivityException
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditUploadInfoFragment : BaseFragment() {
    
    private lateinit var uploadFileUri: String
    private var descTextWatcher: TextWatcher? = null
    private var titleTextWatcher: TextWatcher? = null
    private var progressDialog: ToffeeProgressDialog? = null
    private var _binding: FragmentEditUploadInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditUploadInfoViewModel by viewModels {
        EditUploadInfoViewModel.provideFactory(editUploadViewModelFactory, uploadFileUri)
    }
    @Inject lateinit var editUploadViewModelFactory: EditUploadInfoViewModel.AssistedFactory
    
    companion object {
        const val UPLOAD_FILE_URI = "UPLOAD_FILE_URI"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadFileUri = requireArguments().getString(UPLOAD_FILE_URI, "")
        
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    ToffeeAlertDialogBuilder(requireContext()).apply {
                        setTitle(getString(R.string.cancel_upload_title))
                        setText(getString(R.string.cancel_upload_msg))
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
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditUploadInfoBinding.inflate(inflater, container, false)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }
    
    override fun onDestroyView() {
        viewModel.tags.value = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
        binding.unbind()
        binding.subCategorySpinner.adapter = null
        binding.categorySpinner.adapter = null
        binding.ageGroupSpinner.adapter = null
        binding.uploadTitle.removeTextChangedListener(titleTextWatcher)
        binding.uploadDescription.removeTextChangedListener(descTextWatcher)
        titleTextWatcher = null
        descTextWatcher = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.container.setOnClickListener {
            Utils.hideSoftKeyboard(requireActivity())
        }
        binding.cancelButton.setOnClickListener {
            ToffeeAlertDialogBuilder(requireContext()).apply {
                setTitle(getString(R.string.cancel_upload_title))
                setText(getString(R.string.cancel_upload_msg))
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
            findNavController().navigate(
                R.id.thumbnailSelectionMethodFragment, bundleOf(
                    ThumbnailSelectionMethodFragment.TITLE to getString(R.string.set_video_cover_photo),
                    ThumbnailSelectionMethodFragment.IS_PROFILE_IMAGE to false
                )
            )
        }
        binding.copyrightCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.errorCopyrightTv.hide()
                binding.copyrightCheckboxLayout.setBackgroundResource(R.drawable.multiline_input_text_bg)
            } else {
                binding.errorCopyrightTv.show()
                binding.copyrightCheckboxLayout.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
            }
        }
        binding.copyrightLayout.uploadFileButton.safeClick({ checkFileSystemPermission() })
        binding.copyrightLayout.closeIv.safeClick({
            it.hide()
            viewModel.copyrightDocUri = null
            viewModel.copyrightFileName.value = null
            binding.copyrightLayout.uploadFileButton.show()
        })
        setupCategorySpinner()
        setupSubcategorySpinner()
        setupAgeSpinner()
        setupTagView()
        observeStatus()
//        observeUpload()
        observeProgressDialog()
        observeThumbnailLoad()
        observeThumbnailChange()
        observeVideoDuration()
        observeExitFragment()
        
        binding.uploadTags.clearFocus()
        binding.uploadTitle.requestFocus()
        
        titleWatcher()
        descriptionDesWatcher()
        binding.uploadTitleCountTv.text = getString(R.string.video_title_limit, 0)
        binding.uploadDesCountTv.text = getString(R.string.video_description_limit, 0)
    }
    
    private fun titleWatcher() {
        titleTextWatcher = binding.uploadTitle.doAfterTextChanged { s: Editable? ->
            binding.uploadTitleCountTv.text = getString(R.string.video_title_limit, s?.length ?: 0)
            binding.uploadTitle.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorTitleTv.hide()
        }
    }
    
    private fun descriptionDesWatcher() {
        descTextWatcher = binding.uploadDescription.doAfterTextChanged { s: Editable? ->
            binding.uploadDesCountTv.text = getString(R.string.video_description_limit, s?.length ?: 0)
            binding.uploadDescription.setBackgroundResource(R.drawable.multiline_input_text_bg)
            binding.errorDescriptionTv.hide()
        }
    }
    
    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.type = "*/*"
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file")
                    fileResultLauncher.launch(chooseFile)
                }
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                requireContext().showToast(getString(R.string.grant_storage_permission))
            } catch (e: NoActivityException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            } catch (e: ActivityNotFoundException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb(e.message ?: "")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
        }
    }
    
    private val fileResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
            ToffeeAnalytics.logEvent(ToffeeEvents.UGC_CHANNEL_FORM_SUBMIT)
            val uri = it.data!!.data!!
            checkFileValidity(uri)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }
    
    private fun checkFileValidity(uri: Uri) {
        lifecycleScope.launch {
            val fileName = Utils.fileNameFromContentUri(requireContext(), uri)
            val fileSize = Utils.fileSizeFromContentUri(requireContext(), uri)
            when (fileName.substringAfterLast(".")) {
                "txt", "pdf", "doc", "docx", "rtf", "png", "jpg" -> {
                    if (fileSize <= 5 * 1024 * 1024) {
                        lifecycleScope.launch { viewModel.loadCopyrightFileName(uri) }
                        binding.copyrightLayout.closeIv.show()
                        binding.copyrightLayout.uploadFileButton.hide()
                    } else {
                        ToffeeAlertDialogBuilder(requireContext()).apply {
                            setTitle(getString(R.string.file_size_title))
                            setText(getString(R.string.file_size_msg))
                            setPositiveButtonListener(getString(R.string.btn_got_it)) {
                                it?.dismiss()
                            }
                        }.create().show()
                    }
                }
                else -> {
                    ToffeeAlertDialogBuilder(requireContext()).apply {
                        setTitle(getString(R.string.file_format_title))
                        setText(getString(R.string.file_format_msg))
                        setPositiveButtonListener(getString(R.string.btn_got_it)) {
                            it?.dismiss()
                        }
                    }.create().show()
                }
            }
        }
    }
    
    private fun observeExitFragment() {
        observe(viewModel.exitFragment) {
            if (it) {
                requireContext().showToast(getString(R.string.unable_to_load_data))
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
            binding.duration.text = Utils.getDurationLongToString(it)
        }
    }
    
    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner) {
                viewModel.saveThumbnail(it)
            }
    }
    
    private fun setupAgeSpinner() {
        val mAgeAdapter = ToffeeSpinnerAdapter<String>(requireContext(), getString(R.string.select_age))
        binding.ageGroupSpinner.adapter = mAgeAdapter
        binding.ageGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0 && viewModel.ageGroupPosition.value != position) {
                    viewModel.ageGroupPosition.value = position
                } else {
                    binding.ageGroupSpinner.setSelection(viewModel.ageGroupPosition.value ?: 1)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        observe(viewModel.ageGroup) {
            mAgeAdapter.setData(it)
            viewModel.ageGroupPosition.value = 1
        }
        
        observe(viewModel.ageGroupPosition) {
            mAgeAdapter.selectedItemPosition = it
            binding.ageGroupSpinner.setSelection(it)
        }
    }
    
    private fun setupCategorySpinner() {
        val mCategoryAdapter = ToffeeSpinnerAdapter<Category>(requireContext(), getString(R.string.select_category))
        binding.categorySpinner.adapter = mCategoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0 && viewModel.categoryPosition.value != position) {
                    viewModel.categoryPosition.value = position
                    viewModel.categoryIndexChanged(position - 1)
                } else {
                    val previousValue = viewModel.categoryPosition.value ?: 1
                    binding.categorySpinner.setSelection(previousValue)
                    viewModel.categoryIndexChanged(previousValue - 1)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        observe(viewModel.categories) {
            mCategoryAdapter.setData(it)
            viewModel.categoryPosition.value = 1
        }
        
        observe(viewModel.categoryPosition) {
            mCategoryAdapter.selectedItemPosition = it
            binding.categorySpinner.setSelection(it)
        }
    }
    
    private fun setupSubcategorySpinner() {
        val mSubCategoryAdapter = ToffeeSpinnerAdapter<SubCategory>(requireContext(), getString(R.string.select_sub_category))
        binding.subCategorySpinner.adapter = mSubCategoryAdapter
        binding.subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0 && viewModel.subCategoryPosition.value != position) {
                    viewModel.subCategoryPosition.value = position
                } else {
                    binding.subCategorySpinner.setSelection(viewModel.subCategoryPosition.value ?: 1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        val chipRecycler = binding.uploadTags.findViewById<RecyclerView>(com.pchmn.materialchips.R.id.chips_recycler)
        chipRecycler.setPadding(0)
        
        binding.uploadTags.addChipsListener(object : ChipsInput.ChipsListener {
            override fun onChipAdded(chip: ChipInterface?, newSize: Int) {}
            override fun onChipRemoved(chip: ChipInterface?, newSize: Int) {}
            override fun onTextChanged(text: CharSequence?) {
                if (text?.endsWith(" ") == true) {
                    text.let {
                        val chipText = it.toString().trim().capitalize(Locale.US)
                        if (chipText.isNotEmpty()) {
                            binding.uploadTags.addChip(chipText, null)
                        }
                    }
                }
            }
        })
    }
    
    private fun observeProgressDialog() {
        observe(viewModel.progressDialog) {
            when (it) {
                true -> {
                    if (progressDialog != null) {
                        progressDialog?.dismiss()
                        progressDialog = null
                    }
                    progressDialog = ToffeeProgressDialog(requireContext())
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
            when (it) {
                is Resource.Success -> {
                    lifecycleScope.launch {
                        progressDialog?.dismiss()
                        progressDialog = null
                        findNavController().navigate(
                            R.id.upload_minimize, bundleOf(
                                MinimizeUploadFragment.UPLOAD_ID to it.data.first,
                                MinimizeUploadFragment.CONTENT_ID to it.data.second
                            )
                        )
                    }
                }
                else -> {
                    context?.showToast(getString(R.string.try_again_msg))
                }
            }
        }
    }
    
    private fun submitVideo() {
        val title = binding.uploadTitle.text.toString().trim()
        val description = binding.uploadDescription.text.toString().trim()
        val orientation = viewModel.orientationData.value ?: 1
        
        if (title.isBlank()) {
            binding.uploadTitle.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorTitleTv.show()
        } else {
            binding.uploadTitle.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorTitleTv.hide()
        }
        if (description.isBlank()) {
            binding.uploadDescription.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
            binding.errorDescriptionTv.show()
        } else {
            binding.uploadDescription.setBackgroundResource(R.drawable.multiline_input_text_bg)
            binding.errorDescriptionTv.hide()
        }
        val isAgreed = binding.copyrightCheckbox.isChecked
        if (isAgreed) {
            binding.errorCopyrightTv.hide()
            binding.copyrightCheckboxLayout.setBackgroundResource(R.drawable.multiline_input_text_bg)
        } else {
            binding.errorCopyrightTv.show()
            binding.copyrightCheckboxLayout.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
        }
        if (viewModel.thumbnailData.value.isNullOrBlank()) {
            context?.showToast(getString(R.string.thumbnail_missing_msg))
            return
        }
        if (title.isNotBlank() and description.isNotBlank() and isAgreed) {
            lifecycleScope.launch {
                val categoryObj = binding.categorySpinner.selectedItem
                val categoryId = if (categoryObj is Category) {
                    categoryObj.id
                } else -1
                
                val subCategoryObj = binding.subCategorySpinner.selectedItem
                val subcategoryId = if (subCategoryObj is SubCategory) {
                    subCategoryObj.id
                } else -1
                
                val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label.replace("#", "") }
                val isUploadCopyrightFile = !viewModel.copyrightDocUri.isNullOrBlank() and !viewModel.copyrightFileName.value.isNullOrBlank()
                
                viewModel.saveUploadInfo(
                    tags,
                    categoryId,
                    subcategoryId,
                    viewModel.durationData.value ?: 0L,
                    orientation,
                    isUploadCopyrightFile
                )
            }
        }
    }
}
