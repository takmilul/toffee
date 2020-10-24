package com.banglalink.toffee.ui.mychannel

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
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.databinding.FragmentMyChannelEditDetailBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.MyChannelDetail
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelEditDetailFragment : Fragment(), OnClickListener {
    private var myChannelDetail: MyChannelDetail? = null
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
            binding.cancelButton -> findNavController().navigateUp()
            binding.saveButton -> editChannel()
        }
    }

    private fun editChannel() {
        
        val byteArrayOutputStream = ByteArrayOutputStream()
        BitmapFactory.decodeResource(resources, R.drawable.hero).compress(PNG, 20, byteArrayOutputStream)
        val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        
        val byteArrayOutputStream2 = ByteArrayOutputStream()
        BitmapFactory.decodeResource(resources, R.drawable.avatar).compress(PNG, 20, byteArrayOutputStream2)
        val imageBytes2: ByteArray = byteArrayOutputStream2.toByteArray()
        val imageString2: String = Base64.encodeToString(imageBytes2, Base64.DEFAULT)

        if (binding.channelName.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please give a channel name", Toast.LENGTH_SHORT).show()
        }
        else if (viewModel.selectedItem?.id == null){
            Toast.makeText(requireContext(), "Category is not selected", Toast.LENGTH_SHORT).show()
        }
        else {
            val ugcEditMyChannelRequest = MyChannelEditRequest(
                0,
                "",
                myChannelDetail?.id ?: 1,
                viewModel.selectedItem?.id!!,
                binding.channelName.text.toString(),
                binding.description.text.toString(),
                myChannelDetail?.bannerUrl ?: "NULL",
                imageString,
                myChannelDetail?.profileUrl ?: "NULL",
                imageString2
            )

            viewModel.editChannel(ugcEditMyChannelRequest)
        }
    }
}