package com.dacosys.assetControl.ui.activities.sync

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.databinding.SyncActivityBinding
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.network.utils.Connection.Companion.isOnline
import com.dacosys.assetControl.ui.activities.sync.SyncActivity.Companion.SyncStatus.*
import com.dacosys.assetControl.ui.adapters.interfaces.Interfaces
import com.dacosys.assetControl.ui.adapters.sync.SyncElementRecyclerAdapter
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setupUI
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.INTERNAL_IMAGE_CONTROL_APP_ID
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetStringSet
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsPutStringSet
import com.dacosys.assetControl.utils.settings.preferences.Repository.Companion.useImageControl
import com.dacosys.assetControl.viewModel.sync.PendingViewModel
import com.dacosys.assetControl.viewModel.sync.SyncViewModel
import com.dacosys.imageControl.dto.DocumentContent
import com.dacosys.imageControl.network.common.ProgramData
import com.dacosys.imageControl.network.download.GetImages.Companion.toDocumentContentList
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import com.dacosys.imageControl.room.dao.ImageCoroutines
import com.dacosys.imageControl.ui.activities.ImageControlGridActivity
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelable
import com.dacosys.imageControl.ui.utils.ParcelUtils.parcelableArrayList
import java.io.File
import kotlin.concurrent.thread
import com.dacosys.imageControl.network.common.ProgressStatus as IcProgressStatus

@Suppress("UNCHECKED_CAST")
class SyncActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    Interfaces.AlbumViewRequiredListener, Interfaces.DataSetChangedListener, Interfaces.CheckedChangedListener {
    override fun onDestroy() {
        saveSharedPreferences()
        destroyLocals()
        super.onDestroy()
    }

    private fun saveSharedPreferences() {
        // Guardar los valores en las preferencias
        val set = HashSet<String>()
        for (i in visibleRegistryArray) set.add(i.id.toString())
        prefsPutStringSet(
            Preference.syncVisibleRegistry.key, set
        )
    }

    private fun destroyLocals() {
        adapter?.refreshListeners(null, null)
        adapter?.refreshImageControlListeners(null)
    }

    private fun onSessionCreated(result: Boolean) {
        if (!result) {
            makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
        }
    }

    private fun onUploadImagesProgress(it: UploadImagesProgress) {
        if (isDestroyed || isFinishing) return

        val result: IcProgressStatus = it.result
        val msg: String = it.msg
        val completed = it.completedTask
        val total = it.totalTask

        when (result.id) {
            ProgressStatus.starting.id, ProgressStatus.success.id -> {
                setProgressBarText(msg)
            }

            ProgressStatus.running.id -> {
                val tasksMsg = if (total > 0) "${completed}/${total}" else ""
                setProgressBarText(msg, tasksMsg)
                removeItem(SyncRegistryType.Image, it.uniqueHash)
            }

            ProgressStatus.crashed.id, ProgressStatus.canceled.id -> {
                showImageProgressBar(false)
                makeText(this, msg, SnackBarType.ERROR)
            }

            ProgressStatus.finished.id -> {
                showImageProgressBar(false)
                makeText(this, getString(R.string.upload_images_success), SnackBarType.SUCCESS)
            }
        }
    }

    private fun setProgressBarText(text: String = "", percent: String = "") {
        runOnUiThread {
            run {
                if (text.isNotEmpty() || percent.isNotEmpty()) showImageProgressBar(true)

                binding.syncStatusTextView.text = text
                binding.syncPercentTextView.text = percent
            }
        }
    }

    private fun showImageProgressBar(show: Boolean) {
        runOnUiThread {
            if (show && binding.progressBarLayout.visibility != View.VISIBLE) {
                binding.progressBarLayout.bringToFront()
                binding.progressBarLayout.visibility = View.VISIBLE

                ViewCompat.setZ(binding.progressBarLayout, 0F)
            } else if (!show && binding.progressBarLayout.visibility != View.GONE) {
                binding.progressBarLayout.visibility = View.GONE
            }
        }
    }

