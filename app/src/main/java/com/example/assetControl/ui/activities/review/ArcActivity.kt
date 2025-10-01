package com.example.assetControl.ui.activities.review

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.example.assetControl.data.async.review.SaveReview
import com.example.assetControl.data.async.review.StartReview
import com.example.assetControl.data.enums.asset.AssetCondition
import com.example.assetControl.data.enums.asset.AssetStatus
import com.example.assetControl.data.enums.asset.OwnershipStatus
import com.example.assetControl.data.enums.common.ConfirmStatus
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.review.AssetReviewContentStatus
import com.example.assetControl.data.enums.review.AssetReviewStatus
import com.example.assetControl.data.enums.review.StartReviewProgress
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.repository.asset.AssetRepository
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.example.assetControl.data.room.repository.review.TempReviewContentRepository
import com.example.assetControl.databinding.AssetReviewContentBottomPanelCollapsedBinding
import com.example.assetControl.databinding.ProgressBarDialogBinding
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
import com.example.assetControl.ui.activities.maintenance.AssetMaintenanceConditionActivity
import com.example.assetControl.ui.adapters.interfaces.Interfaces
import com.example.assetControl.ui.adapters.interfaces.Interfaces.AdapterProgress
import com.example.assetControl.ui.adapters.review.ArcRecyclerAdapter
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.SUCCESS
import com.example.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.parcel.ParcelLong
import com.example.assetControl.utils.parcel.Parcelables.parcelable
import com.example.assetControl.utils.parcel.Parcelables.parcelableArrayList
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetStringSet
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutBoolean
import com.example.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutStringSet
import com.example.assetControl.utils.settings.preferences.Repository.Companion.useImageControl
import com.example.assetControl.viewModel.review.SaveReviewViewModel
import com.example.assetControl.viewModel.sync.SyncViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.parceler.Parcels
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.thread
import com.dacosys.imageControl.network.common.ProgressStatus as IcProgressStatus

