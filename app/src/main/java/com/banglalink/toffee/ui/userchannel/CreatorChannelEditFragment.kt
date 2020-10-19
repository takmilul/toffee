package com.banglalink.toffee.ui.userchannel

import android.graphics.Bitmap.CompressFormat.JPEG
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

@AndroidEntryPoint
class CreatorChannelEditFragment : Fragment(), OnClickListener {
    private var myChannelDetail: UgcMyChannelDetail? = null
    private lateinit var binding: FragmentCreatorChannelEditBinding
    private val viewModel by viewModels<CreatorChannelEditViewModel>()

    companion object {
        fun newInstance(): CreatorChannelEditFragment {
            return CreatorChannelEditFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        viewModel.getCategories()
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
        val args = CreatorChannelEditFragmentArgs.fromBundle(requireArguments())
        myChannelDetail = args.myChannelDetail
        viewModel.userChannel.postValue(myChannelDetail)
        binding.cancelButton.setOnClickListener(this)
        binding.saveButton.setOnClickListener(this)
    }

    private fun observeLiveData() {
        observe(viewModel.liveData) {
            when (it) {
                is Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
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
        
        val byteArrayOutputStream = ByteArrayOutputStream()
        BitmapFactory.decodeResource(resources, R.drawable.hero).compress(JPEG, 100, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        
        val ugcEditMyChannelRequest = UgcEditMyChannelRequest(
            0,
            "",
            myChannelDetail?.id ?: 0,
            0,
            binding.channelName.text.toString(),
            binding.description.text.toString(),
            myChannelDetail?.bannerUrl ?: "NULL",
            imageString,
            myChannelDetail?.profileUrl ?: "NULL",
            imageString
        )
        
        viewModel.editChannel(ugcEditMyChannelRequest)
    }
}