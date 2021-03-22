package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentMoviesPreviewItemBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel

class MoviesPreviewItemFragment : BaseFragment(), BaseListItemCallback<ChannelInfo> {
    private lateinit var moviePreview: ChannelInfo
    private var _binding: FragmentMoviesPreviewItemBinding ? = null
    private val binding get() = _binding!!
    val homeViewModel by activityViewModels<HomeViewModel>()

    companion object {
        @JvmStatic
        fun newInstance(preview: ChannelInfo): MoviesPreviewItemFragment {
            return MoviesPreviewItemFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable("channel-info", preview)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moviePreview = requireArguments().getParcelable("channel-info")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movies_preview_item, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.data = moviePreview
        binding.callback = this
    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }
}