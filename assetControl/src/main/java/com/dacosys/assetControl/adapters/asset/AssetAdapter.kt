package com.dacosys.assetControl.adapters.asset

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
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
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.asset.OwnershipStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.views.custom.AutoResizeTextView
import com.dacosys.assetControl.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.utils.Screen.Companion.isTablet
import com.dacosys.assetControl.utils.Screen.Companion.manipulateColor
import com.dacosys.assetControl.utils.Screen.Companion.textLightColor
import com.dacosys.assetControl.utils.preferences.Repository
import java.lang.ref.WeakReference
import java.util.*


/**
 * Created by Agustin on 18/01/2017.
 */

@Suppress("SpellCheckingInspection")
class AssetAdapter : ArrayAdapter<Asset>, Filterable {
    private var activity: AppCompatActivity
    private var resource: Int = 0
    private var suspendReport = false
    private var lastSelectedPos = -1

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

    private var weakRefAlbumViewRequiredListener: WeakReference<AlbumViewRequiredListener?>? = null
    private var albumViewRequiredListener: AlbumViewRequiredListener?
        get() {
            return weakRefAlbumViewRequiredListener?.get()
        }
        set(newValue) {
            weakRefAlbumViewRequiredListener = WeakReference(newValue)
        }

    private var weakRefAddPhotoRequiredListener: WeakReference<AddPhotoRequiredListener?>? = null
    private var addPhotoRequiredListener: AddPhotoRequiredListener?
        get() {
            return weakRefAddPhotoRequiredListener?.get()
        }
        set(newValue) {
            weakRefAddPhotoRequiredListener = WeakReference(newValue)
        }

    private var weakRefEditAssetRequiredListener: WeakReference<EditAssetRequiredListener?>? = null
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

    private var multiSelect: Boolean = false

    private var assetArray: ArrayList<Asset> = ArrayList()
    private var suggestedList: ArrayList<Asset> = ArrayList()

