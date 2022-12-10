package com.dacosys.assetControl.views.sync.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.R.id
import com.dacosys.assetControl.R.layout
import com.dacosys.assetControl.databinding.SyncActivityBinding
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.itemCategory.`object`.ItemCategory
import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.movements.warehouseMovement.`object`.WarehouseMovement
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.`object`.DataCollection
import com.dacosys.assetControl.model.routes.routeProcess.`object`.RouteProcess
import com.dacosys.assetControl.network.adapter.SyncElementAdapter
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.sync.*
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.assetControl.views.sync.viewModels.PendingViewModel
import com.dacosys.assetControl.views.sync.viewModels.SyncViewModel
import com.dacosys.imageControl.main.UploadImagesProgress
import kotlinx.coroutines.*
import kotlin.concurrent.thread

@Suppress("UNCHECKED_CAST")
class SyncActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    SyncElementAdapter.CheckedChangedListener, SyncElementAdapter.DataSetChangedListener {
    override fun onDestroy() {
        saveSharedPreferences()
        destroyLocals()
        super.onDestroy()
    }

    private fun saveSharedPreferences() {
        // Guardar los valores en las preferencias
        val set = HashSet<String>()
        for (i in visibleRegistryArray) set.add(i.id.toString())
        Statics.prefsPutStringSet(Preference.syncVisibleRegistry.key, set)
    }

    private fun destroyLocals() {
        syncAdapter?.refreshListeners(null, null)
    }

    override fun onDataSetChanged() {
    }

    override fun onCheckedChanged(isChecked: Boolean, pos: Int) {
    }

    private fun onSessionCreated(result: Boolean) {
        if (!result) {
            makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
        }
    }

    private fun onUploadImagesProgress(it: UploadImagesProgress) {
        if (isDestroyed || isFinishing) return

        val result: com.dacosys.imageControl.misc.ProgressStatus = it.result
        val msg: String = it.msg

        when (result.id) {
            ProgressStatus.starting.id, ProgressStatus.running.id -> {
                setProgressBarText(msg)
                showImageProgressBar(true)
            }
            ProgressStatus.crashed.id, ProgressStatus.canceled.id -> {
                showImageProgressBar(false)
                makeText(this, msg, SnackBarType.ERROR)

                fillPendingData()
            }
            ProgressStatus.success.id -> {
                showImageProgressBar(false)
                makeText(this, getString(R.string.upload_images_success), SnackBarType.SUCCESS)

                fillPendingData()
            }
        }
    }

    private fun setProgressBarText(text: String) {
        runOnUiThread {
            run {
                binding.syncStatusTextView.text = text
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

                CURRENT_MODE = -1
                syncing = false
                checkConnection()
            }
            ProgressStatus.success,
            ProgressStatus.finished,
            ProgressStatus.starting,
            ProgressStatus.running,
            -> {
                sendText(totalTask = totalTask,
                    completedTask = completedTask,
                    registryType = registryType,
                    progressStatus = progressStatus)
            }
        }
    }

    private fun sendText(
        totalTask: Int,
        completedTask: Int,
        registryType: SyncRegistryType?,
        progressStatus: ProgressStatus,
    ) {
        refreshSyncTexts(totalTask = totalTask,
            completedTask = completedTask,
            registryType = registryType,
            progressStatus = progressStatus)

        if (CURRENT_MODE == MODE_DOWNLOAD) {
            setDownloadText(concatDownloadText())
        } else {
            setUploadText(concatUploadText())
        }
    }

    private var tempTitle = ""

    //region ListView
    private var visibleRegistryArray: ArrayList<SyncRegistryType> = ArrayList()
    private var checkedIdArray: ArrayList<String> = ArrayList()
    private var currentPos: Int? = null
    private var firstVisiblePos: Int? = null
    private var syncAdapter: SyncElementAdapter? = null
    //

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

        b.putInt("CURRENT_MODE", CURRENT_MODE)

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

