package com.dacosys.assetControl.views.movements.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.AssetControl.Companion.getContext
import com.dacosys.assetControl.utils.Statics.Companion.isRfidRequired
import com.dacosys.assetControl.databinding.ProgressBarDialogBinding
import com.dacosys.assetControl.databinding.WarehouseMovementContentBottomPanelCollapsedBinding
import com.dacosys.assetControl.utils.UTCDataTime.Companion.getUTCDateTimeAsString
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scannedCode.ScannedCode
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.confirmStatus.ConfirmStatus
import com.dacosys.assetControl.model.confirmStatus.ConfirmStatus.CREATOR.cancel
import com.dacosys.assetControl.model.confirmStatus.ConfirmStatus.CREATOR.confirm
import com.dacosys.assetControl.model.confirmStatus.ConfirmStatus.CREATOR.modify
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.movements.async.SaveMovement
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentAdapter
import com.dacosys.assetControl.model.movements.warehouseMovementContentStatus.WarehouseMovementContentStatus
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.views.assets.asset.activities.AssetCRUDActivity
import com.dacosys.assetControl.views.assets.asset.activities.AssetDetailActivity
import com.dacosys.assetControl.views.assets.asset.activities.AssetPrintLabelActivity
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.movements.fragments.LocationHeaderFragment
import com.dacosys.imageControl.`object`.Images
import com.dacosys.imageControl.`object`.StatusObject
import com.dacosys.imageControl.activities.ImageControlCameraActivity
import com.dacosys.imageControl.activities.ImageControlGridActivity
import com.dacosys.imageControl.dbHelper.DbCommands
import com.dacosys.imageControl.main.DownloadTask
import com.dacosys.imageControl.main.GetImagesTask
import com.dacosys.imageControl.wsObject.DocumentContentObject
import com.dacosys.imageControl.wsObject.DocumentContentRequestResultObject
import org.parceler.Parcels
import kotlin.concurrent.thread

