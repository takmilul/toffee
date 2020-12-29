package com.banglalink.toffee.ui.upload

//import com.bumptech.glide.Glide

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.extension.loadBase64
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.model.UgcSubCategory
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.pchmn.materialchips.ChipsInput
import com.pchmn.materialchips.model.ChipInterface
import dagger.hilt.android.AndroidEntryPoint
import io.tus.android.client.TusAndroidUpload
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import io.tus.java.client.TusUpload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class EditUploadInfoFragment : BaseFragment() {
    private lateinit var binding: FragmentEditUploadInfoBinding
    private var client: TusClient? = null
    private var progressDialog: VelBoxProgressDialog? = null

    @Inject
    lateinit var editUploadViewModelFactory: EditUploadInfoViewModel.AssistedFactory

    @Inject
    lateinit var uploadRepo: UploadInfoRepository

    @AppCoroutineScope
    @Inject
    lateinit var appScope: CoroutineScope

    private lateinit var uploadFileUri: String


    private var bool:Boolean?=false

    private val viewModel: EditUploadInfoViewModel by viewModels {
        EditUploadInfoViewModel.provideFactory(editUploadViewModelFactory, uploadFileUri)
    }

    private val uploadProgressViewModel by activityViewModels<UploadProgressViewModel>()

    companion object {
        const val UPLOAD_FILE_URI = "UPLOAD_FILE_URI"


        fun newInstance(): EditUploadInfoFragment {
            return EditUploadInfoFragment()
        }
    }

    var myUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadFileUri = requireArguments().getString(UPLOAD_FILE_URI, "")

        myUri = Uri.parse(uploadFileUri)
        try {
            client = TusClient()
            val sharedPref = activity?.getSharedPreferences("tus", 0)
            client?.setUploadCreationURL(URL("https://ugc-upload.toffeelive.com/upload"))
            client?.enableResuming(TusPreferencesURLStore(sharedPref))
        } catch (e: Exception) {

        }
    }

    class someTask(
        val clients: TusClient?,
        val uploads: TusUpload?
    ) : AsyncTask<Void, Long, URL>() {
        override fun doInBackground(vararg params: Void?): URL? {
            try {
                val uploader = clients!!.resumeOrCreateUpload(uploads!!)
                val totalBytes = uploads!!.size
                var uploadedBytes = uploader.offset
                // Upload file in 1MiB chunks
                uploader.chunkSize = 1024 * 1024
                while (!isCancelled && uploader.uploadChunk() > 0) {
                    uploadedBytes = uploader.offset
                    publishProgress(uploadedBytes, totalBytes)
                }
                uploader.finish()
                return uploader.uploadURL
            } catch (e: java.lang.Exception) {
                cancel(true)


            }
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: URL?) {
            Log.e("data", "data" + result.toString())
            // ...
        }

        override fun onProgressUpdate(vararg updates: Long?) {
            val uploadedBytes: Long? = updates.get(0)
            val totalBytes: Long? = updates.get(1)
            Log.e("data",String.format("Uploaded %d/%d.", uploadedBytes, totalBytes))
            var data:Int
            try {
                data= (uploadedBytes?.toDouble()!! / totalBytes!! * 100).toInt()
                Log.e("ff","prog"+data?.toString() )
            } catch (e: Exception) {
                Log.e("data","exception" +e.message)
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_upload_info,
            container,
            false
        )

        binding.setVariable(BR.viewmodel, viewModel)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.container.setOnClickListener {
            UtilsKt.hideSoftKeyboard(requireActivity())
        }

        binding.cancelButton.setOnClickListener {
//            lifecycleScope.launch {
//                if(UploadService.taskList.isNotEmpty()) {
//                    UploadService.stopAllUploads()
//                }
//                getUploadInfo()?.apply {
//                    status = UploadStatus.CANCELED.value
//                }?.also {
//                    uploadRepo.updateUploadInfo(it)
//                }
//                mPref.uploadId = null
//                findNavController().popBackStack()
//            }
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setTitle("Cancel Uploading")
                setText(
                    "Are you sure that you want to\n" +
                            "cancel uploading video?"
                )
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
            val action =
                EditUploadInfoFragmentDirections.actionEditUploadInfoFragmentToThumbnailSelectionMethodFragment(
                    "Set Video Cover Photo"
                )
            findNavController().navigate(action)
        }

        setupTagView()
        observeStatus()
//        observeUpload()
        observeProgressDialog()
        observeThumbnailLoad()
        observeThumbnailChange()

        binding.uploadTitle.requestFocus()
    }

    private fun observeThumbnailLoad() {
        observe(viewModel.thumbnailData) {
            it?.let { thumb ->
                binding.videoThumb.loadBase64(thumb)
            }
        }
    }

    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            ThumbnailSelectionMethodFragment.THUMB_URI
        )?.observe(viewLifecycleOwner, {
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

        val chippRecycler = binding.uploadTags.findViewById<RecyclerView>(R.id.chips_recycler)
        chippRecycler.setPadding(0)

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


//    override fun onDestroy() {
//        if(mPref.uploadId != null) {
//            appScope.launch {
//                saveInfo()
//            }
//        }
//        super.onDestroy()
//    }

    private fun observeProgressDialog() {
        observe(viewModel.progressDialog) {
            when (it) {
                true -> {
                    progressDialog = VelBoxProgressDialog(requireContext())
                    progressDialog?.show()
                }
                false -> progressDialog?.dismiss()
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
//                        getUploadInfo()?.apply {
//                            status = UploadStatus.SUBMITTED.value
//                        }?.also {info ->
//                            uploadRepo.updateUploadInfo(info)
//                        }
                        progressDialog?.dismiss()
//                        val dialog = VelBoxAlertDialogBuilder(
//                            requireContext(),
//                            text = it.data.message,
//                            icon = R.drawable.subscription_success
//                        ).create()
//                        dialog.setOnDismissListener {
//                            findNavController().popBackStack()
//                        }
//                        dialog.show()
                        findNavController().navigate(R.id.upload_minimize, Bundle().apply {
                            putString(MinimizeUploadFragment.UPLOAD_ID, it.data.first)
                            putLong(MinimizeUploadFragment.CONTENT_ID, it.data.second)
                        })
                    }
                }
                else -> {
                    context?.showToast("Unable to submit the video.")
                }
            }
        }
    }

