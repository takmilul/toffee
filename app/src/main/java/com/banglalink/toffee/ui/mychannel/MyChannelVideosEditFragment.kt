package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMyChannelVideosEditBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
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
    private lateinit var progressDialog: VelBoxProgressDialog
    private lateinit var binding: FragmentMyChannelVideosEditBinding
    private val viewModel: MyChannelVideosEditViewModel by viewModels()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    companion object {
        const val CHANNEL_INFO = "channelInfo"
        
        fun newInstance(channelInfo: ChannelInfo): MyChannelVideosEditFragment {
            return MyChannelVideosEditFragment().apply { 
                arguments = Bundle().apply {
                    putParcelable(CHANNEL_INFO, channelInfo)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyChannelVideosEditBinding.inflate(inflater)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelInfo = MyChannelVideosEditFragmentArgs.fromBundle(requireArguments()).channelInfo
        binding.container.setOnClickListener { UtilsKt.hideSoftKeyboard(requireActivity()) }
        progressDialog.show()
        setupSubcategorySpinner()
        setupTagView()
        observeThumbnailChange()
        observeCategory()
        binding.cancelButton.setOnClickListener {
            UtilsKt.hideSoftKeyboard(requireActivity())
            findNavController().popBackStack()
        }
        binding.submitButton.setOnClickListener {
            UtilsKt.hideSoftKeyboard(requireActivity())
            updateVideoInfo()
        }
        binding.thumbEditButton.setOnClickListener {
            if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment &&findNavController().currentDestination?.id == R.id.myChannelVideosEditFragment) {
                val action =
                    MyChannelVideosEditFragmentDirections.actionMyChannelVideosEditFragmentToThumbnailSelectionMethodFragment(
                        "Set Video Cover Photo",
                        false
                    )
                findNavController().navigate(action)
            }
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
            if(categoryList.isNotEmpty()){
                val selectedCategory = categoryList.find { it.id.toInt() == channelInfo?.categoryId }
                val categoryIndex = categoryList.indexOf(selectedCategory).takeIf { it > 0 } ?: 0
                viewModel.categoryPosition.value = categoryIndex
                viewModel.ageGroupPosition.value = channelInfo?.age_restriction?.toInt()?:0
            }
            progressDialog.dismiss()
        }
    }

    private fun setupSubcategorySpinner() {
        val mSubCategoryAdapter = ToffeeSpinnerAdapter<SubCategory>(requireContext(), "Select Sub Category")
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

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
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
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)?.observe(viewLifecycleOwner, {
            it?.let {
                binding.bannerImageView.load(it) {
                    memoryCachePolicy(CachePolicy.DISABLED)
                    diskCachePolicy(CachePolicy.ENABLED)
                    crossfade(false)
                }
            }
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
         if(description.isBlank())
        {
            progressDialog.dismiss()
            binding.uploadDescription.setBackgroundResource(R.drawable.error_multiline_input_text_bg)
            binding.errorDescriptionTv.show()
        }
        else{
             progressDialog.dismiss()
             binding.uploadDescription.setBackgroundResource(R.drawable.multiline_input_text_bg)
             binding.errorDescriptionTv.hide()
        }

        if (title.isNotBlank() and description.isNotBlank()) {
            val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
            val categoryId = viewModel.categories.value?.getOrNull(viewModel.categoryPosition.value ?: 0)?.id ?: 1
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
                }
                is Resource.Failure -> {
                    progressDialog.dismiss()
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
}
