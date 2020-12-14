package com.banglalink.toffee.ui.category.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.LayoutHorizontalContentContainerBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.ui.common.BaseFragment

class MoviesComingSoonFragment : BaseFragment() {
    private lateinit var adapter: MoviesComingSoonAdapter
    private lateinit var binding: LayoutHorizontalContentContainerBinding
    private val viewModel by activityViewModels<MovieViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = MoviesComingSoonFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.layout_horizontal_content_container, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.titleTextView.text = "Coming Soon"
        adapter = MoviesComingSoonAdapter()
        binding.listView.adapter = adapter
        loadContent()
        viewModel.loadComingSoonContents
    }

    private fun loadContent() {
        observe(viewModel.comingSoonContents){
            adapter.addAll(it)
        }
    }

    override fun onStop() {
        viewModel.comingSoonContents.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
}