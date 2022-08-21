package com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper

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
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.getColorWithAlpha
import com.dacosys.assetControl.utils.Statics.Companion.manipulateColor
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter.*
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter.Companion.AddPhotoRequiredListener
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter.Companion.AlbumViewRequiredListener
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter.Companion.EditAssetRequiredListener
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.assets.ownershipStatus.OwnershipStatus
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.movements.warehouseMovementContentStatus.WarehouseMovementContentStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.commons.views.AutoResizeTextView
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by Agustin on 18/01/2017.
 */

class WarehouseMovementContentAdapter :
    ArrayAdapter<WarehouseMovementContent>,
    Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0

    // region LISTENERS
    private var weakRefDataSetChangedListener: WeakReference<DataSetChangedListener?>? = null
    var dataSetChangedListener: DataSetChangedListener?
        get() {
            return weakRefDataSetChangedListener?.get()
        }
        set(newValue) {
            weakRefDataSetChangedListener = WeakReference(newValue)
        }

    private var weakRefCheckedChangedListener: WeakReference<CheckedChangedListener?>? = null
    var checkedChangedListener: CheckedChangedListener?
        get() {
            return weakRefCheckedChangedListener?.get()
        }
        set(newValue) {
            weakRefCheckedChangedListener = WeakReference(newValue)
        }

    private var weakRefAlbumViewRequiredListener: WeakReference<AlbumViewRequiredListener?>? =
        null
    private var albumViewRequiredListener: AlbumViewRequiredListener?
        get() {
            return weakRefAlbumViewRequiredListener?.get()
        }
        set(newValue) {
            weakRefAlbumViewRequiredListener = WeakReference(newValue)
        }

    private var weakRefAddPhotoRequiredListener: WeakReference<AddPhotoRequiredListener?>? =
        null
    private var addPhotoRequiredListener: AddPhotoRequiredListener?
        get() {
            return weakRefAddPhotoRequiredListener?.get()
        }
        set(newValue) {
            weakRefAddPhotoRequiredListener = WeakReference(newValue)
        }

    private var weakRefEditAssetRequiredListener: WeakReference<EditAssetRequiredListener?>? =
        null
    private var editAssetRequiredListener: EditAssetRequiredListener?
        get() {
            return weakRefEditAssetRequiredListener?.get()
        }
        set(newValue) {
            weakRefEditAssetRequiredListener = WeakReference(newValue)
        }
    // endregion LISTENERS

    private var weakRefListView: WeakReference<ListView?>? = null
        set(newValue) {
            field = newValue
            val l = listView
            if (l != null) {
                activity.runOnUiThread {
                    l.adapter = this
                }

                l.setOnItemClickListener { _, _, position, _ ->
                    selectItem(pos = position, smoothScroll = false)
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

    private var lastSelectedPos = -1
    private var multiSelect: Boolean = false

    private var visibleStatus: ArrayList<WarehouseMovementContentStatus> = ArrayList()
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var wmContArray: ArrayList<WarehouseMovementContent> = ArrayList()
    private var suggestedList: ArrayList<WarehouseMovementContent> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        wmContArray: ArrayList<WarehouseMovementContent>,
        suggestedList: ArrayList<WarehouseMovementContent>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<Long>,
        visibleStatus: ArrayList<WarehouseMovementContentStatus>,

        ) : super(Statics.AssetControl.getContext(), resource, suggestedList) {
        this.activity = activity
        this.resource = resource
        this.visibleStatus = visibleStatus
        this.checkedIdArray = checkedIdArray

        this.multiSelect = multiSelect

        this.listView = listView
        this.wmContArray = wmContArray
        this.suggestedList = suggestedList
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

    /**
     * Esta función es la que agrega realmente los items al adaptador.
     * Está separada para poder realizar antes el ordenamiento especial.
     * Recibe la lista, la ordena, limpia el adaptador, lo llena y llama al
     * refresh.
     * */
    private fun refreshItems(originalList: ArrayList<WarehouseMovementContent>) {
        val sortedList = sortItems(originalList)

        activity.runOnUiThread {
            super.clear()
            super.addAll(sortedList)
        }

        refresh()
    }

    /**
     * Funciones para AGREGAR, ACTUALIZAR y ELIMINAR contenidos del adaptador
     */

    /**
     * Override de la función add del Adapter
     * No llamo a super.add porque yo me encargo de disparar los eventos de cambios
     * cuando creo necesario.
     */
    override fun add(wmc: WarehouseMovementContent?) {
        if (wmc != null) {
            add(arrayListOf(wmc), true)
        }
    }

    fun add(wmContArray: ArrayList<WarehouseMovementContent>, selectItem: Boolean) {
        val all = getAll()
        val wmcAdded: ArrayList<WarehouseMovementContent> = ArrayList()

        activity.runOnUiThread {
            for (wmc in wmContArray) {
                if (!all.contains(wmc)) {
                    wmcAdded.add(wmc)
                    all.add(wmc)
                }
            }

            if (!wmcAdded.any()) return@runOnUiThread

            reportWarehouseMovementAdded(wmcAdded)

            refreshItems(all)

            for (wmc in wmcAdded) {
                if (wmc.contentStatusId == WarehouseMovementContentStatus.toMove.id) {
                    setChecked(content = wmc, isChecked = true)
                } else {
                    setChecked(content = wmc, isChecked = false)
                }
            }

            if (selectItem) selectItem(
                wmc = wmcAdded.first(),
                smoothScroll = true
            )
        }
    }

    /**
     * Se utiliza cuando se edita un activo y necesita actualizarse
     */
    fun updateAsset(asset: Asset, selectItem: Boolean = true) {
        for (i in 0 until count) {
            val t = (getItem(i) as WarehouseMovementContent)
            if (t.assetId == asset.assetId) {
                activity.runOnUiThread {
                    t.assetId = asset.assetId
                    t.code = asset.code
                    t.description = asset.description
                    t.warehouseAreaId = asset.warehouseAreaId
                    t.ownershipStatusId = asset.ownershipStatusId
                    t.assetStatusId = asset.assetStatusId
                    t.itemCategoryId = asset.itemCategoryId
                    t.labelNumber = asset.labelNumber ?: 0
                    t.manufacturer = asset.manufacturer ?: ""
                    t.model = asset.model ?: ""
                    t.serialNumber = asset.serialNumber ?: ""
                    t.parentId = asset.parentAssetId ?: 0
                    t.ean = asset.ean ?: ""
                }

                dataSetChangedListener?.onDataSetChanged()

                if (selectItem) forceSelectItem(t)

                break
            }
        }
    }

    /**
     * ACTUALIZA un contenido.
     */
    fun updateContent(
        wmc: WarehouseMovementContent,
        wmcStatusId: Int,
        assetStatusId: Int,
        selectItem: Boolean,
        changeCheckedState: Boolean,
    ) {
        for (i in 0 until count) {
            val t = getItem(i) as WarehouseMovementContent
            if (t == wmc) {
                activity.runOnUiThread {
                    t.assetStatusId = assetStatusId
                    t.contentStatusId = wmcStatusId
                }

                if (changeCheckedState) {
                    if (t.contentStatusId == WarehouseMovementContentStatus.toMove.id) {
                        setChecked(content = t, isChecked = true)
                    } else {
                        setChecked(content = t, isChecked = false)
                    }
                }

                if (selectItem) forceSelectItem(wmc)

                refresh()

                break
            }
        }
    }

    /**
     * ELIMINA un contenido.
     * Override de la función del ArrayAdapter
     */
    override fun remove(wmc: WarehouseMovementContent?) {
        if (wmc != null) {
            remove(arrayListOf(wmc))
        }
    }

    /**
     * Función que propiamente elimina el contenido de la colección.
     * También se ocupa de eliminar el Id de la colección de Ids mwmcados (checked).
     */
    fun remove(assets: ArrayList<WarehouseMovementContent>) {
        if (assets.isEmpty()) return

        val all = getAll()
        lastSelectedPos = currentPos()

        val assetsRemoved: ArrayList<WarehouseMovementContent> = ArrayList()
        activity.runOnUiThread {
            for (w in assets) {
                if (all.contains(w)) {
                    assetsRemoved.add(w)

                    if (checkedIdArray.contains(w.assetId)) {
                        checkedIdArray.remove((w.assetId))
                    }
                }
            }

            if (!assetsRemoved.any()) return@runOnUiThread

            all.removeAll(assetsRemoved.toSet())

            reportWarehouseMovementRemoved(assetsRemoved)

            refreshItems(all)

            selectNearVisible()
        }
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()
        }
    }

    /** Permite suspender la aparición del mensaje en pantalla
     * que muestra el código leído y la acción realizada.
     **/
    var suspendReport = false

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportWarehouseMovementAdded(wmContArray: ArrayList<WarehouseMovementContent>) {
        if (suspendReport) {
            return
        }
        if (wmContArray.size <= 0) {
            return
        }

        var res = ""
        for (wmc in wmContArray) {
            res += "${wmc.code}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (wmContArray.size > 1)
                    Statics.AssetControl.getContext().getString(R.string.added_plural) else
                    Statics.AssetControl.getContext().getString(R.string.added)

        makeText(activity, res, SnackbarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportWarehouseMovementRemoved(wmContArray: ArrayList<WarehouseMovementContent>) {
        if (suspendReport) {
            return
        }
        if (wmContArray.size <= 0) {
            return
        }

        var res = ""
        for (wmc in wmContArray) {
            res += "${wmc.code}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " +
                if (wmContArray.size > 1)
                    " ${Statics.AssetControl.getContext().getString(R.string.removed_plural)}" else
                    " ${Statics.AssetControl.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackbarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    private fun getIndex(wmc: WarehouseMovementContent): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as WarehouseMovementContent)
            if (t == wmc) {
                return i
            }
        }
        return -1
    }

    fun count(): Int {
        return count
    }

    fun countVisible(): Int {
        var visibleItems = 0
        for (i in 0 until count) {
            if (isStatusVisible(i)) {
                visibleItems++
            }
        }
        return visibleItems
    }

    fun getAll(): ArrayList<WarehouseMovementContent> {
        val r: ArrayList<WarehouseMovementContent> = ArrayList()
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

    fun setChecked(checkedItems: ArrayList<WarehouseMovementContent>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    private var isFilling = false
    private fun setChecked(contents: ArrayList<WarehouseMovementContent>, isChecked: Boolean) {
        if (isFilling) return
        isFilling = true

        for (i in contents) {
            setChecked(i, isChecked)
        }

        isFilling = false
        refresh()
    }

    /**
     * Esta función simplemente elimina/agrega un contenido
     * de la colección de Ids mwmcados (checked).
     */
    private fun setChecked(
        content: WarehouseMovementContent,
        isChecked: Boolean,
    ) {
        if (isChecked) {
            if (!checkedIdArray.contains(content.assetId)) {
                checkedIdArray.add(content.assetId)
            }
        } else {
            checkedIdArray.remove(content.assetId)
        }
    }

    fun clearChecked() {
        checkedIdArray.clear()
    }

    override fun sort(comparator: Comparator<in WarehouseMovementContent>) {
        super.sort(customComparator)
    }

    private val customComparator =
        Comparator { o1: WarehouseMovementContent?, o2: WarehouseMovementContent? ->
            WarehouseMovementContentComparator().compareNullable(
                o1,
                o2
            )
        }

    fun refresh() {
        activity.runOnUiThread { notifyDataSetChanged() }
        dataSetChangedListener?.onDataSetChanged()
    }

    /**
     * Fuerza la selección aunque el ítem esté o no previamente seleccionado.
     * Cambia a verdadero estado mwmcado (checked).
     * Hace scroll suave hasta el ítem.
     */
    fun forceSelectItem(a: WarehouseMovementContent) {
        if (listView == null) return

        val pos = getPosition(a)

        listView?.clearChoices()

        activity.runOnUiThread {
            listView?.setItemChecked(pos, true)
            listView?.setSelection(pos)
        }

        lastSelectedPos = currentPos()

        activity.runOnUiThread {
            notifyDataSetChanged()
            listView?.smoothScrollToPosition(pos)
        }
    }

    fun selectItem(
        wmc: WarehouseMovementContent?,
        smoothScroll: Boolean,
    ) {
        var pos = -1
        if (wmc != null) pos = getPosition(wmc)
        selectItem(pos, smoothScroll)
    }

    fun selectItem(
        pos: Int,
        smoothScroll: Boolean,
    ) {
        selectItem(pos, pos, smoothScroll)
    }

    fun selectItem(
        wmc: WarehouseMovementContent?,
        scrollPos: Int,
        smoothScroll: Boolean,
    ) {
        var pos = -1
        if (wmc != null) pos = getPosition(wmc)
        selectItem(pos, scrollPos, smoothScroll)
    }

    fun selectItem(
        pos: Int,
        scrollPos: Int,
        smoothScroll: Boolean,
    ) {
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

        activity.runOnUiThread {
            if (smoothScroll) {
                listView?.smoothScrollToPosition(scrollPos)
            } else {
                listView?.setSelection(scrollPos)
            }
        }
    }

    fun currentWmCont(): WarehouseMovementContent? {
        return (0 until count)
            .firstOrNull { isSelected(it) }
            ?.let {
                val t = getItem(it)
                t
            }
    }

    fun currentAsset(): Asset? {
        return (0 until count)
            .firstOrNull { isSelected(it) }
            ?.let {
                val t = AssetDbHelper().selectById(getItem(it)?.assetId ?: 0)
                t
            }
    }

    fun getToMove(warehouseAreaId: Long): ArrayList<WarehouseMovementContent> {
        val r: ArrayList<WarehouseMovementContent> = ArrayList()
        for (i in 0 until count) {
            // Tanto los que se van a mover como los que se encontraron en el área
            val tempItem = getItem(i) as WarehouseMovementContent
            if (tempItem.warehouseAreaId != warehouseAreaId ||
                tempItem.warehouseAreaId == warehouseAreaId && tempItem
                    .assetStatusId == AssetStatus.missing.id
            ) {
                r.add(getItem(i) as WarehouseMovementContent)
            }
        }
        return r
    }

    val assetsToMove: Int
        get() {
            return getAll().count {
                it.contentStatusId == WarehouseMovementContentStatus.toMove.id
            }
        }

    val assetsNoNeedToMove: Int
        get() {
            return getAll().count { it.contentStatusId == WarehouseMovementContentStatus.noNeedToMove.id }
        }

    fun assetsFounded(destWaId: Long): Int {
        var r = 0
        for (wmc in getAll()) {
            if (wmc.warehouseAreaId == destWaId && wmc.assetStatusId == AssetStatus.missing.id) {
                r++
            }
        }
        return r
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

    fun getItems(): ArrayList<WarehouseMovementContent> {
        val r: ArrayList<WarehouseMovementContent> = ArrayList()
        for (i in 0 until count) {
            val t = getItem(i) ?: continue
            r.add(t)
        }
        return r
    }

    fun isStatusVisible(position: Int): Boolean {
        if (position < 0) return false
        return isStatusVisible(getItem(position))
    }

    fun isStatusVisible(wmc: WarehouseMovementContent?): Boolean {
        return if (wmc != null) {
            (visibleStatus.contains(WarehouseMovementContentStatus.getById(wmc.contentStatusId)))
        } else false
    }

    fun getVisibleStatus(): ArrayList<WarehouseMovementContentStatus> {
        return visibleStatus
    }

    fun setVisibleStatus(status: ArrayList<WarehouseMovementContentStatus>) {
        visibleStatus = status
        refresh()
    }

    fun addVisibleStatus(status: WarehouseMovementContentStatus) {
        if (!visibleStatus.contains(status)) {
            visibleStatus.add(status)
            refresh()
        }
    }

    fun removeVisibleStatus(status: WarehouseMovementContentStatus) {
        if (visibleStatus.contains(status)) {
            visibleStatus.remove(status)
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

        selectItem(pos = newPos, smoothScroll = true)
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
        editAssetRequiredListener: EditAssetRequiredListener?,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
        this.editAssetRequiredListener = editAssetRequiredListener
    }

    fun refreshImageControlListeners(
        addPhotoListener: AddPhotoRequiredListener?,
        albumViewListener: AlbumViewRequiredListener?,
    ) {
        this.addPhotoRequiredListener = addPhotoListener
        this.albumViewRequiredListener = albumViewListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        // Seleccionamos el layout dependiendo si es
        // un row visible u oculto según su AsseStatus.

        val currentLayout: Int =
            if (listView == null) {
                // Estamos trabajando en un Dropdown
                when {
                    isStatusVisible(position) -> R.layout.asset_simple_row
                    else -> R.layout.null_row
                }
            } else when {
                // Estamos trabajando en un ListView
                !isStatusVisible(position) -> R.layout.null_row
                isSelected(position) -> R.layout.asset_row_expanded
                else -> R.layout.asset_row
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
                v.tag is String && currentLayout == R.layout.asset_row ||
                v.tag is String && currentLayout == R.layout.asset_row_expanded ||
                v.tag is String && currentLayout == R.layout.asset_simple_row ||

                v.tag is CollapsedViewHolder && currentLayout != R.layout.asset_row ||
                v.tag is ExpandedViewHolder && currentLayout != R.layout.asset_row_expanded ||
                v.tag is SimpleViewHolder && currentLayout != R.layout.asset_simple_row
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
            R.layout.asset_row_expanded -> fillExpandedItemView(position, v!!, alreadyExists)
            else -> fillCollapsedListView(position, v!!, alreadyExists)
        }
        return v
    }

    private fun fillNullView(v: View): View {
        v.tag = ""
        return v
    }

    private fun createExpandedViewHolder(v: View, holder: ExpandedViewHolder) {
        // Holder para los rows expandidos
        holder.checkBoxConstraintLayout = v.findViewById(R.id.checkBoxConstraintLayout)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.gralConstraintLayout = v.findViewById(R.id.generalDataConstraintLayout)
        holder.descriptionTextView = v.findViewById(R.id.descriptionAutoSize)
        holder.codeTextView = v.findViewById(R.id.code)
        holder.assetStatusTextView = v.findViewById(R.id.assetStatus)
        holder.locationConstraintLayout = v.findViewById(R.id.locationConstraintLayout)
        holder.warehouseTextView = v.findViewById(R.id.warehouseStr)
        holder.warehouseAreaTextView = v.findViewById(R.id.warehouseAreaStr)
        holder.manufacturerConstraintLayout = v.findViewById(R.id.manufacturerConstraintLayout)
        holder.manufacturerTextView = v.findViewById(R.id.manufacturer)
        holder.modelTextView = v.findViewById(R.id.model)
        holder.categoryConstraintLayout = v.findViewById(R.id.categoryConstraintLayout)
        holder.categoryTextView = v.findViewById(R.id.category)
        holder.ownershipTextView = v.findViewById(R.id.ownership)
        holder.serialNumberConstraintLayout = v.findViewById(R.id.serialNumberConstraintLayout)
        holder.serialNumberTextView = v.findViewById(R.id.serialNumberStr)
        holder.eanTextView = v.findViewById(R.id.eanStr)

        holder.signImageView = v.findViewById(R.id.signImageView)
        holder.addPhotoImageView = v.findViewById(R.id.addPhotoImageView)
        holder.albumImageView = v.findViewById(R.id.albumImageView)
        holder.editImageView = v.findViewById(R.id.editImageView)
        holder.imageControlConstraintLayout = v.findViewById(R.id.imageControlConstraintLayout)

        holder.divider1 = v.findViewById(R.id.dividerInternal1)
        holder.divider2 = v.findViewById(R.id.dividerInternal2)
        holder.divider3 = v.findViewById(R.id.dividerInternal3)
        holder.divider4 = v.findViewById(R.id.dividerInternal4)
        holder.divider5 = v.findViewById(R.id.dividerInternal5)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }
        holder.locationConstraintLayout = v.findViewById(R.id.locationConstraintLayout)

        // TextView de títulos
        holder.manufacturerTitleTextView = v.findViewById(R.id.manufacturerTextView)
        holder.modelTitleTextView = v.findViewById(R.id.modelTextView)
        holder.locationTitleTextView = v.findViewById(R.id.locationTextView)
        holder.categoryTitleTextView = v.findViewById(R.id.categoryTextView)
        holder.ownershipTitleTextView = v.findViewById(R.id.ownershipTextView)
        holder.serialNumberTitleTextView = v.findViewById(R.id.serialNumberTextView)
        holder.eanTitleTextView = v.findViewById(R.id.eanTextView)

        if (Statics.useImageControl) {
            holder.signImageView?.visibility = GONE
            holder.addPhotoImageView?.visibility = VISIBLE
            holder.albumImageView?.visibility = VISIBLE
        } else {
            holder.signImageView?.visibility = GONE
            holder.addPhotoImageView?.visibility = GONE
            holder.albumImageView?.visibility = GONE
        }

        if (editAssetRequiredListener != null) {
            holder.divider5?.visibility = VISIBLE
            holder.imageControlConstraintLayout?.visibility = VISIBLE

            holder.editImageView?.visibility = VISIBLE
        } else {
            holder.editImageView?.visibility = GONE
            if (!Statics.useImageControl) {
                holder.divider5?.visibility = GONE
                holder.imageControlConstraintLayout?.visibility = GONE
            } else {
                holder.divider5?.visibility = VISIBLE
                holder.imageControlConstraintLayout?.visibility = VISIBLE
            }
        }

        v.tag = holder
    }

    @SuppressLint("ClickableViewAccessibility", "ObsoleteSdkInt")
    private fun fillExpandedItemView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = ExpandedViewHolder()
        if (alreadyExists) {
            if (v.tag is CollapsedViewHolder || v.tag is String) {
                createExpandedViewHolder(v, holder)
            } else {
                holder = v.tag as ExpandedViewHolder
            }
        } else {
            createExpandedViewHolder(v, holder)
        }

        if (position >= 0) {
            val arC = getItem(position)
            val isSelected = isSelected(position)

            if (arC != null) {
                holder.descriptionTextView?.text = arC.description
                holder.codeTextView?.text = arC.code
                holder.assetStatusTextView?.text =
                    WarehouseMovementContentStatus.getById(arC.contentStatusId)?.description ?: ""

                // region Manufacturer
                val manufacturerStr = arC.manufacturer
                val modelStr = arC.model

                if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
                    holder.divider1?.visibility = GONE
                    holder.manufacturerConstraintLayout?.visibility = GONE
                } else {
                    holder.divider1?.visibility = VISIBLE
                    holder.manufacturerConstraintLayout?.visibility = VISIBLE
                    holder.manufacturerTextView?.text = manufacturerStr
                    holder.modelTextView?.text = modelStr
                }
                // endregion

                // region Location
                val wStr = arC.warehouseStr
                val waStr = arC.warehouseAreaStr

                if (wStr.isEmpty() && waStr.isEmpty()) {
                    holder.divider2?.visibility = GONE
                    holder.locationConstraintLayout?.visibility = GONE
                } else {
                    holder.divider2?.visibility = VISIBLE
                    holder.locationConstraintLayout?.visibility = VISIBLE
                    holder.warehouseTextView?.text = wStr
                    holder.warehouseAreaTextView?.text = waStr
                }
                // endregion

                // region Category
                val categoryStr = arC.itemCategoryStr
                val ownershipStr =
                    OwnershipStatus.getById(arC.ownershipStatusId)?.description ?: ""

                if (categoryStr.isEmpty() && ownershipStr.isEmpty()) {
                    holder.divider3?.visibility = GONE
                    holder.categoryConstraintLayout?.visibility = GONE
                } else {
                    holder.divider3?.visibility = VISIBLE
                    holder.categoryConstraintLayout?.visibility = VISIBLE
                    holder.categoryTextView?.text = categoryStr
                    holder.ownershipTextView?.text = ownershipStr
                }
                // endregion

                // region SerialNumber
                val serialNumberStr = arC.serialNumber
                val eanStr = arC.ean

                if (serialNumberStr.isEmpty() && eanStr.isEmpty()) {
                    holder.divider4?.visibility = GONE
                    holder.serialNumberConstraintLayout?.visibility = GONE
                } else {
                    holder.divider4?.visibility = VISIBLE
                    holder.serialNumberConstraintLayout?.visibility = VISIBLE
                    holder.serialNumberTextView?.text = serialNumberStr
                    holder.eanTextView?.text = eanStr
                }
                // endregion

                // Edit Button
                holder.editImageView!!.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        editAssetRequiredListener?.onEditAssetRequired(
                            Table.asset.tableId,
                            arC.assetId
                        )
                    }
                    true
                }

                // region ImageControl
                if (Statics.useImageControl) {
                    if (holder.albumImageView != null) {
                        holder.albumImageView!!.setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                albumViewRequiredListener?.onAlbumViewRequired(
                                    Table.asset.tableId,
                                    arC.assetId
                                )
                            }
                            true
                        }
                    }
                    if (holder.addPhotoImageView != null) {
                        holder.addPhotoImageView!!.setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                addPhotoRequiredListener?.onAddPhotoRequired(
                                    Table.asset.tableId,
                                    arC.assetId,
                                    arC.description
                                )
                            }
                            true
                        }
                    }
                }
                // endregion ImageControl

                // region CheckBox

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(arC, isChecked)
                            checkedChangedListener?.onCheckedChanged(isChecked, position)
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
                    holder.checkBox!!.isChecked = checkedIdArray.contains(arC.assetId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }
                // endregion CheckBox

                // Background layouts
                val layoutToMove = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutNoNeedToMove = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )
                val layoutDefault = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border,
                    null
                )

                // Font colors
                val white =
                    ResourcesCompat.getColor(
                        Statics.AssetControl.getContext().resources,
                        R.color.text_light,
                        null
                    )
                val black =
                    ResourcesCompat.getColor(
                        Statics.AssetControl.getContext().resources,
                        R.color.text_dark,
                        null
                    )

                val backColor: Drawable
                val foreColor: Int
                when (WarehouseMovementContentStatus.getById(arC.contentStatusId)) {
                    WarehouseMovementContentStatus.toMove -> {
                        backColor = layoutToMove!!
                        foreColor = white
                    }
                    WarehouseMovementContentStatus.noNeedToMove -> {
                        backColor = layoutNoNeedToMove!!
                        foreColor = if (isSelected) white else black
                    }
                    else -> {
                        backColor = layoutDefault!!
                        foreColor = if (isSelected) white else black
                    }
                }

                val darkerColor = when {
                    isSelected -> true
                    foreColor == Statics.textLightColor() -> true
                    else -> false
                }

                val titleForeColor: Int =
                    manipulateColor(foreColor, if (darkerColor) 0.8f else 1.4f)

                v.background = backColor
                holder.descriptionTextView?.setTextColor(foreColor)
                holder.codeTextView?.setTextColor(foreColor)
                holder.assetStatusTextView?.setTextColor(foreColor)
                holder.warehouseTextView?.setTextColor(foreColor)
                holder.warehouseAreaTextView?.setTextColor(foreColor)
                holder.manufacturerTextView?.setTextColor(foreColor)
                holder.modelTextView?.setTextColor(foreColor)
                holder.categoryTextView?.setTextColor(foreColor)
                holder.ownershipTextView?.setTextColor(foreColor)
                holder.serialNumberTextView?.setTextColor(foreColor)
                holder.eanTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)
                holder.signImageView?.imageTintList = ColorStateList.valueOf(titleForeColor)
                holder.addPhotoImageView?.imageTintList = ColorStateList.valueOf(titleForeColor)
                holder.albumImageView?.imageTintList = ColorStateList.valueOf(titleForeColor)
                holder.editImageView?.imageTintList = ColorStateList.valueOf(titleForeColor)

                // Titles
                holder.manufacturerTitleTextView?.setTextColor(titleForeColor)
                holder.modelTitleTextView?.setTextColor(titleForeColor)
                holder.locationTitleTextView?.setTextColor(titleForeColor)
                holder.categoryTitleTextView?.setTextColor(titleForeColor)
                holder.ownershipTitleTextView?.setTextColor(titleForeColor)
                holder.serialNumberTitleTextView?.setTextColor(titleForeColor)
                holder.eanTitleTextView?.setTextColor(titleForeColor)
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

    private fun createCollapsedViewHolder(v: View, holder: CollapsedViewHolder) {
        // Holder para los rows normales.
        holder.checkBoxConstraintLayout = v.findViewById(R.id.checkBoxConstraintLayout)
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.gralConstraintLayout = v.findViewById(R.id.generalDataConstraintLayout)
        holder.descriptionTextView = v.findViewById(R.id.descriptionAutoSize)
        holder.codeTextView = v.findViewById(R.id.code)
        holder.assetStatusTextView = v.findViewById(R.id.assetStatus)
        holder.manufacturerConstraintLayout = v.findViewById(R.id.manufacturerConstraintLayout)
        holder.manufacturerTextView = v.findViewById(R.id.manufacturer)
        holder.modelTextView = v.findViewById(R.id.model)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        // TextView's de títulos
        holder.manufacturerTitleTextView = v.findViewById(R.id.manufacturerTextView)
        holder.modelTitleTextView = v.findViewById(R.id.modelTextView)

        v.tag = holder
    }

    @SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility")
    private fun fillCollapsedListView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = CollapsedViewHolder()
        if (alreadyExists) {
            if (v.tag is ExpandedViewHolder || v.tag is String) {
                createCollapsedViewHolder(v, holder)
            } else {
                holder = v.tag as CollapsedViewHolder
            }
        } else {
            createCollapsedViewHolder(v, holder)
        }

        if (position >= 0) {
            val arC = getItem(position)
            val isSelected = isSelected(position)

            if (arC != null) {
                holder.descriptionTextView?.text = arC.description
                holder.codeTextView?.text = arC.code
                holder.assetStatusTextView!!.text =
                    WarehouseMovementContentStatus.getById(arC.contentStatusId)?.description ?: ""

                // region Manufacturer
                val manufacturerStr = arC.manufacturer
                val modelStr = arC.model

                if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
                    holder.manufacturerConstraintLayout?.visibility = GONE
                } else {
                    holder.manufacturerConstraintLayout?.visibility = VISIBLE
                    holder.manufacturerTextView?.text = manufacturerStr
                    holder.modelTextView?.text = modelStr
                }
                // endregion

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(arC, isChecked)
                            checkedChangedListener?.onCheckedChanged(isChecked, position)
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
                    holder.checkBox!!.isChecked = checkedIdArray.contains(arC.assetId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                val layoutToMove = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutNoNeedToMove = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )
                val layoutDefault = ResourcesCompat.getDrawable(
                    Statics.AssetControl.getContext().resources,
                    R.drawable.layout_thin_border,
                    null
                )

                // Font colors
                val white =
                    ResourcesCompat.getColor(
                        Statics.AssetControl.getContext().resources,
                        R.color.text_light,
                        null
                    )
                val black =
                    ResourcesCompat.getColor(
                        Statics.AssetControl.getContext().resources,
                        R.color.text_dark,
                        null
                    )

                val backColor: Drawable
                val foreColor: Int
                when (WarehouseMovementContentStatus.getById(arC.contentStatusId)) {
                    WarehouseMovementContentStatus.toMove -> {
                        backColor = layoutToMove!!
                        foreColor = white
                    }
                    WarehouseMovementContentStatus.noNeedToMove -> {
                        backColor = layoutNoNeedToMove!!
                        foreColor = if (isSelected) white else black
                    }
                    else -> {
                        backColor = layoutDefault!!
                        foreColor = if (isSelected) white else black
                    }
                }

                val darkerColor = when {
                    isSelected -> true
                    foreColor == Statics.textLightColor() -> true
                    else -> false
                }

                val titleForeColor: Int =
                    manipulateColor(foreColor, if (darkerColor) 0.8f else 1.4f)

                v.background = backColor
                holder.descriptionTextView?.setTextColor(foreColor)
                holder.codeTextView?.setTextColor(foreColor)
                holder.assetStatusTextView?.setTextColor(foreColor)
                holder.manufacturerTextView?.setTextColor(foreColor)
                holder.modelTextView?.setTextColor(foreColor)
                holder.checkBox?.buttonTintList = ColorStateList.valueOf(titleForeColor)

                // Titles
                holder.manufacturerTitleTextView?.setTextColor(titleForeColor)
                holder.modelTitleTextView?.setTextColor(titleForeColor)
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

    internal class CollapsedViewHolder {
        var checkBoxConstraintLayout: ConstraintLayout? = null
        var checkBox: CheckBox? = null

        var gralConstraintLayout: ConstraintLayout? = null
        var descriptionTextView: AutoResizeTextView? = null
        var codeTextView: AutoResizeTextView? = null
        var assetStatusTextView: CheckedTextView? = null

        var manufacturerConstraintLayout: ConstraintLayout? = null
        var manufacturerTextView: CheckedTextView? = null
        var modelTextView: CheckedTextView? = null

        // TextView de títulos
        var manufacturerTitleTextView: TextView? = null
        var modelTitleTextView: TextView? = null
    }

    internal class ExpandedViewHolder {
        var checkBoxConstraintLayout: ConstraintLayout? = null
        var checkBox: CheckBox? = null

        var gralConstraintLayout: ConstraintLayout? = null
        var descriptionTextView: AutoResizeTextView? = null
        var codeTextView: AutoResizeTextView? = null
        var assetStatusTextView: CheckedTextView? = null

        var locationConstraintLayout: ConstraintLayout? = null
        var warehouseTextView: AutoResizeTextView? = null
        var warehouseAreaTextView: AutoResizeTextView? = null

        var manufacturerConstraintLayout: ConstraintLayout? = null
        var manufacturerTextView: CheckedTextView? = null
        var modelTextView: CheckedTextView? = null

        var categoryConstraintLayout: ConstraintLayout? = null
        var categoryTextView: CheckedTextView? = null
        var ownershipTextView: CheckedTextView? = null

        var serialNumberConstraintLayout: ConstraintLayout? = null
        var serialNumberTextView: CheckedTextView? = null
        var eanTextView: CheckedTextView? = null

        var signImageView: AppCompatImageView? = null
        var addPhotoImageView: AppCompatImageView? = null
        var albumImageView: AppCompatImageView? = null
        var editImageView: AppCompatImageView? = null
        var imageControlConstraintLayout: ConstraintLayout? = null

        var divider1: View? = null
        var divider2: View? = null
        var divider3: View? = null
        var divider4: View? = null
        var divider5: View? = null // Divider para el imageControlLayout

        // TextView de títulos
        var manufacturerTitleTextView: TextView? = null
        var modelTitleTextView: TextView? = null
        var locationTitleTextView: TextView? = null
        var categoryTitleTextView: TextView? = null
        var ownershipTitleTextView: TextView? = null
        var serialNumberTitleTextView: TextView? = null
        var eanTitleTextView: TextView? = null
    }

    private var showAllOnFilterEmpty = false

    fun refreshFilter(s: String, showAllOnFilterEmpty: Boolean) {
        this.showAllOnFilterEmpty = showAllOnFilterEmpty
        filter.filter(s)
        refresh()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                var r: ArrayList<WarehouseMovementContent> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: WarehouseMovementContent

                        for (i in 0 until wmContArray.size) {
                            filterableItem = wmContArray[i]
                            if (isFilterable(filterableItem, filterString)) {
                                r.add(filterableItem)
                            }
                        }
                    } else if (showAllOnFilterEmpty) {
                        r = wmContArray
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
                    suggestedList.addAll(results.values as ArrayList<WarehouseMovementContent>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    fun isFilterable(filterableAsset: WarehouseMovementContent, filterString: String): Boolean =
        (filterableAsset.code.contains(filterString, true) ||
                filterableAsset.description.contains(filterString, true) ||
                filterableAsset.serialNumber.contains(filterString, true) ||
                filterableAsset.ean.contains(filterString, true))

    companion object {
        class WarehouseMovementContentComparator(private val priorityText: String) :
            Comparator<WarehouseMovementContent> {
            constructor() : this("")

            fun compareNullable(o1: WarehouseMovementContent?, o2: WarehouseMovementContent?): Int {
                return if (o1 == null || o2 == null) {
                    -1
                } else {
                    compare(o1, o2)
                }
            }

            override fun compare(o1: WarehouseMovementContent, o2: WarehouseMovementContent): Int {
                val firstField: Int
                val secondField: Int
                val thirdField: Int
                val fourthField: Int

                val fieldValue1: String = o1.code
                val fieldValue2: String = o2.code
                val secondValue1: String = o1.description
                val secondValue2: String = o2.description
                val thirdValue1: String = o1.ean
                val thirdValue2: String = o2.ean
                val fourthValue1: String = o1.serialNumber
                val fourthValue2: String = o2.serialNumber

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

                    thirdField =
                        if (thirdValue2.startsWith(priorityText, true) &&
                            !thirdValue1.startsWith(priorityText, true)
                        ) (return 1)
                        else if (thirdValue1.startsWith(priorityText, true) &&
                            !thirdValue2.startsWith(priorityText, true)
                        ) (return -1)
                        else if (thirdValue1.startsWith(priorityText, true) &&
                            thirdValue2.startsWith(priorityText, true)
                        ) (return thirdValue1.compareTo(thirdValue2))
                        else thirdValue1.compareTo(thirdValue2)

                    fourthField =
                        if (fourthValue2.startsWith(priorityText, true) &&
                            !fourthValue1.startsWith(priorityText, true)
                        ) (return 1)
                        else if (fourthValue1.startsWith(priorityText, true) &&
                            !fourthValue2.startsWith(priorityText, true)
                        ) (return -1)
                        else if (fourthValue1.startsWith(priorityText, true) &&
                            fourthValue2.startsWith(priorityText, true)
                        ) (return fourthValue1.compareTo(fourthValue2))
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

        fun sortItems(originalList: ArrayList<WarehouseMovementContent>): ArrayList<WarehouseMovementContent> {
            // Get all of the parent groups
            val groups = originalList
                .sortedWith(
                    compareBy(
                        { it.parentId },
                        { it.code },
                        { it.description },
                        { it.serialNumber },
                        { it.ean })
                )
                .groupBy { it.parentId }

            // Recursively get the children
            fun follow(wmc: WarehouseMovementContent): List<WarehouseMovementContent> {
                return listOf(wmc) + (groups[wmc.assetId]
                    ?: emptyList()).flatMap(::follow)
            }

            // Run the follow method on each of the roots
            return originalList.map { it.parentId }
                .subtract(originalList.map { it.assetId }.toSet())
                .flatMap { groups[it] ?: emptyList() }
                .flatMap(::follow) as ArrayList<WarehouseMovementContent>
        }
    }
}