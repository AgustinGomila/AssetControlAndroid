package com.example.assetControl.ui.fragments.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import com.example.assetControl.AssetControlApp
import com.example.assetControl.AssetControlApp.Companion.isLogged
import com.example.assetControl.AssetControlApp.Companion.svm
import com.example.assetControl.R
import com.example.assetControl.data.room.database.AcDatabase
import com.example.assetControl.data.room.database.AcDatabase.Companion.DATABASE_NAME
import com.example.assetControl.data.room.database.AcDatabase.Companion.cleanInstance
import com.example.assetControl.network.download.DownloadDb
import com.example.assetControl.network.sync.SyncDownload
import com.example.assetControl.ui.common.snackbar.MakeText
import com.example.assetControl.ui.common.snackbar.SnackBarEventData
import com.example.assetControl.ui.common.snackbar.SnackBarType
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.ERROR
import com.example.assetControl.ui.common.snackbar.SnackBarType.CREATOR.SUCCESS
import com.example.assetControl.utils.Statics
import com.example.assetControl.utils.errorLog.ErrorLog
import com.example.assetControl.utils.settings.entries.ConfEntry
import com.example.assetControl.utils.settings.io.FileHelper
import com.example.assetControl.utils.settings.io.PathHelper
import com.google.android.gms.common.api.CommonStatusCodes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*

