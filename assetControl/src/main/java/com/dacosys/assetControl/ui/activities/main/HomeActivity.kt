package com.dacosys.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.inputmethod.EditorInfo
import android.widget.Button
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
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.cleanTemporaryTables
import com.dacosys.assetControl.databinding.HomeActivityBinding
import com.dacosys.assetControl.model.location.WarehouseArea
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.user.User
import com.dacosys.assetControl.model.user.permission.PermissionEntry
import com.dacosys.assetControl.network.sync.Sync
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.utils.Connection.Companion.isOnline
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.activities.code.CodeCheckActivity
import com.dacosys.assetControl.ui.activities.common.CRUDActivity
import com.dacosys.assetControl.ui.activities.manteinance.AssetManteinanceSelectActivity
import com.dacosys.assetControl.ui.activities.movement.WarehouseMovementContentActivity
import com.dacosys.assetControl.ui.activities.print.PrintLabelActivity
import com.dacosys.assetControl.ui.activities.review.AssetReviewContentActivity
import com.dacosys.assetControl.ui.activities.review.AssetReviewSelectActivity
import com.dacosys.assetControl.ui.activities.route.DataCollectionRuleTargetActivity
import com.dacosys.assetControl.ui.activities.route.RouteSelectActivity
import com.dacosys.assetControl.ui.activities.sync.SyncActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.ConfigHelper
import com.dacosys.assetControl.utils.ImageControl.Companion.setupImageControl
import com.dacosys.assetControl.utils.Screen.Companion.getBestContrastColor
import com.dacosys.assetControl.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.OFFLINE_MODE
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.mainButton.MainButton
import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.preferences.Repository
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.ScannedCode
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.settings.Preference
import com.dacosys.assetControl.viewModel.sync.SyncViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import me.weishu.reflection.Reflection
import org.parceler.Parcels
import java.lang.reflect.Field
import kotlin.concurrent.thread
import kotlin.math.ceil


