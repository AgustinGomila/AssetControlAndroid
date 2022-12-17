package com.dacosys.assetControl.model.locations.warehouse.dbHelper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.utils.Statics
import java.util.*

/**
 * Created by Agustin on 18/01/2017.
 */

class WarehouseAdapter(
    private var resource: Int,
    private var wArray: ArrayList<Warehouse>,
    private var suggestedList: ArrayList<Warehouse>,
) : ArrayAdapter<Warehouse>(getContext(), resource, suggestedList),
    Filterable {

    override fun add(`object`: Warehouse?) {
        super.add(`object`)
    }

    override fun remove(`object`: Warehouse?) {
        super.remove(`object`)
    }

    private fun getIndex(`object`: Warehouse): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as Warehouse)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun getItems(): ArrayList<Warehouse> {
        val r: ArrayList<Warehouse> = ArrayList()
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
        return count() * defaultRowHeight()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var v = convertView

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)

            holder = ViewHolder()
            if (resource == R.layout.warehouse_row && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.warehouseStr)
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
            val warehouse = getItem(position)

            if (warehouse != null) {
                holder.descriptionTextView?.text = warehouse.description


                // Background colors
                val lightgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.lightgray,
                        null
                    )
                val whitesmoke =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.whitesmoke,
                        null
                    )

                // Font colors
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_dark,
                        null
                    )
                val dimgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.dimgray,
                        null
                    )

                if (resource == R.layout.warehouse_row && v != null) {
                    when {
                        !warehouse.active -> {
                            v.setBackgroundColor(lightgray)
                            holder.descriptionTextView?.setTextColor(dimgray)

                        }
                        else -> {
                            v.setBackgroundColor(whitesmoke)
                            holder.descriptionTextView?.setTextColor(black)

                        }
                    }
                } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                    if (warehouse.active && warehouse.warehouseId > 0) {
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
        var checkBox: CheckBox? = null
    }

    override fun sort(comparator: Comparator<in Warehouse>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: Warehouse?, o2: Warehouse? ->
            WarehouseComparator().compareNullable(
                o1,
                o2
            )
        }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<Warehouse> = ArrayList()
                var filterString = ""

                if (constraint != null) {
                    filterString = constraint.toString().lowercase(Locale.getDefault())
                    var filterableIc: Warehouse

                    for (i in 0 until wArray.size) {
                        filterableIc = wArray[i]
                        if (filterableIc.description.contains(filterString, true)) {
                            r.add(filterableIc)
                        }
                    }
                }

                Collections.sort(r, WarehouseComparator(filterString))
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
                    suggestedList.addAll(results.values as ArrayList<Warehouse>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    companion object {
        fun defaultRowHeight(): Int {
            return if (Statics.isTablet()) 45 else 90
        }

        class WarehouseComparator(private val priorityText: String) : Comparator<Warehouse> {
            constructor() : this("")

            fun compareNullable(o1: Warehouse?, o2: Warehouse?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: Warehouse, o2: Warehouse): Int {
                return try {
                    val firstField: Int

                    val fieldValue1: String = o1.description
                    val fieldValue2: String = o2.description

                    firstField = if (priorityText.isNotEmpty()) {
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
                    } else {
                        fieldValue1.compareTo(fieldValue2)
                    }

                    return firstField
                } catch (ex: Exception) {
                    0
                }
            }
        }
    }
}