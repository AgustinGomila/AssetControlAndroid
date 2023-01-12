package com.dacosys.assetControl.adapters.manteinance

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
import com.dacosys.assetControl.model.manteinance.AssetManteinance
import com.dacosys.assetControl.model.manteinance.ManteinanceStatus
import com.dacosys.assetControl.utils.Statics.Companion.getColorWithAlpha
import java.lang.ref.WeakReference

/**
 * Created by Agustin on 18/01/2017.
 */

class AssetManteinanceAdapter : ArrayAdapter<AssetManteinance> {

    private var lastSelectedPos = -1

    private var activity: AppCompatActivity
    private var resource: Int
    private var multiSelect: Boolean
    private var positionArray: ArrayList<Boolean>
    var listener: CustomCheckedChangeListener?

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        assets: List<AssetManteinance>,
        listView: ListView?,
        multiSelect: Boolean,
        listener: CustomCheckedChangeListener?,
    ) : super(AssetControlApp.getContext(), resource, assets) {
        this.activity = activity
        this.listView = listView
        this.resource = resource
        this.multiSelect = multiSelect
        this.listener = listener
        this.positionArray = ArrayList()
        for (i in assets.indices) {
            positionArray.add(false)
        }
    }

    fun refreshListeners(
        customCheckedChangeListener: CustomCheckedChangeListener?,
    ) {
        this.listener = customCheckedChangeListener
    }

    interface CustomCheckedChangeListener {
        // Define data you like to return from AysncTask
        fun onCustomCheckedChangeListener(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(`object`: AssetManteinance?) {
        super.add(`object`)
        if (`object` != null) {
            positionArray.add(false)
        }
    }

    override fun clear() {
        super.clear()
        positionArray.clear()
    }

    override fun remove(`object`: AssetManteinance?) {
        if (`object` != null) {
            val i = getIndex(`object`)
            positionArray.removeAt(i)
        }

        super.remove(`object`)
    }

    private fun getIndex(`object`: AssetManteinance): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as AssetManteinance)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun setChecked(`object`: AssetManteinance, isChecked: Boolean) {
        val i = getIndex(`object`)
        positionArray[i] = isChecked
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    fun setSelectItemAndScrollPos(am: AssetManteinance?, tScrollPos: Int?) {
        var pos = -1
        if (am != null) pos = getPosition(am)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(am: AssetManteinance?) {
        var pos = -1
        if (am != null) pos = getPosition(am)
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

        // Deseleccionar cuando:
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

        refresh()

        activity.runOnUiThread {
            if (smoothScroll) {
                listView?.smoothScrollToPosition(scrollPos)
            } else {
                listView?.setSelection(scrollPos)
            }
        }
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
                    selectItem(position)
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

    fun currentAssetManteinance(): AssetManteinance? {
        return (0 until count)
            .firstOrNull { isSelected(it) }
            ?.let {
                val t = getItem(it)
                t
            }
    }

    fun currentPos(): Int {
        return (0 until count)
            .firstOrNull { isSelected(it) } ?: -1
    }

    fun firstVisiblePos(): Int {
        var pos = (listView ?: return -1).firstVisiblePosition
        if ((listView ?: return -1).childCount > 1 && (listView
                ?: return -1).getChildAt(0).top < 0
        ) pos++
        return pos
    }

    private fun isSelected(position: Int): Boolean {
        return position >= 0 && listView?.isItemChecked(position) ?: false
    }

    fun getItems(): ArrayList<AssetManteinance> {
        val r: ArrayList<AssetManteinance> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var v = convertView

        if (v == null || v.tag == null) {
            val vi = LayoutInflater.from(context)
            v = vi.inflate(this.resource, parent, false)

            holder = ViewHolder()
            if (resource == R.layout.asset_manteinance_row && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.description)
                holder.codeTextView = v.findViewById(R.id.code)
                holder.manteinanceStatusTextView = v.findViewById(R.id.manteinanceStatus)
                holder.manteinanceTypeTextView = v.findViewById(R.id.manteinanceType)
                holder.checkBox = v.findViewById(R.id.checkBox)
                v.tag = holder
            } else if (resource == R.layout.custom_spinner_dropdown_item && v != null) {
                holder.descriptionTextView = v.findViewById(R.id.descriptionTextView)
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
            val assetManteinance = getItem(position)

            if (assetManteinance != null) {
                holder.descriptionTextView?.text = assetManteinance.assetStr
                holder.codeTextView?.text = assetManteinance.assetCode
                holder.manteinanceStatusTextView?.text =
                    assetManteinance.manteinanceStatus!!.description
                holder.manteinanceTypeTextView?.text = assetManteinance.manteinanceTypeStr

                // Background colors
                val firebrick =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.firebrick,
                        null
                    )
                val seagreen =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.seagreen,
                        null
                    )
                val gold = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources,
                    R.color.gold,
                    null
                )
                val lightblue =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.lightblue,
                        null
                    )
                val darkseagreen =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkseagreen,
                        null
                    )
                val darkslategray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkslategray,
                        null
                    )
                val darkred =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkred,
                        null
                    )
                val dodgerblue =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.dodgerblue,
                        null
                    )
                val darkcyan =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkcyan,
                        null
                    )
                val crimson =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.crimson,
                        null
                    )

                // Font colors
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_dark,
                        null
                    )
                val white =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_light,
                        null
                    )

                /*
                var repair = ManteinanceStatus(1, "Para reparar")
                var income = ManteinanceStatus(2, "Ingresado")
                var underDiagnosis = ManteinanceStatus(3, "En diagnóstico")
                var diagnosed = ManteinanceStatus(4, "Diagnosticado")
                var cost = ManteinanceStatus(5, "Presupuestado")
                var approvedCost = ManteinanceStatus(6, "Presupuesto aprobado")
                var underRepair = ManteinanceStatus(7, "En reparación")
                var repaired = ManteinanceStatus(8, "Reparado")
                var finished = ManteinanceStatus(9, "Finalizado")
                var repairImposible = ManteinanceStatus(10, "Imposible reparar")
                 */

                if (v != null) {
                    var backColor = white
                    var foreColor = black
                    val isSelected = isSelected(position)
                    when (assetManteinance.manteinanceStatusId) {
                        ManteinanceStatus.repair.id -> {
                            backColor = firebrick
                            foreColor = white
                        }
                        ManteinanceStatus.income.id -> {
                            backColor = seagreen
                            foreColor = white
                        }
                        ManteinanceStatus.underDiagnosis.id -> {
                            backColor = gold
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }
                        ManteinanceStatus.diagnosed.id -> {
                            backColor = lightblue
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }
                        ManteinanceStatus.cost.id -> {
                            backColor = darkseagreen
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }
                        ManteinanceStatus.approvedCost.id -> {
                            backColor = darkslategray
                            foreColor = white
                        }
                        ManteinanceStatus.underRepair.id -> {
                            backColor = darkred
                            foreColor = white
                        }
                        ManteinanceStatus.repaired.id -> {
                            backColor = dodgerblue
                            foreColor = white
                        }
                        ManteinanceStatus.finished.id -> {
                            backColor = darkcyan
                            foreColor = white
                        }
                        ManteinanceStatus.repairImposible.id -> {
                            backColor = crimson
                            foreColor = white
                        }
                    }

                    v.setBackgroundColor(backColor)
                    holder.descriptionTextView?.setTextColor(foreColor)
                    holder.codeTextView?.setTextColor(foreColor)
                    holder.manteinanceStatusTextView?.setTextColor(foreColor)
                    holder.manteinanceTypeTextView?.setTextColor(foreColor)
                }
            }

            if (listView!!.isItemChecked(position)) {
                v!!.background.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
                        BlendModeCompat.MODULATE
                    )
            } else {
                v!!.background.colorFilter = null
            }
        }
        if ((v?.height ?: 0) > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v?.height}}-------")
        }
        return v!!
    }

    internal class ViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var codeTextView: CheckedTextView? = null
        var manteinanceStatusTextView: CheckedTextView? = null
        var manteinanceTypeTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }
}