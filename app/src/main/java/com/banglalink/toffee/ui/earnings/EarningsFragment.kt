package com.banglalink.toffee.ui.earnings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentEarningsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.tabs.TabLayoutMediator

class EarningsFragment : Fragment() {

    private lateinit var binding: FragmentEarningsBinding
    private val viewModel by unsafeLazy { ViewModelProviders.of(this).get(EarningsViewModel::class.java) }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_earnings, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()

        binding.viewPager.offscreenPageLimit = 1
        viewPagerAdapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

    }
    
    private fun observeData(){
        observe(viewModel.getEarningInfo()){
            when(it){
                is Success -> {

                }
                is Failure -> {

                }
            }
        }
    }
}