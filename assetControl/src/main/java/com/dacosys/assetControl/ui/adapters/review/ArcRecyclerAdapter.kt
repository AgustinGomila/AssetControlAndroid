package com.dacosys.assetControl.ui.adapters.review

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
import com.dacosys.assetControl.data.model.asset.Asset
import com.dacosys.assetControl.data.model.asset.AssetStatus
import com.dacosys.assetControl.data.model.asset.OwnershipStatus
import com.dacosys.assetControl.data.model.review.AssetReviewContent
import com.dacosys.assetControl.data.model.review.AssetReviewContentStatus
import com.dacosys.assetControl.data.model.table.Table
import com.dacosys.assetControl.data.model.user.User
import com.dacosys.assetControl.data.model.user.permission.PermissionEntry
import com.dacosys.assetControl.databinding.AssetRowBinding
import com.dacosys.assetControl.databinding.AssetRowExpandedBinding
import com.dacosys.assetControl.network.utils.ProgressStatus
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

class ArcRecyclerAdapter(
    private val recyclerView: RecyclerView,
    var fullList: ArrayList<AssetReviewContent>,
    var checkedIdArray: ArrayList<Long> = ArrayList(),
    var allowQuickReview: Boolean = false,
    private var allowEditAsset: Boolean = false,
    var showCheckBoxes: Boolean = false,
    private var showCheckBoxesChanged: (Boolean) -> Unit = { },
    private var showImages: Boolean = false,
    private var showImagesChanged: (Boolean) -> Unit = { },
    var visibleStatus: ArrayList<AssetReviewContentStatus> = AssetReviewContentStatus.getAll(),
    private var filterOptions: FilterOptions = FilterOptions()
) : ListAdapter<AssetReviewContent, ViewHolder>(AssetReviewContentDiffUtilCallback), Filterable {

    private var currentIndex = NO_POSITION
    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null
    private var editAssetListener: EditAssetRequiredListener? = null

    // Listeners para los eventos de ImageControl.
    private var addPhotoRequiredListener: AddPhotoRequiredListener? = null
    private var albumViewRequiredListener: AlbumViewRequiredListener? = null

    // Listener para los eventos que requieren que la actividad anfitriona
    // muestre un mensaje de espera mientras se realiza alguna tarea que demanda más tiempo.
    private var uiEventListener: UiEventListener? = null

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

    fun refreshUiEventListener(uiEventListener: UiEventListener? = null) {
        this.uiEventListener = uiEventListener
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

    private object AssetReviewContentDiffUtilCallback : DiffUtil.ItemCallback<AssetReviewContent>() {
        override fun areItemsTheSame(oldItem: AssetReviewContent, newItem: AssetReviewContent): Boolean {
            return oldItem.assetId == newItem.assetId
        }

        override fun areContentsTheSame(oldItem: AssetReviewContent, newItem: AssetReviewContent): Boolean {
            if (oldItem.contentStatusId != newItem.contentStatusId) return false
            if (oldItem.assetId != newItem.assetId) return false
            if (oldItem.code != newItem.code) return false
            if (oldItem.description != newItem.description) return false
            if (oldItem.assetStatusId != newItem.assetStatusId) return false
            if (oldItem.originWarehouseAreaId != newItem.originWarehouseAreaId) return false
            if (oldItem.labelNumber != newItem.labelNumber) return false
            if (oldItem.parentId != newItem.parentId) return false
            if (oldItem.warehouseAreaId != newItem.warehouseAreaId) return false
            if (oldItem.warehouseAreaStr != newItem.warehouseAreaStr) return false
            if (oldItem.warehouseStr != newItem.warehouseStr) return false
            if (oldItem.itemCategoryId != newItem.itemCategoryId) return false
            if (oldItem.itemCategoryStr != newItem.itemCategoryStr) return false
            if (oldItem.ownershipStatusId != newItem.ownershipStatusId) return false
            if (oldItem.manufacturer != newItem.manufacturer) return false
            if (oldItem.model != newItem.model) return false
            if (oldItem.serialNumber != newItem.serialNumber) return false
            if (oldItem.qty != newItem.qty) return false
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
        private var layoutRevisedForeColor: Int = 0
        private var layoutAppearedForeColor: Int = 0
        private var layoutNotInReviewForeColor: Int = 0
        private var layoutExternalForeColor: Int = 0
        private var layoutNewForeColor: Int = 0
        private var layoutDefaultForeColor: Int = 0

        private var layoutRevisedSelectedForeColor: Int = 0
        private var layoutAppearedSelectedForeColor: Int = 0
        private var layoutNotInReviewSelectedForeColor: Int = 0
        private var layoutExternalSelectedForeColor: Int = 0
        private var layoutNewSelectedForeColor: Int = 0
        private var layoutDefaultSelectedForeColor: Int = 0

        private fun setupColors() {
            // Color de los diferentes estados
            layoutRevisedForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_revised, null))
            layoutAppearedForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_appeared, null))
            layoutNotInReviewForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_not_in_review, null))
            layoutExternalForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_external, null))
            layoutNewForeColor = getBestContrastColor(getColor(getContext().resources, R.color.status_new, null))
            layoutDefaultForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_default, null))

            // Mejor contraste para los ítems seleccionados
            layoutRevisedSelectedForeColor = getBestContrastColor(manipulateColor(layoutRevisedForeColor, 0.5f))
            layoutAppearedSelectedForeColor = getBestContrastColor(manipulateColor(layoutAppearedForeColor, 0.5f))
            layoutNotInReviewSelectedForeColor = getBestContrastColor(manipulateColor(layoutNotInReviewForeColor, 0.5f))
            layoutExternalSelectedForeColor = getBestContrastColor(manipulateColor(layoutExternalForeColor, 0.5f))
            layoutNewSelectedForeColor = getBestContrastColor(manipulateColor(layoutNewForeColor, 0.5f))
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
        if (!allowQuickReview) {
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
                    tableId = Table.asset.tableId, itemId = assetId
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
                    tableId = Table.asset.tableId, itemId = assetId
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
                    tableId = Table.asset.tableId,
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
     * Lógica del comportamiento del CheckBox de marcado de ítems cuando [allowQuickReview] es verdadero
     *
     * @param checkBox Control CheckBox para marcado del ítem
     * @param content Datos del ítem
     * @param position Posición en el adaptador
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setCheckBoxLogic(checkBox: CheckBox, content: AssetReviewContent, position: Int) {
        val checkChangeListener = OnCheckedChangeListener { _, isChecked ->
            if (!ignoreCheckBoxStateChanged) {

                updateCheckedList(content = content, isChecked = isChecked)

                if (isChecked && content.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                    updateContent(
                        content,
                        contentStatusId = AssetReviewContentStatus.revised.id,
                        assetStatusId = AssetStatus.onInventory.id
                    )
                } else if (!isChecked && content.contentStatusId == AssetReviewContentStatus.revised.id) {
                    updateContent(
                        content,
                        contentStatusId = AssetReviewContentStatus.notInReview.id,
                        assetStatusId = AssetStatus.missing.id
                    )
                }

                notifyItemChanged(position, PAYLOADS.STATUS_CHANGE)
                checkedChangedListener?.onCheckedChanged(isChecked, position)
            }
        }

        val longClickListener = OnLongClickListener { _ ->
            val isChecked = !checkBox.isChecked

            if (!isChecked) {
                // Si el activo seleccionado estaba [revised], desmarcamos y pasamos a [notInReview] todos los [revised].
                if (content.contentStatusId == AssetReviewContentStatus.revised.id) {
                    val revised = getCurrentItemsByStatus(content.contentStatusId)
                    updateContent(
                        revised,
                        contentStatusId = AssetReviewContentStatus.notInReview.id,
                        assetStatusId = AssetStatus.missing.id
                    )
                } else {
                    // En todos los demás casos no cambia el estado, solo el marcado.
                    checkedIdArray.clear()
                }
            } else {
                // Si el activo seleccionado estaba [notInReview], marcamos y pasamos a [revised] todos los [notInReview].
                if (content.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                    val notInReview = getCurrentItemsByStatus(content.contentStatusId)
                    updateContent(
                        notInReview,
                        contentStatusId = AssetReviewContentStatus.revised.id,
                        assetStatusId = AssetStatus.onInventory.id
                    )
                } else {
                    // En todos los demás casos no cambia el estado, solo desmarcamos.
                    val all = getCurrentItemsByStatus(content.contentStatusId)
                    for (tempCont in all) {
                        updateCheckedList(content = tempCont, isChecked = true)
                    }
                }
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

    fun removeContent(arCont: AssetReviewContent) {
        when (arCont.contentStatusId) {
            AssetReviewContentStatus.revised.id,
            AssetReviewContentStatus.newAsset.id,
            -> {
                updateContent(
                    assetId = arCont.assetId,
                    contentStatusId = AssetReviewContentStatus.notInReview.id,
                    assetStatusId = arCont.assetStatusId,
                    selectItem = false
                )
            }

            AssetReviewContentStatus.external.id,
            AssetReviewContentStatus.appeared.id,
            AssetReviewContentStatus.unknown.id,
            -> {
                remove(arCont.assetId)
            }

            else -> {
                return
            }
        }
    }

    /**
     * Get images thumbs
     *
     * @param adapter Parent adapter
     * @param holder ImageControl image holder
     * @param assetId Asset ID of which we are requesting the image
     */
    private fun getImagesThumbs(
        adapter: ArcRecyclerAdapter,
        holder: ImageControlHolder,
        assetId: Long
    ) {
        if (!adapter.showImages) return

        Handler(Looper.getMainLooper()).postDelayed({
            adapter.run {
                ImageAdapter.getImages(context = getContext(), programData = ProgramData(
                    programObjectId = Table.asset.tableId.toLong(), objId1 = assetId.toString()
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
        var selected: AssetReviewContent? = null
        var firstVisible: AssetReviewContent? = null

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
                var r: ArrayList<AssetReviewContent> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: AssetReviewContent

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

            fun isFilterable(content: AssetReviewContent, filterString: String): Boolean =
                content.description.contains(filterString, true) ||
                        content.code.contains(filterString, true) ||
                        content.ean.contains(filterString, true)

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                var contents: ArrayList<AssetReviewContent> = ArrayList()
                if (results?.values != null) {
                    contents = results.values as ArrayList<AssetReviewContent>
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

    private fun sortItems(originalList: MutableList<AssetReviewContent>): ArrayList<AssetReviewContent> {
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

    fun add(content: AssetReviewContent?) {
        if (content == null) return
        val lastPost = fullList.lastIndex + 1
        add(content, lastPost)
    }

    fun add(content: AssetReviewContent, position: Int) {
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
     * Se utiliza cuando se edita un ítem y necesita actualizarse
     */
    fun updateItem(asset: Asset, scrollToPos: Boolean = false) {
        val t = fullList.firstOrNull { it.assetId == asset.assetId } ?: return

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

        submitList(fullList) {
            run {
                notifyItemChanged(getIndexById(asset.assetId))

                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                // Seleccionamos el ítem y hacemos scroll hasta él.
                selectItemById(asset.assetId, scrollToPos)
            }
        }
    }

    /**
     * ACTUALIZA un contenido.
     */
    fun updateContent(
        assetId: Long,
        contentStatusId: Int,
        assetStatusId: Int,
        selectItem: Boolean = true
    ) {
        val t = fullList.firstOrNull { it.assetId == assetId } ?: return

        t.assetStatusId = assetStatusId
        t.contentStatusId = contentStatusId

        if (contentStatusId == AssetReviewContentStatus.notInReview.id) checkedIdArray.remove(assetId)
        else if (contentStatusId == AssetReviewContentStatus.revised.id) updateCheckedList(assetId, true)

        submitList(fullList) {
            run {
                notifyItemChanged(getIndexById(assetId))

                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                // Seleccionamos el ítem y hacemos scroll hasta él.
                if (selectItem) selectItemById(assetId)
            }
        }
    }

    private fun updateContent(
        content: AssetReviewContent,
        contentStatusId: Int,
        assetStatusId: Int
    ) {
        val tempCont = currentList.firstOrNull { it.assetId == content.assetId } ?: return
        tempCont.contentStatusId = contentStatusId
        tempCont.assetStatusId = assetStatusId
    }

    private fun updateContent(
        list: ArrayList<AssetReviewContent>,
        contentStatusId: Int,
        assetStatusId: Int
    ) {
        val total = currentList.size

        uiEventListener?.onUiEventRequired(
            AdapterProgress(
                totalTask = total,
                completedTask = 0,
                msg = getContext().getString(R.string.processing_asset),
                progressStatus = ProgressStatus.starting
            )
        )

        currentList.forEachIndexed { index, content ->
            uiEventListener?.onUiEventRequired(
                AdapterProgress(
                    totalTask = total,
                    completedTask = index + 1,
                    msg = getContext().getString(R.string.please_wait),
                    progressStatus = ProgressStatus.running
                )
            )

            val isChecked = contentStatusId == AssetReviewContentStatus.revised.id
            if (list.any { it.assetId == content.assetId }) {
                currentList[index].contentStatusId = contentStatusId
                currentList[index].assetStatusId = assetStatusId
                updateCheckedList(
                    content = currentList[index],
                    isChecked = isChecked
                )
            }
        }

        uiEventListener?.onUiEventRequired(
            AdapterProgress(
                totalTask = total,
                completedTask = total,
                msg = getContext().getString(R.string.finished),
                progressStatus = ProgressStatus.finished
            )
        )
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

    fun selectItem(a: AssetReviewContent?, scroll: Boolean = true) {
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

    private fun getCurrentItemsByStatus(statusId: Int): ArrayList<AssetReviewContent> {
        return ArrayList(currentList.mapNotNull { if (it.contentStatusId == statusId) it else null })
    }

    @Suppress("unused")
    private fun getItemsByStatus(statusId: Int): ArrayList<AssetReviewContent> {
        return ArrayList(fullList.mapNotNull { if (it.contentStatusId == statusId) it else null })
    }

    val countItemsAdded: Int
        get() = fullList.count {
            it.contentStatusId == AssetReviewContentStatus.appeared.id ||
                    it.contentStatusId == AssetReviewContentStatus.unknown.id ||
                    it.contentStatusId == AssetReviewContentStatus.external.id
        }

    val countItemsMissed: Int
        get() = fullList.count { it.contentStatusId == AssetReviewContentStatus.notInReview.id }

    val countItemsRevised: Int
        get() = fullList.count {
            it.contentStatusId == AssetReviewContentStatus.revised.id ||
                    it.contentStatusId == AssetReviewContentStatus.newAsset.id
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
    private fun getIndex(content: AssetReviewContent): Int {
        return currentList.indexOf(content)
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getIndexByItemId(itemId: Long): Int {
        return currentList.indexOfFirst { it.assetId == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByItem(item: Asset): AssetReviewContent? {
        return currentList.firstOrNull { it.assetId == item.assetId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByIndex(pos: Int): AssetReviewContent? {
        return if (currentList.lastIndex > pos) currentList[pos] else null
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByItemId(itemId: Long): AssetReviewContent? {
        return currentList.firstOrNull { it.assetId == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByCode(code: String): AssetReviewContent? {
        return currentList.firstOrNull { it.code == code }
    }

    fun getAllChecked(): ArrayList<AssetReviewContent> {
        val items = ArrayList<AssetReviewContent>()
        checkedIdArray.mapNotNullTo(items) { getContentByItemId(it) }
        return items
    }

    fun currentItem(): AssetReviewContent? {
        if (currentIndex == NO_POSITION) return null
        return if (currentList.any() && itemCount > currentIndex) getItem(currentIndex)
        else null
    }

    fun countChecked(): Int {
        return checkedIdArray.count()
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
    private fun updateCheckedList(content: AssetReviewContent, isChecked: Boolean) {
        return updateCheckedList(content.assetId, isChecked)
    }

    private fun updateCheckedList(assetId: Long, isChecked: Boolean) {
        checkedIdArray.remove(assetId)
        if (isChecked) checkedIdArray.add(assetId)
    }

    fun addVisibleStatus(status: AssetReviewContentStatus) {
        if (visibleStatus.contains(status)) return
        visibleStatus.add(status)
        refreshFilter()
    }

    fun removeVisibleStatus(status: AssetReviewContentStatus) {
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

        fun bindStatusChange(content: AssetReviewContent) {
            binding.assetStatus.text = AssetReviewContentStatus.getById(content.contentStatusId)?.description ?: ""
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

        fun bind(content: AssetReviewContent, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageControlVisibility(visibility = if (useImageControl) VISIBLE else GONE)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)
            bindStatusChange(content = content)

            binding.descriptionAutoSize.text = content.description
            binding.code.text = content.code

            val manufacturerStr = content.manufacturer
            val modelStr = content.model

            if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
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
            val ownershipStr = OwnershipStatus.getById(content.ownershipStatusId)?.description ?: ""

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

            if (serialNumberStr.isEmpty() && eanStr.isEmpty()) {
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

        fun setStyle(content: AssetReviewContent) {
            val v = itemView

            // region Background layouts
            val layoutRevised = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutAppeared = getDrawable(getContext().resources, R.drawable.layout_thin_border_red, null)
            val layoutNotInReview = getDrawable(getContext().resources, R.drawable.layout_thin_border_marron, null)
            val layoutExternal = getDrawable(getContext().resources, R.drawable.layout_thin_border_blue, null)
            val layoutNew = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.contentStatusId) {
                AssetReviewContentStatus.revised.id -> {
                    backColor = layoutRevised!!
                    foreColor = layoutRevisedSelectedForeColor
                }

                AssetReviewContentStatus.appeared.id -> {
                    backColor = layoutAppeared!!
                    foreColor = layoutAppearedSelectedForeColor
                }

                AssetReviewContentStatus.notInReview.id -> {
                    backColor = layoutNotInReview!!
                    foreColor = layoutNotInReviewSelectedForeColor
                }

                AssetReviewContentStatus.external.id -> {
                    backColor = layoutExternal!!
                    foreColor = layoutExternalSelectedForeColor
                }

                AssetReviewContentStatus.newAsset.id -> {
                    backColor = layoutNew!!
                    foreColor = layoutNewSelectedForeColor
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

        fun bindStatusChange(content: AssetReviewContent) {
            binding.assetStatus.text = AssetReviewContentStatus.getById(content.contentStatusId)?.description ?: ""
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

        fun bind(content: AssetReviewContent, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)
            bindStatusChange(content = content)

            binding.descriptionAutoSize.text = content.description
            binding.code.text = content.code

            val manufacturerStr = content.manufacturer
            val modelStr = content.model

            if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
                binding.manufacturerConstraintLayout.visibility = GONE
            } else {
                binding.manufacturerConstraintLayout.visibility = VISIBLE
                binding.manufacturerTextView.text = manufacturerStr
                binding.modelTextView.text = modelStr
            }

            setStyle(content)
        }

        fun setStyle(content: AssetReviewContent) {
            val v = itemView

            // region Background layouts
            val layoutRevised = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutAppeared = getDrawable(getContext().resources, R.drawable.layout_thin_border_red, null)
            val layoutNotInReview = getDrawable(getContext().resources, R.drawable.layout_thin_border_marron, null)
            val layoutExternal = getDrawable(getContext().resources, R.drawable.layout_thin_border_blue, null)
            val layoutNew = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.contentStatusId) {
                AssetReviewContentStatus.revised.id -> {
                    backColor = layoutRevised!!
                    foreColor = layoutRevisedForeColor
                }

                AssetReviewContentStatus.appeared.id -> {
                    backColor = layoutAppeared!!
                    foreColor = layoutAppearedForeColor
                }

                AssetReviewContentStatus.notInReview.id -> {
                    backColor = layoutNotInReview!!
                    foreColor = layoutNotInReviewForeColor
                }

                AssetReviewContentStatus.external.id -> {
                    backColor = layoutExternal!!
                    foreColor = layoutExternalForeColor
                }

                AssetReviewContentStatus.newAsset.id -> {
                    backColor = layoutNew!!
                    foreColor = layoutNewForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = layoutDefaultForeColor
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
    private fun sortedVisibleList(list: MutableList<AssetReviewContent>?): MutableList<AssetReviewContent> {
        val croppedList = (list
            ?: mutableListOf()).mapNotNull { if (it.contentStatusId in visibleStatus.map { it2 -> it2.id }) it else null }
        return sortItems(croppedList.toMutableList())
    }

    // Sobrecargamos estos métodos para suministrar siempre una lista ordenada y filtrada por estado de visibilidad
    override fun submitList(list: MutableList<AssetReviewContent>?) {
        super.submitList(sortedVisibleList(list))
    }

    override fun submitList(list: MutableList<AssetReviewContent>?, commitCallback: Runnable?) {
        super.submitList(sortedVisibleList(list), commitCallback)
    }
}