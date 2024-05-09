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
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper
import com.dacosys.assetControl.databinding.ProgressViewBinding
import com.dacosys.assetControl.network.clientPackages.ClientPackagesProgress
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.network.utils.ClientPackage
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen
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

    private var alreadyAnsweredYes = false
    val p = com.dacosys.assetControl.utils.settings.config.Preference

    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val emailPref: EditTextPreference? = findPreference(p.clientEmail.key)
        val passPref: EditTextPreference? = findPreference(p.clientPassword.key)

        if (BuildConfig.DEBUG) {
            emailPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            passPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        emailPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(preference = preference, newValue = newValue)
                diaBox.show()
                false
            } else {
                true
            }
        }

        passPref?.setOnPreferenceChangeListener { preference, newValue ->
            if (!alreadyAnsweredYes) {
                val diaBox = askForDownloadDbRequired(preference = preference, newValue = newValue)
                diaBox.show()
                false
            } else {
                true
            }
        }

        val selectPackagePref: Preference? = findPreference("select_package")
        selectPackagePref?.onPreferenceClickListener = OnPreferenceClickListener {
            if (emailPref != null && passPref != null) {
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

        val scanCodePref: Preference? = findPreference("scan_config_code")
        scanCodePref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                if (!isLogged()) {
                    val settingsActivity: SettingsActivity = requireActivity() as? SettingsActivity
                        ?: return@OnPreferenceClickListener true

                    SettingsActivity.doScanWork(settingsActivity, QRConfigType.QRConfigClientAccount)
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
        val updateAppPref: Preference? = findPreference("update_app")
        updateAppPref?.isEnabled = false
        updateAppPref?.onPreferenceClickListener = OnPreferenceClickListener {
            MakeText.makeText(requireView(), getString(R.string.no_available_option), SnackBarType.INFO)
            true
        }

        // Si ya está autentificado, deshabilitar estas opciones
        if (isLogged()) {
            passPref?.isEnabled = false
            emailPref?.isEnabled = false
            selectPackagePref?.isEnabled = false
            scanCodePref?.isEnabled = false
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
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return

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