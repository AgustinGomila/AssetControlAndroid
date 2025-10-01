package com.example.assetControl.ui.activities.movement

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.imageControl.dto.DocumentContent
import com.dacosys.imageControl.dto.DocumentContentRequestResult
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.network.download.GetImages.Companion.toDocumentContentList
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.ui.activities.ImageControlCameraActivity
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.data.async.movement.SaveMovement
import com.example.assetControl.data.enums.common.ConfirmStatus
import com.example.assetControl.data.enums.common.ConfirmStatus.CREATOR.cancel
import com.example.assetControl.data.enums.common.ConfirmStatus.CREATOR.confirm
import com.example.assetControl.data.enums.common.ConfirmStatus.CREATOR.modify
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.movement.WarehouseMovementContentStatus
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.room.repository.movement.TempMovementContentRepository
import com.example.assetControl.databinding.ProgressBarDialogBinding
import com.example.assetControl.databinding.WarehouseMovementContentBottomPanelCollapsedBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.rfid.Rfid.Companion.isRfidRequired
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.Connection.Companion.autoSend
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.ui.activities.asset.AssetCRUDActivity
import com.example.assetControl.ui.activities.asset.AssetDetailActivity
import com.example.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.example.assetControl.ui.adapters.interfaces.Interfaces
import com.example.assetControl.ui.adapters.movement.WmcRecyclerAdapter
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.SUCCESS
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.ParcelLong
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.parcel.Parcelables.parcelableArrayList
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutBoolean
import com.example.assetControl.utils.settings.preferences.Repository.Companion.useImageControl
import com.example.assetControl.viewModel.review.SaveReviewViewModel
import com.example.assetControl.viewModel.sync.SyncViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.parceler.Parcels
import java.util.*
import kotlin.concurrent.thread
import com.dacosys.imageControl.network.common.ProgressStatus as IcProgressStatus

