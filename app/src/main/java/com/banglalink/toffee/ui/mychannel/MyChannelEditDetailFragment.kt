package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.databinding.FragmentMyChannelEditDetailBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyChannelEditDetailFragment : Fragment(), OnClickListener {
    private var isPosterClicked = false
    private var myChannelDetail: MyChannelDetail? = null
    private var newBannerUrl: String? = null
    private var newProfileImageUrl: String? = null
    private lateinit var binding: FragmentMyChannelEditDetailBinding

    @Inject lateinit var viewModelAssistedFactory: MyChannelEditDetailViewModel.AssistedFactory
    private val viewModel by viewModels<MyChannelEditDetailViewModel> { MyChannelEditDetailViewModel.provideFactory(viewModelAssistedFactory, myChannelDetail) }

    companion object {
        fun newInstance(): MyChannelEditDetailFragment {
            return MyChannelEditDetailFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = MyChannelEditDetailFragmentArgs.fromBundle(requireArguments())
        myChannelDetail = args.myChannelDetail
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_channel_edit_detail, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEditChannel()
        observeThumbnailChange()

        binding.bannerEditButton.setOnClickListener(this)
        binding.profileImageEditButton.setOnClickListener(this)
        binding.cancelButton.setOnClickListener(this)
        binding.saveButton.setOnClickListener(this)
    }

    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner, {
                it?.let {
                    if (isPosterClicked) {
                        newBannerUrl = it
                        loadImage()
                    }
                    else {
                        newProfileImageUrl = it
                        loadImage()
                    }
                }
            })
    }

    private fun loadImage(){
        newBannerUrl?.let {
            binding.bannerImageView.load(it){
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
            }
        }
        newProfileImageUrl?.let {
            binding.profileImageView.load(it) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
            }
        }
    }
    
    private fun observeEditChannel() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    binding.saveButton.isClickable = true
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigateUp()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    binding.saveButton.isClickable = true
                    binding.progressBar.visibility = View.GONE
                    println(it.error)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.cancelButton -> findNavController().navigateUp()
            binding.saveButton -> editChannel()
            binding.bannerEditButton -> {
                isPosterClicked = true
                val action = MyChannelEditDetailFragmentDirections.actionMyChannelEditFragmentToThumbnailSelectionMethodFragment("Set Channel Cover Photo")
                findNavController().navigate(action)
            }
            binding.profileImageEditButton -> {
                isPosterClicked = false
                val action = MyChannelEditDetailFragmentDirections.actionMyChannelEditFragmentToThumbnailSelectionMethodFragment("Set Channel Photo")
                findNavController().navigate(action)
            }
        }
    }

    private fun editChannel() {
        binding.saveButton.isClickable = false
        binding.progressBar.visibility = View.VISIBLE
        
        var bannerBase64 = "NULL"
        try {
            if (!newBannerUrl.isNullOrEmpty()) {
                /*val image = File(newBannerUrl!!.substringAfter("file:"))
                bannerBase64 = convertImageFileToBase64(image)*/
                bannerBase64 = imagePathToBase64(requireContext(), newBannerUrl!!)
            }
        }
        catch (e: Exception) {
            bannerBase64 = "NULL"
        }
        
        var profileImageBase64 = "NULL"
        try {
            if (!newProfileImageUrl.isNullOrEmpty()) {
                /*val image = File(newProfileImageUrl!!.substringAfter("file:"))
                profileImageBase64 = convertImageFileToBase64(image)*/
                profileImageBase64 = imagePathToBase64(requireContext(), newProfileImageUrl!!)
            }
        }
        catch (e: Exception) {
            profileImageBase64 = "NULL"
        }

        when {
            binding.channelName.text.isNullOrBlank() -> {
                binding.progressBar.visibility = GONE
                Toast.makeText(requireContext(), "Please give a channel name", Toast.LENGTH_SHORT).show()
            }
            viewModel.selectedCategory?.id == null -> {
                binding.progressBar.visibility = GONE
                Toast.makeText(requireContext(), "Category is not selected", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val ugcEditMyChannelRequest = MyChannelEditRequest(
                    0,
                    "",
                    0,
                    viewModel.selectedCategory?.id!!,
                    binding.channelName.text.toString(),
                    binding.description.text.toString(),
                    myChannelDetail?.bannerUrl ?: "NULL",
                    bannerBase64,
                    myChannelDetail?.profileUrl ?: "NULL",
                    profileImageBase64
                )

                viewModel.editChannel(ugcEditMyChannelRequest)
            }
        }
    }
    
    /*private fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, Base64.NO_WRAP).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close()
                    outputStream.toString()
                }
            }
        }
    }*/
}