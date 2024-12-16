package com.dacosys.assetControl.ui.activities.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.async.location.GetLocationAsync
import com.dacosys.assetControl.data.enums.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.repository.barcode.BarcodeLabelCustomRepository
import com.dacosys.assetControl.data.room.repository.location.TempWarehouseAreaRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.databinding.WarehouseAreaPrintLabelActivityTopPanelCollapsedBinding
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.adapters.location.WarehouseAreaAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.isKeyboardVisible
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.ui.fragments.location.WarehouseAreaSelectFilterFragment
import com.dacosys.assetControl.ui.fragments.print.PrinterFragment
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetLong
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import kotlin.concurrent.thread

class WarehouseAreaPrintLabelActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    Scanner.ScannerListener, KeyboardVisibilityEventListener, Rfid.RfidDeviceListener,
    WarehouseAreaSelectFilterFragment.FragmentListener, GetLocationAsync.GetLocationAsyncListener,
    PrinterFragment.FragmentListener, WarehouseAreaAdapter.DataSetChangedListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private var isFinishingByUser = false
    private fun destroyLocals() {
        // Borramos los Ids temporales que se usaron en la actividad.
        if (isFinishingByUser) TempWarehouseAreaRepository().deleteAll()

        adapter?.refreshListeners()
        warehouseAreaSelectFilterFragment?.onDestroy()
        printerFragment?.onDestroy()
    }
    // endregion

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshWarehouseArea.isRefreshing = false
            }
        }, 1000)
    }

    private var tempTitle = ""
    private var rejectNewInstances = false

    private var multiSelect = false
    private var adapter: WarehouseAreaAdapter? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var lastSelected: WarehouseArea? = null
    private var firstVisiblePos: Int? = null
    private var panelBottomIsExpanded = true
    private var panelTopIsExpanded = false

    // A esta actividad se le puede pasar una lista de WarehouseAreas a seleccionar y ocultar el panel
    // que contiene los controles de filtrado para evitar que cambie la lista de WarehouseAreas o las opciones
    // de filtrado.
    private var fixedItemList = false
    private var hideFilterPanel = false
    private var completeList: ArrayList<WarehouseArea> = ArrayList()
    private var warehouseAreaSelectFilterFragment: WarehouseAreaSelectFilterFragment? = null
    private var printerFragment: PrinterFragment? = null

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("multiSelect", multiSelect)
        b.putBoolean("hideFilterPanel", hideFilterPanel)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)
        b.putBoolean("fixedItemList", fixedItemList)

        if (adapter != null) {
            b.putParcelable("lastSelected", adapter?.currentWarehouseArea())
            b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: 0)
            b.putLongArray("checkedIdArray", adapter?.getAllChecked()?.toLongArray())
        }

        // Guardar en la DB temporalmente los ítems listados
        if (fixedItemList) TempWarehouseAreaRepository().insert(
            adapter?.getAllId() ?: ArrayList()
        )
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title") ?: ""
        tempTitle = t1.ifEmpty { getString(R.string.select_warehouse_area) }
        // endregion

        // PANELS
        if (b.containsKey("hideFilterPanel")) hideFilterPanel =
            b.getBoolean("hideFilterPanel", hideFilterPanel)
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded")) panelTopIsExpanded =
            b.getBoolean("panelTopIsExpanded")

        // ADAPTER
        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        fixedItemList = b.getBoolean("fixedItemList")
        // Cargar la lista desde la DB local
        if (fixedItemList) completeList = ArrayList(WarehouseAreaRepository().selectByTempIds())
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_warehouse_area)
        val id = prefsGetLong(Preference.defaultBarcodeLabelCustomWa)
        val blc = BarcodeLabelCustomRepository().selectById(id)
        if (blc != null) printerFragment?.barcodeLabelCustom = blc
    }


    private lateinit var binding: WarehouseAreaPrintLabelActivityTopPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseAreaPrintLabelActivityTopPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        KeyboardVisibilityEvent.registerEventListener(this, this)

        warehouseAreaSelectFilterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as WarehouseAreaSelectFilterFragment
        printerFragment =
            supportFragmentManager.findFragmentById(binding.printFragment.id) as PrinterFragment

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            // Borramos los Ids temporales al crear la actividad por primera vez.
            TempWarehouseAreaRepository().deleteAll()

            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        warehouseAreaSelectFilterFragment?.setListener(this)

        printerFragment?.setListener(this)
        printerFragment?.barcodeLabelTarget = BarcodeLabelTarget.WarehouseArea

        binding.swipeRefreshWarehouseArea.setOnRefreshListener(this)
        binding.swipeRefreshWarehouseArea.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.okButton.setOnClickListener { itemSelect() }

        // OCULTAR PANEL DE CONTROLES DE FILTRADO
        if (hideFilterPanel) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (panelBottomIsExpanded) {
                    binding.expandBottomPanelButton?.performClick()
                }

                runOnUiThread {
                    binding.expandBottomPanelButton?.visibility = GONE
                }
            }
        }

        setPanels()

        thread { fillAdapter(completeList) }

        setupUI(binding.root, this)
    }

    private fun itemSelect() {
        closeKeyboard(this)
        val data = Intent()

        if (adapter != null) {
            val warehouseArea = adapter?.currentWarehouseArea()
            val warehouseAreaIdArray = adapter?.getAllChecked()

            if (!multiSelect && warehouseArea != null) {
                data.putParcelableArrayListExtra(
                    "ids", arrayListOf(ParcelLong(warehouseArea.id))
                )
                setResult(RESULT_OK, data)
            } else if (multiSelect && warehouseAreaIdArray != null && warehouseAreaIdArray.isNotEmpty()) {
                val parcelIdArray: ArrayList<ParcelLong> = ArrayList()
                for (it in warehouseAreaIdArray) parcelIdArray.add(ParcelLong(it))
                data.putParcelableArrayListExtra("ids", parcelIdArray)
                setResult(RESULT_OK, data)
            } else {
                setResult(RESULT_CANCELED)
            }
        } else {
            setResult(RESULT_CANCELED)
        }

        isFinishingByUser = true
        finish()
    }

    private fun setPanels() {
        val currentLayout = ConstraintSet()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) {
                    currentLayout.load(this, R.layout.warehouse_area_print_label_activity)
                } else {
                    currentLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_top_panel_collapsed
                    )
                }
            } else {
                if (panelTopIsExpanded) {
                    currentLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_bottom_panel_collapsed
                    )
                } else {
                    currentLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_both_panels_collapsed
                    )
                }
            }
        } else {
            if (panelTopIsExpanded) {
                currentLayout.load(this, R.layout.warehouse_area_print_label_activity)
            } else {
                currentLayout.load(
                    this, R.layout.warehouse_area_print_label_activity_top_panel_collapsed
                )
            }
        }

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                refreshTextViews()
            }

            override fun onTransitionCancel(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(
            binding.warehouseAreaPrintLabel, transition
        )

        currentLayout.applyTo(binding.warehouseAreaPrintLabel)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
                }

                else -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.search_options)
                }
            }
        }

        when {
            panelTopIsExpanded -> {
                binding.expandTopPanelButton.text = getString(R.string.collapse_panel)
            }

            else -> {
                binding.expandTopPanelButton.text = getString(R.string.label_print)
            }
        }
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_bottom_panel_collapsed
                    )
                } else {
                    nextLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_both_panels_collapsed
                    )
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.warehouse_area_print_label_activity)
                } else {
                    nextLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_top_panel_collapsed
                    )
                }
            }

            panelBottomIsExpanded = !panelBottomIsExpanded
            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    refreshTextViews()
                }

                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.warehouseAreaPrintLabel, transition
            )

            nextLayout.applyTo(binding.warehouseAreaPrintLabel)

            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
                }

                else -> {
                    binding.expandBottomPanelButton?.text = getString(R.string.search_options)
                }
            }
        }
    }

    private fun setTopPanelAnimation() {
        binding.expandTopPanelButton.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (panelBottomIsExpanded) {
                    if (panelTopIsExpanded) {
                        nextLayout.load(
                            this, R.layout.warehouse_area_print_label_activity_top_panel_collapsed
                        )
                    } else {
                        nextLayout.load(this, R.layout.warehouse_area_print_label_activity)
                    }
                } else {
                    if (panelTopIsExpanded) {
                        nextLayout.load(
                            this, R.layout.warehouse_area_print_label_activity_both_panels_collapsed
                        )
                    } else {
                        nextLayout.load(
                            this,
                            R.layout.warehouse_area_print_label_activity_bottom_panel_collapsed
                        )
                    }
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this, R.layout.warehouse_area_print_label_activity_top_panel_collapsed
                    )
                } else {
                    nextLayout.load(this, R.layout.warehouse_area_print_label_activity)
                }
            }

            panelTopIsExpanded = !panelTopIsExpanded

            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    refreshTextViews()
                }

                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.warehouseAreaPrintLabel, transition
            )

            nextLayout.applyTo(binding.warehouseAreaPrintLabel)

            when {
                panelTopIsExpanded -> {
                    binding.expandTopPanelButton.text = getString(R.string.collapse_panel)
                }

                else -> {
                    binding.expandTopPanelButton.text = getString(R.string.label_print)
                }
            }
        }
    }

    private fun refreshTextViews() {
        runOnUiThread {
            printerFragment?.refreshViews()
            warehouseAreaSelectFilterFragment?.refreshViews()
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWarehouseArea.isRefreshing = show
        }
    }

    private fun fillListView() {
        thread {
            val sync = GetLocationAsync()
            sync.addParams(this)
            sync.addExtraParams(
                waDescription = warehouseAreaSelectFilterFragment?.waDescription ?: "",
                wDescription = warehouseAreaSelectFilterFragment?.wDescription ?: "",
                onlyActive = warehouseAreaSelectFilterFragment?.onlyActive != false
            )
            sync.execute()
        }
    }

    private fun fillAdapter(warehouseAreaArray: ArrayList<WarehouseArea>?) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (adapter != null) {
                    lastSelected = adapter?.currentWarehouseArea()
                    firstVisiblePos = adapter?.firstVisiblePos()
                }

                if (adapter == null || warehouseAreaArray != null) {
                    adapter = WarehouseAreaAdapter(
                        activity = this,
                        resource = R.layout.warehouse_area_row,
                        warehouseAreas = warehouseAreaArray ?: ArrayList(),
                        listView = binding.warehouseAreaListView,
                        checkedIdArray = checkedIdArray,
                        multiSelect = multiSelect,
                    )
                }

                adapter?.refreshListeners(dataSetChangedListener = this)
                adapter?.refresh()

                while (binding.warehouseAreaListView.adapter == null) {
                    // Horrible wait for full load
                }

                adapter?.setSelectItemAndScrollPos(lastSelected, firstVisiblePos)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    public override fun onResume() {
        super.onResume()
        rejectNewInstances = false
        closeKeyboard(this)

        refreshTextViews()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            JotterListener.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        JotterListener.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scanCode.trim().isEmpty()) {
                val res = getString(R.string.invalid_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                ErrorLog.writeLog(this, this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                code = scanCode,
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                searchAssetEan = false
            )

            val warehouseArea = if (sc.warehouseArea != null) {
                sc.warehouseArea
            } else {
                val res = this.getString(R.string.invalid_warehouse_area_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                null
            }

            if (warehouseArea != null) {
                adapter?.selectItem(warehouseArea)
                printerFragment?.printWa(arrayListOf(warehouseArea))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    private fun isBackPressed() {
        isFinishingByUser = true

        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
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

        return true
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
                JotterListener.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_trigger_scan -> {
                JotterListener.trigger(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_read_barcode -> {
                JotterListener.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onGetLocationProgress(
        msg: String,
        progressStatus: ProgressStatus,
        completeList: ArrayList<WarehouseArea>,
    ) {
        when (progressStatus) {
            ProgressStatus.starting -> {
                showProgressBar(true)
            }

            ProgressStatus.canceled -> {
                showProgressBar(false)
            }

            ProgressStatus.crashed -> {
                showProgressBar(false)
                makeText(binding.root, msg, SnackBarType.ERROR)
            }

            ProgressStatus.finished -> {
                showProgressBar(false)
                fillAdapter(completeList)
            }
        }
    }

    override fun onFilterChanged(waDescription: String, wDescription: String, onlyActive: Boolean) {
        closeKeyboard(this)

        if (fixedItemList) return

        if (waDescription.isEmpty() && wDescription.isEmpty()) {
            // Limpiar el control
            completeList.clear()
            adapter?.clear()
            return
        }

        fillListView()
    }

    override fun onFilterChanged(printer: String, template: BarcodeLabelCustom?, qty: Int?) {}
    override fun onPrintRequested(printer: String, template: BarcodeLabelCustom, qty: Int) {
        printerFragment?.printWaById(adapter?.getAllChecked() ?: ArrayList())
    }

    private var qtyTextViewFocused: Boolean = false
    override fun onQtyTextViewFocusChanged(hasFocus: Boolean) {
        qtyTextViewFocused = hasFocus

        // Si el control de ingreso de cantidades del fragmento de impresión pierde el foco, pero
        // el foco pasa al control de texto de búsqueda, se debe colapsar el panel de impresión
        // manualmente porque no se dispara el evento de cambio de visibilidad del teclado en
        // pantalla que lo haría normalmente.
        if (isKeyboardVisible() && !hasFocus) collapseTopPanel()
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
        if (prefsGetBoolean(Preference.rfidShowConnectedMessage)) {
            when (Rfid.vh75State) {
                Vh75Bt.STATE_CONNECTED -> {
                    makeText(
                        binding.root,
                        getString(R.string.rfid_connected),
                        SnackBarType.SUCCESS
                    )
                }

                Vh75Bt.STATE_CONNECTING -> {
                    makeText(
                        binding.root,
                        getString(R.string.searching_rfid_reader),
                        SnackBarType.RUNNING
                    )
                }

                else -> {
                    makeText(
                        binding.root,
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

    override fun onVisibilityChanged(isOpen: Boolean) {
        if (isOpen) {
            when {
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && !qtyTextViewFocused -> {
                    collapseBottomPanel()
                    collapseTopPanel()
                }

                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT && qtyTextViewFocused -> {
                    collapseBottomPanel()
                }

                resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT && !qtyTextViewFocused -> {
                    collapseTopPanel()
                }
            }
        }
    }

    private fun collapseBottomPanel() {
        if (panelBottomIsExpanded && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            runOnUiThread {
                binding.expandBottomPanelButton?.performClick()
            }
        }
    }

    private fun collapseTopPanel() {
        if (panelTopIsExpanded) {
            runOnUiThread {
                binding.expandTopPanelButton.performClick()
            }
        }
    }

    override fun onDataSetChanged() {
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = (adapter?.itemCount ?: 0) == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.warehouseAreaListView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }
}