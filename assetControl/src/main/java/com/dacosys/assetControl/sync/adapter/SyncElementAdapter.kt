package com.dacosys.assetControl.sync.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.getColorWithAlpha
import com.dacosys.assetControl.utils.Statics.Companion.manipulateColor
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.`object`.AssetManteinance
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.movements.warehouseMovement.`object`.WarehouseMovement
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.`object`.DataCollection
import com.dacosys.assetControl.model.routes.routeProcess.`object`.RouteProcess
import com.dacosys.assetControl.sync.functions.SyncRegistryType
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.commons.views.AutoResizeTextView
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by Agustin on 18/01/2017.
 */

@Suppress("SpellCheckingInspection", "DEPRECATION")
@SuppressLint("ClickableViewAccessibility", "ObsoleteSdkInt")
class SyncElementAdapter :
    ArrayAdapter<Any>,
    Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    var suspendReport = false
    private var lastSelectedPos = -1
    private var checkedDefault = true

    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    private var multiSelect: Boolean = false

    private var syncElementArray: ArrayList<Any> = ArrayList()
    private var suggestedList: ArrayList<Any> = ArrayList()

    private var visibleRegistry: ArrayList<SyncRegistryType> = ArrayList()
    private var checkedIdArray: ArrayList<String> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        syncElements: ArrayList<Any>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<String>,
        visibleRegistry: ArrayList<SyncRegistryType>,
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,

        ) : super(Statics.AssetControl.getContext(), resource, syncElements) {
        this.activity = activity
        this.resource = resource
        this.multiSelect = multiSelect

        this.visibleRegistry = visibleRegistry
        this.checkedIdArray = checkedIdArray

        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener

        this.listView = listView
        this.syncElementArray = syncElements

        setupColors()
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    interface DataSetChangedListener {
        // Define data you like to return from AysncTask
        fun onDataSetChanged()
    }

    interface CheckedChangedListener {
        // Define data you like to return from AysncTask
        fun onCheckedChanged(
            isChecked: Boolean,
            pos: Int,
        )
    }

    override fun add(syncElement: Any?) {
        if (syncElement != null) {
            if (!getAll().contains(syncElement)) {
                activity.runOnUiThread {
                    super.add(syncElement)
                    if (checkedDefault) {
                        val key = getKey(syncElement)
                        if (!checkedIdArray.contains(key)) {
                            checkedIdArray.add(key)
                        }
                    }
                }

                reportItemAdded(arrayListOf(syncElement))
            }
        }
    }

    fun add(syncElements: ArrayList<Any>) {
        val syncElementsAdded: ArrayList<Any> = ArrayList()

        activity.runOnUiThread {
            for (w in syncElements) {
                if (!getAll().contains(w)) {
                    syncElementsAdded.add(w)
                    super.add(w)
                    if (checkedDefault) {
                        val key = getKey(w)
                        if (!checkedIdArray.contains(key)) {
                            checkedIdArray.add(key)
                        }
                    }
                }
            }

            if (syncElementsAdded.size > 0) {
                reportItemAdded(syncElementsAdded)
            }
        }
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    override fun remove(syncElement: Any?) {
        if (syncElement != null) {
            remove(arrayListOf(syncElement))
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

    fun remove(syncElements: ArrayList<Any>) {
        lastSelectedPos = currentPos()

        val syncElementsRemoved: ArrayList<Any> = ArrayList()
        activity.runOnUiThread {
            for (w in syncElements) {
                if (getAll().contains(w)) {
                    syncElementsRemoved.add(w)
                    checkedIdArray.remove(getKey(w))
                    super.remove(w)
                }
            }

            if (syncElementsRemoved.size > 0) {
                reportItemRemoved(syncElementsRemoved)
            }
        }

        activity.runOnUiThread { sort(customComparator) }

        refresh()

        activity.runOnUiThread {
            listView?.smoothScrollToPosition(lastSelectedPos)
        }
    }

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportItemAdded(syncElementArray: ArrayList<Any>) {
        if (suspendReport) {
            return
        }
        if (syncElementArray.size <= 0) {
            return
        }

        var res = ""
        for (syncElement in syncElementArray) {
            when (syncElement) {
                is Asset -> {
                    res += "${syncElement.code}, "
                }
                is ItemCategory -> {
                    res += "${syncElement.description}, "
                }
                is Warehouse -> {
                    res += "${syncElement.description}, "
                }
                is WarehouseArea -> {
                    res += "${syncElement.description}, "
                }
                is AssetManteinance -> {
                    res += "${syncElement.manteinanceTypeStr}, "
                }
                is DataCollection -> {
                    res += "${syncElement.assetId} ${syncElement.dateEnd}, "
                }
                is RouteProcess -> {
                    res += "${syncElement.routeId} ${syncElement.routeProcessDate}, "
                }
                is WarehouseMovement -> {
                    res += "${syncElement.destWarehouseAreaId}, "
                }
                is AssetReview -> {
                    res += "${syncElement.warehouseAreaStr}, "
                }
            }
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (syncElementArray.size > 1)
                    " ${Statics.AssetControl.getContext().getString(R.string.added_plural)}" else
                    " ${Statics.AssetControl.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackbarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportItemRemoved(syncElementArray: ArrayList<Any>) {
        // En modo arqueo no se muestran los carteles de eliminación de syncElements
        // porque nunca se eliminan sino que se ponen en cero
        if (suspendReport) {
            return
        }
        if (syncElementArray.size <= 0) {
            return
        }

        var res = ""
        for (syncElement in syncElementArray) {
            when (syncElement) {
                is Asset -> {
                    res += "${syncElement.code}, "
                }
                is ItemCategory -> {
                    res += "${syncElement.description}, "
                }
                is Warehouse -> {
                    res += "${syncElement.description}, "
                }
                is WarehouseArea -> {
                    res += "${syncElement.description}, "
                }
                is AssetManteinance -> {
                    res += "${syncElement.manteinanceTypeStr}, "
                }
                is DataCollection -> {
                    res += "${syncElement.assetId} ${syncElement.dateEnd}, "
                }
                is RouteProcess -> {
                    res += "${syncElement.routeId} ${syncElement.routeProcessDate}, "
                }
                is WarehouseMovement -> {
                    res += "${syncElement.destWarehouseAreaId}, "
                }
                is AssetReview -> {
                    res += "${syncElement.warehouseAreaStr}, "
                }
            }
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (syncElementArray.size > 1)
                    " ${Statics.AssetControl.getContext().getString(R.string.removed_plural)}" else
                    " ${Statics.AssetControl.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackbarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    private fun getIndex(syncElement: Any): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as Any)
            if (t == syncElement) {
                return i
            }
        }
        return -1
    }

    fun count(): Int {
        return count
    }

    fun getAll(): ArrayList<Any> {
        val r: ArrayList<Any> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun countChecked(): Int {
        return checkedIdArray.size
    }

    fun getAllChecked(): ArrayList<String> {
        return checkedIdArray
    }

    private var isFilling = false
    fun setChecked(items: ArrayList<Any>, isChecked: Boolean) {
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

    private fun getKey(item: Any): String {
        var regId = ""
        var itemId = ""

        when (item) {
            is Asset -> {
                regId = SyncRegistryType.Asset.id.toString()
                itemId = item.assetId.toString()
            }
            is ItemCategory -> {
                regId = SyncRegistryType.ItemCategory.id.toString()
                itemId = item.itemCategoryId.toString()
            }
            is Warehouse -> {
                regId = SyncRegistryType.Warehouse.id.toString()
                itemId = item.warehouseId.toString()
            }
            is WarehouseArea -> {
                regId = SyncRegistryType.WarehouseArea.id.toString()
                itemId = item.warehouseAreaId.toString()
            }
            is AssetManteinance -> {
                regId = SyncRegistryType.AssetManteinance.id.toString()
                itemId = item.collectorAssetManteinanceId.toString()
            }
            is DataCollection -> {
                regId = SyncRegistryType.DataCollection.id.toString()
                itemId = item.collectorDataCollectionId.toString()
            }
            is RouteProcess -> {
                regId = SyncRegistryType.RouteProcess.id.toString()
                itemId = item.collectorRouteProcessId.toString()
            }
            is WarehouseMovement -> {
                regId = SyncRegistryType.WarehouseMovement.id.toString()
                itemId = item.collectorWarehouseMovementId.toString()
            }
            is AssetReview -> {
                regId = SyncRegistryType.AssetReview.id.toString()
                itemId = item.collectorAssetReviewId.toString()
            }
        }

        val regIdStr = String.format("%0" + (4 - regId.length).toString() + "d%s", 0, regId)
        val itemIdStr = String.format("%0" + (11 - itemId.length).toString() + "d%s", 0, itemId)
        return "${regIdStr}${itemIdStr}"
    }

    fun setChecked(item: Any, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val position = getIndex(item)
        val key = getKey(item)

        if (isChecked) {
            if (!checkedIdArray.contains(key)) {
                checkedIdArray.add(key)
            }
        } else {
            checkedIdArray.remove(key)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    fun setChecked(checkedItems: ArrayList<Any>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    fun clearChecked() {
        checkedIdArray.clear()
    }

    override fun sort(comparator: Comparator<in Any>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: Any?, o2: Any? ->
            ItemComparator().compareNullable(
                o1,
                o2
            )
        }

    fun refresh() {
        activity.runOnUiThread {
            notifyDataSetChanged()
        }
        dataSetChangedListener?.onDataSetChanged()
    }

    fun setSelectItemAndScrollPos(pos: Int?, tScrollPos: Int?) {
        var tPos = -1
        if (pos != null) tPos = pos
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(tPos, scrollPos, false)
    }

    fun selectItem(a: Any?) {
        var pos = -1
        if (a != null) pos = getPosition(a)
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

    fun currentSyncElement(): Any? {
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

    fun isStatusVisible(position: Int): Boolean {
        if (position < 0) return false
        return isStatusVisible(getItem(position))
    }

    fun isStatusVisible(arc: Any?): Boolean {
        return if (arc != null) {
            when (arc) {
                is Asset -> {
                    return visibleRegistry.contains(SyncRegistryType.Asset)
                }
                is ItemCategory -> {
                    return visibleRegistry.contains(SyncRegistryType.ItemCategory)
                }
                is Warehouse -> {
                    return visibleRegistry.contains(SyncRegistryType.Warehouse)
                }
                is WarehouseArea -> {
                    return visibleRegistry.contains(SyncRegistryType.WarehouseArea)
                }
                is AssetManteinance -> {
                    return visibleRegistry.contains(SyncRegistryType.AssetManteinance)
                }
                is DataCollection -> {
                    return visibleRegistry.contains(SyncRegistryType.DataCollection)
                }
                is RouteProcess -> {
                    return visibleRegistry.contains(SyncRegistryType.RouteProcess)
                }
                is WarehouseMovement -> {
                    return visibleRegistry.contains(SyncRegistryType.WarehouseMovement)
                }
                is AssetReview -> {
                    return visibleRegistry.contains(SyncRegistryType.AssetReview)
                }
                else -> return false
            }
        } else false
    }

    fun getVisibleRegistry(): ArrayList<SyncRegistryType> {
        return visibleRegistry
    }

    fun setVisibleRegistry(status: ArrayList<SyncRegistryType>) {
        visibleRegistry = status
        refresh()
    }

    fun addVisibleRegistry(status: SyncRegistryType) {
        if (!visibleRegistry.contains(status)) {
            visibleRegistry.add(status)
            refresh()
        }
    }

    fun removeVisibleRegistry(status: SyncRegistryType) {
        if (visibleRegistry.contains(status)) {
            visibleRegistry.remove(status)
            refresh()
        }
    }

    fun selectNearVisible() {
        val currentPos = currentPos()
        var newPos = -1

        val allItems = getAll()
        if (allItems.size > 0) {
            // Buscar el siguiente visible
            for (i in currentPos until allItems.size) {
                if (isStatusVisible(i)) {
                    newPos = i
                    break
                }
            }

            // No encontró otro visible hacia adelante...
            // ir hacia atrás
            if (newPos == -1) {
                // Buscar el anterior visible
                for (i in currentPos downTo 1) {
                    if (isStatusVisible(i)) {
                        newPos = i
                        break
                    }
                }
            }
        }

        selectItem(newPos)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        // Seleccionamos el layout dependiendo si es
        // un row visible u oculto según su AsseStatus.

        val currentLayout: Int =
            if (listView == null) {
                // Estamos trabajando en un Dropdown
                R.layout.null_row
            } else when {
                // Estamos trabajando en un ListView
                !isStatusVisible(position) -> R.layout.null_row
                isSelected(position) -> {
                    when (val it = getItem(position)) {
                        null -> {
                            R.layout.null_row
                        }
                        else -> {
                            when (it) {
                                is Asset -> {
                                    R.layout.sync_element_asset_row
                                }
                                is ItemCategory -> {
                                    R.layout.sync_element_item_category_row
                                }
                                is Warehouse -> {
                                    R.layout.sync_element_warehouse_row
                                }
                                is WarehouseArea -> {
                                    R.layout.sync_element_warehouse_area_row
                                }
                                /*is AssetManteinance -> {
                                    R.layout.sync_element_asset_manteinance_row
                                }*/
                                is DataCollection -> {
                                    R.layout.sync_element_data_collection_row
                                }
                                is RouteProcess -> {
                                    R.layout.sync_element_route_process_row
                                }
                                is WarehouseMovement -> {
                                    R.layout.sync_element_warehouse_movement_row
                                }
                                is AssetReview -> {
                                    R.layout.sync_element_asset_review_row
                                }
                                else -> {
                                    R.layout.null_row
                                }
                            }
                        }
                    }
                }
                else -> {
                    when (val it = getItem(position)) {
                        null -> {
                            R.layout.null_row
                        }
                        else -> {
                            when (it) {
                                is Asset -> {
                                    R.layout.sync_element_asset_row
                                }
                                is ItemCategory -> {
                                    R.layout.sync_element_item_category_row
                                }
                                is Warehouse -> {
                                    R.layout.sync_element_warehouse_row
                                }
                                is WarehouseArea -> {
                                    R.layout.sync_element_warehouse_area_row
                                }
                                /* is AssetManteinance -> {
                                     R.layout.sync_element_asset_manteinance_row
                                 }*/
                                is DataCollection -> {
                                    R.layout.sync_element_data_collection_row
                                }
                                is RouteProcess -> {
                                    R.layout.sync_element_route_process_row
                                }
                                is WarehouseMovement -> {
                                    R.layout.sync_element_warehouse_movement_row
                                }
                                is AssetReview -> {
                                    R.layout.sync_element_asset_review_row
                                }
                                else -> {
                                    R.layout.null_row
                                }
                            }
                        }
                    }
                }
            }

        if (v == null || v.tag == null) {
            // El view todavía no fue creado, crearlo con el layout correspondiente.
            val vi = LayoutInflater.from(context)
            v = vi.inflate(currentLayout, parent, false)

            alreadyExists = false
        } else {
            // El view ya existe, comprobar que no necesite cambiar de layout.
            if (
            // Row null cambiando...
                v.tag is String ||
                (v.tag is AssetViewHolder && currentLayout != R.layout.sync_element_asset_row) ||
                (v.tag is ItemCategoryViewHolder && currentLayout != R.layout.sync_element_item_category_row) ||
                (v.tag is WarehouseViewHolder && currentLayout != R.layout.sync_element_warehouse_row) ||
                (v.tag is WarehouseAreaViewHolder && currentLayout != R.layout.sync_element_warehouse_area_row) ||
                (v.tag is WarehouseViewHolder && currentLayout != R.layout.sync_element_warehouse_row) ||
                /*(v.tag is AssetManteinanceViewHolder && currentLayout != R.layout.sync_element_asset_manteinance_row) ||*/
                (v.tag is DataCollectionViewHolder && currentLayout != R.layout.sync_element_data_collection_row) ||
                (v.tag is RouteProcessViewHolder && currentLayout != R.layout.sync_element_route_process_row) ||
                (v.tag is WarehouseMovementViewHolder && currentLayout != R.layout.sync_element_warehouse_movement_row) ||
                (v.tag is AssetReviewViewHolder && currentLayout != R.layout.sync_element_asset_review_row)
            ) {
                // Ya fue creado, si es un row normal que está siendo seleccionada
                // o un row expandido que está siendo deseleccionado
                // debe cambiar de layout, por lo tanto volver a crearse.
                val vi = LayoutInflater.from(context)
                v = vi.inflate(currentLayout, parent, false)

                alreadyExists = false
            }
        }

        v = when (currentLayout) {
            R.layout.null_row -> fillNullView(v!!)
            R.layout.sync_element_asset_row -> getAssetItemView(position, v!!, alreadyExists)
            R.layout.sync_element_item_category_row -> getItemCategoryItemView(
                position,
                v!!,
                alreadyExists
            )
            R.layout.sync_element_warehouse_row -> getWarehouseItemView(
                position,
                v!!,
                alreadyExists
            )
            R.layout.sync_element_warehouse_area_row -> getWarehouseAreaItemView(
                position,
                v!!,
                alreadyExists
            )
            /*R.layout.sync_element_asset_manteinance_row -> getAssetManteinanceItemView(position, v!!, alreadyExists)*/
            R.layout.sync_element_data_collection_row -> getDataCollectionItemView(
                position,
                v!!,
                alreadyExists
            )
            R.layout.sync_element_route_process_row -> getRouteProcessItemView(
                position,
                v!!,
                alreadyExists
            )
            R.layout.sync_element_warehouse_movement_row -> getWarehouseMovementItemView(
                position,
                v!!,
                alreadyExists
            )
            R.layout.sync_element_asset_review_row -> getAssetReviewItemView(
                position,
                v!!,
                alreadyExists
            )
            else -> fillNullView(v!!)
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    private fun fillNullView(v: View): View {
        v.tag = ""
        return v
    }

    private fun getWarehouseAreaItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = WarehouseAreaViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createWarehouseAreaViewHolder(v, holder)
            } else {
                holder = v.tag as WarehouseAreaViewHolder
            }
        } else {
            createWarehouseAreaViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is WarehouseArea) {
                holder.descriptionTextView?.text = syncElement.description
                holder.warehouseTextView?.text = syncElement.warehouseStr

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Set layouts
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.descriptionTextView?.setTextColor(col.foreColor)
                holder.warehouseTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    private fun getWarehouseItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = WarehouseViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createWarehouseViewHolder(v, holder)
            } else {
                holder = v.tag as WarehouseViewHolder
            }
        } else {
            createWarehouseViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is Warehouse) {
                holder.descriptionTextView?.text = syncElement.description

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Set layouts
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.descriptionTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)

                if (listView != null) {
                    if (listView!!.isItemChecked(position)) {
                        v.background.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
                                BlendModeCompat.MODULATE
                            )
                    } else {
                        v.background.colorFilter = null
                    }
                }
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
        return v
    }

    private fun getItemCategoryItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ItemCategoryViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createItemCategoryViewHolder(v, holder)
            } else {
                holder = v.tag as ItemCategoryViewHolder
            }
        } else {
            createItemCategoryViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is ItemCategory) {
                holder.descriptionTextView?.text = syncElement.description
                holder.parentCategoryTextView?.text = syncElement.parentStr

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Set layouts
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.descriptionTextView?.setTextColor(col.foreColor)
                holder.parentCategoryTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)

                if (listView != null) {
                    if (listView!!.isItemChecked(position)) {
                        v.background.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
                                BlendModeCompat.MODULATE
                            )
                    } else {
                        v.background.colorFilter = null
                    }
                }
            }
        }
        return v
    }

    private fun getDataCollectionItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = DataCollectionViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createDataCollectionViewHolder(v, holder)
            } else {
                holder = v.tag as DataCollectionViewHolder
            }
        } else {
            createDataCollectionViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is DataCollection) {
                holder.targetStrTextView?.text =
                    when {
                        syncElement.assetCode.isNotEmpty() -> {
                            syncElement.assetCode
                        }
                        syncElement.warehouseStr.isNotEmpty() -> {
                            syncElement.warehouseStr
                        }
                        syncElement.warehouseAreaStr.isNotEmpty() -> {
                            syncElement.warehouseAreaStr
                        }
                        else -> {
                            ""
                        }
                    }
                holder.dataCollectionDateTextView?.text = syncElement.dateEnd

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Set layouts
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.targetStrTextView?.setTextColor(col.foreColor)
                holder.dataCollectionDateTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    private fun getRouteProcessItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = RouteProcessViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createRouteProcessViewHolder(v, holder)
            } else {
                holder = v.tag as RouteProcessViewHolder
            }
        } else {
            createRouteProcessViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is RouteProcess) {
                holder.routeStrTextView?.text = syncElement.routeStr
                holder.routeProcessDateTextView?.text = syncElement.routeProcessDate

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Set layouts
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.routeStrTextView?.setTextColor(col.foreColor)
                holder.routeProcessDateTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (listView!!.isItemChecked(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    private fun createRouteProcessViewHolder(v: View, holder: RouteProcessViewHolder) {
        holder.routeStrTextView = v.findViewById(R.id.routeStr)
        holder.routeProcessDateTextView = v.findViewById(R.id.routeProcessDate)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createDataCollectionViewHolder(v: View, holder: DataCollectionViewHolder) {
        holder.targetStrTextView = v.findViewById(R.id.targetStr)
        holder.dataCollectionDateTextView = v.findViewById(R.id.dataCollectionDate)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createWarehouseAreaViewHolder(v: View, holder: WarehouseAreaViewHolder) {
        holder.descriptionTextView = v.findViewById(R.id.warehouseAreaStr)
        holder.warehouseTextView = v.findViewById(R.id.warehouseStr)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createWarehouseViewHolder(v: View, holder: WarehouseViewHolder) {
        holder.descriptionTextView = v.findViewById(R.id.warehouseStr)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createItemCategoryViewHolder(v: View, holder: ItemCategoryViewHolder) {
        holder.descriptionTextView = v.findViewById(R.id.itemCategoryStr)
        holder.parentCategoryTextView = v.findViewById(R.id.parentStr)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createWarehouseMovementViewHolder(v: View, holder: WarehouseMovementViewHolder) {
        // Holder para los rows normales.
        holder.checkBoxConstraintLayout = v.findViewById(R.id.checkBoxConstraintLayout)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)
        holder.gralConstraintLayout = v.findViewById(R.id.generalDataConstraintLayout)
        holder.origWarehouseAreaTextView = v.findViewById(R.id.origWarehouseAreaStr)
        holder.destWarehouseAreaTextView = v.findViewById(R.id.destWarehouseAreaStr)
        holder.obsTextView = v.findViewById(R.id.obs)
        holder.dividerObs = v.findViewById(R.id.dividerObs)
        holder.movDateCheckedTextView = v.findViewById(R.id.warehouseMovementDate)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createAssetReviewViewHolder(v: View, holder: AssetReviewViewHolder) {
        // Holder para los rows normales.
        holder.checkBoxConstraintLayout = v.findViewById(R.id.checkBoxConstraintLayout)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)
        holder.gralConstraintLayout = v.findViewById(R.id.generalDataConstraintLayout)
        holder.warehouseAreaTextView = v.findViewById(R.id.warehouseAreaStr)
        holder.obsTextView = v.findViewById(R.id.obs)
        holder.dividerObs = v.findViewById(R.id.dividerObs)
        holder.assetReviewDateCheckedTextView = v.findViewById(R.id.assetReviewDate)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    private fun createAssetViewHolder(v: View, holder: AssetViewHolder) {
        // Holder para los rows normales.
        holder.checkBoxConstraintLayout = v.findViewById(R.id.checkBoxConstraintLayout)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.titleTextView = v.findViewById(R.id.titleTextView)
        holder.gralConstraintLayout = v.findViewById(R.id.generalDataConstraintLayout)
        holder.descriptionTextView = v.findViewById(R.id.descriptionAutoSize)
        holder.codeTextView = v.findViewById(R.id.code)
        holder.assetStatusCheckedTextView = v.findViewById(R.id.assetStatus)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    @SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility")
    private fun getWarehouseMovementItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = WarehouseMovementViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createWarehouseMovementViewHolder(v, holder)
            } else {
                holder = v.tag as WarehouseMovementViewHolder
            }
        } else {
            createWarehouseMovementViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is WarehouseMovement) {
                holder.origWarehouseAreaTextView?.text = syncElement.origWarehouseAreaStr
                holder.destWarehouseAreaTextView?.text = syncElement.destWarehouseAreaStr
                holder.obsTextView?.text = syncElement.obs
                if (syncElement.obs.isEmpty()) {
                    holder.obsTextView?.visibility = GONE
                    holder.dividerObs?.visibility = GONE
                } else {
                    holder.obsTextView?.visibility = VISIBLE
                    holder.dividerObs?.visibility = VISIBLE
                }
                holder.movDateCheckedTextView?.text = syncElement.warehouseMovementDate

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background layouts
                // Resalta por estado del activo
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.origWarehouseAreaTextView?.setTextColor(col.foreColor)
                holder.destWarehouseAreaTextView?.setTextColor(col.foreColor)
                holder.obsTextView?.setTextColor(col.foreColor)
                holder.movDateCheckedTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (isSelected(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    @SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility")
    private fun getAssetReviewItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = AssetReviewViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createAssetReviewViewHolder(v, holder)
            } else {
                holder = v.tag as AssetReviewViewHolder
            }
        } else {
            createAssetReviewViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is AssetReview) {
                holder.warehouseAreaTextView?.text = syncElement.warehouseAreaStr
                holder.obsTextView?.text = syncElement.obs
                if (syncElement.obs.isEmpty()) {
                    holder.obsTextView?.visibility = GONE
                    holder.dividerObs?.visibility = GONE
                } else {
                    holder.obsTextView?.visibility = VISIBLE
                    holder.dividerObs?.visibility = VISIBLE
                }
                holder.assetReviewDateCheckedTextView?.text = syncElement.assetReviewDate

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background layouts
                // Resalta por estado del activo
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.warehouseAreaTextView?.setTextColor(col.foreColor)
                holder.obsTextView?.setTextColor(col.foreColor)
                holder.assetReviewDateCheckedTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (isSelected(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    @SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility")
    private fun getAssetItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = AssetViewHolder()
        if (alreadyExists) {
            if (v.tag is String) {
                createAssetViewHolder(v, holder)
            } else {
                holder = v.tag as AssetViewHolder
            }
        } else {
            createAssetViewHolder(v, holder)
        }

        if (position >= 0) {
            val syncElement = getItem(position)

            if (syncElement != null && syncElement is Asset) {
                holder.descriptionTextView?.text = syncElement.description
                holder.codeTextView?.text = syncElement.code
                holder.assetStatusCheckedTextView?.text =
                    AssetStatus.getById(syncElement.assetStatusId)?.description ?: ""

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(syncElement, isChecked, true)
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
                    holder.checkBox!!.setOnCheckedChangeListener(null)
                    holder.checkBox!!.isChecked = checkedIdArray.contains(getKey(syncElement))
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background layouts
                // Resalta por estado del activo
                val col = getColor(syncElement, position)
                v.background = col.backColor
                holder.descriptionTextView?.setTextColor(col.foreColor)
                holder.codeTextView?.setTextColor(col.foreColor)
                holder.assetStatusCheckedTextView?.setTextColor(col.foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(col.titleForeColor)
                holder.titleTextView?.setTextColor(col.titleForeColor)
            }

            if (listView != null) {
                if (isSelected(position)) {
                    v.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 240),
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

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val r: ArrayList<Any> = ArrayList()

                if (constraint != null) {
                    val filterString = constraint.toString().toLowerCase(Locale.getDefault())
                    var filterableItem: Any

                    for (i in 0 until syncElementArray.size) {
                        filterableItem = syncElementArray[i]
                        if (filterableItem !is Asset) {
                            continue
                        }

                        if (
                            filterableItem.code.toLowerCase(Locale.getDefault())
                                .contains(filterString) ||
                            filterableItem.description.toLowerCase(Locale.getDefault())
                                .contains(filterString) ||
                            (filterableItem.serialNumber != null
                                    && filterableItem.serialNumber!!.toLowerCase(Locale.getDefault())
                                .contains(filterString)) ||
                            (filterableItem.ean != null
                                    && filterableItem.ean.toString().contains(filterString))
                        ) {
                            r.add(filterableItem)
                        }
                    }
                }

                results.values = r
                results.count = r.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                suggestedList.clear()
                suggestedList.addAll(results?.values as ArrayList<Any>)
                if (results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    internal open class RouteProcessViewHolder {
        var routeStrTextView: AutoResizeTextView? = null
        var routeProcessDateTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null
    }

    internal open class DataCollectionViewHolder {
        var targetStrTextView: AutoResizeTextView? = null
        var dataCollectionDateTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null
    }

    internal open class WarehouseAreaViewHolder {
        var descriptionTextView: AutoResizeTextView? = null
        var warehouseTextView: AutoResizeTextView? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null
    }

    internal open class WarehouseViewHolder {
        var descriptionTextView: AutoResizeTextView? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null
    }

    internal open class ItemCategoryViewHolder {
        var descriptionTextView: AutoResizeTextView? = null
        var parentCategoryTextView: AutoResizeTextView? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null
    }

    internal class WarehouseMovementViewHolder {
        var checkBoxConstraintLayout: ConstraintLayout? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null

        var gralConstraintLayout: ConstraintLayout? = null
        var origWarehouseAreaTextView: AutoResizeTextView? = null
        var destWarehouseAreaTextView: AutoResizeTextView? = null

        var dividerObs: View? = null
        var obsTextView: AutoResizeTextView? = null
        var movDateCheckedTextView: CheckedTextView? = null
    }

    internal class AssetReviewViewHolder {
        var checkBoxConstraintLayout: ConstraintLayout? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null

        var gralConstraintLayout: ConstraintLayout? = null
        var warehouseAreaTextView: AutoResizeTextView? = null

        var dividerObs: View? = null
        var obsTextView: AutoResizeTextView? = null
        var assetReviewDateCheckedTextView: CheckedTextView? = null
    }

    internal class AssetViewHolder {
        var checkBoxConstraintLayout: ConstraintLayout? = null
        var checkBox: CheckBox? = null
        var titleTextView: TextView? = null

        var gralConstraintLayout: ConstraintLayout? = null
        var descriptionTextView: AutoResizeTextView? = null
        var codeTextView: AutoResizeTextView? = null
        var assetStatusCheckedTextView: CheckedTextView? = null
    }

    companion object {

        fun defaultRowHeight(): Int {
            return if (Statics.isTablet()) 51 else 157
        }

        class ItemComparator : Comparator<Any> {
            fun compareNullable(o1: Any?, o2: Any?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: Any, o2: Any): Int {
                return try {
                    when {
                        o1 is Asset && o2 is Asset -> {
                            val codeComp = o1.code.compareTo(o2.code, true)
                            val serialComp = (o1.serialNumber ?: "").compareTo(
                                o2.serialNumber
                                    ?: "", true
                            )
                            val eanComp = (o1.ean ?: "").compareTo(o2.ean ?: "", true)

                            // Orden natural: code, serialNumber, EAN
                            when (codeComp) {
                                0 -> when (serialComp) {
                                    0 -> eanComp
                                    else -> serialComp
                                }
                                else -> codeComp
                            }
                        }
                        o1 is ItemCategory && o2 is ItemCategory -> {
                            val descComp = o1.description.compareTo(o2.description, true)
                            val parentIdComp = (o1.parentId ?: 0).compareTo((o2.parentId ?: 0))
                            val parentComp = o1.parentStr.compareTo(o2.parentStr, true)

                            // Orden natural: code, serialNumber, EAN
                            when (parentIdComp) {
                                0 -> when (parentComp) {
                                    0 -> descComp
                                    else -> parentComp
                                }
                                else -> parentIdComp
                            }
                        }
                        o1 is Warehouse && o2 is Warehouse -> {
                            val descComp = o1.description.compareTo(o2.description, true)

                            // Orden natural: code, serialNumber, EAN
                            when (val idcomp = o1.warehouseId.compareTo(o2.warehouseId)) {
                                0 -> descComp
                                else -> idcomp
                            }
                        }
                        o1 is WarehouseArea && o2 is WarehouseArea -> {
                            val descComp = o1.description.compareTo(o2.description, true)

                            // Orden natural: code, serialNumber, EAN
                            when (val parentIdComp = o1.warehouseId.compareTo(o2.warehouseId)) {
                                0 -> descComp
                                else -> parentIdComp
                            }
                        }
                        else -> {
                            0
                        }
                    }
                } catch (ex: Exception) {
                    0
                }
            }
        }
    }

    //region COLORS

    private var selectedForeColor: Int = 0

    private var itemCategoryForeColor: Int = 0
    private var warehouseForeColor: Int = 0
    private var warehouseAreaForeColor: Int = 0
    private var assetForeColor: Int = 0
    private var assetMaintenanceForeColor: Int = 0
    private var dataCollectionForeColor: Int = 0
    private var routeProcessForeColor: Int = 0
    private var warehouseMovementForeColor: Int = 0
    private var assetReviewForeColor: Int = 0
    private var defaultForeColor: Int = 0

    private fun setupColors() {
        selectedForeColor = ResourcesCompat.getColor(
            Statics.AssetControl.getContext().resources,
            R.color.text_light,
            null
        )

        itemCategoryForeColor = Statics.getBestContrastColor("#009688")
        warehouseForeColor = Statics.getBestContrastColor("#F44336")
        warehouseAreaForeColor = Statics.getBestContrastColor("#C22319")
        assetForeColor = Statics.getBestContrastColor("#2196F3")
        assetMaintenanceForeColor = Statics.getBestContrastColor("#AD1457")
        dataCollectionForeColor = Statics.getBestContrastColor("#7323A3")
        routeProcessForeColor = Statics.getBestContrastColor("#5639AF")
        warehouseMovementForeColor = Statics.getBestContrastColor("#FFC107")
        assetReviewForeColor = Statics.getBestContrastColor("#7323A3")
        defaultForeColor = Statics.getBestContrastColor("#E4971B")
    }

    //endregion

    private fun getColor(syncElement: Any?, position: Int): SyncElementLayout {
        // Background layouts
        val layoutItemCategory = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_green,
            null
        )
        val layoutWarehouse = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_red,
            null
        )
        val layoutWarehouseArea = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_marron,
            null
        )
        val layoutAsset = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_blue,
            null
        )
        val layoutAssetManteinance = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_violet,
            null
        )
        val layoutDataCollection = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_violet2,
            null
        )
        val layoutRouteProcess = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_violet3,
            null
        )
        val layoutWarehouseMovement = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_yellow,
            null
        )
        val layoutAssetReview = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border_yellow2,
            null
        )
        val layoutDefault = ResourcesCompat.getDrawable(
            Statics.AssetControl.getContext().resources,
            R.drawable.layout_thin_border,
            null
        )

        val isSelected = isSelected(position)

        val backColor: Drawable
        val foreColor: Int
        when (syncElement) {
            is Asset -> {
                backColor = layoutAsset!!
                foreColor = if (isSelected) selectedForeColor else assetForeColor
            }
            is ItemCategory -> {
                backColor = layoutItemCategory!!
                foreColor = if (isSelected) selectedForeColor else itemCategoryForeColor
            }
            is Warehouse -> {
                backColor = layoutWarehouse!!
                foreColor = if (isSelected) selectedForeColor else warehouseForeColor
            }
            is WarehouseArea -> {
                backColor = layoutWarehouseArea!!
                foreColor = if (isSelected) selectedForeColor else warehouseAreaForeColor
            }
            is AssetManteinance -> {
                backColor = layoutAssetManteinance!!
                foreColor = if (isSelected) selectedForeColor else assetMaintenanceForeColor
            }
            is DataCollection -> {
                backColor = layoutDataCollection!!
                foreColor = if (isSelected) selectedForeColor else dataCollectionForeColor
            }
            is RouteProcess -> {
                backColor = layoutRouteProcess!!
                foreColor = if (isSelected) selectedForeColor else routeProcessForeColor
            }
            is WarehouseMovement -> {
                backColor = layoutWarehouseMovement!!
                foreColor = if (isSelected) selectedForeColor else warehouseMovementForeColor
            }
            is AssetReview -> {
                backColor = layoutAssetReview!!
                foreColor = if (isSelected) selectedForeColor else assetReviewForeColor
            }
            else -> {
                backColor = layoutDefault!!
                foreColor = if (isSelected) selectedForeColor else defaultForeColor
            }
        }

        val darkerColor = when {
            isSelected -> true
            foreColor == Statics.textLightColor() -> true
            else -> false
        }

        return SyncElementLayout(
            foreColor,
            backColor,
            manipulateColor(foreColor, if (darkerColor) 0.8f else 1.4f)
        )
    }

    class SyncElementLayout(foreColor: Int, backColor: Drawable, titleForeColor: Int) {
        var foreColor: Int = 0
        var backColor: Drawable? = null
        var titleForeColor: Int = 0

        init {
            this.foreColor = foreColor
            this.backColor = backColor
            this.titleForeColor = titleForeColor
        }
    }
}