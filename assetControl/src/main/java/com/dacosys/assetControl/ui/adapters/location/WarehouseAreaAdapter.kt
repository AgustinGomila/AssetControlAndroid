package com.dacosys.assetControl.ui.adapters.location

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.utils.Screen.Companion.isTablet
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Agustin on 18/01/2017.
 */

class WarehouseAreaAdapter : ArrayAdapter<WarehouseArea>, Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    private var multiSelect: Boolean = false
    var suspendReport = false

    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    private var lastSelectedPos = -1
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var waArray: ArrayList<WarehouseArea> = ArrayList()
    private var suggestedList: ArrayList<WarehouseArea> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        warehouseAreas: ArrayList<WarehouseArea>,
        listView: ListView?,
        checkedIdArray: ArrayList<Long>,
        multiSelect: Boolean,
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) : super(AssetControlApp.getContext(), resource, warehouseAreas) {
        this.activity = activity
        this.resource = resource
        this.multiSelect = multiSelect
        this.checkedIdArray = checkedIdArray
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
        this.listView = listView
        this.waArray = warehouseAreas
    }

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        warehouseAreas: ArrayList<WarehouseArea>,
        suggestedList: ArrayList<WarehouseArea>,
    ) : super(AssetControlApp.getContext(), resource, suggestedList) {
        this.activity = activity
        this.resource = resource
        this.waArray = warehouseAreas
        this.suggestedList = suggestedList
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    interface DataSetChangedListener {
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        fun onCheckedChanged(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(wa: WarehouseArea?) {
        if (wa != null) {
            if (!getAll().contains(wa)) {
                activity.runOnUiThread {
                    super.add(wa)
                }

                reportWarehouseAreaAdded(arrayListOf(wa))
            }
        }
    }

    fun add(assets: ArrayList<WarehouseArea>) {
        val assetsAdded: ArrayList<WarehouseArea> = ArrayList()

        activity.runOnUiThread {
            for (w in assets) {
                if (!getAll().contains(w)) {
                    assetsAdded.add(w)
                    super.add(w)
                }
            }

            if (assetsAdded.size > 0) {
                reportWarehouseAreaAdded(assetsAdded)
            }
        }
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    override fun remove(warehouseArea: WarehouseArea?) {
        if (warehouseArea != null) {
            remove(arrayListOf(warehouseArea))
        }
    }

    fun remove(warehouseAreas: ArrayList<WarehouseArea>) {
        lastSelectedPos = currentPos()

        val warehouseAreasRemoved: ArrayList<WarehouseArea> = ArrayList()
        activity.runOnUiThread {
            for (w in warehouseAreas) {
                if (getAll().contains(w)) {
                    warehouseAreasRemoved.add(w)
                    checkedIdArray.remove(w.warehouseAreaId)
                    super.remove(w)
                }
            }

            if (warehouseAreasRemoved.size > 0) {
                reportWarehouseAreaRemoved(warehouseAreasRemoved)
            }
        }

        activity.runOnUiThread { sort(customComparator) }

        refresh()

        activity.runOnUiThread {
            listView?.smoothScrollToPosition(lastSelectedPos)
        }
    }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
        dataSetChangedListener?.onDataSetChanged()
    }

    fun setSelectItemAndScrollPos(wa: WarehouseArea?, tScrollPos: Int?) {
        var pos = -1
        if (wa != null) pos = getPosition(wa)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(wa: WarehouseArea?) {
        var pos = -1
        if (wa != null) pos = getPosition(wa)
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

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportWarehouseAreaAdded(warehouseAreaArray: ArrayList<WarehouseArea>) {
        if (suspendReport) {
            return
        }
        if (warehouseAreaArray.size <= 0) {
            return
        }

        var res = ""
        for (warehouseArea in warehouseAreaArray) {
            res += "${warehouseArea.description}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (warehouseAreaArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.added_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackBarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportWarehouseAreaRemoved(warehouseAreaArray: ArrayList<WarehouseArea>) {
        // En modo arqueo no se muestran los carteles de eliminación de warehouseAreas
        // porque nunca se eliminan sino que se ponen en cero
        if (suspendReport) {
            return
        }
        if (warehouseAreaArray.size <= 0) {
            return
        }

        var res = ""
        for (warehouseArea in warehouseAreaArray) {
            res += "${warehouseArea.description}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (warehouseAreaArray.size > 1)
                    " ${AssetControlApp.getContext().getString(R.string.removed_plural)}" else
                    " ${AssetControlApp.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackBarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    fun currentWarehouseArea(): WarehouseArea? {
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

    private fun getIndex(`object`: WarehouseArea): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as WarehouseArea)
            if (t == `object`) {
                return i
            }
        }
        return -1
    }

    fun count(): Int {
        return count
    }

    fun maxHeightNeeded(): Int {
        return count() * defaultRowHeight()
    }

    fun getAllId(): ArrayList<Long> {
        val r: ArrayList<Long> = ArrayList()
        for (i in 0 until count) {
            val it = getItem(i)
            if (it != null) {
                r.add(it.warehouseAreaId)
            }
        }
        return r
    }

    fun getAll(): ArrayList<WarehouseArea> {
        val r: ArrayList<WarehouseArea> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun countChecked(): Int {
        return checkedIdArray.size
    }

    fun getAllChecked(): ArrayList<Long> {
        return checkedIdArray
    }

    private var isFilling = false
    fun setChecked(items: ArrayList<WarehouseArea>, isChecked: Boolean) {
        if (isFilling) {
            return
        }
        isFilling = true

        for (i in items) {
            setChecked(i, isChecked)
        }

        isFilling = false
        refresh()
    }

    fun setChecked(item: WarehouseArea, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val position = getIndex(item)
        if (isChecked) {
            if (!checkedIdArray.contains(item.warehouseAreaId)) {
                checkedIdArray.add(item.warehouseAreaId)
            }
        } else {
            checkedIdArray.remove(item.warehouseAreaId)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<WarehouseArea>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    fun clearChecked() {
        checkedIdArray.clear()
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        val currentLayout: Int = R.layout.warehouse_area_row
        if (v == null || v.tag == null) {
            // El view todavía no fue creado, crearlo con el layout correspondiente.

            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)
            alreadyExists = false
        } else if (
            v.tag is String && currentLayout == R.layout.warehouse_area_row ||
            v.tag is ViewHolder && currentLayout != R.layout.warehouse_area_row
        ) {
            // Ya fue creado, si es un row normal que está siendo seleccionada
            // o un row expandido que está siendo deseleccionado
            // debe cambiar de layout, por lo tanto volver a crearse.

            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)
            alreadyExists = false
        }

        v = when (currentLayout) {
            R.layout.null_row -> fillNullView(v!!)
            else -> fillListView(position, v!!, alreadyExists)
        }
        return v
    }

    private fun fillNullView(v: View): View {
        v.tag = ""
        return v
    }

    private fun fillListView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ViewHolder()
        if (alreadyExists) {
            holder = v.tag as ViewHolder
        } else {
            holder.descriptionTextView = v.findViewById(R.id.warehouseAreaStr)
            holder.warehouseTextView = v.findViewById(R.id.warehouseStr)
            holder.checkBox = v.findViewById(R.id.checkBox)

            if (multiSelect) {
                holder.checkBox?.visibility = VISIBLE
            } else {
                holder.checkBox?.visibility = GONE
            }

            v.tag = holder
        }

        if (position >= 0) {
            val warehouseArea = getItem(position)

            if (warehouseArea != null) {
                holder.descriptionTextView?.text = warehouseArea.description
                holder.warehouseTextView?.text = warehouseArea.warehouseStr

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(warehouseArea, isChecked, true)
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
                    holder.checkBox!!.isChecked =
                        checkedIdArray.contains(warehouseArea.warehouseAreaId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Font colors
                val white =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_light,
                        null
                    )
                val black =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.text_dark,
                        null
                    )

                // CheckBox color
                val darkslategray =
                    ResourcesCompat.getColor(
                        AssetControlApp.getContext().resources,
                        R.color.darkslategray,
                        null
                    )
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

                val foreColor: Int
                val titleForeColor: Int
                val isSelected = isSelected(position)
                when {
                    !warehouseArea.active -> {
                        v.setBackgroundColor(lightgray)
                        foreColor = if (isSelected) {
                            white
                        } else {
                            black
                        }
                        titleForeColor = if (isSelected) {
                            lightgray
                        } else {
                            darkslategray
                        }
                    }

                    else -> {
                        v.setBackgroundColor(whitesmoke)
                        foreColor = if (isSelected) {
                            white
                        } else {
                            black
                        }
                        titleForeColor = if (isSelected) {
                            lightgray
                        } else {
                            darkslategray
                        }
                    }
                }
                holder.descriptionTextView?.setTextColor(foreColor)
                holder.warehouseTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(
                                colorId = R.color.lightslategray,
                                alpha = 240
                            ),
                            BlendModeCompat.MODULATE
                        )
                } else {
                    v.background.colorFilter = null
                }
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    internal class ViewHolder {
        var descriptionTextView: CheckedTextView? = null
        var warehouseTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
    }

    override fun sort(comparator: Comparator<in WarehouseArea>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: WarehouseArea?, o2: WarehouseArea? ->
            WarehouseAreaComparator().compareNullable(
                o1,
                o2
            )
        }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<WarehouseArea> = ArrayList()
                var filterString = ""

                if (constraint != null) {
                    filterString = constraint.toString().lowercase(Locale.getDefault())
                    var filterableIc: WarehouseArea

                    for (i in 0 until waArray.size) {
                        filterableIc = waArray[i]
                        if (
                            filterableIc.description.contains(filterString, true) ||
                            filterableIc.warehouseStr.contains(filterString, true)
                        ) {
                            r.add(filterableIc)
                        }
                    }
                }

                Collections.sort(r, WarehouseAreaComparator(filterString))
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
                    suggestedList.addAll(results.values as ArrayList<WarehouseArea>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    companion object {

        fun defaultRowHeight(): Int {
            return if (isTablet()) 58 else 127
        }

        class WarehouseAreaComparator(private val priorityText: String) :
            Comparator<WarehouseArea> {
            constructor() : this("")

            fun compareNullable(o1: WarehouseArea?, o2: WarehouseArea?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: WarehouseArea, o2: WarehouseArea): Int {
                return try {
                    val firstField: Int
                    val secondField: Int

                    val fieldValue1: String = o1.description
                    val fieldValue2: String = o2.description
                    val secondValue1: String = o1.warehouseStr
                    val secondValue2: String = o2.warehouseStr

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