package com.banglalink.toffee.ui.audiobook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.Episodes
import com.banglalink.toffee.databinding.FragmentAudiobookPlaylistBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class AudioBookEpisodeListFragment : BaseFragment(), BaseListItemCallback<Episodes> {
    private var id: String? = null

    private var _binding: FragmentAudiobookPlaylistBinding?=null
    private lateinit var mAdapter: AudioBookEpisodeListAdapter
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    val homeViewModel by activityViewModels<HomeViewModel>()
    val viewModel by activityViewModels<AudioBookViewModel>()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudiobookPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id = arguments?.getString("id")

        progressDialog.show()
//        binding.progressBar.load(R.drawable.content_loader)
        mAdapter = AudioBookEpisodeListAdapter(this)
        binding.episodeListAudioBook.adapter = mAdapter

        observeAudioBookEpisode()
    }

    private fun observeAudioBookEpisode() {
        observe(viewModel.audioBookEpisodeResponse) { response ->
            when (response) {
                is Resource.Success -> {
//                    binding.progressBar.hide()
                    progressDialog.dismiss()
                    binding.episodeDescription.show()
                    binding.title.show()

                    binding.title.text = response.data?.name.toString()
                    binding.episodeDescription.text = HtmlCompat.fromHtml(response.data?.description.toString()
                        .trim()
                        .replace("\n", "<br/>"),
                        HtmlCompat.FROM_HTML_MODE_LEGACY)

                    response.data?.let { responseData ->
                        val episodesList = responseData.episodes ?: emptyList()

                        if (episodesList.isNotEmpty()) {
                            mAdapter.addAll(episodesList)
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
            }
        }
        id?.let { viewModel.getAudioBookEpisode(it) }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClicked(item: Episodes) {
        super.onItemClicked(item)

        for (index in 0 until mAdapter.itemCount) {
            val currentItem = mAdapter.getItem(index)
            currentItem?.isSelected = false
        }
        item.isSelected = !item.isSelected!!
        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}