class WmcActivity : AppCompatActivity(), Scanner.ScannerListener,
    Rfid.RfidDeviceListener, SwipeRefreshLayout.OnRefreshListener,
    LocationHeaderFragment.LocationChangedListener,
    Interfaces.CheckedChangedListener,
    Interfaces.DataSetChangedListener,
    Interfaces.EditAssetRequiredListener,
    Interfaces.AddPhotoRequiredListener,
    Interfaces.AlbumViewRequiredListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private var isFinishingByUser = false
    private fun destroyLocals() {
        // Borramos los Ids temporales que se usaron en la actividad.
        if (isFinishingByUser) TempMovementContentRepository().deleteAll()

        adapter?.refreshListeners()
        adapter?.refreshImageControlListeners()
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onLocationChanged(warehouse: Warehouse, warehouseArea: WarehouseArea) {
        adapter?.locationChange(warehouseArea.id)
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshWmCont.isRefreshing = false
            }
        }, 1000)
    }

    private var tempTitle = ""

    private var _fillAdapter = false

    private var adapter: WmcRecyclerAdapter? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var lastSelected: WarehouseMovementContent? = null
    private var currentScrollPosition: Int = 0
    private var firstVisiblePos: Int? = null

    private var completeList: ArrayList<WarehouseMovementContent> = ArrayList()
    private var currentInventory: ArrayList<String>? = null

    private var tempId: Long = 0L
    private var obs = ""

    private var allowClicks = true
    private var rejectNewInstances = false

    private var tempWarehouseArea: WarehouseArea? = null
    private var headerFragment: LocationHeaderFragment? = null

    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

    private var multiSelect = false

    private val menuItemShowImages = 9999
    private var showImages
        get() = prefsGetBoolean(Preference.reviewContentShowImages)
        set(value) {
            prefsPutBoolean(Preference.reviewContentShowImages.key, value)
        }

    private var showCheckBoxes
        get() =
            if (!multiSelect) false
            else prefsGetBoolean(Preference.reviewContentShowCheckBoxes)
        set(value) {
            prefsPutBoolean(Preference.reviewContentShowCheckBoxes.key, value)
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveBundleValues(outState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        b.putLong("tempId", tempId)

        b.putStringArrayList("currentInventory", currentInventory)

        b.putParcelable("lastSelected", adapter?.currentItem())
        b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: RecyclerView.NO_POSITION)
        b.putLongArray("checkedIdArray", adapter?.checkedIdArray?.map { it }?.toLongArray())
        b.putInt("currentScrollPosition", currentScrollPosition)

        // Guardamos el movimiento en una tabla temporal.
        // Movimientos de miles de artículos no pueden pasarse en el intent.
        TempMovementContentRepository().insert(
            wmId = 1,
            contents = adapter?.fullList?.toList() ?: listOf()
        )
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title") ?: ""
        tempTitle = t1.ifEmpty { getString(R.string.assets_movement) }
        // endregion

        tempId = b.getLong("tempId")

        tempWarehouseArea = Parcels.unwrap<WarehouseArea>(b.parcelable("warehouseArea"))
        currentInventory = b.getStringArrayList("currentInventory")

        // Panels
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded")) panelTopIsExpanded = b.getBoolean("panelTopIsExpanded")

        // Adapter
        checkedIdArray = (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        currentScrollPosition = b.getInt("currentScrollPosition")

        // Cargamos el movimiento desde la tabla temporal
        completeList.clear()
        val tempCont = ArrayList(TempMovementContentRepository().selectByTempId(1))
        if (tempCont.any()) completeList = tempCont

        _fillAdapter = true
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.assets_movement)
    }

    private lateinit var binding: WarehouseMovementContentBottomPanelCollapsedBinding
    private val saveViewModel: SaveReviewViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = WarehouseMovementContentBottomPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        saveViewModel.saveProgress.observe(this) { if (it != null) onSaveProgress(it) }
        syncViewModel.syncUploadProgress.observe(this) { if (it != null) onSyncUploadProgress(it) }
        syncViewModel.uploadImagesProgress.observe(this) { if (it != null) onUploadImagesProgress(it) }

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

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                currentScrollPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            }
        })

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()
        setTopPanelAnimation()

        binding.okButton.setOnClickListener {
            if (allowClicks) {
                val assetToMove = adapter?.assetsToMove ?: 0
                val assetFounded = adapter?.assetsFounded(headerFragment?.warehouseArea?.id ?: 0) ?: 0

                if (assetToMove <= 0 && assetFounded <= 0) {
                    makeText(
                        binding.root,
                        context.getString(R.string.you_must_add_at_least_one_asset),
                        ERROR
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

        setPanels()

        setupUI(binding.root, this)
    }

    private fun setHeaderTextBox() {
        headerFragment?.setChangeLocationListener(this)
        headerFragment?.showChangePostButton(true)
        headerFragment?.setTitle(context.getString(R.string.destination))

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
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.warehouse_movement_content_activity)
            else currentLayout.load(this, R.layout.warehouse_movement_content_top_panel_collapsed)
        } else {
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.warehouse_movement_content_bottom_panel_collapsed)
            else currentLayout.load(this, R.layout.warehouse_movement_content_both_panels_collapsed)
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
            binding.warehouseMovementContent, transition
        )

        currentLayout.applyTo(binding.warehouseMovementContent)

        if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text =
            context.getString(R.string.collapse_panel)
        else binding.expandBottomPanelButton?.text = getString(R.string.more_options)

        if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandTopPanelButton?.text = context.getString(R.string.select_destination)
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.warehouse_movement_content_bottom_panel_collapsed
                )
                else nextLayout.load(this, R.layout.warehouse_movement_content_both_panels_collapsed)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.warehouse_movement_content_activity)
                else nextLayout.load(this, R.layout.warehouse_movement_content_top_panel_collapsed)
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
                binding.warehouseMovementContent, transition
            )

            nextLayout.applyTo(binding.warehouseMovementContent)

            if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text =
                context.getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text = context.getString(R.string.more_options)
        }
    }

    private fun setTopPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandTopPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.warehouse_movement_content_top_panel_collapsed)
                else nextLayout.load(this, R.layout.warehouse_movement_content_activity)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.warehouse_movement_content_both_panels_collapsed)
                else nextLayout.load(this, R.layout.warehouse_movement_content_bottom_panel_collapsed)
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
                binding.warehouseMovementContent, transition
            )

            nextLayout.applyTo(binding.warehouseMovementContent)

            if (panelTopIsExpanded) binding.expandTopPanelButton?.text = context.getString(R.string.collapse_panel)
            else binding.expandTopPanelButton?.text = context.getString(R.string.select_destination)
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWmCont.isRefreshing = show
        }
    }

    private fun fillAdapter(contents: ArrayList<WarehouseMovementContent>) {
        showProgressBar(true)

        runOnUiThread {
            try {
                if (adapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = adapter?.currentItem()
                }

                adapter = WmcRecyclerAdapter(
                    recyclerView = binding.recyclerView,
                    fullList = contents,
                    checkedIdArray = checkedIdArray,
                    multiSelect = multiSelect,
                    showCheckBoxes = showCheckBoxes,
                    showCheckBoxesChanged = { showCheckBoxes = it },
                    showImages = showImages,
                    showImagesChanged = { showImages = it },
                    visibleStatus = ArrayList(WarehouseMovementContentStatus.getAll())
                )

                refreshAdapterListeners()

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = adapter

                while (binding.recyclerView.adapter == null) {
                    // Horrible wait for a full load
                }

                // Recuperar la última posición seleccionada
                val ls = lastSelected ?: contents.firstOrNull()
                val cs = currentScrollPosition
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter?.selectItem(ls, false)
                    adapter?.scrollToPos(cs, true)
                }, 200)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                gentlyReturn()
            }
        }
    }

    private fun refreshAdapterListeners() {
        adapter?.refreshListeners(
            checkedChangedListener = this,
            dataSetChangedListener = this,
            editAssetListener = this
        )

        if (useImageControl) {
            adapter?.refreshImageControlListeners(
                addPhotoListener = this,
                albumViewListener = this
            )
        }
    }

    private fun gentlyReturn() {
        closeKeyboard(this)
        allowClicks = true

        ScannerManager.lockScanner(this, false)
        rejectNewInstances = false

        showProgressBar(false)
    }

    private fun setupTextView() {
        val assetToMove = adapter?.assetsToMove ?: 0
        val tempText = if (assetToMove == 1) getString(R.string._asset) else getString(R.string._assets)

        runOnUiThread {
            binding.toMoveTextView.text = String.format("%s %s", assetToMove.toString(), tempText)
        }
    }

    private fun cancelWarehouseMovement() {
        if ((adapter?.itemCount ?: 0) <= 0) {
            closeKeyboard(this)

            isFinishingByUser = true
            setResult(RESULT_CANCELED)
            finish()
        } else {
            ScannerManager.lockScanner(this, true)
            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(context.getString(R.string.cancel_movement))
                alert.setMessage(context.getString(R.string.discard_changes_and_return_to_the_main_menu_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    isFinishingByUser = true

                    closeKeyboard(this)
                    setResult(RESULT_CANCELED)
                    finish()
                }

                alert.show()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                ScannerManager.lockScanner(this, false)
                allowClicks = true
            }
        }
    }

    private fun detailAsset() {
        val asset = adapter?.currentAsset()
        if (asset != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, AssetDetailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("asset", asset)
                resultForAssetDetails.launch(intent)
            }
        } else allowClicks = true
    }

    private val resultForAssetDetails =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            allowClicks = true
            rejectNewInstances = false
        }

    private fun removeAsset() {
        val content = adapter?.currentItem()
        if (content != null) {
            ScannerManager.lockScanner(this, true)

            try {
                val adb = AlertDialog.Builder(this)
                adb.setTitle(R.string.remove_item)
                adb.setMessage(
                    String.format(
                        context.getString(R.string.do_you_want_to_remove_the_item), content.code
                    )
                )
                adb.setNegativeButton(R.string.cancel, null)
                adb.setPositiveButton(R.string.accept) { _, _ ->
                    adapter?.remove(content.assetId)
                }
                adb.setOnDismissListener { allowClicks = true }
                adb.show()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                ScannerManager.lockScanner(this, false)
            }
        } else allowClicks = true
    }

    private fun addAsset() {
        if (!rejectNewInstances) {
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

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
                    val idParcel = data.parcelableArrayList<ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: ArrayList<Long?> = ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val codes: ArrayList<String> = ArrayList()
                    val assetRepository = AssetRepository()
                    for (id in ids) {
                        if (id == null) continue
                        val a = assetRepository.selectById(id) ?: continue
                        codes.add(a.code)
                    }

                    try {
                        scannerHandleScanCompleted(codes, true)
                    } catch (ex: Exception) {
                        val res =
                            context.getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                        makeText(binding.root, res, ERROR)
                        Log.d(this::class.java.simpleName, res)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                ScannerManager.lockScanner(this, false)
                allowClicks = true
            }
        }

    private fun finishWarehouseMovement() {
        if (headerFragment == null || headerFragment?.warehouseArea == null) {
            makeText(
                binding.root,
                context.getString(R.string.you_must_select_a_destination_for_assets),
                ERROR
            )

            allowClicks = true
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

            // Guardamos la revisión en una tabla temporal.
            // Revisiones de miles de artículos no pueden pasarse en el intent.
            TempMovementContentRepository().insert(
                wmId = 1,
                contents = adapter?.fullList?.toList() ?: listOf()
            )

            val intent = Intent(this, WmcConfirmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("warehouseArea", Parcels.wrap<WarehouseArea>(headerFragment?.warehouseArea))
            resultForFinishMovement.launch(intent)
        }
    }

    private val resultForFinishMovement =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    when (Parcels.unwrap<ConfirmStatus>(data.parcelable("confirmStatus"))) {
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
                ScannerManager.lockScanner(this, false)
                allowClicks = true
            }
        }

    private fun processWarehouseMovement() {
        val warehouseArea = headerFragment?.warehouseArea ?: return

        val destWaId = warehouseArea.id
        val allWmc: ArrayList<WarehouseMovementContent> = ArrayList()

        // Procesar la lista, cambiar los estados si son incorrectos.
        for (wmc in adapter?.fullList ?: ArrayList()) {
            if (wmc.warehouseAreaId == destWaId) {
                wmc.contentStatusId = WarehouseMovementContentStatus.noNeedToMove.id
            }
            allWmc.add(wmc)
        }

        saveMovement(destWaId, allWmc)
    }

    private fun saveMovement(destWaId: Long, allWmc: ArrayList<WarehouseMovementContent>) {
        ScannerManager.lockScanner(this, true)

        thread {
            val saveMovement = SaveMovement()
            saveMovement.addParams(
                destWarehouseAreaId = destWaId,
                obs = obs,
                movementContents = allWmc,
                onProgress = { saveViewModel.setSaveProgress(it) },
                onSyncProgress = { syncViewModel.setSyncUploadProgress(it) },
                onUploadImageProgress = { syncViewModel.setUploadImagesProgress(it) },
            )
            saveMovement.execute()
        }
    }

    private fun scannerHandleScanCompleted(scannedCode: ArrayList<String>, manuallyAdded: Boolean) {
        ScannerManager.lockScanner(this, true)

        try {
            checkCode(
                codesArray = scannedCode, manuallyAdded = manuallyAdded
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    override fun onStart() {
        super.onStart()

        if (_fillAdapter) {
            _fillAdapter = false
            fillAdapter(completeList)
        } else {
            showListOrEmptyListMessage()
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        closeKeyboard(this)
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        scannerHandleScanCompleted(arrayListOf(scanCode), false)
    }

    private fun checkCode(codesArray: ArrayList<String>, manuallyAdded: Boolean) {
        val allMovements: ArrayList<WarehouseMovementContent> = ArrayList()

        try {
            for (scannedCode in codesArray) {
                // Nada que hacer, volver
                if (scannedCode.isEmpty()) {
                    val res = getString(R.string.invalid_code)
                    makeText(binding.root, res, ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                val sc = ScannedCode(this).getFromCode(
                    code = scannedCode,
                    searchWarehouseAreaId = true,
                    searchAssetCode = true,
                    searchAssetSerial = true,
                    searchAssetEan = true
                )

                val scWa = sc.warehouseArea
                if (scWa != null) {
                    val wa = headerFragment?.warehouseArea
                    if (wa != scWa) {
                        makeText(
                            binding.root,
                            context.getString(R.string.destination_changed),
                            SnackBarType.INFO
                        )
                        Handler(Looper.getMainLooper()).postDelayed({
                            runOnUiThread {
                                headerFragment?.fill(scWa)
                            }
                        }, 50)
                    }
                    break
                }

                if (sc.codeFound && sc.asset != null && sc.labelNbr == 0) {
                    val res = getString(R.string.report_code)
                    makeText(binding.root, res, ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                if (sc.codeFound && sc.asset != null && sc.asset?.labelNumber == null) {
                    val res = getString(R.string.no_printed_label)
                    makeText(binding.root, res, ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                if (sc.codeFound && sc.asset != null && !manuallyAdded &&
                    sc.labelNbr != null && sc.asset?.labelNumber != sc.labelNbr
                ) {
                    val res = getString(R.string.invalid_code)
                    makeText(binding.root, res, ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                var tempCode = scannedCode
                if (sc.asset != null) {
                    // Si ya se encontró un activo, utilizo su código real,
                    // ya que el código escaneado puede contener caracteres especiales
                    // que no aparecen en la lista
                    tempCode = sc.asset?.code ?: ""
                }

                val itemCount = adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    // Buscar primero en el adaptador de la lista
                    val arCont = adapter?.getContentByCode(tempCode)
                    if (arCont != null) {
                        val res = "$scannedCode: ${getString(R.string.already_registered)}"
                        makeText(binding.root, res, SnackBarType.INFO)
                        Log.d(this::class.java.simpleName, res)

                        runOnUiThread {
                            adapter?.selectItem(arCont)
                        }
                        return
                    }
                }

                if (sc.asset == null) {
                    val res = getString(R.string.unknown_code)
                    makeText(binding.root, res, ERROR)
                    Log.d(this::class.java.simpleName, res)
                    continue
                }

                var contStatus = WarehouseMovementContentStatus.toMove
                val tempAsset = sc.asset
                val tempWarehouseArea = headerFragment?.warehouseArea

                if (tempAsset != null) {
                    if (tempWarehouseArea != null) {
                        /////////////////////////////////////////////////////////
                        // STATUS 2 = Si el activo está en la misma área, dejamos que lo agregue.
                        if (tempAsset.warehouseAreaId == tempWarehouseArea.id) {
                            contStatus = WarehouseMovementContentStatus.noNeedToMove

                            val res = getString(R.string.is_already_in_the_area)
                            makeText(binding.root, res, SnackBarType.INFO)
                        }
                    }

                    tempId--

                    val finalWmc = WarehouseMovementContent(
                        movementId = 0,
                        id = tempId,
                        asset = tempAsset,
                        contentStatusId = contStatus.id
                    )

                    allMovements.add(finalWmc)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        }

        if (allMovements.isNotEmpty()) {
            runOnUiThread {
                if (adapter == null) {
                    completeList = allMovements
                    fillAdapter(completeList)
                } else {
                    adapter?.add(allMovements, true)
                }
            }
        }

        ScannerManager.lockScanner(this, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        if (BuildConfig.DEBUG || Statics.DEMO_MODE) {
            menu.add(Menu.NONE, menuItemManualCode, Menu.NONE, "Manual code")
            menu.add(Menu.NONE, menuItemRandomCode, Menu.NONE, "Random asset code")
            menu.add(Menu.NONE, menuItemRandomOnListL, Menu.NONE, "Random asset on list")
            menu.add(Menu.NONE, menuItemRandomWa, Menu.NONE, "Random área")
            menu.add(Menu.NONE, menuItemRandomSerial, Menu.NONE, "Random asset serial")
        }

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    private val menuItemRandomCode = 999001
    private val menuItemManualCode = 999002
    private val menuItemRandomOnListL = 999003
    private val menuItemRandomWa = 999004
    private val menuItemRandomSerial = 999005

    private fun isBackPressed() {
        cancelWarehouseMovement()
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

            menuItemRandomOnListL -> {
                val codes: ArrayList<String> = ArrayList()

                (adapter?.fullList ?: ArrayList<WarehouseMovementContent>()
                    .filter { it.code.isNotEmpty() })
                    .mapTo(codes) { it.code }

                if (codes.any()) scannerCompleted(codes[Random().nextInt(codes.count())])
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomCode -> {
                val allCodes = AssetRepository().selectAllCodes()
                if (allCodes.any()) scannerCompleted(allCodes[Random().nextInt(allCodes.count())])
                return super.onOptionsItemSelected(item)
            }

            menuItemManualCode -> {
                enterCode()
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomWa -> {
                val allWa = WarehouseAreaRepository().select(true)
                val waId = allWa[Random().nextInt(allWa.count())].id
                if (allWa.any()) scannerCompleted("#WA#${String.format("%05d", waId)}#")
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomSerial -> {
                val allSerials = AssetRepository().selectAllSerials()
                if (allSerials.any()) scannerCompleted(allSerials[Random().nextInt(allSerials.count())])
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun enterCode() {
        runOnUiThread {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.enter_code))

            val inputLayout = TextInputLayout(this)
            inputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT

            val input = TextInputEditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
            input.isFocusable = true
            input.isFocusableInTouchMode = true
            input.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (alertDialog != null) {
                                alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
                                    .performClick()
                            }
                        }
                    }
                }
                false
            }

            inputLayout.addView(input)
            builder.setView(inputLayout)
            builder.setPositiveButton(R.string.accept) { _, _ ->
                scannerCompleted(input.text.toString())
            }
            builder.setNegativeButton(R.string.cancel, null)
            alertDialog = builder.create()

            alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            alertDialog.show()
            input.requestFocus()
        }
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    private fun onSaveProgress(it: SaveProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val progress: Int = it.progress
        val total: Int = it.total

        showProgressDialog(
            context.getString(R.string.saving_route_process), msg, taskStatus, progress, total
        )

        if (ProgressStatus.isFinishStatus(taskStatus)) {
            ScannerManager.lockScanner(this, false)
        }

        if (taskStatus == ProgressStatus.finished.id) {
            closeKeyboard(this)
            makeText(
                binding.root,
                context.getString(R.string.movement_performed_correctly),
                SUCCESS
            )

            isFinishingByUser = true
            setResult(RESULT_OK)
            finish()
        } else if (taskStatus == ProgressStatus.canceled.id || taskStatus == ProgressStatus.crashed.id) {
            makeText(binding.root, msg, ERROR)
        }
    }

    private fun onUploadImagesProgress(it: UploadImagesProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val result: IcProgressStatus = it.result
        val msg: String = it.msg
        val completed = it.completedTask
        val total = it.totalTask

        when (result.id) {
            ProgressStatus.starting.id, ProgressStatus.success.id, ProgressStatus.running.id -> {
                showProgressDialog(
                    title = getString(R.string.uploading_images),
                    msg = msg,
                    status = result.id,
                    progress = completed,
                    total = total
                )
            }

            ProgressStatus.crashed.id, ProgressStatus.canceled.id -> {
                makeText(this, msg, ERROR)
            }

            ProgressStatus.finished.id -> {
                makeText(this, getString(R.string.upload_images_success), SUCCESS)
            }
        }
    }

    private fun onSyncUploadProgress(it: SyncProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        val progressStatusDesc = progressStatus.description
        var registryDesc = getString(R.string.all_tasks)
        if (registryType != null) {
            registryDesc = registryType.description
        }

        when (progressStatus) {
            ProgressStatus.bigStarting,
            ProgressStatus.starting,
            ProgressStatus.running,
                -> {
                showProgressDialog(
                    title = getString(R.string.synchronizing_),
                    msg = msg,
                    status = progressStatus.id,
                    progress = completedTask,
                    total = totalTask
                )
            }

            ProgressStatus.bigFinished -> {
                closeKeyboard(this)

                isFinishingByUser = true
                setResult(RESULT_OK)
                finish()
            }

            ProgressStatus.bigCrashed,
            ProgressStatus.canceled,
                -> {
                closeKeyboard(this)
                makeText(binding.root, msg, ERROR)
                ErrorLog.writeLog(
                    this, this::class.java.simpleName, "$progressStatusDesc: $registryDesc ${
                        Statics.getPercentage(completedTask, totalTask)
                    }, $msg"
                )

                isFinishingByUser = true
                setResult(RESULT_OK)
                finish()
            }

            else -> {
                Log.d(
                    this::class.java.simpleName, "$progressStatusDesc: $registryDesc ${
                        Statics.getPercentage(completedTask, totalTask)
                    }, $msg"
                )
            }
        }
    }

    // region ProgressBar
    // Aparece mientras se realizan operaciones sobre la base de datos remota y local
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
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        runOnUiThread {
            if (progressDialog == null) {
                createProgressDialog()
            }

            val appColor =
                ResourcesCompat.getColor(context.resources, R.color.assetControl, null)

            when (status) {
                ProgressStatus.starting.id -> {
                    progressDialog?.setTitle(title)
                    //dialog?.setMessage(msg)
                    alertBinding.messageTextView.text = msg
                    alertBinding.progressBarHor.progress = 0
                    alertBinding.progressBarHor.max = 0
                    alertBinding.progressBarHor.visibility = GONE
                    alertBinding.progressTextView.visibility = GONE
                    alertBinding.progressBarHor.progressTintList = ColorStateList.valueOf(appColor)
                    alertBinding.progressBar.visibility = VISIBLE
                    alertBinding.progressBar.progressTintList = ColorStateList.valueOf(appColor)

                    progressDialog?.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        context.getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing && !isDestroyed) progressDialog?.show()
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

                        if (alertBinding.progressBarHor.isGone) {
                            alertBinding.progressBarHor.visibility = VISIBLE
                            alertBinding.progressTextView.visibility = VISIBLE
                        }

                        if (alertBinding.progressBar.isVisible) alertBinding.progressBar.visibility =
                            GONE
                    } else {
                        alertBinding.progressBar.progress = 0
                        alertBinding.progressBar.max = 0
                        alertBinding.progressBar.isIndeterminate = true

                        if (alertBinding.progressBarHor.isVisible) {
                            alertBinding.progressBarHor.visibility = GONE
                            alertBinding.progressTextView.visibility = GONE
                        }
                        if (alertBinding.progressBar.isGone) alertBinding.progressBar.visibility =
                            VISIBLE
                    }

                    progressDialog?.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        context.getString(R.string.cancel),
                        DialogInterface.OnClickListener { _, _ ->
                            return@OnClickListener
                        })

                    if (!isFinishing && !isDestroyed) progressDialog?.show()
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
                        SUCCESS
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
        if (currentInventory == null) currentInventory = ArrayList()
        if (currentInventory?.contains(scanCode) == false) currentInventory?.add(scanCode)

        scannerHandleScanCompleted(
            scannedCode = arrayListOf(scanCode), manuallyAdded = false
        )
    }

    //endregion READERS Reception

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        setupTextView()
    }

    override fun onDataSetChanged() {
        setupTextView()
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = (adapter?.itemCount ?: 0) == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.recyclerView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        val asset = adapter?.currentAsset()

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
                    val a = Parcels.unwrap<Asset>(data.parcelable("asset"))
                        ?: return@registerForActivityResult
                    adapter?.updateContent(a, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    // region ImageControl

    override fun onAlbumViewRequired(tableId: Int, itemId: Long, filename: String) {
        if (!useImageControl) {
            return
        }

        if (rejectNewInstances) return
        rejectNewInstances = true

        tempObjectId = itemId.toString()
        tempTableId = tableId

        val programData = ProgramData(
            programObjectId = tempTableId.toLong(),
            objId1 = tempObjectId
        )

        ImageCoroutines().get(context = context, programData = programData) {
            val allLocal = toDocumentContentList(it, programData)
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
                makeText(binding.root, getString(R.string.no_images), SnackBarType.INFO)
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
            makeText(binding.root, getString(R.string.no_images), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        val anyAvailable = docContReqResObj.documentContentArray.any { it.available }

        if (!anyAvailable) {
            makeText(
                binding.root,
                context.getString(R.string.images_not_yet_processed),
                SnackBarType.INFO
            )
            rejectNewInstances = false
            return
        }

        showPhotoAlbum()
    }

    override fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String, obs: String, reference: String) {
        if (!useImageControl) {
            return
        }

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
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    adapter?.currentAsset()?.saveChanges()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    // endregion IC
}