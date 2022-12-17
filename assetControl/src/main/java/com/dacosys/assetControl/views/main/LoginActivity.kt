package com.dacosys.assetControl.views.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.databinding.LoginActivityBinding
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.model.users.user.dbHelper.UserDbHelper
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.network.download.DownloadStatus
import com.dacosys.assetControl.network.download.DownloadStatus.*
import com.dacosys.assetControl.network.download.DownloadTask
import com.dacosys.assetControl.network.download.FileType
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.OFFLINE_MODE
import com.dacosys.assetControl.utils.Statics.Companion.appName
import com.dacosys.assetControl.utils.Statics.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Statics.Companion.prefsGetString
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.configuration.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.configuration.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.configuration.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.Md5
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackBarEventData
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import com.dacosys.assetControl.views.sync.viewModels.DownloadDbViewModel
import com.dacosys.assetControl.views.sync.viewModels.SyncViewModel
import com.dacosys.assetControl.views.users.fragment.UserSpinnerFragment
import com.dacosys.imageControl.dbHelper.ImageControlDbHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity(), UserSpinnerFragment.OnItemSelectedListener,
    Scanner.ScannerListener, Statics.TaskConfigEnded, Statics.Companion.TaskConfigPanelEnded {
    override fun onTaskConfigPanelEnded(status: ProgressStatus) {
        if (status == ProgressStatus.finished) {
            makeText(binding.root, getString(R.string.configuration_applied), SnackBarType.SUCCESS)
            initialSetup()
        } else if (status == ProgressStatus.crashed) {
            makeText(binding.root, getString(R.string.error_setting_user_panel), SnackBarType.ERROR)
        }
    }

    private fun onTaskGetPackagesEnded(it: ClientPackagesProgress) {
        if (isDestroyed || isFinishing) return

        val status: ProgressStatus = it.status
        val result: ArrayList<JSONObject> = it.result
        val clientEmail: String = it.clientEmail
        val clientPassword: String = it.clientPassword
        val msg: String = it.msg

        if (status == ProgressStatus.finished) {
            if (result.size > 0) {
                runOnUiThread {
                    Statics.selectClientPackage(parentView = binding.login,
                        callback = this,
                        weakAct = WeakReference(this),
                        allPackage = result,
                        email = clientEmail,
                        password = clientPassword)
                }
            } else {
                makeText(binding.root, msg, SnackBarType.INFO)
            }
        } else if (status == ProgressStatus.success) {
            makeText(binding.root, msg, SnackBarType.SUCCESS)
            initialSetup()
        }
        if (status == ProgressStatus.running) {
            setButton(ButtonStyle.BUSY)
        } else if (status == ProgressStatus.crashed || status == ProgressStatus.canceled) {
            // Error de conexión
            makeText(binding.root, msg, SnackBarType.ERROR)
            attemptSync = false
            JotterListener.resumeReaderDevices(this)
            setButton(ButtonStyle.REFRESH)
        }
    }

    override fun onDestroy() {
        destroyLocals()
        super.onDestroy()
    }

    private fun destroyLocals() {
        userSpinnerFragment?.onDestroy()
    }

    private fun onSyncTaskProgress(it: SyncProgress) {
        if (isDestroyed || isFinishing) return

        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        if (registryType != null) {
            setProgressBarText("${registryType.description}: ${
                Statics.getPercentage(completedTask, totalTask)
            }")
        }

        when (progressStatus) {
            ProgressStatus.starting,
            ProgressStatus.bigStarting,
            ProgressStatus.running,
            -> {
                setButton(ButtonStyle.BUSY)
                showProgressBar(true)
            }
            ProgressStatus.bigFinished -> {
                setButton(ButtonStyle.READY)
                showProgressBar(false)
                attemptRunning = false
                finish()
            }
            ProgressStatus.bigCrashed,
            ProgressStatus.crashed,
            ProgressStatus.canceled,
            -> {
                makeText(binding.root, msg, SnackBarType.ERROR)
                refreshUsers()
                attemptRunning = false
                showProgressBar(false)
            }
        }
    }

    private fun onDownloadDbTask(it: DownloadTask) {
        if (isDestroyed || isFinishing) return

        val msg: String = it.msg
        val fileType: FileType? = it.fileType
        val downloadStatus: DownloadStatus = it.downloadStatus
        val progress: Int = it.progress
        val bytesCompleted: Long = it.bytesCompleted
        val bytesTotal: Long = it.bytesTotal

        if (downloadStatus == CRASHED) {
            // Error al descargar
            makeText(binding.root, msg, SnackBarType.ERROR)

            attemptSync = false
            setButton(ButtonStyle.REFRESH)

            setProgressBarText("")
            showProgressBar(false)
            return
        } else if (downloadStatus == CANCELED) {
            // CANCELED = Sin conexión
            makeText(binding.root, msg, SnackBarType.INFO)

            refreshTitle()
            refreshUsers()

            setProgressBarText("")
            showProgressBar(false)
            return
        }

        if (fileType == FileType.TIMEFILE) return

        when (downloadStatus) {
            STARTING -> {
                setProgressBarText(msg)
                showProgressBar(true)
            }
            FINISHED -> {
                // FINISHED = Ok
                refreshTitle()
                refreshUsers()
                showProgressBar(false)
            }
            DOWNLOADING -> {
                Log.w(this::class.java.simpleName,
                    "${downloadStatus.name}: ${bytesCompleted}/${bytesTotal} (${progress}%)")
                setProgressBarText("${getString(R.string.downloading_)} ${progress}%")
            }
            CANCELED,
            CRASHED,
            INFO,
            -> {
            }
        }
    }

    override fun onTaskConfigEnded(result: Boolean, msg: String) {
        if (result) {
            makeText(binding.root, msg, SnackBarType.SUCCESS)
            initialSetup()
        } else {
            makeText(binding.root, msg, SnackBarType.ERROR)
        }
    }

    private var userSpinnerFragment: UserSpinnerFragment? = null
    private var firstTime = true
    private var rejectNewInstances = false
    private var isReturnedFromSettings = false

    private var userId: Long? = -1
    private var password: String = ""

    private fun onSessionCreated(result: Boolean) {
        if (OFFLINE_MODE) {
            makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
            attemptRunning = false
            finish()
        } else {
            if (!result) {
                makeText(binding.root, getString(R.string.offline_mode), SnackBarType.INFO)
                attemptRunning = false
                finish()
            } else {
                showProgressBar(true)
                setButton(ButtonStyle.BUSY)

                // Cerramos la base...
                DataBaseHelper().close()
                SQLiteDatabase.releaseMemory()

                // Enviar las imágenes pendientes...
                com.dacosys.imageControl.Statics.sendPendingImages()

                thread {
                    SyncDownload(onSyncTaskProgress = { syncViewModel.setSyncDownloadProgress(it) },
                        onSessionCreated = { syncViewModel.setSessionCreated(it) })
                }
            }
        }
    }

    private fun setEditTextFocus(isFocused: Boolean) {
        binding.password.isCursorVisible = isFocused
        binding.password.isFocusable = isFocused
        binding.password.isFocusableInTouchMode = isFocused

        if (isFocused) {
            binding.password.requestFocus()
        }
    }

    override fun onResume() {
        super.onResume()

        JotterListener.lockScanner(this, false)
        rejectNewInstances = false

        // Parece que las actividades de tipo Setting no devuelven resultados
        // así que de esta manera puedo volver a llenar el fragmento de usuarios
        if (isReturnedFromSettings) {
            // Vamos a reconstruir el scanner por si cambió la configuración
            JotterListener.autodetectDeviceModel(this)

            isReturnedFromSettings = false
            attemptSync = false
            attemptRunning = false
        }

        initialSetup()
    }

    private fun refreshUsers() {
        if (Statics.downloadDbRequired) {
            if (!Statics.isOnline()) {
                showSnackBar(SnackBarEventData(getString(R.string.download_db_required_and_no_connection),
                    SnackBarType.ERROR))
            }
            setButton(ButtonStyle.REFRESH)
            attemptSync = false
            return
        }

        /////////////// BASE DE DATOS SQLITE /////////////////
        // Acá arranca la base de datos, si no existe se crea.
        DataBaseHelper.beginDataBase()

        // Acá arranca la base de datos de ImageControl, si no existe se crea.
        if (Statics.useImageControl) {
            ImageControlDbHelper.beginDataBase()
        }
        ///////////// FIN INICIALIZACIÓN SQLITE //////////////

        try {
            runOnUiThread {
                if (userSpinnerFragment?.fillAdapter() == true) {
                    setButton(ButtonStyle.READY)
                } else {
                    makeText(binding.root,
                        getString(R.string.there_are_no_users_you_must_synchronize_the_database),
                        SnackBarType.ERROR)
                    setButton(ButtonStyle.REFRESH)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            if (!Statics.isOnline()) {
                makeText(binding.root, getString(R.string.no_connection), SnackBarType.INFO)
            }
            attemptSync = false
        }
    }

    private var connectionSuccess = false

    // region Cambiar el aspecto del botón de ingreso

    enum class ButtonStyle {
        READY, REFRESH, BUSY
    }

    private fun setButton(style: ButtonStyle) {
        runOnUiThread {
            connectionSuccess = style == ButtonStyle.READY
            when (style) {
                ButtonStyle.READY -> setLoginButton()
                ButtonStyle.REFRESH -> setRefreshButton()
                ButtonStyle.BUSY -> setWaitButton()
            }
        }
    }

    private fun setRefreshButton() {
        runOnUiThread {
            binding.loginImageView.setImageResource(R.drawable.ic_refresh)
            binding.loginImageView.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_button_gold, null)
            binding.loginImageView.contentDescription = getString(R.string.retry_connection)
            binding.loginImageView.foregroundTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.black, null))
        }
    }

    private fun setWaitButton() {
        runOnUiThread {
            binding.loginImageView.setImageResource(R.drawable.ic_hourglass)
            binding.loginImageView.background = ResourcesCompat.getDrawable(resources,
                R.drawable.rounded_corner_button_steelblue,
                null)
            binding.loginImageView.contentDescription = getString(R.string.connecting)
            binding.loginImageView.foregroundTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.white, null))
        }
    }

    private fun setLoginButton() {
        runOnUiThread {
            binding.loginImageView.setImageResource(R.drawable.ic_check)
            binding.loginImageView.background = ResourcesCompat.getDrawable(resources,
                R.drawable.rounded_corner_button_seagreen,
                null)
            binding.loginImageView.contentDescription = getString(R.string.sign_in)
            binding.loginImageView.foregroundTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.cornsilk, null))
        }
    }
    // endregion

    override fun onItemSelected(user: User?) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article

        // El ADAPTER dispara siempre este evento la primera vez
        if (firstTime) {
            firstTime = false
        }

        setEditTextFocus(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Set up touch setOnTouchListener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { _, motionEvent ->
                closeKeyboard(this)
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

    override fun onBackPressed() {
        // Esto sirve para salir del programa desde la pantalla de Login
        moveTaskToBack(true)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putLong("userId", userSpinnerFragment?.selectedUserId ?: -1)
        savedInstanceState.putString("password", binding.password.text.toString())
    }

    private lateinit var binding: LoginActivityBinding
    private val syncViewModel: SyncViewModel by viewModels()
    private val downloadDbViewModel: DownloadDbViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statics.setScreenRotation(this)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        syncViewModel.syncDownloadProgress.observe(this) { if (it != null) onSyncTaskProgress(it) }
        syncViewModel.sessionCreated.observe(this) { if (it != null) onSessionCreated(it) }
        downloadDbViewModel.downloadTaskEvent.observe(this) { if (it != null) onDownloadDbTask(it) }
        downloadDbViewModel.uiEvent.observe(this) { if (it != null) showSnackBar(it) }

        userSpinnerFragment =
            supportFragmentManager.findFragmentById(binding.userSpinnerFragment.id) as UserSpinnerFragment

        if (savedInstanceState != null) {
            userId = savedInstanceState.getLong("userId")
            password = savedInstanceState.getString("pass") ?: ""
        } else {
            val extras = intent.extras
            if (extras != null) {
                userId = extras.getLong("userId")
                password = extras.getString("password") ?: ""
            }
        }

        binding.imageView.setImageResource(0)

        var draw = ContextCompat.getDrawable(this, R.drawable.ac)

        draw = resize(draw ?: return)
        binding.imageView.setImageDrawable(draw)
        // endregion CABECERA DE LA ACTIVIDAD

        userSpinnerFragment?.selectedUserId = userId

        binding.password.setText(password, TextView.BufferType.EDITABLE)
        binding.password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        binding.password.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == EditorInfo.IME_ACTION_DONE || (keyEvent.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER))) {
                binding.loginImageView.performClick()
                true
            } else {
                false
            }
        }
        binding.password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !KeyboardVisibilityEvent.isKeyboardVisible(this)) {
                Statics.showKeyboard(this)
            } else {
                closeKeyboard(this)
            }
        }
        binding.password.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    binding.loginImageView.performClick()
                    true
                }
                else -> false
            }
        }

        binding.loginImageView.setOnClickListener {
            if (connectionSuccess) {
                runOnUiThread { attemptLogin() }
            } else {
                initialSetup()
            }
        }

        refreshTitle()
        showProgressBar(false)

        initialSetup()

        // ESTO SIRVE PARA OCULTAR EL TECLADO EN PANTALLA CUANDO PIERDEN EL FOCO LOS CONTROLES QUE LO NECESITAN
        setupUI(binding.root)
    }

    @SuppressLint("SetTextI18n")
    private fun refreshTitle() {
        runOnUiThread {
            try {
                // region CABECERA DE LA ACTIVIDAD
                binding.versionTextView.text = "${getString(R.string.app_milestone)} ${
                    packageManager.getPackageInfo(packageName, 0).versionName
                }"
                binding.packageTextView.text = Statics.clientPackage
                when {
                    Statics.clientPackage.isEmpty() -> binding.packageTextView.visibility =
                        View.GONE
                    else -> binding.packageTextView.visibility = View.VISIBLE
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(this, this::class.java.simpleName, ex)
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

    private fun showProgressBar(show: Boolean) {
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

    private var attemptSync: Boolean = false
    private fun initialSetup() {
        if (attemptSync) return
        attemptSync = true

        setButton(ButtonStyle.BUSY)

        // Comprobar validez de la fecha del dispositivo
        if (Statics.deviceDateIsValid(binding.root)) {
            if (Statics.wsUrl.isEmpty() || Statics.wsNamespace.isEmpty()) {
                showSnackBar(SnackBarEventData(getString(R.string.webservice_is_not_configured),
                    SnackBarType.ERROR))
                attemptSync = false
            } else {
                initSync()
            }
        } else {
            attemptSync = false
        }
    }

    private fun initSync() {
        try {
            if (OFFLINE_MODE) {
                refreshTitle()
                refreshUsers()
            } else {
                /* Des-inicializamos IC para evitar que se
                   suban imágenes pendientes antes de loggearse.
                   Escenario en el que el usuario ha vuelto a esta
                   actividad después haber estado loggeado.
                 */
                Statics.closeImageControl()

                if (!Statics.isOnline()) {
                    refreshTitle()
                    refreshUsers()
                } else {
                    thread {
                        DownloadDb(onDownloadEvent = { downloadDbViewModel.setDownloadTask(it) },
                            onSnackBarEvent = { downloadDbViewModel.setUiEvent(it) })
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)

            attemptSync = false
        }
    }

    private fun showSnackBar(it: SnackBarEventData) {
        if (isDestroyed || isFinishing) return

        makeText(binding.root, it.text, it.snackBarType)
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
        input.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        if (alertDialog != null) {
                            alertDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                        }
                    }
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
            Statics.setDebugConfigValues()

            if (!rejectNewInstances) {
                rejectNewInstances = true

                val intent = Intent(baseContext, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            isReturnedFromSettings = true
        } else {
            makeText(binding.root, getString(R.string.invalid_password), SnackBarType.ERROR)
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

    private fun resize(image: Drawable): Drawable {
        val bitmap = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(bitmap,
            (bitmap.width * 0.5).toInt(),
            (bitmap.height * 0.5).toInt(),
            false)
        return BitmapDrawable(resources, bitmapResized)
    }

    private var attemptRunning: Boolean = false
    private fun attemptLogin() {
        if (attemptRunning) return
        attemptRunning = true

        // Store values at the time of the login attempt.
        val userId = userSpinnerFragment?.selectedUserId ?: -1
        val userPass = userSpinnerFragment?.selectedUserPass ?: ""
        val password = binding.password.text.toString()

        runOnUiThread {
            attemptLogin(userId = userId, encondedPass = userPass, password = password)
        }
    }

    private fun attemptLogin(userId: Long?, encondedPass: String, password: String) {
        // Reset errors.
        binding.password.error = null

        var cancel = false
        var focusView: View? = null

        // Check for a valid email address.
        if (userId == null || userId <= 0) {
            focusView = userSpinnerFragment?.view
            makeText(binding.root, getString(R.string.you_must_select_a_user), SnackBarType.ERROR)
            cancel = true
        } else if (!isUserValid(userId)) {
            focusView = userSpinnerFragment?.view
            makeText(binding.root, getString(R.string.you_must_select_a_user), SnackBarType.ERROR)
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            attemptRunning = false
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (encondedPass == Md5.getMd5(password)) {
                Statics.currentUserId = userId
                Statics.setupImageControl()

                thread {
                    SetCurrentSession { syncViewModel.setSessionCreated(it) }
                }
            } else {
                showSnackBar(SnackBarEventData(getString(R.string.wrong_user_password_combination),
                    SnackBarType.ERROR))
                attemptRunning = false
            }
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
            grantResults)
    }

    override fun scannerCompleted(scanCode: String) {
        JotterListener.lockScanner(this, true)
        JotterListener.hideWindow(this)

        try {
            val mainJson = JSONObject(scanCode)

            // LOGIN
            if (scanCode.contains("log_user_name") && scanCode.contains("log_password")) {
                val confJson = mainJson.getJSONObject(appName)

                val userName = confJson.getString("log_user_name")
                val userPass = confJson.getString("log_password")

                if (userName.isNotEmpty() && userPass.isNotEmpty()) {
                    val user = UserDbHelper().selectUserByNameOrEmail(userName)
                    if (user != null) {
                        runOnUiThread {
                            attemptLogin(userId = user.userId,
                                encondedPass = userPass,
                                password = user.password)
                        }
                    } else {
                        makeText(binding.root, getString(R.string.invalid_user), SnackBarType.ERROR)
                    }
                } else {
                    makeText(binding.root, getString(R.string.invalid_code), SnackBarType.ERROR)
                }
                return
            }

            when {
                mainJson.has("config") -> {
                    Statics.getConfigFromScannedCode(scanCode = scanCode,
                        mode = QRConfigClientAccount) { onTaskGetPackagesEnded(it) }
                }
                mainJson.has(appName) -> {
                    when {
                        scanCode.contains(Preference.acWsServer.key) -> {
                            val adb = AlertDialog.Builder(this)
                            adb.setTitle(getString(R.string.download_database_required))
                            adb.setMessage(getString(R.string.download_database_required_question))
                            adb.setNegativeButton(R.string.cancel, null)
                            adb.setPositiveButton(R.string.accept) { _, _ ->
                                Statics.downloadDbRequired = true
                                Statics.getConfigFromScannedCode(scanCode = scanCode,
                                    mode = QRConfigWebservice) { onTaskGetPackagesEnded(it) }
                            }
                            adb.show()
                        }
                        else -> {
                            // APP CONFIGURATION
                            Statics.getConfigFromScannedCode(scanCode = scanCode,
                                mode = QRConfigApp) { onTaskGetPackagesEnded(it) }
                        }
                    }
                }
                else -> {
                    makeText(binding.root, getString(R.string.invalid_code), SnackBarType.ERROR)
                }
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

    private fun isUserValid(userId: Long): Boolean {
        return userId > 0
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_login, menu)

        if (!Statics.prefsGetBoolean(Preference.showConfButton)) {
            menu.removeItem(menu.findItem(R.id.action_settings).itemId)
        }

        if (!Statics.isRfidRequired()) {
            menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.home, android.R.id.home -> {
                onBackPressed()
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
                ///* For Debug */
                //scannerCompleted(
                //    """{"config":{"client_email":"miguel@dacosys.com","client_password":"sarasa123!!"}}""".trimIndent()
                //)
                //return super.onOptionsItemSelected(item)

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