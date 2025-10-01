package com.example.assetControl.ui.activities.main

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.AssetControlApp.Companion.getUserId
import com.example.assetControl.AssetControlApp.Companion.isLogged
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.databinding.SettingsActivityBinding
import com.example.assetControl.devices.deviceLifecycle.ScannerManager
import com.example.assetControl.devices.scanners.Scanner
import com.example.assetControl.network.checkConn.CheckWsConnection
import com.example.assetControl.network.clientPackages.ClientPackagesProgress
import com.example.assetControl.network.utils.ClientPackage
import com.example.assetControl.network.utils.ClientPackage.Companion.selectClientPackage
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import com.example.assetControl.ui.fragments.settings.HeaderFragment
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.ConfigHelper
import com.example.assetControl.utils.settings.config.QRConfigType
import com.example.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigApp
import com.example.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigClientAccount
import com.example.assetControl.utils.settings.config.QRConfigType.CREATOR.QRConfigWebservice
import com.example.assetControl.utils.settings.io.FileHelper
import org.json.JSONObject
import java.lang.ref.WeakReference

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    Scanner.ScannerListener, ConfigHelper.TaskConfigEnded, ClientPackage.Companion.TaskConfigPanelEnded {

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        val args: Bundle = pref.extras
        val fragment: Fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment ?: "")
        fragment.arguments = args

        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        supportFragmentManager.setFragmentResultListener("requestKey", this) { key, _ ->
            if (key == "requestKey") title = pref.title
        }
        return true
    }

    private lateinit var binding: SettingsActivityBinding
    private lateinit var titleTag: String

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createOptionsMenu()

        setSupportActionBar(binding.topAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleTag = getString(R.string.settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            titleTag = savedInstanceState.getCharSequence(ARG_TITLE).toString()
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
                menu.removeItem(menu.findItem(R.id.action_rfid_connect).itemId)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.home, android.R.id.home -> {
                        finish()
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
            FileHelper.removeDataBases(context)
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
            if (result.isNotEmpty()) {
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
        if (showScannedCode) makeText(binding.root, scanCode, INFO)
        ScannerManager.lockScanner(this, true)

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
            ScannerManager.lockScanner(this, false)
        }
    }

    companion object {
        var currentQRConfigType = QRConfigApp

        const val ARG_TITLE = "title"

        fun doTriggerWork(activity: SettingsActivity, qrConfigType: QRConfigType) {
            if (isLogged() && (qrConfigType == QRConfigWebservice || qrConfigType == QRConfigClientAccount))
                return

            currentQRConfigType = qrConfigType
            ScannerManager.trigger(activity)
        }

        fun doScanWork(activity: SettingsActivity, qrConfigType: QRConfigType) {
            if (isLogged() && (qrConfigType == QRConfigWebservice || qrConfigType == QRConfigClientAccount))
                return

            currentQRConfigType = qrConfigType
            ScannerManager.toggleCameraFloatingWindowVisibility(activity)
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
                        context.getString(R.string.invalid_webservice_data), INFO
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