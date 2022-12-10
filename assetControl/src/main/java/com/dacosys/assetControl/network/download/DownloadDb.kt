package com.dacosys.assetControl.network.download

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.network.download.DownloadStatus.*
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.sync.*
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.Companion.resetLastUpdateDates
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.views.commons.snackbar.SnackBarEventData
import com.dacosys.assetControl.views.commons.snackbar.SnackBarType
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class DownloadDb(
    private var onDownloadEvent: (DownloadTask) -> Unit = {},
    private var onSnackBarEvent: (SnackBarEventData) -> Unit = {},
) {
    private var timeFileLocation: File? = null
    private var dbFileLocation: File? = null

    companion object {
        ///////////////////////
        // region Constantes //
        const val timeFilename = "android_time.txt"
        const val dbFileName = "android.assetcontroldb.sqlite.txt"
        const val dbDirectory = "collectordb"
    }

    private fun onDownloadFileTask(it: DownloadTask) {
        val msg: String = it.msg
        val downloadStatus: DownloadStatus = it.downloadStatus
        this.fileType = it.fileType
        this.downloadStatus = downloadStatus

        if (downloadStatus == CRASHED) {
            ErrorLog.writeLog(null,
                this::class.java.simpleName,
                "${downloadStatus.name}: ${fileType?.name}, $msg")

            // Si falla en el Timefile puede ser por no tener conexión.
            // No mostrar error
            if (fileType == FileType.TIMEFILE) {
                scope.launch {
                    onUiEvent(SnackBarEventData(getContext().getString(R.string.offline_mode),
                        SnackBarType.INFO))
                }
            } else {
                scope.launch { onUiEvent(SnackBarEventData(msg, SnackBarType.ERROR)) }
            }
        } else if (downloadStatus == FINISHED && fileType == FileType.DBFILE) {
            DataBaseHelper().close()
        }

        Log.d(this::class.java.simpleName, "${downloadStatus.name}: ${fileType?.name}, $msg")
    }

    private suspend fun onUiEvent(it: SnackBarEventData) {
        withContext(Dispatchers.Main) {
            onSnackBarEvent.invoke(it)
        }
    }

    private fun onSyncTaskProgress(it: SyncProgress) {
        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        this.progressStatus = progressStatus
        val progressStatusDesc = progressStatus.description
        var registryDesc = getContext().getString(R.string.all_tasks)

        if (registryType != null) {
            registryDesc = registryType.description
        }

        if (downloadStatus == CRASHED) {
            ErrorLog.writeLog(null,
                this::class.java.simpleName,
                "$progressStatusDesc: $registryDesc, $msg")

            scope.launch { onUiEvent(SnackBarEventData(msg, SnackBarType.ERROR)) }
        }

        Log.d(this::class.java.simpleName, "$progressStatusDesc: $registryDesc, $msg ${
            Statics.getPercentage(completedTask, totalTask)
        }")
    }

    /////////////////////
    // region Privadas //
    private var forceDownload: Boolean = false

    private var errorMsg = ""
    private var resultStatus: ProgressStatus? = null
    private var downloadStatus: DownloadStatus? = null
    private var progressStatus: ProgressStatus? = null

    private var oldDateTimeStr: String = ""
    private var currentDateTimeStr: String = ""

    private var fileType: FileType? = null
    // endregion Privadas //
    ////////////////////////

    private val timeUrl = "${Statics.wsUrlCron}/$dbDirectory/$timeFilename"
    private val dbUrl = "${Statics.wsUrlCron}/$dbDirectory/$dbFileName"
    // endregion Constantes //
    //////////////////////////

    private fun deleteTimeFile() {
        timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        if ((timeFileLocation ?: return).exists()) {
            (timeFileLocation ?: return).delete()
        }

        forceDownload = true
    }

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            when (it.status) {
                ProgressStatus.finished -> {
                    launchDownload()
                }
                ProgressStatus.crashed -> {
                    onDownloadEvent.invoke(DownloadTask(
                        msg = it.msg,
                        fileType = fileType,
                        downloadStatus = CRASHED,
                    ))
                }
                ProgressStatus.canceled -> {
                    onDownloadEvent.invoke(DownloadTask(
                        msg = it.msg,
                        fileType = fileType,
                        downloadStatus = CANCELED,
                    ))
                }
            }
        }
        GetMySqlDate(Statics.getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun postExecute(result: Boolean) {
        forceDownload = false
        if (result) {
            Log.d(this::class.java.simpleName, getContext().getString(R.string.ok))
        } else {
            if (resultStatus == ProgressStatus.canceled) {
                scope.launch { onUiEvent(SnackBarEventData(errorMsg, SnackBarType.INFO)) }
            } else if (resultStatus == ProgressStatus.crashed) {
                scope.launch { onUiEvent(SnackBarEventData(errorMsg, SnackBarType.ERROR)) }
                ErrorLog.writeLog(null, this::class.java.simpleName, errorMsg)
            }
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private fun launchDownload() {
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground() {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        postExecute(result)
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        return@withContext startDownload()
    }

    private fun startDownload(): Boolean {
        resultStatus = ProgressStatus.starting
        onDownloadEvent.invoke(DownloadTask(
            msg = getContext().getString(R.string.starting_download),
            fileType = fileType,
            downloadStatus = STARTING,
        ))

        try {
            if (Statics.wsUrlCron.isEmpty()) {
                errorMsg = getContext().getString(R.string.webservice_is_not_configured)
                resultStatus = ProgressStatus.crashed
                onDownloadEvent.invoke(DownloadTask(
                    msg = errorMsg,
                    fileType = fileType,
                    downloadStatus = CRASHED,
                ))
                return false
            }

            if (!forceDownload) {
                /////////////////////////////////////
                // region: Enviar datos pendientes //

                // Si aún no está loggeado y hay datos por enviar, no descargar la base de datos
                if (Statics.currentUserId == null && Statics.pendingDelivery()) {
                    errorMsg =
                        getContext().getString(R.string.the_database_will_not_be_downloaded_because_there_is_data_pending_delivery)
                    resultStatus = ProgressStatus.canceled
                    onDownloadEvent.invoke(DownloadTask(
                        msg = errorMsg,
                        fileType = fileType,
                        downloadStatus = CANCELED,
                    ))
                    return false
                }

                progressStatus = null
                SyncUpload(onSyncTaskProgress = { onSyncTaskProgress(it) })

                // Espera hasta que salga del SyncUpload
                while (true) {
                    if (progressStatus == ProgressStatus.bigFinished) {
                        break
                    } else if (progressStatus == ProgressStatus.crashed || progressStatus == ProgressStatus.canceled) {
                        resultStatus = ProgressStatus.crashed
                        onDownloadEvent.invoke(DownloadTask(
                            msg = getContext().getString(R.string.could_not_send_pending_data),
                            fileType = fileType,
                            downloadStatus = CANCELED,
                        ))
                        return false
                    }
                }
                // endregion: Enviar datos pendientes //
                ////////////////////////////////////////
            }

            // Leer el archivo antiguo de fecha de creación de la base de datos
            // en el servidor, si la esta fecha es igual a la del archivo del servidor,
            // no hace falta descargar la base de datos.
            if (timeFileLocation!!.exists()) {
                oldDateTimeStr = getDateTimeStr()
                timeFileLocation!!.delete()
            }

            downloadStatus = null

            DownloadFile(
                urlDestination = UrlDestParam(
                    url = timeUrl,
                    destination = timeFileLocation!!),
                fileType = FileType.TIMEFILE) { onDownloadFileTask(it) }

            var crashNr = 0
            var cancelThread = false
            loop@ while (true) {
                if (downloadStatus != null && fileType != null) {
                    // Si se cancela, sale
                    when (downloadStatus) {
                        CANCELED -> {
                            cancelThread = true
                            break@loop
                        }
                        FINISHED -> {
                            // Si estamos descargando el archivo de la fecha
                            // y termina de descargarse salir del loop para poder hacer
                            // las comparaciones
                            if (fileType == FileType.TIMEFILE) {
                                break@loop
                            } else {
                                downloadStatus = null
                                DownloadFile(urlDestination = UrlDestParam(timeUrl,
                                    timeFileLocation!!),
                                    fileType = FileType.TIMEFILE) { onDownloadFileTask(it) }
                            }
                        }
                        CRASHED -> {
                            // Si no existe el archivo con la fecha en el servidor
                            // es porque aún no se creó la base de datos.
                            // Generate DB
                            if (fileType == FileType.TIMEFILE) {
                                // Si falla al bajar la fecha
                                crashNr++
                            }

                            if (crashNr > 1) {
                                // Si ya falló dos veces en bajar la fecha, a la mierda.
                                cancelThread = true
                                break@loop
                            }
                        }
                        else -> {
                            continue@loop
                        }
                    }
                }
                // Poner un timer o algo que salga de acá
            }

            if (cancelThread) {
                errorMsg =
                    getContext().getString(R.string.failed_to_get_the_db_creation_date_from_the_server)
                resultStatus = ProgressStatus.crashed
                onDownloadEvent.invoke(DownloadTask(
                    msg = errorMsg,
                    fileType = FileType.TIMEFILE,
                    downloadStatus = CRASHED,
                ))
                return false
            }

            //Read text from file
            currentDateTimeStr = getDateTimeStr()
            if (!hasNewVersion(oldDateTimeStr, currentDateTimeStr)) {
                Log.d(this::class.java.simpleName,
                    getContext().getString(R.string.is_not_necessary_to_download_the_database))

                SyncInitialUser { onSyncTaskProgress(it) }
                SyncStatics()

                resultStatus = ProgressStatus.finished
                onDownloadEvent.invoke(DownloadTask(
                    msg = getContext().getString(R.string.is_not_necessary_to_download_the_database),
                    fileType = null,
                    downloadStatus = CANCELED,
                ))
                return true
            }

            try {
                downloadStatus = null

                DownloadFile(urlDestination = UrlDestParam(dbUrl, dbFileLocation!!),
                    fileType = FileType.DBFILE) { onDownloadFileTask(it) }

                loop@ while (true) {
                    if (downloadStatus != null && fileType != null) {
                        // Si se cancela, sale
                        when (downloadStatus) {
                            CANCELED, CRASHED -> {
                                cancelThread = true
                                break@loop
                            }
                            FINISHED -> {
                                break@loop
                            }
                            else -> {
                                continue@loop
                            }
                        }
                    }
                }

                if (cancelThread) {
                    errorMsg =
                        getContext().getString(R.string.error_downloading_the_database_from_the_server)
                    resultStatus = ProgressStatus.crashed
                    onDownloadEvent.invoke(DownloadTask(
                        msg = errorMsg,
                        fileType = FileType.DBFILE,
                        downloadStatus = CRASHED,
                    ))
                    return false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                errorMsg = "${
                    getContext().getString(R.string.exception_error)
                } (Download DBFILE): $ex"

                resultStatus = ProgressStatus.crashed
                onDownloadEvent.invoke(DownloadTask(
                    msg = errorMsg,
                    fileType = fileType,
                    downloadStatus = CRASHED,
                ))
                return false
            }

            saveLastUpdateDates(currentDateTimeStr)
            Statics.downloadDbRequired = false

            return if (copyDataBase()) {
                SyncInitialUser { onSyncTaskProgress(it) }
                SyncStatics()

                resultStatus = ProgressStatus.finished
                onDownloadEvent.invoke(DownloadTask(
                    msg = getContext().getString(R.string.ok),
                    fileType = fileType,
                    downloadStatus = FINISHED,
                ))
                true
            } else {
                resultStatus = ProgressStatus.crashed
                onDownloadEvent.invoke(DownloadTask(
                    getContext().getString(R.string.error_when_copying_database),
                    fileType,
                    CRASHED,
                ))
                false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorMsg = getContext().getString(R.string.error_downloading_the_database)

            resultStatus = ProgressStatus.crashed
            onDownloadEvent.invoke(DownloadTask(
                msg = errorMsg,
                fileType = fileType,
                downloadStatus = CRASHED,
            ))
            return false
        }
    }

    private fun hasNewVersion(oldDate: String, newDate: String): Boolean {
        try {
            Log.d(this::class.java.simpleName,
                "DATABASE OLD VERSION: ${oldDate}, NEW VERSION: $newDate")
            if (oldDate.isEmpty()) {
                return true
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val oldDateParsed = sdf.parse(oldDate)
            val newDateParsed = sdf.parse(newDate)

            return if (oldDateParsed == null) {
                true
            } else {
                oldDateParsed < newDateParsed
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getDateTimeStr(): String {
        var dateTime = ""
        //Read text from file
        try {
            val br = BufferedReader(FileReader(timeFileLocation!!.absolutePath))
            while (true) {
                dateTime = br.readLine() ?: break
            }
            br.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorMsg = "${
                getContext().getString(R.string.failed_to_get_the_date_from_the_file)
            }: $ex"
        }
        return dateTime
    }

    /**
     * Copia la base de datos descarga a la ubicación de la DB de la aplicación.
     * Antes elimina la DB antigua.
     */
    private fun copyDataBase(): Boolean {
        Log.d(this::class.java.simpleName, getContext().getString(R.string.copying_database))

        if (dbFileLocation == null || !dbFileLocation!!.exists()) {
            Log.e(this::class.java.simpleName,
                getContext().getString(R.string.database_file_does_not_exist))
            return false
        }

        DataBaseHelper().deleteDb()
        SQLiteDatabase.releaseMemory()

        //Open your local db as the input stream
        val myInput = FileInputStream(dbFileLocation)

        // Path to the just created empty db
        val outFileName = getContext().getDatabasePath(Statics.DATABASE_NAME).toString()

        Log.d(this::class.java.simpleName,
            "${getContext().getString(R.string.origin)}: ${dbFileLocation!!.absolutePath}")
        Log.d(this::class.java.simpleName,
            "${getContext().getString(R.string.destination)}: $outFileName")

        try {
            //Open the empty db as the output stream
            val myOutput = FileOutputStream(outFileName)

            //transfer bytes from the inputfile to the outputfile
            val buffer = ByteArray(1024)
            var length: Int
            while (run {
                    length = myInput.read(buffer)
                    length
                } > 0) {
                myOutput.write(buffer, 0, length)
            }

            //Close the streams
            myOutput.flush()
            myOutput.close()
            myInput.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        }

        Log.d(this::class.java.simpleName, getContext().getString(R.string.copy_ok))
        return true
    }

    private fun saveLastUpdateDates(timeDateStr: String): Boolean {
        try {
            if (timeDateStr.isNotEmpty()) {
                // set last time of update
                val registryTypeUpdated: ArrayList<ConfEntry> = ArrayList()
                registryTypeUpdated.add(ConfEntry.acLastUpdateAsset)
                registryTypeUpdated.add(ConfEntry.acLastUpdateWarehouseArea)
                registryTypeUpdated.add(ConfEntry.acLastUpdateWarehouse)
                registryTypeUpdated.add(ConfEntry.acLastUpdateItemCategory)
                registryTypeUpdated.add(ConfEntry.acLastUpdateUser)
                registryTypeUpdated.add(ConfEntry.acLastUpdateAttribute)
                registryTypeUpdated.add(ConfEntry.acLastUpdateAttributeCategory)
                registryTypeUpdated.add(ConfEntry.acLastUpdateDataCollectionRule)
                registryTypeUpdated.add(ConfEntry.acLastUpdateRoute)
                registryTypeUpdated.add(ConfEntry.acLastUpdateBarcodeLabelCustom)

                val registries: ArrayList<String> = ArrayList()
                for (confEntry in registryTypeUpdated) {
                    registries.add(confEntry.description)
                }
                Statics.prefsPutString(registries, timeDateStr)

                registries.clear()
                registries.add(ConfEntry.acLastUpdateRepairshop.description)
                registries.add(ConfEntry.acLastUpdateAssetManteinance.description)
                registries.add(ConfEntry.acLastUpdateManteinanceType.description)
                registries.add(ConfEntry.acLastUpdateManteinanceTypeGroup.description)
                Statics.prefsPutString(registries, Statics.defaultDate)
            } else {
                scope.launch {
                    onUiEvent(SnackBarEventData(getContext().getString(R.string.the_creation_date_of_the_db_on_the_server_is_invalid),
                        SnackBarType.ERROR))
                }
                return false
            }

            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }
        return false
    }

    init {
        this.timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        this.dbFileLocation = File(getContext().cacheDir.absolutePath + "/" + dbFileName)

        // Antes de iniciar el proceso de descarga...
        if (Statics.downloadDbRequired) {
            resetLastUpdateDates()
            deleteTimeFile()
        }

        checkConnection()
    }
}