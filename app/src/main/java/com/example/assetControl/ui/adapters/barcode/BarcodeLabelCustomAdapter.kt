package com.example.assetControl.ui.adapters.barcode

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.example.assetControl.ui.common.utils.Screen.Companion.isTablet
import java.util.*

class BarcodeLabelCustomAdapter(
    private var activity: AppCompatActivity,
    private var resource: Int,
    private var fullList: ArrayList<BarcodeLabelCustom>,
    private var suggestedList: ArrayList<BarcodeLabelCustom>,
) : ArrayAdapter<BarcodeLabelCustom>(context, resource, suggestedList),
    Filterable {

    private var checkedChangedListener: CheckedChangedListener? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    interface DataSetChangedListener {
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        fun onCheckedChanged(
            isChecked: Boolean,
            pos: Int,
        )
    }

    interface SelectedItemChangedListener {
        fun onSelectedItemChanged(
            item: BarcodeLabelCustom?,
            pos: Int,
        )
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    private fun getIndex(`object`: BarcodeLabelCustom): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as BarcodeLabelCustom)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun getItems(): ArrayList<BarcodeLabelCustom> {
        val r: ArrayList<BarcodeLabelCustom> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue

            r.add(t)
        }
        return r
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    fun getAll(): ArrayList<BarcodeLabelCustom> {
        val r: ArrayList<BarcodeLabelCustom> = ArrayList()
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

    fun countChecked(): Int {
        return checkedIdArray.count()
    }

    fun getAllChecked(): ArrayList<Long> {
        return checkedIdArray
    }

    private var isFilling = false
    fun setChecked(items: ArrayList<BarcodeLabelCustom>, isChecked: Boolean) {
        if (isFilling) {
            return
        }
        isFilling = true

        for (i in items) {
            setChecked(i, isChecked, true)
        }

        isFilling = false
        refresh()
    }

    fun setChecked(
        item: BarcodeLabelCustom,
        isChecked: Boolean,
        suspendRefresh: Boolean = false,
    ) {
        val position = getIndex(item)
        if (isChecked) {
            if (!checkedIdArray.contains(item.id)) {
                checkedIdArray.add(item.id)
            }
        } else {
            checkedIdArray.remove(item.id)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<BarcodeLabelCustom>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    private fun clearChecked() {
        checkedIdArray.clear()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(AssetControlApp.context)
            v = vi.inflate(this.resource, parent, false)

            alreadyExists = false
        }

        v = fillView(position, v!!, alreadyExists)
        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun fillView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ViewHolder()
        if (alreadyExists) {
            holder = v.tag as ViewHolder
        } else {
            holder.descriptionTextView = v.findViewById(R.id.barcodeLabelCustomStr)
            holder.checkBox = v.findViewById(R.id.checkBox)
            holder.checkBox?.visibility = GONE

            v.tag = holder
        }

        if (position >= 0) {
            val barcodeLabelCustom = getItem(position)

            if (barcodeLabelCustom != null) {
                if (barcodeLabelCustom.description.isEmpty()) {
                    holder.descriptionTextView?.text =
                        AssetControlApp.context.getString(R.string.no_description)
                } else {
                    holder.descriptionTextView?.text = barcodeLabelCustom.description
                }

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(barcodeLabelCustom, isChecked, false)
                        }

                    val pressHoldListener =
                        View.OnLongClickListener { // Do something when your hold starts here.
                            isSpeakButtonLongPressed = true
                            true
                        }

                    val pressTouchListener = View.OnTouchListener { pView, pEvent ->
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.setOnLongClickListener(null)
                    holder.checkBox!!.setOnTouchListener(null)
                    holder.checkBox!!.isChecked =
                        checkedIdArray.contains(barcodeLabelCustom.id)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

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
                val black = ResourcesCompat.getColor(
                    AssetControlApp.context.resources,
                    R.color.black,
                    null
                )
                val dimgray =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.dimgray,
                        null
                    )

                when {
                    barcodeLabelCustom.active != 1 -> {
                        v.setBackgroundColor(lightgray)
                        holder.descriptionTextView?.setTextColor(dimgray)

                    }

                    else -> {
                        v.setBackgroundColor(whitesmoke)
                        holder.descriptionTextView?.setTextColor(black)

                    }
                }
            }
        }

        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<BarcodeLabelCustom> = ArrayList()
                var filterString = ""

                if (constraint != null) {
                    filterString = constraint.toString().lowercase(Locale.getDefault())
                    var t: BarcodeLabelCustom

                    for (i in 0 until fullList.size) {
                        t = fullList[i]
                        if (t.description.contains(filterString, true)) {
                            r.add(t)
                        }
                    }
                }

                Collections.sort(r, ItemComparator(filterString))
                results.values = r
                results.count = r.count()
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                if (results != null && results.count > 0) {
                    suggestedList.addAll(results.values as ArrayList<BarcodeLabelCustom>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    internal class ViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    companion object {

        fun defaultRowHeight(): Int {
            return if (isTablet()) 45 else 90
        }

        class ItemComparator(private val priorityText: String) : Comparator<BarcodeLabelCustom> {
            fun compareNullable(o1: BarcodeLabelCustom?, o2: BarcodeLabelCustom?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: BarcodeLabelCustom, o2: BarcodeLabelCustom): Int {
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