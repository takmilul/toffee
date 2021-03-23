package com.banglalink.toffee.ui.subscription

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.databinding.ActivitySubscribePackageListBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class PackageListActivity: AppCompatActivity(), PackageCallBack {
    private lateinit var binding: ActivitySubscribePackageListBinding
    private val viewModel by viewModels<PackageListViewModel>()

    private val mAdapter:PackageListAdapter by unsafeLazy {
        PackageListAdapter(this){
            launchActivity<PackageChannelListActivity> {
                putExtra(PackageChannelListActivity.PACKAGE,it)
            }
        }
    }

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribePackageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.list.apply {
            layoutManager = LinearLayoutManager(this@PackageListActivity)
            adapter = mAdapter
        }

        loadPackageList()

    }

    private fun loadPackageList(){
        progressDialog.show()
        observe(viewModel.packageLiveData){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    mAdapter.removeAll()
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    override fun onShowChannelClick(mPackage: Package) {
        launchActivity<PackageChannelListActivity> {
            putExtra(PackageChannelListActivity.PACKAGE,mPackage)
        }
    }

    override fun onAutoRenewUpdate(mPackage: Package) {
        progressDialog.show()
        observe(viewModel.setAutoRenew(mPackage, mPackage.isAutoRenewable != 1)){
            progressDialog.dismiss()
            when(it){
                is Resource.Success ->{
                    mPackage.isAutoRenewable = mPackage.isAutoRenewable xor 1
                    mAdapter.notifyDataSetChanged()
                    showToast(it.data)
                }
                is Resource.Failure ->{
                    mAdapter.updatePackage(mPackage)
                    showToast(it.error.msg)
                }
            }
        }
    }

    override fun onSubscribeClick(mPackage: Package) {
        launchActivity<SubscribePackageActivity> {
            putExtra(SubscribePackageActivity.PACKAGE,mPackage)
        }
    }
}
