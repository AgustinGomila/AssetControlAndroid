package com.example.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.currentUser
import com.example.assetControl.AssetControlApp.Companion.isLogged
import com.example.assetControl.AssetControlApp.Companion.setCurrentUserId
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.data.enums.permission.PermissionEntry
import com.example.assetControl.data.room.database.AcTempDatabase
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.user.User
import com.example.assetControl.data.room.repository.review.AssetReviewRepository
import com.example.assetControl.data.room.repository.user.UserRepository
import com.example.assetControl.databinding.HomeActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.ScannedCode
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.devices.scanners.rfid.Rfid.Companion.isRfidRequired
import com.example.assetControl.network.download.DownloadDb
import com.example.assetControl.network.sync.Sync
import com.example.assetControl.network.sync.SyncDownload
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.utils.Connection.Companion.isOnline
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.ui.activities.code.CodeCheckActivity
import com.example.assetControl.ui.activities.common.CRUDActivity
import com.example.assetControl.ui.activities.dataCollection.DataCollectionRuleTargetActivity
import com.example.assetControl.ui.activities.maintenance.AssetMaintenanceSelectActivity
import com.example.assetControl.ui.activities.movement.WmcActivity
import com.example.assetControl.ui.activities.print.PrintLabelActivity
import com.example.assetControl.ui.activities.review.ArcActivity
import com.example.assetControl.ui.activities.review.AssetReviewSelectActivity
import com.example.assetControl.ui.activities.route.RouteSelectActivity
import com.example.assetControl.ui.activities.sync.SyncActivity
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen
import com.example.assetControl.ui.common.utils.Screen.Companion.getBestContrastColor
import com.example.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.Statics.Companion.OFFLINE_MODE
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.imageControl.ImageControl.Companion.setupImageControl
import com.example.assetControl.utils.mainButton.MainButton
import com.example.assetControl.utils.settings.config.ConfigHelper
import com.example.assetControl.utils.settings.config.Preference
import com.example.assetControl.viewModel.sync.SyncViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.runBlocking
import org.parceler.Parcels
import kotlin.concurrent.thread
import kotlin.math.ceil


