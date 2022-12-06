package com.dacosys.assetControl.views.assets.asset.activities

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
import android.view.*
import android.view.View.GONE
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
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
import com.dacosys.assetControl.databinding.AssetPrintLabelActivityTopPanelCollapsedBinding
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.async.GetAssetAsync
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetAdapter
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.`object`.BarcodeLabelCustom
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelTarget.`object`.BarcodeLabelTarget
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.isRfidRequired
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scannedCode.ScannedCode
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.views.assets.asset.fragments.AssetSelectFilterFragment
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import com.dacosys.assetControl.views.print.fragments.PrinterFragment
import com.dacosys.imageControl.`object`.Images
import com.dacosys.imageControl.`object`.StatusObject
import com.dacosys.imageControl.activities.ImageControlCameraActivity
import com.dacosys.imageControl.activities.ImageControlGridActivity
import com.dacosys.imageControl.dbHelper.DbCommands
import com.dacosys.imageControl.main.DownloadTask
import com.dacosys.imageControl.main.GetImagesTask
import com.dacosys.imageControl.wsObject.DocumentContentObject
import com.dacosys.imageControl.wsObject.DocumentContentRequestResultObject
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.parceler.Parcels
import kotlin.concurrent.thread

class AssetPrintLabelActivity :
    AppCompatActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    Scanner.ScannerListener,
    Rfid.RfidDeviceListener,
    AssetSelectFilterFragment.FragmentListener,
    GetAssetAsync.GetAssetAsyncListener,
    KeyboardVisibilityEventListener,
    PrinterFragment.FragmentListener,
    AssetAdapter.CheckedChangedListener,
    AssetAdapter.DataSetChangedListener,
    AssetAdapter.Companion.AddPhotoRequiredListener,
    AssetAdapter.Companion.AlbumViewRequiredListener,
    AssetAdapter.Companion.EditAssetRequiredListener,
    GetImagesTask.GetImagesTask,
    DownloadTask.DownloadTaskListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        arrayAdapter?.refreshListeners(null, null, null)
        arrayAdapter?.refreshImageControlListeners(null, null)
        assetSelectFilterFragment?.onDestroy()
        printerFragment?.onDestroy()
    }
    // endregion

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefreshAsset.isRefreshing = false
            }
        }, 1000)
    }

    override fun onDataSetChanged() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                fillSummaryRow()
            }
        }, 100)
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        runOnUiThread {
            binding.selectedTextView.text = arrayAdapter?.countChecked().toString()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch checkedChangedListener for non-text box views to hide keyboard.
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

    private var tempTitle = ""

    private var rejectNewInstances = false

    // Configuración guardada de los controles que se ven o no se ven
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var onlyActive: Boolean = true
    private var multiSelect = false
    private var arrayAdapter: AssetAdapter? = null
    private var lastSelected: Asset? = null
    private var firstVisiblePos: Int? = null
    private var panelBottomIsExpanded = true
    private var panelTopIsExpanded = false

    private var searchText: String = ""

    // A esta actividad se le puede pasar una lista de Assets a seleccionar y ocultar el panel
    // que contiene los controles de filtrado para evitar que cambie la lista de Assets o las opciones
    // de filtrado.
    private var fixedItemList = false
    private var hideFilterPanel = false
    private var completeList: ArrayList<Asset> = ArrayList()
    private var assetSelectFilterFragment: AssetSelectFilterFragment? = null
    private var printerFragment: PrinterFragment? = null

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)
        b.putBoolean("onlyActive", onlyActive)
        b.putBoolean("multiSelect", multiSelect)
        b.putBoolean("hideFilterPanel", hideFilterPanel)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)
        b.putBoolean("fixedItemList", fixedItemList)

        if (arrayAdapter != null) {
            b.putParcelable("lastSelected", arrayAdapter?.currentAsset())
            b.putInt("firstVisiblePos", arrayAdapter?.firstVisiblePos() ?: 0)
            b.putLongArray("checkedIdArray", arrayAdapter?.getAllChecked()?.toLongArray())
        }

        // Guardar en la DB temporalmente los ítems listados
        if (fixedItemList) AssetDbHelper().insertTempId(arrayAdapter?.getAllId() ?: ArrayList())

        b.putString("searchText", searchText)
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.select_asset)
        // endregion

        // PANELS
        if (b.containsKey("hideFilterPanel"))
            hideFilterPanel = b.getBoolean("hideFilterPanel", hideFilterPanel)
        if (b.containsKey("panelBottomIsExpanded"))
            panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded"))
            panelTopIsExpanded = b.getBoolean("panelTopIsExpanded")

        // ADAPTER
        if (b.containsKey("onlyActive")) onlyActive = b.getBoolean("onlyActive")
        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        // Cargar la lista desde la DB local
        fixedItemList = b.getBoolean("fixedItemList")
        if (fixedItemList) completeList = AssetDbHelper().selectTempId()
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_asset)
        val id = Statics.prefsGetLong(Preference.defaultBarcodeLabelCustomAsset)
        val blc = BarcodeLabelCustomDbHelper().selectById(id)
        if (blc != null) printerFragment?.barcodeLabelCustom = blc
    }

    private lateinit var binding: AssetPrintLabelActivityTopPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = AssetPrintLabelActivityTopPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        KeyboardVisibilityEvent.registerEventListener(this, this)

        assetSelectFilterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as AssetSelectFilterFragment
        printerFragment =
            supportFragmentManager.findFragmentById(binding.printFragment.id) as PrinterFragment

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)

            searchText = savedInstanceState.getString("searchText") ?: ""
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        assetSelectFilterFragment?.setListener(this)
        assetSelectFilterFragment?.onlyActive = onlyActive

        printerFragment?.setListener(this)
        printerFragment?.barcodeLabelTarget = BarcodeLabelTarget.Asset

        binding.swipeRefreshAsset.setOnRefreshListener(this)
        binding.swipeRefreshAsset.setColorSchemeResources(
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

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int,
            ) {
                searchText = s.toString()
                arrayAdapter?.refreshFilter(searchText, true)
            }
        })
        binding.searchEditText.setText(
            searchText,
            TextView.BufferType.EDITABLE
        )
        binding.searchEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE ||
                (keyEvent.action == KeyEvent.ACTION_UP &&
                        (keyCode == KeyEvent.KEYCODE_ENTER ||
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
            ) {
                Statics.closeKeyboard(this)
            }
            false
        }
        binding.searchEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        binding.searchTextImageView.setOnClickListener { binding.searchEditText.requestFocus() }
        binding.searchTextClearImageView.setOnClickListener {
            binding.searchEditText.setText("")
        }

        // MODO SELECCION A PARTIR DE UNA LISTA FIJA DE ITEMS
        if (fixedItemList) fillAdapter(completeList)

        setPanels()

        setupUI(binding.root)

        showProgressBar(false)
    }

    private fun itemSelect() {
        Statics.closeKeyboard(this)

        val data = Intent()

        if (arrayAdapter != null) {
            val asset = arrayAdapter?.currentAsset()
            val assetIdArray = arrayAdapter?.getAllChecked()

            if (!multiSelect && asset != null) {
                data.putParcelableArrayListExtra(
                    "ids",
                    arrayListOf(Statics.ParcelLong(asset.assetId))
                )
                setResult(RESULT_OK, data)
            } else if (multiSelect && assetIdArray != null && assetIdArray.size > 0) {
                val parcelIdArray: ArrayList<Statics.ParcelLong> = ArrayList()
                for (it in assetIdArray) {
                    parcelIdArray.add(Statics.ParcelLong(it))
                }
                data.putParcelableArrayListExtra("ids", parcelIdArray)
                setResult(RESULT_OK, data)
            } else {
                setResult(RESULT_CANCELED)
            }
        } else {
            setResult(RESULT_CANCELED)
        }

        finish()
    }

    private fun fillSummaryRow() {
        runOnUiThread {
            if (multiSelect) {
                binding.totalLabelTextView.text = getString(R.string.total)
                binding.selectedLabelTextView.text = getString(R.string.checked)

                if (arrayAdapter != null) {
                    binding.totalTextView.text = arrayAdapter!!.count.toString()
                    binding.selectedTextView.text = arrayAdapter!!.countChecked().toString()
                }
            } else {
                binding.totalLabelTextView.text = getString(R.string.total)
                binding.selectedLabelTextView.text = getString(R.string.assets)

                if (arrayAdapter != null) {
                    binding.totalTextView.text = arrayAdapter!!.count.toString()
                    binding.selectedTextView.text = arrayAdapter!!.count.toString()
                }
            }

            if (arrayAdapter == null) {
                binding.totalTextView.text = 0.toString()
                binding.selectedTextView.text = 0.toString()
            }
        }
    }

    private fun setPanels() {
        val currentLayout = ConstraintSet()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) currentLayout.load(
                    this,
                    R.layout.asset_print_label_activity
                )
                else currentLayout.load(
                    this,
                    R.layout.asset_print_label_activity_top_panel_collapsed
                )
            } else {
                if (panelTopIsExpanded) currentLayout.load(
                    this,
                    R.layout.asset_print_label_activity_bottom_panel_collapsed
                )
                else currentLayout.load(
                    this,
                    R.layout.asset_print_label_activity_both_panels_collapsed
                )
            }
        } else {
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.asset_print_label_activity)
            else currentLayout.load(this, R.layout.asset_print_label_activity_top_panel_collapsed)
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

        TransitionManager.beginDelayedTransition(binding.assetPrintLabel, transition)
        currentLayout.applyTo(binding.assetPrintLabel)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text =
                getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text =
                getString(R.string.search_options)
        }

        if (panelTopIsExpanded) binding.expandTopPanelButton.text =
            getString(R.string.collapse_panel)
        else binding.expandTopPanelButton.text = getString(R.string.label_print)
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded)
                    nextLayout.load(
                        this,
                        R.layout.asset_print_label_activity_bottom_panel_collapsed
                    )
                else
                    nextLayout.load(this, R.layout.asset_print_label_activity_both_panels_collapsed)
            } else
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_print_label_activity)
                else nextLayout.load(this, R.layout.asset_print_label_activity_top_panel_collapsed)

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

            TransitionManager.beginDelayedTransition(binding.assetPrintLabel, transition)
            nextLayout.applyTo(binding.assetPrintLabel)

            if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text =
                getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text = getString(R.string.search_options)
        }
    }

    private fun setTopPanelAnimation() {
        binding.expandTopPanelButton.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (panelBottomIsExpanded) {
                    if (panelTopIsExpanded) nextLayout.load(
                        this,
                        R.layout.asset_print_label_activity_top_panel_collapsed
                    )
                    else nextLayout.load(this, R.layout.asset_print_label_activity)
                } else if (panelTopIsExpanded)
                    nextLayout.load(this, R.layout.asset_print_label_activity_both_panels_collapsed)
                else {
                    nextLayout.load(
                        this,
                        R.layout.asset_print_label_activity_bottom_panel_collapsed
                    )
                }
            } else if (panelTopIsExpanded) nextLayout.load(
                this,
                R.layout.asset_print_label_activity_top_panel_collapsed
            )
            else nextLayout.load(this, R.layout.asset_print_label_activity)

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

            TransitionManager.beginDelayedTransition(binding.assetPrintLabel, transition)
            nextLayout.applyTo(binding.assetPrintLabel)

            if (panelTopIsExpanded) binding.expandTopPanelButton.text =
                getString(R.string.collapse_panel)
            else binding.expandTopPanelButton.text = getString(R.string.label_print)
        }
    }

    private fun refreshTextViews() {
        runOnUiThread {
            printerFragment?.refreshViews()
            assetSelectFilterFragment?.refreshViews()
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefreshAsset.isRefreshing = show
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

    private fun fillListView() {
        thread {
            val sync = GetAssetAsync()
            sync.addParams(this)
            sync.addExtraParams(
                code = assetSelectFilterFragment?.itemCode ?: "",
                itemCategory = assetSelectFilterFragment?.itemCategory,
                warehouseArea = assetSelectFilterFragment?.warehouseArea,
                onlyActive = assetSelectFilterFragment?.onlyActive ?: true
            )
            sync.execute()
        }
    }

    private fun fillAdapter(assetArray: ArrayList<Asset>?) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (arrayAdapter != null) {
                    lastSelected = arrayAdapter?.currentAsset()
                    firstVisiblePos = arrayAdapter?.firstVisiblePos()
                }

                if (arrayAdapter == null || assetArray != null) {
                    arrayAdapter = AssetAdapter(
                        activity = this,
                        resource = R.layout.asset_row,
                        assets = assetArray ?: ArrayList(),
                        suggestedList = ArrayList(),
                        listView = binding.assetListView,
                        multiSelect = multiSelect,
                        checkedIdArray = checkedIdArray,
                        visibleStatus = assetSelectFilterFragment?.visibleStatusArray
                            ?: AssetStatus.getAll()
                    )
                    refreshAdapterListeners()
                } else {
                    refreshAdapterListeners()
                    arrayAdapter?.refresh()
                }

                arrayAdapter?.refreshFilter(searchText, true)

                while (binding.assetListView.adapter == null) {
                    // Horrible wait for full load
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    arrayAdapter?.setSelectItemAndScrollPos(lastSelected, firstVisiblePos)
                }, 120)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            Statics.closeKeyboard(this)
            showProgressBar(false)
        }
    }

    private fun refreshAdapterListeners() {
        // IMPORTANTE:
        // Se deben actualizar los listeners, sino
        // las variables de esta actividad pueden
        // tener valores antiguos en del adaptador.

        arrayAdapter?.refreshListeners(
            checkedChangedListener = this,
            dataSetChangedListener = this,
            editAssetRequiredListener = this
        )

        if (Statics.useImageControl) {
            arrayAdapter?.refreshImageControlListeners(
                this,
                this
            )
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        Statics.closeKeyboard(this)
        refreshTextViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT))
            JotterListener.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)

        try {
            // Nada que hacer, volver
            if (scanCode.trim().isEmpty()) {
                val res = getString(R.string.invalid_code)
                makeText(binding.root, res, SnackbarType.ERROR)
                ErrorLog.writeLog(this, this::class.java.simpleName, res)
                return
            }

            val sc = ScannedCode(this).getFromCode(
                code = scanCode,
                searchWarehouseAreaId = false,
                searchAssetCode = true,
                searchAssetSerial = true,
                validateId = true
            )

            val asset = if (sc.asset != null) {
                sc.asset
            } else {
                val res = this.getString(R.string.invalid_asset_code)
                makeText(binding.root, res, SnackbarType.ERROR)
                null
            }

            if (asset != null) {
                arrayAdapter?.selectItem(asset)
                printerFragment?.printAsset(arrayListOf(asset))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackbarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    override fun onBackPressed() {
        Statics.closeKeyboard(this)

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

        if (!isRfidRequired()) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        val drawable =
            ContextCompat.getDrawable(Statics.AssetControl.getContext(), R.drawable.ic_visibility)
        val toolbar = findViewById<Toolbar>(R.id.action_bar)
        toolbar.overflowIcon = drawable

        // Opciones de visibilidad del menú
        for (i in AssetStatus.getAll()) {
            menu.add(
                0,
                i.id,
                i.id,
                i.description
            )
                .setChecked(
                    (assetSelectFilterFragment?.visibleStatusArray
                        ?: AssetStatus.getAll()).contains(i)
                )
                .isCheckable = true
        }

        //region Icon colors
        val gray = ResourcesCompat.getColor(resources, R.color.whitesmoke, null)
        val seagreen = ResourcesCompat.getColor(resources, R.color.seagreen, null)
        val firebrick = ResourcesCompat.getColor(resources, R.color.firebrick, null)
        val gold = ResourcesCompat.getColor(resources, R.color.gold, null)

        val colors: ArrayList<Int> = ArrayList()
        colors.add(gray)          // unknown
        colors.add(seagreen)      // onInventory
        colors.add(gold)          // removed
        colors.add(firebrick)     // missing
        //endregion Icon colors

        for (i in AssetStatus.getAll()) {
            val icon = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources,
                R.drawable.ic_lens,
                null
            )

            icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    colors[i.id],
                    BlendModeCompat.SRC_IN
                )

            val item = menu.findItem(i.id)
            item.icon = icon

            // Keep the popup menu open
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
            item.actionView = View(this)
            item.setOnActionExpandListener(object :
                MenuItem.OnActionExpandListener {
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
        if (assetSelectFilterFragment == null) {
            return false
        }

        val visibleStatus = assetSelectFilterFragment!!.visibleStatusArray
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetStatus.onInventory.id ->
                if (item.isChecked && !visibleStatus.contains(AssetStatus.onInventory)) {
                    arrayAdapter?.addVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.add(AssetStatus.onInventory)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.onInventory)) {
                    arrayAdapter?.removeVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.remove(AssetStatus.onInventory)
                }
            AssetStatus.missing.id ->
                if (item.isChecked && !visibleStatus.contains(AssetStatus.missing)) {
                    arrayAdapter?.addVisibleStatus(AssetStatus.missing)
                    visibleStatus.add(AssetStatus.missing)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.missing)) {
                    arrayAdapter?.removeVisibleStatus(AssetStatus.missing)
                    visibleStatus.remove(AssetStatus.missing)
                }
            AssetStatus.removed.id ->
                if (item.isChecked && !visibleStatus.contains(AssetStatus.removed)) {
                    arrayAdapter?.addVisibleStatus(AssetStatus.removed)
                    visibleStatus.add(AssetStatus.removed)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.removed)) {
                    arrayAdapter?.removeVisibleStatus(AssetStatus.removed)
                    visibleStatus.remove(AssetStatus.removed)
                }
            AssetStatus.unknown.id ->
                if (item.isChecked && !visibleStatus.contains(AssetStatus.unknown)) {
                    arrayAdapter?.addVisibleStatus(AssetStatus.unknown)
                    visibleStatus.add(AssetStatus.unknown)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.unknown)) {
                    arrayAdapter?.removeVisibleStatus(AssetStatus.unknown)
                    visibleStatus.remove(AssetStatus.unknown)
                }
            else -> return super.onOptionsItemSelected(item)
        }

        if (arrayAdapter?.isStatusVisible(arrayAdapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            arrayAdapter?.selectNearVisible()
        }

        assetSelectFilterFragment!!.visibleStatusArray = visibleStatus

        return true
    }

    override fun onGetAssetProgress(
        msg: String,
        progressStatus: ProgressStatus,
        completeList: ArrayList<Asset>,
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
                makeText(binding.root, msg, SnackbarType.ERROR)
            }
            ProgressStatus.finished -> {
                showProgressBar(false)
                this.completeList = completeList
                fillAdapter(completeList)
            }
        }
    }

    override fun onFilterChanged(
        code: String,
        itemCategory: ItemCategory?,
        warehouseArea: WarehouseArea?,
        onlyActive: Boolean,
    ) {
        Statics.closeKeyboard(this)

        if (fixedItemList) return

        if (code.isEmpty() &&
            itemCategory == null &&
            warehouseArea == null
        ) {
            // Limpiar el control
            completeList.clear()
            arrayAdapter?.clear()
            return
        }

        fillListView()
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
                    arrayAdapter?.currentAsset()?.saveChanges()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

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
        images: ArrayList<Images>,
    ): ArrayList<DocumentContentObject> {
        val list: ArrayList<DocumentContentObject> = ArrayList()
        for (i in images) {
            val x = DocumentContentObject()

            x.description = i.description
            x.reference = i.reference
            x.obs = i.obs
            x.filename_original = i.filenameOriginal
            x.status_object_id = StatusObject.Waiting.statusObjectId.toInt()
            x.status_str = StatusObject.Waiting.description
            x.status_date = com.dacosys.assetControl.utils.UTCDataTime.getUTCDateTimeAsString()

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

    private fun showPhotoAlbum(images: ArrayList<DocumentContentObject> = ArrayList()) {
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
                (docContReqResObj.documentContentArray ?: arrayOf()).isEmpty()
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
                    getString(R.string.images_not_yet_processed),
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

    override fun onFilterChanged(printer: String, template: BarcodeLabelCustom?, qty: Int?) {}

    override fun onPrintRequested(printer: String, template: BarcodeLabelCustom, qty: Int) {
        printerFragment?.printAssetById(arrayAdapter?.getAllChecked() ?: ArrayList())
    }

    private var qtyTextViewFocused: Boolean = false
    override fun onQtyTextViewFocusChanged(hasFocus: Boolean) {
        qtyTextViewFocused = hasFocus

        // Si el control de ingreso de cantidades del fragmento de impresión pierde el foco, pero
        // el foco pasa al control de texto de búsqueda, se debe colapsar el panel de impresión
        // manualmente porque no se dispara el evento de cambio de visibilidad del teclado en
        // pantalla que lo haría normalmente.
        if (Statics.isKeyboardVisible() && !hasFocus && binding.searchEditText.isFocused)
            collapseTopPanel()

        if (Statics.isKeyboardVisible() && hasFocus && panelBottomIsExpanded)
            collapseBottomPanel()
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

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        val asset = arrayAdapter?.currentAsset()

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
                    arrayAdapter!!.updateAsset(a, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }
}