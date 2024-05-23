package com.dacosys.assetControl.ui.adapters.movement

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
import android.widget.CompoundButton.OnCheckedChangeListener
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
import com.dacosys.assetControl.AssetControlApp.Companion.currentUser
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.asset.OwnershipStatus
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.enums.movement.WarehouseMovementContentStatus
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.databinding.AssetRowBinding
import com.dacosys.assetControl.databinding.AssetRowExpandedBinding
import com.dacosys.assetControl.ui.adapters.asset.AssetRecyclerAdapter.FilterOptions
import com.dacosys.assetControl.ui.adapters.interfaces.Interfaces.*
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.manipulateColor
import com.dacosys.assetControl.utils.settings.preferences.Repository.Companion.useImageControl
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.ui.adapter.ImageAdapter
import com.dacosys.imageControl.ui.adapter.ImageAdapter.Companion.GetImageStatus
import com.dacosys.imageControl.ui.adapter.ImageAdapter.Companion.ImageControlHolder
import java.util.*


/**
 * Created by Agustin on 10/03/2023.
 */

class WmcRecyclerAdapter(
    private val recyclerView: RecyclerView,
    var fullList: ArrayList<WarehouseMovementContent>,
    var checkedIdArray: ArrayList<Long> = ArrayList(),
    var multiSelect: Boolean = false,
    private var allowEditAsset: Boolean = false,
    var showCheckBoxes: Boolean = false,
    private var showCheckBoxesChanged: (Boolean) -> Unit = { },
    private var showImages: Boolean = false,
    private var showImagesChanged: (Boolean) -> Unit = { },
    var visibleStatus: ArrayList<WarehouseMovementContentStatus> = ArrayList(WarehouseMovementContentStatus.getAll()),
    private var filterOptions: FilterOptions = FilterOptions()
) : ListAdapter<WarehouseMovementContent, ViewHolder>(WarehouseMovementContentDiffUtilCallback), Filterable {

    private var currentIndex = NO_POSITION
    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null
    private var editAssetListener: EditAssetRequiredListener? = null

    // Listeners para los eventos de ImageControl.
    private var addPhotoRequiredListener: AddPhotoRequiredListener? = null
    private var albumViewRequiredListener: AlbumViewRequiredListener? = null

    // Clase para distinguir actualizaciones parciales
    private enum class PAYLOADS {
        STATUS_CHANGE,
        CHECKBOX_VISIBILITY,
        IMAGE_VISIBILITY,
        IMAGE_CONTROL_VISIBILITY
    }

    fun clear() {
        checkedIdArray.clear()
        idWithImage.clear()

        fullList.clear()
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
            }
        }
    }

    fun refreshListeners(
        checkedChangedListener: CheckedChangedListener? = null,
        dataSetChangedListener: DataSetChangedListener? = null,
        editAssetListener: EditAssetRequiredListener? = null,
    ) {
        this.editAssetListener = editAssetListener
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    fun refreshImageControlListeners(
        addPhotoListener: AddPhotoRequiredListener? = null,
        albumViewListener: AlbumViewRequiredListener? = null,
    ) {
        addPhotoRequiredListener = addPhotoListener
        albumViewRequiredListener = albumViewListener
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

    // Ids de activos que tienen imágenes
    private var idWithImage: ArrayList<Long> = ArrayList()

    // Visibilidad del panel de miniaturas depende de la existencia de una imagen para ese activo.
    private fun imageVisibility(assetId: Long): Int {
        return if (showImages && idWithImage.contains(assetId)) VISIBLE else GONE
    }

    // Permiso de edición de activos
    private var userHasPermissionToEdit: Boolean = User.hasPermission(PermissionEntry.ModifyAsset)

    private object WarehouseMovementContentDiffUtilCallback : DiffUtil.ItemCallback<WarehouseMovementContent>() {
        override fun areItemsTheSame(oldItem: WarehouseMovementContent, newItem: WarehouseMovementContent): Boolean {
            return oldItem.assetId == newItem.assetId
        }

        override fun areContentsTheSame(oldItem: WarehouseMovementContent, newItem: WarehouseMovementContent): Boolean {
            if (oldItem.id != newItem.id) return false
            if (oldItem.contentStatusId != newItem.contentStatusId) return false
            if (oldItem.assetId != newItem.assetId) return false
            if (oldItem.code != newItem.code) return false
            if (oldItem.description != newItem.description) return false
            if (oldItem.assetStatusId != newItem.assetStatusId) return false
            if (oldItem.warehouseAreaId != newItem.warehouseAreaId) return false
            if (oldItem.labelNumber != newItem.labelNumber) return false
            if (oldItem.parentId != newItem.parentId) return false
            if (oldItem.qty != newItem.qty) return false
            if (oldItem.warehouseAreaDescription != newItem.warehouseAreaDescription) return false
            if (oldItem.warehouseDescription != newItem.warehouseDescription) return false
            if (oldItem.itemCategoryId != newItem.itemCategoryId) return false
            if (oldItem.itemCategoryDescription != newItem.itemCategoryDescription) return false
            if (oldItem.ownershipStatusId != newItem.ownershipStatusId) return false
            if (oldItem.manufacturer != newItem.manufacturer) return false
            if (oldItem.model != newItem.model) return false
            if (oldItem.serialNumber != newItem.serialNumber) return false
            return oldItem.ean == newItem.ean
        }
    }

    companion object {
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

        // Aquí definimos dos constantes para identificar los dos diseños diferentes
        const val SELECTED_VIEW_TYPE = 1
        const val UNSELECTED_VIEW_TYPE = 2

        // region COLORS
        private var layoutToMoveForeColor: Int = 0
        private var layoutNoNeedToMoveForeColor: Int = 0
        private var layoutDefaultForeColor: Int = 0

        private var layoutToMoveSelectedForeColor: Int = 0
        private var layoutNoNeedToMoveSelectedForeColor: Int = 0
        private var layoutDefaultSelectedForeColor: Int = 0

        private fun setupColors() {
            // Color de los diferentes estados
            layoutToMoveForeColor = getBestContrastColor(getColor(getContext().resources, R.color.status_to_move, null))
            layoutNoNeedToMoveForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_no_need_to_move, null))
            layoutDefaultForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_default, null))

            // Mejor contraste para los ítems seleccionados
            layoutToMoveSelectedForeColor = getBestContrastColor(manipulateColor(layoutToMoveForeColor, 0.5f))
            layoutNoNeedToMoveSelectedForeColor =
                getBestContrastColor(manipulateColor(layoutNoNeedToMoveForeColor, 0.5f))
            layoutDefaultSelectedForeColor = getBestContrastColor(manipulateColor(layoutDefaultForeColor, 0.5f))
        }
        // endregion
    }

    // El método onCreateViewHolder infla los diseños para cada tipo de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SELECTED_VIEW_TYPE -> {
                SelectedViewHolder(
                    AssetRowExpandedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                UnselectedViewHolder(
                    AssetRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
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
                    if (position == currentIndex)
                        (holder as SelectedViewHolder).bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                    else
                        (holder as UnselectedViewHolder).bindCheckBoxVisibility(if (showCheckBoxes) VISIBLE else GONE)
                }

                PAYLOADS.STATUS_CHANGE -> {
                    ignoreCheckBoxStateChanged = true
                    val content = getItem(position)
                    val isChecked = checkedIdArray.contains(content.assetId)
                    if (position == currentIndex) {
                        (holder as SelectedViewHolder).bindCheckBoxState(isChecked)
                        holder.bindStatusChange(content)
                        holder.setStyle(content)
                    } else {
                        (holder as UnselectedViewHolder).bindCheckBoxState(isChecked)
                        holder.bindStatusChange(content)
                        holder.setStyle(content)
                    }
                    ignoreCheckBoxStateChanged = false
                }

                PAYLOADS.IMAGE_VISIBILITY -> {
                    val assetId = getItem(position)?.assetId ?: -1
                    if (position == currentIndex) {
                        getImagesThumbs(this, (holder as SelectedViewHolder).icHolder, assetId)
                        holder.bindImageVisibility(
                            imageVisibility = imageVisibility(assetId),
                            changingState = true
                        )
                    } else {
                        getImagesThumbs(this, (holder as UnselectedViewHolder).icHolder, assetId)
                        holder.bindImageVisibility(
                            imageVisibility = imageVisibility(assetId),
                            changingState = true
                        )
                    }
                }

                PAYLOADS.IMAGE_CONTROL_VISIBILITY -> {
                    if (position == currentIndex) {
                        (holder as SelectedViewHolder).bindImageControlVisibility(if (useImageControl) VISIBLE else GONE)
                    }
                    if (!useImageControl) showImages(false)
                }
            }
        }
    }

    // El método onBindViewHolder establece los valores de las vistas en función de los datos
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Aquí puedes establecer los valores para cada elemento
        holder.itemView.setOnClickListener {

            // Si el elemento ya está seleccionado, quitamos la selección.
            if (currentIndex == holder.bindingAdapterPosition) {
                currentIndex = NO_POSITION
                notifyItemChanged(holder.bindingAdapterPosition)
            } else {
                val previousSelectedItemPosition = currentIndex
                currentIndex = holder.bindingAdapterPosition
                notifyItemChanged(currentIndex)

                if (previousSelectedItemPosition != NO_POSITION) {
                    notifyItemChanged(previousSelectedItemPosition)
                }
            }

            // Seleccionamos el ítem
            holder.itemView.isSelected = currentIndex == position
        }

        // Actualiza la vista según el estado de selección del elemento
        if (currentIndex == position) {
            // Establece el estado seleccionado
            setSelectedHolder(holder as SelectedViewHolder, position)
        } else {
            // Establece el estado no seleccionado
            setUnselectedHolder(holder as UnselectedViewHolder, position)
        }
    }

    private fun setSelectedHolder(holder: SelectedViewHolder, position: Int) {
        val content = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            content = content,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE
        )

        holder.itemView.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 220), BlendModeCompat.MODULATE
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, content, position)

        // Acción de edición
        setEditLogic(holder.binding.editImageView, content.assetId)

        // Botones de acciones de ImageControl
        setAddSignLogic(holder.binding.signImageView)
        setAlbumViewLogic(holder.binding.albumImageView, content.assetId)
        setAddPhotoLogic(holder.binding.addPhotoImageView, content.assetId, content.description)

        // Miniatura de ImageControl
        getImagesThumbs(this, holder.icHolder, content.assetId)
    }

    private fun setUnselectedHolder(holder: UnselectedViewHolder, position: Int) {
        val content = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        // Perform a full update
        holder.bind(
            content = content,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE
        )

        holder.itemView.background.colorFilter = null

        setCheckBoxLogic(holder.binding.checkBox, content, position)

        // Miniatura de ImageControl
        getImagesThumbs(this, holder.icHolder, content.assetId)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setEditLogic(editImageView: AppCompatImageView, assetId: Long) {
        if (!userHasPermissionToEdit || !allowEditAsset) {
            editImageView.visibility = GONE
            return
        }

        editImageView.visibility = VISIBLE

        editImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                editAssetListener?.onEditAssetRequired(
                    tableId = Table.asset.id, itemId = assetId
                )
            }
            true
        }
    }

    private fun setAddSignLogic(signImageView: AppCompatImageView) {
        signImageView.visibility = GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAlbumViewLogic(albumImageView: AppCompatImageView, assetId: Long) {
        albumImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                albumViewRequiredListener?.onAlbumViewRequired(
                    tableId = Table.asset.id, itemId = assetId
                )
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAddPhotoLogic(addPhotoImageView: AppCompatImageView, assetId: Long, assetDescription: String) {
        addPhotoImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                addPhotoRequiredListener?.onAddPhotoRequired(
                    tableId = Table.asset.id,
                    itemId = assetId,
                    description = assetDescription,
                    obs = "${getContext().getString(R.string.user)}: ${currentUser()?.name}"
                )
            }
            true
        }
    }

    @get:Synchronized
    private var ignoreCheckBoxStateChanged = false

    /**
     * Lógica del comportamiento del CheckBox de marcado de ítems cuando [multiSelect] es verdadero
     *
     * @param checkBox Control CheckBox para marcado del ítem
     * @param content Datos del ítem
     * @param position Posición en el adaptador
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setCheckBoxLogic(checkBox: CheckBox, content: WarehouseMovementContent, position: Int) {
        val checkChangeListener = OnCheckedChangeListener { _, isChecked ->
            if (!ignoreCheckBoxStateChanged) {

                updateCheckedList(content = content, isChecked = isChecked)

                notifyItemChanged(position, PAYLOADS.STATUS_CHANGE)
                checkedChangedListener?.onCheckedChanged(true, position)
            }
        }

        val longClickListener = OnLongClickListener { _ ->
            val isChecked = !checkBox.isChecked

            for (tempCont in currentList) {
                updateCheckedList(content = tempCont, isChecked = isChecked)
            }

            notifyItemRangeChanged(0, itemCount, PAYLOADS.STATUS_CHANGE)
            dataSetChangedListener?.onDataSetChanged()
            return@OnLongClickListener true
        }

        // Important to remove previous checkedChangedListener before calling setChecked
        checkBox.setOnCheckedChangeListener(null)

        checkBox.isChecked = checkedIdArray.contains(content.assetId)
        checkBox.isLongClickable = true
        checkBox.tag = position

        checkBox.setOnLongClickListener(longClickListener)
        checkBox.setOnCheckedChangeListener(checkChangeListener)
    }

    /**
     * Get images thumbs
     *
     * @param adapter Parent adapter
     * @param holder ImageControl image holder
     * @param assetId Asset ID of which we are requesting the image
     */
    private fun getImagesThumbs(
        adapter: WmcRecyclerAdapter,
        holder: ImageControlHolder,
        assetId: Long
    ) {
        if (!adapter.showImages) return

        Handler(Looper.getMainLooper()).postDelayed({
            adapter.run {
                ImageAdapter.getImages(context = getContext(), programData = ProgramData(
                    programObjectId = Table.asset.id.toLong(), objId1 = assetId.toString()
                ), onProgress = {
                    when (it.status) {
                        GetImageStatus.STARTING -> {
                            waitingImagePanel(holder)
                        }

                        GetImageStatus.NO_IMAGES -> {
                            idWithImage.remove(assetId)
                            collapseImagePanel(holder)
                        }

                        GetImageStatus.IMAGE_BROKEN, GetImageStatus.NO_AVAILABLE, GetImageStatus.IMAGE_AVAILABLE -> {
                            if (!idWithImage.contains(assetId)) {
                                idWithImage.add(assetId)
                            }
                            val image = it.image
                            if (image != null) showImagePanel(holder, image)
                            else collapseImagePanel(holder)
                        }
                    }
                })
            }
        }, 0)
    }

    // El método getItemCount devuelve el número de elementos en la lista
    override fun getItemCount(): Int {
        return currentList.size
    }

    // El método getItemViewType devuelve el tipo de vista que se usará para el elemento en la posición dada
    override fun getItemViewType(position: Int): Int {
        return if (currentIndex == position) {
            SELECTED_VIEW_TYPE
        } else {
            UNSELECTED_VIEW_TYPE
        }
    }

    override fun getFilter(): Filter {
        var selected: WarehouseMovementContent? = null
        var firstVisible: WarehouseMovementContent? = null

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                // Guardamos el item seleccionado y la posición del scroll
                selected = currentItem()
                val vsIds = visibleStatus.map { it.id }.toList()
                var scrollPos =
                    (recyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()
                        ?: NO_POSITION

                if (scrollPos != NO_POSITION && itemCount > scrollPos) {
                    var currentScrolled = getItem(scrollPos)

                    // Comprobamos si es visible el ítem del Scroll
                    if (currentScrolled.contentStatusId in vsIds)
                        firstVisible = currentScrolled
                    else {
                        // Si no es visible, intentar encontrar el próximo visible.
                        while (firstVisible == null) {
                            scrollPos++
                            if (itemCount > scrollPos) {
                                currentScrolled = getItem(scrollPos)
                                if (currentScrolled.contentStatusId in vsIds)
                                    firstVisible = currentScrolled
                            } else break
                        }
                    }
                }

                // Filtramos los resultados
                val results = FilterResults()
                var r: ArrayList<WarehouseMovementContent> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: WarehouseMovementContent

                        for (i in 0 until fullList.size) {
                            filterableItem = fullList[i]

                            // Descartamos aquellos que no debe ser visibles
                            if (filterableItem.contentStatusId !in vsIds) continue

                            if (isFilterable(filterableItem, filterString)) {
                                r.add(filterableItem)
                            }
                        }
                    } else if (filterOptions.showAllOnFilterEmpty) {
                        r = ArrayList(fullList.map { it })
                    }
                }

                results.values = r
                results.count = r.size
                return results
            }

            fun isFilterable(content: WarehouseMovementContent, filterString: String): Boolean =
                content.description.contains(filterString, true) ||
                        content.code.contains(filterString, true) ||
                        content.ean.orEmpty().contains(filterString, true)

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                var contents: ArrayList<WarehouseMovementContent> = ArrayList()
                if (results?.values != null) {
                    contents = results.values as ArrayList<WarehouseMovementContent>
                }
                submitList(contents) {
                    run {
                        // Notificamos al Listener superior
                        dataSetChangedListener?.onDataSetChanged()

                        // Recuperamos el item seleccionado y la posición del scroll
                        if (firstVisible != null)
                            scrollToPos(getIndexById(firstVisible?.assetId ?: -1), true)
                        if (selected != null)
                            selectItem(selected, false)
                    }
                }
            }
        }
    }

    private fun sortItems(originalList: MutableList<WarehouseMovementContent>): ArrayList<WarehouseMovementContent> {
        // Run the follow method on each of the roots
        return ArrayList(
            originalList.sortedWith(
                compareBy(
                    { it.description },
                    { it.code },
                    { it.ean },
                )
            ).toList()
        )
    }

    fun refreshFilter(options: FilterOptions) {
        filterOptions = options
        filter.filter(filterOptions.filterString)
    }

    private fun refreshFilter() {
        refreshFilter(filterOptions)
    }

    fun add(contents: ArrayList<WarehouseMovementContent>, scrollToPos: Boolean) {
        for (content in contents) {
            val position = fullList.lastIndex + 1
            fullList.add(position, content)
        }
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                if (scrollToPos) selectItem(fullList.lastIndex)
            }
        }
    }

    fun add(content: WarehouseMovementContent?) {
        if (content == null) return
        val lastPost = fullList.lastIndex + 1
        add(content, lastPost)
    }

    fun add(content: WarehouseMovementContent, position: Int) {
        fullList.add(position, content)
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
                selectItem(getIndex(content))
            }
        }
    }

    /**
     * Se utiliza cuando se edita un activo y necesita actualizarse
     */
    fun updateContent(asset: Asset, scrollToPos: Boolean = false) {
        val t = fullList.firstOrNull { it.assetId == asset.id } ?: return

        t.assetId = asset.id
        t.code = asset.code
        t.description = asset.description
        t.assetStatusId = asset.status
        t.warehouseAreaId = asset.warehouseAreaId
        t.labelNumber = asset.labelNumber ?: 0
        t.parentId = asset.parentId ?: 0
        t.qty = 1.0
        t.warehouseAreaDescription = asset.warehouseAreaStr
        t.warehouseDescription = asset.warehouseStr
        t.itemCategoryId = asset.itemCategoryId
        t.itemCategoryDescription = asset.itemCategoryStr
        t.ownershipStatusId = asset.ownershipStatus
        t.manufacturer = asset.manufacturer ?: ""
        t.model = asset.model ?: ""
        t.serialNumber = asset.serialNumber ?: ""
        t.ean = asset.ean ?: ""

        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                // Seleccionamos el ítem y hacemos scroll hasta él.
                selectItemById(t.assetId, scrollToPos)
            }
        }
    }

    fun locationChange(warehouseAreaId: Long) {
        val all = fullList
        val noNeedToMove = all.mapNotNull { if (it.warehouseAreaId == warehouseAreaId) it else null }.toList()
        val toMove = all.mapNotNull { if (it.warehouseAreaId != warehouseAreaId) it else null }.toList()

        for (tempCont in noNeedToMove) tempCont.contentStatusId = WarehouseMovementContentStatus.noNeedToMove.id
        for (tempCont in toMove) tempCont.contentStatusId = WarehouseMovementContentStatus.toMove.id

        notifyItemRangeChanged(0, itemCount, PAYLOADS.STATUS_CHANGE)
    }

    fun remove(position: Int) {
        if (position < 0) return

        val content = getContentByIndex(position) ?: return
        updateCheckedList(content, false)

        if (fullList.lastIndex >= position) {
            fullList.removeAt(position)
            submitList(fullList) {
                run {
                    // Notificamos al Listener superior
                    dataSetChangedListener?.onDataSetChanged()
                }
            }
        }
    }

    fun remove(assetId: Long) {
        val arC = fullList.firstOrNull { it.assetId == assetId } ?: return
        remove(getIndex(arC))
    }

    fun selectItem(a: WarehouseMovementContent?, scroll: Boolean = true) {
        var pos = NO_POSITION
        if (a != null) pos = getIndex(a)
        selectItem(pos, scroll)
    }

    fun selectItemById(id: Long, scroll: Boolean = true) {
        var pos = NO_POSITION
        val arC = fullList.firstOrNull { it.assetId == id }
        if (arC != null) pos = getIndex(arC)
        selectItem(pos, scroll)
    }

    private fun selectItem(pos: Int, scroll: Boolean = true) {
        // Si la posición está fuera del rango válido, reseteamos currentIndex a NO_POSITION.
        currentIndex = if (pos < 0 || pos >= itemCount) NO_POSITION else pos
        notifyItemChanged(currentIndex)
        if (scroll) scrollToPos(currentIndex)
    }

    private fun getCurrentItemsByStatus(statusId: Int): ArrayList<WarehouseMovementContent> {
        return ArrayList(currentList.mapNotNull { if (it.contentStatusId == statusId) it else null })
    }

    private fun getCurrentPos(): ArrayList<Int> {
        return currentList
            .map { getIndexById(it.assetId) }
            .filterTo(ArrayList()) { it > NO_POSITION }
    }

    @Suppress("unused")
    private fun getItemsByStatus(statusId: Int): ArrayList<WarehouseMovementContent> {
        return ArrayList(fullList.mapNotNull { if (it.contentStatusId == statusId) it else null })
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

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getIndexById(id: Long): Int {
        return currentList.indexOfFirst { it.assetId == id }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    private fun getIndex(content: WarehouseMovementContent): Int {
        return currentList.indexOf(content)
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getIndexByItemId(itemId: Long): Int {
        return currentList.indexOfFirst { it.assetId == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByItem(item: Asset): WarehouseMovementContent? {
        return currentList.firstOrNull { it.assetId == item.id }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByIndex(pos: Int): WarehouseMovementContent? {
        return if (currentList.lastIndex > pos) currentList[pos] else null
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByItemId(itemId: Long): WarehouseMovementContent? {
        return currentList.firstOrNull { it.assetId == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByCode(code: String): WarehouseMovementContent? {
        return currentList.firstOrNull { it.code == code }
    }

    fun getAllChecked(): ArrayList<WarehouseMovementContent> {
        val items = ArrayList<WarehouseMovementContent>()
        checkedIdArray.mapNotNullTo(items) { getContentByItemId(it) }
        return items
    }

    fun currentItem(): WarehouseMovementContent? {
        if (currentIndex == NO_POSITION) return null
        return if (currentList.any() && itemCount > currentIndex) getItem(currentIndex)
        else null
    }

    fun currentAsset(): Asset? {
        if (currentIndex == NO_POSITION) return null
        val item = getItem(currentIndex) ?: return null
        return AssetRepository().selectById(item.assetId)
    }

    fun countChecked(): Int {
        return checkedIdArray.count()
    }

    val assetsToMove: Int
        get() = fullList.count {
            it.contentStatusId == WarehouseMovementContentStatus.toMove.id
        }

    val assetsNoNeedToMove: Int
        get() = fullList.count {
            it.contentStatusId == WarehouseMovementContentStatus.noNeedToMove.id
        }

    fun assetsFounded(destWaId: Long): Int =
        fullList.count { it.warehouseAreaId == destWaId && it.assetStatusId == AssetStatus.missing.id }

    fun getToMove(warehouseAreaId: Long): ArrayList<WarehouseMovementContent> {
        return ArrayList(fullList.mapNotNull {
            if (it.warehouseAreaId != warehouseAreaId || it.assetStatusId == AssetStatus.missing.id) it
            else null
        }.toList())
    }

    fun firstVisiblePos(): Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        return layoutManager.findFirstVisibleItemPosition()
    }

    /**
     * Update the checked assets list
     *
     * @param content
     * @param isChecked
     * @return Return true if the list is modified
     */
    private fun updateCheckedList(content: WarehouseMovementContent, isChecked: Boolean) {
        return updateCheckedList(content.assetId, isChecked)
    }

    private fun updateCheckedList(assetId: Long, isChecked: Boolean) {
        checkedIdArray.remove(assetId)
        if (isChecked) checkedIdArray.add(assetId)
    }

    fun addVisibleStatus(status: WarehouseMovementContentStatus) {
        if (visibleStatus.contains(status)) return
        visibleStatus.add(status)
        refreshFilter()
    }

    fun removeVisibleStatus(status: WarehouseMovementContentStatus) {
        if (!visibleStatus.contains(status)) return
        visibleStatus.remove(status)
        refreshFilter()
    }

    // Aquí creamos dos ViewHolder, uno para cada tipo de vista
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    class SelectedViewHolder(val binding: AssetRowExpandedBinding) : ViewHolder(binding.root) {
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

        fun bindStatusChange(content: WarehouseMovementContent) {
            binding.assetStatus.text =
                WarehouseMovementContentStatus.getById(content.contentStatusId)?.description ?: ""
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

        fun bind(content: WarehouseMovementContent, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageControlVisibility(visibility = if (useImageControl) VISIBLE else GONE)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)
            bindStatusChange(content = content)

            binding.descriptionAutoSize.text = content.description
            binding.code.text = content.code

            val manufacturerStr = content.manufacturer
            val modelStr = content.model

            if (manufacturerStr.orEmpty().isEmpty() && modelStr.orEmpty().isEmpty()) {
                binding.dividerInternal1.visibility = GONE
                binding.manufacturerConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal1.visibility = VISIBLE
                binding.manufacturerConstraintLayout.visibility = VISIBLE
                binding.manufacturerTextView.text = manufacturerStr
                binding.modelTextView.text = modelStr
            }

            val wStr = content.warehouseStr
            val waStr = content.warehouseAreaStr

            if (wStr.isEmpty() && waStr.isEmpty()) {
                binding.dividerInternal2.visibility = GONE
                binding.locationConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal2.visibility = VISIBLE
                binding.locationConstraintLayout.visibility = VISIBLE
                binding.warehouseStr.text = wStr
                binding.warehouseAreaStr.text = waStr
            }

            val categoryStr = content.itemCategoryStr
            val ownershipStr = OwnershipStatus.getById(content.ownershipStatusId).description

            if (categoryStr.isEmpty() && ownershipStr.isEmpty()) {
                binding.dividerInternal3.visibility = GONE
                binding.categoryConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal3.visibility = VISIBLE
                binding.categoryConstraintLayout.visibility = VISIBLE
                binding.categoryTextView.text = categoryStr
                binding.ownershipTextView.text = ownershipStr
            }

            val serialNumberStr = content.serialNumber
            val eanStr = content.ean

            if (serialNumberStr.orEmpty().isEmpty() && eanStr.orEmpty().isEmpty()) {
                binding.dividerInternal4.visibility = GONE
                binding.serialNumberConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal4.visibility = VISIBLE
                binding.serialNumberConstraintLayout.visibility = VISIBLE
                binding.serialNumberTextView.text = serialNumberStr
                binding.eanTextView.text = eanStr
            }

            setStyle(content)
        }

        fun setStyle(content: WarehouseMovementContent) {
            val v = itemView

            // region Background layouts
            val layoutToMove = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutNoNeedToMove = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.contentStatusId) {
                WarehouseMovementContentStatus.toMove.id -> {
                    backColor = layoutToMove!!
                    foreColor = layoutToMoveSelectedForeColor
                }

                WarehouseMovementContentStatus.noNeedToMove.id -> {
                    backColor = layoutNoNeedToMove!!
                    foreColor = layoutNoNeedToMoveSelectedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = layoutDefaultSelectedForeColor
                }
            }

            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.descriptionAutoSize.setTextColor(foreColor)
            binding.code.setTextColor(foreColor)
            binding.assetStatus.setTextColor(foreColor)
            binding.warehouseStr.setTextColor(foreColor)
            binding.warehouseAreaStr.setTextColor(foreColor)
            binding.manufacturer.setTextColor(foreColor)
            binding.model.setTextColor(foreColor)
            binding.category.setTextColor(foreColor)
            binding.ownership.setTextColor(foreColor)
            binding.serialNumberStr.setTextColor(foreColor)
            binding.eanStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)
            binding.signImageView.imageTintList = ColorStateList.valueOf(titleForeColor)
            binding.addPhotoImageView.imageTintList = ColorStateList.valueOf(titleForeColor)
            binding.albumImageView.imageTintList = ColorStateList.valueOf(titleForeColor)
            binding.editImageView.imageTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.manufacturerTextView.setTextColor(titleForeColor)
            binding.modelTextView.setTextColor(titleForeColor)
            binding.locationTextView.setTextColor(titleForeColor)
            binding.categoryTextView.setTextColor(titleForeColor)
            binding.ownershipTextView.setTextColor(titleForeColor)
            binding.serialNumberTextView.setTextColor(titleForeColor)
            binding.eanTextView.setTextColor(titleForeColor)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    internal class UnselectedViewHolder(val binding: AssetRowBinding) : ViewHolder(binding.root) {
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

        fun bindStatusChange(content: WarehouseMovementContent) {
            binding.assetStatus.text =
                WarehouseMovementContentStatus.getById(content.contentStatusId)?.description ?: ""
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

        fun bind(content: WarehouseMovementContent, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)
            bindStatusChange(content = content)

            binding.descriptionAutoSize.text = content.description
            binding.code.text = content.code

            val manufacturerStr = content.manufacturer
            val modelStr = content.model

            if (manufacturerStr.orEmpty().isEmpty() && modelStr.orEmpty().isEmpty()) {
                binding.manufacturerConstraintLayout.visibility = GONE
            } else {
                binding.manufacturerConstraintLayout.visibility = VISIBLE
                binding.manufacturerTextView.text = manufacturerStr
                binding.modelTextView.text = modelStr
            }

            setStyle(content)
        }

        fun setStyle(content: WarehouseMovementContent) {
            val v = itemView

            // region Background layouts
            val layoutToMove = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutNoNeedToMove = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.contentStatusId) {
                WarehouseMovementContentStatus.toMove.id -> {
                    backColor = layoutToMove!!
                    foreColor = layoutToMoveSelectedForeColor
                }

                WarehouseMovementContentStatus.noNeedToMove.id -> {
                    backColor = layoutNoNeedToMove!!
                    foreColor = layoutNoNeedToMoveSelectedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = layoutDefaultSelectedForeColor
                }
            }

            val titleForeColor: Int = manipulateColor(foreColor, 1.4f)

            v.background = backColor
            binding.descriptionAutoSize.setTextColor(foreColor)
            binding.code.setTextColor(foreColor)
            binding.assetStatus.setTextColor(foreColor)
            binding.manufacturer.setTextColor(foreColor)
            binding.model.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)

            // Titles
            binding.manufacturerTextView.setTextColor(titleForeColor)
            binding.modelTextView.setTextColor(titleForeColor)
        }
    }

    init {
        setupColors()

        // Por el momento no queremos animaciones, ni transiciones ante cambios en el DataSet
        recyclerView.itemAnimator = null

        // Vamos a retener en el caché un [cacheFactor] por ciento de los ítems creados o un máximo de [maxCachedItems]
        val maxCachedItems = 50
        val cacheFactor = 0.10
        var cacheSize = (fullList.size * cacheFactor).toInt()
        if (cacheSize > maxCachedItems) cacheSize = maxCachedItems
        recyclerView.setItemViewCacheSize(cacheSize)
        recyclerView.recycledViewPool.setMaxRecycledViews(SELECTED_VIEW_TYPE, 0)
        recyclerView.recycledViewPool.setMaxRecycledViews(UNSELECTED_VIEW_TYPE, 0)

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
    private fun sortedVisibleList(list: MutableList<WarehouseMovementContent>?): MutableList<WarehouseMovementContent> {
        val croppedList = (list
            ?: mutableListOf()).mapNotNull { if (it.contentStatusId in visibleStatus.map { it2 -> it2.id }) it else null }
        return sortItems(croppedList.toMutableList())
    }

    // Sobrecargamos estos métodos para suministrar siempre una lista ordenada y filtrada por estado de visibilidad
    override fun submitList(list: MutableList<WarehouseMovementContent>?) {
        super.submitList(sortedVisibleList(list))
    }

    override fun submitList(list: MutableList<WarehouseMovementContent>?, commitCallback: Runnable?) {
        super.submitList(sortedVisibleList(list), commitCallback)
    }
}