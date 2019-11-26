package com.banglalink.toffee.ui.subscription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.View
import androidx.databinding.DataBindingUtil
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityConfirmPurchaseBinding
import com.banglalink.toffee.model.Package
import com.banglalink.toffee.ui.common.BaseAppCompatActivity

class SubscribePackageActivity : BaseAppCompatActivity() {

    companion object{
        const val PACKAGE = "PACAKGE"
    }
    lateinit var binding:ActivityConfirmPurchaseBinding
    lateinit var mPackage:Package

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPackage = intent.getSerializableExtra(PACKAGE) as Package
        binding = DataBindingUtil.setContentView(this,R.layout.activity_confirm_purchase)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        populateUI()
    }

    private fun populateUI() {
        binding.purchaseCard.packageNameTv.text = mPackage.packageName
        binding.purchaseCard.amount.text = getString(R.string.amount_formatted_text, mPackage.price)
        binding.purchaseCard.validity.text = getString(R.string.day_formatted_text, mPackage.duration)

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

        binding.purchaseCard.autoRenewCheckbox.text = getString(
            R.string.auto_renew_formatted_text,
            mPackage.duration,
            "Days"
        )
    }
}
