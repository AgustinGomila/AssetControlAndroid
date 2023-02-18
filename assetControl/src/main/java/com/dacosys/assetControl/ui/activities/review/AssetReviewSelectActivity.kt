package com.dacosys.assetControl.ui.activities.review

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
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
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.review.AssetReviewAdapter
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.location.WarehouseAreaDbHelper
import com.dacosys.assetControl.dataBase.review.AssetReviewContentDbHelper
import com.dacosys.assetControl.dataBase.review.AssetReviewDbHelper
import com.dacosys.assetControl.databinding.AssetReviewSelectActivityBinding
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.ui.activities.location.LocationSelectActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.fragments.location.WarehouseAreaSelectFilterFragment
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.room.dao.ImageCoroutines
import org.parceler.Parcels
import java.io.File
import java.util.concurrent.ThreadLocalRandom

@Suppress("UNCHECKED_CAST")
class AssetReviewSelectActivity : AppCompatActivity(), Scanner.ScannerListener,
    SwipeRefreshLayout.OnRefreshListener, WarehouseAreaSelectFilterFragment.FragmentListener,
    Rfid.RfidDeviceListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        saveSharedPreferences()
        arrayAdapter?.refreshListeners(null, null)
        waSelectFilterFragment?.onDestroy()
    }

    private fun saveSharedPreferences() {
        val set = HashSet<String>()
        for (i in visibleStatusArray) set.add(i.id.toString())
        Statics.prefsPutStringSet(Preference.assetReviewVisibleStatus.key, set)
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
    private var arrayAdapter: AssetReviewAdapter? = null
    private var panelBottomIsExpanded = true

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
            (0 until view.childCount).map { view.getChildAt(it) }.forEach { setupUI(it) }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("multiSelect", multiSelect)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentAssetReview())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
            b.putParcelableArrayList(
                "visibleStatusArray", arrayAdapter?.getVisibleStatus()
            )
            b.putLongArray("checkedIdArray", arrayAdapter?.getAllChecked()?.toLongArray())
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle =
            if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.select_asset_review)
        // endregion

        // PANELS
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")

        // ADAPTER
        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1

        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        visibleStatusArray.clear()
        if (b.containsKey("visibleStatusArray")) {
            val t3 = b.getParcelableArrayList<AssetReviewStatus>("visibleStatusArray")
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
        var set = Statics.prefsGetStringSet(
            Preference.assetReviewVisibleStatus.key,
            Preference.assetReviewVisibleStatus.defaultValue as ArrayList<String>
        )
        if (set == null) set = AssetReviewStatus.getAllIdAsString().toSet()

        for (i in set) {
            val status = AssetReviewStatus.getById(i.toInt())
            if (status != null && !visibleStatusArray.contains(status)) {
                visibleStatusArray.add(status)
            }
        }
    }

    private lateinit var binding: AssetReviewSelectActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = AssetReviewSelectActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Llenar la grilla
        setPanels()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.assetReviewSelect)
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

    private fun assetReviewSelect() {
        if (arrayAdapter != null) {
            val ar = arrayAdapter?.currentAssetReview() ?: return

            if (ar.statusId == AssetReviewStatus.onProcess.id) {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, AssetReviewContentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("isNew", false)
                    intent.putExtra("assetReview", Parcels.wrap(ar))
                    resultForReviewSuccess.launch(intent)
                }
            } else {
                makeText(
                    binding.root, getString(R.string.selected_review_is_not_in_process), ERROR
                )
            }
        }
    }

    private fun assetReviewRemove() {
        if (arrayAdapter != null) {
            val ar = arrayAdapter?.currentAssetReview() ?: return

            if (ar.statusId == AssetReviewStatus.completed.id) {
                makeText(
                    binding.root,
                    getString(R.string.the_selected_revision_has_already_been_completed_and_can_not_be_deleted),
                    SnackBarType.INFO
                )
                return
            }

            JotterListener.pauseReaderDevices(this)

            try {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.remove_review))
                alert.setMessage(getString(R.string.do_you_want_to_delete_the_selected_revision_question))
                alert.setNegativeButton(R.string.cancel, null)
                alert.setPositiveButton(R.string.accept) { _, _ ->
                    AssetReviewDbHelper().deleteById(ar.collectorAssetReviewId)
                    AssetReviewContentDbHelper().deleteByAssetReviewId(ar.collectorAssetReviewId)
                    removeAssetReviewImages()

                    arrayAdapter?.remove(ar)
                    fillListView()
                }

                alert.show()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                JotterListener.resumeReaderDevices(this)
            }
        }
    }

    private fun removeAssetReviewImages() {
        if (arrayAdapter != null) {
            val ar = arrayAdapter?.currentAssetReview() ?: return

            try {
                val programData = ProgramData(
                    programId = Statics.INTERNAL_IMAGE_CONTROL_APP_ID.toLong(),
                    programObjectId = Table.assetReview.tableId.toLong(),
                    objId1 = ar.collectorAssetReviewId.toString()
                )

                ImageCoroutines().get(programData = programData) {
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
                        programObjectId = Table.assetReview.tableId.toLong(),
                        objectId1 = ar.collectorAssetReviewId.toString()
                    )
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
        }
    }

    private fun assetReviewNew() {
        // Si hay más de uno debe seleccionar la ubicación
        // Comprobar que sólo exista uno en la ubicación seleccionada.
        if (!rejectNewInstances) {
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

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
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val warehouseArea =
                        Parcels.unwrap<WarehouseArea>(data.getParcelableExtra("warehouseArea"))
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
                JotterListener.lockScanner(this, false)
            }
        }

    private fun fillListView() {
        try {
            val w = waSelectFilterFragment?.wDescription ?: ""
            val wa = waSelectFilterFragment?.waDescription ?: ""
            val onlyActive = waSelectFilterFragment?.onlyActive ?: true

            var assetReviewList: ArrayList<AssetReview> = ArrayList()
            try {
                assetReviewList = AssetReviewDbHelper().selectByDescription(w, wa, onlyActive)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }

            fillAdapter(assetReviewList)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    private fun fillAdapter(assetReviewArray: ArrayList<AssetReview>) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (arrayAdapter != null) {
                    lastSelected = arrayAdapter?.currentAssetReview()
                    firstVisiblePos = arrayAdapter?.firstVisiblePos()
                }

                arrayAdapter = AssetReviewAdapter(
                    activity = this,
                    resource = R.layout.asset_review_row,
                    assetReviews = assetReviewArray,
                    listView = binding.assetReviewListView,
                    multiSelect = false,
                    checkedIdArray = checkedIdArray,
                    visibleStatus = visibleStatusArray
                )

                while (binding.assetReviewListView.adapter == null) {
                    // Horrible wait for full load
                }

                if (arrayAdapter != null) {
                    arrayAdapter?.setSelectItemAndScrollPos(
                        lastSelected, firstVisiblePos
                    )
                }

                if (Statics.demoMode) Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showProgressBar(false)
        }
    }

    private fun beginAssetReview(warehouseArea: WarehouseArea) {
        makeText(binding.assetReviewSelect, warehouseArea.description, SnackBarType.INFO)

        // Contar la cantidad activos del área
        // Si es mayor a 1000, pedir que divida el área para poder hacer revisiones
        val qty = AssetDbHelper().countAssets(warehouseArea.warehouseAreaId)
        if (qty > 1000) {
            makeText(
                binding.root, getString(
                    R.string.there_are_x_assets_in_the_selected_area_divide_the_area_into_units_of_up_to_1000_assets_to_be_able_to_make_revisions,
                    qty.toString()
                ), ERROR
            )
            fillListView()
        } else {
            // Agregar un AssetReview del área
            val ar = AssetReview.add(warehouseArea)
            if (ar != null) {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, AssetReviewContentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("isNew", true)
                    intent.putExtra("assetReview", Parcels.wrap(ar))
                    resultForReviewSuccess.launch(intent)
                }
            } else {
                makeText(this, getString(R.string.error_inserting_assets_to_review), ERROR)
            }
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
                JotterListener.lockScanner(this, false)
            }
        }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_read_activity, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        if (!Statics.isRfidRequired()) {
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
                return statusItemSelected(item)
            }
        }
    }

    private fun statusItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (arrayAdapter == null) {
            return false
        }

        val visibleStatus = arrayAdapter!!.getVisibleStatus()
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetReviewStatus.onProcess.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.onProcess
                )
            ) {
                arrayAdapter!!.addVisibleStatus(AssetReviewStatus.onProcess)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.onProcess)) {
                arrayAdapter!!.removeVisibleStatus(AssetReviewStatus.onProcess)
            }
            AssetReviewStatus.completed.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.completed
                )
            ) {
                arrayAdapter!!.addVisibleStatus(AssetReviewStatus.completed)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.completed)) {
                arrayAdapter!!.removeVisibleStatus(AssetReviewStatus.completed)
            }
            AssetReviewStatus.transferred.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.transferred
                )
            ) {
                arrayAdapter!!.addVisibleStatus(AssetReviewStatus.transferred)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.transferred)) {
                arrayAdapter!!.removeVisibleStatus(AssetReviewStatus.transferred)
            }
            AssetReviewStatus.unknown.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewStatus.unknown
                )
            ) {
                arrayAdapter!!.addVisibleStatus(AssetReviewStatus.unknown)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewStatus.unknown)) {
                arrayAdapter!!.removeVisibleStatus(AssetReviewStatus.unknown)
            }
            else -> return super.onOptionsItemSelected(item)
        }

        if (arrayAdapter?.isStatusVisible(arrayAdapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            arrayAdapter?.selectNearVisible()
        }

        return true
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        Statics.closeKeyboard(this)
        waSelectFilterFragment?.refreshViews()
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }
    }

    override fun onFilterChanged(waDescription: String, wDescription: String, onlyActive: Boolean) {
        Statics.closeKeyboard(this)
        fillListView()
    }

    private fun demo() {
        if (!Statics.demoMode) return

        val allWarehouseArea = WarehouseAreaDbHelper().select(true)
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
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) JotterListener.onRequestPermissionsResult(
            this, requestCode, permissions, grantResults
        )
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scanCode.trim().isEmpty()) {
                val res = getString(R.string.invalid_code)
                makeText(binding.root, res, ERROR)
                ErrorLog.writeLog(this, this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                scanCode,
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                validateId = true
            )

            val warehouseArea = if (sc.warehouseArea != null) {
                sc.warehouseArea
            } else {
                val res = getString(R.string.invalid_warehouse_area_code)
                makeText(binding.root, res, ERROR)
                null
            }

            if (warehouseArea != null) {
                rejectNewInstances = false
                beginAssetReview(warehouseArea)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
            Statics.closeKeyboard(this)
        }
    }

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
        scannerCompleted(scanCode)
    }

    //endregion READERS Reception


}