package com.dacosys.assetControl.ui.adapters.asset

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.getColor
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.model.asset.Asset
import com.dacosys.assetControl.data.model.asset.AssetStatus
import com.dacosys.assetControl.ui.adapters.asset.AssetRecyclerAdapter.FilterOptions
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.isTablet
import java.util.*


class AssetAdapter(
    private var resource: Int,
    private var activity: AppCompatActivity,
    private var fullList: ArrayList<Asset>,
    private var suggestedList: ArrayList<Asset>,
    private var filterOptions: FilterOptions = FilterOptions(),
    private var visibleStatus: ArrayList<AssetStatus>
) : ArrayAdapter<Asset>(getContext(), R.layout.asset_simple_row, suggestedList), Filterable {

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    override fun sort(comparator: Comparator<in Asset>) {
        super.sort(customComparator)
    }

    private val customComparator = Comparator { o1: Asset?, o2: Asset? ->
        AssetComparator().compareNullable(o1, o2)
    }

    fun count(): Int {
        return count
    }

    fun countChecked(): Int {
        return checkedIdArray.count()
    }

    fun getAll(): ArrayList<Asset> {
        val r: ArrayList<Asset> = ArrayList()
        for (i in 0 until count) {
            r.add(getItem(i) as Asset)
        }
        return r
    }

    fun getAllChecked(): ArrayList<Long> {
        return checkedIdArray
    }

    private var isFilling = false
    fun setChecked(items: ArrayList<Asset>, isChecked: Boolean) {
        if (isFilling) return
        isFilling = true

        for (i in items) {
            setChecked(i, isChecked)
        }

        isFilling = false
        refresh()
    }

    fun setChecked(item: Asset, isChecked: Boolean, suspendRefresh: Boolean = false) {
        if (isChecked) {
            if (!checkedIdArray.contains(item.assetId)) {
                checkedIdArray.add(item.assetId)
            }
        } else {
            checkedIdArray.remove(item.assetId)
        }

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<Asset>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    private fun clearChecked() {
        checkedIdArray.clear()
    }

    private fun refresh() {
        activity.runOnUiThread { notifyDataSetChanged() }
    }

    fun maxHeightNeeded(): Int {
        return count() * defaultDropDownItemHeight()
    }

    private var multiSelect: Boolean = false
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val alreadyExists: Boolean

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)
            alreadyExists = false
        } else {
            alreadyExists = true
        }

        createViewHolder(position, v!!, alreadyExists)

        return v
    }

    private fun createViewHolder(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = SimpleViewHolder()
        if (alreadyExists) {
            holder = v.tag as SimpleViewHolder
        } else {
            holder.descriptionCheckedTextView = v.findViewById(R.id.descriptionCheckedTextView)
            holder.codeCheckedTextView = v.findViewById(R.id.codeCheckedTextView)
            holder.serialNumberCheckedTextView = v.findViewById(R.id.serialNumberCheckedTextView)
            holder.checkBox = v.findViewById(R.id.checkBox)

            if (multiSelect) {
                holder.checkBox?.visibility = VISIBLE
            } else {
                holder.checkBox?.visibility = GONE
            }

            v.tag = holder
        }

        return bind(position, v, holder)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bind(position: Int, v: View, holder: SimpleViewHolder): View {
        if (position >= 0) {
            val item = getItem(position)

            if (item != null) {
                holder.codeCheckedTextView?.text = item.code
                holder.descriptionCheckedTextView?.text = item.description
                holder.serialNumberCheckedTextView?.text = item.serialNumber ?: ""

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(item, isChecked, true)
                        }

                    val pressHoldListener =
                        OnLongClickListener { // Do something when your hold starts here.
                            isSpeakButtonLongPressed = true
                            true
                        }

                    val pressTouchListener = OnTouchListener { pView, pEvent ->
                        pView.onTouchEvent(pEvent)
                        // We're only interested in when the button is released.
                        if (pEvent.action == MotionEvent.ACTION_UP) {
                            // We're only interested in anything if our speak button is currently pressed.
                            if (isSpeakButtonLongPressed) {
                                // Do something when the button is released.
                                if (!isFilling) {
                                    holder.checkBox!!.setOnCheckedChangeListener(null)
                                    val newState = !holder.checkBox!!.isChecked
                                    this.setChecked(getAll(), newState)
                                }
                                isSpeakButtonLongPressed = false
                            }
                        }
                        return@OnTouchListener true
                    }

                    //Important to remove previous checkedChangedListener before calling setChecked
                    holder.checkBox?.setOnCheckedChangeListener(null)
                    holder.checkBox?.isChecked = checkedIdArray.contains(item.assetId)
                    holder.checkBox?.tag = position
                    holder.checkBox?.setOnLongClickListener(pressHoldListener)
                    holder.checkBox?.setOnTouchListener(pressTouchListener)
                    holder.checkBox?.setOnCheckedChangeListener(checkChangeListener)
                }

                setStyle(item, v, holder)
            }
        }

        if (v.height > 0) {
            viewHeight = v.height
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    private fun setStyle(item: Asset, v: View, holder: SimpleViewHolder) {
        // Background colors
        val lightgray = getColor(context.resources, R.color.lightgray, null)
        val whitesmoke = getColor(context.resources, R.color.whitesmoke, null)

        when (item.active) {
            true -> {
                v.setBackgroundColor(whitesmoke)
                holder.codeCheckedTextView?.setTextColor(defaultForeColor)
                holder.descriptionCheckedTextView?.setTextColor(defaultForeColor)
                holder.serialNumberCheckedTextView?.setTextColor(defaultForeColor)
            }

            else -> {
                v.setBackgroundColor(lightgray)
                holder.codeCheckedTextView?.setTextColor(inactiveForeColor)
                holder.descriptionCheckedTextView?.setTextColor(inactiveForeColor)
                holder.serialNumberCheckedTextView?.setTextColor(defaultForeColor)
            }
        }
    }

    //region COLORS

    private var selectedForeColor: Int = 0
    private var inactiveForeColor: Int = 0
    private var defaultForeColor: Int = 0

    private fun setupColors() {
        selectedForeColor = getColor(context.resources, R.color.text_light, null)
        inactiveForeColor = getBestContrastColor(getColor(context.resources, R.color.status_inactive, null))
        defaultForeColor = getBestContrastColor(getColor(context.resources, R.color.status_default, null))
    }

    //endregion

    internal inner class SimpleViewHolder {
        var codeCheckedTextView: CheckedTextView? = null
        var descriptionCheckedTextView: CheckedTextView? = null
        var serialNumberCheckedTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                var r: ArrayList<Asset> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: Asset

                        for (i in 0 until fullList.size) {
                            filterableItem = fullList[i]

                            // Descartamos aquellos que no debe ser visibles
                            if (filterableItem.assetStatus !in visibleStatus) continue

                            if (isFilterable(filterableItem, filterString)) {
                                r.add(filterableItem)
                            }
                        }
                    } else if (filterOptions.showAllOnFilterEmpty) {
                        r = ArrayList(suggestedList.map { it })
                    }
                }

                val s = sortItems(r)
                results.values = s
                results.count = s.count()
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                if (results != null && results.count > 0) {
                    suggestedList.addAll(results.values as ArrayList<Asset>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    companion object {
        fun defaultDropDownItemHeight(): Int {
            return viewHeight
        }

        var viewHeight = if (isTablet()) 251 else 143

        class AssetComparator(private val t: String) : Comparator<Asset> {
            constructor() : this("")

            fun compareNullable(o1: Asset?, o2: Asset?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: Asset, o2: Asset): Int {
                val firstField: Int
                val secondField: Int
                val thirdField: Int
                val fourthField: Int

                val fieldValue1: String = o1.code
                val fieldValue2: String = o2.code
                val secondValue1: String = o1.description
                val secondValue2: String = o2.description
                val thirdValue1: String = o1.ean ?: ""
                val thirdValue2: String = o2.ean ?: ""
                val fourthValue1: String = o1.serialNumber ?: ""
                val fourthValue2: String = o2.serialNumber ?: ""

                if (t.isNotEmpty()) {
                    firstField =
                        if (fieldValue2.startsWith(t, true) && !fieldValue1.startsWith(t, true))
                            return 1
                        else if (fieldValue1.startsWith(t, true) && !fieldValue2.startsWith(t, true))
                            return -1
                        else if (fieldValue1.startsWith(t, true) && fieldValue2.startsWith(t, true))
                            return fieldValue1.compareTo(fieldValue2)
                        else fieldValue1.compareTo(fieldValue2)

                    secondField =
                        if (secondValue2.startsWith(t, true) && !secondValue1.startsWith(t, true))
                            return 1
                        else if (secondValue1.startsWith(t, true) && !secondValue2.startsWith(t, true))
                            return -1
                        else if (secondValue1.startsWith(t, true) && secondValue2.startsWith(t, true))
                            return secondValue1.compareTo(secondValue2)
                        else secondValue1.compareTo(secondValue2)

                    thirdField =
                        if (thirdValue2.startsWith(t, true) && !thirdValue1.startsWith(t, true))
                            return 1
                        else if (thirdValue1.startsWith(t, true) && !thirdValue2.startsWith(t, true))
                            return -1
                        else if (thirdValue1.startsWith(t, true) && thirdValue2.startsWith(t, true))
                            return thirdValue1.compareTo(thirdValue2)
                        else thirdValue1.compareTo(thirdValue2)

                    fourthField =
                        if (fourthValue2.startsWith(t, true) && !fourthValue1.startsWith(t, true))
                            return 1
                        else if (fourthValue1.startsWith(t, true) && !fourthValue2.startsWith(t, true))
                            return -1
                        else if (fourthValue1.startsWith(t, true) && fourthValue2.startsWith(t, true))
                            return fourthValue1.compareTo(fourthValue2)
                        else fourthValue1.compareTo(fourthValue2)
                } else {
                    firstField = fieldValue1.compareTo(fieldValue2)
                    secondField = secondValue1.compareTo(secondValue2)
                    thirdField = thirdValue1.compareTo(thirdValue2)
                    fourthField = fourthValue1.compareTo(fourthValue2)
                }

                // Orden natural: code, serialNumber, ean
                return when (firstField) {
                    0 -> when (secondField) {
                        0 -> when (thirdField) {
                            0 -> fourthField
                            else -> thirdField
                        }

                        else -> secondField
                    }

                    else -> firstField
                }
            }
        }

        fun sortItems(originalList: ArrayList<Asset>): ArrayList<Asset> {
            // Get all the parent groups
            val groups = originalList.sortedWith(
                compareBy({ it.parentAssetId },
                    { it.code },
                    { it.description },
                    { it.serialNumber },
                    { it.ean })
            ).groupBy { it.parentAssetId }

            // Recursively get the children
            fun follow(asset: Asset): List<Asset> {
                return listOf(asset) + (groups[asset.assetId] ?: emptyList()).flatMap(::follow)
            }

            // Run the follow method on each of the roots
            return originalList.map { it.parentAssetId }
                .subtract(originalList.map { it.assetId }.toSet())
                .flatMap { groups[it] ?: emptyList() }.flatMap(::follow) as ArrayList<Asset>
        }

        fun isFilterable(filterableItem: Asset, filterString: String): Boolean =
            filterableItem.code.contains(filterString, true) ||
                    filterableItem.itemCategoryId.toString().contains(filterString) ||
                    (filterableItem.serialNumber ?: "").contains(filterString, true) ||
                    (filterableItem.ean ?: "").contains(filterString, true)
    }

    init {
        setupColors()
    }
}