class HomeActivity : AppCompatActivity(), Scanner.ScannerListener {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        Sync.stopTimer()
        if (isTaskRoot && isFinishing) {
            finishAfterTransition()
        }
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
        if (showScannedCode) makeText(binding.root, scanCode, SnackBarType.INFO)
        ScannerManager.lockScanner(this, true)
        ScannerManager.hideWindow(this)

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
                searchWarehouseAreaId = true,
                searchAssetCode = false,
                searchAssetSerial = false,
                searchAssetEan = false
            )

            val warehouseArea = if (sc.warehouseArea != null) {
                sc.warehouseArea
            } else {
                val res = getString(R.string.invalid_warehouse_area_code)
                makeText(binding.root, res, SnackBarType.ERROR)
                null
            }

            if (warehouseArea != null) {
                beginAssetReview(warehouseArea)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), SnackBarType.ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            ScannerManager.lockScanner(this, false)
        }
    }

    private var rejectNewInstances = false

    override fun onResume() {
        super.onResume()

        // Parece que las actividades de tipo Setting no devuelven resultados
        // así que de esta manera puedo volver a llenar el fragmento de usuarios
        // ¿Ya está autentificado?
        // Evitar hacer un nuevo login cuando se hace la rotación de la pantalla
        if (svm.wsUrl.isEmpty() || svm.wsNamespace.isEmpty()) {
            setupInitConfig()
            return
        }

        if (!isLogged()) {
            rejectNewInstances = false
            login()
            return
        }

        // Inicia el hilo de sincronización
        thread {
            Sync.startTimer(
                onSyncProgress = { syncViewModel.setSyncDownloadProgress(it) },
                onTimerTick = { syncViewModel.setSyncTimerProgress(it) },
                onSessionCreated = { syncViewModel.setSessionCreated(it) })
        }

        ScannerManager.lockScanner(this, false)
        rejectNewInstances = false
    }

    @SuppressLint("SetTextI18n")
    @Throws(PackageManager.NameNotFoundException::class)
    private fun initLayoutActivity() {
        // Limpiamos las tablas temporales que puedan tener datos.
        runBlocking { AcTempDatabase.cleanDatabase() }

        setupHeaderPanel()
        setupSyncPanel()
        setupButtons()
    }

    public override fun onPause() {
        super.onPause()
        Sync.stopTimer()
    }

    private fun clickButton(clickedButton: Button) {
        if (!Statics.deviceDateIsValid()) {
            makeText(
                binding.root, getString(R.string.device_date_is_invalid), SnackBarType.ERROR
            )
            return
        }

        when (MainButton.getById(clickedButton.tag.toString().toLong())) {
            MainButton.AssetReview -> {
                if (!User.hasPermission(PermissionEntry.AddAssetReview)) {
                    makeText(
                        binding.root,
                        getString(R.string.you_do_not_have_permission_to_make_revisions),
                        SnackBarType.ERROR
                    )
                    return
                }

                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, AssetReviewSelectActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.SendAndDownload -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, SyncActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.AssetMovement -> {
                if (!User.hasPermission(PermissionEntry.AddWarehouseMovement)) {
                    makeText(
                        binding.root,
                        getString(R.string.you_do_not_have_permission_to_move_assets),
                        SnackBarType.ERROR
                    )
                    return
                }

                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, WmcActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.CheckCode -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, CodeCheckActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.PrintLabel -> {
                if (!User.hasPermission(PermissionEntry.PrintLabel)) {
                    makeText(
                        binding.root,
                        getString(R.string.you_do_not_have_permission_to_print_labels),
                        SnackBarType.ERROR
                    )
                    return
                }

                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, PrintLabelActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.CRUD -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, CRUDActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.AssetMaintenance -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, AssetMaintenanceSelectActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.Route -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, RouteSelectActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.DataCollection -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, DataCollectionRuleTargetActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }

            MainButton.Configuration -> {
                configApp()
            }
        }
    }

    private fun configApp() {
        val realPass = svm.confPassword
        if (realPass.isEmpty()) {
            attemptEnterConfig(realPass)
            return
        }

        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_password))

        val inputLayout = TextInputLayout(this)
        inputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

        val input = TextInputEditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.isFocusable = true
        input.isFocusableInTouchMode = true
        input.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                if (alertDialog != null) {
                    alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                }
            }
            false
        }

        inputLayout.addView(input)
        builder.setView(inputLayout)
        builder.setPositiveButton(R.string.accept) { _, _ ->
            attemptEnterConfig(input.text.toString())
        }
        builder.setNegativeButton(R.string.cancel, null)
        alertDialog = builder.create()

        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alertDialog.show()
        input.requestFocus()
    }

    private fun attemptEnterConfig(password: String) {
        val realPass = svm.confPassword
        if (password != realPass) {
            makeText(binding.root, getString(R.string.invalid_password), SnackBarType.ERROR)
            return
        }

        if (rejectNewInstances) return
        rejectNewInstances = true

        ConfigHelper.setDebugConfigValues()

        val intent = Intent(baseContext, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        resultForSettings.launch(intent)
    }

    private val resultForSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (DownloadDb.downloadDbRequired) {
                    rejectNewInstances = false
                    login()
                } else {
                    // Vamos a reconstruir el scanner por si cambió la configuración
                    ScannerManager.autodetectDeviceModel(this)
                    setupImageControl()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private lateinit var splashScreen: SplashScreen
    private lateinit var binding: HomeActivityBinding
    private val syncViewModel: SyncViewModel by viewModels()

    private fun createSplashScreen() {
        // Set up 'core-splashscreen' to handle the splash screen in a backward compatible manner.
        splashScreen = installSplashScreen()
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        createSplashScreen()
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initLayoutActivity()

        syncViewModel.syncDownloadProgress.observe(this) { if (it != null) onSyncTaskProgress(it) }
        syncViewModel.sessionCreated.observe(this) { if (it != null) onSessionCreated(it) }
        syncViewModel.syncTimerProgress.observe(this) { if (it != null) onTimerTick(it) }
    }

    private fun setupInitConfig() {
        if (rejectNewInstances) return
        rejectNewInstances = true

        val intent = Intent(this, InitConfigActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        resultForInitConfig.launch(intent)
    }

    private val resultForInitConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (svm.wsUrl.isEmpty() || svm.wsNamespace.isEmpty()) {
                    makeText(
                        binding.root,
                        getString(R.string.webservice_is_not_configured),
                        SnackBarType.ERROR
                    )
                    setupInitConfig()
                } else {
                    login()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                ScannerManager.lockScanner(this, false)
            }
        }

    private fun login() {
        ///////////// LOGIN /////////////
        if (rejectNewInstances) return
        rejectNewInstances = true

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        resultForLogin.launch(intent)
    }

    private val resultForLogin =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                initLayoutActivity()
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
                ScannerManager.lockScanner(this, false)
            }
        }
    //region HEADER PANELS

    private fun setupHeaderPanel() {
        /// USUARIO
        val currentUser = currentUser()
        if (currentUser != null) {
            val user = UserRepository().selectById(currentUser.id)
            binding.userTextView.text = user?.name.orEmpty()
        }

        /// VERSION
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionTextView.text =
            String.format("%s %s", getString(R.string.app_milestone), pInfo.versionName)
        binding.installationCodeTextView.text = svm.installationCode
        binding.packageTextView.text = svm.clientPackage
        when {
            svm.clientPackage.isEmpty() -> binding.packageTextView.visibility = GONE
            else -> binding.packageTextView.visibility = View.VISIBLE
        }

        /// IMAGEN DE CABECERA
        binding.imageView2.setImageResource(0)

        var draw = ContextCompat.getDrawable(this, R.drawable.ac)
        draw = resize(draw ?: return)
        binding.imageView2.setImageDrawable(draw)
    }

    private fun resize(image: Drawable): Drawable {
        val bitmap = (image as BitmapDrawable).bitmap
        val bitmapResized = bitmap.scale((bitmap.width * 0.5).toInt(), (bitmap.height * 0.5).toInt(), false)
        return bitmapResized.toDrawable(resources)
    }
    //endregion

    //region SYNC PANELS

    private fun onSessionCreated(result: Boolean) {
        if (!result) {
            makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
        }
    }

    private fun onTimerTick(secs: Int) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        runOnUiThread {
            val restSec = svm.acSyncInterval - secs
            val restMin = restSec / 60
            val rstSecsInMin = restSec % 60
            val msg = "$restMin:${String.format("%02d", rstSecsInMin)}"
            binding.timeTextView.text = msg
        }
    }

    private fun onSyncTaskProgress(it: SyncProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        when (it.progressStatus) {
            ProgressStatus.bigStarting,
            ProgressStatus.starting,
            ProgressStatus.running,
                -> runOnUiThread {
                setSyncTextView(it.msg)
            }

            ProgressStatus.bigCrashed,
            ProgressStatus.bigFinished,
            ProgressStatus.canceled,
                -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    run {
                        setSyncStatusBarVisibility(View.INVISIBLE)
                    }
                }, 500)
            }
        }
    }

    private var textSyncing = false
    private fun setSyncTextView(msg: String) {
        if (msg.isEmpty() || textSyncing) return
        textSyncing = true
        Log.i(this::class.java.simpleName, msg)
        runOnUiThread {
            binding.syncTextView.text = msg
            setSyncStatusBarVisibility(View.VISIBLE)
        }
        textSyncing = false
    }

    private var statusBarVisibilityChanging = false
    private fun setSyncStatusBarVisibility(visibility: Int) {
        if (statusBarVisibilityChanging) return
        statusBarVisibilityChanging = true

        runOnUiThread {
            if (binding.syncStatusLayout.visibility != visibility) {
                binding.syncStatusLayout.visibility = visibility

                binding.syncStatusLayout.invalidate()
                binding.syncStatusLayout.requestLayout()
            }
        }

        statusBarVisibilityChanging = false
    }

    private fun setupSyncPanel() {
        binding.syncStatusLayout.visibility = GONE

        if (!Statics.isDebuggable() || !BuildConfig.DEBUG) {
            // Mostramos el Timer solo en DEBUG
            binding.timeTextView.visibility = GONE
        }
    }
    //endregion

    //region BOTONES

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButton(button: Button) {
        button.setOnClickListener {
            try {
                clickButton(button)
            } catch (ex: Exception) {
                ex.printStackTrace()
                makeText(
                    binding.root,
                    "${getString(R.string.exception_error)}: " + ex.message,
                    SnackBarType.ERROR
                )
            }
        }
        button.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            Screen.touchButton(motionEvent, view as Button)
            return@OnTouchListener true
        })
    }

    @SuppressLint("DiscouragedPrivateApi") /// El campo mGradientState no es parte de la SDK
    private fun setupButtons() {
        val rowCollection: ArrayList<ConstraintLayout> = ArrayList()
        rowCollection.add(binding.rowButton1)
        rowCollection.add(binding.rowButton2)
        rowCollection.add(binding.rowButton3)
        rowCollection.add(binding.rowButton4)
        rowCollection.add(binding.rowButton5)
        rowCollection.add(binding.rowButton6)
        rowCollection.add(binding.rowButton7)

        val buttonCollection: ArrayList<Button> = ArrayList()
        buttonCollection.add(binding.mainButton1)
        buttonCollection.add(binding.mainButton2)
        buttonCollection.add(binding.mainButton3)
        buttonCollection.add(binding.mainButton4)
        buttonCollection.add(binding.mainButton5)
        buttonCollection.add(binding.mainButton6)
        buttonCollection.add(binding.mainButton7)
        buttonCollection.add(binding.mainButton8)
        buttonCollection.add(binding.mainButton9)
        buttonCollection.add(binding.mainButton10)
        buttonCollection.add(binding.mainButton11)
        buttonCollection.add(binding.mainButton12)
        buttonCollection.add(binding.mainButton13)

        val t = MainButton.getAllMain()
        val allButtonMain: ArrayList<MainButton> = ArrayList()

        for (b in t) {
            // Omitir el botón de mantenimientos
            if (b.mainButtonId == MainButton.AssetMaintenance.mainButtonId && !sr.prefsGetBoolean(
                    Preference.useAssetControlManteinance
                )
            ) {
                continue
            }

            if (User.hasPermission(b.permissionEntry ?: return)) {
                allButtonMain.add(b)
            }
        }

        for (i in buttonCollection.indices) {
            val b = buttonCollection[i]
            if (i < allButtonMain.count()) {
                val backColor: Int =
                    ((b.background as StateListDrawable).current as GradientDrawable).color?.defaultColor
                        ?: R.color.white

                val textColor = getBestContrastColor(
                    "#" + Integer.toHexString(
                        backColor
                    )
                )

                b.setTextColor(textColor)
                b.visibility = View.VISIBLE
                b.tag = allButtonMain[i].mainButtonId
                b.text = allButtonMain[i].description
                b.textAlignment = TEXT_ALIGNMENT_VIEW_START

                if (allButtonMain[i].iconResource != null) {
                    b.setCompoundDrawablesWithIntrinsicBounds(
                        AppCompatResources.getDrawable(
                            this, allButtonMain[i].iconResource!!
                        ), null, null, null
                    )
                    b.compoundDrawables.filterNotNull().forEach {
                        it.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                ResourcesCompat.getColor(
                                    context.resources, R.color.white, null
                                ), BlendModeCompat.SRC_IN
                            )
                    }
                }
                b.compoundDrawablePadding = 25
            } else {
                b.visibility = GONE
            }
        }

        val visibleRows = ceil((allButtonMain.size / 2).toDouble()).toInt()
        for (i in rowCollection.indices) {
            if (i < visibleRows) {
                rowCollection[i].visibility = View.VISIBLE
            } else {
                rowCollection[i].visibility = GONE
            }
        }

        val backColor: Int =
            ((binding.mainButton13.background as StateListDrawable).current as GradientDrawable).color?.defaultColor
                ?: R.color.white

        val textColor = getBestContrastColor(
            "#" + Integer.toHexString(backColor)
        )

        binding.mainButton13.tag = MainButton.Configuration.mainButtonId
        binding.mainButton13.text = MainButton.Configuration.description
        binding.mainButton13.textAlignment = TEXT_ALIGNMENT_VIEW_START
        binding.mainButton13.setTextColor(textColor)
        binding.mainButton13.setCompoundDrawablesWithIntrinsicBounds(
            AppCompatResources.getDrawable(this, MainButton.Configuration.iconResource!!),
            null,
            null,
            null
        )
        binding.mainButton13.compoundDrawablePadding = 25
        binding.mainButton13.visibility = View.VISIBLE
        binding.mainButton13.isEnabled =
            User.hasPermission(MainButton.Configuration.permissionEntry ?: return)

        for (a in buttonCollection) {
            setupButton(a)
        }
    }
    //endregion

    private fun beginAssetReview(warehouseArea: WarehouseArea) {
        if (rejectNewInstances) return
        rejectNewInstances = true

        makeText(binding.root, warehouseArea.description, SnackBarType.INFO)

        val reviewRepository = AssetReviewRepository()

        // Agregar un AssetReview del área
        val arId = reviewRepository.insert(warehouseArea)
        val ar = reviewRepository.selectById(arId)

        val intent = Intent(baseContext, ArcActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("isNew", true)
        intent.putExtra("assetReview", Parcels.wrap(ar))
        resultForReviewSuccess.launch(intent)
    }

    private val resultForReviewSuccess =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            rejectNewInstances = false
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (!svm.showConfButton) {
            menu.removeItem(menu.findItem(R.id.action_settings).itemId)
        }

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
    }

    private fun syncDownload() {
        try {
            if (OFFLINE_MODE || !isOnline()) {
                makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
                return
            }
            thread {
                Sync.goSync(
                    onSyncProgress = { syncViewModel.setSyncDownloadProgress(it) },
                    onSessionCreated = { syncViewModel.setSessionCreated(it) })
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(
                binding.root,
                "${context.getString(R.string.error)}: ${ex.message}",
                SnackBarType.ERROR
            )
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        }
    }

    private fun forceSync() {
        try {
            if (SyncDownload.resetSyncDates()) {
                syncDownload()
            } else {
                makeText(
                    binding.root,
                    context.getString(R.string.error_restarting_sync_dates),
                    SnackBarType.ERROR
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(
                binding.root, "${
                    context.getString(R.string.error)
                }: ${ex.message}", SnackBarType.ERROR
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }
    }

    private fun isBackPressed() {
        setCurrentUserId(null)

        if (isTaskRoot &&
            supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.backStackEntryCount == 0 &&
            supportFragmentManager.backStackEntryCount == 0
        ) {
            finishAfterTransition()
        } else {
            finish()
        }
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

            R.id.action_settings -> {
                if (User.hasPermission(MainButton.Configuration.permissionEntry!!)) {
                    configApp()
                } else {
                    makeText(
                        binding.root,
                        getString(R.string.you_do_not_have_access_to_the_configuration),
                        SnackBarType.ERROR
                    )
                }
                return true
            }

            R.id.action_sync_now -> {
                syncDownload()
                return true
            }

            R.id.action_initial_sync -> {
                forceSync()
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
                return super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        fun equals(a: Any?, b: Any): Boolean {
            return a != null && a == b
        }
    }
}