//    private suspend fun getUploadInfo(): UploadInfo? {
//        return mPref.uploadId?.let {
//            uploadRepo.getUploadById(UtilsKt.stringToUploadId(it))
//        }
//    }

//    private suspend fun saveInfo(): UploadInfo? {
//        return getUploadInfo()?.apply {
//            title = binding.uploadTitle.text.toString()
//            description = binding.uploadDescription.text.toString()
//
//            tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
//            Log.e("TAG", "TAG - $tags, === ${binding.uploadTags.selectedChipList}")
//
//            ageGroupIndex = binding.ageGroupSpinner.selectedItemPosition
//            ageGroup = binding.ageGroupSpinner.selectedItem.toString()
//
//            categoryIndex = binding.categorySpinner.selectedItemPosition
//            category = binding.categorySpinner.selectedItem.toString()
//
////                submitToChallengeIndex = binding.challengeSelectionSpinner.selectedItemPosition
////                submitToChallenge = binding.challengeSelectionSpinner.selectedItem.toString()
//
//            Log.e("UploadInfo", "$this")
//        }?.also {
//            uploadRepo.updateUploadInfo(it)
//        }
//    }

    public fun submitVideo() {
        try {
            val upload: TusUpload = TusAndroidUpload(myUri, context)
            val uploadTask = someTask(client, upload)
            uploadTask.execute()
        } catch (e: java.lang.Exception) {

        }
        val title = binding.uploadTitle.text.toString()
        val description = binding.uploadDescription.text.toString()
        if(title.isBlank() || description.isBlank()) {
            context?.showToast("Missing required field", Toast.LENGTH_SHORT)
            return
        }
        lifecycleScope.launch {
            val categoryObj = binding.categorySpinner.selectedItem
            val categoryId = if(categoryObj is UgcCategory) {
                categoryObj.id
            } else -1

            val subCategoryObj = binding.subCategorySpinner.selectedItem
            val subcategoryId = if(subCategoryObj is UgcSubCategory) {
                subCategoryObj.id
            } else -1

            val tags = binding.uploadTags.selectedChipList.joinToString(" | ") { it.label }
            viewModel.saveUploadInfo(tags, categoryId, subcategoryId)
        }
    }
    @SuppressLint("NewApi")
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}
