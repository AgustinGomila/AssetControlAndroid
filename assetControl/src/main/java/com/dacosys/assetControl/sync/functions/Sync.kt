package com.dacosys.assetControl.sync.functions

import android.os.Handler
import android.os.Looper
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import java.lang.ref.WeakReference
import java.util.*

@Suppress("unused")
class Sync {
    companion object {
        private var timer: Timer? = null
        private val handler = Handler(Looper.getMainLooper())
        private var timerTask: TimerTask? = null

        interface SyncTaskProgress {
            // Define data you like to return from AysncTask
            fun onSyncTaskProgress(
                totalTask: Int,
                completedTask: Int,
                msg: String,
                registryType: SyncRegistryType?,
                progressStatus: ProgressStatus,
            )
        }

        //To stop timer
        fun stopTimer() {
            timerTask?.cancel()
            timerTask = null
        }

        private fun cancelTimer() {
            timer?.cancel()
            timer?.purge()
            timer = null
        }

        fun startTimer(
            callback: WeakReference<SyncTaskProgress>,
            callback2: WeakReference<Statics.SessionCreated>,
        ) {
            try {
                cancelTimer()
                timer = Timer()
                val interval = Statics.prefsGetInt(Preference.acSyncInterval)

                timerTask = object : TimerTask() {
                    override fun run() {
                        try {
                            if (Statics.autoSend()) {
                                handler.post {
                                    val syncUpload = SyncUpload()
                                    syncUpload.addParams(callback)
                                    syncUpload.execute()

                                    val syncDownload = SyncDownload()
                                    syncDownload.addParams(callback, callback2)
                                    syncDownload.execute()
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                        }
                    }

                    override fun cancel(): Boolean {
                        cancelTimer()
                        return super.cancel()
                    }
                }
                (timer ?: return).scheduleAtFixedRate(
                    timerTask,
                    interval.toLong(),
                    interval.toLong() * 1000
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
        }
    }
}