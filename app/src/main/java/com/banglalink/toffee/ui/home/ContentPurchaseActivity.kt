package com.banglalink.toffee.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.banglalink.toffee.R
import com.banglalink.toffee.util.unsafeLazy

class ContentPurchaseActivity : AppCompatActivity() {

    private val toolbar by unsafeLazy {
        findViewById<Toolbar>(R.id.toolbar)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_purchase_activity)

        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}
