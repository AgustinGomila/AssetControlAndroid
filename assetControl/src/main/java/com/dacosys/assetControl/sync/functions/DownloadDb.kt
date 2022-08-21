package com.dacosys.assetControl.sync.functions

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.View
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.Statics.AssetControl.Companion.getContext
import com.dacosys.assetControl.utils.Statics.Companion.resetLastUpdateDates
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.cleanInstance
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.sync.functions.DownloadDb.DownloadStatus.*
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.views.commons.snackbar.MakeText.Companion.makeText
import com.dacosys.assetControl.views.commons.snackbar.SnackbarType
import kotlinx.coroutines.*
import java.io.*
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class DownloadDb :
    SyncTaskProgress,
    DownloadFileTask.OnDownloadFileTask {

    companion object {
        ///////////////////////
        // region Constantes //
        const val timeFilename = "android_time.txt"
        const val dbFileName = "android.assetcontroldb.sqlite.txt"
        const val dbDirectory = "collectordb"
    }

    interface DownloadDbTask {
        fun onDownloadDbTask(
            msg: String,
            fileType: FileType?,
            downloadStatus: DownloadStatus,
            progress: Int?,
            bytesCompleted: Long?,
            bytesTotal: Long?,
        )
    }

    override fun onDownloadFileTask(
        msg: String,
        fileType: FileType,
        downloadStatus: DownloadStatus,
        progress: Int?,
        bytesCompleted: Long?,
        bytesTotal: Long?,
    ) {
        this.fileType = fileType
        this.downloadStatus = downloadStatus

        if (downloadStatus == CRASHED) {
            ErrorLog.writeLog(
                null,
                this::class.java.simpleName,
                "${downloadStatus.name}: ${fileType.name}, $msg"
            )

            // Si falla en el Timefile puede ser por no tener conexión.
            // No mostrar error
            if (fileType == FileType.TIMEFILE) {
                makeText(
                    parentView ?: return,
                    getContext().getString(R.string.offline_mode),
                    SnackbarType.INFO
                )
            } else {
                makeText(parentView ?: return, msg, SnackbarType.ERROR)
            }
        } else if (downloadStatus == FINISHED && fileType == FileType.DBFILE) {
            DataBaseHelper().close()
        }

        Log.d(this::class.java.simpleName, "${downloadStatus.name}: ${fileType.name}, $msg")
    }

    override fun onSyncTaskProgress(
        totalTask: Int,
        completedTask: Int,
        msg: String,
        registryType: SyncRegistryType?,
        progressStatus: ProgressStatus,
    ) {
        this.progressStatus = progressStatus

        val progressStatusDesc = progressStatus.description
        var registryDesc = getContext().getString(R.string.all_tasks)

        if (registryType != null) {
            registryDesc = registryType.description
        }

        if (downloadStatus == CRASHED) {
            ErrorLog.writeLog(
                null,
                this::class.java.simpleName,
                "$progressStatusDesc: $registryDesc, $msg"
            )
            makeText(parentView ?: return, msg, SnackbarType.ERROR)
        }

        Log.d(
            this::class.java.simpleName,
            "$progressStatusDesc: $registryDesc, $msg ${
                Statics.getPercentage(
                    completedTask,
                    totalTask
                )
            }"
        )
    }

    /**
     * Tiene el tipo de archivo que se está descargando.
     * De esto depende la lógica para la secuencia de descargas.
     */
    @Suppress("unused")
    enum class FileType(val id: Long) {
        TIMEFILE(1),
        CREATIONLOGFILE(2),
        DBFILE(3)
    }

    /**
     * Tiene los diferentes estados durante una descarga
     */
    enum class DownloadStatus(val id: Long) {
        STARTING(1),
        DOWNLOADING(2),
        CANCELED(3),
        FINISHED(4),
        CRASHED(5),
        INFO(6)
    }

    /////////////////////
    // region Privadas //
    private var forceDownload: Boolean = false

    private var errorMsg = ""
    private var resultStatus: ProgressStatus? = null
    private var downloadStatus: DownloadStatus? = null
    private var progressStatus: ProgressStatus? = null

    private var timeFileLocation: File? = null
    private var dbFileLocation: File? = null

    private var oldDateTimeStr: String = ""
    private var currentDateTimeStr: String = ""

    private var fileType: FileType? = null

    private var mCallback: DownloadDbTask? = null

    private var weakRefView: WeakReference<View>? = null
    private var parentView: View?
        get() {
            return weakRefView?.get()
        }
        set(value) {
            weakRefView = if (value != null) WeakReference(value) else null
        }
    // endregion Privadas //
    ////////////////////////

    private val timeUrl = "${Statics.wsUrlCron}/$dbDirectory/$timeFilename"
    private val dbUrl = "${Statics.wsUrlCron}/$dbDirectory/$dbFileName"
    // endregion Constantes //
    //////////////////////////

    fun addParams(parentView: View, callback: DownloadDbTask) {
        this.parentView = parentView
        this.mCallback = callback
        this.timeFileLocation =
            File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        this.dbFileLocation =
            File(getContext().cacheDir.absolutePath + "/" + dbFileName)
    }

    fun execute() {
        preExecute()
    }

    private fun deleteTimeFile() {
        timeFileLocation =
            File(getContext().cacheDir.absolutePath + "/" + timeFilename)
        if (timeFileLocation != null && (timeFileLocation ?: return).exists()) {
            (timeFileLocation ?: return).delete()
        }

        forceDownload = true
    }

    private fun preExecute() {
        if (Statics.downloadDbRequired) {
            resetLastUpdateDates()
            deleteTimeFile()
        }

        checkConnection()
    }

    private fun checkConnection() {
        thread {
            val getMySqlDate = GetMySqlDate()
            val r = getMySqlDate.execute(Statics.getWebservice())

            when (r.status) {
                ProgressStatus.finished -> {
                    val result = doInBackground()
                    postExecute(result)
                }
                ProgressStatus.crashed -> {
                    mCallback?.onDownloadDbTask(
                        msg = r.msg,
                        fileType = fileType,
                        downloadStatus = CRASHED,
                        progress = null,
                        bytesCompleted = null,
                        bytesTotal = null
                    )
                }
                ProgressStatus.canceled -> {
                    mCallback?.onDownloadDbTask(
                        msg = r.msg,
                        fileType = fileType,
                        downloadStatus = CANCELED,
                        progress = null,
                        bytesCompleted = null,
                        bytesTotal = null
                    )
                }
            }
        }
    }

    private fun postExecute(result: Boolean): Boolean {
        forceDownload = false

        if (result) {
            Log.d(this::class.java.simpleName, getContext().getString(R.string.ok))
        } else {
            if (resultStatus == ProgressStatus.canceled) {
                if (parentView != null) makeText(parentView!!, errorMsg, SnackbarType.INFO)
            } else if (resultStatus == ProgressStatus.crashed) {
                if (parentView != null) makeText(parentView!!, errorMsg, SnackbarType.ERROR)
                ErrorLog.writeLog(null, this::class.java.simpleName, errorMsg)
            }
        }

        return result
    }

    private var deferred: Deferred<Boolean>? = null

    private fun doInBackground(): Boolean {
        var result = false
        runBlocking {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        return@withContext startDownload()
    }

    private fun startDownload(): Boolean {
        resultStatus = ProgressStatus.starting
        mCallback?.onDownloadDbTask(
            msg = getContext().getString(R.string.starting_download),
            fileType = fileType,
            downloadStatus = STARTING,
            progress = null,
            bytesCompleted = null,
            bytesTotal = null
        )

        try {
            if (Statics.wsUrlCron.isEmpty()) {
                errorMsg = getContext().getString(R.string.webservice_is_not_configured)
                resultStatus = ProgressStatus.crashed
                mCallback?.onDownloadDbTask(
                    msg = errorMsg,
                    fileType = fileType,
                    downloadStatus = CRASHED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
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
                    mCallback?.onDownloadDbTask(
                        msg = errorMsg,
                        fileType = fileType,
                        downloadStatus = CANCELED,
                        progress = null,
                        bytesCompleted = null,
                        bytesTotal = null
                    )
                    return false
                }

                progressStatus = null

                thread {
                    val syncUpload = SyncUpload()
                    syncUpload.addParams(WeakReference(this))
                    syncUpload.execute() //<-- TODO: Horrible while!!! Escuchar eventos!
                }

                // Espera hasta que salga del SyncUpload
                while (true) {
                    if (progressStatus == ProgressStatus.bigFinished) {
                        break
                    } else if (progressStatus == ProgressStatus.crashed ||
                        progressStatus == ProgressStatus.canceled
                    ) {
                        resultStatus = ProgressStatus.crashed
                        mCallback?.onDownloadDbTask(
                            msg = getContext().getString(R.string.could_not_send_pending_data),
                            fileType = fileType,
                            downloadStatus = CANCELED,
                            progress = null,
                            bytesCompleted = null,
                            bytesTotal = null
                        )
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

            var downloadTask = DownloadFileTask()
            downloadTask.addParams(
                UrlDestParam(timeUrl, timeFileLocation!!),
                this,
                FileType.TIMEFILE
            )
            downloadTask.execute() // <-- TODO: VER ESTO!!! Es horrible, eliminar el while

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
                                downloadTask = DownloadFileTask()
                                downloadTask.addParams(
                                    UrlDestParam(timeUrl, timeFileLocation!!),
                                    this,
                                    FileType.TIMEFILE
                                )
                                downloadTask.execute()
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
                mCallback?.onDownloadDbTask(
                    msg = errorMsg,
                    fileType = FileType.TIMEFILE,
                    downloadStatus = CRASHED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
                return false
            }

            //Read text from file
            currentDateTimeStr = getDateTimeStr()
            if (!hasNewVersion(oldDateTimeStr, currentDateTimeStr)) {
                Log.d(
                    this::class.java.simpleName,
                    getContext().getString(R.string.is_not_necessary_to_download_the_database)
                )

                SyncDownload.initialUser(this)
                SyncDownload.insertStatics()

                resultStatus = ProgressStatus.finished
                mCallback?.onDownloadDbTask(
                    msg = getContext().getString(R.string.is_not_necessary_to_download_the_database),
                    fileType = null,
                    downloadStatus = CANCELED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
                return true
            }

            // Eliminar la base de datos antigua
            if (dbFileLocation!!.exists()) {
                dbFileLocation!!.delete()
            }

            try {
                downloadStatus = null

                downloadTask = DownloadFileTask()
                downloadTask.addParams(UrlDestParam(dbUrl, dbFileLocation!!), this, FileType.DBFILE)
                downloadTask.execute() // <-- TODO: Esto es horrible, sacar el while

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
                    mCallback?.onDownloadDbTask(
                        msg = errorMsg,
                        fileType = FileType.DBFILE,
                        downloadStatus = CRASHED,
                        progress = null,
                        bytesCompleted = null,
                        bytesTotal = null
                    )
                    return false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                errorMsg = "${
                    getContext().getString(R.string.exception_error)
                } (Download DBFILE): $ex"

                resultStatus = ProgressStatus.crashed
                mCallback?.onDownloadDbTask(
                    msg = errorMsg,
                    fileType = fileType,
                    downloadStatus = CRASHED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
                return false
            }

            saveLastUpdateDates(currentDateTimeStr)
            Statics.downloadDbRequired = false

            return if (copyDataBase()) {
                SyncDownload.initialUser(this)
                SyncDownload.insertStatics()

                resultStatus = ProgressStatus.finished
                mCallback?.onDownloadDbTask(
                    msg = "OK",
                    fileType = fileType,
                    downloadStatus = FINISHED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
                true
            } else {
                resultStatus = ProgressStatus.crashed
                mCallback?.onDownloadDbTask(
                    "Error al copiar la base de datos",
                    fileType,
                    CRASHED,
                    progress = null,
                    bytesCompleted = null,
                    bytesTotal = null
                )
                false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorMsg =
                getContext().getString(R.string.error_downloading_the_database)

            resultStatus = ProgressStatus.crashed
            mCallback?.onDownloadDbTask(
                msg = errorMsg,
                fileType = fileType,
                downloadStatus = CRASHED,
                progress = null,
                bytesCompleted = null,
                bytesTotal = null
            )
            return false
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
            errorMsg =
                "${
                    getContext().getString(R.string.failed_to_get_the_date_from_the_file)
                }: $ex"
        }
        return dateTime
    }

    private fun copyDataBase(): Boolean {
        Log.d(this::class.java.simpleName, getContext().getString(R.string.copying_database))

        if (dbFileLocation == null || !dbFileLocation!!.exists()) {
            Log.e(
                this::class.java.simpleName,
                getContext().getString(R.string.database_file_does_not_exist)
            )
            return false
        }

        DataBaseHelper().close()
        SQLiteDatabase.releaseMemory()
        cleanInstance()

        //Open your local db as the input stream
        val myInput = FileInputStream(dbFileLocation)

        // Path to the just created empty db
        val outFileName =
            getContext().getDatabasePath(Statics.DATABASE_NAME).toString()
        val file = File(outFileName)
        if (file.exists()) {
            Log.d(this::class.java.simpleName, "Eliminando base de datos antigua: $outFileName")
            SQLiteDatabase.deleteDatabase(file)
        }

        Log.d(
            this::class.java.simpleName,
            "${
                getContext().getString(R.string.origin)
            }: ${dbFileLocation!!.absolutePath}"
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
                makeText(
                    parentView!!,
                    getContext().getString(R.string.the_creation_date_of_the_db_on_the_server_is_invalid),
                    SnackbarType.ERROR
                )
                return false
            }

            return true
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }
        return false
    }
}