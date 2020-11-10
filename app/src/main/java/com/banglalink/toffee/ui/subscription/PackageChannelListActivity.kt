package com.banglalink.toffee.ui.subscription

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityPackageChannelListLayoutBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.GridSpacingItemDecoration
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class PackageChannelListActivity : AppCompatActivity() {

    companion object{
        const val PACKAGE= "PACKAGE"
    }
    lateinit var binding:ActivityPackageChannelListLayoutBinding
    lateinit var mPackage:Package
    lateinit var mAdapter:PackageChannelListAdapter

    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(PackageChannelListViewModel::class.java)
    }

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPackage = intent.getSerializableExtra(PACKAGE) as Package
        binding= DataBindingUtil.setContentView(this,R.layout.activity_package_channel_list_layout)

        setupToolbar()
        setupUi()
        getPackages()
    }

    private fun setupUi(){
        mAdapter = PackageChannelListAdapter()
        binding.listview.apply {
            val spanCount = 3// 3 columns
            val spacing = 35 // px
            val includeEdge = true
            addItemDecoration(GridSpacingItemDecoration(spanCount,spacing,includeEdge))
            layoutManager = GridLayoutManager(this@PackageChannelListActivity,spanCount)
            adapter = mAdapter
        }

        binding.packageNameTv.text=mPackage.packageName
        binding.badgeIcon.load(mPackage.posterMobile){
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_logo_home)
            error(R.drawable.ic_logo_home)
        }

        binding.packageChannelsTv.text= getString(R.string.formatted_channel_number_text,mPackage.programs)



        if(mPackage.price==0 || mPackage.isSubscribed || mPackage.autoRenewButton == 0){
            binding.subscribe.visibility= View.GONE
        }
        else{
            setSubscribeText(mPackage)
        }

        binding.subscribe.setOnClickListener{
            launchActivity<SubscribePackageActivity> {
                putExtra(SubscribePackageActivity.PACKAGE,mPackage)
            }
        }
    }

    private fun setupToolbar(){
        binding.toolbar.title = mPackage.packageName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getPackages(){
        progressDialog.show()
        observe(viewModel.getPackageChannels(mPackage.packageId)){
            progressDialog.dismiss()
            when(it){
                is Resource.Success ->{
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    private fun setSubscribeText(mPackage: Package) {
        val duration = when {
            mPackage.duration >= 360 -> "year"
            mPackage.duration >= 30 -> "month"
            mPackage.duration >= 7 -> "week"
            else -> "day"
        }

        if (mPackage.discount > 0) {
            val spannable = SpannableStringBuilder(
                mPackage.discount.toString() + " " + getString(
                    R.string.package_price_formatted_text,
                    mPackage.price,
                    duration
                )
            )
            spannable.setSpan(
                StrikethroughSpan(),
                0,
                mPackage.discount.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor(getString(R.string.strike_through_color_code))),
                0,
                mPackage.discount.toString().length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            binding.subscribe.text = spannable
        } else {
            binding.subscribe.text = getString(
                R.string.package_price_formatted_text,
                mPackage.price,
                duration
            )
        }

    }
}
