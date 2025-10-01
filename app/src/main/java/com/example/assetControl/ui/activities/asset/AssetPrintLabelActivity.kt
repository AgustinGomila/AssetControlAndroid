package com.example.assetControl.ui.activities.asset

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dacosys.imageControl.dto.DocumentContent
import com.dacosys.imageControl.dto.DocumentContentRequestResult
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.network.download.GetImages.Companion.toDocumentContentList
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.ui.activities.ImageControlCameraActivity
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.enums.asset.AssetStatus
import com.example.assetControl.data.enums.barcode.BarcodeLabelTarget
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.entity.asset.TempAssetEntity
import com.example.assetControl.data.room.repository.asset.TempAssetRepository
import com.example.assetControl.data.room.repository.barcode.BarcodeLabelCustomRepository
import com.example.assetControl.databinding.AssetPrintLabelActivityTopPanelCollapsedBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.rfid.Rfid.Companion.isRfidRequired
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.network.utils.Connection.Companion.autoSend
import com.example.assetControl.ui.adapters.asset.AssetRecyclerAdapter
import com.example.assetControl.ui.adapters.asset.AssetRecyclerAdapter.FilterOptions
import com.example.assetControl.ui.adapters.interfaces.Interfaces
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.asset.AssetSelectFilterFragment
import com.example.assetControl.ui.fragments.asset.SummaryFragment
import com.example.assetControl.ui.fragments.print.PrinterFragment
import com.example.assetControl.ui.panel.BasePanelActivity
import com.example.assetControl.ui.panel.PanelController
import com.example.assetControl.ui.panel.PanelState
import com.example.assetControl.ui.panel.PanelType
import com.example.assetControl.utils.conversor.IntConversor.orZero
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.ParcelLong
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.viewModel.assetSelect.AssetSelectUiState
import com.example.assetControl.viewModel.assetSelect.AssetSelectViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.parceler.Parcels

