package com.banglalink.toffee.ui.audiobook.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentAudioBookCategoryBaseBinding
import com.banglalink.toffee.ui.category.movie.MoviesAdapter
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.MyBaseAdapter


abstract class AudioBookCategoryBaseFragment<T: Any> : BaseFragment(), ProviderIconCallback<T> {
    protected abstract val cardTitle: String
    protected open val adapter: MyBaseAdapter<T> by lazy { AudioBookCategoryBaseAdapter(this) }
    private var _binding: FragmentAudioBookCategoryBaseBinding ?= null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAudioBookCategoryBaseBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = cardTitle
        binding.rvAudioBooks.adapter = adapter
        loadContent()
    }
    protected abstract fun loadContent()
    protected fun showCard(isShow: Boolean) {
        binding.root.isVisible = isShow
    }

    override fun onItemClicked(item: T) {
        super.onItemClicked(item)
    }

}