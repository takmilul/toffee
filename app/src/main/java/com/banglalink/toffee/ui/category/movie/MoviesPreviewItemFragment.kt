package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentMoviesPreviewItemBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment

class MoviesPreviewItemFragment : BaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var moviePreview: ChannelInfo
    private lateinit var binding: FragmentMoviesPreviewItemBinding

    companion object {
        @JvmStatic
        fun newInstance(moviePreview: ChannelInfo): MoviesPreviewItemFragment {
            val instance = MoviesPreviewItemFragment()
            instance.moviePreview = moviePreview
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movies_preview_item, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.data = moviePreview
        binding.callback = this
    }
}