class AssetPrintLabelActivity : BasePanelActivity(), SwipeRefreshLayout.OnRefreshListener,
    Scanner.ScannerListener, Rfid.RfidDeviceListener, AssetSelectFilterFragment.FragmentListener,
    PrinterFragment.FragmentListener, Interfaces.CheckedChangedListener,
    Interfaces.DataSetChangedListener, Interfaces.AddPhotoRequiredListener,
    Interfaces.AlbumViewRequiredListener,
    Interfaces.EditAssetRequiredListener {

    private val viewModel: AssetSelectViewModel by viewModels()

    // region Set Panels
    override val stateConfig: PanelController.PanelStateConfiguration
        get() = PanelController.PanelStateConfiguration(
            initialPanelTopState = PanelState.COLLAPSED,
            initialPanelBottomState = PanelState.EXPANDED,
        )
    override val layoutConfig: PanelController.PanelLayoutConfiguration
        get() = PanelController.PanelLayoutConfiguration(
            topPanelExpandedLayout = R.layout.asset_print_label_activity_bottom_panel_collapsed,
            bottomPanelExpandedLayout = R.layout.asset_print_label_activity_top_panel_collapsed,
            allPanelsExpandedLayout = R.layout.asset_print_label_activity,
            allPanelsCollapsedLayout = R.layout.asset_print_label_activity_both_panels_collapsed,
        )
    override val textConfig: PanelController.PanelTextConfiguration
        get() = PanelController.PanelTextConfiguration(
            topButtonText = R.string.label_print,
            bottomButtonText = R.string.search_options,
        )
    override val animationConfig: PanelController.PanelAnimationConfiguration
        get() = PanelController.PanelAnimationConfiguration(
            postImeShowAnimation = ::postImeShowAnimation,
            postTopPanelAnimation = ::postTopPanelAnimation,
            postBottomPanelAnimation = ::postBottomPanelAnimation,
        )

    override fun provideRootLayout(): ConstraintLayout {
        return binding.root
    }

    override fun provideTopButton(): Button {
        return binding.expandTopPanelButton
    }

    override fun provideBottomButton(): Button? {
        return binding.expandBottomPanelButton
    }

    private fun postImeShowAnimation() {
        if (qtyTextViewFocused) handlePanelState(PanelType.BOTTOM, PanelState.COLLAPSED)
        else handlePanelState(PanelState.COLLAPSED, PanelState.COLLAPSED)
    }

    private fun postBottomPanelAnimation() {
        if (panelBottomState == PanelState.EXPANDED) filterFragment?.refreshViews()
    }

    private fun postTopPanelAnimation() {
        if (panelBottomState == PanelState.EXPANDED) printerFragment?.refreshViews()
    }
    // endregion Set Panels

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private var isFinishingByUser = false
    private fun destroyLocals() {
        // Borramos los Ids temporales que se usaron en la actividad.
        if (isFinishingByUser) TempAssetRepository().deleteAll()

        adapter?.refreshListeners()
        adapter?.refreshImageControlListeners()
        filterFragment?.onDestroy()
        summaryFragment?.onDestroy()
        printerFragment?.onDestroy()
    }
    // endregion

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 1000)
    }

    override fun onDataSetChanged() {
        fillSummaryRow()
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = itemCount == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.recyclerView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        fillSummaryRow()
    }

    private var rejectNewInstances = false

    // Se usa para saber si estamos en onStart luego de onCreate
    private var fillRequired = false

    // Configuración guardada de los controles que se ven o no se ven
    private var adapter: AssetRecyclerAdapter? = null

    private var filterFragment: AssetSelectFilterFragment? = null
    private var printerFragment: PrinterFragment? = null
    private var summaryFragment: SummaryFragment? = null

    // Image Control
    private val menuItemShowImages = 9999
    private var showImages
        get() = sr.prefsGetBoolean(Preference.printLabelAssetShowImages)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelAssetShowImages.key, value)
        }

    private var showCheckBoxes
        get() = if (!viewModel.multiSelect) false
        else sr.prefsGetBoolean(Preference.printLabelAssetShowCheckBoxes)
        set(value) {
            sr.prefsPutBoolean(Preference.printLabelAssetShowCheckBoxes.key, value)
        }

    private val visibleStatus
        get() = filterFragment?.visibleStatusArray ?: ArrayList(AssetStatus.getAll())

    private lateinit var binding: AssetPrintLabelActivityTopPanelCollapsedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AssetPrintLabelActivityTopPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupObservers()
        setupActivity(savedInstanceState)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Carga del estado del adaptador desde la tabla temporal.
                    val list = TempAssetRepository().select().map { Asset(it.tempId) }
                    viewModel.applyCompleteList(list)

                    updateUI(state)
                }
            }
            repeatOnLifecycle(Lifecycle.State.DESTROYED) {
                // Guardar en la DB temporalmente los ítems listados
                TempAssetRepository().insert(currentList.map { TempAssetEntity(it.id) })
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupActivity(savedInstanceState: Bundle?) {
        setScreenRotation(this)

        initPanelController(savedInstanceState)

        setupBackPressHandler()
        setScrollListener()
        setFragments()

        setSelectFragment()
        setupPrintFragment()
        setSearchEditText()

        if (savedInstanceState == null) {
            TempAssetRepository().deleteAll()
        }

        setupSwipe()
        setButtons()
        setSearchPanelVisibility()

        fillRequired = true
        setupUI(binding.root, this)
    }

    private fun updateUI(state: AssetSelectUiState) {
        binding.topAppbar.title = state.title
        binding.expandBottomPanelButton?.visibility = if (state.hideFilterPanel) GONE else View.VISIBLE

        // Actualizar adapter
        adapter?.let {
            it.setMultiSelect(state.multiSelect)
            it.setCheckedIds(state.checkedIds)
            it.setFullList(state.completeList)
            it.refreshFilter(FilterOptions(state.searchedText))
        }

        // Controlar loading
        binding.swipeRefresh.isRefreshing = state.isLoading
    }

    // region Valores con información de items contados y esperados
    private val currentAsset: Asset?
        get() = adapter?.currentAsset()

    private val currentList: ArrayList<Asset>
        get() = adapter?.fullList ?: arrayListOf()

    private val itemCount: Int
        get() = adapter?.itemCount.orZero()

    private val countChecked: Int
        get() = adapter?.countChecked.orZero()

    private val allChecked: List<Asset>
        get() = adapter?.getAllChecked().orEmpty()
    // endregion Valores con información de items contados y esperados

    private fun setSelectFragment() {
        filterFragment?.setListener(this)
        filterFragment?.warehouseArea = viewModel.filterWarehouseArea
        filterFragment?.itemCode = viewModel.filterCode
        filterFragment?.itemCategory = viewModel.filterCategory
        filterFragment?.onlyActive = viewModel.filterOnlyActive
    }

    private fun setupPrintFragment() {
        printerFragment?.setListener(this)
        val target = BarcodeLabelTarget.Asset
        printerFragment?.barcodeLabelTarget = target

        val id = sr.prefsGetLong(Preference.defaultBarcodeLabelCustomAsset)
        val blc = BarcodeLabelCustomRepository().selectById(id)
        if (blc != null) {
            printerFragment?.barcodeLabelCustom = blc
        }

        if (viewModel.targetId == null) {
            viewModel.updateState { it.copy(labelTargetId = target.id, templateId = id) }
        }
    }

    private fun setScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.updateState {
                    it.copy(
                        currentScrollPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    )
                }
            }
        })
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setFragments() {
        filterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as AssetSelectFilterFragment
        summaryFragment = supportFragmentManager.findFragmentById(binding.summaryFragment.id) as SummaryFragment
        printerFragment = supportFragmentManager.findFragmentById(binding.printFragment.id) as PrinterFragment
    }

    private fun setupSwipe() {
        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun setButtons() {
        binding.okButton.setOnClickListener { itemSelect() }
    }

    private fun setSearchEditText() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.updateState { it.copy(searchedText = s.toString()) }
                adapter?.refreshFilter(FilterOptions(viewModel.searchedText, true))
            }
        })
        binding.searchEditText.setText(viewModel.searchedText, TextView.BufferType.EDITABLE)
        binding.searchEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || keyEvent.action == KeyEvent.ACTION_UP &&
                (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            ) {
                closeKeyboard(this)
            }
            false
        }
        binding.searchEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        binding.searchTextImageView.setOnClickListener { binding.searchEditText.requestFocus() }
        binding.searchTextClearImageView.setOnClickListener {
            binding.searchEditText.setText("")
        }
    }

    private fun setSearchPanelVisibility() {
        if (viewModel.hideFilterPanel) {
            handlePanelState(PanelType.BOTTOM, PanelState.COLLAPSED)
            if (lastOrientation == Configuration.ORIENTATION_PORTRAIT) binding.expandBottomPanelButton?.visibility =
                GONE
        }
    }

    override fun onStart() {
        super.onStart()

        if (fillRequired) {
            fillRequired = false
            fillAdapter(viewModel.completeList)
        }
    }

    private fun itemSelect() {
        closeKeyboard(this)

        if (adapter == null) {
            setResult(RESULT_CANCELED)
        } else {
            val data = Intent()

            val asset = currentAsset
            val countChecked = countChecked
            var assetArray: List<Asset> = listOf()

            if (!viewModel.multiSelect && asset != null) {
                data.putParcelableArrayListExtra("ids", arrayListOf(ParcelLong(asset.id)))
                setResult(RESULT_OK, data)
            } else if (viewModel.multiSelect) {
                if (countChecked > 0 || asset != null) {
                    if (countChecked > 0) assetArray = allChecked
                    else if (adapter?.showCheckBoxes == false) {
                        assetArray = arrayListOf(asset!!)
                    }
                    data.putParcelableArrayListExtra(
                        "ids",
                        assetArray.map { ParcelLong(it.id) } as ArrayList<ParcelLong>)
                    setResult(RESULT_OK, data)
                } else {
                    setResult(RESULT_CANCELED)
                }
            } else {
                setResult(RESULT_CANCELED)
            }
        }

        isFinishingByUser = true
        finish()
    }

    private fun fillSummaryRow() {
        runOnUiThread {
            if (viewModel.multiSelect) {
                summaryFragment?.setTitles(getString(R.string.total), getString(R.string.checked))
                if (adapter != null) {
                    summaryFragment?.fill(
                        total = adapter?.totalVisible,
                        checked = countChecked,
                        onInventory = adapter?.totalOnInventory,
                        missing = adapter?.totalMissing,
                        removed = adapter?.totalRemoved
                    )
                }
            } else {
                summaryFragment?.setTitles(getString(R.string.total), getString(R.string.assets))
                if (adapter != null) {
                    summaryFragment?.fill(
                        total = adapter?.totalVisible,
                        checked = adapter?.totalVisible,
                        onInventory = adapter?.totalOnInventory,
                        missing = adapter?.totalMissing,
                        removed = adapter?.totalRemoved
                    )
                }
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = show
        }
    }

    private fun fillAdapter(data: List<Asset>) {
        showProgressBar(true)

        if (adapter != null) {
            viewModel.updateState { it.copy(lastSelected = currentAsset) }
        }

        lifecycleScope.launch {
            try {
                setupRecyclerView(data)
                binding.recyclerView.doOnNextLayout {
                    applyInitialSelections()
                }

            } catch (e: Exception) {
                handleAdapterError(e)
            } finally {
                cleanupAfterSetup()
            }
        }
    }

    private fun cleanupAfterSetup() {
        closeKeyboard(this)
        showProgressBar(false)
    }

    private suspend fun setupRecyclerView(data: List<Asset>) {
        val builder = AssetRecyclerAdapter.Builder().recyclerView(binding.recyclerView)
            .visibleStatus(visibleStatus)
            .fullList(data)
            .checkedIdArray(viewModel.checkedIds.toList())
            .multiSelect(viewModel.multiSelect)
            .showCheckBoxes(`val` = showCheckBoxes, callback = { showCheckBoxes = it })
            .filterOptions(FilterOptions(viewModel.searchedText))
            .dataSetChangedListener(this)
            .checkedChangedListener(this)
            .editAssetRequiredListener(this)

        if (svm.useImageControl) {
            builder
                .showImages(`val` = showImages, callback = { showImages = it })
                .addPhotoRequiredListener(this)
                .albumViewRequiredListener(this)
        }

        adapter = builder.build()

        withContext(Dispatchers.Main.immediate) {
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this.adapter
                setHasFixedSize(true)
            }
        }
    }

    private fun applyInitialSelections() {
        if (itemCount == 1 && viewModel.hideFilterPanel) {
            adapter?.selectItem(0, true)
            itemSelect()
        } else {
            adapter?.apply {
                if (viewModel.lastSelected != null) {
                    selectItem(viewModel.lastSelected, false)
                    scrollToPos(viewModel.currentScrollPosition, true)
                } else {
                    selectItem(0)
                }
            }
        }
    }

    private fun handleAdapterError(e: Exception) {
        e.printStackTrace()
        ErrorLog.writeLog(this, this::class.java.simpleName, e)

        lifecycleScope.launch(Dispatchers.Main) {
            showMessage(getString(R.string.error_loading_data), ERROR)
        }
    }

    private fun showMessage(msg: String, type: SnackBarType) {
        if (isFinishing || isDestroyed) return
        if (type == ERROR) logError(msg)
        showMessage(msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false
        closeKeyboard(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            ScannerManager.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return sr.prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) showMessage(scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scanCode.trim().isEmpty()) {
                val res = getString(R.string.invalid_code)
                showMessage(res, SnackBarType.ERROR)
                ErrorLog.writeLog(this, this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                code = scanCode,
                searchWarehouseAreaId = false,
                searchAssetCode = true,
                searchAssetSerial = true,
                searchAssetEan = true
            )

            val asset = if (sc.asset != null) {
                sc.asset
            } else {
                val res = this.getString(R.string.invalid_asset_code)
                showMessage(res, SnackBarType.ERROR)
                null
            }

            if (asset != null) {
                adapter?.selectItem(asset)
                printerFragment?.printAsset(arrayListOf(asset))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            showMessage(ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        // Opción de visibilidad de Imágenes
        if (svm.useImageControl) {
            menu.add(Menu.NONE, menuItemShowImages, menu.size, context.getString(R.string.show_images))
                .setChecked(showImages).isCheckable = true
            val item = menu.findItem(menuItemShowImages)
            if (showImages)
                item.icon = ContextCompat.getDrawable(context, R.drawable.ic_photo_library)
            else
                item.icon = ContextCompat.getDrawable(context, R.drawable.ic_hide_image)
            item.icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                )
        }

        val menuItems = menu.size

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        // Opciones de visibilidad del menú
        val allStatus = AssetStatus.getAll()
        val s = visibleStatus
        for (i in allStatus.indices) {
            menu.add(0, allStatus[i].id, i + menuItems, allStatus[i].description)
                .setChecked(s.contains(allStatus[i])).isCheckable = true
        }

        //region Icon colors
        val colorDefault = ResourcesCompat.getColor(resources, R.color.status_default, null)
        val colorOnInventory = ResourcesCompat.getColor(resources, R.color.status_on_inventory, null)
        val colorMissing = ResourcesCompat.getColor(resources, R.color.status_missing, null)
        val colorRemoved = ResourcesCompat.getColor(resources, R.color.status_removed, null)

        val colors: ArrayList<Int> = ArrayList()
        colors.add(colorDefault)
        colors.add(colorOnInventory)
        colors.add(colorRemoved)
        colors.add(colorMissing)
        //endregion Icon colors

        for (i in allStatus.indices) {
            val icon = ContextCompat.getDrawable(this, R.drawable.ic_lens)
            icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    colors[i], BlendModeCompat.SRC_IN
                )

            val item = menu[i + menuItems]
            item.icon = icon

            // Keep the popup menu open
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            item.actionView = View(this)
            item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return false
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    return false
                }
            })
        }

        return true
    }

    private fun isBackPressed() {
        isFinishingByUser = true

        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                return true
            }

            R.id.action_rfid_connect -> {
                ScannerManager.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_trigger_scan -> {
                ScannerManager.trigger(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_read_barcode -> {
                ScannerManager.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return statusItemSelected(item)
            }
        }
    }

    private fun statusItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (filterFragment == null) {
            return false
        }

        val visibleStatus = visibleStatus
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetStatus.onInventory.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.onInventory)) {
                    adapter?.addVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.add(AssetStatus.onInventory)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.onInventory)) {
                    adapter?.removeVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.remove(AssetStatus.onInventory)
                }
            }

            AssetStatus.missing.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.missing)) {
                    adapter?.addVisibleStatus(AssetStatus.missing)
                    visibleStatus.add(AssetStatus.missing)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.missing)) {
                    adapter?.removeVisibleStatus(AssetStatus.missing)
                    visibleStatus.remove(AssetStatus.missing)
                }
            }

            AssetStatus.removed.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.removed)) {
                    adapter?.addVisibleStatus(AssetStatus.removed)
                    visibleStatus.add(AssetStatus.removed)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.removed)) {
                    adapter?.removeVisibleStatus(AssetStatus.removed)
                    visibleStatus.remove(AssetStatus.removed)
                }
            }

            AssetStatus.unknown.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.unknown)) {
                    adapter?.addVisibleStatus(AssetStatus.unknown)
                    visibleStatus.add(AssetStatus.unknown)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.unknown)) {
                    adapter?.removeVisibleStatus(AssetStatus.unknown)
                    visibleStatus.remove(AssetStatus.unknown)
                }
            }

            menuItemShowImages -> {
                adapter?.showImages(item.isChecked)
                if (item.isChecked)
                    item.icon = ContextCompat.getDrawable(context, R.drawable.ic_photo_library)
                else
                    item.icon = ContextCompat.getDrawable(context, R.drawable.ic_hide_image)
                item.icon?.mutate()?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                    )
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

        filterFragment?.visibleStatusArray = visibleStatus

        return true
    }

    override fun onFilterChanged(
        code: String,
        itemCategory: ItemCategory?,
        warehouseArea: WarehouseArea?,
        onlyActive: Boolean,
    ) {
        handlePanelState(PanelType.BOTTOM, PanelState.COLLAPSED)

        if (code.isEmpty() && itemCategory == null && warehouseArea == null) {
            // Limpiar el control
            adapter?.clear()
            return
        }

        viewModel.applyFilters(
            code = code,
            category = itemCategory,
            warehouseArea = warehouseArea,
            onlyActive = onlyActive,
        )
    }

    override fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String, obs: String, reference: String) {
        if (!svm.useImageControl) return

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, ImageControlCameraActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(ImageControlCameraActivity.ARG_PROGRAM_OBJECT_ID, tableId.toLong())
            intent.putExtra(ImageControlCameraActivity.ARG_OBJECT_ID_1, itemId.toString())
            intent.putExtra(ImageControlCameraActivity.ARG_DESCRIPTION, description)
            intent.putExtra(ImageControlCameraActivity.ARG_OBS, obs)
            intent.putExtra(ImageControlCameraActivity.ARG_REFERENCE, reference)
            intent.putExtra(ImageControlCameraActivity.ARG_ADD_PHOTO, autoSend())
            resultForPhotoCapture.launch(intent)
        }
    }

    private val resultForPhotoCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            try {
                if (it.resultCode == RESULT_OK && data != null) {
                    val asset = currentAsset ?: return@registerForActivityResult
                    asset.saveChanges()
                    val pos = adapter?.getIndexById(asset.id) ?: RecyclerView.NO_POSITION
                    adapter?.notifyItemChanged(pos)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    override fun onAlbumViewRequired(tableId: Int, itemId: Long, filename: String) {
        if (!svm.useImageControl) return

        if (rejectNewInstances) return
        rejectNewInstances = true

        tempObjectId = itemId.toString()
        tempTableId = tableId

        val programData = ProgramData(
            programObjectId = tempTableId.toLong(),
            objId1 = tempObjectId
        )

        ImageCoroutines().get(context = context, programData = programData) {
            val allLocal = toDocumentContentList(images = it, programData = programData)
            if (allLocal.isEmpty()) {
                getFromWebservice()
            } else {
                showPhotoAlbum(allLocal)
            }
        }
    }

    private fun getFromWebservice() {
        WsFunction().documentContentGetBy12(
            programObjectId = tempTableId,
            objectId1 = tempObjectId
        ) { it2 ->
            if (it2 != null) fillResults(it2)
            else {
                showMessage(getString(R.string.no_images), SnackBarType.INFO)
                rejectNewInstances = false
            }
        }
    }

    private fun showPhotoAlbum(images: ArrayList<DocumentContent> = ArrayList()) {
        val intent = Intent(this, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(ImageControlGridActivity.ARG_PROGRAM_OBJECT_ID, tempTableId.toLong())
        intent.putExtra(ImageControlGridActivity.ARG_OBJECT_ID_1, tempObjectId)
        intent.putExtra(ImageControlGridActivity.ARG_DOC_CONT_OBJ_ARRAY_LIST, images)
        resultForShowPhotoAlbum.launch(intent)
    }

    private val resultForShowPhotoAlbum =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            rejectNewInstances = false
        }

    private var tempObjectId = ""
    private var tempTableId = 0

    private fun fillResults(docContReqResObj: DocumentContentRequestResult) {
        if (docContReqResObj.documentContentArray.isEmpty()) {
            showMessage(getString(R.string.no_images), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        val anyAvailable = docContReqResObj.documentContentArray.any { it.available }

        if (!anyAvailable) {
            showMessage(getString(R.string.images_not_yet_processed), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        showPhotoAlbum()
    }

    override fun onFilterChanged(printer: String, template: BarcodeLabelCustom?, qty: Int?) {
        val templateId = template?.id ?: return
        viewModel.updateState { it.copy(printQty = qty ?: 1, templateId = templateId) }
        printerFragment?.saveSharedPreferences()
    }

    override fun onPrintRequested(printer: String, template: BarcodeLabelCustom, qty: Int) {
        /** Acá seleccionamos siguiendo estos criterios:
         *
         * Si NO es multiSelect tomamos el ítem seleccionado de forma simple.
         *
         * Si es multiSelect nos fijamos que o bien estén marcados algunos ítems o
         * bien tengamos un ítem seleccionado de forma simple.
         *
         * Si es así, vamos a devolver los ítems marcados si existen como prioridad.
         *
         * Si no, nos fijamos que NO sean visibles los CheckBoxes, esto quiere
         * decir que el usuario está seleccionado el ítem de forma simple y
         * devolvemos este ítem.
         *
         **/

        val asset = currentAsset
        val countChecked = countChecked
        var assetList: List<Asset> = listOf()

        if (!viewModel.multiSelect && asset != null) {
            assetList = arrayListOf(asset)
        } else if (viewModel.multiSelect) {
            if (countChecked > 0 || asset != null) {
                if (countChecked > 0) assetList = allChecked
                else if (adapter?.showCheckBoxes == false) {
                    assetList = arrayListOf(asset!!)
                }
            }
        }

        if (assetList.isNotEmpty())
            printerFragment?.printAssetById(assetList.map { it.id })
    }

    private var qtyTextViewFocused: Boolean = false
    override fun onQtyTextViewFocusChanged(hasFocus: Boolean) {
        qtyTextViewFocused = hasFocus
        if (!hasFocus && binding.searchEditText.isFocused) {
            handlePanelState(PanelType.TOP, PanelState.COLLAPSED)
        }
    }

    // region READERS Reception
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onStateChanged(state: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (sr.prefsGetBoolean(Preference.rfidShowConnectedMessage)) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    showMessage(
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    showMessage(
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    showMessage(
                        getString(R.string.there_is_no_rfid_device_connected),
                        SnackBarType.INFO
                    )
                }
            }
        }
    }

    override fun onReadCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        scannerCompleted(scanCode)
    }
    //endregion READERS Reception

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        val asset = currentAsset

        if (!rejectNewInstances && asset != null) {
            rejectNewInstances = true

            val intent = Intent(baseContext, AssetCRUDActivity::class.java)
            intent.putExtra("asset", asset)
            intent.putExtra("return_on_success", true)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            resultForEditAsset.launch(intent)
        }
    }

    private val resultForEditAsset =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            try {
                if (it.resultCode == RESULT_OK && data != null) {
                    val a = Parcels.unwrap<Asset>(data.parcelable("asset"))
                        ?: return@registerForActivityResult
                    adapter?.updateAsset(a, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }
}