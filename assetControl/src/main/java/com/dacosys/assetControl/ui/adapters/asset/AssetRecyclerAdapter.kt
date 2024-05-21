package com.dacosys.assetControl.ui.adapters.asset

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
import com.dacosys.assetControl.AssetControlApp.Companion.currentUser
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.asset.OwnershipStatus
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.databinding.AssetRowBinding
import com.dacosys.assetControl.databinding.AssetRowExpandedBinding
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

class AssetRecyclerAdapter private constructor(builder: Builder) :
    ListAdapter<Asset, ViewHolder>(AssetDiffUtilCallback), Filterable {
    private var recyclerView: RecyclerView
    var fullList: ArrayList<Asset> = ArrayList()
    var checkedIdArray: ArrayList<Long> = ArrayList()
    private var multiSelect: Boolean = false
    var showCheckBoxes: Boolean = false
    private var showCheckBoxesChanged: (Boolean) -> Unit = { }
    private var showImages: Boolean = false
    private var showImagesChanged: (Boolean) -> Unit = { }
    private var visibleStatus: ArrayList<AssetStatus> = arrayListOf()
    private var filterOptions: FilterOptions = FilterOptions()

    // Este Listener debe usarse para los cambios de cantidad o de ítems marcados de la lista,
    // ya que se utiliza para actualizar los valores sumarios en la actividad.
    private var dataSetChangedListener: DataSetChangedListener? = null

    private var checkedChangedListener: CheckedChangedListener? = null
    private var editAssetRequiredListener: EditAssetRequiredListener? = null

    // Listeners para los eventos de ImageControl.
    private var addPhotoRequiredListener: AddPhotoRequiredListener? = null
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

    // Ids de activos que tienen imágenes
    private var idWithImage: ArrayList<Long> = ArrayList()

    // Visibilidad del panel de miniaturas depende de la existencia de una imagen para ese activo.
    private fun imageVisibility(assetId: Long): Int {
        return if (showImages && idWithImage.contains(assetId)) VISIBLE else GONE
    }

    // Permiso de edición de activos
    private val userHasPermissionToEdit: Boolean by lazy { User.hasPermission(PermissionEntry.ModifyAsset) }

    // Parámetros del filtro
    data class FilterOptions(
        var filterString: String = "",
        var showAllOnFilterEmpty: Boolean = true,
    )

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
        editAssetRequiredListener: EditAssetRequiredListener? = null,
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
        this.editAssetRequiredListener = editAssetRequiredListener
    }

    fun refreshImageControlListeners(
        addPhotoListener: AddPhotoRequiredListener? = null,
        albumViewListener: AlbumViewRequiredListener? = null,
    ) {
        addPhotoRequiredListener = addPhotoListener
        albumViewRequiredListener = albumViewListener
    }

    // El método onCreateViewHolder infla los diseños para cada tipo de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SELECTED_VIEW_TYPE -> {
                SelectedViewHolder(AssetRowExpandedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

            else -> {
                UnselectedViewHolder(AssetRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

                PAYLOADS.CHECKBOX_STATE -> {
                    val asset = getItem(position)
                    if (position == currentIndex)
                        (holder as SelectedViewHolder).bindCheckBoxState(checkedIdArray.contains(asset.id))
                    else
                        (holder as UnselectedViewHolder).bindCheckBoxState(checkedIdArray.contains(asset.id))
                }

                PAYLOADS.IMAGE_VISIBILITY -> {
                    val asset = getItem(position)
                    if (position == currentIndex) {
                        getImagesThumbs(this, (holder as SelectedViewHolder).icHolder, asset)
                        holder.bindImageVisibility(
                            imageVisibility = imageVisibility(asset.id),
                            changingState = true
                        )
                    } else {
                        getImagesThumbs(this, (holder as UnselectedViewHolder).icHolder, asset)
                        holder.bindImageVisibility(
                            imageVisibility = imageVisibility(asset.id),
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
        }

        // Establecer los valores para cada elemento según su posición con el estilo correspondiente
        if (currentIndex == position) {
            // Establece el estado seleccionado
            setSelectedHolder(holder as SelectedViewHolder, position)
        } else {
            // Establece el estado no seleccionado
            setUnselectedHolder(holder as UnselectedViewHolder, position)
        }
    }

    private fun setSelectedHolder(holder: SelectedViewHolder, position: Int) {
        val asset = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        holder.bind(
            asset = asset,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            imageVisibility = imageVisibility(asset.id)
        )

        holder.itemView.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            getColorWithAlpha(colorId = R.color.lightslategray, alpha = 220), BlendModeCompat.MODULATE
        )

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, asset, position)

        // Acción de edición
        setEditLogic(holder.binding.editImageView, asset)

        // Botones de acciones de ImageControl
        setAddSignLogic(holder.binding.signImageView)
        setAlbumViewLogic(holder.binding.albumImageView, asset)
        setAddPhotoLogic(holder.binding.addPhotoImageView, asset)

        // Miniatura de ImageControl
        getImagesThumbs(this, holder.icHolder, asset)
    }

    private fun setUnselectedHolder(holder: UnselectedViewHolder, position: Int) {
        val asset = getItem(position)

        // Lógica de clic largo sobre el ítem
        setItemCheckBoxLogic(holder.itemView)

        // Perform a full update
        holder.bind(
            asset = asset,
            checkBoxVisibility = if (showCheckBoxes) VISIBLE else GONE,
            imageVisibility = imageVisibility(asset.id)
        )

        holder.itemView.background.colorFilter = null

        // Acciones del checkBox de marcado
        setCheckBoxLogic(holder.binding.checkBox, asset, position)

        // Miniatura de ImageControl
        getImagesThumbs(this, holder.icHolder, asset)
    }

    private fun setAddSignLogic(signImageView: AppCompatImageView) {
        signImageView.visibility = GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAlbumViewLogic(albumImageView: AppCompatImageView, asset: Asset) {
        albumImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                albumViewRequiredListener?.onAlbumViewRequired(
                    tableId = Table.asset.id, itemId = asset.id
                )
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAddPhotoLogic(addPhotoImageView: AppCompatImageView, asset: Asset) {
        addPhotoImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                addPhotoRequiredListener?.onAddPhotoRequired(
                    tableId = Table.asset.id,
                    itemId = asset.id,
                    description = asset.description,
                    obs = "${getContext().getString(R.string.user)}: ${currentUser()?.name}"
                )
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEditLogic(editImageView: AppCompatImageView, asset: Asset) {
        if (!userHasPermissionToEdit) {
            editImageView.visibility = GONE
            return
        }

        editImageView.visibility = VISIBLE

        editImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                editAssetRequiredListener?.onEditAssetRequired(
                    tableId = Table.asset.id, itemId = asset.id
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
     * @param asset Datos del ítem
     * @param position Posición en el adaptador
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setCheckBoxLogic(checkBox: CheckBox, asset: Asset, position: Int) {
        val checkChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            setChecked(asset = asset, isChecked = isChecked, suspendRefresh = false)
        }

        val longClickListener = OnLongClickListener { _ ->
            checkBox.setOnCheckedChangeListener(null)

            // Notificamos los cambios solo a los ítems que cambian de estado.
            val newState = !checkBox.isChecked
            if (newState) {
                currentList.mapIndexed { pos, asset ->
                    if (asset.id !in checkedIdArray) {
                        checkedIdArray.add(asset.id)
                        notifyItemChanged(pos, PAYLOADS.CHECKBOX_STATE)
                    }
                }
            } else {
                currentList.mapIndexed { pos, asset ->
                    if (asset.id in checkedIdArray) {
                        checkedIdArray.remove(asset.id)
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

        checkBox.isChecked = checkedIdArray.contains(asset.id)
        checkBox.isLongClickable = true
        checkBox.tag = position

        checkBox.setOnLongClickListener(longClickListener)
        checkBox.setOnCheckedChangeListener(checkChangeListener)
    }

    // El método getItemViewType devuelve el tipo de vista que se usará para el elemento en la posición dada
    override fun getItemViewType(position: Int): Int {
        return if (currentIndex == position) SELECTED_VIEW_TYPE
        else UNSELECTED_VIEW_TYPE
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            var selected: Asset? = null
            var firstVisible: Asset? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                // Guardamos el item seleccionado y la posición del scroll
                selected = currentAsset()
                var scrollPos =
                    (recyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()
                        ?: NO_POSITION

                if (scrollPos != NO_POSITION && itemCount > scrollPos) {
                    var currentScrolled = getItem(scrollPos)

                    // Comprobamos si es visible el ítem del Scroll
                    if (currentScrolled.assetStatus in visibleStatus)
                        firstVisible = currentScrolled
                    else {
                        // Si no es visible, intentar encontrar el próximo visible.
                        while (firstVisible == null) {
                            scrollPos++
                            if (itemCount > scrollPos) {
                                currentScrolled = getItem(scrollPos)
                                if (currentScrolled.assetStatus in visibleStatus)
                                    firstVisible = currentScrolled
                            } else break
                        }
                    }
                }

                // Filtramos los resultados
                val results = FilterResults()
                var r: ArrayList<Asset> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: Asset

                        for (i in 0 until fullList.size) {
                            filterableItem = fullList[i]

                            // Descartamos aquellos que no debe ser visibles
                            if (filterableItem.assetStatus !in visibleStatus) continue

                            if (isFilterable(filterableItem, filterString)) {
                                r.add(filterableItem)
                            }
                        }
                    } else if (filterOptions.showAllOnFilterEmpty) {
                        r = ArrayList(fullList.mapNotNull { if (it.assetStatus in visibleStatus) it else null })
                    }
                }

                results.values = r
                results.count = r.size
                return results
            }

            fun isFilterable(filterableAsset: Asset, filterString: String): Boolean =
                (filterableAsset.code.contains(filterString, true) ||
                        filterableAsset.description.contains(filterString, true) ||
                        (filterableAsset.serialNumber ?: "").contains(filterString, true) ||
                        (filterableAsset.ean ?: "").contains(filterString, true))

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                submitList(results?.values as ArrayList<Asset>) {
                    run {
                        // Notificamos al Listener superior
                        dataSetChangedListener?.onDataSetChanged()

                        // Recuperamos el item seleccionado y la posición del scroll
                        if (firstVisible != null)
                            scrollToPos(getIndexById(firstVisible?.id ?: -1), true)
                        if (selected != null)
                            selectItem(selected, false)
                    }
                }
            }
        }
    }

    private fun sortItems(originalList: MutableList<Asset>): ArrayList<Asset> {
        // Get all the parent groups
        val groups = originalList.sortedWith(
            compareBy({ it.parentId },
                { it.code },
                { it.description },
                { it.serialNumber },
                { it.ean })
        ).groupBy { it.parentId }

        // Recursively get the children
        fun follow(asset: Asset): List<Asset> {
            return listOf(asset) + (groups[asset.id] ?: emptyList()).flatMap(::follow)
        }

        // Run the follow method on each of the roots
        return originalList.map { it.parentId }
            .subtract(originalList.map { it.id }.toSet())
            .flatMap { groups[it] ?: emptyList() }
            .flatMap(::follow) as ArrayList<Asset>
    }

    fun refreshFilter(options: FilterOptions) {
        filterOptions = options
        filter.filter(filterOptions.filterString)
    }

    private fun refreshFilter() {
        refreshFilter(filterOptions)
    }

    fun add(asset: Asset, position: Int) {
        fullList.add(position, asset)
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
                selectItem(getIndex(asset))
            }
        }
    }

    fun remove(position: Int) {
        if (position < 0) return

        val id = getItemId(position)
        checkedIdArray.remove(id)

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

    /**
     * Se utiliza cuando se edita un activo y necesita actualizarse
     */
    fun updateAsset(asset: Asset, scrollToPos: Boolean = false) {
        val t = fullList.firstOrNull { it == asset } ?: return

        val index = fullList.indexOf(t)
        if (index != -1) fullList[index] = asset

        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                // Seleccionamos el ítem y hacemos scroll hasta él.
                selectItem(asset, scrollToPos)
            }
        }
    }

    fun selectItem(a: Asset?, scroll: Boolean = true) {
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

    fun getIndexById(id: Long): Int {
        return currentList.indexOfFirst { it.id == id }
    }

    private fun getIndex(asset: Asset): Int {
        return currentList.indexOfFirst { it == asset }
    }

    private fun getAssetById(id: Long): Asset? {
        return fullList.firstOrNull { it.id == id }
    }

    fun getAllChecked(): ArrayList<Asset> {
        val items = ArrayList<Asset>()
        checkedIdArray.mapNotNullTo(items) { getAssetById(it) }
        return items
    }

    fun currentAsset(): Asset? {
        if (currentIndex == NO_POSITION) return null
        return if (currentList.any() && itemCount > currentIndex) getItem(currentIndex)
        else null
    }

    val countChecked: Int
        get() = checkedIdArray.size

    val totalVisible: Int
        get() = itemCount

    val totalOnInventory: Int
        get() = fullList.count { it.status == AssetStatus.onInventory.id }

    val totalMissing: Int
        get() = fullList.count { it.status == AssetStatus.missing.id }

    val totalRemoved: Int
        get() = fullList.count { it.status == AssetStatus.removed.id }

    fun firstVisiblePos(): Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        return layoutManager.findFirstVisibleItemPosition()
    }

    fun setChecked(asset: Asset, isChecked: Boolean, suspendRefresh: Boolean = false) {
        val pos = getIndexById(asset.id)
        checkedIdArray.remove(asset.id)
        if (isChecked) checkedIdArray.add(asset.id)

        // Notificamos al Listener superior
        if (!suspendRefresh) checkedChangedListener?.onCheckedChanged(isChecked, pos)
    }

    fun addVisibleStatus(status: AssetStatus) {
        if (visibleStatus.contains(status)) return
        visibleStatus.add(status)

        refreshFilter()
    }

    fun removeVisibleStatus(status: AssetStatus) {
        if (!visibleStatus.contains(status)) return
        visibleStatus.remove(status)

        // Quitamos los ítems con el estado seleccionado de la lista marcados.
        val uncheckedItems = ArrayList(fullList.mapNotNull { if (it.assetStatus == status) it.id else null })
        checkedIdArray.removeAll(uncheckedItems.toSet())

        refreshFilter()
    }

    internal class SelectedViewHolder(val binding: AssetRowExpandedBinding) :
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

        fun bind(asset: Asset, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageControlVisibility(visibility = if (useImageControl) VISIBLE else GONE)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)

            binding.descriptionAutoSize.text = asset.description
            binding.code.text = asset.code
            binding.assetStatus.text = AssetStatus.getById(asset.status).description

            // region Manufacturer
            val manufacturerStr = asset.manufacturer ?: ""
            val modelStr = asset.model ?: ""

            if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
                binding.dividerInternal1.visibility = GONE
                binding.manufacturerConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal1.visibility = VISIBLE
                binding.manufacturerConstraintLayout.visibility = VISIBLE
                binding.manufacturer.text = manufacturerStr
                binding.model.text = modelStr
            }
            // endregion

            // region Location
            val wStr = asset.warehouseStr
            val waStr = asset.warehouseAreaStr

            if (wStr.isEmpty() && waStr.isEmpty()) {
                binding.dividerInternal2.visibility = GONE
                binding.locationConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal2.visibility = VISIBLE
                binding.locationConstraintLayout.visibility = VISIBLE
                binding.warehouseStr.text = wStr
                binding.warehouseAreaStr.text = waStr
            }
            // endregion

            // region Category
            val categoryStr = asset.itemCategoryStr
            val ownershipStr = OwnershipStatus.getById(asset.ownershipStatus).description

            if (categoryStr.isEmpty() && ownershipStr.isEmpty()) {
                binding.dividerInternal3.visibility = GONE
                binding.categoryConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal3.visibility = VISIBLE
                binding.categoryConstraintLayout.visibility = VISIBLE
                binding.category.text = categoryStr
                binding.ownership.text = ownershipStr
            }
            // endregion

            // region SerialNumber
            val serialNumberStr = asset.serialNumber ?: ""
            val eanStr = asset.ean ?: ""

            if (serialNumberStr.isEmpty() && eanStr.isEmpty()) {
                binding.dividerInternal4.visibility = GONE
                binding.serialNumberConstraintLayout.visibility = GONE
            } else {
                binding.dividerInternal4.visibility = VISIBLE
                binding.serialNumberConstraintLayout.visibility = VISIBLE
                binding.serialNumberStr.text = serialNumberStr
                binding.eanStr.text = eanStr
            }
            // endregion

            setStyle(asset)
        }

        private fun setStyle(asset: Asset) {
            val v = itemView

            // Background layouts
            // Resalta por estado del activo
            val layoutOnInventory = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutMissing = getDrawable(getContext().resources, R.drawable.layout_thin_border_red, null)
            val layoutRemoved = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            when (asset.status) {
                AssetStatus.onInventory.id -> {
                    backColor = layoutOnInventory!!
                    foreColor = onInventorySelectedForeColor
                }

                AssetStatus.missing.id -> {
                    backColor = layoutMissing!!
                    foreColor = missingSelectedForeColor
                }

                AssetStatus.removed.id -> {
                    backColor = layoutRemoved!!
                    foreColor = removedSelectedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = defaultSelectedForeColor
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

    internal class UnselectedViewHolder(val binding: AssetRowBinding) :
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

        fun bind(asset: Asset, checkBoxVisibility: Int = GONE, imageVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindImageVisibility(imageVisibility = imageVisibility, changingState = false)

            binding.descriptionAutoSize.text = asset.description
            binding.code.text = asset.code
            binding.assetStatus.text = AssetStatus.getById(asset.status).description

            val manufacturerStr = asset.manufacturer ?: ""
            val modelStr = asset.model ?: ""

            if (manufacturerStr.isEmpty() && modelStr.isEmpty()) {
                binding.manufacturerConstraintLayout.visibility = GONE
            } else {
                binding.manufacturerConstraintLayout.visibility = VISIBLE
                binding.manufacturer.text = manufacturerStr
                binding.model.text = modelStr
            }

            setStyle(asset)
        }

        private fun setStyle(asset: Asset) {
            val v = itemView

            // Background layouts
            // Resalta por estado del activo
            val layoutOnInventory = getDrawable(getContext().resources, R.drawable.layout_thin_border_green, null)
            val layoutMissing = getDrawable(getContext().resources, R.drawable.layout_thin_border_red, null)
            val layoutRemoved = getDrawable(getContext().resources, R.drawable.layout_thin_border_yellow, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_thin_border, null)

            val backColor: Drawable
            val foreColor: Int
            val titleForeColor: Int = darkslategray

            when (asset.status) {
                AssetStatus.onInventory.id -> {
                    backColor = layoutOnInventory!!
                    foreColor = onInventoryForeColor
                }

                AssetStatus.missing.id -> {
                    backColor = layoutMissing!!
                    foreColor = missingForeColor
                }

                AssetStatus.removed.id -> {
                    backColor = layoutRemoved!!
                    foreColor = removedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = defaultForeColor
                }
            }

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

    private object AssetDiffUtilCallback : DiffUtil.ItemCallback<Asset>() {
        override fun areItemsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Asset, newItem: Asset): Boolean {
            if (oldItem.id != newItem.id) return false
            if (oldItem.description != newItem.description) return false
            if (oldItem.ean != newItem.ean) return false
            if (oldItem.code != newItem.code) return false
            if (oldItem.active != newItem.active) return false
            if (oldItem.itemCategoryId != newItem.itemCategoryId) return false
            if (oldItem.status != newItem.status) return false
            if (oldItem.ownershipStatus != newItem.ownershipStatus) return false
            if (oldItem.warehouseId != newItem.warehouseId) return false
            if (oldItem.warehouseAreaId != newItem.warehouseAreaId) return false
            if (oldItem.serialNumber != newItem.serialNumber) return false
            if (oldItem.manufacturer != newItem.manufacturer) return false
            if (oldItem.model != newItem.model) return false
            if (oldItem.parentId != newItem.parentId) return false
            return oldItem.condition == newItem.condition
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
        private var onInventoryForeColor: Int = 0
        private var missingForeColor: Int = 0
        private var removedForeColor: Int = 0
        private var defaultForeColor: Int = 0

        private var onInventorySelectedForeColor: Int = 0
        private var missingSelectedForeColor: Int = 0
        private var removedSelectedForeColor: Int = 0
        private var defaultSelectedForeColor: Int = 0

        private var darkslategray: Int = 0
        private var lightgray: Int = 0

        /**
         * Setup colors
         * Simplemente inicializamos algunas variables con los colores que vamos a usar para cada estado.
         */
        private fun setupColors() {
            // Color de los diferentes estados
            val onInventory = getColor(getContext().resources, R.color.status_on_inventory, null)
            val missing = getColor(getContext().resources, R.color.status_missing, null)
            val removed = getColor(getContext().resources, R.color.status_removed, null)
            val default = getColor(getContext().resources, R.color.status_default, null)

            // Mejor contraste para los ítems seleccionados
            onInventorySelectedForeColor = getBestContrastColor(manipulateColor(onInventory, 0.5f))
            missingSelectedForeColor = getBestContrastColor(manipulateColor(missing, 0.5f))
            removedSelectedForeColor = getBestContrastColor(manipulateColor(removed, 0.5f))
            defaultSelectedForeColor = getBestContrastColor(manipulateColor(default, 0.5f))

            // Mejor contraste para los ítems no seleccionados
            onInventoryForeColor = getBestContrastColor(onInventory)
            missingForeColor = getBestContrastColor(missing)
            removedForeColor = getBestContrastColor(removed)
            defaultForeColor = getBestContrastColor(default)

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
         * @param asset Asset of which we are requesting the image
         */
        private fun getImagesThumbs(
            adapter: AssetRecyclerAdapter,
            holder: ImageControlHolder,
            asset: Asset
        ) {
            if (!adapter.showImages) return

            Handler(Looper.getMainLooper()).postDelayed({
                adapter.run {
                    ImageAdapter.getImages(
                        context = getContext(),
                        programData = ProgramData(
                            programObjectId = Table.asset.id.toLong(),
                            objId1 = asset.id.toString()
                        ),
                        onProgress = {
                            when (it.status) {
                                GetImageStatus.STARTING -> {
                                    waitingImagePanel(holder)
                                }

                                GetImageStatus.NO_IMAGES -> {
                                    idWithImage.remove(asset.id)
                                    collapseImagePanel(holder)
                                }

                                GetImageStatus.IMAGE_BROKEN, GetImageStatus.NO_AVAILABLE, GetImageStatus.IMAGE_AVAILABLE -> {
                                    if (!idWithImage.contains(asset.id)) {
                                        idWithImage.add(asset.id)
                                    }
                                    val image = it.image
                                    if (image != null) showImagePanel(holder, image)
                                    else collapseImagePanel(holder)
                                }
                            }
                        }
                    )
                }
            }, 0)
        }
    }

    init {
        // Set values
        recyclerView = builder.recyclerView
        fullList = builder.fullList
        checkedIdArray = builder.checkedIdArray
        multiSelect = builder.multiSelect
        showCheckBoxes = builder.showCheckBoxes
        showCheckBoxesChanged = builder.showCheckBoxesChanged
        showImages = builder.showImages
        showImagesChanged = builder.showImagesChanged
        visibleStatus = builder.visibleStatus
        filterOptions = builder.filterOptions

        dataSetChangedListener = builder.dataSetChangedListener
        checkedChangedListener = builder.checkedChangedListener
        editAssetRequiredListener = builder.editAssetRequiredListener
        addPhotoRequiredListener = builder.addPhotoRequiredListener
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
    private fun sortedVisibleList(list: MutableList<Asset>?): MutableList<Asset> {
        val croppedList = (list ?: mutableListOf()).mapNotNull { if (it.assetStatus in visibleStatus) it else null }
        return sortItems(croppedList.toMutableList())
    }

    // Sobrecargamos estos métodos para suministrar siempre una lista ordenada y filtrada por estado de visibilidad
    override fun submitList(list: MutableList<Asset>?) {
        super.submitList(sortedVisibleList(list))
    }

    override fun submitList(list: MutableList<Asset>?, commitCallback: Runnable?) {
        super.submitList(sortedVisibleList(list), commitCallback)
    }

    class Builder {
        internal lateinit var recyclerView: RecyclerView
        internal var fullList: ArrayList<Asset> = ArrayList()
        internal var checkedIdArray: ArrayList<Long> = ArrayList()
        internal var multiSelect: Boolean = false
        internal var showCheckBoxes: Boolean = false
        internal var showCheckBoxesChanged: (Boolean) -> Unit = { }
        internal var showImages: Boolean = false
        internal var showImagesChanged: (Boolean) -> Unit = { }
        internal var visibleStatus: ArrayList<AssetStatus> = arrayListOf()
        internal var filterOptions: FilterOptions = FilterOptions()

        internal var dataSetChangedListener: DataSetChangedListener? = null
        internal var checkedChangedListener: CheckedChangedListener? = null
        internal var editAssetRequiredListener: EditAssetRequiredListener? = null
        internal var addPhotoRequiredListener: AddPhotoRequiredListener? = null
        internal var albumViewRequiredListener: AlbumViewRequiredListener? = null

        // Setter methods for variables with chained methods
        fun recyclerView(`val`: RecyclerView): Builder {
            recyclerView = `val`
            return this
        }

        fun fullList(`val`: ArrayList<Asset>): Builder {
            fullList = `val`
            return this
        }

        fun checkedIdArray(`val`: ArrayList<Long>): Builder {
            checkedIdArray = `val`
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

        fun visibleStatus(`val`: ArrayList<AssetStatus>): Builder {
            visibleStatus = `val`
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

        fun editAssetRequiredListener(listener: EditAssetRequiredListener?): Builder {
            editAssetRequiredListener = listener
            return this
        }

        fun addPhotoRequiredListener(listener: AddPhotoRequiredListener?): Builder {
            addPhotoRequiredListener = listener
            return this
        }

        fun albumViewRequiredListener(listener: AlbumViewRequiredListener?): Builder {
            albumViewRequiredListener = listener
            return this
        }

        fun build(): AssetRecyclerAdapter {
            return AssetRecyclerAdapter(this)
        }
    }
}