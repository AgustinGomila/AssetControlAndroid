package com.dacosys.assetControl.ui.fragments.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.dacosys.assetControl.AssetControlApp
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.network.download.DownloadDb
import com.dacosys.assetControl.network.sync.SyncDownload
import com.dacosys.assetControl.ui.activities.main.SettingsActivity.Companion.bindPreferenceSummaryToValue
import com.dacosys.assetControl.ui.common.snackbar.MakeText
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Repository
import com.dacosys.assetControl.utils.settings.PathHelper
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.google.android.gms.common.api.CommonStatusCodes
import java.io.File
import java.util.*


/**
 * This fragment shows data and sync preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
class DataSyncPreferenceFragment : PreferenceFragmentCompat(), ActivityCompat.OnRequestPermissionsResultCallback {
    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }

        private var NEXT_STEP = 0
        private const val REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC = 4001
        private const val REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB = 3001
        private const val REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB = 2001
        private const val REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL = 1001
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_data_sync, key)
    }

    override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
        val prefFragment = DataSyncPreferenceFragment()
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

        bindPreferenceSummaryToValue(this, ConfEntry.acSyncQtyRegistry)
        bindPreferenceSummaryToValue(this, p.acSyncInterval)

        val downloadDbButton = findPreference<Preference>("download_db_data")
        downloadDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                askForDownload().show()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val resetSyncDateButton = findPreference<Preference>("reset_last_sync_date")
        resetSyncDateButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                if (SyncDownload.resetSyncDates()) {
                    if (view != null) MakeText.makeText(
                        requireView(),
                        AssetControlApp.getContext().getString(R.string.synchronization_dates_restarted_successfully),
                        SnackBarType.SUCCESS
                    )
                } else {
                    if (view != null) MakeText.makeText(
                        requireView(),
                        AssetControlApp.getContext().getString(R.string.error_restarting_sync_dates),
                        SnackBarType.ERROR
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val loadCustomDbButton = findPreference<Preference>("load_custom_db")
        loadCustomDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                activity?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || hasPermissions(
                            activity as Context,
                            permissions
                        )
                    ) {
                        selectFileDb()
                    } else {
                        NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB
                        permReqLauncher.launch(permissions)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val removeCustomDbButton = findPreference<Preference>("remove_custom_db")
        removeCustomDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                deleteTempDbFiles()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val sendDbButton = findPreference<Preference>("send_db_by_mail")
        sendDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                activity?.let {
                    if (hasPermissions(activity as Context, permissions)) {
                        sendDbByMail()
                    } else {
                        NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL
                        permReqLauncher.launch(permissions)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val copyDbButton = findPreference<Preference>("copy_db")
        copyDbButton?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                activity?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || hasPermissions(
                            activity as Context,
                            permissions
                        )
                    ) {
                        copyDbToDocuments()
                    } else {
                        NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC
                        permReqLauncher.launch(permissions)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null) MakeText.makeText(
                    requireView(), "${
                        AssetControlApp.getContext().getString(R.string.error)
                    }: ${ex.message}", SnackBarType.ERROR
                )
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        // Si ya está loggeado, deshabilitar la descargar completa de la base de datos
        if (Statics.currentUserId != null) {
            downloadDbButton?.isEnabled = false
            loadCustomDbButton?.isEnabled = false
            removeCustomDbButton?.isEnabled = false
        }
    }

    private fun selectFileDb() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        intent.type = "*/*" //"application/vnd.sqlite3" //"application/x-sqlite3"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            resultForPickfileRequestCode.launch(
                Intent.createChooser(
                    intent, getString(R.string.select_db_file)
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.error_sending_email), SnackBarType.ERROR
                )
            )
        }
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }

            if (granted) {
                when (NEXT_STEP) {
                    REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB -> selectFileDb()
                    REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB -> copyDb()
                    REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC -> copyDbToDocuments()
                    REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL -> sendDbByMail()
                }
            }
        }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private val resultForPickfileRequestCode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it?.data
            try {
                if ((it?.resultCode == CommonStatusCodes.SUCCESS || it.resultCode == CommonStatusCodes.SUCCESS_CACHE) && data != null) {
                    val dataFile: Uri? = data.data
                    if (dataFile != null) {
                        tempDbFile = PathHelper.getPath(dataFile) ?: ""
                        if (tempDbFile != "") {
                            val min = 10000
                            val max = 99999
                            DataBaseHelper.DATABASE_NAME = String.format(
                                "temp%s.sqlite", Random().nextInt(max - min + 1) + min
                            )
                            Statics.OFFLINE_MODE = true

                            val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            activity?.let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                                    hasPermissions(activity as Context, permissions)
                                ) {
                                    copyDb()
                                } else {
                                    NEXT_STEP = REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB
                                    permReqLauncher.launch(permissions)
                                }
                            }
                        } else {
                            if (view != null) MakeText.makeText(
                                requireView(), getString(R.string.unable_to_open_file), SnackBarType.ERROR
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE_FOR_SEND_MAIL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendDbByMail()
                }
                return
            }

            REQUEST_EXTERNAL_STORAGE_FOR_CUSTOM_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFileDb()
                }
                return
            }

            REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    copyDb()
                }
                return
            }

            REQUEST_EXTERNAL_STORAGE_FOR_COPY_DB_TO_DOC -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    copyDbToDocuments()
                }
                return
            }
        }
    }

    private fun sendDbByMail() {
        try {
            // Base de datos
            val dbFile = File(
                AssetControlApp.getContext().getDatabasePath(DataBaseHelper.DATABASE_NAME).toString()
            )
            if (!dbFile.exists()) {
                if (view != null) MakeText.makeText(
                    requireView(), getString(R.string.database_file_does_not_exist), SnackBarType.ERROR
                )
                return
            }

            // Copiar la base a Documentos
            val result = DataBaseHelper.copyDbToDocuments()
            val outFile = File(result.outFile)
            if (!outFile.exists()) {
                if (view != null) MakeText.makeText(
                    requireView(), getString(R.string.database_file_does_not_exist), SnackBarType.ERROR
                )
                return
            }

            val dbFilePath = FileProvider.getUriForFile(
                AssetControlApp.getContext(),
                AssetControlApp.getContext().applicationContext.packageName + ".provider",
                outFile
            )
            if (dbFilePath == null) {
                if (view != null) MakeText.makeText(
                    requireView(), getString(R.string.database_file_does_not_exist), SnackBarType.ERROR
                )
                return
            }

            // Último registro de error
            val lastErrorLog = ErrorLog.getLastErrorLog()
            var lastErrorLogPath: Uri? = null
            if (lastErrorLog != null) {
                lastErrorLogPath = FileProvider.getUriForFile(
                    AssetControlApp.getContext(),
                    AssetControlApp.getContext().applicationContext.packageName + ".provider",
                    lastErrorLog
                )
            }

            /* Crear una colección de los URI del archivo a adjuntar */
            val uris = ArrayList<Uri>()
            uris.add(dbFilePath)
            if (lastErrorLogPath != null) {
                uris.add(lastErrorLogPath)
            }

            // Enviar con múltiples archivos adjuntos
            val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)

            // Set the type to 'email'
            emailIntent.type = "vnd.android.cursor.dir/email"

            // EXTRAS
            // Destinatario
            val to = arrayOf("agustin@dacosys.com")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)

            // Agregar los adjuntos
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

            // Permisos de lectura de adjuntos
            grantUrisPermissions(requireActivity(), emailIntent, uris)

            // Mensaje
            val pInfo = AssetControlApp.getContext().applicationContext.packageManager.getPackageInfo(
                AssetControlApp.getContext().packageName, 0
            )

            // The mail subject
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT, String.format(
                    "%s %s",
                    getString(R.string.email_error_subject),
                    "${getString(R.string.app_milestone)} ${pInfo.versionName}"
                )
            )

            val msg = String.format(
                "Ver: %s%sInstallation Code: %s%sClient Package: %s",
                "${getString(R.string.app_milestone)} ${pInfo.versionName}",
                Statics.newLine,
                Repository.installationCode,
                Statics.newLine,
                Repository.clientPackage
            )

            val extraText = ArrayList<String>()
            extraText.add(msg)
            emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extraText)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(
                Intent.createChooser(
                    emailIntent, getString(R.string.sending_mail_)
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.error_sending_email), SnackBarType.ERROR
                )
            )
        }
    }

    private fun grantUrisPermissions(activity: Activity, intent: Intent, uris: List<Uri>) {
        // A possible fix to the problems with sharing files on new Androids, taken from https://github.com/lubritto/flutter_share/pull/20
        val packageManager = activity.packageManager
        val resInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            for (uri in uris) {
                activity.grantUriPermission(
                    packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
    }

    private fun deleteTempDbFiles() {
        var anyDeleted = false
        val path = AssetControlApp.getContext().getDatabasePath(DataBaseHelper.DATABASE_NAME).parent ?: return

        val dir = File(path)
        val files = dir.listFiles()

        if (files != null && files.any()) {
            for (f in files) {
                if (f.name.startsWith("temp") && f.extension == "sqlite") {
                    anyDeleted = true
                    f.delete()
                }
            }
        }

        if (anyDeleted) {
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.temporary_databases_deleted), SnackBarType.SUCCESS
                )
            )
        } else {
            if (view != null) showSnackBar(
                SnackBarEventData(
                    getString(R.string.no_temporary_bases_found), SnackBarType.INFO
                )
            )
        }
    }

    private var tempDbFile: String = ""

    private fun copyDbToDocuments() {
        try {
            DataBaseHelper.copyDbToDocuments()
            if (view != null) showSnackBar(
                SnackBarEventData(
                    String.format(
                        "%s: %s", getString(R.string.database_changed), DataBaseHelper.DATABASE_NAME
                    ), SnackBarType.INFO
                )
            )
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun copyDb() {
        if (tempDbFile == "") return
        try {
            DataBaseHelper.copyDataBase(tempDbFile)
            if (view != null) MakeText.makeText(
                requireView(), String.format(
                    "%s: %s", getString(R.string.database_changed), DataBaseHelper.DATABASE_NAME
                ), SnackBarType.INFO
            )

            // Reiniciamos la instancia
            DataBaseHelper.cleanInstance()

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun askForDownload(): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            //set message, title, and icon
            .setTitle(getString(R.string.download_full_database))
            .setMessage(getString(R.string.do_you_want_to_download_the_complete_database_changes_not_sent_will_be_lost))
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, _ ->
                // Forzar descarga de la base de datos
                DownloadDb.downloadDbRequired = true
                if (view != null) MakeText.makeText(
                    requireView(),
                    getString(R.string.the_database_will_be_downloaded_when_you_return_to_the_login_screen),
                    SnackBarType.INFO
                )
                dialog.dismiss()
            }.setNegativeButton(
                R.string.no
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun showSnackBar(it: SnackBarEventData) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return

        MakeText.makeText(requireView(), it.text, it.snackBarType)
    }
}