package com.example.assetControl.viewModel.assetSelect

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetControl.data.async.asset.GetAssetAsync
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssetSelectUiState(
    val title: String = "",
    val showSelectButton: Boolean = true,
    val multiSelect: Boolean = false,
    val hideFilterPanel: Boolean = false,
    val completeList: List<Asset> = emptyList(),
    val checkedIds: Set<Long> = emptySet(),
    val currentScrollPosition: Int = 0,
    val filterCode: String = "",
    val filterCategory: ItemCategory? = null,
    val filterWarehouseArea: WarehouseArea? = null,
    val filterWarehouse: Warehouse? = null,
    val filterOnlyActive: Boolean = true,
    val filterOnlyContainers: Boolean = false,
    val searchedText: String = "", val printQty: Int = 1,
    val labelTargetId: Int? = null,
    val templateId: Long? = null,
    val showImages: Boolean = false,
    val showCheckBoxes: Boolean = false,
    val isLoading: Boolean = false,
    val lastSelected: Asset? = null,
    val firstVisiblePos: Int = 1,
    val originWarehouseArea: WarehouseArea? = null,
    val originWarehouse: Warehouse? = null,
)

class AssetSelectViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), GetAssetAsync.GetAssetAsyncListener {
    override fun onGetAssetProgress(
        msg: String,
        progressStatus: ProgressStatus,
        completeList: ArrayList<Asset>
    ) {
        when (progressStatus) {
            ProgressStatus.starting -> {
                updateState { it.copy(isLoading = true) }
            }

            ProgressStatus.canceled, ProgressStatus.crashed -> {
                updateState { it.copy(completeList = listOf(), isLoading = false) }
            }

            ProgressStatus.finished -> {
                updateState { it.copy(completeList = completeList, isLoading = false) }
            }
        }
    }

    private val _uiState = MutableStateFlow(AssetSelectUiState())
    val uiState: StateFlow<AssetSelectUiState> = _uiState.asStateFlow()

    init {
        // Inicializar estado con valores persistentes
        _uiState.value = AssetSelectUiState(
            multiSelect = savedStateHandle[ARG_MULTI_SELECT] ?: false,
            hideFilterPanel = savedStateHandle[ARG_HIDE_FILTER_PANEL] ?: false,
            completeList = savedStateHandle[ARG_COMPLETE_LIST] ?: emptyList(),
            checkedIds = savedStateHandle[ARG_CHECKED_IDS] ?: emptySet(),
            lastSelected = savedStateHandle[ARG_LAST_SELECTED],
            firstVisiblePos = savedStateHandle[ARG_FIRST_VISIBLE_POS] ?: 1,
            currentScrollPosition = savedStateHandle[ARG_CURRENT_SCROLL_POS] ?: 0,
            filterCode = savedStateHandle[ARG_FILTER_CODE] ?: "",
            filterCategory = savedStateHandle[ARG_FILTER_CATEGORY],
            filterWarehouseArea = savedStateHandle[ARG_FILTER_AREA],
            filterWarehouse = savedStateHandle[ARG_FILTER_RACK],
            filterOnlyActive = savedStateHandle[ARG_FILTER_ACTIVE] ?: true,
            filterOnlyContainers = savedStateHandle[ARG_FILTER_CONTAINERS] ?: false,
            searchedText = savedStateHandle[ARG_SEARCHED_TEXT] ?: "",
            printQty = savedStateHandle[ARG_PRINT_QTY] ?: 1,
            labelTargetId = savedStateHandle[ARG_LABEL_TARGET_ID],
            templateId = savedStateHandle[ARG_LABEL_TEMPLATE_ID],
            showImages = savedStateHandle[ARG_SHOW_IMAGES] ?: false,
            showCheckBoxes = savedStateHandle[ARG_SHOW_CHECKBOXES] ?: false,
            showSelectButton = savedStateHandle[ARG_SHOW_SELECT_BUTTON] ?: true,
            originWarehouseArea = savedStateHandle[ARG_ORIGIN_AREA],
            originWarehouse = savedStateHandle[ARG_ORIGIN_RACK],
        )
    }

    fun updateState(update: (AssetSelectUiState) -> AssetSelectUiState) {
        val newState = update(_uiState.value)

        // Persistir propiedades críticas
        savedStateHandle[ARG_TITLE] = newState.title
        savedStateHandle[ARG_MULTI_SELECT] = newState.multiSelect
        savedStateHandle[ARG_HIDE_FILTER_PANEL] = newState.hideFilterPanel
        savedStateHandle[ARG_SHOW_CHECKBOXES] = newState.showCheckBoxes
        savedStateHandle[ARG_SHOW_IMAGES] = newState.showImages
        savedStateHandle[ARG_SHOW_SELECT_BUTTON] = newState.showSelectButton

        _uiState.value = newState
    }

