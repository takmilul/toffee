package com.banglalink.toffee.ui.widget

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.banglalink.toffee.ui.widget.StickyHeaderGridAdapter.ViewHolder
import java.security.InvalidParameterException

/**
 * Created by Sergej Kravcenko on 4/24/2017.
 * Copyright (c) 2017 Sergej Kravcenko
 */

abstract class StickyHeaderGridAdapter : Adapter<ViewHolder>() {
    private var mSections: ArrayList<Section>? = null
    private var mSectionIndices = intArrayOf()
    private var mTotalItemNumber = 0
    
    companion object {
        const val TAG = "StickyHeaderGridAdapter"
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        private fun internalViewType(type: Int): Int {
            return type and 0xFF
        }
        
        private fun externalViewType(type: Int): Int {
            return type shr 8
        }
    }
    
    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        open val isHeader: Boolean
            get() = false
        val sectionItemViewType: Int
            get() = externalViewType(itemViewType)
    }
    
    open class ItemViewHolder(itemView: View?) : ViewHolder(itemView)
    open class HeaderViewHolder(itemView: View?) : ViewHolder(itemView) {
        override val isHeader: Boolean
            get() = true
    }
    
    private class Section {
        var position = 0
        var itemNumber = 0
        var length = 0
    }
    
    private fun calculateSections() {
        val sectionCount = sectionCount
        var total = 0
        mSections = ArrayList(sectionCount)
        for (s in 0 until sectionCount) {
            val section = Section()
            section.position = total
            section.itemNumber = getSectionItemCount(s)
            section.length = section.itemNumber + 1
            mSections!!.add(section)
            total += section.length
        }
        mTotalItemNumber = total
        total = 0
        mSectionIndices = IntArray(mTotalItemNumber)
        for (s in 0 until sectionCount) {
            val section = mSections!![s]
            for (i in 0 until section.length) {
                mSectionIndices[total + i] = s
            }
            total += section.length
        }
    }
    
    fun getItemViewInternalType(position: Int): Int {
        val section = getAdapterPositionSection(position)
        val sectionObject = mSections!![section]
        val sectionPosition = position - sectionObject.position
        return getItemViewInternalType(section, sectionPosition)
    }
    
    private fun getItemViewInternalType(section: Int, position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }
    
    override fun getItemCount(): Int {
        if (mSections == null) {
            calculateSections()
        }
        return mTotalItemNumber
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val internalType = internalViewType(viewType)
        val externalType = externalViewType(viewType)
        return when (internalType) {
            TYPE_HEADER -> onCreateHeaderViewHolder(parent, externalType)
            TYPE_ITEM -> onCreateItemViewHolder(parent, externalType)
            else -> throw InvalidParameterException("Invalid viewType: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mSections == null) {
            calculateSections()
        }
        val section = mSectionIndices[position]
        val internalType = internalViewType(holder.itemViewType)
        val externalType = externalViewType(holder.itemViewType)
        when (internalType) {
            TYPE_HEADER -> onBindHeaderViewHolder(holder as HeaderViewHolder, section)
            TYPE_ITEM -> {
                val itemHolder = holder as ItemViewHolder
                val offset = getItemSectionOffset(section, position)
                onBindItemViewHolder(holder, section, offset)
            }
            else -> throw InvalidParameterException("invalid viewType: $internalType")
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        val section = getAdapterPositionSection(position)
        val sectionObject = mSections!![section]
        val sectionPosition = position - sectionObject.position
        val internalType = getItemViewInternalType(section, sectionPosition)
        var externalType = 0
        when (internalType) {
            TYPE_HEADER -> externalType = getSectionHeaderViewType(section)
            TYPE_ITEM -> externalType = getSectionItemViewType(section, sectionPosition - 1)
        }
        return externalType and 0xFF shl 8 or (internalType and 0xFF)
    }
    
    // Helpers
    private fun getItemSectionHeaderPosition(position: Int): Int {
        return getSectionHeaderPosition(getAdapterPositionSection(position))
    }
    
    private fun getAdapterPosition(section: Int, offset: Int): Int {
        if (mSections == null) {
            calculateSections()
        }
        if (section < 0) {
            throw IndexOutOfBoundsException("section $section < 0")
        }
        if (section >= mSections!!.size) {
            throw IndexOutOfBoundsException("section " + section + " >=" + mSections!!.size)
        }
        val sectionObject = mSections!![section]
        return sectionObject.position + offset
    }
    
    /**
     * Given a `section` and an adapter `position` get the offset of an item
     * inside `section`.
     *
     * @param section section to query
     * @param position adapter position
     * @return The item offset inside the section.
     */
    fun getItemSectionOffset(section: Int, position: Int): Int {
        if (mSections == null) {
            calculateSections()
        }
        if (section < 0) {
            throw IndexOutOfBoundsException("section $section < 0")
        }
        if (section >= mSections!!.size) {
            throw IndexOutOfBoundsException("section " + section + " >=" + mSections!!.size)
        }
        val sectionObject = mSections!![section]
        val localPosition = position - sectionObject.position
        if (localPosition >= sectionObject.length) {
            throw IndexOutOfBoundsException("localPosition: " + localPosition + " >=" + sectionObject.length)
        }
        return localPosition - 1
    }
    
    /**
     * Returns the section index having item or header with provided
     * provider `position`.
     *
     * @param position adapter position
     * @return The section containing provided adapter position.
     */
    fun getAdapterPositionSection(position: Int): Int {
        if (mSections == null) {
            calculateSections()
        }
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        if (position < 0) {
            throw IndexOutOfBoundsException("position $position < 0")
        }
        if (position >= itemCount) {
            throw IndexOutOfBoundsException("position $position >=$itemCount")
        }
        return mSectionIndices[position]
    }
    
    /**
     * Returns the adapter position for given `section` header. Use
     * this only for [RecyclerView.scrollToPosition] or similar functions.
     * Never directly manipulate adapter items using this position.
     *
     * @param section section to query
     * @return The adapter position.
     */
    fun getSectionHeaderPosition(section: Int): Int {
        return getAdapterPosition(section, 0)
    }
    
    /**
     * Returns the adapter position for given `section` and
     * `offset`. Use this only for [RecyclerView.scrollToPosition]
     * or similar functions. Never directly manipulate adapter items using this position.
     *
     * @param section section to query
     * @param position item position inside the `section`
     * @return The adapter position.
     */
    fun getSectionItemPosition(section: Int, position: Int): Int {
        return getAdapterPosition(section, position + 1)
    }
    // Overrides
    /**
     * Returns the total number of sections in the data set held by the adapter.
     *
     * @return The total number of section in this adapter.
     */
    open val sectionCount: Int
        get() = 0
    
    /**
     * Returns the number of items in the `section`.
     *
     * @param section section to query
     * @return The total number of items in the `section`.
     */
    open fun getSectionItemCount(section: Int): Int {
        return 0
    }
    
    /**
     * Return the view type of the `section` header for the purposes
     * of view recycling.
     *
     *
     * The default implementation of this method returns 0, making the assumption of
     * a single view type for the headers. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param section section to query
     * @return integer value identifying the type of the view needed to represent the header in
     * `section`. Type codes need not be contiguous.
     */
    open fun getSectionHeaderViewType(section: Int): Int {
        return 0
    }
    
    /**
     * Return the view type of the item at `position` in `section` for
     * the purposes of view recycling.
     *
     *
     * The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Unlike ListView adapters, types need not
     * be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param section section to query
     * @param offset section position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * `position` in `section`. Type codes need not be
     * contiguous.
     */
    fun getSectionItemViewType(section: Int, offset: Int): Int {
        return 0
    }
    
    /**
     * Returns true if header in `section` is sticky.
     *
     * @param section section to query
     * @return true if `section` header is sticky.
     */
    fun isSectionHeaderSticky(section: Int): Boolean {
        return true
    }
    
    /**
     * Called when RecyclerView needs a new [HeaderViewHolder] of the given type to represent
     * a header.
     *
     *
     * This new HeaderViewHolder should be constructed with a new View that can represent the headers
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new HeaderViewHolder will be used to display items of the adapter using
     * [.onBindHeaderViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param headerType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getSectionHeaderViewType
     * @see .onBindHeaderViewHolder
     */
    abstract fun onCreateHeaderViewHolder(parent: ViewGroup, headerType: Int): HeaderViewHolder
    
    /**
     * Called when RecyclerView needs a new [ItemViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindItemViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param itemType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getSectionItemViewType
     * @see .onBindItemViewHolder
     */
    abstract fun onCreateItemViewHolder(parent: ViewGroup, itemType: Int): ItemViewHolder
    
    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [HeaderViewHolder.itemView] to reflect the header at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the header changes in the data set unless the header itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `section` parameter while acquiring the
     * related header data inside this method and should not keep a copy of it. If you need the
     * position of a header later on (e.g. in a click listener), use
     * [HeaderViewHolder.getAdapterPosition] which will have the updated adapter
     * position. Then you can use [.getAdapterPositionSection] to get section index.
     *
     *
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     * header at the given position in the data set.
     * @param section The index of the section.
     */
    abstract fun onBindHeaderViewHolder(viewHolder: HeaderViewHolder, section: Int)
    
    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ItemViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `offset` and `section` parameters while acquiring the
     * related data item inside this method and should not keep a copy of it. If you need the
     * position of an item later on (e.g. in a click listener), use
     * [ItemViewHolder.getAdapterPosition] which will have the updated adapter
     * position. Then you can use [.getAdapterPositionSection] and
     * [.getItemSectionOffset]
     *
     *
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param section The index of the section.
     * @param offset The position of the item within the section.
     */
    abstract fun onBindItemViewHolder(viewHolder: ItemViewHolder, section: Int, offset: Int)
    // Notify
    /**
     * Notify any registered observers that the data set has changed.
     *
     *
     * There are two different classes of data change events, item changes and structural
     * changes. Item changes are when a single item has its data updated but no positional
     * changes have occurred. Structural changes are when items are inserted, removed or moved
     * within the data set.
     *
     *
     * This event does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * LayoutManagers will be forced to fully rebind and relayout all visible views.
     *
     *
     * `RecyclerView` will attempt to synthesize visible structural change events
     * for adapters that report that they have [stable IDs][.hasStableIds] when
     * this method is used. This can help for the purposes of animation and visual
     * object persistence but individual item views will still need to be rebound
     * and relaid out.
     *
     *
     * If you are writing an adapter it will always be more efficient to use the more
     * specific change events if you can. Rely on `notifyDataSetChanged()`
     * as a last resort.
     *
     * @see .notifySectionDataSetChanged
     * @see .notifySectionHeaderChanged
     * @see .notifySectionItemChanged
     * @see .notifySectionInserted
     * @see .notifySectionItemInserted
     * @see .notifySectionItemRangeInserted
     * @see .notifySectionRemoved
     * @see .notifySectionItemRemoved
     * @see .notifySectionItemRangeRemoved
     */
    fun notifyAllSectionsDataSetChanged() {
        calculateSections()
        notifyDataSetChanged()
    }
    
    fun notifySectionDataSetChanged(section: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            notifyItemRangeChanged(sectionObject.position, sectionObject.length)
        }
    }
    
    fun notifySectionHeaderChanged(section: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            notifyItemRangeChanged(sectionObject.position, 1)
        }
    }
    
    fun notifySectionItemChanged(section: Int, position: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            if (position >= sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + position + ", size is " + sectionObject.itemNumber)
            }
            notifyItemChanged(sectionObject.position + position + 1)
        }
    }
    
    fun notifySectionInserted(section: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            notifyItemRangeInserted(sectionObject.position, sectionObject.length)
        }
    }
    
    fun notifySectionItemInserted(section: Int, position: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            if (position < 0 || position >= sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + position + ", size is " + sectionObject.itemNumber)
            }
            notifyItemInserted(sectionObject.position + position + 1)
        }
    }
    
    fun notifySectionItemRangeInserted(section: Int, position: Int, count: Int) {
        calculateSections()
        if (mSections == null) {
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            if (position < 0 || position >= sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + position + ", size is " + sectionObject.itemNumber)
            }
            if (position + count > sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + (position + count) + ", size is " + sectionObject.itemNumber)
            }
            notifyItemRangeInserted(sectionObject.position + position + 1, count)
        }
    }
    
    fun notifySectionRemoved(section: Int) {
        if (mSections == null) {
            calculateSections()
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            calculateSections()
            notifyItemRangeRemoved(sectionObject.position, sectionObject.length)
        }
    }
    
    fun notifySectionItemRemoved(section: Int, position: Int) {
        if (mSections == null) {
            calculateSections()
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            if (position < 0 || position >= sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + position + ", size is " + sectionObject.itemNumber)
            }
            calculateSections()
            notifyItemRemoved(sectionObject.position + position + 1)
        }
    }
    
    fun notifySectionItemRangeRemoved(section: Int, position: Int, count: Int) {
        if (mSections == null) {
            calculateSections()
            notifyAllSectionsDataSetChanged()
        } else {
            val sectionObject = mSections!![section]
            if (position < 0 || position >= sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + position + ", size is " + sectionObject.itemNumber)
            }
            if (position + count > sectionObject.itemNumber) {
                throw IndexOutOfBoundsException("Invalid index " + (position + count) + ", size is " + sectionObject.itemNumber)
            }
            calculateSections()
            notifyItemRangeRemoved(sectionObject.position + position + 1, count)
        }
    }
}