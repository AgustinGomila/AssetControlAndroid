package com.dacosys.assetControl.ui.fragments.settings

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Size
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.AssetControlApp.Companion.appName
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper
import com.dacosys.assetControl.network.checkConn.ImageControlCheckUser
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
import java.io.File

class ImageControlPreferenceFragment : PreferenceFragmentCompat(), ClientPackage.Companion.TaskConfigPanelEnded {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_image_control, key)
    }

    val p = com.dacosys.assetControl.utils.settings.config.Preference

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
                val url = Preferences.prefsGetString(p.icWsServer)
                val namespace = Preferences.prefsGetString(p.icWsNamespace)

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

        val qrCodeButton: Preference? = findPreference("ic_qr_code")
        qrCodeButton?.onPreferenceClickListener = OnPreferenceClickListener {
            val icUrl = Preferences.prefsGetString(p.icWsServer)
            val icNamespace = Preferences.prefsGetString(p.icWsNamespace)
            val icUserWs = Preferences.prefsGetString(p.icWsUser)
            val icPasswordWs = Preferences.prefsGetString(p.icWsPass)
            //val icUser = prefsGetString(P.icUser)
            //val icPassword = prefsGetString(P.icPass)

            if (icUrl.isEmpty() || icNamespace.isEmpty() || icUserWs.isEmpty() || icPasswordWs.isEmpty()) {
                MakeText.makeText(
                    requireView(),
                    AssetControlApp.getContext().getString(R.string.invalid_webservice_data),
                    SnackBarType.ERROR
                )
                return@OnPreferenceClickListener false
            }

            GenerateQR(data = ConfigHelper.getBarcodeForConfig(p.getImageControl(), appName),
                size = Size(Screen.getScreenWidth(requireActivity()), Screen.getScreenHeight(requireActivity())),
                onProgress = {},
                onFinish = { showQrCode(it) })
            true
        }

        val scanConfigCode: Preference? = findPreference("scan_config_code")
        scanConfigCode?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val settingsActivity: SettingsActivity = requireActivity() as? SettingsActivity
                    ?: return@OnPreferenceClickListener true

                SettingsActivity.doScanWork(settingsActivity, QRConfigType.QRConfigImageControl)
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
                //your deleting code
                val albumFolder = File(
                    AssetControlApp.getContext().getExternalFilesDir(
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
            if (view != null) showSnackBar(
                SnackBarEventData(
                    AssetControlApp.getContext().getString(R.string.invalid_webservice_data), SnackBarType.INFO
                )
            )
            return
        }
        ImageControlCheckUser { showSnackBar(it) }.execute()
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
}