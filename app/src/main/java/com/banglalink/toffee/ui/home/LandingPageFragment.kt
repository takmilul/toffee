package com.banglalink.toffee.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_page2.*
import kotlinx.android.synthetic.main.home_mini_upload_progress.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserver
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

//import com.daimajia.slider.library.SliderTypes.BaseSliderView
//import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class LandingPageFragment : HomeBaseFragment()/*,BaseSliderView.OnSliderClickListener*/ {

    companion object {
        fun newInstance(): LandingPageFragment {
            return LandingPageFragment()
        }
    }

//    override fun onSliderClick(slider: BaseSliderView?) {
//        homeViewModel.fragmentDetailsMutableLiveData.postValue((slider as DefaultSliderView).data as ChannelInfo)
//    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {
//        popularVideoListAdapter.remove(channelInfo)
    }

//    lateinit var popularVideoListAdapter: PopularVideoListAdapter
//    private var popularVideoListView: RecyclerView? = null
//    private var bottomProgress: ProgressBar? = null
//    private lateinit var popularVideoScrollListener : EndlessRecyclerViewScrollListener


    lateinit var viewModel: LandingPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(LandingPageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"

//        viewModel.loadChannels()
//        viewModel.loadPopularVideos()
//        viewModel.loadCategories()
//        viewModel.loadFeatureContents()
//        viewModel.loadUserChannels()
        viewModel.loadMostPopularVideos()
        observeUpload()
    }

    fun onBackPressed(): Boolean {

        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    private fun observeUpload() {
        add_upload_info_button.setOnClickListener {
            findNavController().navigate(R.id.editUploadInfoFragment)
        }

        if(Preference.getInstance().uploadStatus < 0) {
            home_mini_progress_container.visibility = View.GONE
        } else {
            home_mini_progress_container.visibility = View.VISIBLE
        }

        RequestObserver(requireContext(), this, object: RequestObserverDelegate {
            override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

            }

            override fun onCompletedWhileNotObserving() {

            }

            override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
                home_mini_progress_container.visibility = View.GONE
            }

            override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                Log.e("UPLOAD", "UploadId - ${uploadInfo.uploadId}, " +
                        "PrefUploadId - ${Preference.getInstance().uploadId}, " +
                        "Progress - ${uploadInfo.progressPercent}")
                if(uploadInfo.uploadId == Preference.getInstance().uploadId &&
                    uploadInfo.progressPercent in 0..100) {
                    Log.e("Upload2", "Inside onProgress")
                    add_upload_info_button.visibility = View.INVISIBLE
                    upload_size_text.visibility = View.VISIBLE
                    mini_upload_progress_text.text = "Uploading - ${uploadInfo.progressPercent}%"
                    mini_upload_progress.progress = uploadInfo.progressPercent
                    upload_size_text.text = Utils.readableFileSize(uploadInfo.totalBytes)
                }
            }

            override fun onSuccess(
                context: Context,
                uploadInfo: UploadInfo,
                serverResponse: ServerResponse
            ) {
                mini_upload_progress.progress = 100
                add_upload_info_button.visibility = View.VISIBLE
                upload_size_text.visibility = View.INVISIBLE
                mini_upload_progress_text.text = "Upload complete"
            }
        })
    }
}