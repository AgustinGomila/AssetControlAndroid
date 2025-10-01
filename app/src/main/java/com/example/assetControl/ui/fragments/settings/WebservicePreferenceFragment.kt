package com.example.assetControl.ui.fragments.settings

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.appName
import com.example.assetControl.AssetControlApp.Companion.isLogged
import com.example.assetControl.AssetControlApp.Companion.sr
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.devices.scanners.GenerateQR
import com.example.assetControl.network.download.DownloadDb
import com.example.assetControl.ui.activities.main.SettingsActivity
import com.example.assetControl.ui.common.snackbar.MakeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.utils.Screen
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.ConfigHelper
import com.example.assetControl.utils.settings.config.QRConfigType

class WebservicePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_webservice, key)
    }

    private var alreadyAnsweredYes = false
    private val p = com.example.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wsServerPref: EditTextPreference? = findPreference(p.acWsServer.key)
        wsServerPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsNamespacePref: EditTextPreference? = findPreference(p.acWsNamespace.key)
        wsNamespacePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsUserPref: EditTextPreference? = findPreference(p.acWsUser.key)
        val wsPassPref: EditTextPreference? = findPreference(p.acWsPass.key)
        val userPref: EditTextPreference? = findPreference(p.acUser.key)
        val passPref: EditTextPreference? = findPreference(p.acPass.key)

        if (BuildConfig.DEBUG) {
            wsUserPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            wsPassPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            userPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            passPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        val wsProxyPref: EditTextPreference? = findPreference(p.acWsProxy.key)
        wsProxyPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsProxyPortPref: EditTextPreference? = findPreference(p.acWsProxyPort.key)
        wsProxyPortPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val testPref: Preference? = findPreference("ac_test")
        testPref?.onPreferenceClickListener = OnPreferenceClickListener {
            if (wsServerPref != null && wsNamespacePref != null) {
                val url = sr.prefsGetString(p.acWsServer)
                val namespace = sr.prefsGetString(p.acWsNamespace)
                val urlProxy = sr.prefsGetString(p.acWsProxy)
                val proxyPort = sr.prefsGetInt(p.acWsProxyPort)
                val useProxy = sr.prefsGetBoolean(p.acWsUseProxy)
                val proxyUser = sr.prefsGetString(p.acWsProxyUser)
                val proxyPass = sr.prefsGetString(p.acWsProxyPass)

                SettingsActivity.testWsConnection(
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

        val scanCodePref: Preference? = findPreference("scan_config_code")
        scanCodePref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                if (!isLogged()) {
                    val settingsActivity: SettingsActivity = requireActivity() as? SettingsActivity
                        ?: return@OnPreferenceClickListener true

                    SettingsActivity.doScanWork(settingsActivity, QRConfigType.QRConfigWebservice)
                }
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                MakeText.makeText(
                    requireView(), "${getString(R.string.error)}: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                false
            }
        }

        val qrCodePref: Preference? = findPreference("ac_qr_code")
        qrCodePref?.onPreferenceClickListener = OnPreferenceClickListener {
            val url = sr.prefsGetString(p.acWsServer)
            val namespace = sr.prefsGetString(p.acWsNamespace)
            val userWs = sr.prefsGetString(p.acWsUser)
            val passwordWs = sr.prefsGetString(p.acWsPass)

            if (url.isEmpty() || namespace.isEmpty() || userWs.isEmpty() || passwordWs.isEmpty()) {
                MakeText.makeText(
                    requireView(),
                    AssetControlApp.context.getString(R.string.invalid_webservice_data),
                    SnackBarType.ERROR
                )
                return@OnPreferenceClickListener false
            }

            GenerateQR(
                data = ConfigHelper.getBarcodeForConfig(
                    p.getAcWebserivce(),
                    appName
                ),
                size = Size(
                    Screen.getScreenWidth(requireActivity()),
                    Screen.getScreenHeight(requireActivity())
                ),
                onProgress = {},
                onFinish = { showQrCode(it) })
            true
        }

        wsServerPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(
                    preference = preference, newValue = newValue
                )
                diaBox.show()
                false
            } else
                true
        }
        wsNamespacePref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(
                    preference = preference, newValue = newValue
                )
                diaBox.show()
                false
            } else true
        }
        wsUserPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(
                    preference = preference, newValue = newValue
                )
                diaBox.show()
                false
            } else true
        }
        wsPassPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(
                    preference = preference, newValue = newValue
                )
                diaBox.show()
                false
            } else true
        }

        // Si ya está autentificado, deshabilitar estas opciones
        if (isLogged()) {
            scanCodePref?.isEnabled = false
            wsServerPref?.isEnabled = false
            wsNamespacePref?.isEnabled = false
            wsUserPref?.isEnabled = false
            wsPassPref?.isEnabled = false
            userPref?.isEnabled = false
            passPref?.isEnabled = false
        }
    }

    private fun showQrCode(it: Bitmap?) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return
        if (it == null) return

        val imageView = ImageView(requireActivity())
        imageView.setImageBitmap(it)
        val builder = AlertDialog.Builder(requireActivity()).setTitle(R.string.configuration_qr_code)
            .setMessage(R.string.scan_the_code_below_with_another_device_to_copy_the_configuration)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }.setView(imageView)

        builder.create().show()
    }

    private fun askForDownloadDbRequired(
        preference: Preference,
        newValue: Any,
    ): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.download_database_required))
            .setMessage(getString(R.string.download_database_required_question)).setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
                cleanPanelWebData()

                DownloadDb.downloadDbRequired = true
                if (!BuildConfig.DEBUG) preference.summary = newValue.toString()
                alreadyAnsweredYes = true
                if (newValue is String) {
                    sr.prefsPutString(preference.key, newValue)
                }
                dialog.dismiss()
            }.setNegativeButton(
                R.string.no
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    /**
     * Limpia la información relacionada con la cuenta del usuario
     * porque está configurando el webservice de manera manual
     */
    private fun cleanPanelWebData() {
        sr.prefsPutString(p.urlPanel.key, "")
        sr.prefsPutString(p.installationCode.key, "")
        sr.prefsPutString(p.clientPackage.key, "")
        sr.prefsPutString(p.clientEmail.key, "")
        sr.prefsPutString(p.clientPassword.key, "")
    }
}