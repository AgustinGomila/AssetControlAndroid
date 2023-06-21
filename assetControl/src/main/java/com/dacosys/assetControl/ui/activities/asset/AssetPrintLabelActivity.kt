package com.dacosys.assetControl.ui.activities.asset

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
import android.view.*
import android.view.View.GONE
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.asset.AssetRecyclerAdapter
import com.dacosys.assetControl.adapters.asset.AssetRecyclerAdapter.FilterOptions
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.barcode.BarcodeLabelCustomDbHelper
import com.dacosys.assetControl.databinding.AssetPrintLabelActivityTopPanelCollapsedBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.asset.asset.async.GetAssetAsync
import com.dacosys.assetControl.model.barcode.BarcodeLabelCustom
import com.dacosys.assetControl.model.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.model.category.ItemCategory
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.fragments.asset.AssetSelectFilterFragment
import com.dacosys.assetControl.ui.fragments.print.PrinterFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.isKeyboardVisible
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetLong
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutBoolean
import com.dacosys.assetControl.utils.preferences.Repository.Companion.useImageControl
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.imageControl.moshi.DocumentContent
import com.dacosys.imageControl.moshi.DocumentContentRequestResult
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.network.download.GetImages.Companion.toDocumentContentList
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.ui.activities.ImageControlCameraActivity
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import org.parceler.Parcels
import kotlin.concurrent.thread

class AssetPrintLabelActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    Scanner.ScannerListener, Rfid.RfidDeviceListener, AssetSelectFilterFragment.FragmentListener,
    GetAssetAsync.GetAssetAsyncListener, PrinterFragment.FragmentListener, AssetRecyclerAdapter.CheckedChangedListener,
    AssetRecyclerAdapter.DataSetChangedListener, AssetRecyclerAdapter.AddPhotoRequiredListener,
    AssetRecyclerAdapter.AlbumViewRequiredListener,
    AssetRecyclerAdapter.EditAssetRequiredListener {
    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private var isFinishingByUser = false
    private fun destroyLocals() {
        // Borramos los Ids temporales que se usaron en la actividad.
        if (isFinishingByUser) AssetDbHelper().deleteTemp()

        recyclerAdapter?.refreshListeners(null, null, null)
        recyclerAdapter?.refreshImageControlListeners(null, null)
        assetSelectFilterFragment?.onDestroy()
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
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                fillSummaryRow()
            }
        }, 100)
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        runOnUiThread {
            binding.selectedTextView.text = recyclerAdapter?.countChecked().toString()
        }
    }


    private var tempTitle = ""

    private var rejectNewInstances = false

    // Se usa para saber si estamos en onStart luego de onCreate
    private var fillRequired = false

    // Configuración guardada de los controles que se ven o no se ven
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var onlyActive: Boolean = true
    private var multiSelect = false
    private var recyclerAdapter: AssetRecyclerAdapter? = null
    private var lastSelected: Asset? = null
    private var currentScrollPosition: Int = 0
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

    private val menuItemShowImages = 9999
    private var showImages
        get() = prefsGetBoolean(Preference.printLabelAssetShowImages)
        set(value) {
            prefsPutBoolean(Preference.printLabelAssetShowImages.key, value)
        }

    private var showCheckBoxes
        get() =
            if (!multiSelect) false
            else prefsGetBoolean(Preference.printLabelAssetShowCheckBoxes)
        set(value) {
            prefsPutBoolean(Preference.printLabelAssetShowCheckBoxes.key, value)
        }

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

        if (recyclerAdapter != null) {
            b.putParcelable("lastSelected", recyclerAdapter?.currentAsset())
            b.putInt("firstVisiblePos", recyclerAdapter?.firstVisiblePos() ?: 0)
            b.putInt("currentScrollPosition", currentScrollPosition)
            b.putLongArray("checkedIdArray", recyclerAdapter?.checkedIdArray?.map { it }?.toLongArray())
        }

        // Guardar en la DB temporalmente los ítems listados
        AssetDbHelper().insertTempId(ArrayList(recyclerAdapter?.fullList?.map { it.assetId } ?: listOf()))

        b.putString("searchText", searchText)
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (!t1.isNullOrEmpty()) t1 else getString(R.string.select_asset)
        // endregion

        // PANELS
        if (b.containsKey("hideFilterPanel")) hideFilterPanel = b.getBoolean("hideFilterPanel", hideFilterPanel)
        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded")) panelTopIsExpanded = b.getBoolean("panelTopIsExpanded")

        // ADAPTER
        if (b.containsKey("onlyActive")) onlyActive = b.getBoolean("onlyActive")

        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.getParcelable("lastSelected")
        currentScrollPosition = b.getInt("currentScrollPosition")
        checkedIdArray = (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        fixedItemList = b.getBoolean("fixedItemList")

        if (b.containsKey("assetArray")) {
            completeList = b.getParcelableArrayList("assetArray") ?: ArrayList()
            fixedItemList = true
            hideFilterPanel = true
        }
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.select_asset)

        val id = prefsGetLong(Preference.defaultBarcodeLabelCustomAsset)
        val blc = BarcodeLabelCustomDbHelper().selectById(id)
        if (blc != null) {
            printerFragment?.barcodeLabelCustom = blc
        }
    }

    private lateinit var binding: AssetPrintLabelActivityTopPanelCollapsedBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetPrintLabelActivityTopPanelCollapsedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        assetSelectFilterFragment =
            supportFragmentManager.findFragmentById(binding.filterFragment.id) as AssetSelectFilterFragment
        printerFragment =
            supportFragmentManager.findFragmentById(binding.printFragment.id) as PrinterFragment

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)

            searchText = savedInstanceState.getString("searchText") ?: ""

            // Cargar la lista desde la DB local
            completeList = AssetDbHelper().selectTempId()
        } else {
            // Borramos los Ids temporales al crear la actividad por primera vez.
            AssetDbHelper().deleteTemp()

            val extras = intent.extras
            if (extras != null) loadBundleValues(extras) else loadDefaultValues()
        }

        title = tempTitle

        assetSelectFilterFragment?.setListener(this)
        assetSelectFilterFragment?.onlyActive = onlyActive

        printerFragment?.setListener(this)
        printerFragment?.barcodeLabelTarget = BarcodeLabelTarget.Asset

        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.setColorSchemeResources(
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

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchText = s.toString()
                recyclerAdapter?.refreshFilter(FilterOptions(searchText, true))
            }
        })
        binding.searchEditText.setText(
            searchText, TextView.BufferType.EDITABLE
        )
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

        setPanels()

        fillRequired = true

        setupUI(binding.root, this)
    }

    // region Inset animation
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupWindowInsetsAnimation()
    }

    private var isKeyboardVisible: Boolean = false

    /**
     * Change panels state at Ime animation finish
     *
     * Estados que recuerdan está pendiente de ejecutar un cambio en estado (colapsado/expandido) de los paneles al
     * terminar la animación de mostrado/ocultamiento del teclado en pantalla. Esto es para sincronizar los cambios,
     * ejecutándolos de manera secuencial. A ojos del usuario la vista completa acompaña el desplazamiento de la
     * animación. Si se ejecutara al mismo tiempo el cambio en los paneles y la animación del teclado la vista no
     * acompaña correctamente al teclado, ya que cambia durante la animación.
     */
    private var changePanelTopStateAtFinish: Boolean = false
    private var changePanelBottomStateAtFinish: Boolean = false

    private fun setupWindowInsetsAnimation() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootView = binding.root

        // Adjust root layout to bottom navigation bar
        val windowInsets = window.decorView.rootWindowInsets
        @Suppress("DEPRECATION") rootView.setPadding(
            windowInsets.systemWindowInsetLeft,
            windowInsets.systemWindowInsetTop,
            windowInsets.systemWindowInsetRight,
            windowInsets.systemWindowInsetBottom
        )

        implWindowInsetsAnimation()
    }

    private fun implWindowInsetsAnimation() {
        val rootView = binding.root

        ViewCompat.setWindowInsetsAnimationCallback(
            rootView,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    val isIme = animation.typeMask and WindowInsetsCompat.Type.ime() != 0
                    if (!isIme) return

                    postExecuteImeAnimation()
                    super.onEnd(animation)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    paddingBottomView(rootView, insets)

                    return insets
                }
            })
    }

    private fun paddingBottomView(rootView: ConstraintLayout, insets: WindowInsetsCompat) {
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val paddingBottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)

        isKeyboardVisible = imeInsets.bottom > 0

        rootView.setPadding(
            rootView.paddingLeft,
            rootView.paddingTop,
            rootView.paddingRight,
            paddingBottom
        )

        Log.d(javaClass.simpleName, "IME Size: ${imeInsets.bottom}")
    }

    private fun postExecuteImeAnimation() {
        // Si estamos mostrando el teclado, colapsamos los paneles.
        if (isKeyboardVisible) {
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

        // Si estamos esperando que termine la animación para ejecutar un cambio de vista
        if (changePanelTopStateAtFinish) {
            changePanelTopStateAtFinish = false
            binding.expandTopPanelButton.performClick()
        }
        if (changePanelBottomStateAtFinish) {
            changePanelBottomStateAtFinish = false
            binding.expandBottomPanelButton?.performClick()
        }
    }
    // endregion

    override fun onStart() {
        super.onStart()

        if (fillRequired) {
            fillRequired = false
            fillAdapter(completeList)
        }
    }

    private fun itemSelect() {
        closeKeyboard(this)

        if (recyclerAdapter != null) {
            val data = Intent()

            val asset = recyclerAdapter?.currentAsset()
            val countChecked = recyclerAdapter?.countChecked() ?: 0
            var assetArray: java.util.ArrayList<Asset> = ArrayList()

            if (!multiSelect && asset != null) {
                data.putParcelableArrayListExtra("ids", arrayListOf(ParcelLong(asset.assetId)))
                setResult(RESULT_OK, data)
            } else if (multiSelect) {
                if (countChecked > 0 || asset != null) {
                    if (countChecked > 0) assetArray = recyclerAdapter?.getAllChecked() ?: ArrayList()
                    else if (recyclerAdapter?.showCheckBoxes == false) {
                        assetArray = arrayListOf(asset!!)
                    }
                    data.putParcelableArrayListExtra(
                        "ids",
                        assetArray.map { ParcelLong(it.assetId) } as ArrayList<ParcelLong>)
                    setResult(RESULT_OK, data)
                } else {
                    setResult(RESULT_CANCELED)
                }
            } else {
                setResult(RESULT_CANCELED)
            }
        } else {
            setResult(RESULT_CANCELED)
        }

        isFinishingByUser = true
        finish()
    }

    private fun fillSummaryRow() {
        runOnUiThread {
            if (multiSelect) {
                binding.totalLabelTextView.text = getString(R.string.total)
                binding.selectedLabelTextView.text = getString(R.string.checked)

                if (recyclerAdapter != null) {
                    binding.totalTextView.text = recyclerAdapter?.totalVisible().toString()
                    binding.selectedTextView.text = recyclerAdapter?.countChecked().toString()
                }
            } else {
                binding.totalLabelTextView.text = getString(R.string.total)
                binding.selectedLabelTextView.text = getString(R.string.assets)

                if (recyclerAdapter != null) {
                    binding.totalTextView.text = recyclerAdapter?.totalVisible().toString()
                    binding.selectedTextView.text = recyclerAdapter?.totalVisible().toString()
                }
            }

            if (recyclerAdapter == null) {
                binding.totalTextView.text = 0.toString()
                binding.selectedTextView.text = 0.toString()
            }
        }
    }

    private fun setPanels() {
        val currentLayout = ConstraintSet()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) currentLayout.load(this, R.layout.asset_print_label_activity)
                else currentLayout.load(this, R.layout.asset_print_label_activity_top_panel_collapsed)
            } else {
                if (panelTopIsExpanded) currentLayout.load(
                    this,
                    R.layout.asset_print_label_activity_bottom_panel_collapsed
                )
                else currentLayout.load(this, R.layout.asset_print_label_activity_both_panels_collapsed)
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
            else binding.expandBottomPanelButton?.text = getString(R.string.search_options)
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
            val bottomVisible = panelBottomIsExpanded
            val imeVisible = isKeyboardVisible

            if (!bottomVisible && imeVisible) {
                // Esperar que se cierre el teclado luego de perder el foco el TextView para expandir el panel
                changePanelBottomStateAtFinish = true
                return@setOnClickListener
            }

            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(
                    this,
                    R.layout.asset_print_label_activity_bottom_panel_collapsed
                )
                else nextLayout.load(this, R.layout.asset_print_label_activity_both_panels_collapsed)
            } else if (panelTopIsExpanded) {
                nextLayout.load(this, R.layout.asset_print_label_activity)
            } else {
                nextLayout.load(this, R.layout.asset_print_label_activity_top_panel_collapsed)
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

            TransitionManager.beginDelayedTransition(binding.assetPrintLabel, transition)
            nextLayout.applyTo(binding.assetPrintLabel)

            if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text =
                getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text = getString(R.string.search_options)
        }
    }

    private fun setTopPanelAnimation() {
        binding.expandTopPanelButton.setOnClickListener {
            val topVisible = panelTopIsExpanded
            val imeVisible = isKeyboardVisible

            if (!topVisible && imeVisible) {
                // Esperar que se cierre el teclado luego de perder el foco el TextView para expandir el panel
                changePanelTopStateAtFinish = true
                return@setOnClickListener
            }

            val nextLayout = ConstraintSet()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (panelBottomIsExpanded) {
                    if (panelTopIsExpanded) nextLayout.load(
                        this,
                        R.layout.asset_print_label_activity_top_panel_collapsed
                    )
                    else nextLayout.load(this, R.layout.asset_print_label_activity)
                } else if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.asset_print_label_activity_both_panels_collapsed)
                } else {
                    nextLayout.load(this, R.layout.asset_print_label_activity_bottom_panel_collapsed)
                }
            } else if (panelTopIsExpanded) {
                nextLayout.load(this, R.layout.asset_print_label_activity_top_panel_collapsed)
            } else {
                nextLayout.load(this, R.layout.asset_print_label_activity)
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
            binding.swipeRefresh.isRefreshing = show
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
                if (recyclerAdapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = recyclerAdapter?.currentAsset()
                }

                if (recyclerAdapter == null || assetArray != null) {
                    recyclerAdapter = AssetRecyclerAdapter(
                        recyclerView = binding.recyclerView,
                        visibleStatus = assetSelectFilterFragment?.visibleStatusArray ?: AssetStatus.getAll(),
                        fullList = assetArray ?: ArrayList(),
                        checkedIdArray = checkedIdArray,
                        multiSelect = multiSelect,
                        showCheckBoxes = showCheckBoxes,
                        showCheckBoxesChanged = { showCheckBoxes = it },
                        showImages = showImages,
                        showImagesChanged = { showImages = it },
                        filterOptions = FilterOptions(searchText, true)
                    )
                }

                refreshAdapterListeners()

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = recyclerAdapter

                while (binding.recyclerView.adapter == null) {
                    // Horrible wait for full load
                }

                // Estas variables locales evitar posteriores cambios de estado.
                val ls = lastSelected
                val cs = currentScrollPosition
                Handler(Looper.getMainLooper()).postDelayed({
                    recyclerAdapter?.selectItem(ls, false)
                    recyclerAdapter?.scrollToPos(cs, true)
                }, 200)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            closeKeyboard(this)
            showProgressBar(false)
        }
    }

    private fun refreshAdapterListeners() {
        // IMPORTANTE:
        // Se deben actualizar los listeners, si no
        // las variables de esta actividad pueden
        // tener valores antiguos en del adaptador.

        recyclerAdapter?.refreshListeners(
            checkedChangedListener = this,
            dataSetChangedListener = this,
            editAssetRequiredListener = this
        )

        if (useImageControl) {
            recyclerAdapter?.refreshImageControlListeners(
                addPhotoListener = this,
                albumViewListener = this
            )
        }
    }

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
                makeText(binding.root, res, SnackBarType.ERROR)
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
                makeText(binding.root, res, SnackBarType.ERROR)
                null
            }

            if (asset != null) {
                recyclerAdapter?.selectItem(asset)
                printerFragment?.printAsset(arrayListOf(asset))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    override fun onBackPressed() {
        closeKeyboard(this)

        isFinishingByUser = true
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

        // Opción de visibilidad de Imágenes
        if (useImageControl) {
            menu.add(Menu.NONE, menuItemShowImages, menu.size, getContext().getString(R.string.show_images))
                .setChecked(showImages).isCheckable = true
            val item = menu.findItem(menuItemShowImages)
            if (showImages)
                item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_photo_library)
            else
                item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_hide_image)
            item.icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                )
        }

        val menuItems = menu.size()

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        // Opciones de visibilidad del menú
        val allStatus = AssetStatus.getAll()
        val s = assetSelectFilterFragment?.visibleStatusArray ?: AssetStatus.getAll()
        for (i in 0 until allStatus.size) {
            menu.add(0, allStatus[i].id, i + menuItems, allStatus[i].description)
                .setChecked(s.contains(allStatus[i])).isCheckable = true
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

        for (i in 0 until allStatus.size) {
            val icon = ContextCompat.getDrawable(this, R.drawable.ic_lens)
            icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    colors[i], BlendModeCompat.SRC_IN
                )

            val item = menu.getItem(i + menuItems)
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
        if (assetSelectFilterFragment == null) {
            return false
        }

        val visibleStatus = assetSelectFilterFragment!!.visibleStatusArray
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetStatus.onInventory.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.onInventory)) {
                    recyclerAdapter?.addVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.add(AssetStatus.onInventory)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.onInventory)) {
                    recyclerAdapter?.removeVisibleStatus(AssetStatus.onInventory)
                    visibleStatus.remove(AssetStatus.onInventory)
                }
            }

            AssetStatus.missing.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.missing)) {
                    recyclerAdapter?.addVisibleStatus(AssetStatus.missing)
                    visibleStatus.add(AssetStatus.missing)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.missing)) {
                    recyclerAdapter?.removeVisibleStatus(AssetStatus.missing)
                    visibleStatus.remove(AssetStatus.missing)
                }
            }

            AssetStatus.removed.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.removed)) {
                    recyclerAdapter?.addVisibleStatus(AssetStatus.removed)
                    visibleStatus.add(AssetStatus.removed)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.removed)) {
                    recyclerAdapter?.removeVisibleStatus(AssetStatus.removed)
                    visibleStatus.remove(AssetStatus.removed)
                }
            }

            AssetStatus.unknown.id -> {
                if (item.isChecked && !visibleStatus.contains(AssetStatus.unknown)) {
                    recyclerAdapter?.addVisibleStatus(AssetStatus.unknown)
                    visibleStatus.add(AssetStatus.unknown)
                } else if (!item.isChecked && visibleStatus.contains(AssetStatus.unknown)) {
                    recyclerAdapter?.removeVisibleStatus(AssetStatus.unknown)
                    visibleStatus.remove(AssetStatus.unknown)
                }
            }

            menuItemShowImages -> {
                recyclerAdapter?.showImages(item.isChecked)
                if (item.isChecked)
                    item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_photo_library)
                else
                    item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_hide_image)
                item.icon?.mutate()?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                    )
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
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
                makeText(binding.root, msg, SnackBarType.ERROR)
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
        closeKeyboard(this)

        if (fixedItemList) return

        if (code.isEmpty() && itemCategory == null && warehouseArea == null) {
            // Limpiar el control
            completeList.clear()
            recyclerAdapter?.clear()
            return
        }

        fillListView()
    }

    override fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String) {
        if (!useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, ImageControlCameraActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("programObjectId", tableId.toLong())
            intent.putExtra("objectId1", itemId.toString())
            intent.putExtra("description", description)
            intent.putExtra("addPhoto", autoSend())
            resultForPhotoCapture.launch(intent)
        }
    }

    private val resultForPhotoCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val asset = recyclerAdapter?.currentAsset() ?: return@registerForActivityResult
                    asset.saveChanges()
                    val pos = recyclerAdapter?.getIndexById(asset.assetId) ?: RecyclerView.NO_POSITION
                    recyclerAdapter?.notifyItemChanged(pos)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    override fun onAlbumViewRequired(tableId: Int, itemId: Long) {
        if (!useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            tempObjectId = itemId.toString()
            tempTableId = tableId

            val programData = ProgramData(
                programObjectId = tempTableId.toLong(),
                objId1 = tempObjectId
            )

            ImageCoroutines().get(programData = programData) {
                val allLocal = toDocumentContentList(images = it, programData = programData)
                if (allLocal.isEmpty()) {
                    getFromWebservice()
                } else {
                    showPhotoAlbum(allLocal)
                }
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
        intent.putExtra("programObjectId", tempTableId.toLong())
        intent.putExtra("objectId1", tempObjectId)
        intent.putExtra("docContObjArrayList", images)
        startActivity(intent)
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
                binding.root, getString(R.string.images_not_yet_processed), SnackBarType.INFO
            )
            rejectNewInstances = false
            return
        }

        showPhotoAlbum()
    }

    override fun onFilterChanged(printer: String, template: BarcodeLabelCustom?, qty: Int?) {}

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

        val asset = recyclerAdapter?.currentAsset()
        val countChecked = recyclerAdapter?.countChecked() ?: 0
        var assetArray: java.util.ArrayList<Asset> = ArrayList()

        if (!multiSelect && asset != null) {
            assetArray = arrayListOf(asset)
        } else if (multiSelect) {
            if (countChecked > 0 || asset != null) {
                if (countChecked > 0) assetArray = recyclerAdapter?.getAllChecked() ?: ArrayList()
                else if (recyclerAdapter?.showCheckBoxes == false) {
                    assetArray = arrayListOf(asset!!)
                }
            }
        }

        if (assetArray.isNotEmpty()) printerFragment?.printAssetById(ArrayList(assetArray.map { it.assetId }))
    }

    private var qtyTextViewFocused: Boolean = false
    override fun onQtyTextViewFocusChanged(hasFocus: Boolean) {
        qtyTextViewFocused = hasFocus

        // Si el control de ingreso de cantidades del fragmento de impresión pierde el foco, pero
        // el foco pasa al control de texto de búsqueda, se debe colapsar el panel de impresión
        // manualmente porque no se dispara el evento de cambio de visibilidad del teclado en
        // pantalla que lo haría normalmente.
        if (isKeyboardVisible() && !hasFocus && binding.searchEditText.isFocused) collapseTopPanel()

        if (isKeyboardVisible() && hasFocus && panelBottomIsExpanded) collapseBottomPanel()
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
        val asset = recyclerAdapter?.currentAsset()

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
                    recyclerAdapter?.updateAsset(a, true)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }
}