    fun onSyncProgress(it: SyncProgress) {
        if (isDestroyed || isFinishing) return

        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType

        when (val progressStatus: ProgressStatus = it.progressStatus) {
            ProgressStatus.bigStarting -> {
                changeButtonsEnableState()
            }

            ProgressStatus.bigCrashed,
            ProgressStatus.bigFinished,
            ProgressStatus.canceled,
            -> {
                if (CURRENT_MODE == MODE_DOWNLOAD) {
                    var tempMsg = msg
                    if (progressStatus != ProgressStatus.bigCrashed) {
                        tempMsg = getString(R.string.data_updated_successfully)
                    }
                    setDownloadText(tempMsg)
                }

                CURRENT_MODE = MODE_WAITING
                syncing = false
                checkConnection()
            }

            ProgressStatus.success,
            ProgressStatus.finished,
            ProgressStatus.starting,
            ProgressStatus.running,
            -> {
                sendText(
                    totalTask = totalTask,
                    completedTask = completedTask,
                    registryType = registryType,
                    progressStatus = progressStatus
                )

                if (progressStatus == ProgressStatus.running) {
                    val rt = registryType ?: return
                    if (it.uniqueId.isEmpty()) return

                    removeItem(rt, it.uniqueId)
                }
            }
        }
    }

    private fun removeItem(rt: SyncRegistryType, uniqueId: String) {
        val items = adapter?.itemCount ?: 0
        if (items == 0) return

        val key = SyncElementRecyclerAdapter.getKey(rt, uniqueId)
        if (key.isEmpty()) return

        val index = adapter?.getIndexByKey(key) ?: return

        if (index in 0 until items) {
            runOnUiThread {
                adapter?.remove(index)
            }
        }
    }

    private fun sendText(
        totalTask: Int,
        completedTask: Int,
        registryType: SyncRegistryType?,
        progressStatus: ProgressStatus,
    ) {
        refreshSyncTexts(
            totalTask = totalTask,
            completedTask = completedTask,
            registryType = registryType,
            progressStatus = progressStatus
        )

        if (CURRENT_MODE == MODE_DOWNLOAD) {
            setDownloadText(concatDownloadText())
        } else {
            setUploadText(concatUploadText())
        }
    }

    private var tempTitle = ""

    private var visibleRegistryArray: ArrayList<SyncRegistryType> = ArrayList()
    private var checkedKeyArray: ArrayList<String> = ArrayList()
    private var adapter: SyncElementRecyclerAdapter? = null

    private var multiSelect = false
    private var showCheckBoxes = false

    private var lastSelected: Any? = null
    private var currentScrollPosition: Int = 0

    private var tempObjectId = ""
    private var tempTableId = 0

    private var rejectNewInstances: Boolean = false

    private var syncing: Boolean = false

    private var userStatus = ""
    private var assetStatus = ""
    private var itemCategoryStatus = ""
    private var warehouseStatus = ""
    private var warehouseAreaStatus = ""
    private var attributeStatus = ""
    private var attributeCategoryStatus = ""
    private var routeStatus = ""
    private var dataCollectionRuleStatus = ""
    private var barcodeLabelCustomStatus = ""

    private var attributeCompStatus = ""
    private var routeCompStatus = ""
    private var dcrContStatus = ""
    private var dcrTargetStatus = ""

    private var routeProcessStatus = ""
    private var assetReviewStatus = ""
    private var warehouseMovementStatus = ""
    private var dataCollectionStatus = ""

    private val menuItemShowImages = 9999
    private var showImages
        get() = Preferences.prefsGetBoolean(Preference.syncShowImages)
        set(value) {
            Preferences.prefsPutBoolean(Preference.syncShowImages.key, value)
        }

    override fun onRefresh() {
        Handler(Looper.getMainLooper()).postDelayed({
            run {
                binding.swipeRefresh.isRefreshing = false
            }
        }, 1000)
    }

    private fun showSwipeProgressBar(show: Boolean) {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = show
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        saveBundleValues(savedInstanceState)
    }

