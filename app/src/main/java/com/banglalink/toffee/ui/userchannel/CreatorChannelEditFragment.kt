package com.banglalink.toffee.ui.userchannel

import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.UgcEditMyChannelRequest
import com.banglalink.toffee.databinding.FragmentCreatorChannelEditBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcMyChannelDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_creator_channel_edit.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class CreatorChannelEditFragment : Fragment(), OnClickListener {
    private var myChannelDetail: UgcMyChannelDetail? = null
    private lateinit var binding: FragmentCreatorChannelEditBinding

    @Inject lateinit var viewModelAssistedFactory: CreatorChannelEditViewModel.AssistedFactory
    private val viewModel by viewModels<CreatorChannelEditViewModel> { CreatorChannelEditViewModel.provideFactory(viewModelAssistedFactory, myChannelDetail) }

    companion object {
        fun newInstance(): CreatorChannelEditFragment {
            return CreatorChannelEditFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = CreatorChannelEditFragmentArgs.fromBundle(requireArguments())
        myChannelDetail = args.myChannelDetail
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_creator_channel_edit, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
        
        binding.cancelButton.setOnClickListener(this)
        binding.saveButton.setOnClickListener(this)
    }

    private fun observeLiveData() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    findNavController().navigateUp()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    println(it.error)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            cancelButton -> findNavController().navigateUp()
            saveButton -> editChannel()
        }
    }

    private fun editChannel() {
        
        var byteArrayOutputStream = ByteArrayOutputStream()
        BitmapFactory.decodeResource(resources, R.drawable.hero).compress(PNG, 50, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        
        val byteArrayOutputStream2 = ByteArrayOutputStream()
        BitmapFactory.decodeResource(resources, R.drawable.avatar).compress(PNG, 50, byteArrayOutputStream2)
        val imageBytes2: ByteArray = byteArrayOutputStream2.toByteArray()
        val imageString2: String = Base64.encodeToString(imageBytes2, Base64.DEFAULT)
        
        val ugcEditMyChannelRequest = UgcEditMyChannelRequest(
            0,
            "",
            myChannelDetail?.id ?: 1,
            viewModel.selectedItem?.id!!,
            binding.channelName.text.toString(),
            binding.description.text.toString(),
            myChannelDetail?.bannerUrl ?: "NULL",
            imageString,
            myChannelDetail?.profileUrl ?:"NULL",
            imageString2
        )
        
        viewModel.editChannel(ugcEditMyChannelRequest)
    }
}

//
// 