    private var visibleStatus: ArrayList<AssetStatus> = ArrayList()
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        assets: ArrayList<Asset>,
        suggestedList: ArrayList<Asset>,
        visibleStatus: ArrayList<AssetStatus>,
    ) : super(AssetControlApp.getContext(), resource, suggestedList) {
        this.activity = activity
        this.resource = resource
        this.visibleStatus = visibleStatus
        this.assetArray = assets
        this.suggestedList = suggestedList
    }

    constructor(
        activity: AppCompatActivity,
        resource: Int,
        assets: ArrayList<Asset>,
        suggestedList: ArrayList<Asset>,
        listView: ListView?,
        multiSelect: Boolean,
        checkedIdArray: ArrayList<Long>,
        visibleStatus: ArrayList<AssetStatus>,

        ) : super(AssetControlApp.getContext(), resource, suggestedList) {
        this.activity = activity
        this.resource = resource
        this.multiSelect = multiSelect

        this.assetArray = assets
        this.suggestedList = suggestedList
        this.visibleStatus = visibleStatus
        this.checkedIdArray = checkedIdArray

        this.listView = listView

        setupColors()
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
    private fun refreshItems(originalList: ArrayList<Asset>) {
        val sortedList = sortItems(originalList)

        activity.runOnUiThread {
            super.clear()
            super.addAll(sortedList)
        }

        refresh()
    }

    override fun add(asset: Asset?) {
        val all = getAll()
        var someChanges = false

        activity.runOnUiThread {
            if (asset != null) {
                if (!all.contains(asset)) {
                    someChanges = true
                    all.add(asset)
                    reportAssetAdded(arrayListOf(asset))
                }
            }

            if (!someChanges) return@runOnUiThread

            refreshItems(all)

            if (asset != null) forceSelectItem(asset)
        }
    }

    fun add(wacs: ArrayList<Asset>, scrollToPosition: Boolean) {
        val all = getAll()
        val wacsAdded: ArrayList<Asset> = ArrayList()

        activity.runOnUiThread {
            for (w in wacs) {
                if (!all.contains(w)) {
                    wacsAdded.add(w)
                    all.add(w)
                }
            }

            if (!wacsAdded.any()) return@runOnUiThread

            reportAssetAdded(wacsAdded)

            refreshItems(all)

            if (scrollToPosition) forceSelectItem(wacsAdded[0])
        }
    }

    /**
     * Se utiliza cuando se edita un activo y necesita actualizarse
     */
    fun updateAsset(asset: Asset, selectItem: Boolean = true) {
        for (i in 0 until count) {
            val t = (getItem(i) as Asset)
            if (t == asset) {
                activity.runOnUiThread {
                    t.assetId = asset.assetId
                    t.code = asset.code
                    t.description = asset.description
                    t.warehouseId = asset.warehouseId
                    t.warehouseAreaId = asset.warehouseAreaId
                    t.active = asset.active
                    t.ownershipStatusId = asset.ownershipStatusId
                    t.assetStatusId = asset.assetStatusId
                    t.missingDate = asset.missingDate
                    t.itemCategoryId = asset.itemCategoryId
                    t.transferred = asset.transferred
                    t.originalWarehouseId = asset.originalWarehouseId
                    t.originalWarehouseAreaId = asset.originalWarehouseAreaId
                    t.labelNumber = asset.labelNumber
                    t.manufacturer = asset.manufacturer
                    t.model = asset.model
                    t.serialNumber = asset.serialNumber
                    t.assetConditionId = asset.assetConditionId
                    t.parentAssetId = asset.parentAssetId
                    t.ean = asset.ean
                    t.lastAssetReviewDate = asset.lastAssetReviewDate
                }

                dataSetChangedListener?.onDataSetChanged()

                if (selectItem) forceSelectItem(asset)

                break
            }
        }
    }

    override fun clear() {
        activity.runOnUiThread {
            super.clear()
            clearChecked()

            dataSetChangedListener?.onDataSetChanged()
        }
    }

    override fun remove(asset: Asset?) {
        if (asset != null) {
            remove(arrayListOf(asset))
        }
    }

    fun remove(assets: ArrayList<Asset>) {
        if (assets.isEmpty()) return

        val all = getAll()
        lastSelectedPos = currentPos()

        val assetsRemoved: ArrayList<Asset> = ArrayList()
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

            reportAssetRemoved(assetsRemoved)

            refreshItems(all)

            Handler(Looper.getMainLooper()).postDelayed({ run { selectItem(lastSelectedPos) } }, 20)
        }
    }

    /**
     * Muestra un mensaje en pantalla con los códigos agregados
     */
    private fun reportAssetAdded(assetArray: ArrayList<Asset>) {
        if (suspendReport) {
            return
        }
        if (assetArray.size <= 0) {
            return
        }

        var res = ""
        for (asset in assetArray) {
            res += "${asset.code}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " + if (assetArray.size > 1) " ${
            AssetControlApp.getContext().getString(R.string.added_plural)
        }" else " ${AssetControlApp.getContext().getString(R.string.added)}"

        makeText(activity, res, SnackBarType.ADD)
        Log.d(this::class.java.simpleName, res)
    }

    /**
     * Muestra un mensaje en pantalla con los códigos eliminados
     */
    private fun reportAssetRemoved(assetArray: ArrayList<Asset>) {
        // En modo arqueo no se muestran los carteles de eliminación de assets
        // porque nunca se eliminan sino que se ponen en cero
        if (suspendReport) {
            return
        }
        if (assetArray.size <= 0) {
            return
        }

        var res = ""
        for (asset in assetArray) {
            res += "${asset.code}, "
        }

        if (res.endsWith(", ")) {
            res = res.substring(0, res.length - 2)
        }

        res += ": " + if (assetArray.size > 1) " ${
            AssetControlApp.getContext().getString(R.string.removed_plural)
        }" else " ${AssetControlApp.getContext().getString(R.string.removed)}"

        makeText(activity, res, SnackBarType.REMOVE)
        Log.d(this::class.java.simpleName, res)
    }

    private fun getIndex(asset: Asset): Int {
        for (i in 0 until count) {
            val t = (getItem(i) as Asset)
            if (t == asset) {
                return i
            }
        }
        return -1
    }

    fun count(): Int {
        return count
    }

    fun maxHeightNeeded(): Int {
        return count() * defaultDropDownItemHeight()
    }

    fun getAllId(): ArrayList<Long> {
        val r: ArrayList<Long> = ArrayList()
        for (i in 0 until count) {
            val it = getItem(i)
            if (it != null) {
                r.add(it.assetId)
            }
        }
        return r
    }

    fun getAll(): ArrayList<Asset> {
        val r: ArrayList<Asset> = ArrayList()
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
    fun setChecked(assets: ArrayList<Asset>, isChecked: Boolean) {
        if (isFilling) {
            return
        }
        isFilling = true

        for (i in assets) {
            setChecked(i, isChecked)
        }

        isFilling = false
        refresh()
    }

    fun setChecked(asset: Asset, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val position = getIndex(asset)
        if (isChecked) {
            if (!checkedIdArray.contains(asset.assetId)) {
                checkedIdArray.add(asset.assetId)
            }
        } else {
            checkedIdArray.remove(asset.assetId)
        }

        checkedChangedListener?.onCheckedChanged(isChecked, position)

        if (!suspendRefresh) {
            refresh()
        }
    }

    override fun sort(comparator: Comparator<in Asset>) {
        super.sort(customComparator)
    }

    private val customComparator = Comparator { o1: Asset?, o2: Asset? ->
        AssetComparator().compareNullable(
            o1, o2
        )
    }

    fun refresh() {
        activity.runOnUiThread { notifyDataSetChanged() }
        dataSetChangedListener?.onDataSetChanged()
    }

    /**
     * Fuerza la selección aunque el ítem esté previamente seleccionado.
     */
    private fun forceSelectItem(a: Asset) {
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

    fun setChecked(checkedItems: ArrayList<Asset>) {
        checkedItems.clear()
        setChecked(checkedItems, true)
    }

    fun clearChecked() {
        checkedIdArray.clear()
    }

    fun setSelectItemAndScrollPos(a: Asset?, tScrollPos: Int?) {
        var pos = -1
        if (a != null) pos = getPosition(a)
        var scrollPos = -1
        if (tScrollPos != null) scrollPos = tScrollPos
        selectItem(pos, scrollPos, false)
    }

    fun selectItem(a: Asset?) {
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

    fun currentAsset(): Asset? {
        return (0 until count).firstOrNull { isSelected(it) }?.let {
            val t = getItem(it)
            t
        }
    }

    fun currentPos(): Int {
        return (0 until count).firstOrNull { isSelected(it) } ?: -1
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

    fun isStatusVisible(asset: Asset?): Boolean {
        return if (asset != null) {
            (visibleStatus.contains(AssetStatus.getById(asset.assetStatusId)))
        } else false
    }

    fun setVisibleStatus(status: ArrayList<AssetStatus>) {
        visibleStatus = status
        refresh()
    }

    fun addVisibleStatus(status: AssetStatus) {
        if (!visibleStatus.contains(status)) {
            visibleStatus.add(status)
            refresh()
        }
    }

    fun removeVisibleStatus(status: AssetStatus) {
        if (visibleStatus.contains(status)) {
            visibleStatus.remove(status)
            refresh()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        var alreadyExists = true

        // Seleccionamos el layout dependiendo si es
        // un row visible u oculto según su AsseStatus.

        val currentLayout: Int = if (listView == null) {
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
                v.tag is String && currentLayout == R.layout.asset_row || v.tag is String && currentLayout == R.layout.asset_row_expanded || v.tag is String && currentLayout == R.layout.asset_simple_row ||

                v.tag is CollapsedViewHolder && currentLayout != R.layout.asset_row || v.tag is ExpandedViewHolder && currentLayout != R.layout.asset_row_expanded || v.tag is SimpleViewHolder && currentLayout != R.layout.asset_simple_row
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
            R.layout.asset_simple_row -> fillSimpleView(position, v!!, alreadyExists)
            R.layout.asset_row_expanded -> fillExpandedItemView(position, v!!, alreadyExists)
            else -> fillCollapsedListView(position, v!!, alreadyExists)
        }
        return v
    }

    private fun fillNullView(v: View): View {
        v.tag = ""
        return v
    }

    private fun createSimpleViewHolder(v: View, holder: SimpleViewHolder) {
        // Holder para los rows de dropdown.
        holder.checkBox = v.findViewById(R.id.checkBox)
        holder.codeCheckedTextView = v.findViewById(R.id.codeCheckedTextView)
        holder.descriptionCheckedTextView = v.findViewById(R.id.descriptionCheckedTextView)
        holder.serialNumberCheckedTextView = v.findViewById(R.id.serialNumberCheckedTextView)

        if (multiSelect) {
            holder.checkBox?.visibility = VISIBLE
        } else {
            holder.checkBox?.visibility = GONE
        }

        v.tag = holder
    }

    @SuppressLint("ClickableViewAccessibility", "ObsoleteSdkInt")
    private fun fillSimpleView(position: Int, v: View, alreadyExists: Boolean): View {
        var holder = SimpleViewHolder()
        if (alreadyExists) {
            if (v.tag is SimpleViewHolder || v.tag is String) {
                createSimpleViewHolder(v, holder)
            } else {
                holder = v.tag as SimpleViewHolder
            }
        } else {
            createSimpleViewHolder(v, holder)
        }

        if (position >= 0) {
            val asset = getItem(position)

            if (asset != null) {
                holder.codeCheckedTextView?.text = asset.code
                holder.descriptionCheckedTextView?.text = asset.description
                holder.serialNumberCheckedTextView?.text = asset.serialNumber ?: ""

                if (holder.checkBox != null) {
                    var isSpeakButtonLongPressed = false

                    val checkChangeListener =
                        CompoundButton.OnCheckedChangeListener { _, isChecked ->
                            this.setChecked(asset, isChecked, true)
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
                    holder.checkBox!!.isChecked = checkedIdArray.contains(asset.assetId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background colors
                val lightgray = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.lightgray, null
                )
                val white = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.text_light, null
                )

                // Font colors
                val black = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.text_dark, null
                )
                val dimgray = ResourcesCompat.getColor(
                    AssetControlApp.getContext().resources, R.color.dimgray, null
                )

                val colorText = when {
                    !asset.active -> dimgray
                    else -> black
                }

                val backColor = when {
                    !asset.active -> lightgray
                    else -> white
                }

                v.setBackgroundColor(backColor)
                holder.descriptionCheckedTextView?.setTextColor(colorText)
                holder.serialNumberCheckedTextView?.setTextColor(colorText)
            }
        }
        if (v.height > 0) {
            Log.d(this::class.java.simpleName, "-------{RES: $resource Height:${v.height}}-------")
        }
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

        if (Repository.useImageControl) {
            holder.signImageView?.visibility = GONE
            holder.addPhotoImageView?.visibility = VISIBLE
            holder.albumImageView?.visibility = VISIBLE
        } else {
            holder.signImageView?.visibility = GONE
            holder.addPhotoImageView?.visibility = GONE
            holder.albumImageView?.visibility = GONE
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
            val asset = getItem(position)
            val isSelected = isSelected(position)

            if (asset != null) {
                holder.descriptionTextView?.text = asset.description
                holder.codeTextView?.text = asset.code
                holder.assetStatusTextView?.text =
                    AssetStatus.getById(asset.assetStatusId)?.description ?: ""

                // region Manufacturer
                val manufacturerStr = asset.manufacturer ?: ""
                val modelStr = asset.model ?: ""

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
                val wStr = asset.warehouseStr
                val waStr = asset.warehouseAreaStr

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
                val categoryStr = asset.itemCategoryStr
                val ownershipStr =
                    OwnershipStatus.getById(asset.ownershipStatusId)?.description ?: ""

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
                val serialNumberStr = asset.serialNumber ?: ""
                val eanStr = asset.ean ?: ""

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
                            Table.asset.tableId, asset.assetId
                        )
                    }
                    true
                }

                // region ImageControl
                if (Repository.useImageControl) {
                    if (holder.albumImageView != null) {
                        holder.albumImageView!!.setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                albumViewRequiredListener?.onAlbumViewRequired(
                                    Table.asset.tableId, asset.assetId
                                )
                            }
                            true
                        }
                    }
                    if (holder.addPhotoImageView != null) {
                        holder.addPhotoImageView!!.setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                addPhotoRequiredListener?.onAddPhotoRequired(
                                    Table.asset.tableId, asset.assetId, asset.description
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
                            this.setChecked(asset, isChecked, true)
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
                    holder.checkBox!!.isChecked = checkedIdArray.contains(asset.assetId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }
                // endregion CheckBox

                // Background layouts
                // Resalta por estado del activo
                val layoutOnIventory = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutMissing = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources, R.drawable.layout_thin_border_red, null
                )
                val layoutRemoved = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )
                val layoutDefault = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources, R.drawable.layout_thin_border, null
                )

                val backColor: Drawable
                val foreColor: Int
                when (asset.assetStatusId) {
                    AssetStatus.onInventory.id -> {
                        backColor = layoutOnIventory!!
                        foreColor = if (isSelected) selectedForeColor else onInventoryForeColor
                    }
                    AssetStatus.missing.id -> {
                        backColor = layoutMissing!!
                        foreColor = if (isSelected) selectedForeColor else missingForeColor
                    }
                    AssetStatus.removed.id -> {
                        backColor = layoutRemoved!!
                        foreColor = if (isSelected) selectedForeColor else removedForeColor
                    }
                    else -> {
                        backColor = layoutDefault!!
                        foreColor = if (isSelected) selectedForeColor else defaultForeColor
                    }
                }

                val darkerColor = when {
                    isSelected -> true
                    foreColor == textLightColor() -> true
                    else -> false
                }

                val titleForeColor: Int = manipulateColor(
                    foreColor, if (darkerColor) 0.8f else 1.4f
                )

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
                            getColorWithAlpha(
                                colorId = R.color.lightslategray, alpha = 240
                            ), BlendModeCompat.MODULATE
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
            val asset = getItem(position)
            val isSelected = isSelected(position)

            if (asset != null) {
                holder.descriptionTextView?.text = asset.description
                holder.codeTextView?.text = asset.code
                holder.assetStatusTextView!!.text =
                    AssetStatus.getById(asset.assetStatusId)?.description ?: ""

                // region Manufacturer
                val manufacturerStr = asset.manufacturer ?: ""
                val modelStr = asset.model ?: ""

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
                            this.setChecked(asset, isChecked, true)
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
                    holder.checkBox!!.isChecked = checkedIdArray.contains(asset.assetId)
                    holder.checkBox!!.tag = position
                    holder.checkBox!!.setOnLongClickListener(pressHoldListener)
                    holder.checkBox!!.setOnTouchListener(pressTouchListener)
                    holder.checkBox!!.setOnCheckedChangeListener(checkChangeListener)
                }

                // Background layouts
                // Resalta por estado del activo
                val layoutOnIventory = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_green,
                    null
                )
                val layoutMissing = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources, R.drawable.layout_thin_border_red, null
                )
                val layoutRemoved = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources,
                    R.drawable.layout_thin_border_yellow,
                    null
                )
                val layoutDefault = ResourcesCompat.getDrawable(
                    AssetControlApp.getContext().resources, R.drawable.layout_thin_border, null
                )

                val backColor: Drawable
                val foreColor: Int
                val titleForeColor: Int = if (isSelected) lightgray else darkslategray

                when (asset.assetStatusId) {
                    AssetStatus.onInventory.id -> {
                        backColor = layoutOnIventory!!
                        foreColor = if (isSelected) selectedForeColor else onInventoryForeColor
                    }
                    AssetStatus.missing.id -> {
                        backColor = layoutMissing!!
                        foreColor = if (isSelected) selectedForeColor else missingForeColor
                    }
                    AssetStatus.removed.id -> {
                        backColor = layoutRemoved!!
                        foreColor = if (isSelected) selectedForeColor else removedForeColor
                    }
                    else -> {
                        backColor = layoutDefault!!
                        foreColor = if (isSelected) selectedForeColor else defaultForeColor
                    }
                }

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
                            getColorWithAlpha(
                                colorId = R.color.lightslategray, alpha = 240
                            ), BlendModeCompat.MODULATE
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

    internal class SimpleViewHolder {
        var codeCheckedTextView: CheckedTextView? = null
        var descriptionCheckedTextView: CheckedTextView? = null
        var serialNumberCheckedTextView: CheckedTextView? = null
        var checkBox: CheckBox? = null
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
                var r: ArrayList<Asset> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: Asset

                        for (i in 0 until assetArray.size) {
                            filterableItem = assetArray[i]
                            if (isFilterable(filterableItem, filterString)) {
                                r.add(filterableItem)
                            }
                        }
                    } else if (showAllOnFilterEmpty) {
                        r = assetArray
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

    fun isFilterable(filterableAsset: Asset, filterString: String): Boolean =
        (filterableAsset.code.contains(filterString, true) || filterableAsset.description.contains(
            filterString, true
        ) || (filterableAsset.serialNumber ?: "").contains(
            filterString, true
        ) || (filterableAsset.ean ?: "").contains(filterString, true))

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

    //region COLORS

    private var selectedForeColor: Int = 0
    private var onInventoryForeColor: Int = 0
    private var missingForeColor: Int = 0
    private var removedForeColor: Int = 0
    private var defaultForeColor: Int = 0
    private var darkslategray: Int = 0
    private var lightgray: Int = 0

    private fun setupColors() {
        selectedForeColor = ResourcesCompat.getColor(
            AssetControlApp.getContext().resources, R.color.text_light, null
        )

        onInventoryForeColor = getBestContrastColor("#009688")
        missingForeColor = getBestContrastColor("#F44336")
        removedForeColor = getBestContrastColor("#FFC107")
        defaultForeColor = getBestContrastColor("#DFDFDF")

        // CheckBox color
        darkslategray = ResourcesCompat.getColor(
            AssetControlApp.getContext().resources, R.color.darkslategray, null
        )

        // Title color
        lightgray = ResourcesCompat.getColor(
            AssetControlApp.getContext().resources, R.color.lightgray, null
        )
    }

    //endregion

    companion object {

        fun defaultRowHeight(): Int {
            return if (isTablet()) 73 else 110
        }

        fun defaultDropDownItemHeight(): Int {
            return if (isTablet()) 54 else 90
            //(54 + 138) / 2 else (90 + 115) / 2
        }

        class AssetComparator(private val priorityText: String) : Comparator<Asset> {
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

                if (priorityText.isNotEmpty()) {
                    firstField = if (fieldValue2.startsWith(
                            priorityText, ignoreCase = true
                        ) && !fieldValue1.startsWith(priorityText, true)
                    ) (return 1)
                    else if (fieldValue1.startsWith(priorityText, true) && !fieldValue2.startsWith(
                            priorityText, true
                        )
                    ) (return -1)
                    else if (fieldValue1.startsWith(priorityText, true) && fieldValue2.startsWith(
                            priorityText, true
                        )
                    ) (return fieldValue1.compareTo(fieldValue2))
                    else (fieldValue1.compareTo(fieldValue2))

                    secondField = if (secondValue2.startsWith(
                            priorityText, ignoreCase = true
                        ) && !secondValue1.startsWith(priorityText, true)
                    ) (return 1)
                    else if (secondValue1.startsWith(
                            priorityText, true
                        ) && !secondValue2.startsWith(priorityText, true)
                    ) (return -1)
                    else if (secondValue1.startsWith(priorityText, true) && secondValue2.startsWith(
                            priorityText, true
                        )
                    ) (return secondValue1.compareTo(secondValue2))
                    else (secondValue1.compareTo(secondValue2))

                    thirdField =
                        if (thirdValue2.startsWith(priorityText, true) && !thirdValue1.startsWith(
                                priorityText, true
                            )
                        ) (return 1)
                        else if (thirdValue1.startsWith(
                                priorityText, true
                            ) && !thirdValue2.startsWith(priorityText, true)
                        ) (return -1)
                        else if (thirdValue1.startsWith(
                                priorityText, true
                            ) && thirdValue2.startsWith(priorityText, true)
                        ) (return thirdValue1.compareTo(thirdValue2))
                        else thirdValue1.compareTo(thirdValue2)

                    fourthField =
                        if (fourthValue2.startsWith(priorityText, true) && !fourthValue1.startsWith(
                                priorityText, true
                            )
                        ) (return 1)
                        else if (fourthValue1.startsWith(
                                priorityText, true
                            ) && !fourthValue2.startsWith(priorityText, true)
                        ) (return -1)
                        else if (fourthValue1.startsWith(
                                priorityText, true
                            ) && fourthValue2.startsWith(priorityText, true)
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

        fun sortItems(originalList: ArrayList<Asset>): ArrayList<Asset> {
            // Get all of the parent groups
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

        interface AlbumViewRequiredListener {
            // Define data you like to return from AysncTask
            fun onAlbumViewRequired(tableId: Int, itemId: Long)
        }

        interface EditAssetRequiredListener {
            // Define data you like to return from AysncTask
            fun onEditAssetRequired(tableId: Int, itemId: Long)
        }

        interface AddPhotoRequiredListener {
            // Define data you like to return from AysncTask
            fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String)
        }
    }
}