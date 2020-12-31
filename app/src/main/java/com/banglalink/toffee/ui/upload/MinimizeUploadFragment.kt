package com.banglalink.toffee.ui.upload

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.UtilsKt
import dagger.hilt.android.AndroidEntryPoint
import io.tus.android.client.TusAndroidUpload
import io.tus.android.client.TusPreferencesURLStore
import io.tus.java.client.TusClient
import io.tus.java.client.TusUpload
import kotlinx.android.synthetic.main.fragment_minimize_upload.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.UploadService
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MinimizeUploadFragment : BaseFragment() {
    private lateinit var uploadId: String
    private lateinit var uploadURI: String
    private var contentId: Long = -1
    private var preference: Preference? = null
    private var client: TusClient? = null
    var myUri: Uri? = null

    @Inject
    lateinit var uploadRepo: UploadInfoRepository
    private lateinit var fileName: String
    private lateinit var exception: String
    private var size: String = ""
    private var actualFileName: String? = null

    companion object {
        const val UPLOAD_ID = "UPLOAD_ID"
        const val UPLOAD_URI = "UPLOAD_URI"
        const val CONTENT_ID = "SERVER_CONTENT_ID"
        const val FILE_NAME = "FILE"
        const val EXCEPTION = "EXCEPTION"
        @JvmStatic
        fun newInstance(): MinimizeUploadFragment {
            return MinimizeUploadFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uploadId = requireArguments().getString(UPLOAD_ID)!!
        uploadURI = requireArguments().getString(UPLOAD_URI)!!
        contentId = requireArguments().getLong(CONTENT_ID)
        fileName = requireArguments().getString(FILE_NAME)!!
        exception = requireArguments().getString(EXCEPTION)!!

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_minimize_upload, container, false)

    }

    suspend fun value() {
        val fileSize = withContext(Dispatchers.IO + Job()) {
            UtilsKt.fileSizeFromContentUri(context!!, Uri.parse(uploadURI))
        }
        size = Utils.readableFileSize(fileSize)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preference = Preference.getInstance()


        try {
            client = TusClient()
            val sharedPref = activity?.getSharedPreferences("tus", 0)
            client?.setUploadCreationURL(URL("https://ugc-upload.toffeelive.com/upload"))
            client?.enableResuming(TusPreferencesURLStore(sharedPref))
        } catch (e: Exception) {

        }
        if (exception.equals("Exception")){
            val file = File(uploadURI)
            myUri = Uri.fromFile(file)
        }
        else{
            myUri = Uri.parse(uploadURI)
        }

        //
        runBlocking { value() }

        cancel_button.setOnClickListener {
            VelBoxAlertDialogBuilder(requireContext()).apply {
                setTitle("Cancel Uploading")
                setText("Are you sure that you want to\n" +
                        "cancel uploading video?")
                setPositiveButtonListener("NO") {
                    it?.dismiss()
                }
                setNegativeButtonListener("YES") {
                    UploadService.stopAllUploads()
                    findNavController().popBackStack(R.id.menu_feed, false)
                    it?.dismiss()
                }
            }.create().show()
        }

        minimize_button.setOnClickListener {
            findNavController().popBackStack(R.id.menu_feed, false)
        }

        observeUpload()
    }

    private fun observeUpload() {
        try {
            val upload: TusUpload = TusAndroidUpload(myUri, context, fileName, uploadURI)
            val uploadTask = someTask(context!!, client, upload)

            uploadTask.execute()
        } catch (e: java.lang.Exception) {
            Log.e("message", "message")

        }

    }

    inner class someTask(
            val context: Context,
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

        private var progressDialog: VelBoxProgressDialog? = null
        override fun onPreExecute() {
            super.onPreExecute()
            uploadSize.text = size
            // ...
        }

        override fun onPostExecute(result: URL?) {
            Log.e("data", "data" + result.toString())
            uploadPercent.text = "100%"
            progressBar.progress = 100
            // ...
        }

        override fun onProgressUpdate(vararg updates: Long?) {
            val uploadedBytes: Long? = updates.get(0)
            val totalBytes: Long? = updates.get(1)
            Log.e("data", String.format("Uploaded %d/%d.", uploadedBytes, totalBytes))
            var data: Int? = 0
            try {
                data = (uploadedBytes?.toDouble()!! / totalBytes!! * 100).toInt()
                setData(data)
                Log.e("ff", "prog" + data?.toString())
            } catch (e: Exception) {
                Log.e("data", "exception" + e.message)
            }


        }

    }

    fun setData(value: Int) {
        uploadPercent.text = value?.toString() + "%"
        progressBar.progress = value
    }

}