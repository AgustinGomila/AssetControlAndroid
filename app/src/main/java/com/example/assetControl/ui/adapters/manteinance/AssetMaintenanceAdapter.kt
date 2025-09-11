package com.example.assetControl.ui.adapters.manteinance

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
import com.example.assetControl.AssetControlApp
import com.example.assetControl.R
import com.example.assetControl.data.enums.maintenance.MaintenanceStatus
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenance
import com.example.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import java.lang.ref.WeakReference

class AssetMaintenanceAdapter : ArrayAdapter<AssetMaintenance> {

    private var lastSelectedPos = -1

    private var activity: AppCompatActivity
    private var resource: Int
    private var multiSelect: Boolean
    private var positionArray: ArrayList<Boolean>
    var listener: CustomCheckedChangeListener?

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        assets: List<AssetMaintenance>,
        listView: ListView?,
        multiSelect: Boolean,
        listener: CustomCheckedChangeListener?,
    ) : super(AssetControlApp.context, resource, assets) {
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
        fun onCustomCheckedChangeListener(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(`object`: AssetMaintenance?) {
        super.add(`object`)
        if (`object` != null) {
            positionArray.add(false)
        }
    }

    override fun clear() {
        super.clear()
        positionArray.clear()
    }

    override fun remove(`object`: AssetMaintenance?) {
        if (`object` != null) {
            val i = getIndex(`object`)
            positionArray.removeAt(i)
        }

        super.remove(`object`)
    }

    private fun getIndex(`object`: AssetMaintenance): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as AssetMaintenance)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun setChecked(`object`: AssetMaintenance, isChecked: Boolean) {
        val i = getIndex(`object`)
        positionArray[i] = isChecked
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
    }

    fun setSelectItemAndScrollPos(am: AssetMaintenance?, tScrollPos: Int?) {
        var pos = -1
        if (am != null) pos = getPosition(am)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(am: AssetMaintenance?) {
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

    fun currentAssetMaintenance(): AssetMaintenance? {
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
        val lv = listView ?: return -1
        var pos = lv.firstVisiblePosition
        if (lv.childCount > 1 && lv.getChildAt(0).top < 0) pos++
        return pos
    }

    private fun isSelected(position: Int): Boolean {
        return position >= 0 && listView?.isItemChecked(position) == true
    }

    fun getItems(): ArrayList<AssetMaintenance> {
        val r: ArrayList<AssetMaintenance> = ArrayList()
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
                holder.maintenanceStatusTextView = v.findViewById(R.id.manteinanceStatus)
                holder.maintenanceTypeTextView = v.findViewById(R.id.manteinanceType)
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
            val maintenance = getItem(position)

            if (maintenance != null) {
                holder.descriptionTextView?.text = maintenance.assetStr
                holder.codeTextView?.text = maintenance.assetCode
                holder.maintenanceStatusTextView?.text = maintenance.maintenanceStatus.description
                holder.maintenanceTypeTextView?.text = maintenance.maintenanceTypeStr

                // Background colors
                val firebrick =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.firebrick,
                        null
                    )
                val seagreen =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.seagreen,
                        null
                    )
                val gold = ResourcesCompat.getColor(
                    AssetControlApp.context.resources,
                    R.color.gold,
                    null
                )
                val lightblue =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.lightblue,
                        null
                    )
                val darkseagreen =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.darkseagreen,
                        null
                    )
                val darkslategray =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.darkslategray,
                        null
                    )
                val darkred =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.darkred,
                        null
                    )
                val dodgerblue =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.dodgerblue,
                        null
                    )
                val darkcyan =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.darkcyan,
                        null
                    )
                val crimson =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.crimson,
                        null
                    )

                // Font colors
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.text_dark,
                        null
                    )
                val white =
                    ResourcesCompat.getColor(
                        AssetControlApp.context.resources,
                        R.color.text_light,
                        null
                    )

                /*
                var repair = MaintenanceStatus(1, "Para reparar")
                var income = MaintenanceStatus(2, "Ingresado")
                var underDiagnosis = MaintenanceStatus(3, "En diagnóstico")
                var diagnosed = MaintenanceStatus(4, "Diagnosticado")
                var cost = MaintenanceStatus(5, "Presupuestado")
                var approvedCost = MaintenanceStatus(6, "Presupuesto aprobado")
                var underRepair = MaintenanceStatus(7, "En reparación")
                var repaired = MaintenanceStatus(8, "Reparado")
                var finished = MaintenanceStatus(9, "Finalizado")
                var repairImposible = MaintenanceStatus(10, "Imposible reparar")
                 */

                if (v != null) {
                    var backColor = white
                    var foreColor = black
                    val isSelected = isSelected(position)
                    when (maintenance.maintenanceStatusId) {
                        MaintenanceStatus.repair.id -> {
                            backColor = firebrick
                            foreColor = white
                        }

                        MaintenanceStatus.income.id -> {
                            backColor = seagreen
                            foreColor = white
                        }

                        MaintenanceStatus.underDiagnosis.id -> {
                            backColor = gold
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }

                        MaintenanceStatus.diagnosed.id -> {
                            backColor = lightblue
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }

                        MaintenanceStatus.cost.id -> {
                            backColor = darkseagreen
                            foreColor = if (isSelected) {
                                white
                            } else {
                                black
                            }
                        }

                        MaintenanceStatus.approvedCost.id -> {
                            backColor = darkslategray
                            foreColor = white
                        }

                        MaintenanceStatus.underRepair.id -> {
                            backColor = darkred
                            foreColor = white
                        }

                        MaintenanceStatus.repaired.id -> {
                            backColor = dodgerblue
                            foreColor = white
                        }

                        MaintenanceStatus.finished.id -> {
                            backColor = darkcyan
                            foreColor = white
                        }

                        MaintenanceStatus.repairImposible.id -> {
                            backColor = crimson
                            foreColor = white
                        }
                    }

                    v.setBackgroundColor(backColor)
                    holder.descriptionTextView?.setTextColor(foreColor)
                    holder.codeTextView?.setTextColor(foreColor)
                    holder.maintenanceStatusTextView?.setTextColor(foreColor)
                    holder.maintenanceTypeTextView?.setTextColor(foreColor)
                }
            }

            if (listView!!.isItemChecked(position)) {
                v!!.background.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColorWithAlpha(
                            colorId = R.color.lightslategray,
                            alpha = 240
                        ),
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
        var maintenanceStatusTextView: CheckedTextView? = null
        var maintenanceTypeTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }
}