class HomeActivity : AppCompatActivity(), Scanner.ScannerListener {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
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
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) JotterListener.onRequestPermissionsResult(
            this,
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)
        JotterListener.hideWindow(this)

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
                validateId = true
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
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    private var rejectNewInstances = false
    private var isReturnedFromSettings = false

    override fun onResume() {
        super.onResume()

        // Inicia el hilo de sincronización
        thread {
            Sync.startTimer(onSyncProgress = { syncViewModel.setSyncDownloadProgress(it) },
                onTimerTick = { syncViewModel.setSyncTimerProgress(it) },
                onSessionCreated = { syncViewModel.setSessionCreated(it) })
        }

        JotterListener.lockScanner(this, false)
        rejectNewInstances = false

        // Parece que las actividades de tipo Setting no devuelven resultados
        // así que de esta manera puedo volver a llenar el fragmento de usuarios
        if (isReturnedFromSettings) {
            isReturnedFromSettings = false

            // Vamos a reconstruir el scanner por si cambió la configuración
            JotterListener.autodetectDeviceModel(this)

            setupImageControl()

            // Todavía no está loggeado
            if (Statics.currentUserId == null) {
                login()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Throws(PackageManager.NameNotFoundException::class)
    private fun initLayoutActivity() {
        // Limpiamos las tablas temporales que puedan tener datos.
        cleanTemporaryTables()

        setupHeaderPanel()
        setupSyncPanel()
        setupButtons()
    }

    override fun onBackPressed() {
        Statics.currentUserId = null

        if (isTaskRoot && supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.backStackEntryCount == 0 && supportFragmentManager.backStackEntryCount == 0) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
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

                    val intent = Intent(baseContext, WarehouseMovementContentActivity::class.java)
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

            MainButton.AssetManteinance -> {
                if (!rejectNewInstances) {
                    rejectNewInstances = true

                    val intent = Intent(baseContext, AssetManteinanceSelectActivity::class.java)
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
        val realPass = prefsGetString(Preference.confPassword)
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
        val realPass = prefsGetString(Preference.confPassword)
        if (password == realPass) {
            ConfigHelper.setDebugConfigValues()

            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            isReturnedFromSettings = true
        } else {
            makeText(
                binding.root, getString(R.string.invalid_password), SnackBarType.ERROR
            )
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

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initLayoutActivity()

        syncViewModel.syncDownloadProgress.observe(this) { if (it != null) onSyncTaskProgress(it) }
        syncViewModel.sessionCreated.observe(this) { if (it != null) onSessionCreated(it) }
        syncViewModel.syncTimerProgress.observe(this) { if (it != null) onTimerTick(it) }

        if (Repository.wsUrl.isEmpty() || Repository.wsNamespace.isEmpty()) {
            makeText(
                binding.root, getString(R.string.webservice_is_not_configured), SnackBarType.ERROR
            )
            setupInitConfig()
        } else {
            // ¿Ya está loggeado?
            // Evitar hacer un nuevo login cuando se hace la rotación de la pantalla
            if (Statics.currentUserId == null) {
                login()
            }
        }
    }

    private fun setupInitConfig() {
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, InitConfigActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            resultForInitConfig.launch(intent)
        }
    }

    private val resultForInitConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (Repository.wsUrl.isEmpty() || Repository.wsNamespace.isEmpty()) {
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
                JotterListener.lockScanner(this, false)
            }
        }

    private fun login() {
        ///////////// LOGIN /////////////
        if (!rejectNewInstances) {
            rejectNewInstances = true

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            resultForLogin.launch(intent)
        }
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
                JotterListener.lockScanner(this, false)
            }
        }
    //region HEADER PANELS

    private fun setupHeaderPanel() {
        /// USUARIO
        if (Statics.currentUserId != null) {
            val user = User(Statics.currentUserId ?: return, false)
            binding.userTextView.text = user.name
        }

        /// VERSION
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        binding.versionTextView.text =
            String.format("%s %s", getString(R.string.app_milestone), pInfo.versionName)
        binding.installationCodeTextView.text = Repository.installationCode
        binding.packageTextView.text = Repository.clientPackage
        when {
            Repository.clientPackage.isEmpty() -> binding.packageTextView.visibility = GONE
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
        val bitmapResized = Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * 0.5).toInt(), (bitmap.height * 0.5).toInt(), false
        )
        return BitmapDrawable(resources, bitmapResized)
    }
    //endregion

    //region SYNC PANELS

    private fun onSessionCreated(result: Boolean) {
        if (!result) {
            makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
        }
    }

    private fun onTimerTick(secs: Int) {
        if (isDestroyed || isFinishing) return

        runOnUiThread {
            val restSec = Preferences.prefsGetInt(Preference.acSyncInterval) - secs
            val restMin = restSec / 60
            val rstSecsInMin = restSec % 60
            val msg = "$restMin:${String.format("%02d", rstSecsInMin)}"
            binding.timeTextView.text = msg
        }
    }

    private fun onSyncTaskProgress(it: SyncProgress) {
        if (isDestroyed || isFinishing) return

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
            // Mostramos el Timer sólo en DEBUG
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
            com.dacosys.assetControl.utils.Screen.touchButton(motionEvent, view as Button)
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
            if (b.mainButtonId == MainButton.AssetManteinance.mainButtonId && !prefsGetBoolean(
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
                val backColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ((b.background as StateListDrawable).current as GradientDrawable).color?.defaultColor
                        ?: R.color.white
                } else {
                    // Use reflection below API level 23
                    try {
                        val drawable =
                            (b.background as StateListDrawable).current as GradientDrawable
                        var field: Field = drawable.javaClass.getDeclaredField("mGradientState")
                        field.isAccessible = true
                        val myObj = field.get(drawable)
                        if (myObj == null) R.color.white
                        else {
                            field = myObj.javaClass.getDeclaredField("mSolidColors")
                            field.isAccessible = true
                            (field.get(myObj) as ColorStateList).defaultColor
                        }
                    } catch (e: NoSuchFieldException) {
                        e.printStackTrace()
                        R.color.white
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                        R.color.white
                    }
                }

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
                                    getContext().resources, R.color.white, null
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

        val backColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((binding.mainButton13.background as StateListDrawable).current as GradientDrawable).color?.defaultColor
                ?: R.color.white
        } else {
            // Use reflection below API level 23
            try {
                val drawable =
                    (binding.mainButton13.background as StateListDrawable).current as GradientDrawable
                var field: Field = drawable.javaClass.getDeclaredField("mGradientState")
                field.isAccessible = true
                val myObj = field.get(drawable)
                if (myObj == null) R.color.white
                else {
                    field = myObj.javaClass.getDeclaredField("mSolidColors")
                    field.isAccessible = true
                    (field.get(myObj) as ColorStateList).defaultColor
                }
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
                R.color.white
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                R.color.white
            }
        }

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

        // Agregar un AssetReview del área
        val ar = AssetReview.add(warehouseArea)
        if (ar == null) {
            rejectNewInstances = false
            return
        }

        val intent = Intent(baseContext, AssetReviewContentActivity::class.java)
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

        if (!prefsGetBoolean(Preference.showConfButton)) {
            menu.removeItem(menu.findItem(R.id.action_settings).itemId)
        }

        if (!isRfidRequired()) {
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
                "${getContext().getString(R.string.error)}: ${ex.message}",
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
                    getContext().getString(R.string.error_restarting_sync_dates),
                    SnackBarType.ERROR
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(
                binding.root, "${
                    getContext().getString(R.string.error)
                }: ${ex.message}", SnackBarType.ERROR
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }
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