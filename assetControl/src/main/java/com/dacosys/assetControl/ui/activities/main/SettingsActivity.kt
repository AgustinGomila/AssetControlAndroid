package com.dacosys.assetControl.ui.activities.main

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.removeDataBases
import com.dacosys.assetControl.databinding.SettingsActivityBinding
import com.dacosys.assetControl.network.checkConn.CheckWsConnection
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.utils.ClientPackage
import com.dacosys.assetControl.network.utils.ClientPackage.Companion.selectClientPackage
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import com.dacosys.assetControl.ui.common.utils.Screen.Companion.closeKeyboard
import com.dacosys.assetControl.ui.fragments.settings.HeaderFragment
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import org.json.JSONObject
import java.lang.ref.WeakReference


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

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    Scanner.ScannerListener, ConfigHelper.TaskConfigEnded, ClientPackage.Companion.TaskConfigPanelEnded {

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
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment).addToBackStack(null).commit()
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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleTag = getString(R.string.settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, HeaderFragment()).commit()
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

    private fun isBackPressed() {
        closeKeyboard(this)
        setResult(RESULT_CANCELED)
        finish()
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
                        isBackPressed()
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

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(com.dacosys.assetControl.utils.settings.config.Preference.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (showScannedCode) makeText(binding.root, scanCode, INFO)
        JotterListener.lockScanner(this, true)

        try {
            // No capturar códigos que cambian el servidor cuando está logeado.
            if (Statics.currentUserId != null && (currentQRConfigType == QRConfigClientAccount || currentQRConfigType == QRConfigWebservice)) {
                return
            }

            ConfigHelper.getConfigFromScannedCode(
                scanCode = scanCode, mode = currentQRConfigType
            ) { onTaskGetPackagesEnded(it) }
        } catch (ex: Exception) {
            ex.printStackTrace()
            makeText(
                binding.settings, ex.message.toString(), ERROR
            )
            ErrorLog.writeLog(this, this::class.java.simpleName, ex)
        } finally {
            JotterListener.lockScanner(this, false)
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
        val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
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
        fun bindPreferenceSummaryToValue(
            frag: PreferenceFragmentCompat,
            pref: ConfEntry,
        ) {
            val preference = frag.findPreference<Preference>(pref.description) ?: return
            val defaultValue: Any = pref.defaultValue
            bindPreferenceSummaryToValue(preference, defaultValue)
        }

        fun bindPreferenceSummaryToValue(
            frag: PreferenceFragmentCompat,
            pref: com.dacosys.assetControl.utils.settings.config.Preference,
        ) {
            val preference = frag.findPreference<Preference>(pref.key) ?: return
            val defaultValue: Any? = if (BuildConfig.DEBUG) pref.debugValue else pref.defaultValue
            bindPreferenceSummaryToValue(preference, defaultValue)
        }

        private fun bindPreferenceSummaryToValue(
            preference: Preference,
            defaultValue: Any?,
        ) {
            val all: Map<String, *> = PreferenceManager.getDefaultSharedPreferences(getContext()).all

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

        fun testWsConnection(
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
    }
}