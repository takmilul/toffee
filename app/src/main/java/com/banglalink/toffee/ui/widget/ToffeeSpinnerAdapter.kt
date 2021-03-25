package com.banglalink.toffee.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R

class ToffeeSpinnerAdapter<T: Any?>(
    private val mContext: Context,
    private val title: String,
): BaseAdapter() {

    private val data: ArrayList<T?> = arrayListOf()
    var selectedItemPosition: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return data.size
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun setData(items: List<T?>) {
        data.clear()
        data.add(null)
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): T? {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource( position, convertView, parent, R.layout.layout_spinner)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?, layoutResourceId: Int): View {
        val retView = convertView ?: LayoutInflater.from(mContext).inflate(layoutResourceId, parent, false)

        try {
            val item = getItem(position)

            if(retView is TextView) {
                retView.text = item?.toString() ?: title
            }
            else {
                val parentTv:TextView = retView.findViewById(R.id.tv_parent_name)
                val closeIv:ImageView = retView.findViewById(R.id.close_iv)
                val line1:LinearLayout = retView.findViewById(R.id.line1)
                val tvName:TextView = retView.findViewById(R.id.tv_name)
                val check:ImageView = retView.findViewById(R.id.check)

                if (position == 0) {
                    parentTv.visibility = View.VISIBLE
                    closeIv.visibility = View.VISIBLE
                    line1.visibility = View.GONE
                    parentTv.text = title
                } else {
                    parentTv.visibility = View.GONE
                    closeIv.visibility = View.GONE
                    line1.visibility = View.VISIBLE
                    tvName.text = item.toString()
                }

                check.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        if (position == selectedItemPosition)
                            R.drawable.ic_radio_checked
                        else
                            R.drawable.ic_radio_unchecked
                    )
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return retView
    }
}