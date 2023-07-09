package com.dacosys.assetControl.ui.fragments.settings

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.activities.main.SettingsActivity.Companion.bindPreferenceSummaryToValue
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.ConfigHelper
import com.dacosys.assetControl.utils.Screen
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences
import com.dacosys.assetControl.utils.scanners.GenerateQR
import com.dacosys.assetControl.utils.settings.QRConfigType

/**
 * This fragment shows notification preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
class WebservicePreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_webservice, key)
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        val prefFragment = WebservicePreferenceFragment()
        val args = Bundle()
        args.putString("rootKey", preferenceScreen.key)
        prefFragment.arguments = args
        parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null).commit()
    }

    private var alreadyAnsweredYes = false
    private val p = com.dacosys.assetControl.utils.settings.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindPreferenceSummaryToValue(this, p.acWsServer)
        bindPreferenceSummaryToValue(this, p.acWsNamespace)

        if (BuildConfig.DEBUG) {
            bindPreferenceSummaryToValue(this, p.acWsUser)
            bindPreferenceSummaryToValue(this, p.acWsPass)
            bindPreferenceSummaryToValue(this, p.acUser)
            bindPreferenceSummaryToValue(this, p.acPass)
        }

        bindPreferenceSummaryToValue(this, p.acWsProxy)
        bindPreferenceSummaryToValue(this, p.acWsProxyPort)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val urlEditText = findPreference<Preference>(p.acWsServer.key)
        val namespaceEditText = findPreference<Preference>(p.acWsNamespace.key)
        val userWsEditText = findPreference<Preference>(p.acWsUser.key)
        val passWsEditText = findPreference<Preference>(p.acWsPass.key)
        val userEditText = findPreference<Preference>(p.acUser.key)
        val passEditText = findPreference<Preference>(p.acPass.key)

        val testButton = findPreference<Preference>("ac_test")
        testButton?.onPreferenceClickListener = OnPreferenceClickListener {
            if (urlEditText != null && namespaceEditText != null) {
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

        val scanConfigCode = findPreference<Preference>("scan_config_code")
        scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                SettingsActivity.doScanWork(QRConfigType.QRConfigWebservice)
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

        val qrCodeButton = findPreference<Preference>("ac_qr_code")
        qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
            val url = Preferences.prefsGetString(p.acWsServer)
            val namespace = Preferences.prefsGetString(p.acWsNamespace)
            val userWs = Preferences.prefsGetString(p.acWsUser)
            val passwordWs = Preferences.prefsGetString(p.acWsPass)

            if (url.isEmpty() || namespace.isEmpty() || userWs.isEmpty() || passwordWs.isEmpty()) {
                MakeText.makeText(
                    requireView(),
                    AssetControlApp.getContext().getString(R.string.invalid_webservice_data),
                    SnackBarType.ERROR
                )
                return@OnPreferenceClickListener false
            }

            GenerateQR(data = ConfigHelper.getBarcodeForConfig(p.getAcWebserivce(), Statics.appName),
                size = Size(Screen.getScreenWidth(requireActivity()), Screen.getScreenHeight(requireActivity())),
                onProgress = {},
                onFinish = { showQrCode(it) })
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

                //your deleting code
                DownloadDb.downloadDbRequired = true
                preference.summary = newValue.toString()
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
     * ya que está configurando el webservice de manera manual
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