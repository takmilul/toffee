package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.databinding.FragmentMyChannelVideosEditBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SubCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.ToffeeSpinnerAdapter
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyChannelVideosEditFragment : BaseFragment() {

    private var channelInfo: ChannelInfo? = null
    private var descWatcher: TextWatcher? = null
    private var titleTextWatcher: TextWatcher? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelVideosEditBinding ? = null
    private val binding get() = _binding!!
    private val viewModel: MyChannelVideosEditViewModel by viewModels()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    companion object {
        const val CHANNEL_INFO = "channelInfo"
        
        fun newInstance(channelInfo: ChannelInfo): MyChannelVideosEditFragment {
            return MyChannelVideosEditFragment().apply { 
                arguments = bundleOf(CHANNEL_INFO to channelInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelVideosEditBinding.inflate(inflater)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        binding.ageGroupSpinner.adapter = null
        binding.categorySpinner.adapter = null
        binding.subCategorySpinner.adapter = null
        binding.uploadTitle.removeTextChangedListener(titleTextWatcher)
        binding.uploadDescription.removeTextChangedListener(descWatcher)
        titleTextWatcher = null
        descWatcher = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelInfo = MyChannelVideosEditFragmentArgs.fromBundle(requireArguments()).channelInfo
        binding.container.setOnClickListener { UtilsKt.hideSoftKeyboard(requireActivity()) }
        progressDialog.show()
        setCategorySpinner()
        setupSubcategorySpinner()
        setupAgeSpinner()
        setupTagView()
        observeThumbnailChange()
        observeCategory()
        titleWatcher()
        descriptionDesWatcher()
        
        with(binding) {
            cancelButton.setOnClickListener {
                UtilsKt.hideSoftKeyboard(requireActivity())
                findNavController().popBackStack()
            }
            submitButton.setOnClickListener {
                UtilsKt.hideSoftKeyboard(requireActivity())
                updateVideoInfo()
            }
            thumbEditButton.setOnClickListener {
                findNavController().navigate(R.id.thumbnailSelectionMethodFragment, bundleOf(
                    ThumbnailSelectionMethodFragment.TITLE to getString(R.string.set_video_cover_photo),
                    ThumbnailSelectionMethodFragment.IS_PROFILE_IMAGE to false
                ))
            }
            
            uploadTitleCountTv.text = getString(R.string.video_title_limit, 0)
            uploadDesCountTv.text = getString(R.string.video_description_limit, 0)
        }
        
        channelInfo?.let { info ->
            info.video_tags?.split(" | ")?.filter { it.isNotBlank() }?.forEach {
                binding.uploadTags.addChip(it, null)
            }
            viewModel.title.value = channelInfo?.program_name
            viewModel.description.value = channelInfo?.getDescriptionDecoded().toString()
            viewModel.tags.value = channelInfo?.video_tags
            viewModel.thumbnailUrl.value = channelInfo?.landscape_ratio_1280_720
            binding.uploadTags.clearFocus()
            binding.uploadTitle.requestFocus()
        }
    }

    private fun observeCategory() {
        observe(viewModel.categories){ categoryList ->
            progressDialog.dismiss()
            if(categoryList.isNotEmpty()){
                val selectedCategory = categoryList.find { it.id.toInt() == channelInfo?.categoryId }
                val categoryIndex = categoryList.indexOf(selectedCategory).takeIf { it > 0 } ?: 0
                viewModel.categoryPosition.value = categoryIndex+1
                viewModel.ageGroupPosition.value = channelInfo?.age_restriction?.toInt()?:0
            }
        }
        observe(viewModel.exitFragment) {
            requireContext().showToast(getString(R.string.unable_to_load_data))
            findNavController().popBackStack()
        }
    }

    private fun setupAgeSpinner(){
        val mAgeAdapter = ToffeeSpinnerAdapter<String>(requireContext(), getString(R.string.select_age))
        binding.ageGroupSpinner.adapter = mAgeAdapter
        binding.ageGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0 && viewModel.ageGroupPosition.value != position) {
                    viewModel.ageGroupPosition.value = position
                } else {
                    binding.ageGroupSpinner.setSelection(viewModel.ageGroupPosition.value ?: 1)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
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
    
    private fun titleWatcher() {
        titleTextWatcher = binding.uploadTitle.doAfterTextChanged { s -> binding.uploadTitleCountTv.text = getString(R.string.video_title_limit, s?.length ?: 0) }
    }

    private fun descriptionDesWatcher() {
        descWatcher = binding.uploadDescription.doAfterTextChanged { s -> binding.uploadDesCountTv.text = getString(R.string.video_description_limit, s?.length ?: 0) }
    }
    
    private fun setCategorySpinner() {
        val mCategoryAdapter = ToffeeSpinnerAdapter<Category>(requireContext(), getString(R.string.select_category))
        binding.categorySpinner.adapter = mCategoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0 && viewModel.categoryPosition.value != position) {
                    viewModel.categoryPosition.value = position
                    viewModel.categoryIndexChanged(position-1)
                }
                else {
                    val previousValue=viewModel.categoryPosition.value ?: 1
                    binding.categorySpinner.setSelection(previousValue)
                    viewModel.categoryIndexChanged(previousValue-1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        observe(viewModel.categories) { categories ->
            mCategoryAdapter.setData(categories)
            viewModel.subCategoryPosition.value = (categories.indexOf(categories.find { it.id.toInt() == channelInfo?.categoryId }).takeIf { it > 0 } ?: 0)+ 1
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
                if(position != 0 && viewModel.subCategoryPosition.value != position) {
                    viewModel.subCategoryPosition.value = position
                }
                else {
                    binding.subCategorySpinner.setSelection(viewModel.subCategoryPosition.value ?: 1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        observe(viewModel.subCategories) { subCategories ->
            mSubCategoryAdapter.setData(subCategories)
            viewModel.subCategoryPosition.value = (subCategories.indexOf(subCategories.find { it.id.toInt() == channelInfo?.subCategoryId }).takeIf { it > 0 }
                ?: 0) + 1
        }

        observe(viewModel.subCategoryPosition) {
            mSubCategoryAdapter.selectedItemPosition = it
            binding.subCategorySpinner.setSelection(it)
        }
    }

    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)?.observe(viewLifecycleOwner) {
            it?.let {
                binding.bannerImageView.load(it) {
                    setImageRequestParams()
                }
            }
            viewModel.saveThumbnail(it)
        }
    }

    private fun setupTagView() {
        val chipRecycler = binding.uploadTags.findViewById<RecyclerView>(R.id.chips_recycler)
        chipRecycler.setPadding(0)
        
        binding.uploadTags.addChipsListener(object : ChipsInput.ChipsListener {
            override fun onChipAdded(chip: ChipInterface?, newSize: Int) { }
            override fun onChipRemoved(chip: ChipInterface?, newSize: Int) { }
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

    private fun updateVideoInfo() {
        progressDialog.show()
        observeEditResponse()
        
        val title = binding.uploadTitle.text.toString().trim()
        val description = binding.uploadDescription.text.toString().trim()
        if (title.isBlank()) {
            progressDialog.dismiss()
            binding.uploadTitle.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorTitleTv.show()
        }
        else {
            progressDialog.dismiss()
            binding.uploadTitle.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorTitleTv.hide()
        }
        if(description.isBlank()) {
            progressDialog.dismiss()
            binding.uploadDescription.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
            binding.errorDescriptionTv.show()
        }
        else {
             progressDialog.dismiss()
             binding.uploadDescription.setBackgroundResource(R.drawable.multiline_input_text_bg)
             binding.errorDescriptionTv.hide()
        }

        if (title.isNotBlank() and description.isNotBlank()) {
            val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label.replace("#", "") }
            val categoryId = viewModel.categories.value?.getOrNull(viewModel.categoryPosition.value?.minus(1) ?: 0)?.id ?: 0
            val subCategoryId = viewModel.subCategories.value?.getOrNull(viewModel.subCategoryPosition.value?.minus(1) ?: 0)?.id ?: 0
            viewModel.saveUploadInfo(channelInfo?.id?.toInt()?:0, "",tags, categoryId, subCategoryId.toInt())
        }
    }

    private fun observeEditResponse() {
        observe(viewModel.editResponse){
            when(it){
                is Resource.Success -> {
                    progressDialog.dismiss()
                    requireContext().showToast(it.data.message)
                    findNavController().navigateUp()
                    videosReloadViewModel.reloadVideos.value = true
                    videosReloadViewModel.reloadPlaylist.value = true
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.EDIT_CONTENT_UPLOAD,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.EDIT_VIDEO_DETAILS,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
}