@Suppress("UNCHECKED_CAST")
class ArcActivity : AppCompatActivity(), Scanner.ScannerListener,
    Rfid.RfidDeviceListener, Interfaces.CheckedChangedListener,
    Interfaces.DataSetChangedListener, SwipeRefreshLayout.OnRefreshListener,
    Interfaces.EditAssetRequiredListener, Interfaces.AlbumViewRequiredListener,
    Interfaces.AddPhotoRequiredListener, Interfaces.UiEventListener {
    override fun onDestroy() {
        saveSharedPreferences()
        destroyLocals()
        super.onDestroy()
    }

    private fun saveSharedPreferences() {
        prefsPutBoolean(Preference.assetReviewAddUnknownAssets.key, binding.addUnknownAssetsSwitch.isChecked)
        prefsPutBoolean(Preference.assetReviewAllowUnknownCodes.key, binding.allowUnknownCodesSwitch.isChecked)
        prefsPutStringSet(
            Preference.assetReviewContentVisibleStatus.key, (adapter?.visibleStatus ?: ArrayList())
                .map { it.id.toString() }
                .toSet())
    }

    private var isFinishingByUser = false

    private fun destroyLocals() {
        // Borramos los Ids temporales que se usaron en la actividad.
        if (isFinishingByUser) TempReviewContentRepository().deleteAll()

        adapter?.refreshListeners()
        adapter?.refreshImageControlListeners()
        adapter?.refreshUiEventListener()
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 1000)
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = show
        }
    }

    private var currentInventory: ArrayList<String>? = null

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

    private var tempTitle = ""
    private var isNew: Boolean = false

    private var saving: Boolean = false

    // Flag que se utiliza la primera vez que se muestra la actividad
    private var _startReview = true
    private var _fillAdapter = false

    private var assetReview: AssetReview? = null
    private var completeList: ArrayList<AssetReviewContent> = ArrayList()

    private var adapter: ArcRecyclerAdapter? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var lastSelected: AssetReviewContent? = null
    private var currentScrollPosition: Int = 0
    private var firstVisiblePos: Int? = null

    private var visibleStatusArray: ArrayList<AssetReviewContentStatus> = ArrayList()
    private val allowQuickReview: Boolean by lazy { prefsGetBoolean(Preference.quickReviews) }

    private var unknownAssetId: Long = 0

    private var allowClicks = true
    private var rejectNewInstances = false

    private var headerFragment: LocationHeaderFragment? = null
    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

    private val menuItemShowImages = 9999
    private var showImages
        get() = prefsGetBoolean(Preference.reviewContentShowImages)
        set(value) {
            prefsPutBoolean(Preference.reviewContentShowImages.key, value)
        }

    private var showCheckBoxes
        get() =
            if (!allowQuickReview) false
            else prefsGetBoolean(Preference.reviewContentShowCheckBoxes)
        set(value) {
            prefsPutBoolean(Preference.reviewContentShowCheckBoxes.key, value)
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        saveBundleValues(outState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putBoolean("startReview", _startReview)

        b.putString("title", tempTitle)
        b.putParcelable("assetReview", Parcels.wrap(assetReview))
        b.putBoolean("isNew", isNew)

        b.putBoolean("saving", saving)

        b.putLong("unknownAssetId", unknownAssetId)

        b.putStringArrayList("currentInventory", currentInventory)

        b.putBoolean("allowUnknownCodes", binding.allowUnknownCodesSwitch.isChecked)
        b.putBoolean("addUnknownAssets", binding.addUnknownAssetsSwitch.isChecked)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        b.putParcelable("lastSelected", adapter?.currentItem())
        b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: RecyclerView.NO_POSITION)
        b.putLongArray("checkedIdArray", adapter?.checkedIdArray?.map { it }?.toLongArray())
        b.putInt("currentScrollPosition", currentScrollPosition)

        // Guardamos la revisión en una tabla temporal.
        // Revisiones de miles de artículos no pueden pasarse en el intent.
        TempReviewContentRepository().insert(
            arId = assetReview?.id ?: 0,
            contents = adapter?.fullList?.toList() ?: listOf()
        )
    }

    private fun loadBundleExtrasValues(b: Bundle) {
        assetReview = Parcels.unwrap<AssetReview>(b.parcelable("assetReview"))
        isNew = b.getBoolean("isNew")

        loadDefaultValues()
    }

    private fun loadBundleValues(b: Bundle) {
        _startReview = b.getBoolean("startReview")

        // region Recuperar el título de la ventana
        val t1 = b.getString("title") ?: ""
        tempTitle = t1.ifEmpty { getString(R.string.asset_review) }
        // endregion

        assetReview = Parcels.unwrap<AssetReview>(b.parcelable("assetReview"))
        isNew = b.getBoolean("isNew")
        saving = b.getBoolean("saving")

        if (b.containsKey("allowUnknownCodes")) binding.allowUnknownCodesSwitch.isChecked =
            b.getBoolean("allowUnknownCodes")
        else binding.allowUnknownCodesSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)

        if (b.containsKey("addUnknownAssets")) binding.addUnknownAssetsSwitch.isChecked =
            b.getBoolean("addUnknownAssets")
        else binding.addUnknownAssetsSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAddUnknownAssets)

        unknownAssetId = b.getLong("unknownAssetId")

        currentInventory = b.getStringArrayList("currentInventory")

        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded = b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded")) panelTopIsExpanded = b.getBoolean("panelTopIsExpanded")

        // Adapter
        checkedIdArray = (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())
        lastSelected = b.parcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1
        currentScrollPosition = b.getInt("currentScrollPosition")

        // Cargamos la revisión desde la tabla temporal
        completeList.clear()
        val tempCont = ArrayList(TempReviewContentRepository().selectByTempId(assetReview?.id ?: 0))
        if (tempCont.any()) completeList = tempCont

        visibleStatusArray.clear()
        if (b.containsKey("visibleStatusArray")) {
            val t3 = b.parcelableArrayList<AssetReviewContentStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            loadDefaultVisibleStatus()
        }

        _fillAdapter = true
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.asset_review)
        binding.allowUnknownCodesSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)
        binding.addUnknownAssetsSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAddUnknownAssets)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleStatusArray.clear()
        var set = prefsGetStringSet(
            Preference.assetReviewContentVisibleStatus.key,
            Preference.assetReviewContentVisibleStatus.defaultValue as ArrayList<String>
        )
        if (set == null) set = AssetReviewContentStatus.getAll().map { it.id.toString() }.toSet()

        for (i in set) {
            val status = AssetReviewContentStatus.getById(i.toInt())
            if (!visibleStatusArray.contains(status)) {
                visibleStatusArray.add(status)
            }
        }
    }

    private lateinit var binding: AssetReviewContentBottomPanelCollapsedBinding
    private val saveViewModel: SaveReviewViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = AssetReviewContentBottomPanelCollapsedBinding.inflate(layoutInflater)
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
        saveViewModel.startReviewProgress.observe(this) { if (it != null) onStartReviewProgress(it) }
        syncViewModel.syncUploadProgress.observe(this) { if (it != null) onSyncUploadProgress(it) }
        syncViewModel.uploadImagesProgress.observe(this) { if (it != null) onUploadImagesProgress(it) }

        headerFragment =
            supportFragmentManager.findFragmentById(binding.headerFragment.id) as LocationHeaderFragment?

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            val extras = intent.extras
            if (extras != null) loadBundleExtrasValues(extras) else loadDefaultValues()
        }

        title = tempTitle

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

        binding.okButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                finishAssetReview()
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

        binding.cancelButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                detailAsset()
            }
        }

        binding.allowUnknownCodesSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.addUnknownAssetsSwitch.isChecked = false
            }
        }
        binding.allowUnknownCodesSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)
        binding.allowUnknownAssetTextView.setOnClickListener { binding.allowUnknownCodesSwitch.performClick() }

        binding.addUnknownAssetsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.allowUnknownCodesSwitch.isChecked = true
            }
        }
        binding.addUnknownAssetsSwitch.isChecked = prefsGetBoolean(Preference.assetReviewAddUnknownAssets)
        binding.unknownAssetRegistrationTextView.setOnClickListener { binding.addUnknownAssetsSwitch.performClick() }

        binding.mantButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                maintenanceAsset()
            }
        }

        if (!prefsGetBoolean(Preference.useAssetControlManteinance)) {
            binding.mantButton.isEnabled = false
        }

        setHeaderTextBox()

        setPanels()
    }

    override fun onStart() {
        super.onStart()

        if (!saving && _startReview) {
            startReview()
        } else if (_fillAdapter) {
            _fillAdapter = false
            fillAdapter(completeList)
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
        closeKeyboard(this)
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
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT /*&& !qtyPrinterIsFocused*/ -> {
                    collapseBottomPanel()
                    collapseTopPanel()
                }

                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT /*&& !qtyPrinterIsFocused*/ -> {
                    collapseBottomPanel()
                }

                resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT /*&& !qtyPrinterIsFocused*/ -> {
                    collapseTopPanel()
                }
            }
        }

        // Si estamos esperando que termine la animación para ejecutar un cambio de vista
        if (changePanelTopStateAtFinish) {
            changePanelTopStateAtFinish = false
            binding.expandTopPanelButton?.performClick()
        }
        if (changePanelBottomStateAtFinish) {
            changePanelBottomStateAtFinish = false
            binding.expandBottomPanelButton?.performClick()
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
                binding.expandTopPanelButton?.performClick()
            }
        }
    }
    // endregion

    private fun setHeaderTextBox() {
        headerFragment?.showChangePostButton(false)
        headerFragment?.setTitle(context.getString(R.string.area_in_review))

        if (assetReview != null && headerFragment != null) {
            runOnUiThread {
                headerFragment?.fill((assetReview ?: return@runOnUiThread).warehouseAreaId)
            }
        }
    }

    private fun setPanels() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        val currentLayout = ConstraintSet()
        if (panelBottomIsExpanded) {
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.asset_review_content_activity)
            else currentLayout.load(this, R.layout.asset_review_content_top_panel_collapsed)
        } else {
            if (panelTopIsExpanded) currentLayout.load(this, R.layout.asset_review_content_bottom_panel_collapsed)
            else currentLayout.load(this, R.layout.asset_review_content_both_panels_collapsed)
        }

        val transition = ChangeBounds()
        transition.interpolator = FastOutSlowInInterpolator()
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionResume(transition: Transition?) {}
            override fun onTransitionPause(transition: Transition?) {}
            override fun onTransitionStart(transition: Transition?) {}
            override fun onTransitionEnd(transition: Transition?) {}
            override fun onTransitionCancel(transition: Transition?) {}
        })

        TransitionManager.beginDelayedTransition(binding.assetReviewContent, transition)
        currentLayout.applyTo(binding.assetReviewContent)

        if (panelBottomIsExpanded) binding.expandBottomPanelButton?.text = getString(R.string.expand_panel)
        else binding.expandBottomPanelButton?.text = getString(R.string.more_options)

        if (panelTopIsExpanded) binding.expandTopPanelButton?.text = getString(R.string.collapse_panel)
        else binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
    }

    private fun setBottomPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandBottomPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_bottom_panel_collapsed)
                else nextLayout.load(this, R.layout.asset_review_content_both_panels_collapsed)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_activity)
                else nextLayout.load(this, R.layout.asset_review_content_top_panel_collapsed)
            }

            panelBottomIsExpanded = !panelBottomIsExpanded
            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition?) {}
                override fun onTransitionPause(transition: Transition?) {}
                override fun onTransitionStart(transition: Transition?) {}
                override fun onTransitionEnd(transition: Transition?) {}
                override fun onTransitionCancel(transition: Transition?) {}
            })

            TransitionManager.beginDelayedTransition(binding.assetReviewContent, transition)
            nextLayout.applyTo(binding.assetReviewContent)

            if (panelBottomIsExpanded) (binding.expandBottomPanelButton ?: return@setOnClickListener).text =
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
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_top_panel_collapsed)
                else nextLayout.load(this, R.layout.asset_review_content_activity)
            } else {
                if (panelTopIsExpanded) nextLayout.load(this, R.layout.asset_review_content_both_panels_collapsed)
                else nextLayout.load(this, R.layout.asset_review_content_bottom_panel_collapsed)
            }

            panelTopIsExpanded = !panelTopIsExpanded

            val transition = ChangeBounds()
            transition.interpolator = FastOutSlowInInterpolator()
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionResume(transition: Transition?) {}
                override fun onTransitionPause(transition: Transition?) {}
                override fun onTransitionStart(transition: Transition?) {}
                override fun onTransitionEnd(transition: Transition?) {}
                override fun onTransitionCancel(transition: Transition?) {}
            })

            TransitionManager.beginDelayedTransition(binding.assetReviewContent, transition)
            nextLayout.applyTo(binding.assetReviewContent)

            if (panelTopIsExpanded) (binding.expandTopPanelButton ?: return@setOnClickListener).text =
                context.getString(R.string.collapse_panel)
            else binding.expandTopPanelButton?.text = context.getString(R.string.area_in_review)
        }
    }

    private fun cancelAssetReview() {
        ScannerManager.lockScanner(this, true)
        try {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(context.getString(R.string.cancel_review))
            alert.setMessage(context.getString(R.string.discard_changes_and_return_to_the_main_menu_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)

                isFinishingByUser = true
                setResult(RESULT_CANCELED)
                finish()
            }

            runOnUiThread {
                alert.show()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
            allowClicks = true
        }
    }

    private fun setupTextView() {
        val assetsMissed = adapter?.countItemsMissed ?: 0
        val assetsAdded = adapter?.countItemsAdded ?: 0
        val assetsRevised = adapter?.countItemsRevised ?: 0

        runOnUiThread {
            binding.missedTextView.text = assetsMissed.toString()
            binding.addedTextView.text = assetsAdded.toString()
            binding.revisedTextView.text = assetsRevised.toString()
        }
    }

    private fun maintenanceAsset() {
        val arc = adapter?.currentItem()
        if (arc == null) {
            allowClicks = true
            return
        }

        val tempAssetId = arc.assetId
        if (tempAssetId > 0) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, AssetMaintenanceConditionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("assetId", tempAssetId)
                startActivity(intent)
            }
        }
    }

    private fun detailAsset() {
        val tempReview = assetReview ?: return
        val arc = adapter?.currentItem()
        if (arc == null) {
            allowClicks = true
            return
        }

        val tempAssetId = arc.assetId
        val tempAsset: Asset?
        if (tempAssetId < 0) {
            // El activo es desconocido
            // Crear un activo temporal para mostrar los detalles
            tempAsset = Asset(
                id = arc.assetId,
                code = arc.code,
                description = arc.description,
                warehouseId = tempReview.warehouseId,
                warehouseAreaId = tempReview.warehouseAreaId,
                active = 1,
                ownershipStatus = OwnershipStatus.unknown.id,
                status = AssetStatus.unknown.id,
                missingDate = null,
                itemCategoryId = 0,
                transferred = 0,
                originalWarehouseId = 0,
                originalWarehouseAreaId = 0,
                labelNumber = null,
                manufacturer = "",
                model = "",
                serialNumber = "",
                condition = AssetCondition.unknown.id,
                parentId = 0,
                ean = "",
                lastAssetReviewDate = null
            )
        } else {
            tempAsset = Asset(tempAssetId)
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, AssetDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("asset", tempAsset)
            resultForAssetDetails.launch(intent)
        } else allowClicks = true
    }

    private val resultForAssetDetails =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            allowClicks = true
            rejectNewInstances = false
        }

    private fun removeAsset() {
        val arc = adapter?.currentItem()
        if (arc == null || arc.contentStatusId == AssetReviewContentStatus.notInReview.id) {
            allowClicks = true
            return
        }

        ScannerManager.lockScanner(this, true)
        try {
            val adb = AlertDialog.Builder(this)
            adb.setTitle(R.string.remove_item)
            adb.setMessage(
                String.format(
                    context.getString(R.string.do_you_want_to_remove_the_item), arc.code
                )
            )
            adb.setNegativeButton(R.string.cancel, null)
            adb.setPositiveButton(R.string.accept) { _, _ ->
                removeFromAdapter(arc)
            }
            adb.show()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
            allowClicks = true
        }
    }

    private fun removeFromAdapter(arCont: AssetReviewContent) {
        if (adapter == null) return
        adapter?.removeContent(arCont)
    }

    private fun addAsset() {
        if (!rejectNewInstances) {
            _fillAdapter = false // Para onResume al regresar de la actividad
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

            val intent = Intent(baseContext, AssetPrintLabelActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("multiSelect", false)
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

                    val id = ids.first() ?: return@registerForActivityResult
                    val a = AssetRepository().selectById(id) ?: return@registerForActivityResult

                    try {
                        scannerHandleScanCompleted(a.code, true)
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

    private fun finishAssetReview() {
        if (!rejectNewInstances) {
            _fillAdapter = false // Para onResume al regresar de la actividad
            rejectNewInstances = true
            ScannerManager.lockScanner(this, true)

            // Guardamos la revisión en una tabla temporal.
            // Revisiones de miles de artículos no pueden pasarse en el intent.
            TempReviewContentRepository().insert(
                arId = assetReview?.id ?: 0,
                contents = adapter?.fullList?.toList() ?: listOf()
            )

            val intent = Intent(this, ArcConfirmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("assetReview", Parcels.wrap<AssetReview>(assetReview))
            resultForFinishReview.launch(intent)
        }
    }

    private val resultForFinishReview =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    when (Parcels.unwrap<ConfirmStatus>(data.parcelable("confirmStatus"))) {
                        ConfirmStatus.modify -> {
                            assetReview?.obs = data.getStringExtra("obs") ?: ""
                        }

                        ConfirmStatus.confirm -> {
                            val obs = data.getStringExtra("obs") ?: ""
                            val completed = data.getBooleanExtra("completed", true)
                            confirmAssetReview(obs, completed)
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

    private fun processAssetReview() {
        val tempReview = assetReview ?: return
        val all = adapter?.fullList ?: ArrayList()

        saving = true

        thread {
            val sr = SaveReview()
            sr.addParams(
                assetReview = tempReview,
                contents = all,
                onSaveProgress = { saveViewModel.setSaveProgress(it) },
                onSyncProgress = { syncViewModel.setSyncUploadProgress(it) },
                onUploadImageProgress = { syncViewModel.setUploadImagesProgress(it) },
            )
            sr.execute()
        }
    }

    @Suppress("unused")
    private fun showSnackBar(it: SnackBarEventData) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        makeText(binding.root, it.text, it.snackBarType)
    }

    private fun scannerHandleScanCompleted(scannedCode: String, manuallyAdded: Boolean) {
        ScannerManager.lockScanner(this, true)

        try {
            checkCode(
                scannedCode = scannedCode,
                manuallyAdded = manuallyAdded
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }

        if (Statics.DEMO_MODE) {
            val assetRevised = adapter?.countItemsRevised ?: 0
            val margin = ThreadLocalRandom.current().nextInt(0, 10) *
                    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) -1 else 1
            if (assetRevised + margin > allCodesInLocation.size) {
                val obs = getString(R.string.test_review)
                val completed = true
                confirmAssetReview(obs, completed)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
            }
        }
    }

    private fun itemDescriptionDialog(arCont: AssetReviewContent) {
        ScannerManager.lockScanner(this, true)
        try {
            val dialog = Dialog(this)
            val title = "${getString(R.string.description_required_for)}: ${arCont.code.trim()}"

            dialog.setContentView(R.layout.dialog_description_required)
            dialog.setTitle(title)

            dialog.setOnDismissListener { }
            dialog.setOnCancelListener { }

            val titleTextView = dialog.findViewById<TextView>(R.id.titleTextView)
            val descriptionEditText = dialog.findViewById<EditText>(R.id.assetDescriptionEditText)
            val okButton = dialog.findViewById<Button>(R.id.okButton)
            val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

            titleTextView.setText(title, TextView.BufferType.EDITABLE)

            descriptionEditText.setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                    okButton.performClick()
                }
                false
            }

            okButton.setOnClickListener {
                var tempDesc = descriptionEditText.editableText.toString().trim()
                if (tempDesc.trim().isEmpty()) {
                    tempDesc = getString(R.string.NO_DATA)
                }
                runOnUiThread {
                    arCont.assetDescription = tempDesc
                }
                dialog.dismiss()
            }

            // if the button is clicked, close the custom dialog
            cancelButton.setOnClickListener {
                dialog.cancel()
            }

            dialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        scannerHandleScanCompleted(scanCode, false)
    }

    // region ProgressBar
// Aparece mientras se realizan operaciones sobre las bases de datos remotos y local
    private var progressDialog: AlertDialog? = null
    private lateinit var alertBinding: ProgressBarDialogBinding
    private fun createProgressDialog() {
        alertBinding = ProgressBarDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        //builder.setCancelable(false) // if you want user to wait for some process to finish
        builder.setView(alertBinding.root)
        progressDialog = builder.create()
    }

    private fun showProgressDialog(it: AdapterProgress) {
        showProgressDialog(
            title = context.getString(R.string.processing_asset),
            msg = it.msg,
            status = it.progressStatus.id,
            progress = it.completedTask,
            total = it.totalTask
        )
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

            val appColor = ResourcesCompat.getColor(this.resources, R.color.assetControl, null)

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
                        this.getString(R.string.cancel),
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
                        this.getString(R.string.cancel),
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

    private fun startReview() {
        val ar = assetReview ?: return

        thread {
            val startReview = StartReview()
            startReview.addParams(
                assetReview = ar,
                isNew = isNew,
                onProgress = { onStartReviewProgress(it) },
                onSaveProgress = { onSaveProgress(it) })
            startReview.execute()
        }
    }

    private fun fillAdapter(contents: ArrayList<AssetReviewContent>) {
        showProgressBar(true)

        runOnUiThread {
            try {
                if (adapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = adapter?.currentItem()
                }

                adapter = ArcRecyclerAdapter(
                    recyclerView = binding.recyclerView,
                    fullList = contents,
                    checkedIdArray = checkedIdArray,
                    allowQuickReview = allowQuickReview,
                    showCheckBoxes = showCheckBoxes,
                    showCheckBoxesChanged = { showCheckBoxes = it },
                    showImages = showImages,
                    showImagesChanged = { showImages = it },
                    visibleStatus = visibleStatusArray
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

                if (!saving && Statics.DEMO_MODE)
                    Handler(Looper.getMainLooper()).postDelayed(
                        { demo() }, 300
                    )
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
        adapter?.refreshUiEventListener(uiEventListener = this)

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

    private fun checkCode(scannedCode: String, manuallyAdded: Boolean) {
        if (scannedCode.isEmpty()) {
            // Nada que hacer, volver
            val res = "$scannedCode: ${getString(R.string.invalid_code)}"
            makeText(binding.root, res, ERROR)
            Log.d(this::class.java.simpleName, res)
            return
        }

        try {
            val sc = ScannedCode(this).getFromCode(
                code = scannedCode,
                searchWarehouseAreaId = true,
                searchAssetCode = true,
                searchAssetSerial = true,
                searchAssetEan = true
            )

            if (sc.warehouseArea != null) {
                val res = getString(R.string.area_label)
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && sc.asset != null && sc.labelNbr == 0) {
                val res = getString(R.string.report_code)
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && sc.asset != null && sc.asset?.labelNumber == null) {
                val res = getString(R.string.no_printed_label)
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && sc.asset != null && !manuallyAdded &&
                sc.labelNbr != null && sc.asset?.labelNumber != sc.labelNbr
            ) {
                val res = getString(R.string.invalid_code)
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            var tempCode = scannedCode
            if (sc.asset != null) {
                // Si ya se encontró un activo, utilizo su código real,
                // ya que el código escaneado puede contener caracteres especiales
                // que no aparecen en la lista
                tempCode = (sc.asset ?: return).code
            }

            val itemCount = adapter?.itemCount ?: 0
            if (itemCount > 0) {
                // Buscar primero en el adaptador de la lista
                val arCont = adapter?.getContentByCode(tempCode)
                if (arCont != null) {
                    /*
                    * 1 = Asset revised
                    * 2 = Added asset from another warehouse
                    * 3 = Added asset does not exist in the database
                    * 4 = Added asset that was missing from the current warehouse
                    * 0 = Not in the review
                    */

                    // Process the ROW
                    if (arCont.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                        runOnUiThread {
                            adapter?.updateContent(
                                assetId = arCont.assetId,
                                contentStatusId = AssetReviewContentStatus.revised.id,
                                assetStatusId = sc.asset?.status ?: AssetStatus.unknown.id
                            )
                        }

                        val res = "$scannedCode: ${getString(R.string.ok)}"
                        makeText(binding.root, res, SUCCESS)
                        Log.d(this::class.java.simpleName, res)
                    } else {
                        val res = "$scannedCode: ${getString(R.string.already_registered)}"
                        makeText(binding.root, res, SnackBarType.INFO)
                        Log.d(this::class.java.simpleName, res)

                        runOnUiThread {
                            adapter?.selectItem(arCont)
                        }
                    }
                    return
                }
            }

            val allowUnknownCodes = binding.allowUnknownCodesSwitch.isChecked

            if (sc.asset == null && !allowUnknownCodes) {
                val res = "$scannedCode: ${getString(R.string.unknown_code)}"
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            val tempReview = assetReview ?: return

            // Agregar códigos desconocidos si está activado el CheckBox
            // AND
            //    El código no se encuentra en la base de datos
            //    OR
            //    El activo existe pero está desactivado
            if (allowUnknownCodes && (!sc.codeFound || sc.asset != null && sc.asset?.active != 1)) {
                /////////////////////////////////////////////////////////
                // STATUS 3 = Add an asset does not exist in the database
                addUnknownAsset(
                    reviewId = tempReview.id,
                    code = tempCode
                )
                return
            }

            val tempAsset = sc.asset
            if (tempAsset != null) {
                val reviewAreaId = tempReview.warehouseAreaId

                /////////////////////////////////////////////////////////
                // STATUS 2 = Add an asset belonging to another warehouse
                addExternalAsset(
                    reviewId = tempReview.id,
                    warehouseAreaId = reviewAreaId,
                    tempAsset = tempAsset
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        }
    }

    private fun addExternalAsset(reviewId: Long, warehouseAreaId: Long, tempAsset: Asset) {
        var contentStatusId: Int = AssetReviewContentStatus.external.id
        if (tempAsset.warehouseAreaId == warehouseAreaId) {

            ////////////////////////////////////////////////////////////////
            // STATUS 4 = Add lost assets belonging to the current warehouse
            contentStatusId = AssetReviewContentStatus.appeared.id
        }

        val nextId = AssetReviewContentRepository().nextId

        val content = AssetReviewContent(
            assetReviewId = reviewId,
            id = nextId,
            asset = tempAsset,
            qty = 1.0,
            contentStatusId = contentStatusId,
            originWarehouseAreaId = tempAsset.warehouseAreaId
        )

        runOnUiThread {
            if (adapter == null) {
                completeList = arrayListOf(content)
                fillAdapter(completeList)
            } else {
                adapter?.add(content)
            }
        }
    }

    private fun addUnknownAsset(reviewId: Long, code: String) {
        unknownAssetId--

        val nextId = AssetReviewContentRepository().nextId

        val content = AssetReviewContent(
            assetReviewId = reviewId,
            id = nextId,
            assetId = unknownAssetId,
            assetCode = code.take(45).uppercase(Locale.ROOT),
            assetDescription = getString(R.string.NO_DATA),
            qty = 1.0,
            contentStatusId = AssetReviewContentStatus.unknown.id,
            originWarehouseAreaId = 0L
        )

        runOnUiThread {
            if (adapter == null) {
                completeList = arrayListOf(content)
                fillAdapter(completeList)
            } else {
                adapter?.add(content)
            }
        }

        try {
            val addUnknownAssets = binding.addUnknownAssetsSwitch.isChecked

            if (!Statics.DEMO_MODE && addUnknownAssets) {
                // Dar de alta el activo
                assetCrud(content)
                return
            }

            // Pedir una descripción y agregar como desconocido
            if (Statics.DEMO_MODE) {
                content.assetDescription = getString(R.string.test_asset)
            } else {
                runOnUiThread { itemDescriptionDialog(content) }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun assetCrud(arc: AssetReviewContent) {
        val tempReview = assetReview ?: return
        val tempAsset = Asset(
            id = arc.assetId,
            code = arc.code,
            description = "",
            ownershipStatus = OwnershipStatus.unknown.id,
            warehouseAreaId = tempReview.warehouseAreaId,
            warehouseId = tempReview.warehouseId,
            status = AssetStatus.unknown.id,
            itemCategoryId = 0,
            originalWarehouseAreaId = tempReview.warehouseAreaId,
            originalWarehouseId = tempReview.warehouseId,
            active = 1
        )

        if (!rejectNewInstances) {
            _fillAdapter = false // Para onResume al regresar de la actividad
            rejectNewInstances = true

            val intent = Intent(baseContext, AssetCRUDActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("asset", tempAsset)
            intent.putExtra("return_on_success", true)
            intent.putExtra("is_new", true)
            resultForAssetCrud.launch(intent)
        }
    }

    private val resultForAssetCrud =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    val asset = Parcels.unwrap<Asset>(data.parcelable("asset"))
                        ?: return@registerForActivityResult

                    val arc = adapter?.currentItem()
                    if (arc != null) {
                        try {
                            runOnUiThread {
                                arc.assetDescription = asset.description
                                arc.assetId = asset.id
                                arc.assetCode = asset.code
                                arc.contentStatusId = AssetReviewContentStatus.newAsset.id
                            }
                        } catch (ex: Exception) {
                            val res =
                                context.getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                            makeText(binding.root, res, ERROR)
                            Log.d(this::class.java.simpleName, res)
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

        // Opción de visibilidad de Imágenes
        if (useImageControl) {
            menu.add(Menu.NONE, menuItemShowImages, menu.size, context.getString(R.string.show_images))
                .setChecked(showImages)
                .isCheckable = true

            val item = menu.findItem(menuItemShowImages)

            if (showImages) item.icon = ContextCompat.getDrawable(context, R.drawable.ic_photo_library)
            else item.icon = ContextCompat.getDrawable(context, R.drawable.ic_hide_image)

            item.icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                )
        }

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        val visibleStatus =
            if (adapter != null) adapter?.visibleStatus ?: ArrayList()
            else visibleStatusArray

        // Opciones de visibilidad del menú
        for (i in AssetReviewContentStatus.getAll()) {
            menu.add(0, i.id, menu.size, i.description)
                .setChecked(visibleStatus.contains(i))
                .isCheckable = true
        }

        //region Icon colors
        val colorUnknown = ResourcesCompat.getColor(resources, R.color.status_default, null)
        val colorRevised = ResourcesCompat.getColor(resources, R.color.status_revised, null)
        val colorAppeared = ResourcesCompat.getColor(resources, R.color.status_appeared, null)
        val colorExternal = ResourcesCompat.getColor(resources, R.color.status_external, null)
        val colorNotInReview = ResourcesCompat.getColor(resources, R.color.status_not_in_review, null)
        val colorNew = ResourcesCompat.getColor(resources, R.color.status_new, null)

        val colors: ArrayList<Int> = ArrayList()
        colors.add(colorNotInReview)
        colors.add(colorRevised)
        colors.add(colorExternal)
        colors.add(colorUnknown)
        colors.add(colorAppeared)
        colors.add(colorNew)
        //endregion Icon colors

        for ((index, i) in AssetReviewContentStatus.getAll().withIndex()) {
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

    private val menuItemRandomCode = 999001
    private val menuItemManualCode = 999002
    private val menuItemRandomOnListL = 999003
    private val menuItemRandomWa = 999004
    private val menuItemRandomSerial = 999005

    private fun isBackPressed() {
        cancelAssetReview()
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

                (adapter?.fullList ?: ArrayList<AssetReviewContent>()
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
                if (allWa.any()) {
                    val waId = allWa[Random().nextInt(allWa.count())].id
                    if (allWa.any()) scannerCompleted("#WA#${String.format("%05d", waId)}#")
                }
                return super.onOptionsItemSelected(item)
            }

            menuItemRandomSerial -> {
                val allSerials = AssetRepository().selectAllSerials()
                if (allSerials.any()) scannerCompleted(allSerials[Random().nextInt(allSerials.count())])
                return super.onOptionsItemSelected(item)
            }

            else -> {
                return statusItemSelected(item)
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

    private fun statusItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (adapter == null) return false

        val visibleStatus = adapter?.visibleStatus ?: AssetReviewContentStatus.getAll()
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetReviewContentStatus.notInReview.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.notInReview)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.notInReview)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.notInReview)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.notInReview)
                }

            AssetReviewContentStatus.revised.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.revised)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.revised)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.revised)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.revised)
                }

            AssetReviewContentStatus.external.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.external)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.external)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.external)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.external)
                }

            AssetReviewContentStatus.unknown.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.unknown)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.unknown)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.unknown)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.unknown)
                }

            AssetReviewContentStatus.appeared.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.appeared)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.appeared)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.appeared)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.appeared)
                }

            AssetReviewContentStatus.newAsset.id ->
                if (item.isChecked && !visibleStatus.contains(AssetReviewContentStatus.newAsset)) {
                    adapter?.addVisibleStatus(AssetReviewContentStatus.newAsset)
                } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.newAsset)) {
                    adapter?.removeVisibleStatus(AssetReviewContentStatus.newAsset)
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

        return true
    }

    private fun confirmAssetReview(obs: String, completed: Boolean) {
        assetReview?.obs = obs

        if (completed) {
            assetReview?.statusId = AssetReviewStatus.completed.id

            if (adapter?.countItemsAdded == 0) {
                processAssetReview()
            } else {
                if (Statics.DEMO_MODE) {
                    processAssetReview()
                    return

                }

                ScannerManager.lockScanner(this, true)
                try {
                    runOnUiThread {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle(context.getString(R.string.added_assets))
                        alert.setMessage(context.getString(R.string.there_are_assets_in_this_revision_that_belonged_to_another_area_do_you_want_to_make_the_movements_of_these_assets_to_the_current_area_question))
                        alert.setNegativeButton(context.getString(R.string.no)) { _, _ ->
                            return@setNegativeButton
                        }
                        alert.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            processAssetReview()
                        }
                        alert.show()
                    }
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                    ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                } finally {
                    ScannerManager.lockScanner(this, false)
                }
            }
        } else {
            assetReview?.statusId = AssetReviewStatus.onProcess.id
            processAssetReview()
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
            context.getString(R.string.saving_review), msg, taskStatus, progress, total
        )
    }

    private fun onStartReviewProgress(it: StartReviewProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val arContArray: ArrayList<AssetReviewContent> = it.arContArray
        val progress: Int = it.progress ?: 0
        val total: Int = it.total ?: 0

        showProgressDialog(
            context.getString(R.string.loanding_review_), msg, taskStatus, progress, total
        )

        if (taskStatus == ProgressStatus.finished.id) {
            // Revisión inicializada
            _startReview = false

            progressDialog?.dismiss()
            progressDialog = null

            checkedIdArray.clear()
            for (arc in arContArray) {
                if (arc.contentStatusId != AssetReviewContentStatus.notInReview.id && arc.contentStatusId != AssetReviewContentStatus.unknown.id) {
                    checkedIdArray.add(arc.assetId)
                }
            }

            fillAdapter(arContArray)
        } else if (taskStatus == ProgressStatus.crashed.id) {
            rejectNewInstances = false

            progressDialog?.dismiss()
            progressDialog = null
            showProgressBar(false)

            makeText(this, msg, ERROR)
        }
    }

    override fun onUiEventRequired(it: AdapterProgress) {
        showProgressDialog(it)
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

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        setupTextView()
    }

    private var allCodes: ArrayList<String> = ArrayList()
    private var allCodesInLocation: ArrayList<String> = ArrayList()
    private fun demo() {
        if (!Statics.DEMO_MODE) {
            return
        }

        val addAsset = binding.addUnknownAssetsSwitch.isChecked
        if (addAsset) {
            runOnUiThread {
                binding.addUnknownAssetsSwitch.isChecked = false
            }
        }

        val isChecked = binding.allowUnknownCodesSwitch.isChecked
        if (!isChecked) {
            runOnUiThread {
                binding.allowUnknownCodesSwitch.isChecked = true
            }
        }

        if (allCodes.isEmpty()) {
            allCodes = ArrayList(AssetRepository().selectAllCodes())
        }

        if (allCodesInLocation.isEmpty()) {
            allCodesInLocation = ArrayList(
                AssetRepository().selectAllCodesByWarehouseAreaId(
                    headerFragment?.warehouseArea?.id ?: 0L
                )
            )
        }

        var code = if (allCodesInLocation.any()) allCodesInLocation[ThreadLocalRandom.current()
            .nextInt(0, allCodesInLocation.size)] else ""
        if (ThreadLocalRandom.current().nextInt(0, 30) == 0) {
            code = if (allCodesInLocation.any()) allCodes[ThreadLocalRandom.current()
                .nextInt(0, allCodes.size)] else ""
        }
        if (ThreadLocalRandom.current().nextInt(0, 70) == 0) {
            code = (1..8).map { ThreadLocalRandom.current().nextInt(0, charPool.size) }
                .map(charPool::get).joinToString("")
        }

        if (code.isNotEmpty()) scannerCompleted(code)
        else demo()
    }

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

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

        scannerHandleScanCompleted(scannedCode = scanCode, manuallyAdded = false)
    }

    //endregion READERS Reception

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        if (rejectNewInstances) return
        rejectNewInstances = true

        val asset = AssetRepository().selectById(itemId)

        if (asset != null) {
            _fillAdapter = false // Para onResume al regresar de la actividad

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
                    adapter?.updateItem(a, true)
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
        intent.putExtra(ImageControlGridActivity.ARG_PROGRAM_ID, INTERNAL_IMAGE_CONTROL_APP_ID)
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
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

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

    override fun onAddPhotoRequired(
        tableId: Int,
        itemId: Long,
        description: String,
        obs: String,
        reference: String
    ) {
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
                    val assetId = adapter?.currentItem()?.assetId ?: return@registerForActivityResult
                    val asset = AssetRepository().selectById(assetId)
                    asset?.saveChanges()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    // endregion IC
}