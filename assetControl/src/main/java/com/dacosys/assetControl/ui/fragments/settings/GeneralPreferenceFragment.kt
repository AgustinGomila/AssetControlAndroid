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
import com.dacosys.assetControl.AssetControlApp.Companion.appName
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.ui.common.utils.Screen
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.GenerateQR
import com.dacosys.assetControl.utils.settings.config.ConfigHelper
import com.dacosys.assetControl.utils.settings.config.QRConfigType
import java.io.File

class GeneralPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_general, key)
    }

    val p = com.dacosys.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterRoutePref: EditTextPreference? = findPreference(p.acFilterRouteDescription.key)
        filterRoutePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        if (BuildConfig.DEBUG) {
            val confPassPref: EditTextPreference? = findPreference(p.confPassword.key)
            confPassPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        val removeLogFiles: Preference? = findPreference("remove_log_files")
        removeLogFiles?.onPreferenceClickListener = OnPreferenceClickListener {
            val diaBox = askForDelete()
            diaBox.show()
            true
        }

        val scanConfigCode: Preference? = findPreference("scan_config_code")
        scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val settingsActivity: SettingsActivity = requireActivity() as? SettingsActivity
                    ?: return@OnPreferenceClickListener true

                SettingsActivity.doScanWork(settingsActivity, QRConfigType.QRConfigApp)
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

        val qrCodeButton: Preference? = findPreference("ac_qr_code")
        qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
            GenerateQR(
                data = ConfigHelper.getBarcodeForConfig(
                    p.getAppConf(),
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
    }

    private fun showQrCode(it: Bitmap?) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return
        if (it == null) return

        val imageView = ImageView(requireActivity())
        imageView.setImageBitmap(it)
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.configuration_qr_code)
            .setMessage(R.string.scan_the_code_below_with_another_device_to_copy_the_configuration)
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setView(imageView)

        builder.create().show()
    }

    private fun askForDelete(): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.do_you_want_to_delete_the_old_error_logs_question)).setPositiveButton(
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