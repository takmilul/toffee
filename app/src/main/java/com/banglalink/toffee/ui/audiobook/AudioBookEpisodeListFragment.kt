package com.banglalink.toffee.ui.audiobook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentAudiobookPlaylistBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class AudioBookEpisodeListFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private var currentItem: ChannelInfo? = null
    private lateinit var playlistInfo: PlaylistPlaybackInfo
    private lateinit var mAdapter: AudioBookEpisodeListAdapter
    private var _binding: FragmentAudiobookPlaylistBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<AudioBookViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    companion object {
        fun newInstance(info: PlaylistPlaybackInfo): AudioBookEpisodeListFragment {
            return AudioBookEpisodeListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlistInfo", info)
                }
            }
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudiobookPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playlistInfo = arguments?.getParcelable("playlistInfo")!!
        currentItem = playlistInfo.currentItem?.apply { feature_image = playlistInfo.playlistThumbnail }
        activity?.title = playlistInfo.playlistName
        
//        progressDialog.show()
//        binding.progressBar.load(R.drawable.content_loader)
        mAdapter = AudioBookEpisodeListAdapter(this)
        binding.episodeListAudioBook.adapter = mAdapter
        
        observeAudioBookEpisodeList()
    }
    
    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        for (index in 0 until mAdapter.itemCount) {
            val currentItem = mAdapter.getItem(index)
            currentItem.isSelected = false
        }
        currentItem?.isSelected = !(currentItem?.isSelected ?: false)
//        homeViewModel.playContentLiveData.value = item
        mAdapter.notifyDataSetChanged()
    }
    
    @SuppressLint("SetTextI18n")
    private fun observeAudioBookEpisodeList() {
        observe(viewModel.audioBookEpisodeResponseFlow) { response ->
            when (response) {
                is Resource.Success -> {
    //                binding.progressBar.hide()
                    progressDialog.dismiss()
                    binding.episodeDescription.show()
                    binding.title.show()
                    binding.authorName.show()
                    
                    binding.title.text = response.data?.firstOrNull()?.playlistName.toString()
                    binding.authorName.text = "By - ${response.data?.firstOrNull()?.authorName.toString()}"
                    binding.episodeDescription.text = HtmlCompat.fromHtml(
                        response.data?.firstOrNull()?.playlistDescription.toString()
                            .trim()
                            .replace("\n", "<br/>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    
                    response.data?.let { responseData ->
                        if (responseData.isNotEmpty()) {
                            mAdapter.removeAll()
                            mAdapter.addAll(responseData.mapIndexed { index, channelInfo ->
                                channelInfo.isSelected = index == 0
                                channelInfo.feature_image = playlistInfo.playlistThumbnail
                                channelInfo
                            })
                            binding.playListText.show()
                            binding.episodeListAudioBook.show()
                            binding.failureInfoLayout.hide()
                        } else {
                            progressDialog.dismiss()
                            binding.episodeListAudioBook.hide()
                            binding.failureInfoLayout.show()
                        }
                    }
                }
                
                is Resource.Failure -> {
    //                    binding.progressBar.hide()
                    progressDialog.dismiss()
                    binding.episodeListAudioBook.hide()
                    binding.failureInfoLayout.show()
                }
                
                else -> {}
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        if (item == currentItem || item.id == currentItem?.id) {
            return
        }
        val index = mAdapter.getItems().indexOf(item)
        homeViewModel.addToPlayListMutableLiveData.postValue(
            AddToPlaylistData(playlistInfo.getPlaylistIdLong(), mAdapter.getItems())
        )
        homeViewModel.playContentLiveData.postValue(
            playlistInfo.copy(playIndex = index, currentItem = currentItem)
        )
        setCurrentChannel(item)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}