package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.banglalink.toffee.databinding.FragmentEarningsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class EarningsFragment : Fragment() {

    private var _binding: FragmentEarningsBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<EarningsViewModel>()

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fragmentList: ArrayList<Fragment> = arrayListOf()
    private var fragmentTitleList: ArrayList<String> = arrayListOf()
    
    companion object {

        @JvmStatic
        fun newInstance() =
            EarningsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (fragmentList.isEmpty()){
            fragmentTitleList.add("Transactions")
            fragmentTitleList.add("Withdraw")
            fragmentTitleList.add("Payment Methods")

            fragmentList.add(TransactionFragment.newInstance(true))
            fragmentList.add(WithdrawFragment.newInstance(true))
            fragmentList.add(PaymentMethodFragment.newInstance(true))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
        viewModel.getEarningInfo()
        
        binding.viewPager.offscreenPageLimit = 1
//        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

    }
    
    private fun observeData(){
        observe(viewModel.liveData){
            when(it){
                is Success -> {

                }
                is Failure -> {

                }
            }
        }
    }
}