    private fun saveBundleValues(b: Bundle) {
        b.putString("title", tempTitle)

        b.putString("CURRENT_MODE", CURRENT_MODE.name)

        b.putBoolean("syncing", syncing)

        b.putString("userStatus", userStatus)
        b.putString("assetStatus", assetStatus)
        b.putString("itemCategoryStatus", itemCategoryStatus)
        b.putString("warehouseStatus", warehouseStatus)
        b.putString("warehouseAreaStatus", warehouseAreaStatus)
        b.putString("attributeStatus", attributeStatus)
        b.putString("attributeCategoryStatus", attributeCategoryStatus)
        b.putString("routeStatus", routeStatus)
        b.putString("dataCollectionRuleStatus", dataCollectionRuleStatus)
        b.putString("barcodeLabelCustomStatus", barcodeLabelCustomStatus)
        b.putString("attributeCompStatus", attributeCompStatus)
        b.putString("routeCompStatus", routeCompStatus)
        b.putString("dcrContStatus", dcrContStatus)
        b.putString("dcrTargetStatus", dcrTargetStatus)
        b.putString("routeProcessStatus", routeProcessStatus)
        b.putString("assetReviewStatus", assetReviewStatus)
        b.putString("warehouseMovementStatus", warehouseMovementStatus)
        b.putString("dataCollectionStatus", dataCollectionStatus)

        if (adapter != null) {
            b.putParcelable("lastSelected", adapter?.currentSyncElement() as Parcelable?)
            b.putInt("firstVisiblePos", adapter?.firstVisiblePos() ?: 0)
            b.putInt("currentScrollPosition", currentScrollPosition)
            b.putStringArrayList("checkedKeyArray", adapter?.checkedKeyArray)
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle = if (!t1.isNullOrEmpty()) t1 else getString(R.string.select_asset_review)
        // endregion

        val syncStatus = b.getString("CURRENT_MODE") ?: ""
        CURRENT_MODE = if (syncStatus.isNotEmpty())
            SyncStatus.valueOf(syncStatus)
        else
            MODE_WAITING

        syncing = b.getBoolean("syncing")

        userStatus = b.getString("userStatus") ?: ""
        assetStatus = b.getString("assetStatus") ?: ""
        itemCategoryStatus = b.getString("itemCategoryStatus") ?: ""
        warehouseStatus = b.getString("warehouseStatus") ?: ""
        warehouseAreaStatus = b.getString("warehouseAreaStatus") ?: ""
        attributeStatus = b.getString("attributeStatus") ?: ""
        attributeCategoryStatus = b.getString("attributeCategoryStatus") ?: ""
        routeStatus = b.getString("routeStatus") ?: ""
        dataCollectionRuleStatus = b.getString("dataCollectionRuleStatus") ?: ""
        barcodeLabelCustomStatus = b.getString("barcodeLabelCustomStatus") ?: ""
        attributeCompStatus = b.getString("attributeCompStatus") ?: ""
        routeCompStatus = b.getString("routeCompStatus") ?: ""
        dcrContStatus = b.getString("dcrContStatus") ?: ""
        dcrTargetStatus = b.getString("dcrTargetStatus") ?: ""
        routeProcessStatus = b.getString("routeProcessStatus") ?: ""
        assetReviewStatus = b.getString("assetReviewStatus") ?: ""
        warehouseMovementStatus = b.getString("warehouseMovementStatus") ?: ""
        dataCollectionStatus = b.getString("dataCollectionStatus") ?: ""

        visibleRegistryArray.clear()
        if (b.containsKey("visibleRegistryArray")) {
            val t3 = b.parcelableArrayList<SyncRegistryType>("visibleRegistryArray")
            if (t3 != null) visibleRegistryArray = t3
        } else {
            loadDefaultVisibleStatus()
        }

        checkedKeyArray.clear()
        val tempC = b.getStringArrayList("checkedKeyArray")
        if (tempC != null) checkedKeyArray = tempC

        multiSelect = b.getBoolean("multiSelect", multiSelect)
        lastSelected = b.parcelable("lastSelected")
        currentScrollPosition = b.getInt("currentScrollPosition")
        checkedKeyArray = b.getStringArrayList("checkedKeyArray") ?: ArrayList()
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.send_and_receive_data)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleRegistryArray.clear()
        val set = prefsGetStringSet(
            Preference.syncVisibleRegistry.key,
            Preference.syncVisibleRegistry.defaultValue as ArrayList<String>
        )
        if (set != null) {
            for (i in set) {
                val status = SyncRegistryType.getById(i.toInt())
                if (status != null && !visibleRegistryArray.contains(status)) {
                    visibleRegistryArray.add(status)
                }
            }
        } else {
            visibleRegistryArray = SyncRegistryType.getSyncUpload()
        }
    }

    override fun onResume() {
        super.onResume()
        if (syncing) {
            setUploadText(
                if (CURRENT_MODE == MODE_UPLOAD)
                    getString(R.string.sending_data_please_wait)
                else getString(R.string.downloading_data_please_wait)
            )
        }
    }

    override fun onStart() {
        super.onStart()
        checkConnection()
    }

