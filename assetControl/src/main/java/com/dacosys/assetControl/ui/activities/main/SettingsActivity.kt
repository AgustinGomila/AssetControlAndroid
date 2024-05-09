package com.dacosys.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
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
import com.dacosys.assetControl.ui.fragments.settings.HeaderFragment
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.JotterListener
import com.dacosys.assetControl.utils.scanners.Scanner
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigApp
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigClientAccount
import com.dacosys.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigWebservice
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import org.json.JSONObject
import java.lang.ref.WeakReference
import com.dacosys.assetControl.utils.settings.config.Preference.Companion as PreferenceConfig

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    Scanner.ScannerListener, ConfigHelper.TaskConfigEnded, ClientPackage.Companion.TaskConfigPanelEnded {

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        // Instantiate the new Fragment.
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment ?: ""
        )
        fragment.arguments = args
        // Replace the existing Fragment with the new Fragment.
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    private lateinit var binding: SettingsActivityBinding

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createOptionsMenu()

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        }
    }

    private fun createOptionsMenu() {
        // Add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                // Inflate the menu; this adds items to the action bar if it is present.
                menuInflater.inflate(R.menu.menu_read_activity, menu)
                menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.home, android.R.id.home -> {
                        // TODO: Crear navegación propiamente para esta actividad
                        onBackPressed()
                        true
                    }

                    R.id.action_read_barcode -> {
                        doScanWork(this@SettingsActivity, QRConfigApp)
                        true
                    }

                    R.id.action_trigger_scan -> {
                        doTriggerWork(this@SettingsActivity, QRConfigApp)
                        true
                    }

                    else -> {
                        true
                    }
                }
            }
        })
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
        if (!::binding.isInitialized || isFinishing || isDestroyed) return

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
        if (permissions.contains(Manifest.permission.BLUETOOTH_CONNECT)) {
            JotterListener.onRequestPermissionsResult(
                this, requestCode, permissions, grantResults
            )
        }
    }

    private val showScannedCode: Boolean
        get() {
            return prefsGetBoolean(PreferenceConfig.showScannedCode)
        }

    override fun scannerCompleted(scanCode: String) {
        if (!::binding.isInitialized || isFinishing || isDestroyed) return
        if (showScannedCode) makeText(binding.root, scanCode, INFO)
        JotterListener.lockScanner(this, true)

        try {
            // No capturar códigos que cambian el servidor cuando está autentificado.
            if (getUserId() != null && (currentQRConfigType == QRConfigClientAccount || currentQRConfigType == QRConfigWebservice)) {
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
        var currentQRConfigType = QRConfigApp

        fun doTriggerWork(activity: SettingsActivity, qrConfigType: QRConfigType) {
            if (isLogged() && (qrConfigType == QRConfigWebservice || qrConfigType == QRConfigClientAccount))
                return
            
            currentQRConfigType = qrConfigType
            JotterListener.trigger(activity)
        }

        fun doScanWork(activity: SettingsActivity, qrConfigType: QRConfigType) {
            if (isLogged() && (qrConfigType == QRConfigWebservice || qrConfigType == QRConfigClientAccount))
                return

            currentQRConfigType = qrConfigType
            JotterListener.toggleCameraFloatingWindowVisibility(activity)
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