class DataSyncPreferenceFragment : PreferenceFragmentCompat(), ActivityCompat.OnRequestPermissionsResultCallback {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        var key = rootKey
        if (arguments != null) {
            key = requireArguments().getString("rootKey")
        }
        setPreferencesFromResource(R.xml.pref_data_sync, key)
    }

    val p = com.example.assetControl.utils.settings.config.Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = AssetControlApp.context

        val syncQtyPref: EditTextPreference? = findPreference(ConfEntry.acSyncQtyRegistry.description)
        syncQtyPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val syncIntervalPref: EditTextPreference? = findPreference(p.acSyncInterval.key)
        syncIntervalPref?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val downloadDbPref: Preference? = findPreference("download_db_data")
        downloadDbPref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                askForDownload().show()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val resetSyncDatePref: Preference? = findPreference("reset_last_sync_date")
        resetSyncDatePref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                if (SyncDownload.resetSyncDates()) {
                    if (view != null)
                        showSnackBar(context.getString(R.string.synchronization_dates_restarted_successfully), SUCCESS)
                } else {
                    if (view != null)
                        showSnackBar(context.getString(R.string.error_restarting_sync_dates), ERROR)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val loadCustomDbPref: Preference? = findPreference("load_custom_db")
        loadCustomDbPref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                NEXT_STEP = LOAD_CUSTOM_DB
                requestPermissions()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val removeCustomDbPref: Preference? = findPreference("remove_custom_db")
        removeCustomDbPref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                deleteTempDbFiles()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val sendDbPref: Preference? = findPreference("send_db_by_mail")
        sendDbPref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                NEXT_STEP = SEND_BY_MAIL
                requestPermissions()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        val copyDbPref: Preference? = findPreference("copy_db")
        copyDbPref?.onPreferenceClickListener = OnPreferenceClickListener {
            try {
                NEXT_STEP = COPY_DB_TO_DOC
                requestPermissions()
            } catch (ex: Exception) {
                ex.printStackTrace()
                if (view != null)
                    showSnackBar("${context.getString(R.string.error)}: ${ex.message}", ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
            true
        }

        // Si ya está autentificado,
        // deshabilitar la descarga completa de la base de datos
        if (isLogged()) {
            downloadDbPref?.isEnabled = false
            loadCustomDbPref?.isEnabled = false
            removeCustomDbPref?.isEnabled = false
        }
    }

    private fun selectFileDb() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)

        intent.type = "*/*" //"application/vnd.sqlite3" //"application/x-sqlite3"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            resultForPickFileRequestCode.launch(
                Intent.createChooser(
                    intent, getString(R.string.select_db_file)
                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (view != null) showSnackBar(
                getString(R.string.error_sending_email), ERROR
            )
        }
    }


    private fun showRationaleDialog(onPositiveClick: () -> Unit) {
        AlertDialog.Builder(AssetControlApp.context)
            .setTitle(getString(R.string.permissions_required))
            .setMessage(getString(R.string.this_app_needs_access_to_your_external_storage_to_read_and_write_files))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                onPositiveClick()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                showSnackBar(getString(R.string.app_dont_have_necessary_permissions), ERROR)
            }
            .show()
    }

    private fun requestPermissions(permissionsToRequest: Array<String>) {
        val shouldShowRationale = permissionsToRequest.any {
            shouldShowRequestPermissionRationale(it)
        }

        if (shouldShowRationale) {
            showRationaleDialog {
                requestMultiplePermissionsLauncher.launch(permissionsToRequest)
            }
        } else {
            requestMultiplePermissionsLauncher.launch(permissionsToRequest)
        }
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        permissions.entries.forEach {
            // val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                runNextStep()
            } else {
                showSnackBar(
                    AssetControlApp.context.getString(R.string.app_dont_have_necessary_permissions),
                    ERROR
                )
            }
        }
    }

    private val readMediaPermissions = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    private val readWritePermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun requestPermissions() {
        val context = AssetControlApp.context

        if (readMediaPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            runNextStep()
        } else {
            requestPermissions(readMediaPermissions)
        }
    }

    private fun runNextStep() {
        when (NEXT_STEP) {
            LOAD_CUSTOM_DB -> selectFileDb()
            COPY_DB -> copyDb()
            COPY_DB_TO_DOC -> copyDbToDocuments()
            SEND_BY_MAIL -> sendDbByMail()
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private val resultForPickFileRequestCode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            try {
                if (!(it.resultCode == CommonStatusCodes.SUCCESS || it.resultCode == CommonStatusCodes.SUCCESS_CACHE) || data == null) return@registerForActivityResult

                val dataFile: Uri = data.data ?: return@registerForActivityResult

                tempDbFile = PathHelper.getPath(dataFile) ?: ""
                if (tempDbFile == "") {
                    if (view != null) showSnackBar(
                        getString(R.string.unable_to_open_file), ERROR
                    )
                    return@registerForActivityResult
                }

                val min = 10000
                val max = 99999
                AcDatabase.changeDatabase(
                    String.format("temp%s.sqlite", Random().nextInt(max - min + 1) + min)
                )
                Statics.isCustomDbInUse = true

                activity?.let {
                    if (hasPermissions(activity as Context, readWritePermissions)) {
                        copyDb()
                    } else {
                        NEXT_STEP = COPY_DB
                        requestPermissions()
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(requireActivity(), this::class.java.simpleName, ex)
            }
        }


    private fun sendDbByMail() {
        val context = AssetControlApp.context

        try {
            // Base de datos
            val dbFile = File(context.getDatabasePath(DATABASE_NAME).toString())
            if (!dbFile.exists()) {
                if (view != null)
                    showSnackBar(getString(R.string.database_file_does_not_exist), ERROR)
                return
            }

            // Copiar la base a Documentos
            val result = FileHelper.copyDbToDocuments()
            val outFile = File(result.outFile)
            if (!outFile.exists()) {
                if (view != null)
                    showSnackBar(getString(R.string.database_file_does_not_exist), ERROR)
                return
            }

            val dbFilePath = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                outFile
            )
            if (dbFilePath == null) {
                if (view != null)
                    showSnackBar(getString(R.string.database_file_does_not_exist), ERROR)
                return
            }

            // Último registro de error
            val lastErrorLog = ErrorLog.getLastErrorLog()
            var lastErrorLogPath: Uri? = null
            if (lastErrorLog != null) {
                lastErrorLogPath = FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".provider",
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
            val to = arrayOf("mail@example.com")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)

            // Agregar los adjuntos
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

            // Permisos de lectura de adjuntos
            grantUrisPermissions(requireActivity(), emailIntent, uris)

            // Mensaje
            val pInfo = context.applicationContext.packageManager.getPackageInfo(
                context.packageName, 0
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
                svm.installationCode,
                Statics.newLine,
                svm.clientPackage
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
            if (view != null)
                showSnackBar(getString(R.string.error_sending_email), ERROR)
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
        val context = AssetControlApp.context

        var anyDeleted = false
        val path = context.getDatabasePath(DATABASE_NAME).parent ?: return

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
            if (view != null)
                showSnackBar(getString(R.string.temporary_databases_deleted), SUCCESS)
        } else {
            if (view != null)
                showSnackBar(getString(R.string.no_temporary_bases_found), SnackBarType.INFO)
        }
    }

    private fun copyDbToDocuments() {
        val context = AssetControlApp.context
        val dbFile = context.getDatabasePath(DATABASE_NAME)

        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (documentsDir != null && !documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val outputFile = File(documentsDir, DATABASE_NAME)

        try {
            FileInputStream(dbFile).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val src: FileChannel = inputStream.channel
                    val dst: FileChannel = outputStream.channel
                    dst.transferFrom(src, 0, src.size())
                }
            }
            if (view != null) {
                showSnackBar(
                    String.format(
                        "%s: %s", getString(R.string.database_changed), DATABASE_NAME
                    ), SnackBarType.INFO
                )
            }
        } catch (e: IOException) {
            println("${String.format(getString(R.string.failed_to_copy_database))}: ${Statics.newLine}")
            e.printStackTrace()

            showSnackBar(String.format(getString(R.string.failed_to_copy_database)), ERROR)
        }
    }

    private var tempDbFile: String = ""

    private fun copyDb() {
        if (tempDbFile == "") return

        val context = AssetControlApp.context
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) {
            throw IllegalArgumentException("The current database does not exist.")
        }

        val newDbFile = File(tempDbFile)
        if (!newDbFile.exists()) {
            throw IllegalArgumentException("The database file at the specified path does not exist.")
        }

        try {
            FileInputStream(newDbFile).use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    val src: FileChannel = inputStream.channel
                    val dst: FileChannel = outputStream.channel
                    dst.transferFrom(src, 0, src.size())
                }
            }
            showSnackBar(
                String.format(
                    "%s: %s", getString(R.string.database_changed), DATABASE_NAME
                ), SnackBarType.INFO
            )
        } catch (e: IOException) {
            println("${String.format(getString(R.string.failed_to_replace_database))}: ${Statics.newLine}")
            e.printStackTrace()

            showSnackBar(String.format(getString(R.string.failed_to_replace_database)), ERROR)
            println(": ${e.message}")
        } finally {
            // Reiniciamos la instancia
            cleanInstance()
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
                if (view != null) showSnackBar(
                    getString(R.string.the_database_will_be_downloaded_when_you_return_to_the_login_screen),
                    SnackBarType.INFO
                )
                dialog.dismiss()
            }.setNegativeButton(
                R.string.no
            ) { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun showSnackBar(text: String, type: SnackBarType) {
        showSnackBar(SnackBarEventData(text, type))
    }

    private fun showSnackBar(it: SnackBarEventData) {
        if (requireActivity().isDestroyed || requireActivity().isFinishing) return

        MakeText.makeText(requireView(), it.text, it.snackBarType)
    }

    companion object {
        fun equals(a: Any?, b: Any?): Boolean {
            return a != null && a == b
        }

        private var NEXT_STEP = 0
        private const val COPY_DB_TO_DOC = 4001
        private const val COPY_DB = 3001
        private const val LOAD_CUSTOM_DB = 2001
        private const val SEND_BY_MAIL = 1001
    }
}