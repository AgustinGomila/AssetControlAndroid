package com.dacosys.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceClickListener
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.DATABASE_NAME
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.copyDataBase
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.removeDataBases
import com.dacosys.assetControl.databinding.SettingsActivityBinding
import com.dacosys.assetControl.network.checkConn.CheckWsConnection
import com.dacosys.assetControl.network.checkConn.ImageControlCheckUser
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.network.utils.*
import com.dacosys.assetControl.network.utils.ClientPackage.Companion.selectClientPackage
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import com.dacosys.assetControl.utils.Collector.Companion.collectorTypeChanged
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetString
import com.dacosys.assetControl.utils.Preferences.Companion.prefsPutString
import com.dacosys.assetControl.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.OFFLINE_MODE
import com.dacosys.assetControl.utils.Statics.Companion.generateQrCode
import com.dacosys.assetControl.utils.Statics.Companion.getBarcodeForConfig
import com.dacosys.assetControl.utils.Statics.Companion.getConfigFromScannedCode
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.errorLog.ErrorLog.Companion.getLastErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.scanners.rfid.Rfid
import com.dacosys.assetControl.utils.scanners.rfid.Rfid.Companion.isRfidRequired
import com.dacosys.assetControl.utils.scanners.rfid.RfidType
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt
import com.dacosys.assetControl.utils.scanners.vh75.Vh75Bt.Companion.STATE_CONNECTED
import com.dacosys.assetControl.utils.settings.PathHelper
import com.dacosys.assetControl.utils.settings.QRConfigType
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigImageControl
import com.dacosys.assetControl.utils.settings.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.utils.settings.collectorType.CollectorType
import com.dacosys.assetControl.utils.settings.collectorType.CollectorTypePreference
import com.dacosys.assetControl.utils.settings.devices.DevicePreference
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread
import com.dacosys.assetControl.utils.settings.Preference.Companion as PreferencesApp


