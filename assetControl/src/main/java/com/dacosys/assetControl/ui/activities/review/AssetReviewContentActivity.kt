package com.dacosys.assetControl.ui.activities.review

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.adapters.asset.AssetAdapter
import com.dacosys.assetControl.adapters.review.AssetReviewContentAdapter
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.databinding.AssetReviewContentBottomPanelCollapsedBinding
import com.dacosys.assetControl.databinding.ProgressBarDialogBinding
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.asset.AssetCondition
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.asset.OwnershipStatus
import com.dacosys.assetControl.model.common.SaveProgress
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.model.review.AssetReviewContentStatus
import com.dacosys.assetControl.model.review.AssetReviewStatus
import com.dacosys.assetControl.model.review.async.SaveReview
import com.dacosys.assetControl.model.review.async.StartReview
import com.dacosys.assetControl.model.review.async.StartReviewProgress
import com.dacosys.assetControl.model.status.ConfirmStatus
import com.dacosys.assetControl.model.status.ConfirmStatus.CREATOR.confirm
import com.dacosys.assetControl.model.status.ConfirmStatus.CREATOR.modify
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.ui.activities.asset.AssetCRUDActivity
import com.dacosys.assetControl.ui.activities.asset.AssetDetailActivity
import com.dacosys.assetControl.ui.activities.asset.AssetPrintLabelActivity
import com.dacosys.assetControl.ui.activities.manteinance.AssetManteinanceConditionActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.fragments.movement.LocationHeaderFragment
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.ParcelLong
import com.dacosys.assetControl.utils.misc.UTCDataTime
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetStringSet
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsPutStringSet
import com.dacosys.assetControl.utils.preferences.Repository
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.nfc.Nfc
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.viewModel.review.SaveReviewViewModel
import com.dacosys.assetControl.viewModel.sync.SyncViewModel
import com.dacosys.imageControl.moshi.DocumentContent
import com.dacosys.imageControl.moshi.DocumentContentRequestResult
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.network.common.StatusObject
import com.dacosys.imageControl.network.webService.WsFunction
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.room.entity.Image
import com.dacosys.imageControl.ui.activities.ImageControlCameraActivity
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import org.parceler.Parcels
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.thread

