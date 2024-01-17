package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.DataBean
import com.banglalink.toffee.databinding.FragmentAudiobookCategoryBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.ifNotNullOrEmpty
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioBookCategoryDetailsFragment: BaseFragment(), BaseListItemCallback<DataBean> {
    
    private val binding get() = _binding!!
    private var myTitle: String? = null
    private lateinit var mAdapter: AudioBookCategoryListAdapter
    private var _binding: FragmentAudiobookCategoryBinding ? =null
    val viewModel by activityViewModels<AudioBookViewModel>()
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
                        val flattenedData = audioBookSeeMoreResponse.data?.flatMap { it.data } ?: emptyList()

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
        myTitle?.let { viewModel.getAudioBookSeeMore(it) }
    }

    override fun onItemClicked(item: DataBean) {
        super.onItemClicked(item)
        val bundle = Bundle().apply {
            putString("id", item.id.toString())
        }
        findNavController().navigate(R.id.audioBookEpisodeList, args = bundle)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}