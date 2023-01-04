package com.dacosys.assetControl.network.download

import android.content.Context
import android.os.PowerManager
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.network.download.DownloadStatus.*
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.System.getProperty
import java.net.HttpURLConnection
import java.net.URL

class DownloadFile(
    private var urlDestination: UrlDestParam,
    private var fileType: FileType,
    private var onDownloadTask: (DownloadTask) -> Unit = {},
) {
    private var mWakeLock: PowerManager.WakeLock? = null

    private fun postExecute(result: Boolean): Boolean {
        mWakeLock?.release()

        if (result) {
            onDownloadTask.invoke(DownloadTask(
                msg = "${
                    getContext().getString(R.string.download_ok)
                }: $fileType",
                fileType = fileType,
                downloadStatus = FINISHED
            ))
        } else {
            onDownloadTask.invoke(DownloadTask(
                msg = "${
                    getContext().getString(R.string.download_error)
                }: $fileType",
                fileType = fileType,
                downloadStatus = CRASHED
            ))
        }

        return result
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(): Boolean {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        return@withContext getDownloadTaskResult()
    }

    private fun getDownloadTaskResult(): Boolean {
        onDownloadTask.invoke(DownloadTask(
            msg = getContext().getString(R.string.starting_download),
            fileType = fileType,
            downloadStatus = STARTING
        ))

        val destination = urlDestination.destination
        val urlStr = urlDestination.url

        if (destination.exists()) {
            onDownloadTask.invoke(DownloadTask(
                msg = "${
                    getContext().getString(R.string.destination_already_exists)
                }: $destination",
                fileType = fileType,
                downloadStatus = INFO,
            ))

            // Eliminar destino
            destination.delete()
        }

        Log.d(this.javaClass.simpleName, "${
            getContext().getString(R.string.destination)
        }: $destination${getProperty("line.separator")}URL: $urlStr")

        var input: InputStream? = null
        var output: OutputStream? = null

        var connection: HttpURLConnection? = null

        try {
            val url = URL(urlStr)

            Log.d(this.javaClass.simpleName, "${
                getContext().getString(R.string.opening_connection)
            }: $urlStr")

            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                onDownloadTask.invoke(DownloadTask(
                    msg = "${
                        getContext().getString(R.string.error_connecting_to)
                    } $urlStr: Server returned HTTP ${connection.responseCode} ${connection.responseMessage}",
                    fileType = fileType,
                    downloadStatus = CRASHED,
                ))
                return false
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            val fileLength = connection.contentLength
            Log.d(this.javaClass.simpleName, "${
                getContext().getString(R.string.file_length)
            }: $fileLength")

            // Crear un nuevo archivo
            if (destination.exists()) {
                destination.delete()
            }
            destination.createNewFile()

            // download the file
            input = connection.inputStream
            output = FileOutputStream(destination)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            do {
                count = input.read(data)
                if (count == -1) {
                    break
                }

                // allow canceling with back button
                if (deferred?.isCancelled == true) {
                    onDownloadTask.invoke(DownloadTask(
                        msg = getContext()
                            .getString(R.string.download_canceled),
                        fileType = fileType,
                        downloadStatus = CANCELED,
                    ))
                    input.close()
                    return false
                }

                total += count.toLong()
                // publishing the progress....

                if (fileLength > 0) {
                    // only if total length is known
                    onDownloadTask.invoke(DownloadTask(
                        msg = getContext().getString(R.string.downloading_),
                        fileType = fileType,
                        downloadStatus = DOWNLOADING,
                        progress = (total * 100 / fileLength).toInt(),
                        bytesCompleted = total,
                        bytesTotal = fileLength.toLong()
                    ))
                }
                output.write(data, 0, count)
            } while (true)
        } catch (e: Exception) {
            onDownloadTask.invoke(DownloadTask(
                msg = "${
                    getContext().getString(R.string.exception_when_downloading)
                }: ${e.message}",
                fileType = fileType,
                downloadStatus = CRASHED
            ))
            return false
        } finally {
            try {
                output?.close()
                input?.close()
            } catch (ignored: IOException) {
            }
            connection?.disconnect()
        }
        return true
    }

    init {
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        val pm = getContext()
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        mWakeLock?.acquire(3 * 60 * 1000L /*3 minutes*/)

        scope.launch {
            val result = doInBackground()
            postExecute(result)
        }
    }
}