        if (syncAdapter != null) {
            b.putInt("currentPos", syncAdapter?.currentPos() ?: 0)
            b.putInt("firstVisiblePos", syncAdapter?.firstVisiblePos() ?: 0)
            b.putParcelableArrayList("visibleRegistryArray", syncAdapter?.getVisibleRegistry())
            b.putStringArrayList("checkedIdArray", syncAdapter?.getAllChecked())
        }
    }

    private fun loadBundleValues(b: Bundle) {
        // region Recuperar el título de la ventana
        val t1 = b.getString("title")
        tempTitle =
            if (t1 != null && t1.isNotEmpty()) t1 else getString(R.string.select_asset_review)
        // endregion

        CURRENT_MODE = b.getInt("CURRENT_MODE")

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

        // Adapter
        currentPos = if (b.containsKey("currentPos")) b.getInt("currentPos") else -1
        firstVisiblePos = if (b.containsKey("firstVisiblePos")) b.getInt("firstVisiblePos") else -1

        visibleRegistryArray.clear()
        if (b.containsKey("visibleRegistryArray")) {
            val t3 = b.getParcelableArrayList<SyncRegistryType>("visibleRegistryArray")
            if (t3 != null) visibleRegistryArray = t3
        } else {
            loadDefaultVisibleStatus()
        }

        checkedIdArray.clear()
        val tempC = b.getStringArrayList("checkedIdArray")
        if (tempC != null) checkedIdArray = tempC
    }

    private fun loadDefaultValues() {
        tempTitle = getString(R.string.send_and_receive_data)
        loadDefaultVisibleStatus()
    }

    private fun loadDefaultVisibleStatus() {
        visibleRegistryArray.clear()
        val set = Statics.prefsGetStringSet(Preference.syncVisibleRegistry.key,
            Preference.syncVisibleRegistry.defaultValue as ArrayList<String>)
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

        checkConnection()

        if (syncing) {
            setUploadText(if (CURRENT_MODE == MODE_UPLOAD) getString(R.string.sending_data_please_wait) else getString(
                R.string.downloading_data_please_wait))
        }
    }

    private lateinit var binding: SyncActivityBinding
    private val syncViewModel: SyncViewModel by viewModels()
    private val pendingViewModel: PendingViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = SyncActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light)

        binding.sendDataButton.isEnabled = !syncing
        binding.sendDataButton.setOnClickListener {
            sendData()
        }

        binding.downloadDataButton.isEnabled = !syncing
        binding.downloadDataButton.setOnClickListener {
            downloadData()
        }

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    private fun sendData() {
        if (syncing) return

        try {
            if (Statics.OFFLINE_MODE || !Statics.isOnline()) {
                makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
                return
            }

            syncing = true
            CURRENT_MODE = MODE_UPLOAD

            addLogText("<b>${getString(R.string.sending_data)}...</b>", binding.uploadTextView)

            thread {
                SyncUpload(onSyncTaskProgress = { syncViewModel.setSyncUploadProgress(it) },
                    onUploadProgress = { syncViewModel.setUploadImagesProgress(it) })
            }
        } catch (ex: Exception) {
            val fontColor = "#" + Integer.toHexString(ContextCompat.getColor(this,
                R.color.firebrick) and 0x00ffffff)
            val t = "<font color='$fontColor'>${getString(R.string.error)}: ${ex.message}...</font>"
            addLogText(t, binding.uploadTextView)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString(), Html.FROM_HTML_MODE_COMPACT).toString())
            } else {
                @Suppress("DEPRECATION") ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString()).toString())
            }

            CURRENT_MODE = -1
            syncing = false
        }
    }

    private fun downloadData() {
        if (syncing) return

        try {
            if (Statics.OFFLINE_MODE || !Statics.isOnline()) {
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
            val fontColor = "#" + Integer.toHexString(ContextCompat.getColor(this,
                R.color.firebrick) and 0x00ffffff)
            val t = "<font color='$fontColor'>${getString(R.string.error)}: ${ex.message}...</font>"
            addLogText(t, binding.dowloadTextView)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString(), Html.FROM_HTML_MODE_COMPACT).toString())
            } else {
                @Suppress("DEPRECATION") ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(ex.message.toString()).toString())
            }

            CURRENT_MODE = -1
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
                @Suppress("DEPRECATION") binding.dowloadTextView.text = Html.fromHtml(d)
            }
        }
    }

    private fun setUploadText(u: String) {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.uploadTextView.text = Html.fromHtml(u, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION") binding.uploadTextView.text = Html.fromHtml(u)
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
                "#" + Integer.toHexString(ContextCompat.getColor(this,
                    R.color.firebrick) and 0x00ffffff)
            }
            ProgressStatus.running -> {
                "#" + Integer.toHexString(ContextCompat.getColor(this,
                    R.color.steelblue) and 0x00ffffff)
            }
            ProgressStatus.success -> {
                "#" + Integer.toHexString(ContextCompat.getColor(this,
                    R.color.seagreen) and 0x00ffffff)
            }
            ProgressStatus.finished -> {
                "#" + Integer.toHexString(ContextCompat.getColor(this,
                    R.color.darkslategray) and 0x00ffffff)
            }
            else -> {
                "#" + Integer.toHexString(ContextCompat.getColor(this,
                    R.color.goldenrod) and 0x00ffffff)
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
                ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT).toString())
            } else {
                @Suppress("DEPRECATION") ErrorLog.writeLog(this,
                    this::class.java.simpleName,
                    Html.fromHtml(message).toString())
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(this::class.java.simpleName,
                    Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT).toString())
            } else {
                @Suppress("DEPRECATION") Log.d(this::class.java.simpleName,
                    Html.fromHtml(message).toString())
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
            val color = "#" + Integer.toHexString(ContextCompat.getColor(this,
                R.color.firebrick) and 0x00ffffff)

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
                @Suppress("DEPRECATION") textView.text = Html.fromHtml(tempText)
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

    companion object {

        private var CURRENT_MODE: Int = -1
        private const val MODE_UPLOAD: Int = 0
        private const val MODE_DOWNLOAD = 1
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
        GetMySqlDate(Statics.getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun fillPendingData() {
        pendingViewModel.refreshPending()
    }

    private fun onPendingData(syncElements: ArrayList<Any>) {
        var t = ""

        var r = syncElements.count { it is AssetReview }
        if (r > 0) t = "$t${getString(R.string.asset_reviews)}: $r<br>"

        r = syncElements.count { it is WarehouseMovement }
        if (r > 0) t = "$t${getString(R.string.movements)}: $r<br>"

        r = syncElements.count { it is Asset }
        if (r > 0) t = "$t${getString(R.string.assets)}: $r<br>"

        r = syncElements.count { it is WarehouseArea }
        if (r > 0) t = "$t${getString(R.string.areas)}: $r}<br>"

        r = syncElements.count { it is Warehouse }
        if (r > 0) t = "$t${getString(R.string.warehouses)}: $r<br>"

        r = syncElements.count { it is ItemCategory }
        if (r > 0) t = "$t${getString(R.string.categories)}: $r<br>"

        r = syncElements.count { it is DataCollection }
        if (r > 0) t = "$t${getString(R.string.data_collections)}: $r<br>"

        r = syncElements.count { it is RouteProcess }
        if (r > 0) t = "$t${getString(R.string.route_process)}: $r<br>"

        r = syncElements.count { it is Int }
        if (r > 0) t = "$t${getString(R.string.images_to_send)}: $r<br>"

        t = when {
            t.isNotEmpty() -> "<b>${getString(R.string.data_to_send)}</b><br>$t"
            else -> getString(R.string.no_data_to_send)
        }

        when {
            syncing -> when (CURRENT_MODE) {
                MODE_DOWNLOAD ->
                    setDownloadText(concatDownloadText())
                else -> setUploadText(concatUploadText())
            }
            else -> setUploadText(t)
        }

        fillAdapter(syncElements)

        changeButtonsEnableState()
    }

    private fun fillAdapter(syncElements: ArrayList<Any>?) {
        showSwipeProgressBar(true)

        try {
            runOnUiThread {
                if (syncAdapter != null) {
                    currentPos = syncAdapter?.currentPos()
                    firstVisiblePos = syncAdapter?.firstVisiblePos()
                }

                if (syncAdapter == null || syncElements != null) {
                    syncAdapter = SyncElementAdapter(activity = this,
                        resource = layout.null_row,
                        syncElements = syncElements ?: ArrayList(),
                        listView = binding.syncElementListView,
                        multiSelect = false,
                        checkedIdArray = checkedIdArray,
                        visibleRegistry = visibleRegistryArray,
                        checkedChangedListener = this,
                        dataSetChangedListener = this)
                } else {
                    // IMPORTANTE:
                    // Se deben actualizar los listeners, sino
                    // las variables de esta actividad pueden
                    // tener valores antiguos en del adaptador.

                    syncAdapter?.refreshListeners(checkedChangedListener = this,
                        dataSetChangedListener = this)
                    syncAdapter?.refresh()
                }

                while (binding.syncElementListView.adapter == null) {
                    // Horrible wait for full load
                }

                syncAdapter?.setSelectItemAndScrollPos(currentPos, firstVisiblePos)
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
        // Inflate the menu; this adds items to the action bar if it is present.
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        val drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_visibility)
        val toolbar = findViewById<Toolbar>(id.action_bar)
        toolbar.overflowIcon = drawable

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
        //endregion Icon colors

        for ((c, i) in SyncRegistryType.getSyncUpload().withIndex()) {
            val icon = ResourcesCompat.getDrawable(getContext().resources, R.drawable.ic_lens, null)
            icon?.mutate()?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colors[c],
                    BlendModeCompat.SRC_IN)
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

        if (item.itemId == id.home || item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return statusItemSelected(item)
    }

    private fun statusItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (syncAdapter == null) {
            return false
        }

        val visibleRegistry = syncAdapter!!.getVisibleRegistry()
        item.isChecked = !item.isChecked

        val syncReg = SyncRegistryType.getById(item.itemId)
        if (syncReg != null) {
            if (item.isChecked && !visibleRegistry.contains(syncReg)) {
                syncAdapter!!.addVisibleRegistry(syncReg)
            } else if (!item.isChecked && visibleRegistry.contains(syncReg)) {
                syncAdapter!!.removeVisibleRegistry(syncReg)
            } else {
                return super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }

        if (syncAdapter?.isStatusVisible(syncAdapter?.currentPos() ?: -1) == false) {
            // La fila actual está invisible, seleccionar la anterior visible
            syncAdapter?.selectNearVisible()
        }

        return true
    }
}