@Suppress("UNCHECKED_CAST")
class AssetReviewContentActivity : AppCompatActivity(), Scanner.ScannerListener,
    Rfid.RfidDeviceListener, AssetReviewContentAdapter.CheckedChangedListener,
    AssetReviewContentAdapter.DataSetChangedListener, SwipeRefreshLayout.OnRefreshListener,
    AssetAdapter.Companion.EditAssetRequiredListener,
    AssetAdapter.Companion.AlbumViewRequiredListener,
    AssetAdapter.Companion.AddPhotoRequiredListener {
    override fun onDestroy() {
        saveSharedPreferences()
        destroyLocals()
        super.onDestroy()
    }

    private fun saveSharedPreferences() {
        prefsPutBoolean(
            Preference.assetReviewAddUnknownAssets.key, binding.addUnknownAssetsSwitch.isChecked
        )
        prefsPutBoolean(
            Preference.assetReviewAllowUnknownCodes.key, binding.allowUnknownCodesSwitch.isChecked
        )
        val set = HashSet<String>()
        for (i in visibleStatusArray) set.add(i.id.toString())
        prefsPutStringSet(
            Preference.assetReviewContentVisibleStatus.key,
            set
        )
    }

    private fun destroyLocals() {
        arContAdapter?.refreshListeners(null, null, null)
        arContAdapter?.refreshImageControlListeners(null, null)
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        // Selecciona la fila correcta, para que se actualice el currentArCont
        val arc = arContAdapter?.getItem(pos)
        if (arc != null) {
            if (isChecked) {
                runOnUiThread {
                    arContAdapter?.updateContent(
                        arc = arc,
                        assetReviewContentStatusId = AssetReviewContentStatus.revised.id,
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

    private fun onSyncUploadProgress(it: SyncProgress) {
        if (isDestroyed || isFinishing) return

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
    private var arContArray: ArrayList<AssetReviewContent> = ArrayList()

    private var arContAdapter: AssetReviewContentAdapter? = null
    private var checkedIdArray: ArrayList<Long> = ArrayList()
    private var lastSelected: AssetReviewContent? = null
    private var firstVisiblePos: Int? = null
    private var visibleStatusArray: ArrayList<AssetReviewContentStatus> = ArrayList()

    private var unknownAssetId: Long = 0
    private var collectorContentId: Long = 0

    private var allowClicks = true
    private var rejectNewInstances = false

    private var headerFragment: LocationHeaderFragment? = null
    private var panelBottomIsExpanded = false
    private var panelTopIsExpanded = true

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putBoolean("startReview", _startReview)

        b.putString("title", tempTitle)
        b.putParcelable("assetReview", Parcels.wrap(assetReview))
        b.putBoolean("isNew", isNew)

        b.putBoolean("saving", saving)

        b.putStringArrayList("currentInventory", currentInventory)

        b.putBoolean("allowUnknownCodes", binding.allowUnknownCodesSwitch.isChecked)
        b.putBoolean("addUnknownAssets", binding.addUnknownAssetsSwitch.isChecked)
        b.putBoolean("panelTopIsExpanded", panelTopIsExpanded)
        b.putBoolean("panelBottomIsExpanded", panelBottomIsExpanded)

        if (arContAdapter != null) {
            b.putParcelable("lastSelected", arContAdapter?.currentArCont())
            b.putInt("firstVisiblePos", arContAdapter?.firstVisiblePos() ?: 0)
            b.putParcelableArrayList("visibleStatusArray", arContAdapter?.getVisibleStatus())
            b.putLongArray("checkedIdArray", arContAdapter?.getAllChecked()?.toLongArray())
            b.putParcelableArrayList("arContArray", arContAdapter?.getAll())
        }
    }

    private fun loadBundleExtrasValues(b: Bundle) {
        assetReview = Parcels.unwrap<AssetReview>(b.getParcelable("assetReview"))
        isNew = b.getBoolean("isNew")

        loadDefaultValues()
    }

    private fun loadBundleValues(b: Bundle) {
        _startReview = b.getBoolean("startReview")

        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.asset_review)
        // endregion

        assetReview = Parcels.unwrap<AssetReview>(b.getParcelable("assetReview"))
        isNew = b.getBoolean("isNew")

        saving = b.getBoolean("saving")

        if (b.containsKey("allowUnknownCodes")) {
            binding.allowUnknownCodesSwitch.isChecked = b.getBoolean("allowUnknownCodes")
        } else {
            binding.allowUnknownCodesSwitch.isChecked =
                prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)
        }

        if (b.containsKey("addUnknownAssets")) {
            binding.addUnknownAssetsSwitch.isChecked = b.getBoolean("addUnknownAssets")
        } else {
            binding.addUnknownAssetsSwitch.isChecked =
                prefsGetBoolean(Preference.assetReviewAddUnknownAssets)
        }

        currentInventory = b.getStringArrayList("currentInventory")

        if (b.containsKey("panelBottomIsExpanded")) panelBottomIsExpanded =
            b.getBoolean("panelBottomIsExpanded")
        if (b.containsKey("panelTopIsExpanded")) panelTopIsExpanded =
            b.getBoolean("panelTopIsExpanded")

        // Adapter
        lastSelected = b.getParcelable("lastSelected")
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1

        checkedIdArray =
            (b.getLongArray("checkedIdArray") ?: longArrayOf()).toCollection(ArrayList())

        arContArray.clear()
        val tempCont = b.getParcelableArrayList<AssetReviewContent>("arContArray")
        if (tempCont != null) arContArray = tempCont

        visibleStatusArray.clear()
        if (b.containsKey("visibleStatusArray")) {
            val t3 = b.getParcelableArrayList<AssetReviewContentStatus>("visibleStatusArray")
            if (t3 != null) visibleStatusArray = t3
        } else {
            loadDefaultVisibleStatus()
        }

        _fillAdapter = true
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.asset_review)
        binding.allowUnknownCodesSwitch.isChecked =
            prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)
        binding.addUnknownAssetsSwitch.isChecked =
            prefsGetBoolean(Preference.assetReviewAddUnknownAssets)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleStatusArray.clear()
        var set = prefsGetStringSet(
            Preference.assetReviewContentVisibleStatus.key,
            Preference.assetReviewContentVisibleStatus.defaultValue as ArrayList<String>
        )
        if (set == null) set = AssetReviewContentStatus.getAllIdAsString().toSet()

        for (i in set) {
            val status = AssetReviewContentStatus.getById(i.toInt())
            if (status != null && !visibleStatusArray.contains(status)) {
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

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        saveViewModel.saveProgress.observe(this) { if (it != null) onSaveProgress(it) }
        saveViewModel.startReviewProgress.observe(this) { if (it != null) onStartReviewProgress(it) }
        syncViewModel.syncUploadProgress.observe(this) { if (it != null) onSyncUploadProgress(it) }

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
        binding.allowUnknownCodesSwitch.isChecked =
            prefsGetBoolean(Preference.assetReviewAllowUnknownCodes)

        binding.addUnknownAssetsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.allowUnknownCodesSwitch.isChecked = true
            }
        }
        binding.addUnknownAssetsSwitch.isChecked =
            prefsGetBoolean(Preference.assetReviewAddUnknownAssets)

        binding.mantButton.setOnClickListener {
            if (allowClicks) {
                allowClicks = false
                mantAsset()
            }
        }

        if (!prefsGetBoolean(Preference.useAssetControlManteinance)) {
            binding.mantButton.isEnabled = false
        }

        setHeaderTextBox()

        setPanels()

        Handler(Looper.getMainLooper()).postDelayed({ fillListView() }, 500)
    }

    private fun setHeaderTextBox() {
        headerFragment?.showChangePostButton(false)
        headerFragment?.setTitle(getContext().getString(R.string.area_in_review))

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
            if (panelTopIsExpanded) {
                currentLayout.load(this, R.layout.asset_review_content_activity)
            } else {
                currentLayout.load(this, R.layout.asset_review_content_top_panel_collapsed)
            }
        } else {
            if (panelTopIsExpanded) {
                currentLayout.load(this, R.layout.asset_review_content_bottom_panel_collapsed)
            } else {
                currentLayout.load(this, R.layout.asset_review_content_both_panels_collapsed)
            }
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

        when {
            panelBottomIsExpanded -> {
                binding.expandBottomPanelButton?.text = getString(R.string.expand_panel)
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
                binding.expandTopPanelButton?.text = getString(R.string.area_in_review)
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
                    nextLayout.load(this, R.layout.asset_review_content_bottom_panel_collapsed)
                } else {
                    nextLayout.load(this, R.layout.asset_review_content_both_panels_collapsed)
                }
            } else {
                if (panelTopIsExpanded) {
                    nextLayout.load(this, R.layout.asset_review_content_activity)
                } else {
                    nextLayout.load(this, R.layout.asset_review_content_top_panel_collapsed)
                }
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

            if (panelBottomIsExpanded) (binding.expandBottomPanelButton
                ?: return@setOnClickListener).text = getContext().getString(R.string.collapse_panel)
            else binding.expandBottomPanelButton?.text =
                getContext().getString(R.string.more_options)
        }
    }

    private fun setTopPanelAnimation() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            return
        }

        binding.expandTopPanelButton?.setOnClickListener {
            val nextLayout = ConstraintSet()
            if (panelBottomIsExpanded) {
                if (panelTopIsExpanded) nextLayout.load(
                    this, R.layout.asset_review_content_top_panel_collapsed
                )
                else nextLayout.load(this, R.layout.asset_review_content_activity)
            } else {
                if (panelTopIsExpanded) nextLayout.load(
                    this, R.layout.asset_review_content_both_panels_collapsed
                )
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

            if (panelTopIsExpanded) (binding.expandTopPanelButton
                ?: return@setOnClickListener).text = getContext().getString(R.string.collapse_panel)
            else binding.expandTopPanelButton?.text =
                getContext().getString(R.string.area_in_review)
        }
    }

    private fun cancelAssetReview() {
        JotterListener.pauseReaderDevices(this)
        try {
            val alert = AlertDialog.Builder(this)
            alert.setTitle(getContext().getString(R.string.cancel_review))
            alert.setMessage(getContext().getString(R.string.discard_changes_and_return_to_the_main_menu_question))
            alert.setNegativeButton(R.string.cancel, null)
            alert.setPositiveButton(R.string.accept) { _, _ ->
                closeKeyboard(this)

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
            JotterListener.resumeReaderDevices(this)
            allowClicks = true
        }
    }

    private fun setupTextView() {
        val assetsMissed = arContAdapter?.assetsMissed ?: 0
        val assetsAdded = arContAdapter?.assetsAdded ?: 0
        val assetsRevised = arContAdapter?.assetsRevised ?: 0

        runOnUiThread {
            binding.missedTextView.text = assetsMissed.toString()
            binding.addedTextView.text = assetsAdded.toString()
            binding.revisedTextView.text = assetsRevised.toString()
        }
    }

    private fun mantAsset() {
        val arc = arContAdapter?.currentArCont()
        if (arc == null) {
            allowClicks = true
            return
        }

        val tempAssetId = arc.assetId
        if (tempAssetId > 0) {
            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(this, AssetManteinanceConditionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("assetId", tempAssetId)
                startActivity(intent)
            }
        }
    }

    private fun detailAsset() {
        val tempReview = assetReview ?: return
        val arc = arContAdapter?.currentArCont()
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
                assetId = arc.assetId,
                code = arc.code,
                description = arc.description,
                warehouse_id = tempReview.warehouseId,
                warehouse_area_id = tempReview.warehouseAreaId,
                active = true,
                ownership_status = OwnershipStatus.unknown.id,
                status = AssetStatus.unknown.id,
                missing_date = null,
                item_category_id = 0,
                transferred = false,
                original_warehouse_id = 0,
                original_warehouse_area_id = 0,
                label_number = null,
                manufacturer = "",
                model = "",
                serial_number = "",
                condition = AssetCondition.unknown.id,
                parent_id = 0,
                ean = "",
                last_asset_review_date = null
            )
        } else {
            tempAsset = Asset(tempAssetId, false)
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, AssetDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("asset", tempAsset)
            startActivity(intent)
        }

        allowClicks = true
    }

    private fun removeAsset() {
        val arc = arContAdapter?.currentArCont()
        if (arc == null || arc.contentStatusId == AssetReviewContentStatus.notInReview.id) {
            allowClicks = true
            return
        }

        JotterListener.pauseReaderDevices(this)
        try {
            val adb = AlertDialog.Builder(this)
            adb.setTitle(R.string.remove_item)
            adb.setMessage(
                String.format(
                    getContext().getString(R.string.do_you_want_to_remove_the_item), arc.code
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
            JotterListener.resumeReaderDevices(this)
            allowClicks = true
        }
    }

    private fun removeFromAdapter(arCont: AssetReviewContent) {
        // Cambiar la fila selecciona sólo cuando la anterior es eliminada de la lista
        if (arContAdapter == null) return

        when (arCont.contentStatusId) {
            AssetReviewContentStatus.revised.id,
            AssetReviewContentStatus.newAsset.id,
            -> {
                runOnUiThread {
                    arContAdapter?.updateContent(
                        arc = arCont,
                        assetReviewContentStatusId = AssetReviewContentStatus.notInReview.id,
                        assetStatusId = arCont.assetStatusId,
                        selectItem = false,
                        changeCheckedState = true
                    )
                }
            }
            AssetReviewContentStatus.external.id,
            AssetReviewContentStatus.appeared.id,
            AssetReviewContentStatus.unknown.id,
            -> {
                runOnUiThread { arContAdapter?.remove(arCont) }
            }
            else -> {
                return
            }
        }

        setupTextView()
    }

    private fun addAsset() {
        if (!rejectNewInstances) {
            _fillAdapter = false // Para onResume al regresar de la actividad
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

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
                    val idParcel = data.getParcelableArrayListExtra<ParcelLong>("ids")
                        ?: return@registerForActivityResult

                    val ids: ArrayList<Long?> = ArrayList()
                    for (i in idParcel) {
                        ids.add(i.value)
                    }

                    val a = AssetDbHelper().selectById(ids[0]) ?: return@registerForActivityResult

                    try {
                        scannerHandleScanCompleted(a.code, true)
                    } catch (ex: Exception) {
                        val res =
                            getContext().getString(R.string.an_error_occurred_while_trying_to_add_the_item)
                        makeText(binding.root, res, ERROR)
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

    private fun finishAssetReview() {
        if (!rejectNewInstances) {
            _fillAdapter = false // Para onResume al regresar de la actividad
            rejectNewInstances = true
            JotterListener.lockScanner(this, true)

            val intent = Intent(this, AssetReviewContentConfirmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("assetReview", Parcels.wrap<AssetReview>(assetReview))
            intent.putParcelableArrayListExtra(
                "arContArray", arContAdapter?.getAll() ?: ArrayList<AssetReviewContent>()
            )
            resultForFinishReview.launch(intent)
        }
    }

    private val resultForFinishReview =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if (it?.resultCode == RESULT_OK && data != null) {
                    when (Parcels.unwrap<ConfirmStatus>(data.getParcelableExtra("confirmStatus"))) {
                        modify -> {
                            (assetReview ?: return@registerForActivityResult).obs =
                                data.getStringExtra("obs") ?: ""
                        }
                        confirm -> {
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
                JotterListener.lockScanner(this, false)
                allowClicks = true
            }
        }

    private fun processAssetReview() {
        val tempReview = assetReview ?: return
        val all = arContAdapter?.getAll() ?: ArrayList()

        saving = true

        thread {
            val sr = SaveReview()
            sr.addParams(assetReview = tempReview,
                allAssetList = all,
                onSaveProgress = { saveViewModel.setSaveProgress(it) },
                onSyncProgress = { syncViewModel.setSyncUploadProgress(it) })
            sr.execute()
        }
    }

    @Suppress("unused")
    private fun showSnackBar(it: SnackBarEventData) {
        if (isDestroyed || isFinishing) return

        makeText(binding.root, it.text, it.snackBarType)
    }

    private fun scannerHandleScanCompleted(scannedCode: String, manuallyAdded: Boolean) {
        JotterListener.lockScanner(this, true)

        try {
            checkCode(
                scannedCode = scannedCode,
                manuallyAdded = manuallyAdded,
                allowUnknownCodes = binding.allowUnknownCodesSwitch.isChecked
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }

        if (Statics.demoMode) {
            val margin = ThreadLocalRandom.current().nextInt(0, 6) * if (ThreadLocalRandom.current()
                    .nextInt(0, 2) == 0
            ) -1 else 1
            if ((arContAdapter?.assetsRevised ?: 0) + margin > allCodesInLocation.size) {
                val obs = getString(R.string.test_review)
                val completed = true
                confirmAssetReview(obs, completed)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({ demo() }, 300)
            }
        }
    }

    private fun itemDescriptionDialog(arCont: AssetReviewContent) {
        JotterListener.pauseReaderDevices(this)
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
                    arCont.description = tempDesc
                }
                dialog.dismiss()
            }

            // if button is clicked, close the custom dialog
            cancelButton.setOnClickListener {
                dialog.cancel()
            }

            dialog.show()
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.resumeReaderDevices(this)
        }
    }

    public override fun onResume() {
        super.onResume()

        rejectNewInstances = false
    }

    override fun onBackPressed() {
        cancelAssetReview()
    }

    override fun scannerCompleted(scanCode: String) {
        scannerHandleScanCompleted(scanCode, false)
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
                    alertBinding.progressBarHor.progressTintList = ColorStateList.valueOf(appColor)
                    alertBinding.progressBar.visibility = View.VISIBLE
                    alertBinding.progressBar.progressTintList = ColorStateList.valueOf(appColor)

                    progressDialog?.setButton(DialogInterface.BUTTON_NEGATIVE,
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

                        if (alertBinding.progressBar.visibility == View.VISIBLE) alertBinding.progressBar.visibility =
                            View.GONE
                    } else {
                        alertBinding.progressBar.progress = 0
                        alertBinding.progressBar.max = 0
                        alertBinding.progressBar.isIndeterminate = true

                        if (alertBinding.progressBarHor.visibility == View.VISIBLE) {
                            alertBinding.progressBarHor.visibility = View.GONE
                            alertBinding.progressTextView.visibility = View.GONE
                        }
                        if (alertBinding.progressBar.visibility == View.GONE) alertBinding.progressBar.visibility =
                            View.VISIBLE
                    }

                    progressDialog?.setButton(DialogInterface.BUTTON_NEGATIVE,
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

    private fun fillListView() {
        if (!saving && _startReview) {
            startReview()
            return
        }

        if (_fillAdapter) {
            fillAdapter(arContArray)
        }

        showProgressBar(false)
    }

    private fun startReview() {
        val ar = assetReview ?: return

        thread {
            val startReview = StartReview()
            startReview.addParams(assetReview = ar,
                isNew = isNew,
                lastCollectorId = collectorContentId,
                onProgress = { saveViewModel.setStartReviewProgress(it) },
                onSaveProgress = { saveViewModel.setSaveProgress(it) })
            startReview.execute()
        }
    }

    private fun fillAdapter(items: ArrayList<AssetReviewContent>) {
        showProgressBar(true)

        try {
            runOnUiThread {
                if (arContAdapter != null) {
                    lastSelected = arContAdapter?.currentArCont()
                    firstVisiblePos = arContAdapter?.firstVisiblePos()
                }

                arContAdapter = AssetReviewContentAdapter(
                    activity = this,
                    resource = R.layout.asset_row,
                    assetReviewContArray = items,
                    suggestedList = items,
                    listView = binding.assetReviewContentListView,
                    multiSelect = prefsGetBoolean(
                        Preference.quickReviews
                    ),
                    checkedIdArray = checkedIdArray,
                    visibleStatus = visibleStatusArray
                )
                refreshAdapterListeners()

                while (binding.assetReviewContentListView.adapter == null) {
                    // Horrible wait for full load
                }

                arContAdapter?.selectItem(
                    arc = lastSelected, scrollPos = firstVisiblePos ?: 0, smoothScroll = true
                )

                setupTextView()

                if (!saving) {
                    if (Statics.demoMode) Handler(Looper.getMainLooper()).postDelayed(
                        { demo() }, 300
                    )
                }
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

        arContAdapter?.refreshListeners(
            checkedChangedListener = this,
            dataSetChangedListener = this,
            editAssetRequiredListener = this
        )

        if (Repository.useImageControl) {
            arContAdapter?.refreshImageControlListeners(this, this)
        }
    }

    private fun checkCode(scannedCode: String, manuallyAdded: Boolean, allowUnknownCodes: Boolean) {
        if (scannedCode.isEmpty()) {
            // Nada que hacer, volver
            val res = "$scannedCode: ${getString(R.string.invalid_code)}"
            makeText(binding.root, res, ERROR)
            Log.d(this::class.java.simpleName, res)
            return
        }

        var finalArc: AssetReviewContent? = null

        try {
            val sc = ScannedCode(this).getFromCode(
                code = scannedCode,
                searchWarehouseAreaId = true,
                searchAssetCode = true,
                searchAssetSerial = true,
                validateId = true
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

            if (sc.codeFound && (sc.asset != null && (sc.asset ?: return).labelNumber == null)) {
                val res = getString(R.string.without_printed_label)

                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            if (sc.codeFound && (sc.asset != null && ((sc.asset
                    ?: return).labelNumber != sc.labelNbr && sc.labelNbr != null) && !manuallyAdded)
            ) {
                val res = getString(R.string.invalid_code)

                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            var tempCode = scannedCode
            if (sc.asset != null) {
                // Si ya se encontró un activo, utilizo su código real
                // ya que el código escaneado puede contener caractéres especiales
                // que no aparecen en la lista
                tempCode = (sc.asset ?: return).code
            }

            if (arContAdapter != null && !arContAdapter!!.isEmpty) {
                // Buscar primero en el adaptador de la lista
                (0 until arContAdapter!!.count).map { arContAdapter!!.getItem(it) }.filter {
                    it != null && it.code == tempCode
                }.forEach {
                    if (it != null) {
                        /*
                        * 1 = Asset revised
                        * 2 = Added a asset from the another warehouse
                        * 3 = Added a asset does not exist in the database
                        * 4 = Added a asset that was missing from the current warehouse
                        * 0 = Not in the review
                        */

                        // Process the ROW
                        if (it.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                            runOnUiThread {
                                arContAdapter?.updateContent(
                                    arc = it,
                                    assetReviewContentStatusId = AssetReviewContentStatus.revised.id,
                                    assetStatusId = sc.asset?.assetStatusId ?: 0,
                                    selectItem = true,
                                    changeCheckedState = true
                                )
                            }

                            val res = "$scannedCode: ${getString(R.string.ok)}"
                            makeText(binding.root, res, SnackBarType.SUCCESS)
                            Log.d(this::class.java.simpleName, res)
                        } else {
                            val res = "$scannedCode: ${getString(R.string.already_registered)}"
                            makeText(binding.root, res, SnackBarType.INFO)
                            Log.d(this::class.java.simpleName, res)

                            runOnUiThread {
                                arContAdapter?.forceSelectItem(it)
                            }
                        }
                        return
                    }
                }
            }

            if (sc.asset == null && !allowUnknownCodes) {
                val res = "$scannedCode: ${getString(R.string.unknown_code)}"
                makeText(binding.root, res, ERROR)
                Log.d(this::class.java.simpleName, res)
                return
            }

            // Agregar códigos desconocidos si está activado el CheckBox
            // Y
            //    El código no se encuentra en la base de datos
            //    O
            //    El activo existe pero está desactivado
            if (allowUnknownCodes && (!sc.codeFound || (sc.asset != null && !(sc.asset
                    ?: return).active))
            ) {
                val tempReview = assetReview ?: return

                /////////////////////////////////////////////////////////
                // STATUS 3 = Add an asset does not exist in the database
                unknownAssetId--

                if (tempCode.length >= 45) {
                    tempCode = tempCode.substring(0, 45)
                }

                collectorContentId--

                finalArc = AssetReviewContent(
                    assetReviewId = tempReview.collectorAssetReviewId,
                    assetReviewContentId = collectorContentId,
                    assetId = unknownAssetId,
                    code = tempCode.uppercase(Locale.ROOT),
                    description = getString(R.string.NO_DATA),
                    qty = 1F,
                    contentStatusId = AssetReviewContentStatus.unknown.id,
                    originWarehouseAreaId = 0L
                )

                finalArc.assetStatusId = AssetStatus.unknown.id
                finalArc.collectorContentId = collectorContentId
                finalArc.labelNumber = 0
                finalArc.parentId = 0L
                finalArc.warehouseAreaId = 0L
                finalArc.warehouseAreaStr = ""
                finalArc.warehouseStr = ""
                finalArc.itemCategoryId = 0
                finalArc.itemCategoryStr = ""
                finalArc.ownershipStatusId = 0
                finalArc.manufacturer = ""
                finalArc.model = ""
                finalArc.serialNumber = ""
                finalArc.ean = ""

                runOnUiThread {
                    arContAdapter?.add(finalArc)
                }
            }

            if (sc.asset != null) {
                val tempReview = assetReview ?: return

                /////////////////////////////////////////////////////////
                // STATUS 2 = Add an asset belonging to another warehouse
                var contentStatusId: Int = AssetReviewContentStatus.external.id
                if ((sc.asset ?: return).warehouseAreaId == (assetReview
                        ?: return).warehouseAreaId
                ) {
                    ////////////////////////////////////////////////////////////////
                    // STATUS 4 = Add lost assets belonging to the current warehouse
                    contentStatusId = AssetReviewContentStatus.appeared.id
                }

                collectorContentId--

                finalArc = AssetReviewContent(
                    assetReviewId = tempReview.collectorAssetReviewId,
                    assetReviewContentId = collectorContentId,
                    assetId = (sc.asset ?: return).assetId,
                    code = (sc.asset ?: return).code,
                    description = (sc.asset ?: return).description,
                    qty = 1F,
                    contentStatusId = contentStatusId,
                    originWarehouseAreaId = (sc.asset ?: return).warehouseAreaId
                )

                finalArc.assetStatusId = sc.asset?.assetStatusId ?: 0
                finalArc.collectorContentId = collectorContentId
                finalArc.labelNumber = sc.asset?.labelNumber ?: 0
                finalArc.parentId = sc.asset?.parentAssetId ?: 0
                finalArc.warehouseAreaId = sc.asset?.warehouseAreaId ?: 0
                finalArc.warehouseAreaStr = sc.asset?.warehouseAreaStr ?: ""
                finalArc.warehouseStr = sc.asset?.warehouseStr ?: ""
                finalArc.itemCategoryId = sc.asset?.itemCategoryId ?: 0
                finalArc.itemCategoryStr = sc.asset?.itemCategoryStr ?: ""
                finalArc.ownershipStatusId = sc.asset?.ownershipStatusId ?: 0
                finalArc.manufacturer = sc.asset?.manufacturer ?: ""
                finalArc.model = sc.asset?.model ?: ""
                finalArc.serialNumber = sc.asset?.serialNumber ?: ""
                finalArc.ean = sc.asset?.ean ?: ""

                runOnUiThread {
                    arContAdapter?.add(finalArc)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            return
        }

        if (finalArc != null) {
            runOnUiThread {
                arContAdapter?.selectItem(arc = finalArc, smoothScroll = true)
            }

            try {
                if (finalArc.contentStatusId == AssetReviewContentStatus.unknown.id) {
                    if (!Statics.demoMode && binding.addUnknownAssetsSwitch.isChecked) {
                        // Dar de alta el activo
                        assetCrud(finalArc)
                        return
                    }

                    if (binding.allowUnknownCodesSwitch.isChecked) {
                        // Pedir una descripción y agregar como desconocido
                        if (Statics.demoMode) {
                            finalArc.description = getString(R.string.test_asset)
                        } else {
                            runOnUiThread { itemDescriptionDialog(finalArc) }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            }
        }
    }

    private fun assetCrud(arc: AssetReviewContent) {
        val tempReview = assetReview ?: return
        val tempAsset = Asset()

        tempAsset.assetId = arc.assetId
        tempAsset.code = arc.code
        tempAsset.description = ""
        tempAsset.originalWarehouseAreaId = tempReview.warehouseAreaId
        tempAsset.originalWarehouseId = tempReview.warehouseId
        tempAsset.warehouseAreaId = tempReview.warehouseAreaId
        tempAsset.warehouseId = tempReview.warehouseId
        tempAsset.active = true

        tempAsset.setDataRead()

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
                    val asset = Parcels.unwrap<Asset>(data.getParcelableExtra("asset"))
                        ?: return@registerForActivityResult

                    val arc = arContAdapter?.currentArCont()
                    if (arc != null) {
                        try {
                            runOnUiThread {
                                arc.description = asset.description
                                arc.assetId = asset.assetId
                                arc.code = asset.code
                                arc.contentStatusId = AssetReviewContentStatus.newAsset.id
                            }
                        } catch (ex: Exception) {
                            val res =
                                getContext().getString(R.string.an_error_occurred_while_trying_to_add_the_item)
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
                JotterListener.lockScanner(this, false)
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

        if (!isRfidRequired()) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        // Opciones de visibilidad del menú
        for (i in AssetReviewContentStatus.getAll()) {
            menu.add(0, i.id, i.id, i.description)
                .setChecked(visibleStatusArray.contains(i)).isCheckable = true
        }

        //region Icon colors
        val gray = ResourcesCompat.getColor(resources, R.color.whitesmoke, null)
        val seagreen = ResourcesCompat.getColor(resources, R.color.seagreen, null)
        val orangered = ResourcesCompat.getColor(resources, R.color.orangered, null)
        val steelblue = ResourcesCompat.getColor(resources, R.color.steelblue, null)
        val firebrick = ResourcesCompat.getColor(resources, R.color.firebrick, null)
        val gold = ResourcesCompat.getColor(resources, R.color.gold, null)

        val colors: ArrayList<Int> = ArrayList()
        colors.add(firebrick)     // notInReview
        colors.add(seagreen)      // revised
        colors.add(steelblue)     // external
        colors.add(gray)          // unknown
        colors.add(orangered)     // appeared
        colors.add(gold)          // _new
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
        if (arContAdapter == null) {
            return false
        }

        val visibleStatus = arContAdapter!!.getVisibleStatus()
        item.isChecked = !item.isChecked

        when (item.itemId) {
            AssetReviewContentStatus.notInReview.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.notInReview
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.notInReview)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.notInReview)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.notInReview)
            }
            AssetReviewContentStatus.revised.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.revised
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.revised)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.revised)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.revised)
            }
            AssetReviewContentStatus.external.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.external
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.external)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.external)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.external)
            }
            AssetReviewContentStatus.unknown.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.unknown
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.unknown)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.unknown)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.unknown)
            }
            AssetReviewContentStatus.appeared.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.appeared
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.appeared)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.appeared)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.appeared)
            }
            AssetReviewContentStatus.newAsset.id -> if (item.isChecked && !visibleStatus.contains(
                    AssetReviewContentStatus.newAsset
                )
            ) {
                arContAdapter!!.addVisibleStatus(AssetReviewContentStatus.newAsset)
            } else if (!item.isChecked && visibleStatus.contains(AssetReviewContentStatus.newAsset)) {
                arContAdapter!!.removeVisibleStatus(AssetReviewContentStatus.newAsset)
            }
            else -> return super.onOptionsItemSelected(item)
        }

        if (arContAdapter?.isStatusVisible(arContAdapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            arContAdapter?.selectNearVisible()
        }

        return true
    }

    private fun confirmAssetReview(obs: String, completed: Boolean) {
        assetReview?.obs = obs

        if (completed) {
            assetReview?.statusId = AssetReviewStatus.completed.id

            if (arContAdapter?.assetsAdded == 0) {
                processAssetReview()
            } else {
                if (Statics.demoMode) {
                    processAssetReview()
                    return

                }

                JotterListener.pauseReaderDevices(this)
                try {
                    runOnUiThread {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle(getContext().getString(R.string.added_assets))
                        alert.setMessage(getContext().getString(R.string.there_are_assets_in_this_revision_that_belonged_to_another_area_do_you_want_to_make_the_movements_of_these_assets_to_the_current_area_question))
                        alert.setNegativeButton(getContext().getString(R.string.no)) { _, _ ->
                            return@setNegativeButton
                        }
                        alert.setPositiveButton(getContext().getString(R.string.yes)) { _, _ ->
                            processAssetReview()
                        }
                        alert.show()
                    }
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                    ErrorLog.writeLog(this, this::class.java.simpleName, ex)
                } finally {
                    JotterListener.resumeReaderDevices(this)
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
        if (isDestroyed || isFinishing) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val progress: Int = it.progress
        val total: Int = it.total

        showProgressDialog(
            getContext().getString(R.string.saving_review), msg, taskStatus, progress, total
        )
    }

    private fun onStartReviewProgress(it: StartReviewProgress) {
        if (isDestroyed || isFinishing) return

        val msg: String = it.msg
        val taskStatus: Int = it.taskStatus
        val arContArray: ArrayList<AssetReviewContent> = it.arContArray
        val progress: Int? = it.progress
        val total: Int? = it.total

        showProgressDialog(
            getContext().getString(R.string.loanding_review_), msg, taskStatus, progress, total
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
            progressDialog?.dismiss()
            progressDialog = null
            showProgressBar(false)

            makeText(this, msg, ERROR)
        }
    }

    override fun onDataSetChanged() {
        setupTextView()
    }

    private var allCodes: ArrayList<String> = ArrayList()
    private var allCodesInLocation: ArrayList<String> = ArrayList()
    private fun demo() {
        if (!Statics.demoMode) {
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

        if (allCodes.size <= 0) {
            allCodes = AssetDbHelper().selectAllCodes()
        }

        if (allCodesInLocation.size <= 0) {
            allCodesInLocation = AssetDbHelper().selectAllCodesByWarehouseAreaId(
                headerFragment?.warehouseArea?.warehouseAreaId ?: 0L
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
        if (currentInventory == null) currentInventory = ArrayList()
        if (currentInventory?.contains(scanCode) == false) currentInventory?.add(scanCode)

        scannerHandleScanCompleted(scannedCode = scanCode, manuallyAdded = false)
    }

    //endregion READERS Reception

    override fun onEditAssetRequired(tableId: Int, itemId: Long) {
        val asset = arContAdapter?.currentAsset()

        if (!rejectNewInstances && asset != null) {
            _fillAdapter = false // Para onResume al regresar de la actividad
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
                    arContAdapter!!.updateAsset(a, true)
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
        if (!Repository.useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            tempObjectId = itemId.toString()
            tempTableId = tableId

            val programData = ProgramData(
                programId = INTERNAL_IMAGE_CONTROL_APP_ID.toLong(),
                programObjectId = tempTableId.toLong(),
                objId1 = tempObjectId
            )

            ImageCoroutines().get(programData = programData) {
                val allLocal = toDocumentContentList(it)
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
            programId = INTERNAL_IMAGE_CONTROL_APP_ID,
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

    private fun toDocumentContentList(
        images: ArrayList<Image>,
    ): ArrayList<DocumentContent> {
        val list: ArrayList<DocumentContent> = ArrayList()
        for (i in images) {
            val x = DocumentContent()

            x.description = i.description ?: ""
            x.reference = i.reference ?: ""
            x.obs = i.obs ?: ""
            x.filenameOriginal = i.filenameOriginal ?: ""
            x.statusObjectId = StatusObject.Waiting.statusObjectId.toInt()
            x.statusStr = StatusObject.Waiting.description
            x.statusDate = UTCDataTime.getUTCDateTimeAsString()

            x.userId = Statics.currentUser()?.userId ?: 0
            x.userStr = Statics.currentUser()?.name ?: ""

            x.programId = INTERNAL_IMAGE_CONTROL_APP_ID
            x.programObjectId = tempTableId
            x.objectId1 = tempObjectId
            x.objectId2 = "0"

            list.add(x)
        }
        return list
    }

    private fun showPhotoAlbum(images: ArrayList<DocumentContent> = ArrayList()) {
        val intent = Intent(this, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("programId", INTERNAL_IMAGE_CONTROL_APP_ID)
        intent.putExtra("programObjectId", tempTableId.toLong())
        intent.putExtra("objectId1", tempObjectId)
        intent.putExtra("docContObjArrayList", images)
        startActivity(intent)
    }

    private var tempObjectId = ""
    private var tempTableId = 0

    private fun fillResults(docContReqResObj: DocumentContentRequestResult) {
        if (isDestroyed || isFinishing) return

        if (docContReqResObj.documentContentArray.isEmpty()) {
            makeText(binding.root, getString(R.string.no_images), SnackBarType.INFO)
            rejectNewInstances = false
            return
        }

        val anyAvailable = docContReqResObj.documentContentArray.any { it.available }

        if (!anyAvailable) {
            makeText(
                binding.root,
                getContext().getString(R.string.images_not_yet_processed),
                SnackBarType.INFO
            )
            rejectNewInstances = false
            return
        }

        showPhotoAlbum()
    }

    override fun onAddPhotoRequired(tableId: Int, itemId: Long, description: String) {
        if (!Repository.useImageControl) {
            return
        }

        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, ImageControlCameraActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("programId", INTERNAL_IMAGE_CONTROL_APP_ID)
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
                    arContAdapter?.currentAsset()?.saveChanges()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                rejectNewInstances = false
            }
        }

    // endregion IC
}