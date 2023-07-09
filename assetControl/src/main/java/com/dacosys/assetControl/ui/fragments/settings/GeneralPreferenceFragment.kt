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
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.ui.activities.main.SettingsActivity
import com.dacosys.assetControl.ui.activities.main.SettingsActivity.Companion.bindPreferenceSummaryToValue
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.ConfigHelper
import com.dacosys.assetControl.utils.Screen
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.scanners.GenerateQR
import com.dacosys.assetControl.utils.settings.QRConfigType
import java.io.File

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
        parentFragmentManager.beginTransaction().replace(id, prefFragment).addToBackStack(null).commit()
    }

    val p = com.dacosys.assetControl.utils.settings.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(this, p.acFilterRouteDescription)

        findPreference<Preference>(p.registryError.key) as Preference
        findPreference<Preference>(p.showConfButton.key) as Preference

        if (BuildConfig.DEBUG) {
            bindPreferenceSummaryToValue(this, p.confPassword)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val removeLogFiles = findPreference<Preference>("remove_log_files")
        removeLogFiles?.onPreferenceClickListener = OnPreferenceClickListener {
            val diaBox = askForDelete()
            diaBox.show()
            true
        }

        val scanConfigCode = findPreference<Preference>("scan_config_code")
        scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                SettingsActivity.doScanWork(QRConfigType.QRConfigApp)
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
            GenerateQR(data = ConfigHelper.getBarcodeForConfig(p.getAppConf(), Statics.appName),
                size = Size(Screen.getScreenWidth(requireActivity()), Screen.getScreenHeight(requireActivity())),
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