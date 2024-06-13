package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.KabbikItem
import com.banglalink.toffee.databinding.FragmentAudiobookCategoryBinding
import com.banglalink.toffee.enums.PlaylistType.Audio_Book_Playlist
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.launchWithLifecycle
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioBookCategoryDetailsFragment: BaseFragment(), BaseListItemCallback<KabbikItem> {
    
    private var selectedItem: KabbikItem? = null
    private val binding get() = _binding!!
    private var myTitle: String? = null
    private lateinit var mAdapter: AudioBookCategoryListAdapter
    private var _binding: FragmentAudiobookCategoryBinding ? =null
    private val viewModel by activityViewModels<AudioBookViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudiobookCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myTitle = arguments?.getString("myTitle")
        activity?.title = myTitle

//        binding.progressBar.load(R.drawable.content_loader)
        progressDialog.show()
        mAdapter = AudioBookCategoryListAdapter(this)
        binding.categoriesListAudioBook.adapter = mAdapter

        observeAudioBookSeeMore()
    }

    private fun observeAudioBookSeeMore() {
        observe(viewModel.audioBookSeeMoreResponse) { response ->
            when (response) {
                is Resource.Success -> {
//                    binding.progressBar.hide()
                    progressDialog.dismiss()

                    response.data?.let { audioBookSeeMoreResponse ->
                        val flattenedData = audioBookSeeMoreResponse.data.flatMap { it.data } ?: emptyList()

                        if (flattenedData.isNotEmpty()) {
                            // Filter out items with premium = 0 and price = 0
                            val filteredData = flattenedData.filter { it.premium == 0 && it.price == 0 }

                            mAdapter.addAll(filteredData)

                            binding.categoriesListAudioBook.show()
                            binding.failureInfoLayout.hide()
                        } else {
                            binding.categoriesListAudioBook.hide()
                            binding.failureInfoLayout.show()
                        }
                    }

                }
                is Resource.Failure -> {
//                    binding.progressBar.hide()
                    progressDialog.dismiss()
                    binding.categoriesListAudioBook.hide()
                    binding.failureInfoLayout.show()
                }
            }
        }
        myTitle?.let { title->
            launchWithLifecycle {
                viewModel.grantToken(
                    success = {token->
                        viewModel.getAudioBookSeeMore(title, token)
                    },
                    failure = {}
                )
            }
        }
    }

    override fun onItemClicked(item: KabbikItem) {
        super.onItemClicked(item)
        selectedItem = item
        launchWithLifecycle {
            viewModel.grantToken(
                success = {token->
                    observeAudioBookEpisode()
                    viewModel.getAudioBookEpisode(item.id.toString(), token, myTitle ?: "")
                },
                failure = {}
            )
        }
    }
    
    private fun observeAudioBookEpisode() {
        observe(viewModel.audioBookEpisodeResponse) { response ->
            when (response) {
                is Resource.Success -> {
                    progressDialog.dismiss()
                    response.data?.let { responseData ->
                        if (responseData.isNotEmpty()) {
                            responseData.firstOrNull()?.let { channelInfo ->
                                val playlistPlaybackInfo = PlaylistPlaybackInfo(
                                    playlistId = selectedItem?.id ?: 0,
                                    playlistName = selectedItem?.name.toString(),
                                    channelOwnerId = 0,
                                    playlistItemCount = responseData.size,
                                    playlistThumbnail = selectedItem?.thumbPath.toString(),
                                    isApproved = 1,
                                    playlistType = Audio_Book_Playlist
                                )
                                homeViewModel.addToPlayListMutableLiveData.postValue(
                                    AddToPlaylistData(playlistPlaybackInfo.getPlaylistIdLong(), responseData)
                                )
                                homeViewModel.playContentLiveData.postValue(
                                    playlistPlaybackInfo.copy(
                                        playIndex = 0,
                                        currentItem = channelInfo
                                    )
                                )
                            }
                        } else {
                            progressDialog.dismiss()
                        }
                    }
                }
                is Resource.Failure -> {
                    progressDialog.dismiss()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}