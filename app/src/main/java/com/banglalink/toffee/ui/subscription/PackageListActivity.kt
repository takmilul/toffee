package com.banglalink.toffee.ui.subscription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog

class PackageListActivity : AppCompatActivity(),PackageCallBack {

    private val toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(PackageListViewModel::class.java)
    }

    private val mAdapter:PackageListAdapter by lazy {
        PackageListAdapter(this){
            launchActivity<PackageChannelListActivity> {
                putExtra(PackageChannelListActivity.PACKAGE,it)
            }
        }
    }

    private val progressDialog by lazy {
        VelBoxProgressDialog(this)
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe_package_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val list :RecyclerView = findViewById(R.id.list)
        list.apply {
            layoutManager = LinearLayoutManager(this@PackageListActivity)
            adapter = mAdapter
        }

        progressDialog.show()
        observe(viewModel.packageLiveData){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
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

    override fun onSubscribeClick(mPackage: Package) {
        launchActivity<SubscribePackageActivity> {
            putExtra(SubscribePackageActivity.PACKAGE,mPackage)
        }
    }
}
