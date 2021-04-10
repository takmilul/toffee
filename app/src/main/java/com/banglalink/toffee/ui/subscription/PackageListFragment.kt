package com.banglalink.toffee.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.banglalink.toffee.databinding.ActivitySubscribePackageListBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PackageListFragment: BaseFragment(), PackageCallBack {
    private lateinit var binding: ActivitySubscribePackageListBinding
    private val viewModel by viewModels<PackageListViewModel>()

    private val mAdapter:PackageListAdapter by unsafeLazy {
        PackageListAdapter(this){
            requireActivity().launchActivity<PackageChannelListActivity> {
                putExtra(PackageChannelListActivity.PACKAGE,it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivitySubscribePackageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(mPref.isSubscriptionActive != "true") {
            binding.alwaysFree.root.visibility = View.VISIBLE
            binding.list.visibility = View.GONE
        } else {
            binding.list.visibility = View.VISIBLE
            binding.alwaysFree.root.visibility = View.GONE
            binding.list.apply {
                adapter = mAdapter
            }

            loadPackageList()
        }
    }

    private fun loadPackageList(){
//        progressDialog.show()
        binding.progressBar.visibility = View.VISIBLE
        observe(viewModel.packageLiveData){
//            progressDialog.dismiss()
            binding.progressBar.visibility = View.GONE
            when(it){
                is Resource.Success->{
                    mAdapter.removeAll()
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure->{
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    override fun onShowChannelClick(mPackage: Package) {
        requireActivity().launchActivity<PackageChannelListActivity> {
            putExtra(PackageChannelListActivity.PACKAGE,mPackage)
        }
    }

    override fun onAutoRenewUpdate(mPackage: Package) {
//        progressDialog.show()
        observe(viewModel.setAutoRenew(mPackage, mPackage.isAutoRenewable != 1)){
//            progressDialog.dismiss()
            when(it){
                is Resource.Success ->{
                    mPackage.isAutoRenewable = mPackage.isAutoRenewable xor 1
                    mAdapter.notifyDataSetChanged()
                    requireContext().showToast(it.data)
                }
                is Resource.Failure ->{
                    mAdapter.updatePackage(mPackage)
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    override fun onSubscribeClick(mPackage: Package) {
        requireActivity().launchActivity<SubscribePackageActivity> {
            putExtra(SubscribePackageActivity.PACKAGE,mPackage)
        }
    }
}