    val targetId get() = uiState.value.labelTargetId
    val templateId get() = uiState.value.templateId
    val printQty: Int get() = uiState.value.printQty
    val searchedText: String get() = _uiState.value.searchedText
    val filterWarehouseArea: WarehouseArea? get() = _uiState.value.filterWarehouseArea
    val filterWarehouse: Warehouse? get() = _uiState.value.filterWarehouse
    val filterCategory: ItemCategory? get() = _uiState.value.filterCategory
    val filterCode: String get() = _uiState.value.filterCode
    val filterOnlyActive: Boolean get() = uiState.value.filterOnlyActive
    val hideFilterPanel: Boolean get() = uiState.value.hideFilterPanel
    val multiSelect: Boolean get() = uiState.value.multiSelect
    val completeList: List<Asset> get() = uiState.value.completeList
    val checkedIds: Set<Long> get() = uiState.value.checkedIds
    val lastSelected: Asset? get() = uiState.value.lastSelected
    val currentScrollPosition: Int get() = uiState.value.currentScrollPosition
    val originWarehouseArea: WarehouseArea? get() = _uiState.value.originWarehouseArea
    val originWarehouse: Warehouse? get() = _uiState.value.originWarehouse

    fun applyCompleteList(
        assets: List<Asset>,
    ) {
        updateState { it.copy(completeList = assets, isLoading = false) }
    }

    fun applyFilters(
        code: String,
        category: ItemCategory? = null,
        warehouseArea: WarehouseArea? = null,
        warehouse: Warehouse? = null,
        onlyActive: Boolean,
    ) {
        updateState { state ->
            state.copy(
                searchedText = code,
                filterCode = code,
                filterCategory = category,
                filterWarehouseArea = warehouseArea,
                filterWarehouse = warehouse,
                filterOnlyActive = onlyActive,
            )
        }
        loadItems()
    }

    fun loadItems() {
        val code = uiState.value.filterCode.trim()
        val category = uiState.value.filterCategory
        val warehouseArea = uiState.value.filterWarehouseArea
        val warehouse = uiState.value.filterWarehouse

        if (category == null && code.isEmpty() && warehouseArea == null && warehouse == null) {
            _uiState.value = _uiState.value.copy(completeList = arrayListOf())
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            updateState { it.copy(isLoading = true) }
            try {
                val sync = GetAssetAsync()
                sync.addParams(this.coroutineContext as GetAssetAsync.GetAssetAsyncListener)
                sync.addExtraParams(
                    code = code,
                    itemCategory = category,
                    warehouseArea = warehouseArea,
                    onlyActive = uiState.value.filterOnlyActive
                )
                sync.execute()
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false) }
                Log.e(javaClass.simpleName, e.message.toString())
            }
        }
    }

    // Constantes para guardar estado
    companion object {
        const val ARG_TITLE = "title"
        const val ARG_SHOW_SELECT_BUTTON = "show_select_button"
        const val ARG_MULTI_SELECT = "multi_select"
        private const val ARG_HIDE_FILTER_PANEL = "hide_filter_panel"
        private const val ARG_LAST_SELECTED = "last_selected"
        private const val ARG_FIRST_VISIBLE_POS = "first_visible_pos"
        private const val ARG_COMPLETE_LIST = "complete_list"
        private const val ARG_CHECKED_IDS = "checked_ids"
        private const val ARG_CURRENT_SCROLL_POS = "current_scroll_pos"
        private const val ARG_FILTER_CODE = "filter_code"
        private const val ARG_FILTER_CATEGORY = "filter_ean"
        private const val ARG_FILTER_AREA = "filter_area"
        private const val ARG_FILTER_RACK = "filter_warehouse"
        private const val ARG_FILTER_ACTIVE = "filter_active"
        private const val ARG_FILTER_CONTAINERS = "filter_containers"
        private const val ARG_SEARCHED_TEXT = "searched_text"
        private const val ARG_PRINT_QTY = "print_qty"
        private const val ARG_LABEL_TARGET_ID = "label_target_id"
        private const val ARG_LABEL_TEMPLATE_ID = "template_id"
        private const val ARG_SHOW_IMAGES = "show_images"
        private const val ARG_SHOW_CHECKBOXES = "show_checkboxes"
        private const val ARG_ORIGIN_AREA = "origin_area"
        private const val ARG_ORIGIN_RACK = "origin_warehouse"
    }
}