package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentMyChannelVideosEditBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyChannelVideosEditFragment : BaseFragment() {

    private var channelInfo: ChannelInfo? = null
    private lateinit var binding: FragmentMyChannelVideosEditBinding
    private val viewModel: MyChannelVideosEditViewModel by viewModels()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    companion object {
        const val CHANNEL_INFO = "channelInfo"
        fun newInstance(channelInfo: ChannelInfo): MyChannelVideosEditFragment {
            val instance = MyChannelVideosEditFragment()
            val bundle = Bundle()
            bundle.putParcelable(CHANNEL_INFO, channelInfo)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_channel_videos_edit, container, false)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelInfo = MyChannelVideosEditFragmentArgs.fromBundle(requireArguments()).channelInfo
        
        setupTagView()
        observeThumbnailChange()
        observeCategory()
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
        binding.submitButton.setOnClickListener { submitVideo() }
        binding.thumbEditButton.setOnClickListener { 
            val action = MyChannelVideosEditFragmentDirections.actionMyChannelVideosEditFragmentToThumbnailSelectionMethodFragment("Set Video Cover Photo")
            findNavController().navigate(action) 
        }
        
        channelInfo?.let { info ->
            info.video_tags?.split(" | ")?.filter { it.isNotBlank() }?.forEach {
                binding.uploadTags.addChip(it, null)
            }
            binding.uploadTitle.requestFocus()
        }
    }

    private fun observeCategory() {
        observe(viewModel.categories){ categoryList ->
            if(categoryList.isNotEmpty()){
                val selectedCategory = categoryList.find { it.id.toInt() == channelInfo?.categoryId }
                val categoryIndex = categoryList.indexOf(selectedCategory).takeIf { it > 0 } ?: 0
                val subCategories = categoryList[categoryIndex].subcategories
                val selectedSubCategory = subCategories.find { it.id.toInt() == channelInfo?.subCategoryId }
                val subCategoryIndex = subCategories.indexOf(selectedSubCategory).takeIf { it > 0 } ?: 0

                viewModel.title.value = channelInfo?.program_name
                viewModel.description.value = channelInfo?.getDescriptionDecoded().toString()
                viewModel.tags.value = channelInfo?.video_tags
                viewModel.thumbnailUrl.value = channelInfo?.landscape_ratio_1280_720
                viewModel.subCategories.value = subCategories

                viewModel.categoryPosition.value = categoryIndex
                viewModel.subCategoryPosition.value = subCategoryIndex
                viewModel.ageGroupPosition.value = channelInfo?.age_restriction?.toInt()?:0
            }
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

    private fun submitVideo() {
        observeEditResponse()
        
        val title = binding.uploadTitle.text.toString().trim()
        val description = binding.uploadDescription.text.toString().trim()
        if (title.isBlank() || description.isBlank()) {
            context?.showToast("Missing required field", Toast.LENGTH_SHORT)
            return
        }
        val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
        val categoryId = viewModel.categories.value?.getOrNull(viewModel.categoryPosition.value ?: 0)?.id ?: 0
        val subCategoryId = viewModel.subCategories.value?.getOrNull(viewModel.subCategoryPosition.value ?: 0)?.id ?: 0
        viewModel.saveUploadInfo(channelInfo?.id?.toInt()?:0, "",tags, categoryId, subCategoryId.toInt())
    }

    private fun observeEditResponse() {
        observe(viewModel.editResponse){
            when(it){
                is Resource.Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                    videosReloadViewModel.reloadVideos.value = true
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