class WarehouseMovementContentActivity :
    AppCompatActivity(),
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener,
    SwipeRefreshLayout.OnRefreshListener,
    LocationHeaderFragment.LocationChangedListener,
    SaveMovement.SaveMovementListener,
    WarehouseMovementContentAdapter.CheckedChangedListener,
    WarehouseMovementContentAdapter.DataSetChangedListener,
    AssetAdapter.Companion.EditAssetRequiredListener,
    AssetAdapter.Companion.AddPhotoRequiredListener,
    AssetAdapter.Companion.AlbumViewRequiredListener,
    GetImagesTask.GetImagesTask,
    DownloadTask.DownloadTaskListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        wmContAdapter?.refreshListeners(null, null, null)
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onLocationChanged(warehouse: Warehouse, warehouseArea: WarehouseArea) {
        if (wmContAdapter == null) return

        for (wmCont in wmContAdapter?.getAll() ?: ArrayList()) {
            if (wmCont.warehouseAreaId == warehouseArea.warehouseAreaId) {
                wmCont.contentStatusId = WarehouseMovementContentStatus.noNeedToMove.id
            } else {
                wmCont.contentStatusId = WarehouseMovementContentStatus.toMove.id
            }
        }

        fillAdapter()
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshWmCont.isRefreshing = false
            }
        }, 1000)
    }

    private var tempTitle = ""

    private var wmContAdapter: WarehouseMovementContentAdapter? = null
    private var wmContArray: ArrayList<WarehouseMovementContent> = ArrayList()
    private var currentInventory: ArrayList<String>? = null

    private var lastSelected: WarehouseMovementContent? = null
    private var firstVisiblePos: Int? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()

    private var collectorContentId: Long = 0
    private var obs = ""

    private var allowClicks = true
    private var rejectNewInstances = false

    private var tempWarehouseArea: WarehouseArea? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, motionEvent ->
                Statics.closeKeyboard(this)
                if (view is Button && view !is Switch && view !is CheckBox) {
                    touchButton(motionEvent, view)
                    true
                } else {
                    false
                }
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .forEach { setupUI(it) }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putStringArrayList("currentInventory", currentInventory)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (wmContAdapter != null) {
            b.putLongArray("checkedIdArray", wmContAdapter?.getAllChecked()?.toLongArray())
            b.putParcelableArrayList("wmContArray", wmContArray)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.assets_movement)
        // endregion

        tempWarehouseArea = Parcels.unwrap<WarehouseArea>(b.getParcelable("warehouseArea"))
        currentInventory = b.getStringArrayList("currentInventory")

        // Panels
        if (b.containsKey("panelBottomIsExpanded"))
            panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded"))
            panelTopIsExpanded = b.getBoolean("panelTopIsExpanded")

        // ADAPTER
        wmContArray.clear()
        val tempCont = b.getParcelableArrayList<WarehouseMovementContent>("wmContArray")
        if (tempCont != null) wmContArray = tempCont

        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.assets_movement)
    }

    private lateinit var binding: WarehouseMovementContentBottomPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = WarehouseMovementContentBottomPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        headerFragment =
            supportFragmentManager.findFragmentById(binding.headerFragment.id) as LocationHeaderFragment?



        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        binding.swipeRefreshWmCont.setOnRefreshListener(this)
        binding.swipeRefreshWmCont.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.okButton.setOnClickListener {
            if (allowClicks) {
                if ((wmContAdapter?.assetsToMove ?: 0) <= 0 &&
                    (wmContAdapter?.assetsFounded(
                        headerFragment?.warehouseArea?.warehouseAreaId ?: 0
                    ) ?: 0) <= 0
                ) {
                    makeText(
                        binding.root,
                        getContext().getString(R.string.you_must_add_at_least_one_asset),
                        SnackbarType.ERROR
                    )
                } else {
                    allowClicks = false
                    finishWarehouseMovement()
                }
            }
        }

        binding.addButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                addAsset()
            }
        }

        binding.removeButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                removeAsset()
            }
        }

        binding.detailButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                detailAsset()
            }
        }

        setHeaderTextBox()

        // Llenar la grilla
        setPanels()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun setHeaderTextBox() {
        headerFragment?.setChangeLocationListener(this)
        headerFragment?.showChangePostButton(true)
        headerFragment?.setTitle(getContext().getString(R.string.destination))

        if (tempWarehouseArea != null && headerFragment != null) {
            runOnUiThread {
                headerFragment?.fill(
                    tempWarehouseArea ?: return@runOnUiThread
                )
            }
        }
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            if (panelTopIsExpanded) {
                currentLayout.load(this, R.layout.warehouse_movement_content_activity)
            } else {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_top_panel_collapsed
                )
            }
        } else {
            if (panelTopIsExpanded) {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_bottom_panel_collapsed
                )
            } else {
                currentLayout.load(
                    this,
                    R.layout.warehouse_movement_content_both_panels_collapsed
                )
            }
        }

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(
            binding.warehouseMovementContent,
            transition
        )

        currentLayout.applyTo(binding.warehouseMovementContent)

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text =
                    getContext().getString(R.string.collapse_panel)
            }
            else -> {
                binding.expandBottomPanelButton?.text = getString(R.string.more_options)
            }
        }

        when {
            panelTopIsExpanded -> {
                binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
            }
            else -> {
                binding.expandTopPanelButton?.text =
                    getContext().getString(R.string.select_destination)
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
                        this,
                        R.layout.warehouse_movement_content_bottom_panel_collapsed
                    )
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_both_panels_collapsed
                    )
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.warehouse_movement_content_activity)
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_top_panel_collapsed
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
                override fun onTransitionEnd(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.warehouseMovementContent,
                transition
            )

            nextLayout.applyTo(binding.warehouseMovementContent)

            when {
                panelBottomIsExpanded -> {
                    binding.expandBottomPanelButton?.text =
                        getContext().getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandBottomPanelButton?.text =
                        getContext().getString(R.string.more_options)
                }
            }
        }
    }

    private fun setTopPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandTopPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_top_panel_collapsed
                    )
                } else {
                    nextLayout.load(this, R.layout.warehouse_movement_content_activity)
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_both_panels_collapsed
                    )
                } else {
                    nextLayout.load(
                        this,
                        R.layout.warehouse_movement_content_bottom_panel_collapsed
                    )
                }
            }

            panelTopIsExpanded = !panelTopIsExpanded

            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
            })

            TransitionManager.beginDelayedTransition(
                binding.warehouseMovementContent,
                transition
            )

            nextLayout.applyTo(binding.warehouseMovementContent)

            when {
                panelTopIsExpanded -> {
                    binding.expandTopPanelButton?.text =
                        getContext().getString(R.string.collapse_panel)
                }
                else -> {
                    binding.expandTopPanelButton?.text =
                        getContext().getString(R.string.select_destination)
                }
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWmCont.isRefreshing = show
        }
    }

    private fun fillAdapter(items: ArrayList<WarehouseMovementContent>? = null) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (wmContAdapter != null) {
                    lastSelected = wmContAdapter?.currentWmCont()
                    firstVisiblePos = wmContAdapter?.firstVisiblePos()
                }

                if (items != null) {
                    wmContAdapter = WarehouseMovementContentAdapter(
                        activity = this,
                        resource = R.layout.asset_row,
                        wmContArray = items,
                        suggestedList = items,
                        listView = binding.wmContentListView,
                        multiSelect = false,
                        checkedIdArray = checkedIdArray,
                        visibleStatus = WarehouseMovementContentStatus.getAll()
                    )
                }
                refreshAdapterListeners()

                while (binding.wmContentListView.adapter == null) {
                    // Horrible wait for full load
                }

                wmContAdapter?.selectItem(
                    wmc = lastSelected,
                    scrollPos = firstVisiblePos ?: 0,
                    smoothScroll = true
                )

                setupTextView()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    private fun refreshAdapterListeners() {
        // IMPORTANTE:
        // Se deben actualizar los listeners, sino
        // las variables de esta actividad pueden
        // tener valores antiguos en del adaptador.

        wmContAdapter?.refreshListeners(
            checkedChangedListener = this,
            dataSetChangedListener = this,
            editAssetRequiredListener = this
        )

        if (Statics.useImageControl) {
            wmContAdapter?.refreshImageControlListeners(
                this,
                this
            )
        }
    }

    private fun setupTextView() {
        val assetToMove = wmContAdapter?.assetsToMove ?: 0
        val tempText =
            if (assetToMove == 1) getString(R.string._asset) else getString(R.string._assets)

        runOnUiThread {
            binding.toMoveTextView.text =
                String.format("%s %s", assetToMove.toString(), tempText)
        }
    }

    private fun touchButton(motionEvent: MotionEvent, button: Button) {
        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> {
                button.isPressed = false
                button.performClick()
            }
            MotionEvent.ACTION_DOWN -> {
                button.isPressed = true
            }
        }
    }

    private fun cancelWarehouseMovement() {
        if (wmContAdapter == null || (wmContAdapter?.count() ?: 0) <= 0) {
            Statics.closeKeyboard(this)

            setResult(RESULT_CANCELED)
            finish()
        } else {
            JotterListener.pauseReaderDevices(this)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getContext().getString(R.string.cancel_movement))
                alert.setMessage(getContext().getString(R.string.discard_changes_and_return_to_the_main_menu_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    Statics.closeKeyboard(this)

                    setResult(RESULT_CANCELED)
                    finish()
                }

                alert.show()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.resumeReaderDevices(this)
                allowClicks = true
            }
        }
    }

    private fun detailAsset() {
        if (wmContAdapter == null || wmContAdapter?.currentWmCont() == null) {
            allowClicks = true
            return
        }

        val tempAsset = (wmContAdapter?.currentWmCont() ?: return).asset
        if (tempAsset != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, AssetDetailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("asset", tempAsset)
                startActivity(intent)
            }
        }

        allowClicks = true
    }

    private fun removeAsset() {
        if (wmContAdapter == null || wmContAdapter?.currentWmCont() == null) {
            allowClicks = true
            return
        }

        JotterListener.pauseReaderDevices(this)
        try {
            val wmCont = wmContAdapter?.currentWmCont() ?: return

            val adb = AlertDialog.Builder(this)
            adb.setTitle(R.string.remove_item)
            adb.setMessage(
                String.format(
                    getContext().getString(R.string.do_you_want_to_remove_the_item),
                    wmCont.code
                )
            )
            adb.setNegativeButton(R.string.cancel, null)
            adb.setPositiveButton(R.string.accept) { _, _ ->
                wmContAdapter?.remove(wmCont)
            }
            adb.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.resumeReaderDevices(this)
            allowClicks = true
        }
    }

    private fun addAsset() {
        if (!rejectNewInstances) {
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("multiSelect", true)
            resultForAssetSelect.launch(intent)
        }
    }

    private val resultForAssetSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val idParcel = data.getParcelableArrayListExtra<Statics.ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: java.util.ArrayList<Long?> = java.util.ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val codes: ArrayList<String> = ArrayList()
                    val aDb = AssetDbHelper()
                    for (id in ids) {
                        val a = aDb.selectById(id) ?: continue
                        codes.add(a.code)
                    }

                    try {
                        scannerHandleScanCompleted(codes, true)
                    } catch (ex: Exception) {
                        val res =
                            getContext().getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                        makeText(binding.root, res, SnackbarType.ERROR)
                        Log.d(this::class.java.simpleName, res)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                JotterListener.lockScanner(this, false)
                allowClicks = true
            }
        }

    private fun finishWarehouseMovement() {
        if (headerFragment == null || headerFragment?.warehouseArea == null) {
            makeText(
                binding.root,
                getContext().getString(R.string.you_must_select_a_destination_for_assets),
                SnackbarType.ERROR
            )

            allowClicks = true
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

            val intent =
                Intent(this, WarehouseMovementContentConfirmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra(
                "warehouseArea",
                Parcels.wrap<WarehouseArea>(headerFragment?.warehouseArea)
            )
            intent.putParcelableArrayListExtra(
                "wmContArray",
                wmContAdapter?.getToMove(
                    (headerFragment?.warehouseArea ?: return).warehouseAreaId
                )
            )
            resultForFinishMovement.launch(intent)
        }
    }

    private val resultForFinishMovement =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    when (Parcels.unwrap<ConfirmStatus>(data.getParcelableExtra("confirmStatus"))) {
                        modify -> obs = data.getStringExtra("obs") ?: ""
                        cancel -> cancelWarehouseMovement()
                        confirm -> {
                            obs = data.getStringExtra("obs") ?: ""
                            processWarehouseMovement()
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                JotterListener.lockScanner(this, false)
                allowClicks = true
            }
        }

    private fun processWarehouseMovement() {
        val destWaId = (headerFragment?.warehouseArea ?: return).warehouseAreaId
        val allWmc: ArrayList<WarehouseMovementContent> = ArrayList()

        // Procesar la lista, cambiar los estados si son incorrectos.
        for (wmc in wmContAdapter?.getAll() ?: ArrayList()) {
            if (wmc.warehouseAreaId == destWaId) {
                wmc.contentStatusId = WarehouseMovementContentStatus.noNeedToMove.id
            }
            allWmc.add(wmc)
        }

        JotterListener.lockScanner(this, true)
        thread {
            val saveMovement = SaveMovement()
            saveMovement.addParams(
                callback = this,
                destWarehouseAreaId = destWaId,
                obs = obs,
                allMovementContent = allWmc
            )
            saveMovement.execute()
        }
    }

    private fun scannerHandleScanCompleted(scannedCode: ArrayList<String>, manuallyAdded: Boolean) {
        JotterListener.lockScanner(this, true)

        try {
            checkCode(
                codesArray = scannedCode,
                manuallyAdded = manuallyAdded
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        fillAdapter(wmContArray)
    }

    override fun onBackPressed() {
        cancelWarehouseMovement()
    }

    override fun scannerCompleted(scanCode: String) {
        scannerHandleScanCompleted(arrayListOf(scanCode), false)
    }

    private fun checkCode(codesArray: ArrayList<String>, manuallyAdded: Boolean) {
        val allMovements: ArrayList<WarehouseMovementContent> = ArrayList()

        try {
            for (scannedCode in codesArray) {
                // Nada que hacer, volver
                if (scannedCode.isEmpty()) {
                    val res = getString(R.string.invalid_code)
                    makeText(binding.root, res, SnackbarType.ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                val sc = ScannedCode(this).getFromCode(
                    code = scannedCode,
                    searchWarehouseAreaId = true,
                    searchAssetCode = true,
                    searchAssetSerial = true,
                    validateId = true
                )

                if (sc.warehouseArea != null) {
                    if (headerFragment != null && (headerFragment
                            ?: return).warehouseArea != sc.warehouseArea
                    ) {
                        makeText(
                            binding.root,
                            getContext().getString(R.string.destination_changed),
                            SnackbarType.INFO
                        )
                        headerFragment?.fill(sc.warehouseArea ?: return)
                    }
                    break
                }

                if (sc.codeFound && sc.asset != null && sc.labelNbr == 0) {
                    val res = getString(R.string.report_code)
                    makeText(binding.root, res, SnackbarType.ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                if (sc.codeFound && (sc.asset != null && (sc.asset
                        ?: return).labelNumber == null)
                ) {
                    val res = getString(R.string.no_printed_label)
                    makeText(binding.root, res, SnackbarType.ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                if (sc.codeFound && (sc.asset != null
                            && ((sc.asset
                        ?: return).labelNumber != sc.labelNbr && sc.labelNbr != null)
                            && !manuallyAdded)
                ) {
                    val res = getString(R.string.invalid_code)
                    makeText(binding.root, res, SnackbarType.ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                var tempCode = scannedCode
                if (sc.asset != null) {
                    // Si ya se encontró un activo, utilizo su código real
                    // ya que el código escaneado puede contener caractéres especiales
                    // que no aparecen en la lista
                    tempCode = (sc.asset ?: return).code
                }

                if (wmContAdapter != null && !wmContAdapter!!.isEmpty) {
                    var alreadyRegistered = false

                    // Buscar primero en el adaptador de la lista
                    (0 until wmContAdapter!!.count)
                        .map { wmContAdapter!!.getItem(it) }
                        .filter {
                            it != null && it.code == tempCode
                        }.forEach {
                            val res =
                                "${(it ?: return@forEach).code} ${getString(R.string.already_registered)}"
                            makeText(binding.root, res, SnackbarType.INFO)
                            Log.d(this::class.java.simpleName, res)

                            alreadyRegistered = true
                        }

                    if (alreadyRegistered) {
                        continue
                    }
                }

                if (sc.asset == null) {
                    val res = getString(R.string.unknown_code)
                    makeText(binding.root, res, SnackbarType.ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                var contStatus = WarehouseMovementContentStatus.toMove
                if (sc.asset != null) {
                    /////////////////////////////////////////////////////////
                    // STATUS 2 = Si el activo está en el mismo área, dejamos que lo agregue.
                    if (headerFragment != null && (headerFragment
                            ?: return).warehouseArea != null
                    ) {
                        if ((sc.asset ?: return).warehouseAreaId == ((headerFragment
                                ?: return).warehouseArea
                                ?: return).warehouseAreaId
                        ) {
                            contStatus = WarehouseMovementContentStatus.noNeedToMove

                            val res = getString(R.string.is_already_in_the_area)
                            makeText(binding.root, res, SnackbarType.INFO)
                        }
                    }

                    collectorContentId--

                    val finalWmc = WarehouseMovementContent(
                        warehouseMovementId = 0,
                        warehouseMovementContentId = collectorContentId,
                        asset = sc.asset ?: return,
                        contentStatusId = contStatus.id
                    )

                    allMovements.add(finalWmc)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        }

        if (allMovements.size > 0) {
            runOnUiThread {
                wmContAdapter?.add(allMovements, true)
            }
        }

        setupTextView()
        JotterListener.lockScanner(this, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired()) {
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
                onBackPressed()
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

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    override fun onSaveMovementProgress(msg: String, taskStatus: Int, progress: Int?, total: Int?) {
        showProgressDialog(
            getContext().getString(R.string.saving_warehouse_movement),
            msg,
            taskStatus,
            progress,
            total
        )

        if (ProgressStatus.isFinishStatus(taskStatus)) {
            JotterListener.lockScanner(this, false)
        }

        if (taskStatus == ProgressStatus.finished.id) {
            Statics.closeKeyboard(this)

            makeText(
                binding.root,
                getContext().getString(R.string.movement_performed_correctly),
                SnackbarType.SUCCESS
            )

            setResult(RESULT_OK)
            finish()
        } else if (taskStatus == ProgressStatus.canceled.id ||
            taskStatus == ProgressStatus.crashed.id
        ) {
            makeText(binding.root, msg, SnackbarType.ERROR)
        }
    }

    // region ProgressBar
    // Aparece mientras se realizan operaciones sobre las bases de datos remota y local
    private var progressDialog: AlertDialog? = null
    private lateinit var alertBinding: ProgressBarDialogBinding
    private fun createProgressDialog() {
        alertBinding = ProgressBarDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        //builder.setCancelable(false) // if you want user to wait for some process to finish
        builder.setView(alertBinding.root)
        progressDialog = builder.create()
    }

    private fun showProgressDialog(
        title: String,
        msg: String,
        status: Int,
        progress: Int? = null,
        total: Int? = null,
    ) {
        if (isFinishing || isDestroyed) return

        runOnUiThread {
            if (progressDialog == null) {
                createProgressDialog()
            }

            val appColor =
                ResourcesCompat.getColor(getContext().resources, R.color.assetControl, null)

            when (status) {
                ProgressStatus.starting.id -> {
                    progressDialog?.setTitle(title)
                    //dialog?.setMessage(msg)
                    alertBinding.messageTextView.text = msg
                    alertBinding.progressBarHor.progress = 0
                    alertBinding.progressBarHor.max = 0
                    alertBinding.progressBarHor.visibility = View.GONE
                    alertBinding.progressTextView.visibility = View.GONE
                    alertBinding.progressBarHor.progressTintList =
                        ColorStateList.valueOf(appColor)
                    alertBinding.progressBar.visibility = View.VISIBLE
                    alertBinding.progressBar.progressTintList =
                        ColorStateList.valueOf(appColor)

                    progressDialog?.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        getContext().getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing) progressDialog?.show()
                }
                ProgressStatus.running.id -> {
                    //dialog?.setMessage(msg)
                    if (msg != "") alertBinding.messageTextView.text = msg
                    if (progress != null && total != null && total > 0) {
                        alertBinding.progressBarHor.max = total
                        alertBinding.progressBarHor.progress = progress
                        alertBinding.progressBarHor.isIndeterminate = false
                        val t = "$progress / $total"
                        alertBinding.progressTextView.text = t

                        if (alertBinding.progressBarHor.visibility == View.GONE) {
                            alertBinding.progressBarHor.visibility = View.VISIBLE
                            alertBinding.progressTextView.visibility = View.VISIBLE
                        }

                        if (alertBinding.progressBar.visibility == View.VISIBLE)
                            alertBinding.progressBar.visibility = View.GONE
                    } else {
                        alertBinding.progressBar.progress = 0
                        alertBinding.progressBar.max = 0
                        alertBinding.progressBar.isIndeterminate = true

                        if (alertBinding.progressBarHor.visibility == View.VISIBLE) {
                            alertBinding.progressBarHor.visibility = View.GONE
                            alertBinding.progressTextView.visibility = View.GONE
                        }
                        if (alertBinding.progressBar.visibility == View.GONE)
                            alertBinding.progressBar.visibility = View.VISIBLE
                    }

                    progressDialog?.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        getContext().getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing) progressDialog?.show()
                }
                ProgressStatus.finished.id, ProgressStatus.canceled.id, ProgressStatus.crashed.id -> {
                    progressDialog?.dismiss()
                    progressDialog = null
                }
            }
        }
    }
    // endregion


    // region READERS Reception

    override fun onNewIntent(intent: Intent) {
        /*
          This method gets called, when a new Intent gets associated with the current activity instance.
          Instead of creating a new activity, onNewIntent will be called. For more information have a look
          at the documentation.

          In our case this method gets called, when the user attaches a className to the device.
         */
        super.onNewIntent(intent)
        Nfc.nfcHandleIntent(intent, this)
    }

    override fun onGetBluetoothName(name: String) {}

    override fun onWriteCompleted(isOk: Boolean) {}

    override fun onReadCompleted(scanCode: String) {
        if (currentInventory == null)
            currentInventory = ArrayList()
        if (currentInventory?.contains(scanCode) == false)
            currentInventory?.add(scanCode)

        scannerHandleScanCompleted(
            scannedCode = arrayListOf(scanCode),
            manuallyAdded = false
        )
    }

    //endregion READERS Reception

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        // Selecciona la fila correcta, para que se actualice el currentWmCont
        val arc = wmContAdapter?.getItem(pos)
        if (arc != null) {
            if (isChecked) {
                runOnUiThread {
                    wmContAdapter?.updateContent(
                        wmc = arc,
                        wmcStatusId = WarehouseMovementContentStatus.toMove.id,
                        assetStatusId = AssetStatus.onInventory.id,
                        selectItem = false,
                        changeCheckedState = false
                    )
                }
                setupTextView()
            } else {
                removeFromAdapter(arc)
            }
        }
    }

    private fun removeFromAdapter(wmCont: WarehouseMovementContent) {
        // Cambiar la fila selecciona sólo cuando la anterior es eliminada de la lista
        if (wmContAdapter == null) return

        runOnUiThread {
            wmContAdapter?.remove(wmCont)
            setupTextView()
        }
    }

    override fun onDataSetChanged() {
        setupTextView()
    }

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        val asset = wmContAdapter?.currentAsset()

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
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val a = Parcels.unwrap<Asset>(data.getParcelableExtra("asset"))
                        ?: return@registerForActivityResult
                    wmContAdapter!!.updateAsset(a, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    // region ImageControl

    override fun onAlbumViewRequired(tableId: Int, itemId: Long) {
        if (!Statics.useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            tempObjectId = itemId.toString()
            tempTableId = tableId

            val localImages = DbCommands.selectByProgramObjectObj1Obj2(
                tempTableId.toString(),
                tempObjectId,
                ""
            )
            val allLocal = toDocumentContentObjectList(tempObjectId, localImages)
            if (allLocal.isNotEmpty()) {
                showPhotoAlbum(allLocal)
            } else {
                // Localmente no hay imágenes vamos a buscar en el servidor
                val getDocs = GetImagesTask()
                getDocs.addParams(
                    callBack = this,
                    callBack2 = this,
                    programId = Statics.INTERNAL_IMAGE_CONTROL_APP_ID,
                    programObjectId = tempTableId,
                    objId1 = tempObjectId,
                    objId2 = ""
                )
                getDocs.downloadFiles(false)
                getDocs.start()
            }
        }
    }

    private fun toDocumentContentObjectList(
        assetId: String,
        images: java.util.ArrayList<Images>,
    ): java.util.ArrayList<DocumentContentObject> {
        val list: java.util.ArrayList<DocumentContentObject> = java.util.ArrayList()
        for (i in images) {
            val x = DocumentContentObject()

            x.description = i.description
            x.reference = i.reference
            x.obs = i.obs
            x.filename_original = i.filenameOriginal
            x.status_object_id = StatusObject.Waiting.statusObjectId.toInt()
            x.status_str = StatusObject.Waiting.description
            x.status_date = getUTCDateTimeAsString()

            x.user_id = Statics.currentUser()?.userId ?: 0
            x.user_str = Statics.currentUser()?.name ?: ""

            x.program_id = Statics.INTERNAL_IMAGE_CONTROL_APP_ID
            x.program_object_id = tempTableId
            x.object_id_1 = assetId
            x.object_id_2 = "0"

            list.add(x)
        }
        return list
    }

    private fun showPhotoAlbum(images: java.util.ArrayList<DocumentContentObject> = java.util.ArrayList()) {
        val intent = Intent(this, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("programId", Statics.INTERNAL_IMAGE_CONTROL_APP_ID)
        intent.putExtra("programObjectId", tempTableId)
        intent.putExtra("objectId1", tempObjectId)
        intent.putExtra("docContObjArrayList", images)
        startActivity(intent)
    }

    private var tempObjectId = ""
    private var tempTableId = 0

    override fun onGetImagesTaskListener(
        status: com.dacosys.imageControl.misc.ProgressStatus,
        msg: String,
        docContReqResObj: DocumentContentRequestResultObject?,
    ) {
        if (status == com.dacosys.imageControl.misc.ProgressStatus.finished) {
            if (docContReqResObj == null) {
                makeText(binding.root, msg, SnackbarType.INFO)
                rejectNewInstances = false
                return
            }

            if (docContReqResObj.documentContentArray == null ||
                (docContReqResObj.documentContentArray ?: return).isEmpty()
            ) {
                makeText(binding.root, getString(R.string.no_images), SnackbarType.INFO)
                rejectNewInstances = false
                return
            }

            val anyAvailable =
                (docContReqResObj.documentContentArray ?: arrayOf()).any { it.available }

            if (!anyAvailable) {
                makeText(
                    binding.root,
                    getContext().getString(R.string.images_not_yet_processed),
                    SnackbarType.INFO
                )
                rejectNewInstances = false
                return
            }

            showPhotoAlbum()
        }
    }

    override fun onDownloadTaskResult(
        docContObj: DocumentContentObject?,
        destination: String,
        target: Int,
    ) {
    }

    override fun onDownloadProgressChanged(
        status: com.dacosys.imageControl.misc.ProgressStatus,
        msg: String,
        value: Int,
        total: Int,
    ) {
    }

    override fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String) {
        if (!Statics.useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, ImageControlCameraActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("programId", Statics.INTERNAL_IMAGE_CONTROL_APP_ID)
            intent.putExtra("programObjectId", tableId)
            intent.putExtra("objectId1", itemId.toString())
            intent.putExtra("objectId2", "")
            intent.putExtra("description", description)
            intent.putExtra("addPhoto", Statics.autoSend())
            resultForPhotoCapture.launch(intent)
        }
    }

    private val resultForPhotoCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    wmContAdapter?.currentAsset()?.saveChanges()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    // endregion IC
}