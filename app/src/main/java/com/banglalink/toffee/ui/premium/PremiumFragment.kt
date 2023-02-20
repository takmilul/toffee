package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentPremiumBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PremiumFragment : BaseFragment(), ProviderIconCallback<ChannelInfo> {
    private  var _binding: FragmentPremiumBinding?=null
    private val binding get() = _binding!!
    private lateinit var mAdapter: PremiumAdapter
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentPremiumBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)

        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener {
            try {

                findNavController().popBackStack()

            } catch (e:Exception){

            }
        }

        mAdapter = PremiumAdapter(this)

        with(binding.premiumBundleList) {

            adapter = mAdapter
            addItemDecoration(MarginItemDecoration(12))
//            binding.premContentScroller.post { binding.premContentScroller.fullScroll(View.FOCUS_DOWN) }
        }

        observeList()

//        (requireActivity() as HomeActivity).binding.tabNavigator.hide()
//        (requireActivity() as HomeActivity).binding.uploadButton.hide()
//        (requireActivity() as HomeActivity).binding.homeBottomSheet.bottomSheet.hide()
//        (requireActivity() as HomeActivity).binding.tbar.toolbar.hide()
//        (requireActivity() as HomeActivity).binding.tbar.toolbarImageView.hide()






    }


    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadLandingEditorsChoiceContent()
            }
            else {
                landingPageViewModel.loadEditorsChoiceContent()
            }
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    override fun onItemClicked(item: ChannelInfo) {
        findNavController().navigate(R.id.packDetailsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        (requireActivity() as HomeActivity).binding.bottomAppBar.show()
//        (requireActivity() as HomeActivity).binding.tabNavigator.show()
//        (requireActivity() as HomeActivity).binding.uploadButton.show()

        _binding = null
    }
}