    private lateinit var binding: SyncActivityBinding
    private val syncViewModel: SyncViewModel by viewModels()
    private val pendingViewModel: PendingViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = SyncActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState != null) {
            loadBundleValues(savedInstanceState)
        } else {
            loadDefaultValues()
        }

        syncViewModel.syncUploadProgress.observe(this) { if (it != null) onSyncProgress(it) }
        syncViewModel.uploadImagesProgress.observe(this) { if (it != null) onUploadImagesProgress(it) }
        syncViewModel.syncDownloadProgress.observe(this) { if (it != null) onSyncProgress(it) }
        syncViewModel.sessionCreated.observe(this) { if (it != null) onSessionCreated(it) }
        pendingViewModel.pendingLiveData.observe(this) { if (it != null) onPendingData(it) }

        title = tempTitle

        binding.dowloadTextView.movementMethod = ScrollingMovementMethod()

        binding.swipeRefresh.setOnRefreshListener(this)
        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        binding.sendDataButton.isEnabled = !syncing
        binding.sendDataButton.setOnClickListener {
            sendData()
        }

        binding.downloadDataButton.isEnabled = !syncing
        binding.downloadDataButton.setOnClickListener {
            downloadData()
        }

        setupUI(binding.root, this)
    }

    private fun sendData() {
        if (syncing) return

        try {
            if (!Statics.SUPER_DEMO_MODE) {
                if (Statics.OFFLINE_MODE || !isOnline()) {
                    makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
                    return
                }
            }

            syncing = true
            CURRENT_MODE = MODE_UPLOAD

            addLogText("<b>${getString(R.string.sending_data)}...</b>", binding.uploadTextView)

            thread {
                SyncUpload(
                    onSyncTaskProgress = { syncViewModel.setSyncUploadProgress(it) },
                    onUploadImageProgress = { syncViewModel.setUploadImagesProgress(it) })
            }
        } catch (ex: Exception) {
            setErrorText(ex.message.toString())

            CURRENT_MODE = MODE_WAITING
            syncing = false
        }
    }

    private fun setErrorText(msg: String) {
        val fontColor = "#" + Integer.toHexString(
            ContextCompat.getColor(
                this, R.color.firebrick
            ) and 0x00ffffff
        )
        val t = "<font color='$fontColor'>${getString(R.string.error)}: ${msg}...</font>"
        addLogText(t, binding.uploadTextView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ErrorLog.writeLog(
                this,
                this::class.java.simpleName,
                Html.fromHtml(msg, Html.FROM_HTML_MODE_COMPACT).toString()
            )
        } else {
            @Suppress("DEPRECATION") ErrorLog.writeLog(
                this,
                this::class.java.simpleName,
                Html.fromHtml(msg).toString()
            )
        }
    }

    private fun downloadData() {
        if (syncing) return

        try {
            if (Statics.OFFLINE_MODE || !isOnline()) {
                makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
                return
            }

            syncing = true
            CURRENT_MODE = MODE_DOWNLOAD

            addLogText("<b>${getString(R.string.downloading_data)}...</b>", binding.dowloadTextView)

            thread {
                SyncDownload(onSyncTaskProgress = { syncViewModel.setSyncDownloadProgress(it) },
                    onSessionCreated = { syncViewModel.setSessionCreated(it) })
            }
        } catch (ex: Exception) {
            val fontColor = "#" + Integer.toHexString(
                ContextCompat.getColor(
                    this, R.color.firebrick
                ) and 0x00ffffff
            )
            val t = "<font color='$fontColor'>${getString(R.string.error)}: ${ex.message}...</font>"
            addLogText(t, binding.dowloadTextView)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ErrorLog.writeLog(
                    this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString(), Html.FROM_HTML_MODE_COMPACT).toString()
                )
            } else {
                @Suppress("DEPRECATION") ErrorLog.writeLog(
                    this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString()).toString()
                )
            }

            CURRENT_MODE = MODE_WAITING
            syncing = false
        }
    }

    private fun changeButtonsEnableState() {
        runOnUiThread {
            binding.sendDataButton.isEnabled = !syncing
            binding.downloadDataButton.isEnabled = !syncing
        }
    }

    private fun setDownloadText(d: String) {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.dowloadTextView.text = Html.fromHtml(d, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                binding.dowloadTextView.text = Html.fromHtml(d)
            }
        }
    }

    private fun setUploadText(u: String) {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.uploadTextView.text = Html.fromHtml(u, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                binding.uploadTextView.text = Html.fromHtml(u)
            }
        }
    }

    private fun refreshSyncTexts(
        totalTask: Int,
        completedTask: Int,
        registryType: SyncRegistryType?,
        progressStatus: ProgressStatus,
    ) {
        val progressStatusDesc = progressStatus.description
        var registryDesc = ""
        if (registryType != null) {
            registryDesc = registryType.description
        }

        val fontColor: String = when (progressStatus) {
            ProgressStatus.crashed -> {
                "#" + Integer.toHexString(
                    ContextCompat.getColor(
                        this, R.color.firebrick
                    ) and 0x00ffffff
                )
            }

            ProgressStatus.running -> {
                "#" + Integer.toHexString(
                    ContextCompat.getColor(
                        this, R.color.steelblue
                    ) and 0x00ffffff
                )
            }

            ProgressStatus.success -> {
                "#" + Integer.toHexString(
                    ContextCompat.getColor(
                        this, R.color.seagreen
                    ) and 0x00ffffff
                )
            }

            ProgressStatus.finished -> {
                "#" + Integer.toHexString(
                    ContextCompat.getColor(
                        this, R.color.darkslategray
                    ) and 0x00ffffff
                )
            }

            else -> {
                "#" + Integer.toHexString(
                    ContextCompat.getColor(
                        this, R.color.goldenrod
                    ) and 0x00ffffff
                )
            }
        }

        // Los tipos de registros con ConfEntry NULL son contenidos,
        // composiciones, etc. y el mensaje es diferente.
        var message = ""
        if (registryType?.confEntry != null) {
            message = "$registryDesc: <font color='$fontColor'>$progressStatusDesc</font>"
            if (progressStatus == ProgressStatus.running) {
                message = "$message ${
                    Statics.getPercentage(completedTask, totalTask)
                }"
            }
        } else {
            if (progressStatus == ProgressStatus.running) {
                var prefix = "C"
                if (registryType == SyncRegistryType.DataCollectionRuleTarget) {
                    prefix = "T"
                }
                message = "$prefix:${
                    Statics.getPercentage(completedTask, totalTask)
                }"
            }
        }

        if (progressStatus == ProgressStatus.crashed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ErrorLog.writeLog(
                    this,
                    this::class.java.simpleName,
                    Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT).toString()
                )
            } else {
                @Suppress("DEPRECATION") ErrorLog.writeLog(
                    this, this::class.java.simpleName, Html.fromHtml(message).toString()
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(
                    this::class.java.simpleName,
                    Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT).toString()
                )
            } else {
                @Suppress("DEPRECATION") Log.d(
                    this::class.java.simpleName, Html.fromHtml(message).toString()
                )
            }
        }

        when (registryType) {
            SyncRegistryType.User -> {
                userStatus = message
            }

            SyncRegistryType.Asset -> {
                assetStatus = message
            }

            SyncRegistryType.ItemCategory -> {
                itemCategoryStatus = message
            }

            SyncRegistryType.Warehouse -> {
                warehouseStatus = message
            }

            SyncRegistryType.WarehouseArea -> {
                warehouseAreaStatus = message
            }

            SyncRegistryType.Attribute -> {
                attributeStatus = message
                if (progressStatus == ProgressStatus.success) {
                    attributeCompStatus = ""
                }
            }

            SyncRegistryType.AttributeCategory -> {
                attributeCategoryStatus = message
            }

            SyncRegistryType.Route -> {
                routeStatus = message
                if (progressStatus == ProgressStatus.success) {
                    routeCompStatus = ""
                }
            }

            SyncRegistryType.DataCollectionRule -> {
                dataCollectionRuleStatus = message
                if (progressStatus == ProgressStatus.success) {
                    dcrContStatus = ""
                    dcrTargetStatus = ""
                }
            }

            SyncRegistryType.BarcodeLabelCustom -> {
                barcodeLabelCustomStatus = message
            }

            SyncRegistryType.AttributeComposition -> {
                attributeCompStatus = message
            }

            SyncRegistryType.RouteComposition -> {
                routeCompStatus = message
            }

            SyncRegistryType.DataCollectionRuleContent -> {
                dcrContStatus = message
            }

            SyncRegistryType.DataCollectionRuleTarget -> {
                dcrTargetStatus = message
            }

            SyncRegistryType.AssetReview -> {
                assetReviewStatus = message
            }

            SyncRegistryType.RouteProcess -> {
                routeProcessStatus = message
            }

            SyncRegistryType.WarehouseMovement -> {
                warehouseMovementStatus = message
            }

            SyncRegistryType.DataCollection -> {
                dataCollectionStatus = message
            }
        }
    }

    private fun concatDownloadText(): String {
        var s: String = if (userStatus.isNotEmpty()) "$userStatus<br>" else ""
        s += if (assetStatus.isNotEmpty()) "$assetStatus<br>" else ""
        s += if (itemCategoryStatus.isNotEmpty()) "$itemCategoryStatus<br>" else ""
        s += if (warehouseStatus.isNotEmpty()) "$warehouseStatus<br>" else ""
        s += if (warehouseAreaStatus.isNotEmpty()) "$warehouseAreaStatus<br>" else ""
        s += if (attributeStatus.isNotEmpty()) "$attributeStatus $attributeCompStatus<br>" else ""
        s += if (attributeCategoryStatus.isNotEmpty()) "$attributeCategoryStatus<br>" else ""
        s += if (routeStatus.isNotEmpty()) "$routeStatus $routeCompStatus<br>" else ""
        s += if (dataCollectionRuleStatus.isNotEmpty()) "$dataCollectionRuleStatus $dcrContStatus $dcrTargetStatus<br>" else ""
        s += barcodeLabelCustomStatus.ifEmpty { "" }
        return s
    }

    private fun concatUploadText(): String {
        var s: String = if (assetStatus.isNotEmpty()) "$assetStatus<br>" else ""
        s += if (itemCategoryStatus.isNotEmpty()) "$itemCategoryStatus<br>" else ""
        s += if (warehouseStatus.isNotEmpty()) "$warehouseStatus<br>" else ""
        s += if (warehouseAreaStatus.isNotEmpty()) "$warehouseAreaStatus<br>" else ""
        s += if (assetReviewStatus.isNotEmpty()) "$assetReviewStatus<br>" else ""
        s += if (warehouseMovementStatus.isNotEmpty()) "$warehouseMovementStatus<br>" else ""
        s += if (routeProcessStatus.isNotEmpty()) "$routeProcessStatus<br>" else ""
        s += dataCollectionStatus.ifEmpty { "" }
        return s
    }

    private fun addLogText(s: String, textView: TextView) {
        val editableText = textView.editableText
        var tempText = ""
        if (editableText != null) tempText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "${Html.toHtml(editableText, Html.FROM_HTML_MODE_COMPACT)}<br>"
        } else {
            @Suppress("DEPRECATION") "${Html.toHtml(editableText)}<br>"
        }

        tempText = "$tempText$s"

        val lines = tempText.split("<br>")

        if (lines.size > 50) {
            tempText = ""
            val color = "#" + Integer.toHexString(
                ContextCompat.getColor(
                    this, R.color.firebrick
                ) and 0x00ffffff
            )

            var x = 0
            for (l in lines) {
                if (x == 50) break
                x++
                if (l.contains(color)) tempText = "$tempText$l<br>"
            }
        }

        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.text = Html.fromHtml(tempText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                textView.text = Html.fromHtml(tempText)
            }

            // AUTO SCROLL
            val scrollAmount = textView.layout.getLineTop(textView.lineCount) - textView.height
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                textView.scrollTo(0, scrollAmount)
            } else {
                textView.scrollTo(0, 0)
            }
        }
    }


    companion object {
        enum class SyncStatus {
            MODE_WAITING,
            MODE_UPLOAD,
            MODE_DOWNLOAD,
        }

        private var CURRENT_MODE = MODE_WAITING
    }

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            if (isDestroyed || isFinishing) return

            when (it.status) {
                ProgressStatus.finished -> {
                    fillPendingData()
                }

                ProgressStatus.crashed, ProgressStatus.canceled -> {
                    runOnUiThread {
                        binding.uploadTextView.text = it.msg
                        changeButtonsEnableState()
                    }
                }
            }
        }
        GetMySqlDate(getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun fillPendingData() {
        pendingViewModel.refreshPending()
    }

    private fun onPendingData(syncElements: ArrayList<Any>) {
        fillAdapter(syncElements)
        changeButtonsEnableState()
    }

    private fun fillSummaryRow() {
        if (syncing) {
            if (CURRENT_MODE == MODE_DOWNLOAD) {
                setDownloadText(concatDownloadText())
            } else {
                setUploadText(concatUploadText())
            }
        } else {
            setUploadText(concatUploadPendingText())
        }
    }

    private fun concatUploadPendingText(): String {
        var t = ""

        var r = adapter?.totalAssetReview ?: 0
        if (r > 0) t = "$t${getString(R.string.asset_reviews)}: $r<br>"

        r = adapter?.totalWarehouseMovement ?: 0
        if (r > 0) t = "$t${getString(R.string.movements)}: $r<br>"

        r = adapter?.totalAsset ?: 0
        if (r > 0) t = "$t${getString(R.string.assets)}: $r<br>"

        r = adapter?.totalWarehouseArea ?: 0
        if (r > 0) t = "$t${getString(R.string.areas)}: $r<br>"

        r = adapter?.totalWarehouse ?: 0
        if (r > 0) t = "$t${getString(R.string.warehouses)}: $r<br>"

        r = adapter?.totalItemCategory ?: 0
        if (r > 0) t = "$t${getString(R.string.categories)}: $r<br>"

        r = adapter?.totalDataCollection ?: 0
        if (r > 0) t = "$t${getString(R.string.data_collections)}: $r<br>"

        r = adapter?.totalRouteProcess ?: 0
        if (r > 0) t = "$t${getString(R.string.route_process)}: $r<br>"

        r = adapter?.totalImage ?: 0
        if (r > 0) t = "$t${getString(R.string.images_to_send)}: $r<br>"

        t = when {
            t.isNotEmpty() -> "<b>${getString(R.string.data_to_send)}</b><br>$t"
            else -> getString(R.string.no_data_to_send)
        }

        return t
    }

    private fun fillAdapter(syncElements: ArrayList<Any>?) {
        showSwipeProgressBar(true)

        try {
            runOnUiThread {
                if (adapter != null) {
                    // Si el adapter es NULL es porque aún no fue creado.
                    // Por lo tanto, puede ser que los valores de [lastSelected]
                    // sean valores guardados de la instancia anterior y queremos preservarlos.
                    lastSelected = adapter?.currentSyncElement()
                }

                if (syncElements != null) {
                    adapter = SyncElementRecyclerAdapter.Builder()
                        .recyclerView(binding.recyclerView)
                        .visibleRegistryTypes(visibleRegistryArray)
                        .fullList(syncElements)
                        .checkedKeyArray(checkedKeyArray)
                        .multiSelect(multiSelect)
                        .showCheckBoxes(`val` = showCheckBoxes, callback = { showCheckBoxes = it })
                        .showImages(`val` = showImages, callback = { showImages = it })
                        .filterOptions(SyncElementRecyclerAdapter.FilterOptions())
                        .albumViewRequiredListener(this)
                        .dataSetChangedListener(this)
                        .checkedChangedListener(this)
                        .build()
                }

                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = adapter

                while (binding.recyclerView.adapter == null) {
                    // Horrible wait for a full load
                }

                // Recuperar la última posición seleccionada
                val ls = lastSelected
                val cs = currentScrollPosition
                Handler(Looper.getMainLooper()).postDelayed({
                    adapter?.selectItem(ls, false)
                    adapter?.scrollToPos(cs, true)
                }, 200)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            showSwipeProgressBar(false)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_visibility)
        binding.topAppbar.overflowIcon = drawable

        // Opciones de visibilidad del menú
        for (i in SyncRegistryType.getSyncUpload()) {
            menu.add(0, i.id, i.id, i.description)
                .setChecked(visibleRegistryArray.contains(i)).isCheckable = true
        }

        //region Icon colors
        val asset = ResourcesCompat.getColor(resources, R.color.sync_element_asset, null)
        val assetManteinance =
            ResourcesCompat.getColor(resources, R.color.sync_element_asset_maintenance, null)
        val assetReview =
            ResourcesCompat.getColor(resources, R.color.sync_element_asset_review, null)
        val dataCollection =
            ResourcesCompat.getColor(resources, R.color.sync_element_data_collection, null)
        val itemCategory =
            ResourcesCompat.getColor(resources, R.color.sync_element_item_category, null)
        val routeProcess =
            ResourcesCompat.getColor(resources, R.color.sync_element_route_process, null)
        val warehouse = ResourcesCompat.getColor(resources, R.color.sync_element_warehouse, null)
        val warehouseArea =
            ResourcesCompat.getColor(resources, R.color.sync_element_warehouse_area, null)
        val warehouseMovement =
            ResourcesCompat.getColor(resources, R.color.sync_element_warehouse_movement, null)
        val image = ResourcesCompat.getColor(resources, R.color.sync_element_image, null)

        val colors: ArrayList<Int> = ArrayList()
        /*
        ORDEN:
        Warehouse,
        Asset,
        WarehouseArea,
        ItemCategory,
        AssetReview,
        WarehouseMovement,
        AssetManteinance,
        DataCollection,
        RouteProcess
         */

        colors.add(warehouse)
        colors.add(asset)
        colors.add(warehouseArea)
        colors.add(itemCategory)
        colors.add(assetReview)
        colors.add(warehouseMovement)
        colors.add(assetManteinance)
        colors.add(dataCollection)
        colors.add(routeProcess)
        if (useImageControl) colors.add(image)
        //endregion Icon colors

        for ((index, i) in SyncRegistryType.getSyncUpload().withIndex()) {
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

        // Opción de visibilidad de Imágenes
        if (useImageControl) {
            menu.add(Menu.NONE, menuItemShowImages, Menu.NONE, getContext().getString(R.string.show_images))
                .setChecked(showImages).isCheckable = true
            val item = menu.findItem(menuItemShowImages)
            setImageVisibilityIcon(showImages, item)
        }

        return true
    }

    private fun setImageVisibilityIcon(show: Boolean, item: MenuItem) {
        runOnUiThread {
            if (show) {
                item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_photo_library)
            } else {
                item.icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_hide_image)
            }

            item.icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    getColor(R.color.dimgray), BlendModeCompat.SRC_IN
                )
        }
    }

    private fun isBackPressed() {
        Screen.closeKeyboard(this)
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

        val visibleRegistry = adapter!!.getVisibleStatus()
        item.isChecked = !item.isChecked

        if (item.itemId == menuItemShowImages) {
            adapter?.showImages(item.isChecked)
            setImageVisibilityIcon(item.isChecked, item)
        }

        val syncReg = SyncRegistryType.getById(item.itemId)
        if (syncReg != null) {
            if (item.isChecked && !visibleRegistry.contains(syncReg)) {
                adapter!!.addVisibleStatus(syncReg)
            } else if (!item.isChecked && visibleRegistry.contains(syncReg)) {
                adapter!!.removeVisibleStatus(syncReg)
            } else {
                return super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }

        if (adapter?.isVisible(adapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            adapter?.selectNearVisible()
        }

        return true
    }

    // region ImageControl Album
    override fun onAlbumViewRequired(tableId: Int, itemId: Long, filename: String) {
        if (!useImageControl) return

        if (rejectNewInstances) return
        rejectNewInstances = true

        tempObjectId = itemId.toString()
        tempTableId = tableId

        val programData = ProgramData(
            programId = INTERNAL_IMAGE_CONTROL_APP_ID,
            programObjectId = tempTableId.toLong(),
            objId1 = tempObjectId
        )

        ImageCoroutines().get(
            context = getContext(),
            programData = programData,
        ) {
            val allLocal = toDocumentContentList(
                images = it,
                programData = programData,
            )
            val r = allLocal.mapNotNull { image -> if (File(image.filenameOriginal).name == filename) image else null }

            if (r.isNotEmpty()) {
                showPhotoAlbum(ArrayList(r), filename)
            }
        }
    }

    private fun showPhotoAlbum(images: ArrayList<DocumentContent> = ArrayList(), filename: String) {
        val intent = Intent(this, ImageControlGridActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(ImageControlGridActivity.ARG_PROGRAM_OBJECT_ID, tempTableId.toLong())
        intent.putExtra(ImageControlGridActivity.ARG_OBJECT_ID_1, tempObjectId)
        intent.putExtra(ImageControlGridActivity.ARG_DOC_CONT_OBJ_ARRAY_LIST, images)
        intent.putExtra(ImageControlGridActivity.ARG_FILTER_FILENAME, filename)
        resultForShowPhotoAlbum.launch(intent)
    }

    private val resultForShowPhotoAlbum =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            rejectNewInstances = false
        }

    // endregion ImageControl Album

    override fun onDataSetChanged() {
        fillSummaryRow()
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
        fillSummaryRow()
    }
}