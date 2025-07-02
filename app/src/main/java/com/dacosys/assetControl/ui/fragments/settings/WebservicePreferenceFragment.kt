package com.dacosys.assetControl.ui.fragments.settings

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.appName
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.devices.scanners.GenerateQR
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import com.dacosys.assetControl.utils.settings.preferences.Preferences

class WebservicePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_webservice, key)
    }

    private var alreadyAnsweredYes = false
    private val p = com.dacosys.assetControl.utils.settings.config.Preference

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
                val url = Preferences.prefsGetString(p.acWsServer)
                val namespace = Preferences.prefsGetString(p.acWsNamespace)
                val urlProxy = Preferences.prefsGetString(p.acWsProxy)
                val proxyPort = Preferences.prefsGetInt(p.acWsProxyPort)
                val useProxy = Preferences.prefsGetBoolean(p.acWsUseProxy)
                val proxyUser = Preferences.prefsGetString(p.acWsProxyUser)
                val proxyPass = Preferences.prefsGetString(p.acWsProxyPass)

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
            val url = Preferences.prefsGetString(p.acWsServer)
            val namespace = Preferences.prefsGetString(p.acWsNamespace)
            val userWs = Preferences.prefsGetString(p.acWsUser)
            val passwordWs = Preferences.prefsGetString(p.acWsPass)

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
                    Preferences.prefsPutString(
                        preference.key, newValue
                    )
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
        Preferences.prefsPutString(
            p.urlPanel.key, ""
        )
        Preferences.prefsPutString(
            p.installationCode.key, ""
        )
        Preferences.prefsPutString(
            p.clientPackage.key, ""
        )
        Preferences.prefsPutString(
            p.clientEmail.key, ""
        )
        Preferences.prefsPutString(
            p.clientPassword.key, ""
        )
    }
}