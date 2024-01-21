package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentAudiobookPlaylistBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class AudioBookEpisodeListFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private var id: String? = null
    private var _binding: FragmentAudiobookPlaylistBinding?=null
    private lateinit var mAdapter: AudioBookEpisodeListAdapter
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<AudioBookViewModel>()
    private val binding get() = _binding!!
    
    companion object {
        fun newInstance(id: String): AudioBookEpisodeListFragment {
            val args = bundleOf(
                Pair("id", id)
            )
            val fragment = AudioBookEpisodeListFragment()
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudiobookPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        id = arguments?.getString("id")
        activity?.title = id
//        progressDialog.show()
//        binding.progressBar.load(R.drawable.content_loader)
        mAdapter = AudioBookEpisodeListAdapter(this)
        binding.episodeListAudioBook.adapter = mAdapter
        
        observeAudioBookEpisodeList()
    }
    
    private fun observeAudioBookEpisodeList() {
        observe(viewModel.audioBookEpisodeResponseObserver) { response ->
            when (response) {
                is Resource.Success -> {
    //                binding.progressBar.hide()
                    progressDialog.dismiss()
                    binding.episodeDescription.show()
                    binding.title.show()
                    
                    binding.title.text = response.data?.firstOrNull()?.playlistName.toString()
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
        
        for (index in 0 until mAdapter.itemCount) {
            val currentItem = mAdapter.getItem(index)
            currentItem.isSelected = false
        }
        item.isSelected = !item.isSelected!!
        homeViewModel.playContentLiveData.value = item
        mAdapter.notifyDataSetChanged()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}