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

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val ts = measureTimeMillis {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        }
//        Log.e("MEASURE_T", "onMeasure_DrawerLayout ->> $ts")
//    }
//
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        val ts = measureTimeMillis {
//            super.onLayout(changed, l, t, r, b)
//        }
//        Log.e("MEASURE_T", "onLayout_DrawerLayout ->> $ts")
//    }
//
//    override fun onDraw(c: Canvas?) {
//        val ts = measureTimeMillis {
//            super.onDraw(c)
//        }
//        Log.e("MEASURE_T", "onDraw_DrawerLayout ->> $ts")
//    }
}