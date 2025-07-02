package com.dacosys.assetControl.ui.adapters.category

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.isTablet
import java.util.*

class ItemCategoryAdapter(
    private var resource: Int,
    private var icArray: ArrayList<ItemCategory>,
    private var suggestedList: ArrayList<ItemCategory>,
) : ArrayAdapter<ItemCategory>(context, resource, suggestedList),
    Filterable {

    fun getAllId(): ArrayList<Long> {
        val r: ArrayList<Long> = ArrayList()
        for (i in 0 until count) {
            val it = getItem(i)
            if (it != null) {
                r.add(it.id)
            }
        }
        return r
    }

    fun getAll(): ArrayList<ItemCategory> {
        val r: ArrayList<ItemCategory> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    override fun add(`object`: ItemCategory?) {
        super.add(`object`)
    }

    override fun remove(ic: ItemCategory?) {
        if (ic != null) {
            remove(arrayListOf(ic))
        }
    }

    fun remove(icArray: ArrayList<ItemCategory>) {
        val icRemoved: ArrayList<ItemCategory> = ArrayList()
        for (w in icArray) {
            if (getAll().contains(w)) {
                icRemoved.add(w)
                super.remove(w)
            }
        }
        refresh()
    }

    private fun getIndex(`object`: ItemCategory): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as ItemCategory)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun getItems(): ArrayList<ItemCategory> {
        val r: ArrayList<ItemCategory> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun count(): Int {
        return count
    }

    fun maxHeightNeeded(): Int {
        return count() * defaultDropDownItemHeight()
    }

    fun refresh() {
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var v = convertView

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)

            holder = ViewHolder()
            if (resource == R.layout.item_category_row && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.itemCategoryStr)
                holder.parentCategoryTextView = v.findViewById(R.id.parentStr)
                holder.checkBox = v.findViewById(R.id.checkBox)
                v.tag = holder
            } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.descriptionTextView)
                v.tag = holder
            }
        } else {
            holder = v.tag as ViewHolder
        }

        holder.checkBox?.visibility = GONE

        if (position >= 0) {
            val itemCategory = getItem(position)

            if (itemCategory != null) {
                holder.descriptionTextView?.text = itemCategory.description
                holder.parentCategoryTextView?.text = itemCategory.parentStr


                // Background colors
                val lightgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.lightgray,
                        null
                    )
                val whitesmoke =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.whitesmoke,
                        null
                    )

                // Font colors
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.text_dark,
                        null
                    )
                val dimgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.dimgray,
                        null
                    )

                if (resource == R.layout.item_category_row && v != null) {
                    when {
                        !itemCategory.active -> {
                            v.setBackgroundColor(lightgray)
                            holder.descriptionTextView?.setTextColor(dimgray)
                            holder.parentCategoryTextView?.setTextColor(dimgray)
                        }

                        else -> {
                            v.setBackgroundColor(whitesmoke)
                            holder.descriptionTextView?.setTextColor(black)
                            holder.parentCategoryTextView?.setTextColor(black)
                        }
                    }
                } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                    if (itemCategory.active && itemCategory.id > 0) {
                        holder.descriptionTextView?.setTextColor(black)
                    } else {
                        holder.descriptionTextView?.setTextColor(dimgray)
                    }
                }
            }
        }
        if ((v?.height ?: 0) > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v?.height}}-------")
        }
        return v!!
    }

    internal class ViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var parentCategoryTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    override fun sort(comparator: Comparator<in ItemCategory>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: ItemCategory?, o2: ItemCategory? ->
            ItemCategoryComparator().compareNullable(
                o1,
                o2
            )
        }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<ItemCategory> = ArrayList()
                var filterString = ""

                if (constraint != null) {
                    filterString = constraint.toString().lowercase(Locale.getDefault())
                    var filterableIc: ItemCategory

                    for (i in 0 until icArray.size) {
                        filterableIc = icArray[i]
                        if (
                            filterableIc.description.contains(filterString, true) ||
                            filterableIc.parentStr.contains(filterString, true)
                        ) {
                            r.add(filterableIc)
                        }
                    }
                }

                Collections.sort(r, ItemCategoryComparator(filterString))
                results.values = r
                results.count = r.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                if (results != null && results.count > 0) {
                    suggestedList.addAll(results.values as ArrayList<ItemCategory>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    companion object {
        fun defaultDropDownItemHeight(): Int {
            return if (isTablet()) 55 else 92
        }

        class ItemCategoryComparator(private val priorityText: String) : Comparator<ItemCategory> {
            constructor() : this("")

            fun compareNullable(o1: ItemCategory?, o2: ItemCategory?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: ItemCategory, o2: ItemCategory): Int {
                return try {
                    val firstField: Int
                    val secondField: Int

                    val fieldValue1: String = o1.description
                    val fieldValue2: String = o2.description
                    val secondValue1: String = o1.parentStr
                    val secondValue2: String = o2.parentStr

                    if (priorityText.isNotEmpty()) {
                        firstField =
                            if (fieldValue2.startsWith(priorityText, ignoreCase = true) &&
                                !fieldValue1.startsWith(priorityText, true)
                            ) (return 1)
                            else if (fieldValue1.startsWith(priorityText, true) &&
                                !fieldValue2.startsWith(priorityText, true)
                            ) (return -1)
                            else if (fieldValue1.startsWith(priorityText, true) &&
                                fieldValue2.startsWith(priorityText, true)
                            ) (return fieldValue1.compareTo(fieldValue2))
                            else (fieldValue1.compareTo(fieldValue2))

                        secondField =
                            if (secondValue2.startsWith(priorityText, ignoreCase = true) &&
                                !secondValue1.startsWith(priorityText, true)
                            ) (return 1)
                            else if (secondValue1.startsWith(priorityText, true) &&
                                !secondValue2.startsWith(priorityText, true)
                            ) (return -1)
                            else if (secondValue1.startsWith(priorityText, true) &&
                                secondValue2.startsWith(priorityText, true)
                            ) (return secondValue1.compareTo(secondValue2))
                            else (secondValue1.compareTo(secondValue2))
                    } else {
                        firstField = fieldValue1.compareTo(fieldValue2)
                        secondField = secondValue1.compareTo(secondValue2)
                    }

                    return when (firstField) {
                        0 -> secondField
                        else -> firstField
                    }
                } catch (ex: Exception) {
                    0
                }
            }
        }
    }
}