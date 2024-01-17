package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.network.response.KabbikItemBean
import com.banglalink.toffee.databinding.FragmentAudioBookLandingBinding
import com.banglalink.toffee.extension.launchWithLifecycle
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.audiobook.category.AudioBookCategoryView
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class AudioBookLandingFragment<T : Any> : BaseFragment(), ProviderIconCallback<T> {
    private val viewModel by viewModels<AudioBookViewModel>()
    private var _binding: FragmentAudioBookLandingBinding? = null
    private val binding get() = _binding!!
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAudioBookLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Kabbik - Audio Book"

//        progressDialog.show()

//        observeHomeApiResponse()
//
//        launchWithLifecycle {
//            viewModel.grantToken(
//                success = { token -> viewModel.homeApi(token) },
//                failure = { progressDialog.dismiss() }
//            )
//        }
    }

    private fun observeHomeApiResponse() {
        observe(viewModel.homeApiResponse) {
            when (it) {
                is Resource.Success -> {
                    it.data.data.forEach { kabbikCategory ->
                        if (kabbikCategory.itemsData.containsFree()){
                            binding.audioBookFragmentContainer.addView(
                                AudioBookCategoryView(
                                    requireContext()
                                ).apply {
                                    setConfiguration(
                                        cardTitle = kabbikCategory.name ?: "",
                                        items = kabbikCategory.itemsData,
                                        onSeeAllClick = {
                                            val bundle = bundleOf(
                                                "myTitle" to kabbikCategory.name
                                            )
                                            findNavController().navigate(
                                                R.id.audioBookCategoryDetails,
                                                args = bundle
                                            )
                                            requireContext().showToast(kabbikCategory.name ?: "")
                                        }
                                    )
                                })
                        }
                    }.also {
                        progressDialog.dismiss()
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
        progressDialog.dismiss()
    }

    private fun List<KabbikItemBean>.containsFree(): Boolean {
        return any { it.premium == 0 && it.price == 0}
    }
}
