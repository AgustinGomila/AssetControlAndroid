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
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper
import com.dacosys.assetControl.databinding.ProgressViewBinding
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.network.utils.ClientPackage
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.activities.main.SettingsActivity.Companion.sBindPreferenceSummaryToValueListener
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.GenerateQR
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import com.dacosys.assetControl.utils.settings.preferences.Preferences
import org.json.JSONObject
import java.lang.ref.WeakReference


class AccountPreferenceFragment : PreferenceFragmentCompat(), ClientPackage.Companion.TaskConfigPanelEnded {
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
        parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null).commit()
    }

    private var alreadyAnsweredYes = false
    val p = com.dacosys.assetControl.utils.settings.config.Preference

    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.

        if (BuildConfig.DEBUG) {
            bindPreferenceSummaryToValue(this, p.clientEmail)
            bindPreferenceSummaryToValue(this, p.clientPassword)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = findPreference<Preference>(p.clientEmail.key)
        emailEditText?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(preference = preference, newValue = newValue)
                diaBox.show()
                false
            } else {
                preference.summary = newValue.toString()
                true
            }
        }

        val passwordEditText = findPreference<Preference>(p.clientPassword.key)
        passwordEditText?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(preference = preference, newValue = newValue)
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
                val email = Preferences.prefsGetString(p.clientEmail)
                val password = Preferences.prefsGetString(p.clientPassword)

                if (!alreadyAnsweredYes) {
                    val diaBox = askForDownloadDbRequired2(email = email, password = password)
                    diaBox.show()
                } else {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        ConfigHelper.getConfig(
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
                SettingsActivity.doScanWork(QRConfigType.QRConfigClientAccount)
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
            val urlPanel = Preferences.prefsGetString(p.urlPanel)
            val installationCode = Preferences.prefsGetString(p.installationCode)
            val clientEmail = Preferences.prefsGetString(p.clientEmail)
            val clientPassword = Preferences.prefsGetString(p.clientPassword)
            val clientPackage = Preferences.prefsGetString(p.clientPackage)

            if (urlPanel.isEmpty() || installationCode.isEmpty() || clientPackage.isEmpty() || clientEmail.isEmpty() || clientPassword.isEmpty()) {
                MakeText.makeText(
                    requireView(),
                    AssetControlApp.getContext().getString(R.string.invalid_client_data),
                    SnackBarType.ERROR
                )
                return@OnPreferenceClickListener false
            }

            GenerateQR(data = ConfigHelper.getBarcodeForConfig(p.getClient(), "config"),
                size = Size(Screen.getScreenWidth(requireActivity()), Screen.getScreenHeight(requireActivity())),
                onProgress = { showProgressBar(getString(R.string.generating_qr_), it) },
                onFinish = {
                    hideProgressBar()
                    showQrCode(it)
                })
            true
        }

        // Actualizar el programa
        val updateAppButton = findPreference<Preference>("update_app") as Preference
        updateAppButton.isEnabled = false
        updateAppButton.onPreferenceClickListener = OnPreferenceClickListener {
            MakeText.makeText(requireView(), getString(R.string.no_available_option), SnackBarType.INFO)
            true
        }

        // Si ya está autentificado, deshabilitar estas opciones
        if (Statics.currentUserId != null) {
            passwordEditText?.isEnabled = false
            emailEditText?.isEnabled = false
            selectPackageButton?.isEnabled = false
            scanConfigCode?.isEnabled = false
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

    private fun askForDownloadDbRequired2(
        email: String,
        password: String,
    ): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.download_database_required))
            .setMessage(getString(R.string.download_database_required_question)).setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
                //your deleting code
                DownloadDb.downloadDbRequired = true
                alreadyAnsweredYes = true

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    ConfigHelper.getConfig(
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
            .setMessage(getString(R.string.download_database_required_question)).setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
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
            pref: com.dacosys.assetControl.utils.settings.config.Preference,
        ) {
            val preference = frag.findPreference<Preference>(pref.key)
            val all: Map<String, *> = PreferenceManager.getDefaultSharedPreferences(getContext()).all

            // Set the listener to watch for value changes.
            preference?.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            val defaultValue: Any? = if (BuildConfig.DEBUG) pref.debugValue else pref.defaultValue

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
                    ClientPackage.selectClientPackage(
                        parentView = requireView(),
                        callback = this,
                        weakAct = WeakReference(requireActivity()),
                        allPackage = result,
                        email = clientEmail,
                        password = clientPassword
                    )
                }
            } else {
                if (view != null) MakeText.makeText(
                    requireView(), msg, SnackBarType.INFO
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
                    msg, SnackBarType.ERROR
                )
            )
        }
    }

    override fun onTaskConfigPanelEnded(status: ProgressStatus) {
        if (status == ProgressStatus.finished) {
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.configuration_applied), SnackBarType.INFO
                )
            )
            DataBaseHelper.removeDataBases()
            requireActivity().finish()
        } else if (status == ProgressStatus.crashed) {
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.error_setting_user_panel), SnackBarType.ERROR
                )
            )
        }
    }

    private fun showSnackBar(it: SnackBarEventData) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return

        MakeText.makeText(requireView(), it.text, it.snackBarType)
    }

    // region QR Generation Progress Bar
    // Aparece mientras se realizan la generación del código QR de configuración
    // TODO: No está funcionando, no se muestra sobre la pantalla de configuración aunque isShowing dice true
    private var progressDialog: AlertDialog? = null
    private lateinit var progressViewBinding: ProgressViewBinding
    private fun createProgressDialog() {
        progressViewBinding = ProgressViewBinding.inflate(layoutInflater)
        progressDialog =
            AlertDialog.Builder(requireActivity())
                .setCancelable(false)
                .setView(progressViewBinding.root)
                .create()
    }

    private fun showProgressBar(
        msg: String,
        progress: Int,
    ) {
        if (requireActivity().isFinishing || requireActivity().isDestroyed) return

        if (progressDialog == null) createProgressDialog()

        val value = "$progress%"
        progressViewBinding.progressBar.progress = progress
        progressViewBinding.syncPercentTextView.text = msg
        progressViewBinding.syncPercentTextView.text = value

        progressDialog?.show()
    }

    private fun hideProgressBar() {
        progressDialog?.dismiss()
        progressDialog = null
    }
    // endregion QR Generation Progress Bar
}