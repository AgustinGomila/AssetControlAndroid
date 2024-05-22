package com.dacosys.assetControl.ui.adapters.datacollection

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import java.lang.ref.WeakReference

/**
 * Created by Agustin on 18/01/2017.
 */

class DataCollectionRuleAdapter : ArrayAdapter<DataCollectionRule> {

    private var activity: AppCompatActivity
    private var multiSelect: Boolean = false
    var listener: CustomCheckedChangeListener? = null
    private var positionArray: ArrayList<Boolean> = ArrayList()
    private var resource: Int = 0
    private var lastSelectedPos = -1

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        dcRules: List<DataCollectionRule>,
        listView: ListView?,
        multiSelect: Boolean,
        listener: CustomCheckedChangeListener?,
    ) : super(AssetControlApp.getContext(), resource, dcRules) {
        this.activity = activity
        this.listView = listView
        this.resource = resource
        this.multiSelect = multiSelect
        this.listener = listener
        for (i in dcRules.indices) {
            positionArray.add(false)
        }
    }

    interface CustomCheckedChangeListener {
        fun onCustomCheckedChangeListener(
            isChecked: Boolean,
            pos: Int,
        )
    }

    fun refreshListeners(listener: CustomCheckedChangeListener?) {
        this.listener = listener
    }

    private var weakRefListView: WeakReference<ListView?>? = null
        set(newValue) {
            field = newValue
            val l = listView
            if (l != null) {
                activity.runOnUiThread {
                    l.adapter = this
                }

                l.setOnItemClickListener { _, _, position, _ ->
                    val fv = firstVisiblePos()
                    val scroll = if (position < fv) position else fv
                    selectItem(position, scroll, false)
                }
            }
        }

    var listView: ListView?
        get() {
            return weakRefListView?.get()
        }
        set(newValue) {
            if (newValue == null) {
                return
            }
            weakRefListView = WeakReference(newValue)
        }

    override fun add(`object`: DataCollectionRule?) {
        super.add(`object`)
        if (`object` != null) {
            positionArray.add(false)
        }
    }

    override fun clear() {
        super.clear()
        positionArray.clear()
    }

    override fun remove(`object`: DataCollectionRule?) {
        if (`object` != null) {
            val i = getIndex(`object`)
            positionArray.removeAt(i)
        }

        super.remove(`object`)
    }

    private fun getIndex(`object`: DataCollectionRule): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as DataCollectionRule)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    fun setSelectItemAndScrollPos(dcr: DataCollectionRule?, tScrollPos: Int?) {
        var pos = -1
        if (dcr != null) pos = getPosition(dcr)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(dcr: DataCollectionRule?) {
        var pos = -1
        if (dcr != null) pos = getPosition(dcr)
        selectItem(pos)
    }

    fun selectItem(pos: Int) {
        selectItem(pos, pos, true)
    }

    private fun selectItem(pos: Int, scrollPos: Int, smoothScroll: Boolean) {
        if (listView == null) {
            return
        }

        listView?.clearChoices()

        // Quitar selección cuando:
        //   - Estaba previamente seleccionado
        //   - La posición es negativa
        //   - La cantidad de ítems es cero o menos

        activity.runOnUiThread {
            if (pos == lastSelectedPos || pos < 0 || count <= 0) {
                listView?.setItemChecked(-1, true)
                listView?.setSelection(-1)
            } else {
                listView?.setItemChecked(pos, true)
                listView?.setSelection(pos)
            }
        }

        lastSelectedPos = currentPos()

        activity.runOnUiThread {
            if (smoothScroll) {
                listView?.smoothScrollToPosition(scrollPos)
            } else {
                listView?.setSelection(scrollPos)
            }
        }
    }

    fun currentDataCollectionRule(): DataCollectionRule? {
        return (0 until count).firstOrNull { isSelected(it) }?.let {
            val t = getItem(it)
            t
        }
    }

    fun currentPos(): Int {
        return (0 until count).firstOrNull { isSelected(it) } ?: -1
    }

    fun firstVisiblePos(): Int {
        val lv = listView ?: return -1
        var pos = lv.firstVisiblePosition
        if (lv.childCount > 1 && lv.getChildAt(0).top < 0) pos++
        return pos
    }

    private fun isSelected(position: Int): Boolean {
        return position >= 0 && listView?.isItemChecked(position) ?: false
    }

    fun getItems(): ArrayList<DataCollectionRule> {
        val r: ArrayList<DataCollectionRule> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun setChecked(`object`: DataCollectionRule, isChecked: Boolean) {
        val i = getIndex(`object`)
        positionArray[i] = isChecked
    }

    fun count(): Int {
        return count
    }

    fun maxHeightNeeded(): Int {
        return count() * defaultDropDownItemHeight()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var v = convertView

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)

            holder = ViewHolder()
            if (resource == R.layout.data_collection_rule_row && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.dcrStr)
                holder.checkBox = v.findViewById(R.id.checkBox)
                v.tag = holder
            } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.descriptionTextView)
                v.tag = holder
            }
        } else {
            holder = v.tag as ViewHolder
        }

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        if (position >= 0) {
            val dataCollectionRule = getItem(position)

            if (dataCollectionRule != null) {
                holder.descriptionTextView?.text = dataCollectionRule.description

                if (holder.checkBox != null) {
                    if (positionArray.size - 1 < position) {
                        positionArray.add(false)
                    }

                    //Important to remove previous listener before calling setChecked
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = positionArray[position]
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnClickListener { }
                    holder.checkBox!!.setOnCheckedChangeListener { _, isChecked ->
                        positionArray[position] = isChecked

                        listener?.onCustomCheckedChangeListener(
                            isChecked, position
                        )
                    }
                }

                // Background colors
                val lightgray = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.lightgray, null
                )
                val whitesmoke = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.whitesmoke, null
                )

                // Font colors
                val black = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.text_dark, null
                )
                val dimgray = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.dimgray, null
                )

                if (resource == R.layout.data_collection_rule_row && v != null) {
                    when {
                        dataCollectionRule.active != 1 -> {
                            v.setBackgroundColor(lightgray)
                            holder.descriptionTextView?.setTextColor(dimgray)

                        }

                        else -> {
                            v.setBackgroundColor(whitesmoke)
                            holder.descriptionTextView?.setTextColor(black)

                        }
                    }

                    if (listView != null) {
                        if (listView!!.isItemChecked(position)) {
                            v.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    getColorWithAlpha(
                                        colorId = R.color.lightslategray, alpha = 240
                                    ), BlendModeCompat.MODULATE
                                )
                        } else {
                            v.background.colorFilter = null
                        }
                    }
                } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                    if (dataCollectionRule.active == 1 && dataCollectionRule.id > 0) {
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

    companion object {
        fun defaultDropDownItemHeight(): Int {
            return 93
        }
    }
}