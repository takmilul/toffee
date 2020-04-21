package com.banglalink.toffee.ui.subscription

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.View
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.ActivityConfirmPurchaseBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class SubscribePackageActivity : BaseAppCompatActivity() {

    companion object{
        const val PACKAGE = "PACKAGE"
    }
    lateinit var binding:ActivityConfirmPurchaseBinding
    lateinit var mPackage:Package

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(this)
    }
    private val viewModel by unsafeLazy {
        getViewModel<SubscribePackageViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPackage = intent.getSerializableExtra(PACKAGE) as Package
        binding = DataBindingUtil.setContentView(this,R.layout.activity_confirm_purchase)

        setUpToolbar()
        populateUI()

        binding.confirmBtn.setOnClickListener{
            subscribePackage()
        }
    }

    private fun subscribePackage(){
        progressDialog.show()
        observe(viewModel.subscribePackage(mPackage,binding.purchaseCard.autoRenewCheckbox.isChecked)){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    ToffeeAnalytics.logSubscription(mPackage)
                    launchActivity<SubscriptionResultActivity>{
                        putExtra(SubscriptionResultActivity.PACKAGE,mPackage)
                    }
                }
                is Resource.Failure->{
                    binding.root.snack(it.error.msg){
                        action("Ok"){

                        }
                    }
                }
            }
        }

    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun populateUI() {
        binding.purchaseCard.packageNameTv.text = mPackage.packageName
        binding.purchaseCard.amount.text = getString(R.string.amount_formatted_text, mPackage.price)
        if(mPackage.duration == 1){
            binding.purchaseCard.validity.text = getString(R.string.single_day_formatted_text, mPackage.duration)
            binding.purchaseCard.autoRenewCheckbox.text = getString(
                R.string.auto_renew_formatted_text,
                mPackage.duration,
                "Day"
            )
        }
        else{
            binding.purchaseCard.validity.text = getString(R.string.day_formatted_text, mPackage.duration)
            binding.purchaseCard.autoRenewCheckbox.text = getString(
                R.string.auto_renew_formatted_text,
                mPackage.duration,
                "Days"
            )
        }


        val str = SpannableStringBuilder(
            getString(
                R.string.confirm_purchase_formatted_text,
                mPackage.price
            )
        )
        str.setSpan(
            android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            0,
            getString(R.string.amount_formatted_text, mPackage.price).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.purchaseCard.purchaseDescriptionText.text = str

    }
}
