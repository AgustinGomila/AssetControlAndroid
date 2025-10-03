package com.example.assetControl.ui.fragments.settings

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.appName
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.BuildConfig
import com.example.assetControl.R
import com.example.assetControl.devices.scanners.GenerateQR
import com.example.assetControl.network.checkConn.ImageControlCheckUser
import com.example.assetControl.network.utils.ClientPackage
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.ui.activities.main.SettingsActivity
import com.example.assetControl.ui.common.snackbar.MakeText.Companion.makeText
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.INFO
import com.example.assetControl.ui.common.utils.Screen
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.config.ConfigHelper
import com.example.assetControl.utils.settings.config.QRConfigType
import com.example.assetControl.utils.settings.io.FileHelper
import java.io.File

class ImageControlPreferenceFragment : PreferenceFragmentCompat(), ClientPackage.Companion.TaskConfigPanelEnded {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_image_control, key)
    }

    val p = com.example.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wsServerPref: EditTextPreference? = findPreference(p.icWsServer.key)
        wsServerPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsNamespacePref: EditTextPreference? = findPreference(p.icWsNamespace.key)
        wsNamespacePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsUserPref: EditTextPreference? = findPreference(p.icWsUser.key)
        val wsPassPref: EditTextPreference? = findPreference(p.icWsPass.key)
        val userPref: EditTextPreference? = findPreference(p.icUser.key)
        val passPref: EditTextPreference? = findPreference(p.icPass.key)

        if (BuildConfig.DEBUG) {
            wsUserPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            wsPassPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            userPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
            passPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }

        val wsProxyPref: EditTextPreference? = findPreference(p.icWsProxy.key)
        wsProxyPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val wsProxyPortPref: EditTextPreference? = findPreference(p.icWsProxyPort.key)
        wsProxyPortPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val photoMaxSizePref: EditTextPreference? = findPreference(p.icPhotoMaxHeightOrWidth.key)
        photoMaxSizePref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val testPref: Preference? = findPreference("ic_test")
        testPref?.onPreferenceClickListener = OnPreferenceClickListener {
            if (wsServerPref != null && wsNamespacePref != null && userPref != null && passPref != null) {
                val url = svm.icWsServer
                val namespace = svm.icWsNamespace

                testImageControlConnection(url = url, namespace = namespace)
            }
            true
        }

        val removeImagesCache: Preference? = findPreference("remove_images_cache")
        removeImagesCache?.onPreferenceClickListener = OnPreferenceClickListener {
            val diaBox = askForDelete()
            diaBox.show()
            true
        }

        val qrCodePref: Preference? = findPreference("ic_qr_code")
        qrCodePref?.onPreferenceClickListener = OnPreferenceClickListener {
            val icUrl = svm.icWsServer
            val icNamespace = svm.icWsNamespace
            val icUserWs = svm.icWsUser
            val icPasswordWs = svm.icWsPass
            //val icUser = svm.icUser)
            //val icPassword = svm.icPass)

            if (icUrl.isEmpty() || icNamespace.isEmpty() || icUserWs.isEmpty() || icPasswordWs.isEmpty()) {
                showMessage(
                    AssetControlApp.context.getString(R.string.invalid_webservice_data),
                    ERROR
                )
                return@OnPreferenceClickListener false
            }

            GenerateQR(
                data = ConfigHelper.getBarcodeForConfig(p.getImageControl(), appName),
                size = Size(Screen.getScreenWidth(requireActivity()), Screen.getScreenHeight(requireActivity())),
                onProgress = {},
                onFinish = { showQrCode(it) })
            true
        }

        val scanCodePref: Preference? = findPreference("scan_config_code")
        scanCodePref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val settingsActivity: SettingsActivity = requireActivity() as? SettingsActivity
                    ?: return@OnPreferenceClickListener true

                SettingsActivity.doScanWork(settingsActivity, QRConfigType.QRConfigImageControl)
                true
            } catch (ex: Exception) {
                ex.printStackTrace()
                showMessage("${getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                false
            }
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

    private fun askForDelete(): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.do_you_want_to_delete_the_image_cache_question)).setPositiveButton(
                getString(R.string.delete)
            ) { dialog, _ ->
                val albumFolder = File(
                    AssetControlApp.context.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                    ), "ImageControl"
                )

                if (albumFolder.isDirectory) {
                    val files = albumFolder.listFiles()
                    if (files != null && files.any()) {
                        for (file in files) {
                            if (file.isFile) {
                                file.delete()
                            }
                        }
                    }
                }

                dialog.dismiss()
            }.setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun testImageControlConnection(
        url: String,
        namespace: String,
    ) {
        if (url.isEmpty() || namespace.isEmpty()) {
            if (view != null) showMessage(
                AssetControlApp.context.getString(R.string.invalid_webservice_data), INFO
            )
            return
        }
        ImageControlCheckUser { showMessage(it.text, it.snackBarType) }.execute()
    }

    override fun onTaskConfigPanelEnded(status: ProgressStatus) {
        if (status == ProgressStatus.finished) {
            if (view != null) showMessage(getString(R.string.configuration_applied), INFO)
            FileHelper.removeDataBases(requireContext())
            @Suppress("DEPRECATION") requireActivity().onBackPressed()
        } else if (status == ProgressStatus.crashed) {
            if (view != null) showMessage(getString(R.string.error_setting_user_panel), ERROR)
        }
    }

    private fun showMessage(msg: String, type: SnackBarType) {
        if (type == ERROR) logError(msg)
        makeText(requireView(), msg, type)
    }

    private fun logError(message: String) = Log.e(this::class.java.simpleName, message)
}