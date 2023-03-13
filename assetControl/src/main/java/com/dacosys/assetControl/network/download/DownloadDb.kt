package com.dacosys.assetControl.network.download

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.sync.*
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.ui.common.snackbar.SnackBarEventData
import com.dacosys.assetControl.ui.common.snackbar.SnackBarType
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
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
    private var oldDateTimeStr: String = ""
    private var currentDateTimeStr: String = ""

    ///////////////////////
    // region Constantes //
    private val timeFilename = "android_time.txt"
    private val dbFileName = "android.assetcontroldb.sqlite.txt"
    private val dbDirectory = "collectordb"

    private val timeUrl = "${Statics.wsUrlCron}/${dbDirectory}/${timeFilename}"
    private val dbUrl = "${Statics.wsUrlCron}/${dbDirectory}/${dbFileName}"

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        scope.launch {
            preExecute()
        }
    }

    private suspend fun preExecute() = withContext(Dispatchers.IO) {
        timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        dbFileLocation = File(getContext().cacheDir.absolutePath + "/" + dbFileName)

        // Antes de iniciar el proceso de descarga...
        if (Statics.downloadDbRequired) {
            Statics.resetLastUpdateDates()
            deleteTimeFile()
        }

        checkConnection()
    }

    private fun deleteTimeFile() {
        timeFileLocation = File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        if ((timeFileLocation ?: return).exists()) {
            (timeFileLocation ?: return).delete()
        }
    }

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            when (it.status) {
                ProgressStatus.finished -> {
                    launchDownload()
                }
                ProgressStatus.crashed -> {
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = null,
                            downloadStatus = DownloadStatus.CRASHED,
                        )
                    )
                }
                ProgressStatus.canceled -> {
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = null,
                            downloadStatus = DownloadStatus.CANCELED,
                        )
                    )
                }
            }
        }

        GetMySqlDate(Statics.getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun launchDownload() {
        if (Statics.wsUrlCron.isEmpty()) {
            onDownloadEvent.invoke(
                DownloadTask(
                    msg = getContext().getString(R.string.webservice_is_not_configured),
                    fileType = null,
                    downloadStatus = DownloadStatus.CRASHED,
                )
            )
            return
        }

        // Si aún no está loggeado y hay datos por enviar, no descargar la base de datos
        if (Statics.currentUserId == null && Statics.pendingDelivery()) {
            onDownloadEvent.invoke(
                DownloadTask(
                    msg = getContext().getString(R.string.the_database_will_not_be_downloaded_because_there_is_data_pending_delivery),
                    fileType = null,
                    downloadStatus = DownloadStatus.CANCELED,
                )
            )
            return
        }

        // Primero subimos los pendientes.
        SyncUpload(onSyncTaskProgress = { onSyncUploadPendingProgress(it) })
    }

    private fun onSyncUploadPendingProgress(it: SyncProgress) {
        val totalTask: Int = it.totalTask
        val completedTask: Int = it.completedTask
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        val progressStatusDesc = progressStatus.description
        var registryDesc = getContext().getString(R.string.all_tasks)

        if (registryType != null) {
            registryDesc = registryType.description
        }

        if (progressStatus == ProgressStatus.crashed || progressStatus == ProgressStatus.canceled) {
            ErrorLog.writeLog(
                null, this::class.java.simpleName, "$progressStatusDesc: $registryDesc, $msg"
            )

            onDownloadEvent.invoke(
                DownloadTask(
                    msg = msg,
                    fileType = null,
                    downloadStatus = DownloadStatus.CRASHED,
                )
            )
            return
        }

        Log.d(
            this::class.java.simpleName, "$progressStatusDesc: $registryDesc, $msg ${
                Statics.getPercentage(completedTask, totalTask)
            }"
        )

        if (progressStatus == ProgressStatus.bigFinished) {
            // Continuamos con la descarga del archivo de la fecha de creación
            // de la base de datos remota
            downloadTimeFile()
        }
    }

    private fun onSyncUsersProgress(it: SyncProgress) {
        val msg: String = it.msg
        val registryType: SyncRegistryType? = it.registryType
        val progressStatus: ProgressStatus = it.progressStatus

        if (progressStatus == ProgressStatus.crashed || progressStatus == ProgressStatus.crashed) {
            ErrorLog.writeLog(
                null,
                this::class.java.simpleName,
                "${progressStatus.description}: ${registryType?.description}, $msg"
            )

            onDownloadEvent.invoke(
                DownloadTask(
                    msg = msg,
                    fileType = null,
                    downloadStatus = DownloadStatus.CRASHED,
                )
            )
            return
        }
    }

    private fun downloadTimeFile() {
        // Leer el archivo antiguo de fecha de creación de la base de datos
        // en el servidor, si la esta fecha es igual a la del archivo del servidor,
        // no hace falta descargar la base de datos.
        if (timeFileLocation!!.exists()) {
            oldDateTimeStr = getDateTimeStr()
            timeFileLocation!!.delete()
        }

        DownloadFile(
            urlDestination = UrlDestParam(
                url = timeUrl, destination = timeFileLocation!!
            ), fileType = FileType.TIMEFILE
        ) { onDownloadTimeFileTask(it) }
    }

    private fun onDownloadTimeFileTask(it: DownloadTask) {
        val downloadStatus = it.downloadStatus
        val msg = it.msg

        when (downloadStatus) {
            DownloadStatus.CANCELED, DownloadStatus.CRASHED -> {
                onDownloadEvent.invoke(
                    DownloadTask(
                        msg = msg,
                        fileType = null,
                        downloadStatus = DownloadStatus.CRASHED,
                    )
                )
                return
            }
            DownloadStatus.FINISHED -> {
                // Seguimos con la comparación de fechas y descarga de base de datos
                downloadDbFile()
            }
            else -> {
                scope.launch { onUiEvent(SnackBarEventData(msg, SnackBarType.INFO)) }
            }
        }
    }

    private fun downloadDbFile() {
        //Read text from file
        currentDateTimeStr = getDateTimeStr()

        if (!hasNewVersion(oldDateTimeStr, currentDateTimeStr)) {
            val msg = getContext().getString(R.string.is_not_necessary_to_download_the_database)

            Log.d(this::class.java.simpleName, msg)

            SyncInitialUser {
                onSyncUsersProgress(it)

                if (it.progressStatus == ProgressStatus.finished) {
                    SyncStatics()
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = msg,
                            fileType = null,
                            downloadStatus = DownloadStatus.FINISHED,
                        )
                    )
                } else if (it.progressStatus == ProgressStatus.starting) {
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = null,
                            downloadStatus = DownloadStatus.STARTING
                        )
                    )
                } else if (it.progressStatus == ProgressStatus.running) {
                    var progress = 0
                    val completedTask: Long = it.completedTask.toLong()
                    val totalTask: Long = it.totalTask.toLong()

                    if (completedTask > 0 && totalTask > 0)
                        progress = (completedTask * 100 / totalTask).toInt()

                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = null,
                            downloadStatus = DownloadStatus.DOWNLOADING,
                            progress = progress,
                            bytesCompleted = completedTask,
                            bytesTotal = totalTask
                        )
                    )
                }
            }
        } else {
            DownloadFile(
                urlDestination = UrlDestParam(dbUrl, dbFileLocation!!), fileType = FileType.DBFILE
            ) { onDownloadDbFileTask(it) }
        }
    }

    private fun onDownloadDbFileTask(it: DownloadTask) {
        val downloadStatus = it.downloadStatus
        val fileType = it.fileType
        val msg = it.msg

        if (downloadStatus == DownloadStatus.FINISHED) {
            DataBaseHelper().close()
            prepareDataBase()
        } else {
            onDownloadEvent.invoke(
                DownloadTask(
                    msg = msg,
                    fileType = fileType,
                    downloadStatus = downloadStatus,
                )
            )
        }
    }

    private fun prepareDataBase() {
        saveLastUpdateDates(currentDateTimeStr)
        Statics.downloadDbRequired = false

        if (copyDataBase()) {
            SyncInitialUser {
                onSyncUsersProgress(it)

                if (it.progressStatus == ProgressStatus.finished) {
                    SyncStatics()
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = getContext().getString(R.string.ok),
                            fileType = FileType.DBFILE,
                            downloadStatus = DownloadStatus.FINISHED,
                        )
                    )
                } else if (it.progressStatus == ProgressStatus.starting) {
                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = FileType.DBFILE,
                            downloadStatus = DownloadStatus.STARTING
                        )
                    )
                } else if (it.progressStatus == ProgressStatus.running) {
                    var progress = 0
                    val completedTask: Long = it.completedTask.toLong()
                    val totalTask: Long = it.totalTask.toLong()

                    if (completedTask > 0 && totalTask > 0)
                        progress = (completedTask * 100 / totalTask).toInt()

                    onDownloadEvent.invoke(
                        DownloadTask(
                            msg = it.msg,
                            fileType = FileType.DBFILE,
                            downloadStatus = DownloadStatus.DOWNLOADING,
                            progress = progress,
                            bytesCompleted = completedTask,
                            bytesTotal = totalTask
                        )
                    )
                }
            }
        } else {
            onDownloadEvent.invoke(
                DownloadTask(
                    getContext().getString(R.string.error_when_copying_database),
                    FileType.DBFILE,
                    DownloadStatus.CRASHED,
                )
            )
        }
    }

    private fun hasNewVersion(oldDate: String, newDate: String): Boolean {
        try {
            Log.d(
                this::class.java.simpleName,
                "DATABASE OLD VERSION: ${oldDate}, NEW VERSION: $newDate"
            )
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
            scope.launch {
                onUiEvent(
                    SnackBarEventData(
                        getContext().getString(R.string.failed_to_get_the_date_from_the_file),
                        SnackBarType.ERROR
                    )
                )
            }
        }
        return dateTime
    }

    private suspend fun onUiEvent(it: SnackBarEventData) {
        withContext(Dispatchers.Main) {
            onSnackBarEvent.invoke(it)
        }
    }

    /**
     * Copia la base de datos descarga a la ubicación de la DB de la aplicación.
     * Antes elimina la DB antigua.
     */
    private fun copyDataBase(): Boolean {
        Log.d(this::class.java.simpleName, getContext().getString(R.string.copying_database))

        if (dbFileLocation == null || !dbFileLocation!!.exists()) {
            Log.e(
                this::class.java.simpleName,
                getContext().getString(R.string.database_file_does_not_exist)
            )
            return false
        }

        DataBaseHelper().deleteDb()
        SQLiteDatabase.releaseMemory()

        //Open your local db as the input stream
        val myInput = FileInputStream(dbFileLocation)

        // Path to the just created empty db
        val outFileName = getContext().getDatabasePath(Statics.DATABASE_NAME).toString()

        Log.d(
            this::class.java.simpleName,
            "${getContext().getString(R.string.origin)}: ${dbFileLocation!!.absolutePath}"
        )
        Log.d(
            this::class.java.simpleName,
            "${getContext().getString(R.string.destination)}: $outFileName"
        )

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
                    onUiEvent(
                        SnackBarEventData(
                            getContext().getString(R.string.the_creation_date_of_the_db_on_the_server_is_invalid),
                            SnackBarType.ERROR
                        )
                    )
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
        execute()
    }
}