/**
 * A [SettingsActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, Scanner.ScannerListener,
    Statics.TaskConfigEnded, ClientPackage.Companion.TaskConfigPanelEnded {
    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey)
        }
    }

    companion object {
        //region CAMERA SCAN
        var currentQRConfigType = QRConfigApp

        fun doScanWork(qrConfigType: QRConfigType) {
            currentQRConfigType = qrConfigType
            // TODO: JotterListener.toggleCameraFloatingWindowVisibility(null)
        }
        //endregion CAMERA READER

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->
                val stringValue = value.toString()

                if (preference is ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                        if (index >= 0) preference.entries[index]
                        else null
                    )
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
                }
                true
            }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(
            frag: PreferenceFragmentCompat,
            pref: ConfEntry,
        ) {
            val preference = frag.findPreference<Preference>(pref.description) ?: return
            val defaultValue: Any = pref.defaultValue
            bindPreferenceSummaryToValue(preference, defaultValue)
        }

        private fun bindPreferenceSummaryToValue(
            frag: PreferenceFragmentCompat,
            pref: com.dacosys.assetControl.utils.settings.Preference,
        ) {
            val preference = frag.findPreference<Preference>(pref.key) ?: return
            val defaultValue: Any? = if (BuildConfig.DEBUG) pref.debugValue else pref.defaultValue
            bindPreferenceSummaryToValue(preference, defaultValue)
        }

        private fun bindPreferenceSummaryToValue(
            preference: Preference,
            defaultValue: Any?,
        ) {
            val all: Map<String, *> =
                PreferenceManager.getDefaultSharedPreferences(getContext()).all

            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            when {
                all[preference.key] is String -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, defaultValue.toString())
                    )
                }
                all[preference.key] is Boolean -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(preference.context)
                            .getBoolean(preference.key, defaultValue.toString().toBoolean())
                    )
                }
                all[preference.key] is Float -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(preference.context)
                            .getFloat(preference.key, defaultValue.toString().toFloat())
                    )
                }
                all[preference.key] is Int -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(preference.context)
                            .getInt(preference.key, defaultValue.toString().toInt())
                    )
                }
                all[preference.key] is Long -> {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(preference.context)
                            .getLong(preference.key, defaultValue.toString().toLong())
                    )
                }
                else -> {
                    try {
                        when (defaultValue) {
                            is String -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                preference,
                                PreferenceManager.getDefaultSharedPreferences(preference.context)
                                    .getString(preference.key, defaultValue)
                            )
                            is Float -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                preference,
                                PreferenceManager.getDefaultSharedPreferences(preference.context)
                                    .getFloat(preference.key, defaultValue)
                            )
                            is Int -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                preference,
                                PreferenceManager.getDefaultSharedPreferences(preference.context)
                                    .getInt(preference.key, defaultValue)
                            )
                            is Long -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                preference,
                                PreferenceManager.getDefaultSharedPreferences(preference.context)
                                    .getLong(preference.key, defaultValue)
                            )
                            is Boolean -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                preference,
                                PreferenceManager.getDefaultSharedPreferences(preference.context)
                                    .getBoolean(preference.key, defaultValue)
                            )
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    }
                }
            }
        }

        private fun testWsConnection(
            parentView: View,
            url: String,
            namespace: String,
            useProxy: Boolean,
            proxyUrl: String,
            proxyPort: Int,
            proxyUser: String,
            proxyPass: String,
        ) {
            if (url.isEmpty() || namespace.isEmpty()) {
                showSnackBar(
                    parentView, SnackBarEventData(
                        getContext().getString(R.string.invalid_webservice_data), INFO
                    )
                )
                return
            }

            val x = CheckWsConnection(
                url = url,
                namespace = namespace,
                onSnackBarEvent = { showSnackBar(parentView, it) },
            )
            if (useProxy) {
                x.addProxyParams(
                    useProxy = true,
                    proxyUrl = proxyUrl,
                    proxyPort = proxyPort,
                    proxyUser = proxyUser,
                    proxyPass = proxyPass
                )
            }
            x.execute()
        }

        private fun showSnackBar(view: View, it: SnackBarEventData) {
            makeText(view, it.text, it.snackBarType)
        }

        /**
         * Limpia la información relacionada con la cuenta del usuario
         * ya que está configurando el webservice de manera manual
         */
        private fun cleanPanelWebData() {
            prefsPutString(
                PreferencesApp.urlPanel.key, ""
            )
            prefsPutString(
                PreferencesApp.installationCode.key, ""
            )
            prefsPutString(
                PreferencesApp.clientPackage.key, ""
            )
            prefsPutString(
                PreferencesApp.clientEmail.key, ""
            )
            prefsPutString(
                PreferencesApp.clientPassword.key, ""
            )
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        val args: Bundle = pref.extras
        val fragment: Fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, pref.fragment ?: ""
        )
        fragment.arguments = args

        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment)
            .addToBackStack(null).commit()
        supportFragmentManager.setFragmentResultListener("requestKey", this) { key, _ ->
            if (key == "requestKey") title = pref.title
        }
        return true
    }

    private lateinit var binding: SettingsActivityBinding
    private lateinit var titleTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Creamos un MenuProvider.
        createOptionsMenu()

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleTag = getString(R.string.settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            titleTag = savedInstanceState.getCharSequence("title").toString()
            title = titleTag
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                title = titleTag
            }
        }
    }

    private fun createOptionsMenu() {
        // Add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                // Inflate the menu; this adds items to the action bar if it is present.
                menuInflater.inflate(R.menu.menu_read_activity, menu)
                menu.removeItem(menu.findItem(R.id.action_trigger_scan).itemId)
                menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
                menu.removeItem(menu.findItem(R.id.action_read_barcode).itemId)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.home, android.R.id.home -> {
                        onBackPressed()
                        true
                    }
                    R.id.action_read_barcode -> {
                        doScanWork(QRConfigApp)
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        closeKeyboard(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current requireActivity() title so we can set it again after a configuration change
        outState.putCharSequence("title", title)
    }

    override fun onTaskConfigPanelEnded(status: ProgressStatus) {
        if (status == ProgressStatus.finished) {
            makeText(
                binding.settings, getString(R.string.configuration_applied), INFO
            )
            removeDataBases()
            finish()
        } else if (status == ProgressStatus.crashed) {
            makeText(
                binding.settings, getString(R.string.error_setting_user_panel), ERROR
            )
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
                    selectClientPackage(
                        parentView = binding.settings,
                        callback = this,
                        weakAct = WeakReference(this),
                        allPackage = result,
                        email = clientEmail,
                        password = clientPassword
                    )
                }
            } else {
                makeText(binding.settings, msg, INFO)
            }
        } else if (status == ProgressStatus.success) {
            makeText(binding.settings, msg, SnackBarType.SUCCESS)
        } else if (status == ProgressStatus.crashed || status == ProgressStatus.canceled) {
            makeText(binding.settings, msg, ERROR)
        }
    }

    override fun onTaskConfigEnded(result: Boolean, msg: String) {
        if (result) {
            makeText(binding.settings, msg, SnackBarType.SUCCESS)
        } else {
            makeText(binding.settings, msg, ERROR)
        }
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
            // No capturar códigos que cambian el servidor cuando está logeado.
            if (Statics.currentUserId != null && (currentQRConfigType == QRConfigClientAccount || currentQRConfigType == QRConfigWebservice)) {
                return
            }

            getConfigFromScannedCode(
                scanCode = scanCode, mode = currentQRConfigType
            ) { onTaskGetPackagesEnded(it) }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(
                binding.settings, ex.message.toString(), ERROR
            )
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            // Unless is blocked, unlock the partial
            JotterListener.lockScanner(this, false)
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class GeneralPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (arguments != null) {
                val key = requireArguments().getString("rootKey")
                setPreferencesFromResource(R.xml.pref_general, key)
            } else {
                setPreferencesFromResource(R.xml.pref_general, rootKey)
            }
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = GeneralPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acFilterRouteDescription
            )

            findPreference<Preference>(PreferencesApp.registryError.key) as Preference
            findPreference<Preference>(PreferencesApp.showConfButton.key) as Preference
            if (BuildConfig.DEBUG) {
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.confPassword
                )
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val removeLogFiles = findPreference<Preference>("remove_log_files")
            removeLogFiles?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                val diaBox = askForDelete()
                diaBox.show()
                true
            }

            val scanConfigCode = findPreference<Preference>("scan_config_code")
            scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
                try {
                    doScanWork(QRConfigApp)
                    true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    makeText(
                        requireView(), "${getString(R.string.error)}: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    false
                }
            }

            val qrCodeButton = findPreference<Preference>("ac_qr_code")
            qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
                generateQrCode(
                    WeakReference(requireActivity()), getBarcodeForConfig(
                        PreferencesApp.getAppConf(), Statics.appName
                    )
                )
                true
            }
        }

        private fun askForDelete(): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.do_you_want_to_delete_the_old_error_logs_question))
                .setPositiveButton(
                    getString(R.string.delete)
                ) { dialog, _ ->
                    deleteRecursive(File(ErrorLog.errorLogPath))
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.cancel
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        private fun deleteRecursive(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory) {
                val files = fileOrDirectory.listFiles()
                if (files != null && files.any()) {
                    for (file in files) {
                        deleteRecursive(file)
                    }
                }
            }

            fileOrDirectory.delete()
        }
    }

    class AccountPreferenceFragment : PreferenceFragmentCompat(),
        ClientPackage.Companion.TaskConfigPanelEnded {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }
            setPreferencesFromResource(R.xml.pref_account, key)
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = AccountPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        private var alreadyAnsweredYes = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            if (BuildConfig.DEBUG) {
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.clientEmail
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.clientPassword
                )
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val emailEditText = findPreference<Preference>(PreferencesApp.clientEmail.key)
            emailEditText?.setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }

            val passwordEditText = findPreference<Preference>(PreferencesApp.clientPassword.key)
            passwordEditText?.setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }

            val selectPackageButton = findPreference<Preference>("select_package")
            selectPackageButton?.onPreferenceClickListener = OnPreferenceClickListener {
                if (emailEditText != null && passwordEditText != null) {
                    val email = prefsGetString(PreferencesApp.clientEmail)
                    val password = prefsGetString(PreferencesApp.clientPassword)

                    if (!alreadyAnsweredYes) {
                        val diaBox = askForDownloadDbRequired2(
                            email = email, password = password
                        )
                        diaBox.show()
                    } else {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            Statics.getConfig(
                                email = email, password = password, installationCode = ""
                            ) { onTaskGetPackagesEnded(it) }
                        }
                    }
                }
                true
            }

            val scanConfigCode = findPreference<Preference>("scan_config_code")
            scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
                try {
                    doScanWork(QRConfigClientAccount)
                    true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    makeText(
                        requireView(), "${getString(R.string.error)}: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    false
                }
            }

            val qrCodeButton = findPreference<Preference>("ac_qr_code")
            qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
                val urlPanel = prefsGetString(PreferencesApp.urlPanel)
                val installationCode = prefsGetString(PreferencesApp.installationCode)
                val clientEmail = prefsGetString(PreferencesApp.clientEmail)
                val clientPassword = prefsGetString(PreferencesApp.clientPassword)
                val clientPackage = prefsGetString(PreferencesApp.clientPackage)

                if (urlPanel.isEmpty() || installationCode.isEmpty() || clientPackage.isEmpty() || clientEmail.isEmpty() || clientPassword.isEmpty()) {
                    makeText(
                        requireView(),
                        AssetControlApp.getContext().getString(R.string.invalid_client_data),
                        ERROR
                    )
                    return@OnPreferenceClickListener false
                }

                generateQrCode(
                    WeakReference(requireActivity()), getBarcodeForConfig(
                        PreferencesApp.getClient(), "config"
                    )
                )
                true
            }

            // Actualizar el programa
            val updateAppButton = findPreference<Preference>("update_app") as Preference
            updateAppButton.isEnabled = false
            updateAppButton.onPreferenceClickListener = OnPreferenceClickListener {
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !requireContext().packageManager.canRequestPackageInstalls()) {
                //     val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(
                //         Uri.parse(
                //             String.format("package:%s", requireContext().packageName)
                //         )
                //     )
                //     resultForRequestPackageInstall.launch(intent)
                // } else {
                //     // check storage permission granted if yes then start downloading file
                //     checkStoragePermission()
                // }

                makeText(requireView(), getString(R.string.no_available_option), INFO)
                true
            }

            // Si ya está loggeado, deshabilitar estas opciones
            if (Statics.currentUserId != null) {
                passwordEditText?.isEnabled = false
                emailEditText?.isEnabled = false
                selectPackageButton?.isEnabled = false
                scanConfigCode?.isEnabled = false
            }
        }

        // private val resultForRequestPackageInstall =
        //     registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //         if (it?.resultCode == CommonStatusCodes.SUCCESS || it?.resultCode == CommonStatusCodes.SUCCESS_CACHE) {
        //             // check storage permission granted if yes then start downloading file
        //             checkStoragePermission()
        //         }
        //     }
        //
        // private fun checkStoragePermission() {
        //     // Check if the storage permission has been granted
        //     if (ActivityCompat.checkSelfPermission(
        //             requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        //         ) != PackageManager.PERMISSION_GRANTED
        //     ) {
        //         // Permission is missing and must be requested.
        //         resultForStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //         return
        //     }
        //
        //     // start downloading
        //     val downloadController = DownloadController(requireView())
        //     downloadController.enqueueDownload()
        // }
        //
        // private val resultForStoragePermission =
        //     registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        //         // returns boolean representing whether the
        //         // permission is granted or not
        //         if (!isGranted) {
        //             makeText(
        //                 requireView(),
        //                 requireContext().getString(R.string.app_dont_have_necessary_permissions),
        //                 ERROR
        //             )
        //         } else {
        //             // start downloading
        //             val downloadController = DownloadController(requireView())
        //             downloadController.enqueueDownload()
        //         }
        //     }

        private fun askForDownloadDbRequired2(
            email: String,
            password: String,
        ): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.download_database_required))
                .setMessage(getString(R.string.download_database_required_question))
                .setPositiveButton(
                    getString(R.string.yes)
                ) { dialog, _ ->
                    //your deleting code
                    DownloadDb.downloadDbRequired = true
                    alreadyAnsweredYes = true

                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        Statics.getConfig(
                            email = email, password = password, installationCode = ""
                        ) { onTaskGetPackagesEnded(it) }
                    }
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.no
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        private fun askForDownloadDbRequired(
            preference: Preference,
            newValue: Any,
        ): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.download_database_required))
                .setMessage(getString(R.string.download_database_required_question))
                .setPositiveButton(
                    getString(R.string.yes)
                ) { dialog, _ ->
                    //your deleting code
                    DownloadDb.downloadDbRequired = true
                    preference.summary = newValue.toString()
                    alreadyAnsweredYes = true
                    if (newValue is String) {
                        prefsPutString(
                            preference.key, newValue
                        )
                    }
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.no
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        companion object {
            fun equals(a: Any?, b: Any?): Boolean {
                return a != null && a == b
            }

            /**
             * Binds a preference's summary to its value. More specifically, when the
             * preference's value is changed, its summary (line of text below the
             * preference title) is updated to reflect the value. The summary is also
             * immediately updated upon calling this method. The exact display format is
             * dependent on the type of preference.
             *
             * @see .sBindPreferenceSummaryToValueListener
             */
            private fun bindPreferenceSummaryToValue(
                frag: PreferenceFragmentCompat,
                pref: com.dacosys.assetControl.utils.settings.Preference,
            ) {
                val preference = frag.findPreference<Preference>(pref.key)
                val all: Map<String, *> =
                    PreferenceManager.getDefaultSharedPreferences(getContext()).all

                // Set the listener to watch for value changes.
                preference?.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

                val defaultValue: Any? =
                    if (BuildConfig.DEBUG) pref.debugValue else pref.defaultValue

                when {
                    all[pref.key] is String && preference != null -> {
                        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                            preference,
                            PreferenceManager.getDefaultSharedPreferences(preference.context)
                                .getString(preference.key, defaultValue.toString())
                        )
                    }
                    all[pref.key] is Boolean && preference != null -> {
                        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                            preference,
                            PreferenceManager.getDefaultSharedPreferences(preference.context)
                                .getBoolean(preference.key, defaultValue.toString().toBoolean())
                        )
                    }
                    all[pref.key] is Float && preference != null -> {
                        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                            preference,
                            PreferenceManager.getDefaultSharedPreferences(preference.context)
                                .getFloat(preference.key, defaultValue.toString().toFloat())
                        )
                    }
                    all[pref.key] is Int && preference != null -> {
                        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                            preference,
                            PreferenceManager.getDefaultSharedPreferences(preference.context)
                                .getInt(preference.key, defaultValue.toString().toInt())
                        )
                    }
                    all[pref.key] is Long && preference != null -> {
                        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                            preference,
                            PreferenceManager.getDefaultSharedPreferences(preference.context)
                                .getLong(preference.key, defaultValue.toString().toLong())
                        )
                    }
                    else -> {
                        try {
                            if (preference != null) when (defaultValue) {
                                is String -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                    preference,
                                    PreferenceManager.getDefaultSharedPreferences(preference.context)
                                        .getString(preference.key, defaultValue)
                                )
                                is Float -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                    preference,
                                    PreferenceManager.getDefaultSharedPreferences(preference.context)
                                        .getFloat(preference.key, defaultValue)
                                )
                                is Int -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                    preference,
                                    PreferenceManager.getDefaultSharedPreferences(preference.context)
                                        .getInt(preference.key, defaultValue)
                                )
                                is Long -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                    preference,
                                    PreferenceManager.getDefaultSharedPreferences(preference.context)
                                        .getLong(preference.key, defaultValue)
                                )
                                is Boolean -> sBindPreferenceSummaryToValueListener.onPreferenceChange(
                                    preference,
                                    PreferenceManager.getDefaultSharedPreferences(preference.context)
                                        .getBoolean(preference.key, defaultValue)
                                )
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        }
                    }
                }
            }
        }

        private fun onTaskGetPackagesEnded(it: ClientPackagesProgress) {
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return

            val status: ProgressStatus = it.status
            val result: ArrayList<JSONObject> = it.result
            val clientEmail: String = it.clientEmail
            val clientPassword: String = it.clientPassword
            val msg: String = it.msg

            if (status == ProgressStatus.finished) {
                if (result.size > 0) {
                    requireActivity().runOnUiThread {
                        selectClientPackage(
                            parentView = requireView(),
                            callback = this,
                            weakAct = WeakReference(requireActivity()),
                            allPackage = result,
                            email = clientEmail,
                            password = clientPassword
                        )
                    }
                } else {
                    if (view != null) makeText(
                        requireView(), msg, INFO
                    )
                }
            } else if (status == ProgressStatus.success) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        msg, SnackBarType.SUCCESS
                    )
                )
            } else if (status == ProgressStatus.crashed || status == ProgressStatus.canceled) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        msg, ERROR
                    )
                )
            }
        }

        override fun onTaskConfigPanelEnded(status: ProgressStatus) {
            if (status == ProgressStatus.finished) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.configuration_applied), INFO
                    )
                )
                removeDataBases()
                requireActivity().finish()
            } else if (status == ProgressStatus.crashed) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.error_setting_user_panel), ERROR
                    )
                )
            }
        }

        private fun showSnackBar(it: SnackBarEventData) {
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return

            makeText(requireView(), it.text, it.snackBarType)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DevicesPreferenceFragment : PreferenceFragmentCompat(), Rfid.RfidDeviceListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }
            setPreferencesFromResource(R.xml.pref_devices, key)

            // Llenar sólo el fragmento que se ve para evitar NullExceptions
            when (key) {
                "printer" -> {
                    setPrinterPref()
                }
                "rfid" -> {
                    setRfidPref()
                }
                "symbology" -> {}
                else -> {
                    setDevicesPref()
                }
            }
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = DevicesPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        private lateinit var v: View
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            v = super.onCreateView(inflater, container, savedInstanceState)
            return v
        }

        override fun onGetBluetoothName(name: String) {
            rfidDeviceNamePreference?.summary = name
        }

        override fun onReadCompleted(scanCode: String) {}

        override fun onWriteCompleted(isOk: Boolean) {}

        private fun setupRfidReader() {
            try {
                if (isRfidRequired()) {
                    Rfid.setListener(this, RfidType.vh75)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                makeText(v, getString(R.string.rfid_reader_not_initialized), INFO)
                ErrorLog.writeLog(activity, this::class.java.simpleName, ex)
            }
        }

        private fun setDevicesPref() {
            setPrinterPref()
            setCollectorPref()
            setRfidPref()
        }

        private fun setCollectorPref() {
            ////////////////// COLECTOR //////////////////
            bindPreferenceSummaryToValue(
                this, PreferencesApp.collectorType
            )

            // PERMITE ACTUALIZAR EN PANTALLA EL ITEM SELECCIONADO EN EL SUMMARY DEL CONTROL
            val collectorTypeListPreference =
                findPreference<Preference>(PreferencesApp.collectorType.key) as CollectorTypePreference
            if (collectorTypeListPreference.value == null) {
                // to ensure we don't selectByItemId a null value
                // set first value by default
                collectorTypeListPreference.setValueIndex(0)
            }

            collectorTypeListPreference.summary =
                CollectorType.getById(collectorTypeListPreference.value?.toInt() ?: 0).description
            collectorTypeListPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    preference.summary =
                        CollectorType.getById(newValue.toString().toInt()).description
                    collectorTypeChanged = true
                    true
                }
        }

        private val ipv4Regex =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"

        private val ipv4Pattern: Pattern = Pattern.compile(ipv4Regex)

        fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned?,
            dStart: Int,
            dEnd: Int,
        ): CharSequence? {
            if (source == "") return null // Para el backspace
            val builder = java.lang.StringBuilder(dest.toString())
            builder.replace(dStart, dEnd, source.subSequence(start, end).toString())
            val matcher: Matcher = ipv4Pattern.matcher(builder)
            return if (!matcher.matches()) "" else null
        }

        private var useBtPrinter = false
        private var useNetPrinter = false

        private fun getPrinterName(
            btPrinterName: CharSequence,
            netPrinterIp: CharSequence,
            netPrinterPort: CharSequence,
        ): String {
            val r = if (!useBtPrinter && !useNetPrinter) {
                getString(R.string.there_is_no_selected_printer)
            } else if (useBtPrinter && btPrinterName.isEmpty()) {
                getString(R.string.there_is_no_selected_printer)
            } else if (useNetPrinter && (netPrinterIp.isEmpty() || netPrinterPort.isEmpty())) {
                getString(R.string.there_is_no_selected_printer)
            } else {
                when {
                    useBtPrinter -> btPrinterName.toString()
                    useNetPrinter -> "$netPrinterIp ($netPrinterPort)"
                    else -> getString(R.string.there_is_no_selected_printer)
                }
            }
            return r
        }

        private fun setPrinterPref() {
            /////// PANTALLA DE CONFIGURACIÓN DE LA IMPRESORA ///////
            val printerPref = findPreference<Preference>("printer") as PreferenceScreen

            //region //// DEVICE LIST
            val deviceListPreference =
                findPreference<Preference>(PreferencesApp.printerBtAddress.key) as DevicePreference
            if (deviceListPreference.value == null) {
                // to ensure we don't selectByItemId a null value
                // set first value by default
                deviceListPreference.setValueIndex(0)
            }
            deviceListPreference.summary =
                if (deviceListPreference.entry.isNullOrEmpty()) getString(R.string.there_is_no_selected_printer)
                else deviceListPreference.entry
            deviceListPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, _ ->
                    val pn =
                        if (deviceListPreference.entry.isNullOrEmpty()) getString(R.string.there_is_no_selected_printer)
                        else deviceListPreference.entry.toString()
                    preference.summary = pn
                    if (useBtPrinter) printerPref.summary = pn
                    true
                }
            //endregion //// DEVICE LIST

            //region //// PRINTER IP / PORT
            val portNetPrinterPref =
                findPreference<Preference>(PreferencesApp.portNetPrinter.key) as EditTextPreference
            portNetPrinterPref.summary = portNetPrinterPref.text

            val ipNetPrinterPref =
                findPreference<Preference>(PreferencesApp.ipNetPrinter.key) as EditTextPreference
            ipNetPrinterPref.summary = ipNetPrinterPref.text

            ipNetPrinterPref.setOnBindEditTextListener {
                val filters = arrayOfNulls<InputFilter>(1)
                filters[0] = InputFilter { source, start, end, dest, dStart, dEnd ->
                    filter(source, start, end, dest, dStart, dEnd)
                }
                it.filters = filters
            }
            ipNetPrinterPref.setOnPreferenceChangeListener { _, newValue ->
                if (useNetPrinter && newValue != null) {
                    ipNetPrinterPref.summary = newValue.toString()
                    val pn = "$newValue (${portNetPrinterPref.text})"
                    printerPref.summary = pn
                }
                true
            }

            portNetPrinterPref.setOnPreferenceChangeListener { _, newValue ->
                if (useNetPrinter && newValue != null) {
                    portNetPrinterPref.summary = newValue.toString()
                    val pn = "${ipNetPrinterPref.text} ($newValue)"
                    printerPref.summary = pn
                }
                true
            }
            //endregion //// PRINTER IP / PORT

            //region //// USE BLUETOOTH / NET PRINTER
            val swPrefBtPrinter =
                findPreference<Preference>(PreferencesApp.useBtPrinter.key) as SwitchPreference
            useBtPrinter = swPrefBtPrinter.isChecked

            val swPrefNetPrinter =
                findPreference<Preference>(PreferencesApp.useNetPrinter.key) as SwitchPreference
            useNetPrinter = swPrefNetPrinter.isChecked

            swPrefBtPrinter.setOnPreferenceChangeListener { _, newValue ->
                useBtPrinter = newValue != null && newValue == true
                if (newValue == true) swPrefNetPrinter.isChecked = false
                val pn =
                    if (deviceListPreference.entry.isNullOrEmpty()) getString(R.string.there_is_no_selected_printer)
                    else deviceListPreference.entry.toString()
                if (useBtPrinter) printerPref.summary = pn
                true
            }
            swPrefNetPrinter.setOnPreferenceChangeListener { _, newValue ->
                useNetPrinter = newValue != null && newValue == true
                if (newValue == true) swPrefBtPrinter.isChecked = false
                val pn = "${ipNetPrinterPref.text} (${portNetPrinterPref.text})"
                if (useNetPrinter) printerPref.summary = pn
                true
            }
            //endregion //// USE BLUETOOTH / NET PRINTER

            //region //// POTENCIA Y VELOCIDAD
            val maxPower = 23
            val printerPowerPref =
                findPreference<Preference>(PreferencesApp.printerPower.key) as EditTextPreference
            printerPowerPref.summary = printerPowerPref.text
            printerPowerPref.setOnBindEditTextListener {
                val filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                    try {
                        val input = (dest.toString() + source.toString()).toInt()
                        if (input in 1 until maxPower) return@InputFilter null
                    } catch (nfe: NumberFormatException) {
                    }
                    ""
                })
                it.filters = filters
            }

            val maxSpeed = 10
            val printerSpeedPref =
                findPreference<Preference>(PreferencesApp.printerSpeed.key) as EditTextPreference
            printerSpeedPref.summary = printerSpeedPref.text
            printerSpeedPref.setOnBindEditTextListener {
                val filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                    try {
                        val input = (dest.toString() + source.toString()).toInt()
                        if (input in 1 until maxSpeed) return@InputFilter null
                    } catch (nfe: NumberFormatException) {
                    }
                    ""
                })
                it.filters = filters
            }
            //endregion //// POTENCIA Y VELOCIDAD

            //region //// CARACTER DE SALTO DE LÍNEA
            val swPrefCharLF =
                findPreference<Preference>("conf_printer_new_line_char_lf") as SwitchPreference
            val swPrefCharCR =
                findPreference<Preference>("conf_printer_new_line_char_cr") as SwitchPreference

            val lineSeparator = prefsGetString(PreferencesApp.lineSeparator)
            if (lineSeparator == Char(10).toString()) swPrefCharLF.isChecked
            else if (lineSeparator == Char(13).toString()) swPrefCharCR.isChecked

            swPrefCharLF.setOnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    prefsPutString(
                        PreferencesApp.lineSeparator.key, Char(10).toString()
                    )
                    swPrefCharCR.isChecked = false
                }
                true
            }

            swPrefCharCR.setOnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    prefsPutString(
                        PreferencesApp.lineSeparator.key, Char(13).toString()
                    )
                    swPrefCharLF.isChecked = false
                }
                true
            }
            //endregion //// CARACTER DE SALTO DE LÍNEA

            printerPref.summary = if (!useBtPrinter && !useNetPrinter) getString(R.string.disabled)
            else getPrinterName(
                btPrinterName = deviceListPreference.entry ?: "",
                netPrinterIp = ipNetPrinterPref.text.toString(),
                netPrinterPort = portNetPrinterPref.toString()
            )
        }

        private var useRfid = false
        private var rfidSummary = ""
        private var rfidName = ""

        private val resultForRfidConnect =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it?.resultCode == CommonStatusCodes.SUCCESS || it?.resultCode == CommonStatusCodes.SUCCESS_CACHE) {
                    setupRfidReader()
                }
            }

        private val resultForRfidPermissionConnect =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                // returns boolean representind whether the
                // permission is granted or not
                if (!isGranted) {
                    makeText(
                        v,
                        AssetControlApp.getContext()
                            .getString(R.string.app_dont_have_necessary_permissions),
                        ERROR
                    )
                } else {
                    setupRfidReader()
                }
            }

        // Esta preferencia se utiliza al recibir el nombre del dispositivo
        // RFID seleccionado para modificar el texto de su sumario.
        private var rfidDeviceNamePreference: EditTextPreference? = null

        private fun setRfidPref() {
            ////////////////// RFID DEVICE //////////////////
            val rfidPref = findPreference<Preference>("rfid") as PreferenceScreen

            //region //// USE RFID
            val swPrefBtRfid =
                findPreference<Preference>(PreferencesApp.useBtRfid.key) as SwitchPreference
            useRfid = swPrefBtRfid.isChecked

            swPrefBtRfid.setOnPreferenceChangeListener { _, newValue ->
                useRfid = newValue != null && newValue == true
                rfidSummary =
                    (if (useRfid) getString(R.string.enabled) else getString(R.string.disabled)) + ": " + rfidName
                rfidPref.summary = rfidSummary

                thread { connectToRfidDevice() }
                true
            }
            //endregion //// USE RFID

            //region //// BLUETOOTH NAME
            rfidDeviceNamePreference =
                findPreference<Preference>("rfid_bluetooth_name") as EditTextPreference
            if (Rfid.rfidDevice != null && (Rfid.rfidDevice as Vh75Bt).getState() == STATE_CONNECTED) {
                (Rfid.rfidDevice as Vh75Bt).getBluetoothName()
            }
            rfidDeviceNamePreference!!.setOnPreferenceClickListener {
                if (Rfid.rfidDevice == null || (Rfid.rfidDevice as Vh75Bt).getState() != STATE_CONNECTED) {
                    makeText(
                        v, getString(R.string.there_is_no_rfid_device_connected), ERROR
                    )
                }
                true
            }
            rfidDeviceNamePreference!!.setOnPreferenceChangeListener { _, newValue ->
                if (Rfid.rfidDevice != null && (Rfid.rfidDevice as Vh75Bt).getState() == STATE_CONNECTED) {
                    (Rfid.rfidDevice as Vh75Bt).setBluetoothName(newValue.toString())
                } else {
                    makeText(
                        v, getString(R.string.there_is_no_rfid_device_connected), ERROR
                    )
                }
                true
            }
            //endregion //// BLUETOOTH NAME

            //region //// DEVICE LIST PREFERENCE
            val deviceListPreference =
                findPreference<Preference>(PreferencesApp.rfidBtAddress.key) as DevicePreference
            if (deviceListPreference.value == null) {
                // to ensure we don't selectByItemId a null value
                // set first value by default
                deviceListPreference.setValueIndex(0)
            }
            deviceListPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    rfidName = getBluetoothNameFromAddress(
                        newValue, getString(R.string.there_is_no_selected_rfid_scanner)
                    )

                    preference.summary = rfidName

                    rfidSummary =
                        (if (useRfid) getString(R.string.enabled) else getString(R.string.disabled)) + ": " + rfidName
                    rfidPref.summary = rfidSummary

                    // De este modo se actualiza el Summary del PreferenceScreen padre
                    //(preferenceScreen.rootAdapter as BaseAdapter).notifyDataSetChanged()
                    true
                }
            deviceListPreference.summary = rfidName
            //endregion //// DEVICE LIST PREFERENCE

            //region //// RFID POWER
            val rfidReadPower =
                findPreference<Preference>(PreferencesApp.rfidReadPower.key) as SeekBarPreference
            rfidReadPower.setOnPreferenceChangeListener { _, newValue ->
                rfidReadPower.summary = "$newValue dB"
                true
            }
            rfidReadPower.summary = "${prefsGetInt(PreferencesApp.rfidReadPower)} dB"
            //endregion //// RFID POWER

            //region //// RESET TO FACTORY
            val resetButton = findPreference<Preference>("rfid_reset_to_factory") as Preference
            resetButton.onPreferenceClickListener = OnPreferenceClickListener {
                if (Rfid.rfidDevice != null && (Rfid.rfidDevice as Vh75Bt).getState() == STATE_CONNECTED) {
                    val diaBox = askForResetToFactory()
                    diaBox.show()
                } else {
                    makeText(
                        v, getString(R.string.there_is_no_rfid_device_connected), ERROR
                    )
                }
                true
            }
            //endregion //// RESET TO FACTORY

            rfidName = if (deviceListPreference.entry == null || !swPrefBtRfid.isChecked) {
                getString(R.string.there_is_no_selected_rfid_scanner)
            } else {
                deviceListPreference.entry!!.toString()
            }

            rfidPref.summary =
                "${if (useRfid) getString(R.string.enabled) else getString(R.string.disabled)}: $rfidName"

            thread { connectToRfidDevice() }
        }

        private fun connectToRfidDevice() {
            if (!useRfid) return

            val bluetoothManager =
                AssetControlApp.getContext().getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val mBluetoothAdapter = bluetoothManager.adapter
            if (mBluetoothAdapter == null) {
                showSnackBar(
                    SnackBarEventData(
                        getString(R.string.there_are_no_bluetooth_devices), INFO
                    )
                )
            } else {
                if (!mBluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBtIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            resultForRfidPermissionConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        return
                    }
                    resultForRfidConnect.launch(enableBtIntent)
                } else {
                    setupRfidReader()
                }
            }
        }

        private fun askForResetToFactory(): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.reset_to_factory))
                .setMessage(getString(R.string.you_want_to_reset_the_rfid_device_to_its_factory_settings))
                .setPositiveButton(
                    getString(R.string.reset)
                ) { dialog, _ ->
                    //your deleting code
                    (Rfid.rfidDevice as Vh75Bt).resetToFactory()
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.cancel
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        private val resultForBtPermissionConnect =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                // returns boolean representind whether the
                // permission is granted or not
                if (!isGranted) {
                    makeText(
                        v,
                        AssetControlApp.getContext()
                            .getString(R.string.app_dont_have_necessary_permissions),
                        ERROR
                    )
                }
            }

        @SuppressLint("MissingPermission")
        private fun getBluetoothNameFromAddress(address: Any?, summary: String): String {
            var s = summary

            if (address != null) {
                val bluetoothManager = AssetControlApp.getContext()
                    .getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                val mBluetoothAdapter = bluetoothManager.adapter

                if (ActivityCompat.checkSelfPermission(
                        AssetControlApp.getContext(), Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        resultForBtPermissionConnect.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }
                    return s
                }

                val mPairedDevices = mBluetoothAdapter!!.bondedDevices
                if (mPairedDevices.size > 0) {
                    for (mDevice in mPairedDevices) {
                        if (mDevice.address == address.toString()) {
                            s = mDevice.name.toString()
                            break
                        }
                    }
                }
            }

            return s
        }

        private fun showSnackBar(it: SnackBarEventData) {
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return

            makeText(requireView(), it.text, it.snackBarType)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class ImageControlPreferenceFragment : PreferenceFragmentCompat(),
        ClientPackage.Companion.TaskConfigPanelEnded {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }

            setPreferencesFromResource(R.xml.pref_image_control, key)
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = ImageControlPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            bindPreferenceSummaryToValue(
                this, PreferencesApp.icPhotoMaxHeightOrWidth
            )

            bindPreferenceSummaryToValue(
                this, PreferencesApp.icWsServer
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.icWsNamespace
            )

            if (BuildConfig.DEBUG) {
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.icWsUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.icWsPass
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.icUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.icPass
                )
            }

            findPreference<Preference>(PreferencesApp.icWsUseProxy.key)
            bindPreferenceSummaryToValue(
                this, PreferencesApp.icWsProxy
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.icWsProxyPort
            )

            /*
            val proxyUrlEditText = findPreference<Preference>(P.icWsProxy.key)
            val proxyPortEditText = findPreference<Preference>(P.icWsProxyPort.key)
            val useProxyCheckBox = findPreference<Preference>(P.icWsUseProxy.key)
            val proxyUserEditText = findPreference<Preference>(P.icWsProxyUser.key)
            val proxyPassEditText = findPreference<Preference>(P.icWsProxyPass.key)
            */
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val urlEditText = findPreference<Preference>(PreferencesApp.icWsServer.key)
            val namespaceEditText = findPreference<Preference>(PreferencesApp.icWsNamespace.key)
            val userEditText = findPreference<Preference>(PreferencesApp.icUser.key)
            val passEditText = findPreference<Preference>(PreferencesApp.icPass.key)

            val button = findPreference<Preference>("ic_test")
            button?.onPreferenceClickListener = OnPreferenceClickListener {
                if (urlEditText != null && namespaceEditText != null && userEditText != null && passEditText != null) {
                    val url = prefsGetString(PreferencesApp.icWsServer)
                    val namespace = prefsGetString(PreferencesApp.icWsNamespace)

                    testImageControlConnection(
                        url = url, namespace = namespace
                    )
                }
                true
            }

            val removeImagesCache = findPreference<Preference>("remove_images_cache")
            removeImagesCache?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                val diaBox = askForDelete()
                diaBox.show()
                true
            }

            val qrCodeButton = findPreference<Preference>("ic_qr_code")
            qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
                val icUrl = prefsGetString(PreferencesApp.icWsServer)
                val icNamespace = prefsGetString(PreferencesApp.icWsNamespace)
                val icUserWs = prefsGetString(PreferencesApp.icWsUser)
                val icPasswordWs = prefsGetString(PreferencesApp.icWsPass)
                //val icUser = prefsGetString(P.icUser)
                //val icPassword = prefsGetString(P.icPass)

                if (icUrl.isEmpty() || icNamespace.isEmpty() || icUserWs.isEmpty() || icPasswordWs.isEmpty()) {
                    makeText(
                        requireView(),
                        AssetControlApp.getContext().getString(R.string.invalid_webservice_data),
                        ERROR
                    )
                    return@OnPreferenceClickListener false
                }

                generateQrCode(
                    WeakReference(requireActivity()), getBarcodeForConfig(
                        PreferencesApp.getImageControl(), Statics.appName
                    )
                )
                true
            }

            val scanConfigCode = findPreference<Preference>("scan_config_code")
            scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
                try {
                    doScanWork(QRConfigImageControl)
                    true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    makeText(
                        requireView(), "${getString(R.string.error)}: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    false
                }
            }
        }

        private fun askForDelete(): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.do_you_want_to_delete_the_image_cache_question))
                .setPositiveButton(
                    getString(R.string.delete)
                ) { dialog, _ ->
                    //your deleting code
                    val albumFolder = File(
                        AssetControlApp.getContext().getExternalFilesDir(
                            Environment.DIRECTORY_PICTURES
                        ), "ImageControl"
                    )

                    if (albumFolder.isDirectory) {
                        val files = albumFolder.listFiles()
                        if (files != null && files.any()) {
                            for (file in files) {
                                if (file.isFile) {
                                    file.delete()
                                }
                            }
                        }
                    }

                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.cancel
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        private fun testImageControlConnection(
            url: String,
            namespace: String,
        ) {
            if (url.isEmpty() || namespace.isEmpty()) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        AssetControlApp.getContext().getString(R.string.invalid_webservice_data),
                        INFO
                    )
                )
                return
            }
            ImageControlCheckUser { showSnackBar(it) }.execute()
        }

        override fun onTaskConfigPanelEnded(status: ProgressStatus) {
            if (status == ProgressStatus.finished) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.configuration_applied), INFO
                    )
                )
                removeDataBases()
                requireActivity().finish()
            } else if (status == ProgressStatus.crashed) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.error_setting_user_panel), ERROR
                    )
                )
            }
        }

        private fun showSnackBar(it: SnackBarEventData) {
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return

            makeText(requireView(), it.text, it.snackBarType)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class AssetControlPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }
            setPreferencesFromResource(R.xml.pref_asset_control, key)
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = AssetControlPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        private var alreadyAnsweredYes = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            bindPreferenceSummaryToValue(
                this, PreferencesApp.acWsServer
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acWsNamespace
            )

            if (BuildConfig.DEBUG) {
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acWsUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acWsPass
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acPass
                )
            }

            bindPreferenceSummaryToValue(
                this, PreferencesApp.acWsProxy
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acWsProxyPort
            )
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            /*
            val proxyUrlEditText = findPreference<Preference>(P.acWsProxy.key)
            val proxyPortEditText = findPreference<Preference>(P.acWsProxyPort.key)
            val useProxyCheckBox = findPreference<Preference>(P.acWsUseProxy.key)
            val proxyUserEditText = findPreference<Preference>(P.acWsProxyUser.key)
            val proxyPassEditText = findPreference<Preference>(P.acWsProxyPass.key)
            */

            val urlEditText = findPreference<Preference>(PreferencesApp.acWsServer.key)
            val namespaceEditText = findPreference<Preference>(PreferencesApp.acWsNamespace.key)
            val userWsEditText = findPreference<Preference>(PreferencesApp.acWsUser.key)
            val passWsEditText = findPreference<Preference>(PreferencesApp.acWsPass.key)
            val userEditText = findPreference<Preference>(PreferencesApp.acUser.key)
            val passEditText = findPreference<Preference>(PreferencesApp.acPass.key)

            val testButton = findPreference<Preference>("ac_test")
            testButton?.onPreferenceClickListener = OnPreferenceClickListener {
                if (urlEditText != null && namespaceEditText != null) {
                    val url = prefsGetString(PreferencesApp.acWsServer)
                    val namespace = prefsGetString(PreferencesApp.acWsNamespace)
                    val urlProxy = prefsGetString(PreferencesApp.acWsProxy)
                    val proxyPort = prefsGetInt(PreferencesApp.acWsProxyPort)
                    val useProxy = prefsGetBoolean(
                        PreferencesApp.acWsUseProxy
                    )
                    val proxyUser = prefsGetString(PreferencesApp.acWsProxyUser)
                    val proxyPass = prefsGetString(PreferencesApp.acWsProxyPass)

                    testWsConnection(
                        parentView = requireView(),
                        url = url,
                        namespace = namespace,
                        useProxy = useProxy,
                        proxyUrl = urlProxy,
                        proxyPort = proxyPort,
                        proxyUser = proxyUser,
                        proxyPass = proxyPass
                    )
                }
                true
            }

            val scanConfigCode = findPreference<Preference>("scan_config_code")
            scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
                try {
                    doScanWork(QRConfigWebservice)
                    true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    makeText(
                        requireView(), "${getString(R.string.error)}: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                    false
                }
            }

            val qrCodeButton = findPreference<Preference>("ac_qr_code")
            qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
                val url = prefsGetString(PreferencesApp.acWsServer)
                val namespace = prefsGetString(PreferencesApp.acWsNamespace)
                val userWs = prefsGetString(PreferencesApp.acWsUser)
                val passwordWs = prefsGetString(PreferencesApp.acWsPass)

                if (url.isEmpty() || namespace.isEmpty() || userWs.isEmpty() || passwordWs.isEmpty()) {
                    makeText(
                        requireView(),
                        AssetControlApp.getContext().getString(R.string.invalid_webservice_data),
                        ERROR
                    )
                    return@OnPreferenceClickListener false
                }

                generateQrCode(
                    WeakReference(requireActivity()), getBarcodeForConfig(
                        PreferencesApp.getAcWebserivce(), Statics.appName
                    )
                )
                true
            }

            (urlEditText ?: return).setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }
            namespaceEditText?.setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }
            userWsEditText?.setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }
            passWsEditText?.setOnPreferenceChangeListener { preference, newValue ->
                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired(
                        preference = preference, newValue = newValue
                    )
                    diaBox.show()
                    false
                } else {
                    preference.summary = newValue.toString()
                    true
                }
            }

            // Si ya está loggeado, deshabilitar estas opciones
            if (Statics.currentUserId != null) {
                scanConfigCode?.isEnabled = false
                urlEditText.isEnabled = false
                namespaceEditText?.isEnabled = false
                userWsEditText?.isEnabled = false
                passWsEditText?.isEnabled = false
                userEditText?.isEnabled = false
                passEditText?.isEnabled = false
            }
        }

        private fun askForDownloadDbRequired(
            preference: Preference,
            newValue: Any,
        ): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.download_database_required))
                .setMessage(getString(R.string.download_database_required_question))
                .setPositiveButton(
                    getString(R.string.yes)
                ) { dialog, _ ->
                    cleanPanelWebData()

                    //your deleting code
                    DownloadDb.downloadDbRequired = true
                    preference.summary = newValue.toString()
                    alreadyAnsweredYes = true
                    if (newValue is String) {
                        prefsPutString(
                            preference.key, newValue
                        )
                    }
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.no
                ) { dialog, _ -> dialog.dismiss() }.create()
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class AssetControlMantPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }
            setPreferencesFromResource(R.xml.pref_asset_control_mant, key)
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = AssetControlMantPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            bindPreferenceSummaryToValue(
                this, PreferencesApp.acMantWsServer
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acMantWsNamespace
            )

            if (BuildConfig.DEBUG) {
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acMantWsUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acMantWsPass
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acMantUser
                )
                bindPreferenceSummaryToValue(
                    this, PreferencesApp.acMantPass
                )
            }

            findPreference<Preference>(PreferencesApp.acMantWsUseProxy.key)
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acMantWsProxy
            )
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acMantWsProxyPort
            )

            /*
            val proxyUrlEditText = findPreference<Preference>(P.acMantWsProxy.key)
            val proxyPortEditText = findPreference<Preference>(P.acMantWsProxyPort.key)
            val useProxyCheckBox = findPreference<Preference>(P.acMantWsUseProxy.key)
            val proxyUserEditText = findPreference<Preference>(P.acMantWsProxyUser.key)
            val proxyPassEditText = findPreference<Preference>(P.acMantWsProxyPass.key)
            */

            val button = findPreference<Preference>("ac_mant_test")
            button?.onPreferenceClickListener = OnPreferenceClickListener {
                val urlEditText = findPreference<Preference>(PreferencesApp.acMantWsServer.key)
                val namespaceEditText =
                    findPreference<Preference>(PreferencesApp.acMantWsNamespace.key)

                if (urlEditText != null && namespaceEditText != null) {
                    val url = prefsGetString(PreferencesApp.acMantWsServer)
                    val namespace = prefsGetString(PreferencesApp.acMantWsNamespace)
                    val urlProxy = prefsGetString(PreferencesApp.acMantWsProxy)
                    val proxyPort = prefsGetInt(PreferencesApp.acMantWsProxyPort)
                    val useProxy = prefsGetBoolean(PreferencesApp.acMantWsUseProxy)
                    val proxyUser = prefsGetString(PreferencesApp.acMantWsProxyUser)
                    val proxyPass = prefsGetString(PreferencesApp.acMantWsProxyPass)

                    testWsConnection(
                        parentView = requireView(),
                        url = url,
                        namespace = namespace,
                        useProxy = useProxy,
                        proxyUrl = urlProxy,
                        proxyPort = proxyPort,
                        proxyUser = proxyUser,
                        proxyPass = proxyPass
                    )
                }
                true
            }
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DataSyncPreferenceFragment : PreferenceFragmentCompat(),
        ActivityCompat.OnRequestPermissionsResultCallback {
        companion object {
            fun equals(a: Any?, b: Any?): Boolean {
                return a != null && a == b
            }

            private var NEXT_STEP = 0
            private const val REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC = 4001
            private const val REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB = 3001
            private const val REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB = 2001
            private const val REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL = 1001
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            var key = rootKey
            if (arguments != null) {
                key = requireArguments().getString("rootKey")
            }
            setPreferencesFromResource(R.xml.pref_data_sync, key)
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
            val prefFragment = DataSyncPreferenceFragment()
            val args = Bundle()
            args.putString("rootKey", preferenceScreen.key)
            prefFragment.arguments = args
            parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null)
                .commit()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(this, ConfEntry.acSyncQtyRegistry)
            bindPreferenceSummaryToValue(
                this, PreferencesApp.acSyncInterval
            )

            val downloadDbButton = findPreference<Preference>("download_db_data")
            downloadDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    askForDownload().show()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            val resetSyncDateButton = findPreference<Preference>("reset_last_sync_date")
            resetSyncDateButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    if (SyncDownload.resetSyncDates()) {
                        if (view != null) makeText(
                            requireView(),
                            AssetControlApp.getContext()
                                .getString(R.string.synchronization_dates_restarted_successfully),
                            SnackBarType.SUCCESS
                        )
                    } else {
                        if (view != null) makeText(
                            requireView(),
                            AssetControlApp.getContext()
                                .getString(R.string.error_restarting_sync_dates),
                            ERROR
                        )
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            val loadCustomDbButton = findPreference<Preference>("load_custom_db")
            loadCustomDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    activity?.let {
                        if (hasPermissions(activity as Context, permissions)) {
                            selectFileDb()
                        } else {
                            NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB
                            permReqLauncher.launch(permissions)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            val removeCustomDbButton = findPreference<Preference>("remove_custom_db")
            removeCustomDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    deleteTempDbFiles()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            val sendDbButton = findPreference<Preference>("send_db_by_mail")
            sendDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    activity?.let {
                        if (hasPermissions(activity as Context, permissions)) {
                            sendDbByMail()
                        } else {
                            NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL
                            permReqLauncher.launch(permissions)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            val copyDbButton = findPreference<Preference>("copy_db")
            copyDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
                //code for what you want it to do
                try {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    activity?.let {
                        if (hasPermissions(activity as Context, permissions)) {
                            copyDbToDocuments()
                        } else {
                            NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC
                            permReqLauncher.launch(permissions)
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    if (view != null) makeText(
                        requireView(), "${
                            AssetControlApp.getContext().getString(R.string.error)
                        }: ${ex.message}", ERROR
                    )
                    ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                }
                true
            }

            // Si ya está loggeado, deshabilitar la descargar completa de la base de datos
            if (Statics.currentUserId != null) {
                downloadDbButton?.isEnabled = false
                loadCustomDbButton?.isEnabled = false
                removeCustomDbButton?.isEnabled = false
            }
        }

        private fun selectFileDb() {
            val intent = Intent(Intent.ACTION_GET_CONTENT)

            intent.type = "*/*" //"application/vnd.sqlite3" //"application/x-sqlite3"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                resultForPickfileRequestCode.launch(
                    Intent.createChooser(
                        intent, getString(R.string.select_db_file)
                    )
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.error_sending_email), ERROR
                    )
                )
            }
        }

        private val permReqLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val granted = permissions.entries.all {
                    it.value
                }

                if (granted) {
                    when (NEXT_STEP) {
                        REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB -> selectFileDb()
                        REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB -> copyDb()
                        REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC -> copyDbToDocuments()
                        REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL -> sendDbByMail()
                    }
                }
            }

        private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
            permissions.all {
                ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }

        private val resultForPickfileRequestCode =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it?.data
                try {
                    if ((it?.resultCode == CommonStatusCodes.SUCCESS || it.resultCode == CommonStatusCodes.SUCCESS_CACHE) && data != null) {
                        val dataFile: Uri? = data.data
                        if (dataFile != null) {
                            tempDbFile = PathHelper.getPath(dataFile) ?: ""
                            if (tempDbFile != "") {
                                val min = 10000
                                val max = 99999
                                DATABASE_NAME = String.format(
                                    "temp%s.sqlite", Random().nextInt(max - min + 1) + min
                                )
                                OFFLINE_MODE = true

                                val permissions =
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                activity?.let {
                                    if (hasPermissions(activity as Context, permissions)) {
                                        copyDb()
                                    } else {
                                        NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB
                                        permReqLauncher.launch(permissions)
                                    }
                                }
                            } else {
                                if (view != null) makeText(
                                    requireView(), getString(R.string.unable_to_open_file), ERROR
                                )
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
                }
            }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray,
        ) {
            when (requestCode) {
                REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        sendDbByMail()
                    }
                    return
                }
                REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        selectFileDb()
                    }
                    return
                }
                REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        copyDb()
                    }
                    return
                }
                REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        copyDbToDocuments()
                    }
                    return
                }
            }
        }

        private fun sendDbByMail() {
            try {
                // Base de datos
                val dbFile = File(
                    AssetControlApp.getContext().getDatabasePath(DATABASE_NAME).toString()
                )
                if (!dbFile.exists()) {
                    if (view != null) makeText(
                        requireView(), getString(R.string.database_file_does_not_exist), ERROR
                    )
                    return
                }

                // Copiar la base a Documentos
                val result = DataBaseHelper.copyDbToDocuments()
                val outFile = File(result.outFile)
                if (!outFile.exists()) {
                    if (view != null) makeText(
                        requireView(), getString(R.string.database_file_does_not_exist), ERROR
                    )
                    return
                }

                val dbFilePath = FileProvider.getUriForFile(
                    AssetControlApp.getContext(),
                    AssetControlApp.getContext().applicationContext.packageName + ".provider",
                    outFile
                )
                if (dbFilePath == null) {
                    if (view != null) makeText(
                        requireView(), getString(R.string.database_file_does_not_exist), ERROR
                    )
                    return
                }

                // Último registro de error
                val lastErrorLog = getLastErrorLog()
                var lastErrorLogPath: Uri? = null
                if (lastErrorLog != null) {
                    lastErrorLogPath = FileProvider.getUriForFile(
                        AssetControlApp.getContext(),
                        AssetControlApp.getContext().applicationContext.packageName + ".provider",
                        lastErrorLog
                    )
                }

                // Crear una colección de las URI's de los archivo a adjuntar
                val uris = ArrayList<Uri>()
                uris.add(dbFilePath)
                if (lastErrorLogPath != null) {
                    uris.add(lastErrorLogPath)
                }

                // Enviar con múltiples archivos adjuntos
                val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)

                // Set the type to 'email'
                emailIntent.type = "vnd.android.cursor.dir/email"

                // EXTRAS
                // Destinatario
                val to = arrayOf("agustin@dacosys.com")
                emailIntent.putExtra(Intent.EXTRA_EMAIL, to)

                // Agregar los adjuntos
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

                // Permisos de lectura de adjuntos
                grantUrisPermissions(requireActivity(), emailIntent, uris)

                // Mensaje
                val pInfo =
                    AssetControlApp.getContext().applicationContext.packageManager.getPackageInfo(
                        AssetControlApp.getContext().packageName, 0
                    )

                // The mail subject
                emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT, String.format(
                        "%s %s",
                        getString(R.string.email_error_subject),
                        "${getString(R.string.app_milestone)} ${pInfo.versionName}"
                    )
                )

                val msg = String.format(
                    "Ver: %s%sInstallation Code: %s%sClient Package: %s",
                    "${getString(R.string.app_milestone)} ${pInfo.versionName}",
                    System.getProperty("line.separator"),
                    Statics.installationCode,
                    System.getProperty("line.separator"),
                    Statics.clientPackage
                )

                val extraText = ArrayList<String>()
                extraText.add(msg)
                emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extraText)
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(
                    Intent.createChooser(
                        emailIntent, getString(R.string.sending_mail_)
                    )
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.error_sending_email), ERROR
                    )
                )
            }
        }

        private fun grantUrisPermissions(activity: Activity, intent: Intent, uris: List<Uri>) {
            // A possible fix to the problems with sharing files on new Androids, taken from https://github.com/lubritto/flutter_share/pull/20
            val packageManager = activity.packageManager
            val resInfoList =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                for (uri in uris) {
                    activity.grantUriPermission(
                        packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }

        private fun deleteTempDbFiles() {
            var anyDeleted = false
            val path = AssetControlApp.getContext().getDatabasePath(DATABASE_NAME).parent ?: return

            val dir = File(path)
            val files = dir.listFiles()

            if (files != null && files.any()) {
                for (f in files) {
                    if (f.name.startsWith("temp") && f.extension == "sqlite") {
                        anyDeleted = true
                        f.delete()
                    }
                }
            }

            if (anyDeleted) {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.temporary_databases_deleted), SnackBarType.SUCCESS
                    )
                )
            } else {
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        getString(R.string.no_temporary_bases_found), INFO
                    )
                )
            }
        }

        private var tempDbFile: String = ""

        private fun copyDbToDocuments() {
            try {
                DataBaseHelper.copyDbToDocuments()
                if (view != null) showSnackBar(
                    SnackBarEventData(
                        String.format(
                            "%s: %s", getString(R.string.database_changed), DATABASE_NAME
                        ), INFO
                    )
                )
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }

        private fun copyDb() {
            if (tempDbFile == "") return
            try {
                copyDataBase(tempDbFile)
                if (view != null) makeText(
                    requireView(), String.format(
                        "%s: %s", getString(R.string.database_changed), DATABASE_NAME
                    ), INFO
                )

                // Reiniciamos la instancia
                DataBaseHelper.cleanInstance()

            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }

        private fun askForDownload(): AlertDialog {
            return AlertDialog.Builder(requireActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.download_full_database))
                .setMessage(getString(R.string.do_you_want_to_download_the_complete_database_changes_not_sent_will_be_lost))
                .setPositiveButton(
                    getString(R.string.yes)
                ) { dialog, _ ->
                    // Forzar descarga de la base de datos
                    DownloadDb.downloadDbRequired = true
                    if (view != null) makeText(
                        requireView(),
                        getString(R.string.the_database_will_be_downloaded_when_you_return_to_the_login_screen),
                        INFO
                    )
                    dialog.dismiss()
                }.setNegativeButton(
                    R.string.no
                ) { dialog, _ -> dialog.dismiss() }.create()
        }

        private fun showSnackBar(it: SnackBarEventData) {
            if (requireActivity().isDestroyed || requireActivity().isFinishing) return

            makeText(requireView(), it.text, it.snackBarType)
        }
    }
}