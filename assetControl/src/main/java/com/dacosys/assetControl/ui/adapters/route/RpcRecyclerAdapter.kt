package com.dacosys.assetControl.ui.adapters.route

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Filter
import android.widget.Filterable
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
import com.dacosys.assetControl.data.enums.route.RouteProcessStatus
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent
import com.dacosys.assetControl.databinding.RouteProcessContentRowBinding
import com.dacosys.assetControl.ui.adapters.asset.AssetRecyclerAdapter.FilterOptions
import com.dacosys.assetControl.ui.adapters.interfaces.Interfaces.*
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.getColorWithAlpha
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.manipulateColor
import java.util.*


/**
 * Created by Agustin on 10/03/2023.
 */

class RpcRecyclerAdapter(
    private val recyclerView: RecyclerView,
    var fullList: ArrayList<RouteProcessContent>,
    var checkedIdArray: ArrayList<Long> = ArrayList(),
    var multiSelect: Boolean = false,
    var showCheckBoxes: Boolean = false,
    private var showCheckBoxesChanged: (Boolean) -> Unit = { },
    var visibleStatus: ArrayList<RouteProcessStatus> = ArrayList(RouteProcessStatus.getAll()),
    private var filterOptions: FilterOptions = FilterOptions()
) : ListAdapter<RouteProcessContent, ViewHolder>(RouteProcessContentDiffUtilCallback), Filterable {

    private var currentIndex = NO_POSITION
    private var dataSetChangedListener: DataSetChangedListener? = null
    private var checkedChangedListener: CheckedChangedListener? = null

    // Listener para los eventos que requieren que la actividad anfitriona
    // muestre un mensaje de espera mientras se realiza alguna tarea que demanda más tiempo.
    private var uiEventListener: UiEventListener? = null

    // Clase para distinguir actualizaciones parciales
    enum class PAYLOADS {
        STATUS_CHANGE,
        CHECKBOX_VISIBILITY,
    }

    fun clear() {
        checkedIdArray.clear()

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
    ) {
        this.checkedChangedListener = checkedChangedListener
        this.dataSetChangedListener = dataSetChangedListener
    }

    fun refreshUiEventListener(uiEventListener: UiEventListener?) {
        this.uiEventListener = uiEventListener
    }

    private object RouteProcessContentDiffUtilCallback : DiffUtil.ItemCallback<RouteProcessContent>() {
        override fun areItemsTheSame(oldItem: RouteProcessContent, newItem: RouteProcessContent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RouteProcessContent, newItem: RouteProcessContent): Boolean {
            if (oldItem.routeProcessId != newItem.routeProcessId) return false
            if (oldItem.dataCollectionRuleId != newItem.dataCollectionRuleId) return false
            if (oldItem.level != newItem.level) return false
            if (oldItem.position != newItem.position) return false
            if (oldItem.routeProcessStatusId != newItem.routeProcessStatusId) return false
            if (oldItem.dataCollectionId != newItem.dataCollectionId) return false
            if (oldItem.id != newItem.id) return false
            if (oldItem.assetId != newItem.assetId) return false
            if (oldItem.assetDescription != newItem.assetDescription) return false
            if (oldItem.code != newItem.code) return false
            if (oldItem.warehouseId != newItem.warehouseId) return false
            if (oldItem.warehouseDescription != newItem.warehouseDescription) return false
            if (oldItem.warehouseAreaId != newItem.warehouseAreaId) return false
            if (oldItem.warehouseAreaDescription != newItem.warehouseAreaDescription) return false
            if (oldItem.routeId != newItem.routeId) return false
            if (oldItem.routeDescription != newItem.routeDescription) return false
            return true
        }
    }

    companion object {
        // Aquí definimos dos constantes para identificar los dos diseños diferentes
        const val SELECTED_VIEW_TYPE = 1
        const val UNSELECTED_VIEW_TYPE = 2

        // region COLORS
        private var layoutProcessedForeColor: Int = 0
        private var layoutNotProcessedForeColor: Int = 0
        private var layoutSkippedForeColor: Int = 0
        private var layoutDefaultForeColor: Int = 0

        private var layoutProcessedSelectedForeColor: Int = 0
        private var layoutNotProcessedSelectedForeColor: Int = 0
        private var layoutSkippedSelectedForeColor: Int = 0
        private var layoutDefaultSelectedForeColor: Int = 0

        private fun setupColors() {
            // Color de los diferentes estados
            layoutProcessedForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_rpc_processed, null))
            layoutSkippedForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_rpc_skipped, null))
            layoutNotProcessedForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_rpc_not_processed, null))
            layoutDefaultForeColor =
                getBestContrastColor(getColor(getContext().resources, R.color.status_default, null))

            // Mejor contraste para los ítems seleccionados
            layoutProcessedSelectedForeColor = getBestContrastColor(manipulateColor(layoutProcessedForeColor, 0.5f))
            layoutSkippedSelectedForeColor = getBestContrastColor(manipulateColor(layoutSkippedForeColor, 0.5f))
            layoutNotProcessedSelectedForeColor =
                getBestContrastColor(manipulateColor(layoutNotProcessedForeColor, 0.5f))
            layoutDefaultSelectedForeColor = getBestContrastColor(manipulateColor(layoutDefaultForeColor, 0.5f))
        }
        // endregion
    }

    // El método onCreateViewHolder infla los diseños para cada tipo de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SELECTED_VIEW_TYPE -> {
                SelectedViewHolder(
                    RouteProcessContentRowBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                UnselectedViewHolder(
                    RouteProcessContentRowBinding.inflate(
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
    private fun setCheckBoxLogic(checkBox: CheckBox, content: RouteProcessContent, position: Int) {
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
        var selected: RouteProcessContent? = null
        var firstVisible: RouteProcessContent? = null

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                // Guardamos el item seleccionado y la posición del scroll
                selected = currentRpc()
                val vsIds = visibleStatus.map { it.id }.toList()
                var scrollPos =
                    (recyclerView.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()
                        ?: NO_POSITION

                if (scrollPos != NO_POSITION && itemCount > scrollPos) {
                    var currentScrolled = getItem(scrollPos)

                    // Comprobamos si es visible el ítem del Scroll
                    if (currentScrolled.routeProcessStatusId in vsIds)
                        firstVisible = currentScrolled
                    else {
                        // Si no es visible, intentar encontrar el próximo visible.
                        while (firstVisible == null) {
                            scrollPos++
                            if (itemCount > scrollPos) {
                                currentScrolled = getItem(scrollPos)
                                if (currentScrolled.routeProcessStatusId in vsIds)
                                    firstVisible = currentScrolled
                            } else break
                        }
                    }
                }

                // Filtramos los resultados
                val results = FilterResults()
                var r: ArrayList<RouteProcessContent> = ArrayList()
                if (constraint != null) {
                    val filterString = constraint.toString().lowercase(Locale.getDefault())
                    if (filterString.isNotEmpty()) {
                        var filterableItem: RouteProcessContent

                        for (i in 0 until fullList.size) {
                            filterableItem = fullList[i]

                            // Descartamos aquellos que no debe ser visibles
                            if (filterableItem.routeProcessStatusId !in vsIds) continue

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

            fun isFilterable(content: RouteProcessContent, filterString: String): Boolean =
                content.assetStr.contains(filterString, true) ||
                        content.assetCode.contains(filterString, true) ||
                        content.routeStr.contains(filterString, true) ||
                        content.warehouseStr.contains(filterString, true) ||
                        content.warehouseAreaStr.contains(filterString, true)

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?, results: FilterResults?,
            ) {
                var contents: ArrayList<RouteProcessContent> = ArrayList()
                if (results?.values != null) {
                    contents = results.values as ArrayList<RouteProcessContent>
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

    private fun sortItems(originalList: MutableList<RouteProcessContent>): ArrayList<RouteProcessContent> {
        // Run the follow method on each of the roots
        return ArrayList(
            originalList.sortedWith(
                compareBy(
                    { it.level },
                    { it.position },
                )
            ).toList()
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun refreshFilter(options: FilterOptions) {
        filterOptions = options
        filter.filter(filterOptions.filterString)
    }

    private fun refreshFilter() {
        refreshFilter(filterOptions)
    }

    fun add(contents: ArrayList<RouteProcessContent>, scrollToPos: Boolean) {
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

    fun add(content: RouteProcessContent?) {
        if (content == null) return
        val lastPost = fullList.lastIndex + 1
        add(content, lastPost)
    }

    fun add(content: RouteProcessContent, position: Int) {
        fullList.add(position, content)
        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()
                selectItem(getIndex(content))
            }
        }
    }

    fun update(content: RouteProcessContent) {
        val t = fullList.firstOrNull { it == content } ?: return

        val index = fullList.indexOf(t)
        if (index != -1) fullList[index] = content

        submitList(fullList) {
            run {
                // Notificamos al Listener superior
                dataSetChangedListener?.onDataSetChanged()

                // Notificamos el cambio de ítem para redibujarlo
                notifyItemChanged(index)
            }
        }
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

    fun selectItem(a: RouteProcessContent?, scroll: Boolean = true) {
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

    fun firstNotProcessedIndex(): Int {
        val f = currentList.firstOrNull { it.routeProcessStatusId == RouteProcessStatus.notProcessed.id }
        return if (f == null) NO_POSITION else getIndex(f)
    }

    fun firstIndex(): Int {
        return if (itemCount > 0) 0 else NO_POSITION
    }

    fun currentPos(): Int {
        return currentIndex
    }

    @Suppress("unused")
    private fun getItemsByStatus(statusId: Int): ArrayList<RouteProcessContent> {
        return ArrayList(fullList.mapNotNull { if (it.routeProcessStatusId == statusId) it else null })
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
        return currentList.indexOfFirst { it.id == id }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    private fun getIndex(content: RouteProcessContent): Int {
        return currentList.indexOf(content)
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getIndexByAssetId(itemId: Long): Int {
        return currentList.indexOfFirst { it.assetId == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByAsset(item: Asset): RouteProcessContent? {
        return currentList.firstOrNull { it.assetId == item.id }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByIndex(pos: Int): RouteProcessContent? {
        return if (currentList.lastIndex > pos) currentList[pos] else null
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentById(itemId: Long): RouteProcessContent? {
        return currentList.firstOrNull { it.id == itemId }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getContentByCode(code: String): RouteProcessContent? {
        return currentList.firstOrNull { it.assetCode == code }
    }

    fun getAllChecked(): ArrayList<RouteProcessContent> {
        val items = ArrayList<RouteProcessContent>()
        checkedIdArray.mapNotNullTo(items) { getContentById(it) }
        return items
    }

    fun currentRpc(): RouteProcessContent? {
        if (currentIndex == NO_POSITION) return null
        return if (currentList.any() && itemCount > currentIndex) getItem(currentIndex)
        else null
    }

    fun currentLevel(): Int {
        return getItem(0)?.level ?: 0
    }

    fun selectPrev() {
        if (!fullList.any()) return

        if (!fullList.any { it.routeProcessStatusId == RouteProcessStatus.notProcessed.id }) {
            selectFirst()
            return
        }

        var prevIndex = if (currentIndex == 0) itemCount - 1 else currentIndex - 1

        while (true) {
            if (prevIndex == currentIndex) {
                break
            }

            if (prevIndex == 0) {
                prevIndex = itemCount - 1
                continue
            }

            val z = getItem(prevIndex)
            if (z == null) {
                prevIndex--
                continue
            }

            val processStatusId = z.routeProcessStatusId

            if (processStatusId == RouteProcessStatus.processed.id ||
                processStatusId == RouteProcessStatus.skipped.id
            ) {
                prevIndex--
                continue
            }

            // Seleccionar la fila correcta
            selectItem(z)
            break
        }
    }

    private fun selectFirst() {
        selectItem(0, true)
    }

    private fun selectLast() {
        selectItem(fullList.lastIndex, true)
    }

    fun selectNext() {
        if (!fullList.any()) return

        if (!fullList.any { it.routeProcessStatusId == RouteProcessStatus.notProcessed.id }) {
            selectLast()
            return
        }

        var nextIndex = if (itemCount == currentIndex + 1) 0 else currentIndex + 1

        while (true) {
            if (nextIndex == currentIndex) {
                break
            }

            if (nextIndex == itemCount) {
                nextIndex = 0
                continue
            }

            val z = getItem(nextIndex)
            if (z == null) {
                nextIndex++
                continue
            }

            val processStatusId = z.routeProcessStatusId

            if (processStatusId == RouteProcessStatus.processed.id ||
                processStatusId == RouteProcessStatus.skipped.id
            ) {
                nextIndex++
                continue
            }

            // Seleccionar la fila correcta
            selectItem(z)
            break
        }
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    val countChecked: Int
        get() {
            return checkedIdArray.count()
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    val isLevelCompleted: Boolean
        get() {
            return notProcessed <= 0
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    val processed: Int
        get() = fullList.count {
            it.routeProcessStatusId == RouteProcessStatus.processed.id
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    val notProcessed: Int
        get() = fullList.count {
            it.routeProcessStatusId == RouteProcessStatus.notProcessed.id
        }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    val skipped: Int
        get() = fullList.count {
            it.routeProcessStatusId == RouteProcessStatus.skipped.id
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
    private fun updateCheckedList(content: RouteProcessContent, isChecked: Boolean) {
        return updateCheckedList(content.id, isChecked)
    }

    private fun updateCheckedList(rpcId: Long, isChecked: Boolean) {
        checkedIdArray.remove(rpcId)
        if (isChecked) checkedIdArray.add(rpcId)
    }

    fun addVisibleStatus(status: RouteProcessStatus) {
        if (visibleStatus.contains(status)) return
        visibleStatus.add(status)
        refreshFilter()
    }

    fun removeVisibleStatus(status: RouteProcessStatus) {
        if (!visibleStatus.contains(status)) return
        visibleStatus.remove(status)
        refreshFilter()
    }

    // Aquí creamos dos ViewHolder, uno para cada tipo de vista
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    class SelectedViewHolder(val binding: RouteProcessContentRowBinding) : ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bindStatusChange(content: RouteProcessContent) {
            binding.routeProcessStatusStr.text = content.status.description
        }

        fun bind(content: RouteProcessContent, checkBoxVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindStatusChange(content = content)

            binding.routeProcessStatusStr.text = content.status.description
            binding.assetStr.text = content.assetStr
            binding.assetCode.text = content.assetCode
            binding.warehouseAreaStr.text = content.warehouseAreaStr
            binding.warehouseStr.text = content.warehouseStr

            if (content.warehouseAreaStr.isEmpty() &&
                content.warehouseStr.isEmpty()
            ) {
                binding.warehouseAreaStr.visibility = GONE
                binding.warehouseStr.visibility = GONE
            } else {
                binding.warehouseAreaStr.visibility = VISIBLE
                binding.warehouseStr.visibility = VISIBLE
            }

            setStyle(content)
        }

        fun setStyle(content: RouteProcessContent) {
            val v = itemView

            // Background layouts
            val layoutProcessed = getDrawable(getContext().resources, R.drawable.layout_rpc_processed, null)
            val layoutSkipped = getDrawable(getContext().resources, R.drawable.layout_rpc_skipped, null)
            val layoutNotProcessed = getDrawable(getContext().resources, R.drawable.layout_not_processed, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_rpc_default, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.routeProcessStatusId) {
                RouteProcessStatus.processed.id -> {
                    backColor = layoutProcessed!!
                    foreColor = layoutProcessedSelectedForeColor
                }

                RouteProcessStatus.notProcessed.id -> {
                    backColor = layoutNotProcessed!!
                    foreColor = layoutNotProcessedSelectedForeColor
                }

                RouteProcessStatus.skipped.id -> {
                    backColor = layoutSkipped!!
                    foreColor = layoutSkippedSelectedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = layoutDefaultSelectedForeColor
                }
            }

            val titleForeColor: Int = manipulateColor(foreColor, 0.8f)

            v.background = backColor
            binding.routeProcessStatusStr.setTextColor(foreColor)
            binding.assetStr.setTextColor(foreColor)
            binding.assetCode.setTextColor(foreColor)
            binding.warehouseAreaStr.setTextColor(foreColor)
            binding.warehouseStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate", "unused")
    internal class UnselectedViewHolder(val binding: RouteProcessContentRowBinding) : ViewHolder(binding.root) {
        fun bindCheckBoxVisibility(checkBoxVisibility: Int = GONE) {
            binding.checkBoxConstraintLayout.visibility = checkBoxVisibility
        }

        fun bindCheckBoxState(checked: Boolean) {
            binding.checkBox.isChecked = checked
        }

        fun bindStatusChange(content: RouteProcessContent) {
            binding.routeProcessStatusStr.text = content.status.description
        }

        fun bind(content: RouteProcessContent, checkBoxVisibility: Int = GONE) {
            bindCheckBoxVisibility(checkBoxVisibility)
            bindStatusChange(content = content)

            binding.routeProcessStatusStr.text = content.status.description
            binding.assetStr.text = content.assetStr
            binding.assetCode.text = content.assetCode
            binding.warehouseAreaStr.text = content.warehouseAreaStr
            binding.warehouseStr.text = content.warehouseStr

            if (content.warehouseAreaStr.isEmpty() &&
                content.warehouseStr.isEmpty()
            ) {
                binding.warehouseAreaStr.visibility = GONE
                binding.warehouseStr.visibility = GONE
            } else {
                binding.warehouseAreaStr.visibility = VISIBLE
                binding.warehouseStr.visibility = VISIBLE
            }

            setStyle(content)
        }

        fun setStyle(content: RouteProcessContent) {
            val v = itemView

            // Background layouts
            val layoutProcessed = getDrawable(getContext().resources, R.drawable.layout_rpc_processed, null)
            val layoutSkipped = getDrawable(getContext().resources, R.drawable.layout_rpc_skipped, null)
            val layoutNotProcessed = getDrawable(getContext().resources, R.drawable.layout_not_processed, null)
            val layoutDefault = getDrawable(getContext().resources, R.drawable.layout_rpc_default, null)

            val backColor: Drawable
            val foreColor: Int
            when (content.routeProcessStatusId) {
                RouteProcessStatus.processed.id -> {
                    backColor = layoutProcessed!!
                    foreColor = layoutProcessedForeColor
                }

                RouteProcessStatus.notProcessed.id -> {
                    backColor = layoutNotProcessed!!
                    foreColor = layoutNotProcessedForeColor
                }

                RouteProcessStatus.skipped.id -> {
                    backColor = layoutSkipped!!
                    foreColor = layoutSkippedForeColor
                }

                else -> {
                    backColor = layoutDefault!!
                    foreColor = layoutDefaultForeColor
                }
            }

            val titleForeColor: Int = manipulateColor(foreColor, 1.4f)

            v.background = backColor
            binding.routeProcessStatusStr.setTextColor(foreColor)
            binding.assetStr.setTextColor(foreColor)
            binding.assetCode.setTextColor(foreColor)
            binding.warehouseAreaStr.setTextColor(foreColor)
            binding.warehouseStr.setTextColor(foreColor)
            binding.checkBox.buttonTintList = ColorStateList.valueOf(titleForeColor)
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
    }

    /**
     * Return a sorted list of visible state items
     *
     * @param list
     * @return Lista ordenada con los estados visibles
     */
    private fun sortedVisibleList(list: MutableList<RouteProcessContent>?): MutableList<RouteProcessContent> {
        val croppedList = (list
            ?: mutableListOf()).mapNotNull { if (it.routeProcessStatusId in visibleStatus.map { it2 -> it2.id }) it else null }
        return sortItems(croppedList.toMutableList())
    }

    // Sobrecargamos estos métodos para suministrar siempre una lista ordenada y filtrada por estado de visibilidad
    override fun submitList(list: MutableList<RouteProcessContent>?) {
        super.submitList(sortedVisibleList(list))
    }

    override fun submitList(list: MutableList<RouteProcessContent>?, commitCallback: Runnable?) {
        super.submitList(sortedVisibleList(list), commitCallback)
    }
}