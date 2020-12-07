package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.databinding.FragmentMyChannelVideosEditBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyChannelVideosEditFragment : BaseFragment() {

    private var channelInfo: ChannelInfo? = null
    private lateinit var binding: FragmentMyChannelVideosEditBinding
    private val viewModel: MyChannelVideosEditViewModel by viewModels()

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
        val uploadInfo = channelInfo?.let {
            UploadInfo(
                null,
                -1L,
                "",
                "",
                it.landscape_ratio_1280_720,
                0,
                0,
                0,
                0,
                null,
                it.program_name,
                it.description,
                /*Base64.decode(it.description, Base64.DEFAULT)
                    .toString(charset("UTF-8"))
                    .removePrefix("<p>")
                    .removeSuffix("</p>"),*/
                it.video_tags,
                it.category,
                it.categoryId,
                null,
                it.age_restriction?.toInt() ?: 0
            )
        }

        setupTagView()
//        observeThumbnailLoad()
        observeThumbnailChange()
        observeCategory()
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
        binding.submitButton.setOnClickListener { submitVideo() }
        binding.thumbEditButton.setOnClickListener { 
            val action = MyChannelVideosEditFragmentDirections.actionMyChannelVideosEditFragmentToThumbnailSelectionMethodFragment("Set Video Cover Photo")
            findNavController().navigate(action) 
        }

        uploadInfo?.let { info ->
            viewModel.initUploadInfo(info)
            info.tags?.split(" | ")?.filter { it.isNotBlank() }?.forEach {
                binding.uploadTags.addChip(it, null)
            }
            binding.uploadTitle.requestFocus()
        }
    }

    private fun observeCategory() {
        observe(viewModel.categories){
            if(it.isNotEmpty()){
                viewModel.categoryPosition.value = channelInfo?.categoryId
            }
        }
    }

    /*private fun observeThumbnailLoad() {
        observe(viewModel.thumbnailUrl) {
            it?.let { thumb ->
                binding.bannerImageView.loadBase64(thumb)
            }
        }
    }*/

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

    /*private suspend fun saveInfo(): UploadInfo? {
//        return getUploadInfo()?.apply {
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
        *//*}?.also {
            uploadRepo.updateUploadInfo(it)
        }*//*
    }*/

    private fun submitVideo() {
        val title = binding.uploadTitle.text.toString()
        val description = binding.uploadDescription.text.toString()
        if (title.isBlank() || description.isBlank()) {
            context?.showToast("Missing required field", Toast.LENGTH_SHORT)
            return
        }
        
        val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }

        val ageGroupIndex = binding.ageGroupSpinner.selectedItemPosition
        val ageGroup = binding.ageGroupSpinner.selectedItem.toString()

        val categoryIndex = binding.categorySpinner.selectedItemPosition
        val category = binding.categorySpinner.selectedItem.toString()
        val categoryId = viewModel.categoryPosition.value ?: 0
        Log.i("_Edit", "Category Id: $categoryId")
        observeEditResponse()
        /*lifecycleScope.launch {
            val categoryObj = binding.categorySpinner.selectedItem
            val categoryId = if (categoryObj is UgcCategory) {
                categoryObj.id
            }
            else -1
                viewModel.saveUploadInfo(channelInfo?.id?.toInt()?:0, "",tags, categoryId)

        }*/
        viewModel.saveUploadInfo(channelInfo?.id?.toInt()?:0, "",tags, categoryId.toLong())
    }

    private fun observeEditResponse() {
        observe(viewModel.editResponse){
            when(it){
                is Resource.Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
