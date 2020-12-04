package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class ToffeeDrawerLayout @JvmOverloads constructor(context: Context,
                         attrs: AttributeSet? = null,
                         defStyleAttr: Int = 0
): DrawerLayout(context, attrs, defStyleAttr) {
    override fun open() {
        openDrawer(GravityCompat.END)
    }

    override fun close() {
        closeDrawer(GravityCompat.END)
    }
}