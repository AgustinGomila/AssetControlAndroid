package com.dacosys.assetControl.adapters.sync

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.interfaces.Interfaces.*
import com.dacosys.assetControl.databinding.*
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.dataCollection.DataCollection
import com.dacosys.assetControl.model.location.Warehouse
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.movement.WarehouseMovement
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.route.RouteProcess
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.utils.Screen.Companion.manipulateColor
import com.dacosys.assetControl.utils.misc.Md5.Companion.getMd5
import com.dacosys.assetControl.utils.preferences.Repository.Companion.useImageControl
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.room.entity.Image
import com.dacosys.imageControl.ui.adapter.ImageAdapter
import com.dacosys.imageControl.ui.adapter.ImageAdapter.Companion.GetImageStatus
import com.dacosys.imageControl.ui.adapter.ImageAdapter.Companion.ImageControlHolder
import java.io.File
import java.math.BigInteger
import java.util.*

class SyncElementRecyclerAdapter private constructor(builder: Builder) :
    ListAdapter<Any, ViewHolder>(SyncElementDiffUtilCallback), Filterable {
    private var recyclerView: RecyclerView
    var fullList: ArrayList<Any> = ArrayList()
    var checkedKeyArray: ArrayList<String> = ArrayList()
    private var multiSelect: Boolean = false
    var showCheckBoxes: Boolean = false
    private var showCheckBoxesChanged: (Boolean) -> Unit = { }
    private var showImages: Boolean = false
    private var showImagesChanged: (Boolean) -> Unit = { }
    private var visibleRegistry: ArrayList<SyncRegistryType> = arrayListOf()
    private var filterOptions: FilterOptions = FilterOptions()

    // Este Listener debe usarse para los cambios de cantidad o de ítems marcados de la lista,
    // ya que se utiliza para actualizar los valores sumarios en la actividad.
    private var dataSetChangedListener: DataSetChangedListener? = null

    private var checkedChangedListener: CheckedChangedListener? = null

    // Listeners para los eventos de ImageControl.
    private var albumViewRequiredListener: AlbumViewRequiredListener? = null

    // Posición del ítem seleccionado
    private var currentIndex = NO_POSITION

    // Clase para distinguir actualizaciones parciales
    private enum class PAYLOADS {
        CHECKBOX_STATE,
        CHECKBOX_VISIBILITY,
        IMAGE_VISIBILITY,
        IMAGE_CONTROL_VISIBILITY,
        ITEM_SELECTED
    }

    /**
     * Show an images panel on the end of layout
     *
     * @param show
     */
    fun showImages(show: Boolean) {
        showImages = show
        showImagesChanged.invoke(showImages)
        notifyItemRangeChanged(0, itemCount, PAYLOADS.IMAGE_VISIBILITY)
    }

    /**
     * Change image control panel visibility on the bottom of the layout.
     * The state is defined by [useImageControl] preference property.
     *
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun showImageControlPanel() {
        notifyItemRangeChanged(currentIndex, 1, PAYLOADS.IMAGE_CONTROL_VISIBILITY)
    }

    // Keys de elementos de sincronización que tienen imágenes
    private var keyWithImage: ArrayList<String> = ArrayList()

    // Visibilidad del panel de miniaturas depende de la existencia de una imagen para ese activo.
    private fun imageVisibility(key: String): Int {
        return if (showImages && keyWithImage.contains(key)) VISIBLE else GONE
    }

    // Parámetros del filtro
    data class FilterOptions(
        var filterString: String = "",
        var showAllOnFilterEmpty: Boolean = true,
    )

    fun clear() {
        checkedKeyArray.clear()
        keyWithImage.clear()

        fullList.clear()
        submitList(fullList)
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener?,
        dataSetChangedListener: DataSetChangedListener?,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    fun refreshImageControlListeners(
        albumViewListener: AlbumViewRequiredListener?,
    ) {
        albumViewRequiredListener = albumViewListener
    }

    // El método onCreateViewHolder infla los diseños para cada tipo de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = SyncRegistryType.getById(viewType)
        return when (item) {
            SyncRegistryType.Asset -> {
                AssetViewHolder(
                    SyncElementAssetRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.ItemCategory -> {
                ItemCategoryViewHolder(
                    SyncElementItemCategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.Warehouse -> {
                WarehouseViewHolder(
                    SyncElementWarehouseRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.WarehouseArea -> {
                WarehouseAreaViewHolder(
                    SyncElementWarehouseAreaRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.DataCollection -> {
                DataCollectionViewHolder(
                    SyncElementDataCollectionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.RouteProcess -> {
                RouteProcessViewHolder(
                    SyncElementRouteProcessRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.WarehouseMovement -> {
                WarehouseMovementViewHolder(
                    SyncElementWarehouseMovementRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.AssetReview -> {
                AssetReviewViewHolder(
                    SyncElementAssetReviewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            SyncRegistryType.Image -> {
                ImageViewHolder(
                    SyncElementImageRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }

            else -> NullViewHolder(
                NullRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    // Sobrecarga del método onBindViewHolder para actualización parcial de las vistas
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // No hay payload, realizar la vinculación completa
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        // Hay payload, realizar la actualización parcial basada en el payload
        for (payload in payloads) {
            // Extraer el payload y utilizarlo para actualizar solo las vistas relevantes
            when (payload) {
                PAYLOADS.CHECKBOX_VISIBILITY -> {
                    when (holder) {
                        is AssetViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is ImageViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is ItemCategoryViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is WarehouseViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is WarehouseAreaViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is DataCollectionViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is RouteProcessViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is WarehouseMovementViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                        is AssetReviewViewHolder -> holder.bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                    }
                }

                PAYLOADS.CHECKBOX_STATE -> {
                    val syncElement = getItem(position)
                    val key = getKey(syncElement)
                    when (holder) {
                        is AssetViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is ImageViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is ItemCategoryViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is WarehouseViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is WarehouseAreaViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is DataCollectionViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is RouteProcessViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is WarehouseMovementViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                        is AssetReviewViewHolder -> holder.bindCheckBoxState(checkedKeyArray.contains(key))
                    }
                }

                PAYLOADS.IMAGE_VISIBILITY -> {
                    if (holder is ImageViewHolder) {
                        val syncElement = getItem(position)
                        getImagesThumbs(this, holder.icHolder, syncElement)
                        holder.bindImageVisibility(
                            imageVisibility = imageVisibility(getKey(syncElement)),
                            changingState = true
                        )
                    }
                }

                PAYLOADS.IMAGE_CONTROL_VISIBILITY -> {
                    if (holder is ImageViewHolder) {
                        holder.bindImageControlVisibility(if (useImageControl) VISIBLE else GONE)
                    }
                    if (!useImageControl) showImages(false)
                }

                PAYLOADS.ITEM_SELECTED -> {
                    // TODO: No regenerar la vista ante cambios de selección
                    // No está funcionando. La idea es usarlo para los cambios de selección.
                    // Pero por algún motivo los Payloads vienen vacíos luego de notifyItemChanged
                    super.onBindViewHolder(holder, position, payloads)
                }
            }
        }
    }

    // El método onBindViewHolder establece los valores de las vistas en función de los datos
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Evento de clic sobre el ítem que controla el estado de Selección para seleccionar el diseño adecuado
        holder.itemView.setOnClickListener {

            // Si el elemento ya está seleccionado quitar la selección
            if (currentIndex == holder.bindingAdapterPosition) {
                currentIndex = NO_POSITION
                notifyItemChanged(holder.bindingAdapterPosition)
            } else {
                // Notificamos los cambios para los dos ítems cuyo diseño necesita cambiar
                val previousSelectedItemPosition = currentIndex
                currentIndex = holder.bindingAdapterPosition
                notifyItemChanged(currentIndex)

                if (previousSelectedItemPosition != NO_POSITION) {
                    notifyItemChanged(previousSelectedItemPosition)
                }

                // Scroll para asegurarnos que se vea completamente el ítem
                holder.itemView.post { scrollToPos(currentIndex) }
            }

            // Seleccionamos el ítem
            holder.itemView.isSelected = currentIndex == position

            // Notificamos al Listener superior
            dataSetChangedListener?.onDataSetChanged()
        }

        // Establecer los valores para cada elemento según su posición con el estilo correspondiente
        val isSelected = currentIndex == position
        // Establece el estado seleccionado
        when (holder) {
            is AssetViewHolder -> setHolderAsset(holder, position, isSelected)
            is ItemCategoryViewHolder -> setHolderItemCategory(holder, position, isSelected)
            is WarehouseViewHolder -> setHolderWarehouse(holder, position, isSelected)
            is WarehouseAreaViewHolder -> setHolderWarehouseArea(holder, position, isSelected)
            is DataCollectionViewHolder -> setHolderDataCollection(holder, position, isSelected)
            is RouteProcessViewHolder -> setHolderRouteProcess(holder, position, isSelected)
            is WarehouseMovementViewHolder -> setHolderWarehouseMovement(holder, position, isSelected)
            is AssetReviewViewHolder -> setHolderAssetReview(holder, position, isSelected)
            is ImageViewHolder -> setHolderImage(holder, position, isSelected)
        }

        if (isSelected) {
            holder.itemView.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                getColorWithAlpha(colorId = R.color.lightslategray, alpha = 220), BlendModeCompat.MODULATE
            )
        } else {
            holder.itemView.background.colorFilter = null
        }
    }

    private fun setHolderAsset(holder: AssetViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderItemCategory(holder: ItemCategoryViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderWarehouse(holder: WarehouseViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderWarehouseArea(holder: WarehouseAreaViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderDataCollection(holder: DataCollectionViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderRouteProcess(holder: RouteProcessViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderWarehouseMovement(holder: WarehouseMovementViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderAssetReview(holder: AssetReviewViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)
    }

    private fun setHolderImage(holder: ImageViewHolder, position: Int, isSelected: Boolean) {
        val syncElement = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            syncElement = syncElement,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            imageVisibility = imageVisibility(getKey(syncElement)),
            isSelected = isSelected
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, syncElement, position)

        // Botones de acciones de ImageControl
        setAlbumViewLogic(holder.binding.albumImageView, syncElement)

        // Miniatura de ImageControl
        getImagesThumbs(this, holder.icHolder, syncElement)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAlbumViewLogic(albumImageView: AppCompatImageView?, syncElement: Any) {
        if (syncElement !is Image) return
        albumImageView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                albumViewRequiredListener?.onAlbumViewRequired(
                    tableId = syncElement.programObjectId.toInt(),
                    itemId = syncElement.objectId1?.toLong() ?: 0L,
                    filename = File(syncElement.filenameOriginal ?: "").name
                )
            }
            true
        }
    }

    /**
     * Lógica del evento de clic sostenido sobre el ítem que cambia la visibilidad de los CheckBox
     *
     * @param itemView Vista general del ítem
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setItemCheckBoxLogic(itemView: View) {
        if (!multiSelect) {
            showCheckBoxes = false
            return
        }

        val longClickListener = OnLongClickListener { _ ->
            showCheckBoxes = !showCheckBoxes
            showCheckBoxesChanged.invoke(showCheckBoxes)
            notifyItemRangeChanged(0, itemCount, PAYLOADS.CHECKBOX_VISIBILITY)
            return@OnLongClickListener true
        }

        itemView.isLongClickable = true
        itemView.setOnLongClickListener(longClickListener)
    }

    /**
     * Lógica del comportamiento del CheckBox de marcado de ítems cuando [multiSelect] es verdadero
     *
     * @param checkBox Control CheckBox para marcado del ítem
     * @param syncElement Datos del ítem
     * @param position Posición en el adaptador
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setCheckBoxLogic(checkBox: CheckBox, syncElement: Any, position: Int) {
        val checkChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            setChecked(syncElement = syncElement, isChecked = isChecked, suspendRefresh = false)
        }

        val longClickListener = OnLongClickListener { _ ->
            checkBox.setOnCheckedChangeListener(null)

            // Notificamos los cambios solo a los ítems que cambian de estado.
            val newState = !checkBox.isChecked
            if (newState) {
                currentList.mapIndexed { pos, syncElement ->
                    if (getKey(syncElement) !in checkedKeyArray) {
                        checkedKeyArray.add(getKey(syncElement))
                        notifyItemChanged(pos, PAYLOADS.CHECKBOX_STATE)
                    }
                }
            } else {
                currentList.mapIndexed { pos, syncElement ->
                    if (getKey(syncElement) in checkedKeyArray) {
                        checkedKeyArray.remove(getKey(syncElement))
                        notifyItemChanged(pos, PAYLOADS.CHECKBOX_STATE)
                    }
                }
            }

            // Notificamos al Listener superior
            dataSetChangedListener?.onDataSetChanged()
            return@OnLongClickListener true
        }

        // Important to remove previous checkedChangedListener before calling setChecked
        checkBox.setOnCheckedChangeListener(null)

        checkBox.isChecked = checkedKeyArray.contains(getKey(syncElement))
        checkBox.isLongClickable = true
        checkBox.tag = position

        checkBox.setOnLongClickListener(longClickListener)
        checkBox.setOnCheckedChangeListener(checkChangeListener)
    }

    // El método getItemViewType devuelve el tipo de vista que se usará para el elemento en la posición dada
    override fun getItemViewType(position: Int): Int {
        return getRegistryType(getItem(position))?.id ?: 0
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            var selected: Any? = null
            var firstVisible: Any? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                // Guardamos el item seleccionado y la posición del scroll
                selected = currentSyncElement()
                var scrollPos =
                    (recyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()
                        ?: NO_POSITION

                if (scrollPos != NO_POSITION && itemCount > scrollPos) {
                    var currentScrolled = getItem(scrollPos)

                    // Comprobamos si es visible el ítem del Scroll
                    if (isStatusVisible(currentScrolled))
                        firstVisible = currentScrolled
                    else {
                        // Si no es visible, intentar encontrar el próximo visible.
                        while (firstVisible == null) {
                            scrollPos++
                            if (itemCount > scrollPos) {
                                currentScrolled = getItem(scrollPos)
                                if (isStatusVisible(currentScrolled))
                                    firstVisible = currentScrolled
                            } else break
                        }
                    }
                }

                // Filtramos los resultados
                val results = FilterResults()
                var r: ArrayList<Any> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: Any

                        for (i in 0 until fullList.size) {
                            filterableItem = fullList[i]

                            // Descartamos aquellos que no debe ser visibles
                            if (!isStatusVisible(filterableItem)) continue
                        }
                    } else if (filterOptions.showAllOnFilterEmpty) {
                        r = ArrayList(fullList.mapNotNull { if (isStatusVisible(it)) it else null })
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
                submitList(results?.values as ArrayList<Any>) {
                    run {
                        // Notificamos al Listener superior
                        dataSetChangedListener?.onDataSetChanged()

                        // Recuperamos el item seleccionado y la posición del scroll
                        val fv = firstVisible
                        val s = selected
                        if (fv != null)
                            scrollToPos(getIndexByKey(getKey(fv)), true)
                        if (s != null)
                            selectItem(s, false)
                    }
                }
            }
        }
    }

    fun isVisible(pos: Int): Boolean {
        if (pos < 0) return false
        return isStatusVisible(getItem(pos))
    }

    private fun isStatusVisible(arc: Any?): Boolean {
        return if (arc != null) {
            when (arc) {
                is Asset -> return visibleRegistry.contains(SyncRegistryType.Asset)
                is ItemCategory -> return visibleRegistry.contains(SyncRegistryType.ItemCategory)
                is Warehouse -> return visibleRegistry.contains(SyncRegistryType.Warehouse)
                is WarehouseArea -> return visibleRegistry.contains(SyncRegistryType.WarehouseArea)
                is DataCollection -> return visibleRegistry.contains(SyncRegistryType.DataCollection)
                is RouteProcess -> return visibleRegistry.contains(SyncRegistryType.RouteProcess)
                is WarehouseMovement -> return visibleRegistry.contains(SyncRegistryType.WarehouseMovement)
                is AssetReview -> return visibleRegistry.contains(SyncRegistryType.AssetReview)
                is Image -> return visibleRegistry.contains(SyncRegistryType.Image)
                else -> return false
            }
        } else false
    }

    private fun sortItems(originalList: MutableList<Any>): ArrayList<Any> {
        return ArrayList(originalList.sortedWith(compareBy { getKey(it) }))
    }

    fun refreshFilter(options: FilterOptions) {
        filterOptions = options
        filter.filter(filterOptions.filterString)
    }

    private fun refreshFilter() {
        refreshFilter(filterOptions)
    }

    fun add(syncElement: Any, position: Int) {
        fullList.add(position, syncElement)
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
                selectItem(getIndex(syncElement))
            }
        }
    }

    fun remove(position: Int) {
        val id = getItem(position)
        checkedKeyArray.remove(id)

        fullList.removeAt(position)
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
            }
        }
    }

    fun selectItem(a: Any?, scroll: Boolean = true) {
        var pos = NO_POSITION
        if (a != null) pos = getIndex(a)
        selectItem(pos, scroll)
    }

    private fun selectItem(pos: Int, scroll: Boolean = true) {
        // Si la posición está fuera del rango válido, reseteamos currentIndex a NO_POSITION.
        currentIndex = if (pos < 0 || pos >= itemCount) NO_POSITION else pos
        notifyItemChanged(currentIndex)
        if (scroll) scrollToPos(currentIndex)
    }

    fun selectNearVisible() {
        var newPos = -1
        val allItems = fullList
        if (allItems.size > 0) {
            // Buscar el siguiente visible
            for (i in currentIndex until allItems.size) {
                if (isStatusVisible(i)) {
                    newPos = i
                    break
                }
            }

            // No encontró otro visible hacia adelante...
            // ir hacia atrás
            if (newPos == -1) {
                // Buscar el anterior visible
                for (i in currentIndex downTo 1) {
                    if (isStatusVisible(i)) {
                        newPos = i
                        break
                    }
                }
            }
        }

        selectItem(newPos)
    }

    /**
     * Scrolls to the given position, making sure the item can be fully displayed.
     *
     * @param position
     * @param scrollToTop If it is activated, the item will scroll until it is at the top of the view
     */
    fun scrollToPos(position: Int, scrollToTop: Boolean = false) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager

        if (position < 0 || position >= itemCount) {
            // La posición está fuera del rango válido, no se puede realizar el scroll
            return
        }

        val selectedView = layoutManager.findViewByPosition(position)

        if (scrollToTop) {
            // Hacemos scroll hasta que el ítem quede en la parte superior
            layoutManager.scrollToPositionWithOffset(position, 0)
        } else {
            if (selectedView != null) {
                // El ítem es visible, realizar scroll para asegurarse de que se vea completamente
                scrollToVisibleItem(selectedView)
            } else {
                // El ítem no es visible, realizar scroll directo a la posición
                recyclerView.scrollToPosition(position)
                recyclerView.addOnScrollListener(object : OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val selected = layoutManager.findViewByPosition(position)
                        if (selected != null) {
                            // El ítem se ha vuelto visible, realizar scroll para asegurarse de que se vea completamente
                            scrollToVisibleItem(selected)
                            recyclerView.removeOnScrollListener(this)
                        }
                    }
                })
            }
        }
    }

    /**
     * Scrolls to the given view, making sure the item can be fully displayed.
     *
     * @param selectedView
     */
    private fun scrollToVisibleItem(selectedView: View) {
        val recyclerViewHeight = recyclerView.height - recyclerView.paddingTop - recyclerView.paddingBottom
        val selectedViewHeight = selectedView.height + selectedView.marginTop + selectedView.marginBottom

        val selectedViewTop = selectedView.top - selectedView.marginTop
        val selectedViewBottom = selectedView.bottom + selectedView.marginBottom

        if (selectedViewTop < 0) {
            // El ítem está parcialmente oculto en la parte superior del RecyclerView
            recyclerView.smoothScrollBy(0, selectedViewTop)
        } else if (selectedViewBottom > recyclerViewHeight) {
            // El ítem está parcialmente oculto en la parte inferior del RecyclerView
            val visibleHeight = recyclerViewHeight - selectedViewTop
            recyclerView.smoothScrollBy(0, selectedViewHeight - visibleHeight)
        }
    }

    fun getIndexByKey(key: String): Int {
        return currentList.indexOfFirst { getKey(it) == key }
    }

    private fun getIndex(syncElement: Any): Int {
        return currentList.indexOfFirst { it == syncElement }
    }

    private fun getSyncElementByKey(key: String): Any? {
        return fullList.firstOrNull { getKey(it) == key }
    }

    fun getAllChecked(): ArrayList<Any> {
        val items = ArrayList<Any>()
        checkedKeyArray.mapNotNullTo(items) { getSyncElementByKey(it) }
        return items
    }

    fun currentPos(): Int {
        return currentIndex
    }

    fun currentSyncElement(): Any? {
        if (currentIndex == NO_POSITION) return null
        return if (currentList.any() && itemCount > currentIndex) getItem(currentIndex)
        else null
    }

    val countChecked: Int
        get() = checkedKeyArray.size

    val totalVisible: Int
        get() = itemCount

    val totalAsset: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.Asset }
    val totalItemCategory: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.ItemCategory }
    val totalWarehouse: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.Warehouse }
    val totalWarehouseArea: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.WarehouseArea }
    val totalDataCollection: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.DataCollection }
    val totalRouteProcess: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.RouteProcess }
    val totalWarehouseMovement: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.WarehouseMovement }
    val totalAssetReview: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.AssetReview }
    val totalImage: Int
        get() = fullList.count { getRegistryType(it) == SyncRegistryType.Image }


    fun firstVisiblePos(): Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        return layoutManager.findFirstVisibleItemPosition()
    }

    fun setChecked(syncElement: Any, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val pos = getIndexByKey(getKey(syncElement))
        checkedKeyArray.remove(getKey(syncElement))
        if (isChecked) checkedKeyArray.add(getKey(syncElement))

        // Notificamos al Listener superior
        if (!suspendRefresh) checkedChangedListener?.onCheckedChanged(isChecked, pos)
    }

    fun getVisibleStatus(): ArrayList<SyncRegistryType> {
        return visibleRegistry
    }

    fun addVisibleStatus(status: SyncRegistryType) {
        if (visibleRegistry.contains(status)) return
        visibleRegistry.add(status)

        refreshFilter()
    }

    fun removeVisibleStatus(status: SyncRegistryType) {
        if (!visibleRegistry.contains(status)) return
        visibleRegistry.remove(status)

        // Quitamos los ítems con el estado seleccionado de la lista marcados.
        val uncheckedItems =
            ArrayList(fullList.mapNotNull { if (getRegistryType(it) == status) getKey(it) else null })
        checkedKeyArray.removeAll(uncheckedItems.toSet())

        refreshFilter()
    }

    internal class AssetViewHolder(val binding: SyncElementAssetRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is Asset) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.descriptionAutoSize.text = syncElement.description
            binding.code.text = syncElement.code
            binding.assetStatus.text = AssetStatus.getById(syncElement.assetStatusId)?.description ?: ""

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_blue, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedAssetForeColor else assetForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.descriptionAutoSize.setTextColor(foreColor)
            binding.code.setTextColor(foreColor)
            binding.assetStatus.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class ItemCategoryViewHolder(val binding: SyncElementItemCategoryRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is ItemCategory) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.itemCategoryStr.text = syncElement.description
            binding.parentStr.text = syncElement.parentStr

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedItemCategoryForeColor else itemCategoryForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.itemCategoryStr.setTextColor(foreColor)
            binding.parentStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class WarehouseViewHolder(val binding: SyncElementWarehouseRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is Warehouse) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.warehouseStr.text = syncElement.description

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_red, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedWarehouseForeColor else warehouseForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.warehouseStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class WarehouseAreaViewHolder(val binding: SyncElementWarehouseAreaRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is WarehouseArea) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.warehouseAreaStr.text = syncElement.description
            binding.warehouseStr.text = syncElement.warehouseStr

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_blue, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedWarehouseAreaForeColor else warehouseAreaForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.warehouseAreaStr.setTextColor(foreColor)
            binding.warehouseStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class DataCollectionViewHolder(val binding: SyncElementDataCollectionRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is DataCollection) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.targetStr.text = when {
                syncElement.assetCode.isNotEmpty() -> syncElement.assetCode
                syncElement.warehouseStr.isNotEmpty() -> syncElement.warehouseStr
                syncElement.warehouseAreaStr.isNotEmpty() -> syncElement.warehouseAreaStr
                else -> ""
            }
            binding.dataCollectionDate.text = syncElement.dateEnd

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_violet2, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedDataCollectionForeColor else dataCollectionForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.targetStr.setTextColor(foreColor)
            binding.dataCollectionDate.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class RouteProcessViewHolder(val binding: SyncElementRouteProcessRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is RouteProcess) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.routeStr.text = syncElement.routeStr
            binding.routeProcessDate.text = syncElement.routeProcessDate

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_violet3, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedRouteProcessForeColor else routeProcessForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.routeStr.setTextColor(foreColor)
            binding.routeProcessDate.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class WarehouseMovementViewHolder(val binding: SyncElementWarehouseMovementRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is WarehouseMovement) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.origWarehouseAreaStr.text = syncElement.origWarehouseAreaStr
            binding.destWarehouseAreaStr.text = syncElement.destWarehouseAreaStr
            binding.obs.text = syncElement.obs
            if (syncElement.obs.isEmpty()) {
                binding.obs.visibility = GONE
                binding.dividerObs.visibility = GONE
            } else {
                binding.obs.visibility = VISIBLE
                binding.dividerObs.visibility = VISIBLE
            }
            binding.warehouseMovementDate.text = syncElement.warehouseMovementDate

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedWarehouseMovementForeColor else warehouseMovementForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.origWarehouseAreaStr.setTextColor(foreColor)
            binding.destWarehouseAreaStr.setTextColor(foreColor)
            binding.obs.setTextColor(foreColor)
            binding.warehouseMovementDate.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class AssetReviewViewHolder(val binding: SyncElementAssetReviewRowBinding) :
        ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is AssetReview) return

            bindCheckBoxVisibility(checkBoxVisibility)

            binding.warehouseAreaStr.text = syncElement.warehouseAreaStr
            binding.obs.text = syncElement.obs
            if (syncElement.obs.isEmpty()) {
                binding.obs.visibility = GONE
                binding.dividerObs.visibility = GONE
            } else {
                binding.obs.visibility = VISIBLE
                binding.dividerObs.visibility = VISIBLE
            }
            binding.assetReviewDate.text = syncElement.assetReviewDate

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow2, null) ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedAssetReviewForeColor else assetReviewForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.warehouseAreaStr.setTextColor(foreColor)
            binding.obs.setTextColor(foreColor)
            binding.assetReviewDate.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class ImageViewHolder(val binding: SyncElementImageRowBinding) :
        ViewHolder(binding.root) {
        val icHolder: ImageControlHolder = ImageControlHolder().apply {
            imageConstraintLayout = binding.imageConstraintLayout
            imageImageView = binding.imageView
            progressBar = binding.progressBar
        }

        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        /**
         * Bind image visibility
         *
         * @param imageVisibility Visibility of the image panel
         * @param changingState Only if we are changing the visibility state, we expand the panel
         */
        fun bindImageVisibility(imageVisibility: Int, changingState: Boolean) {
            if (!useImageControl || imageVisibility == GONE) collapseImagePanel(icHolder)
            else if (changingState) expandImagePanel(icHolder)
        }

        /**
         * Bind image control visibility
         *
         * @param visibility Visibility of the image control panel
         */
        fun bindImageControlVisibility(visibility: Int = GONE) {
            binding.imageControlConstraintLayout.visibility = visibility
        }

        fun bind(syncElement: Any, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE, isSelected: Boolean) {
            if (syncElement !is Image) return

            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageControlVisibility(visibility = if (useImageControl) VISIBLE else GONE)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)

            binding.descriptionTv.text = syncElement.description
            binding.obsArTv.text = syncElement.obs
            binding.filenameTv.text = File(syncElement.filenameOriginal ?: "").name

            setStyle(isSelected)
        }

        private fun setStyle(isSelected: Boolean) {
            val v = itemView

            // Background layouts
            val layout = getDrawable(getContext().resources, R.drawable.layout_thin_border_orange, null)
                ?: return

            val backColor: Drawable = layout
            val foreColor: Int = if (isSelected) selectedImageForeColor else imageForeColor
            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.descriptionTv.setTextColor(foreColor)
            binding.obsArTv.setTextColor(foreColor)
            binding.filenameTv.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)
            binding.albumImageView.imageTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.titleTextView.setTextColor(titleForeColor)
        }
    }

    internal class NullViewHolder(val binding: NullRowBinding) :
        ViewHolder(binding.root)

    private object SyncElementDiffUtilCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return getKey(oldItem) == getKey(newItem)
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return getKey(oldItem) == getKey(newItem)
        }
    }

    companion object {
        fun getKey(item: Any): String {
            /**
             * Md5 hash to int
             *
             * @param hash
             * @return Entero de 10 dígitos de longitud que puede usarse como identificador
             */
            fun md5HashToInt(hash: String): Long {
                val bigInt = BigInteger(hash, 16)
                val maxVal = BigInteger.TEN.pow(10).subtract(BigInteger.ONE)
                return bigInt.mod(maxVal).toLong()
            }

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

                is Image -> {
                    regId = SyncRegistryType.Image.id.toString()
                    itemId = md5HashToInt(item.hash ?: getMd5(item.objectId1.toString())).toString()
                }
            }

            val regIdStr = String.format("%0" + (4 - regId.length).toString() + "d%s", 0, regId)
            val itemIdStr = String.format("%0" + (11 - itemId.length).toString() + "d%s", 0, itemId)
            return "${regIdStr}${itemIdStr}"
        }

        fun getRegistryType(item: Any): SyncRegistryType? {
            return when (item) {
                is Asset -> SyncRegistryType.Asset
                is ItemCategory -> SyncRegistryType.ItemCategory
                is Warehouse -> SyncRegistryType.Warehouse
                is WarehouseArea -> SyncRegistryType.WarehouseArea
                is DataCollection -> SyncRegistryType.DataCollection
                is RouteProcess -> SyncRegistryType.RouteProcess
                is WarehouseMovement -> SyncRegistryType.WarehouseMovement
                is AssetReview -> SyncRegistryType.AssetReview
                is Image -> SyncRegistryType.Image

                else -> null
            }
        }

        fun collapseImagePanel(icHolder: ImageControlHolder) {
            icHolder.imageConstraintLayout?.post { icHolder.imageConstraintLayout?.visibility = GONE }
            icHolder.imageImageView?.post { icHolder.imageImageView?.visibility = INVISIBLE }
            icHolder.progressBar?.post { icHolder.progressBar?.visibility = GONE }
        }

        private fun waitingImagePanel(icHolder: ImageControlHolder) {
            icHolder.imageImageView?.post { icHolder.imageImageView?.visibility = INVISIBLE }
            icHolder.progressBar?.post { icHolder.progressBar?.visibility = VISIBLE }
            icHolder.imageConstraintLayout?.post { icHolder.imageConstraintLayout?.visibility = VISIBLE }
        }

        private fun expandImagePanel(icHolder: ImageControlHolder) {
            icHolder.imageImageView?.post { icHolder.imageImageView?.visibility = VISIBLE }
            icHolder.progressBar?.post { icHolder.progressBar?.visibility = GONE }
            icHolder.imageConstraintLayout?.post { icHolder.imageConstraintLayout?.visibility = VISIBLE }
        }

        private fun showImagePanel(icHolder: ImageControlHolder, image: Bitmap) {
            icHolder.imageImageView?.post { icHolder.imageImageView?.setImageBitmap(image) }
            expandImagePanel(icHolder)
        }

        // region COLORS
        private var assetForeColor: Int = 0
        private var imageForeColor: Int = 0
        private var itemCategoryForeColor: Int = 0
        private var warehouseForeColor: Int = 0
        private var warehouseAreaForeColor: Int = 0
        private var dataCollectionForeColor: Int = 0
        private var routeProcessForeColor: Int = 0
        private var warehouseMovementForeColor: Int = 0
        private var assetReviewForeColor: Int = 0

        private var selectedAssetForeColor: Int = 0
        private var selectedImageForeColor: Int = 0
        private var selectedItemCategoryForeColor: Int = 0
        private var selectedWarehouseForeColor: Int = 0
        private var selectedWarehouseAreaForeColor: Int = 0
        private var selectedDataCollectionForeColor: Int = 0
        private var selectedRouteProcessForeColor: Int = 0
        private var selectedWarehouseMovementForeColor: Int = 0
        private var selectedAssetReviewForeColor: Int = 0

        private var darkslategray: Int = 0
        private var lightgray: Int = 0

        /**
         * Setup colors
         * Simplemente inicializamos algunas variables con los colores que vamos a usar para cada estado.
         */
        private fun setupColors() {
            // Color de los diferentes estados
            val asset = getColor(getContext().resources, R.color.sync_element_asset, null)
            val image = getColor(getContext().resources, R.color.sync_element_image, null)
            val itemCategory = getColor(getContext().resources, R.color.sync_element_item_category, null)
            val warehouse = getColor(getContext().resources, R.color.sync_element_warehouse, null)
            val warehouseArea = getColor(getContext().resources, R.color.sync_element_warehouse_area, null)
            val dataCollection = getColor(getContext().resources, R.color.sync_element_data_collection, null)
            val routeProcess = getColor(getContext().resources, R.color.sync_element_route_process, null)
            val warehouseMovement = getColor(getContext().resources, R.color.sync_element_warehouse_movement, null)
            val assetReview = getColor(getContext().resources, R.color.sync_element_asset_review, null)

            // Mejor contraste para los ítems seleccionados
            selectedAssetForeColor = getBestContrastColor(manipulateColor(asset, 0.5f))
            selectedImageForeColor = getBestContrastColor(manipulateColor(image, 0.5f))
            selectedItemCategoryForeColor = getBestContrastColor(manipulateColor(itemCategory, 0.5f))
            selectedWarehouseForeColor = getBestContrastColor(manipulateColor(warehouse, 0.5f))
            selectedWarehouseAreaForeColor = getBestContrastColor(manipulateColor(warehouseArea, 0.5f))
            selectedDataCollectionForeColor = getBestContrastColor(manipulateColor(dataCollection, 0.5f))
            selectedRouteProcessForeColor = getBestContrastColor(manipulateColor(routeProcess, 0.5f))
            selectedWarehouseMovementForeColor = getBestContrastColor(manipulateColor(warehouseMovement, 0.5f))
            selectedAssetReviewForeColor = getBestContrastColor(manipulateColor(assetReview, 0.5f))

            // Mejor contraste para los ítems no seleccionados
            assetForeColor = getBestContrastColor(asset)
            imageForeColor = getBestContrastColor(image)
            itemCategoryForeColor = getBestContrastColor(itemCategory)
            warehouseForeColor = getBestContrastColor(warehouse)
            warehouseAreaForeColor = getBestContrastColor(warehouseArea)
            dataCollectionForeColor = getBestContrastColor(dataCollection)
            routeProcessForeColor = getBestContrastColor(routeProcess)
            warehouseMovementForeColor = getBestContrastColor(warehouseMovement)
            assetReviewForeColor = getBestContrastColor(assetReview)

            // CheckBox color
            darkslategray = getColor(getContext().resources, R.color.darkslategray, null)

            // Title color
            lightgray = getColor(getContext().resources, R.color.lightgray, null)
        }
        // endregion

        /**
         * Get images thumbs
         *
         * @param adapter Parent adapter
         * @param holder ImageControl image holder
         * @param syncElement Any of which we are requesting the image
         */
        fun getImagesThumbs(
            adapter: SyncElementRecyclerAdapter,
            holder: ImageControlHolder,
            syncElement: Any
        ) {
            if (!adapter.showImages) return
            if (syncElement !is Image) return

            Handler(Looper.getMainLooper()).postDelayed({
                adapter.run {
                    val objectId1 = syncElement.objectId1 ?: ""
                    val matchFilename = File(syncElement.filenameOriginal ?: "").name

                    ImageAdapter.getImages(
                        context = getContext(),
                        programData = ProgramData(
                            programObjectId = syncElement.programObjectId,
                            objId1 = objectId1
                        ),
                        matchFilename = matchFilename,
                        onProgress = {
                            when (it.status) {
                                GetImageStatus.STARTING -> {
                                    waitingImagePanel(holder)
                                }

                                GetImageStatus.NO_IMAGES -> {
                                    keyWithImage.remove(syncElement.objectId1)
                                    collapseImagePanel(holder)
                                }

                                GetImageStatus.IMAGE_BROKEN, GetImageStatus.NO_AVAILABLE, GetImageStatus.IMAGE_AVAILABLE -> {
                                    if (objectId1.isNotEmpty() && !keyWithImage.contains(objectId1)) {
                                        keyWithImage.add(objectId1)
                                    }
                                    val tempImage = it.image
                                    if (tempImage != null) showImagePanel(holder, tempImage)
                                    else collapseImagePanel(holder)
                                }
                            }
                        })
                }
            }, 0)
        }
    }

    init {
        // Set values
        recyclerView = builder.recyclerView
        fullList = builder.fullList
        checkedKeyArray = builder.checkedKeyArray
        multiSelect = builder.multiSelect
        showCheckBoxes = builder.showCheckBoxes
        showCheckBoxesChanged = builder.showCheckBoxesChanged
        showImages = builder.showImages
        showImagesChanged = builder.showImagesChanged
        visibleRegistry = builder.visibleRegistryType
        filterOptions = builder.filterOptions

        dataSetChangedListener = builder.dataSetChangedListener
        checkedChangedListener = builder.checkedChangedListener
        albumViewRequiredListener = builder.albumViewRequiredListener

        // Configuramos variables de estilo que se van a reutilizar.
        setupColors()

        // Por el momento no queremos animaciones, ni transiciones ante cambios en el DataSet
        recyclerView.itemAnimator = null

        // Vamos a retener en el caché un [cacheFactor] por ciento de los ítems creados o un máximo de [maxCachedItems]
        val maxCachedItems = 50
        val cacheFactor = 0.10
        var cacheSize = (fullList.size * cacheFactor).toInt()
        if (cacheSize > maxCachedItems) cacheSize = maxCachedItems
        recyclerView.setItemViewCacheSize(cacheSize)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.Asset.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.ItemCategory.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.Warehouse.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.WarehouseArea.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.DataCollection.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.RouteProcess.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.WarehouseMovement.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.AssetReview.id, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(SyncRegistryType.Image.id, 0)

        // Ordenamiento natural de la lista completa para trabajar en adelante con una lista ordenada
        val tList = sortItems(fullList)
        fullList = tList

        // Suministramos la lista a publicar refrescando el filtro que recorre la lista completa y devuelve los resultados filtrados y ordenados
        refreshFilter(filterOptions)

        // Cambiamos la visibilidad del panel de imágenes.
        if (!useImageControl) showImages = false
        showImages(showImages)
    }

    /**
     * Return a sorted list of visible state items
     *
     * @param list
     * @return Lista ordenada con los estados visibles
     */
    private fun sortedVisibleList(list: MutableList<Any>?): MutableList<Any> {
        val croppedList =
            (list ?: mutableListOf()).mapNotNull { if (isStatusVisible(it)) it else null }
        return sortItems(croppedList.toMutableList())
    }

    // Sobrecargamos estos métodos para suministrar siempre una lista ordenada y filtrada por estado de visibilidad
    override fun submitList(list: MutableList<Any>?) {
        super.submitList(sortedVisibleList(list))
    }

    override fun submitList(list: MutableList<Any>?, commitCallback: Runnable?) {
        super.submitList(sortedVisibleList(list), commitCallback)
    }

    class Builder {
        internal lateinit var recyclerView: RecyclerView
        internal var fullList: ArrayList<Any> = ArrayList()
        internal var checkedKeyArray: ArrayList<String> = ArrayList()
        internal var multiSelect: Boolean = false
        internal var showCheckBoxes: Boolean = false
        internal var showCheckBoxesChanged: (Boolean) -> Unit = { }
        internal var showImages: Boolean = false
        internal var showImagesChanged: (Boolean) -> Unit = { }
        internal var visibleRegistryType: ArrayList<SyncRegistryType> = arrayListOf()
        internal var filterOptions: FilterOptions = FilterOptions()

        internal var dataSetChangedListener: DataSetChangedListener? = null
        internal var checkedChangedListener: CheckedChangedListener? = null
        internal var albumViewRequiredListener: AlbumViewRequiredListener? = null

        // Setter methods for variables with chained methods
        fun recyclerView(`val`: RecyclerView): Builder {
            recyclerView = `val`
            return this
        }

        fun fullList(`val`: ArrayList<Any>): Builder {
            fullList = `val`
            return this
        }

        fun checkedKeyArray(`val`: ArrayList<String>): Builder {
            checkedKeyArray = `val`
            return this
        }

        fun multiSelect(`val`: Boolean): Builder {
            multiSelect = `val`
            return this
        }

        @Suppress("unused")
        fun showCheckBoxes(`val`: Boolean, callback: (Boolean) -> Unit): Builder {
            showCheckBoxes = `val`
            showCheckBoxesChanged = callback
            return this
        }

        @Suppress("unused")
        fun showImages(`val`: Boolean, callback: (Boolean) -> Unit): Builder {
            showImages = `val`
            showImagesChanged = callback
            return this
        }

        fun visibleRegistryTypes(`val`: ArrayList<SyncRegistryType>): Builder {
            visibleRegistryType = `val`
            return this
        }

        fun filterOptions(`val`: FilterOptions): Builder {
            filterOptions = `val`
            return this
        }

        fun dataSetChangedListener(listener: DataSetChangedListener?): Builder {
            dataSetChangedListener = listener
            return this
        }

        fun checkedChangedListener(listener: CheckedChangedListener?): Builder {
            checkedChangedListener = listener
            return this
        }

        fun albumViewRequiredListener(listener: AlbumViewRequiredListener?): Builder {
            albumViewRequiredListener = listener
            return this
        }

        fun build(): SyncElementRecyclerAdapter {
            return SyncElementRecyclerAdapter(this)
        }
    }
}