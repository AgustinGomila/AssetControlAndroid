package com.example.assetControl.ui.activities.review

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.getUserId
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.Table
import com.example.assetControl.data.enums.review.AssetReviewStatus
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.example.assetControl.data.room.repository.review.AssetReviewRepository
import com.example.assetControl.databinding.AssetReviewSelectActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.nfc.Nfc
import com.example.assetControl.devices.scanners.rfid.Rfid
import com.example.assetControl.devices.scanners.rfid.Rfid.Companion.isRfidRequired
import com.example.assetControl.devices.scanners.vh75.Vh75Bt
import com.example.assetControl.ui.activities.location.LocationSelectActivity
import com.example.assetControl.ui.adapters.review.AssetReviewAdapter
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.example.assetControl.ui.fragments.location.WarehouseAreaSelectFilterFragment
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.parcel.Parcelables.parcelableArrayList
import com.example.assetControl.utils.settings.config.Preference
import org.parceler.Parcels
import java.io.File
import java.util.concurrent.ThreadLocalRandom

@Suppress("UNCHECKED_CAST")
class AssetReviewSelectActivity : AppCompatActivity(), Scanner.ScannerListener,
    SwipeRefreshLayout.OnRefreshListener, WarehouseAreaSelectFilterFragment.FragmentListener,
    Rfid.RfidDeviceListener, AssetReviewAdapter.DataSetChangedListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        saveSharedPreferences()
        adapter?.refreshListeners()
        waSelectFilterFragment?.onDestroy()
    }

    private fun saveSharedPreferences() {
        val set = HashSet<String>()
        for (i in visibleStatusArray) set.add(i.id.toString())
        sr.prefsPutStringSet(
            Preference.assetReviewVisibleStatus.key,
            set
        )
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshWarehouseArea.isRefreshing = false
            }
        }, 1000)
    }

    private var tempTitle = ""

    private var rejectNewInstances = false

    private var visibleStatusArray: ArrayList<AssetReviewStatus> = ArrayList()

    private var multiSelect = false
    private var waSelectFilterFragment: WarehouseAreaSelectFilterFragment? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var lastSelected: AssetReview? = null
    private var firstVisiblePos: Int? = null
    private var adapter: AssetReviewAdapter? = null
    private var panelBottomIsExpanded = true


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveBundleValues(outState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("multiSelect", multiSelect)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (adapter != null) {
            b.putParcelable("lastSelected", adapter?.currentAssetReview())
            b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: 0)
            b.putParcelableArrayList(
                "visibleStatusArray", adapter?.getVisibleStatus()
            )
            b.putLongArray("checkedIdArray", adapter?.getAllChecked()?.toLongArray())
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title") ?: ""
        tempTitle = t1.ifEmpty { getString(R.string.select_asset_review) }
        // endregion

        // PANELS
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")

        // ADAPTER
        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1

        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        visibleStatusArray.clear()
        if (b.containsKey("visibleStatusArray")) {
            val t3 = b.parcelableArrayList<AssetReviewStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            loadDefaultVisibleStatus()
        }
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_asset_review)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleStatusArray.clear()
        var set = sr.prefsGetStringSet(
            Preference.assetReviewVisibleStatus.key,
            Preference.assetReviewVisibleStatus.defaultValue as ArrayList<String>
        )
        if (set == null) set = AssetReviewStatus.getAll().map { it.id.toString() }.toSet()

        for (i in set) {
            val status = AssetReviewStatus.getById(i.toInt())
            if (!visibleStatusArray.contains(status)) {
                visibleStatusArray.add(status)
            }
        }
    }

    private lateinit var binding: AssetReviewSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetReviewSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        waSelectFilterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as WarehouseAreaSelectFilterFragment

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        waSelectFilterFragment?.setListener(this)

        binding.swipeRefreshWarehouseArea.setOnRefreshListener(this)
        binding.swipeRefreshWarehouseArea.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Para expandir y colapsar el panel inferior
        setBottomPanelAnimation()

        binding.okButton.setOnClickListener { assetReviewSelect() }
        binding.removeButton.setOnClickListener { assetReviewRemove() }
        binding.newButton.setOnClickListener { assetReviewNew() }

        setPanels()

        setupUI(binding.root, this)
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            currentLayout.load(this, R.layout.asset_review_select_activity)
        } else {
            currentLayout.load(
                this, R.layout.asset_review_select_bottom_panel_collapsed
            )
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
            binding.assetReviewSelect, transition
        )

        currentLayout.applyTo(binding.assetReviewSelect)

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text = getString(R.string.collapse_panel)
            }

            else -> {
                binding.expandBottomPanelButton?.text = getString(R.string.search_options)
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
                nextLayout.load(
                    this, R.layout.asset_review_select_bottom_panel_collapsed
                )
            } else {
                nextLayout.load(this, R.layout.asset_review_select_activity)
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
                binding.assetReviewSelect, transition
            )

            nextLayout.applyTo(binding.assetReviewSelect)

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

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshWarehouseArea.isRefreshing = show
        }
    }

    private fun assetReviewSelect() {
        if (adapter == null) return

        val ar = adapter?.currentAssetReview() ?: return

        if (ar.statusId == AssetReviewStatus.onProcess.id) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, ArcActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("isNew", false)
                intent.putExtra("assetReview", Parcels.wrap(ar))
                resultForReviewSuccess.launch(intent)
            }
        } else {
            showMessage(
                getString(R.string.selected_review_is_not_in_process), ERROR
            )
        }
    }

    private fun assetReviewRemove() {
        if (adapter == null) return

        val ar = adapter?.currentAssetReview() ?: return

        if (ar.statusId == AssetReviewStatus.completed.id) {
            showMessage(
                getString(R.string.the_selected_revision_has_already_been_completed_and_can_not_be_deleted),
                SnackBarType.INFO
            )
            return
        }

        ScannerManager.lockScanner(this, true)

        try {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getString(R.string.remove_review))
            alert.setMessage(getString(R.string.do_you_want_to_delete_the_selected_revision_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                AssetReviewRepository().deleteById(ar.id)
                AssetReviewContentRepository().deleteByAssetReviewId(ar.id)
                removeAssetReviewImages()

                adapter?.remove(ar)
                fillListView()
            }

            alert.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    private fun removeAssetReviewImages() {
        if (adapter == null) return

        val ar = adapter?.currentAssetReview() ?: return

        try {
            val programData = ProgramData(
                programObjectId = Table.assetReview.id.toLong(),
                objId1 = ar.id.toString()
            )

            ImageCoroutines().get(context = context, programData = programData) {
                if (it.isNotEmpty()) {
                    for (t in it) {
                        val file = File(t.filenameOriginal ?: "")
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }

                // Eliminar las referencias a las imágenes en
                // la base de datos local de ImageControl
                ImageCoroutines().delete(
                    context = context,
                    programObjectId = Table.assetReview.id.toLong(),
                    objectId1 = ar.id.toString()
                )
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun assetReviewNew() {
        // Si hay más de uno debe seleccionar la ubicación
        // Comprobar que solo exista uno en la ubicación seleccionada.
        if (!rejectNewInstances) {
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

            val intent = Intent(baseContext, LocationSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("title", getString(R.string.select_area_to_review))
            intent.putExtra("warehouseVisible", true)
            intent.putExtra("warehouseAreaVisible", true)
            resultForLocationSelect.launch(intent)
        }
    }

    private val resultForLocationSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            try {
                if (it.resultCode == RESULT_OK && data != null) {
                    val warehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.parcelable("warehouseArea"))
                    if (warehouseArea != null) {
                        rejectNewInstances = false
                        beginAssetReview(warehouseArea)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                ScannerManager.lockScanner(this, false)
            }
        }

    private fun fillListView() {
        val userId = getUserId() ?: return

        val w = waSelectFilterFragment?.wDescription ?: ""
        val wa = waSelectFilterFragment?.waDescription ?: ""
        val onlyActive = waSelectFilterFragment?.onlyActive != false

        val assetReviewList =
            ArrayList(
                AssetReviewRepository().selectByDescription(
                    wDescription = w,
                    waDescription = wa,
                    userId = userId,
                    onlyActive = onlyActive
                )
            )

        fillAdapter(assetReviewList)
    }

    private fun fillAdapter(assetReviewArray: ArrayList<AssetReview>) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (adapter != null) {
                    lastSelected = adapter?.currentAssetReview()
                    firstVisiblePos = adapter?.firstVisiblePos()
                }

                adapter = AssetReviewAdapter(
                    activity = this,
                    resource = R.layout.asset_review_row,
                    assetReviews = assetReviewArray,
                    listView = binding.assetReviewListView,
                    multiSelect = false,
                    checkedIdArray = checkedIdArray,
                    visibleStatus = visibleStatusArray
                )

                adapter?.refreshListeners(dataSetChangedListener = this)
                adapter?.refresh()

                while (binding.assetReviewListView.adapter == null) {
                    // Horrible wait for full load
                }

                adapter?.setSelectItemAndScrollPos(lastSelected, firstVisiblePos)

                if (Statics.DEMO_MODE) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    private fun beginAssetReview(warehouseArea: WarehouseArea) {
        showMessage(warehouseArea.description, SnackBarType.INFO)

        val reviewRepository = AssetReviewRepository()

        // Agregar un AssetReview del área
        val arId = reviewRepository.insert(warehouseArea)
        val ar = reviewRepository.selectById(arId)

        if (ar != null) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, ArcActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("isNew", true)
                intent.putExtra("assetReview", Parcels.wrap(ar))
                resultForReviewSuccess.launch(intent)
            }
        } else {
            showMessage(getString(R.string.error_inserting_assets_to_review), ERROR)
        }
    }

    private val resultForReviewSuccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (it?.resultCode == RESULT_OK) {
                    fillListView()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
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

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        val allStatus = AssetReviewStatus.getAll()

        // Opciones de visibilidad del menú
        for (i in allStatus) {
            menu.add(0, i.id, i.id, i.description).setChecked(allStatus.contains(i)).isCheckable =
                true
        }

        //region Icon colors
        val seagreen = ResourcesCompat.getColor(resources, R.color.seagreen, null)
        val gold = ResourcesCompat.getColor(resources, R.color.gold, null)
        val steelblue = ResourcesCompat.getColor(resources, R.color.steelblue, null)
        val whitesmoke = ResourcesCompat.getColor(resources, R.color.whitesmoke, null)

        /*
        Ordenar los colores por AssetReviewStatus.id
        unknown = 0
        onProcess = 1
        completed = 2
        transferred = 3
        */

        val colors: ArrayList<Int> = ArrayList()
        colors.add(whitesmoke)   // unknown
        colors.add(steelblue)    // onProcess
        colors.add(gold)         // completed
        colors.add(seagreen)     // transferred
        //endregion Icon colors

        for ((index, i) in allStatus.withIndex()) {
            val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lens, null)
            icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    colors[index], BlendModeCompat.SRC_IN
                )
            val item = menu.findItem(i.id)
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
        if (adapter == null) {
            return false
        }

        val visibleStatus = adapter!!.getVisibleStatus()
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetReviewStatus.onProcess.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.onProcess
                )
            ) {
                adapter!!.addVisibleStatus(AssetReviewStatus.onProcess)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.onProcess)) {
                adapter!!.removeVisibleStatus(AssetReviewStatus.onProcess)
            }

            AssetReviewStatus.completed.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.completed
                )
            ) {
                adapter!!.addVisibleStatus(AssetReviewStatus.completed)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.completed)) {
                adapter!!.removeVisibleStatus(AssetReviewStatus.completed)
            }

            AssetReviewStatus.transferred.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.transferred
                )
            ) {
                adapter!!.addVisibleStatus(AssetReviewStatus.transferred)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.transferred)) {
                adapter!!.removeVisibleStatus(AssetReviewStatus.transferred)
            }

            AssetReviewStatus.unknown.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.unknown
                )
            ) {
                adapter!!.addVisibleStatus(AssetReviewStatus.unknown)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.unknown)) {
                adapter!!.removeVisibleStatus(AssetReviewStatus.unknown)
            }

            else -> return super.onOptionsItemSelected(item)
        }

        if (adapter?.isStatusVisible(adapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            adapter?.selectNearVisible()
        }

        return true
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        closeKeyboard(this)
        waSelectFilterFragment?.refreshViews()
    }

    override fun onFilterChanged(waDescription: String, wDescription: String, onlyActive: Boolean) {
        closeKeyboard(this)
        fillListView()
    }

    override fun onDataSetChanged() {
        showListOrEmptyListMessage()
    }

    private fun showListOrEmptyListMessage() {
        runOnUiThread {
            val isEmpty = (adapter?.itemCount ?: 0) == 0
            binding.emptyTextView.visibility = if (isEmpty) VISIBLE else GONE
            binding.assetReviewListView.visibility = if (isEmpty) GONE else VISIBLE
        }
    }

    private fun demo() {
        if (!Statics.DEMO_MODE) return

        val allWarehouseArea = WarehouseAreaRepository().select(true)
        if (!allWarehouseArea.any()) return

        val warehouseArea =
            allWarehouseArea[ThreadLocalRandom.current().nextInt(0, allWarehouseArea.size)]

        rejectNewInstances = false
        beginAssetReview(warehouseArea)
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
            return svm.showScannedCode
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) showMessage(scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scanCode.trim().isEmpty()) {
                val res = getString(R.string.invalid_code)
                showMessage(res, ERROR)
                ErrorLog.writeLog(this, this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                scanCode,
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                searchAssetEan = false
            )

            val warehouseArea = if (sc.warehouseArea != null) {
                sc.warehouseArea
            } else {
                val res = getString(R.string.invalid_warehouse_area_code)
                showMessage(res, ERROR)
                null
            }

            if (warehouseArea != null) {
                rejectNewInstances = false
                beginAssetReview(warehouseArea)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            showMessage(ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
            closeKeyboard(this)
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
        if (svm.rfidShowConnectedMessage) {
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

    private fun showMessage(msg: String, type: SnackBarType) {
        if (isFinishing || isDestroyed) return
        if (type == ERROR) logError(msg)
        makeText(binding.root, msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }
}