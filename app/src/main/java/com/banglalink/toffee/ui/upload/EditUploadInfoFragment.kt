package com.banglalink.toffee.ui.upload

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.unsafeLazy
import com.bumptech.glide.Glide
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class EditUploadInfoFragment: Fragment() {
    private var uploadId: String? = null
    private var uploadUri: String? = null
    private lateinit var binding: FragmentEditUploadInfoBinding

    private val viewModel: EditUploadInfoViewModel by unsafeLazy {
        ViewModelProvider(this)[EditUploadInfoViewModel::class.java]
    }

    companion object {
        private const val ARG_UPLOAD_ID = "arg_upload_id"
        private const val ARG_UPLOAD_URI = "arg_upload_uri"

        fun newInstance(_uploadUri: String, _uploadId: String): EditUploadInfoFragment {
            return EditUploadInfoFragment().apply {
                arguments = Bundle().also {
                    it.putString(ARG_UPLOAD_URI, _uploadUri)
                    it.putString(ARG_UPLOAD_ID, _uploadId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_upload_info, container, false)
        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadId = arguments?.getString(ARG_UPLOAD_ID, null)
        uploadUri = arguments?.getString(ARG_UPLOAD_URI, null)
        uploadUri?.let {
            Log.e("IMAGE", it)
            Glide.with(this).load(it).into(binding.videoThumb)
        }

        binding.submitButton.setOnClickListener {
            VelBoxAlertDialogBuilder(requireContext(),
                text="Your video has been submitted!\n" +
                        "You will be notified once its published.",
                icon = R.drawable.subscription_success
            ).create().show()
        }

        binding.thumbEditButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.content_viewer, ThumbnailSelectionMethodFragment.newInstance())
                .addToBackStack(ThumbnailSelectionMethodFragment::class.java.simpleName)
                .commit()
        }

        setupTagView()
        observeUpload()
    }

    private fun setupTagView() {
//        binding.uploadTags.setMaxHeight(Utils.convertDpToPixel(200f, requireContext()).toInt())
        with(binding.uploadTags.editText) {
            gravity = Gravity.START or Gravity.TOP
            setLines(2)
            maxLines = 2
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val chippRecycler = binding.uploadTags.findViewById<RecyclerView>(R.id.chips_recycler)
        chippRecycler.setPadding(0)

        binding.uploadTags.addChipsListener(object: ChipsInput.ChipsListener {
            override fun onChipAdded(chip: ChipInterface?, newSize: Int) {

            }

            override fun onChipRemoved(chip: ChipInterface?, newSize: Int) {

            }

            override fun onTextChanged(text: CharSequence?) {
                Log.e("CHIPS", text.toString())
                if(text?.endsWith(" ") == true) {
                    val chipText = text.toString().trim().capitalize()
                    binding.uploadTags.addChip(chipText, null)
                }
            }
        })
    }

    private fun observeUpload() {
        RequestObserver(requireContext(), this, object: RequestObserverDelegate {
            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                binding.progressBar.visibility = View.GONE
                binding.uploadProgressText.visibility = View.GONE
                binding.uploadSizeText.visibility = View.GONE
            }

            override fun onCompletedWhileNotObserving() {
                binding.progressBar.visibility = View.GONE
                binding.uploadProgressText.visibility = View.GONE
                binding.uploadSizeText.visibility = View.GONE
            }

            override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {

            }

            override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                viewModel.updateProgress(uploadInfo.progressPercent, uploadInfo.totalBytes)
            }

            override fun onSuccess(
                context: Context,
                uploadInfo: UploadInfo,
                serverResponse: ServerResponse
            ) {

            }
        }, shouldAcceptEventsFrom = { it.uploadId == uploadId })
    }
}
