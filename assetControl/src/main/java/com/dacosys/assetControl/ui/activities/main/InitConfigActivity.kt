package com.dacosys.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.dacosys.assetControl.AssetControlApp.Companion.appName
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.databinding.InitConfigActivityBinding
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.utils.ClientPackage
import com.dacosys.assetControl.network.utils.ClientPackage.Companion.selectClientPackage
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.network.utils.Proxy
import com.dacosys.assetControl.network.utils.Proxy.Companion.setupProxy
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.setScreenRotation
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.showKeyboard
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.io.FileHelper
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.cleanPrefs
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.settings.preferences.Repository
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.cdimascio.dotenv.DotenvBuilder
import org.json.JSONObject
import java.lang.ref.WeakReference

class InitConfigActivity : AppCompatActivity(), Scanner.ScannerListener,
    Proxy.Companion.TaskSetupProxyEnded, ClientPackage.Companion.TaskConfigPanelEnded {
    override fun onTaskConfigPanelEnded(status: ProgressStatus) {
        if (status in ProgressStatus.getAllFinish()) {
            isConfiguring = false
        }

        if (status == ProgressStatus.finished) {
            makeText(binding.root, getString(R.string.configuration_applied), SnackBarType.SUCCESS)
            FileHelper.removeDataBases(getContext())
            finish()
        }
    }

    private fun onTaskGetPackagesEnded(it: ClientPackagesProgress) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

        val status: ProgressStatus = it.status
        val result: ArrayList<JSONObject> = it.result
        val clientEmail: String = it.clientEmail
        val clientPassword: String = it.clientPassword
        val msg: String = it.msg

        if (status in ProgressStatus.getAllFinish()) {
            showProgressBar(false)
        }

        when (status) {
            ProgressStatus.starting, ProgressStatus.running -> {
                setProgressBarText(msg)
            }

            ProgressStatus.finished -> {
                if (result.isNotEmpty()) {
                    runOnUiThread {
                        selectClientPackage(
                            parentView = binding.root,
                            callback = this,
                            weakAct = WeakReference(this),
                            allPackage = result,
                            email = clientEmail,
                            password = clientPassword
                        )
                    }
                } else {
                    isConfiguring = false
                    makeText(binding.root, msg, INFO)
                }
            }

            ProgressStatus.success -> {
                isConfiguring = false
                makeText(binding.root, msg, SnackBarType.SUCCESS)
                finish()
            }

            ProgressStatus.crashed, ProgressStatus.canceled -> {
                isConfiguring = false
                makeText(binding.root, msg, ERROR)
            }
        }
    }

    override fun onTaskSetupProxyEnded(
        status: ProgressStatus,
        email: String,
        password: String,
        installationCode: String,
    ) {
        if (status == ProgressStatus.finished) {
            ConfigHelper.getConfig(
                email = email, password = password, installationCode = installationCode
            ) { onTaskGetPackagesEnded(it) }
        }
    }

    private var rejectNewInstances = false

    private var email: String = ""
    private var password: String = ""

    override fun onResume() {
        super.onResume()

        JotterListener.lockScanner(this, false)
        rejectNewInstances = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("email", binding.emailEditText.text.toString())
        savedInstanceState.putString("password", binding.passwordEditText.text.toString())
    }

    private lateinit var binding: InitConfigActivityBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenRotation(this)
        binding = InitConfigActivityBinding.inflate(layoutInflater)
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
            email = savedInstanceState.getString("email") ?: ""
            password = savedInstanceState.getString("password") ?: ""
        } else {
            val extras = intent.extras
            if (extras != null) {
                email = extras.getString("email") ?: ""
                password = extras.getString("password") ?: ""
            }

            clearOldPrefs()
            JotterListener.autodetectDeviceModel(this)
        }

        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val str = "${getString(R.string.app_milestone)} ${pInfo.versionName}"
        binding.versionTextView.text = str

        binding.imageView.setImageResource(0)

        var draw = ContextCompat.getDrawable(this, R.drawable.ac)

        draw = resize(draw ?: return)
        binding.imageView.setImageDrawable(draw)

        binding.passwordEditText.setText(password, TextView.BufferType.EDITABLE)
        binding.passwordEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                closeKeyboard(this)
                attemptToConfigure()
                true
            } else {
                false
            }
        }
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard(this)
            }
        }
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    closeKeyboard(this)
                    attemptToConfigure()
                    true
                }

                else -> false
            }
        }

        binding.emailEditText.setText(email, TextView.BufferType.EDITABLE)
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard(this)
            }
        }
        binding.emailEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                binding.passwordEditText.requestFocus()
                true
            } else {
                false
            }
        }
        binding.emailEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    binding.passwordEditText.requestFocus()
                    true
                }

                else -> false
            }
        }

        binding.emailEditText.requestFocus()
    }

    override fun onStart() {
        super.onStart()
//        if (Statics.SUPER_DEMO_MODE) {
//            val env = DotenvBuilder()
//                .directory("/assets")
//                .filename("env")
//                .load()
//            val environment = env["CLIENT_UADE_QR"]
//            scannerCompleted(environment)
//        }
    }

    private fun clearOldPrefs() {
        cleanPrefs()
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
        if (password != realPass) {
            makeText(binding.root, getString(R.string.invalid_password), ERROR)
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
                // Vamos a reconstruir el scanner por si cambió la configuración
                JotterListener.autodetectDeviceModel(this)

                if (Repository.urlPanel.isEmpty()) {
                    makeText(binding.root, getString(R.string.server_is_not_configured), ERROR)
                    return@registerForActivityResult
                }

                closeKeyboard(this)

                setResult(RESULT_OK)
                finish()
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
            } finally {
                rejectNewInstances = false
            }
        }

    private var isConfiguring = false
    private fun attemptToConfigure() {
        if (isConfiguring) return
        isConfiguring = true

        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (email.trim().isNotEmpty() && password.trim().isNotEmpty()) {
            if (!binding.proxyCheckBox.isChecked) {
                ConfigHelper.getConfig(
                    email = email, password = password, installationCode = ""
                ) {
                    onTaskGetPackagesEnded(it)
                }
            } else {
                setupProxy(
                    callback = this,
                    weakAct = WeakReference(this),
                    email = email,
                    password = password
                )
            }
        } else {
            isConfiguring = false
        }
    }

    private fun setProgressBarText(text: String = "", percent: String = "") {
        runOnUiThread {
            run {
                if (text.isNotEmpty() || percent.isNotEmpty()) showProgressBar(true)

                binding.syncStatusTextView.text = text
                binding.syncPercentTextView.text = percent
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        runOnUiThread {
            if (show && binding.progressBarLayout.visibility != View.VISIBLE) {
                binding.progressBarLayout.bringToFront()
                binding.progressBarLayout.visibility = View.VISIBLE

                ViewCompat.setZ(binding.progressBarLayout, 0F)
            } else if (!show && binding.progressBarLayout.visibility != View.GONE) {
                binding.progressBarLayout.visibility = View.GONE

                // Limpiamos los textos...
                setProgressBarText()
            }
        }
    }


    private fun resize(image: Drawable): Drawable {
        val bitmap = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * 0.5).toInt(), (bitmap.height * 0.5).toInt(), false
        )
        return BitmapDrawable(resources, bitmapResized)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            JotterListener.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, INFO)
        JotterListener.lockScanner(this, true)
        JotterListener.hideWindow(this)

        try {
            val mainJson = JSONObject(scanCode)

            when {
                mainJson.has("config") -> {
                    ConfigHelper.getConfigFromScannedCode(
                        scanCode = scanCode, mode = QRConfigClientAccount
                    ) { onTaskGetPackagesEnded(it) }
                }

                mainJson.has(appName) -> {
                    if (scanCode.contains(Preference.acWsServer.key)) {
                        ConfigHelper.getConfigFromScannedCode(
                            scanCode = scanCode, mode = QRConfigType.QRConfigWebservice
                        ) { onTaskGetPackagesEnded(it) }
                    }
                }

                else -> {
                    makeText(binding.root, getString(R.string.invalid_code), ERROR)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(binding.root, ex.message.toString(), ERROR)
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_login, menu)

        if (!prefsGetBoolean(Preference.showConfButton)) {
            menu.removeItem(menu.findItem(R.id.action_settings).itemId)
        }

        if (!isRfidRequired(this)) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
    }

    private fun isBackPressed() {
        val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                isBackPressed()
                return true
            }

            R.id.action_settings -> {
                configApp()
                true
            }

            R.id.action_rfid_connect -> {
                JotterListener.rfidStart(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_trigger_scan -> {
                if (Statics.SUPER_DEMO_MODE && BuildConfig.DEBUG) {
                    val env = DotenvBuilder()
                        .directory("/assets")
                        .filename("env")
                        .load()

                    // val uade = env["CLIENT_UADE_QR"]
                    // scannerCompleted(uade)

                    // val qrHttp = env["CLIENT_HTTP_QR"]
                    // scannerCompleted(qrHttp)

                    // val username = env["CLIENT_EMAIL_CP"]
                    // val password = env["CLIENT_PASSWORD_CP"]

                    val username = env["CLIENT_EMAIL"]
                    val password = env["CLIENT_PASSWORD"]
                    scannerCompleted("""{"config":{"client_email":"$username","client_password":"$password"}}""".trimIndent())
                    return super.onOptionsItemSelected(item)
                }

                JotterListener.trigger(this)
                return super.onOptionsItemSelected(item)
            }

            R.id.action_read_barcode -> {
                JotterListener.toggleCameraFloatingWindowVisibility(this)
                return super.onOptionsItemSelected(item)
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}