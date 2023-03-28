package com.dacosys.assetControl.network.sync

import android.os.Handler
import android.os.Looper
import com.dacosys.assetControl.utils.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.Preference
import java.util.*

@Suppress("unused")
class Sync {
    companion object {
        private var timer: Timer? = null
        private val handler = Handler(Looper.getMainLooper())
        private var timerTask: TimerTask? = null

        private var syncDownload: SyncDownload? = null
        private var syncUpload: SyncUpload? = null

        //To stop timer
        fun stopTimer() {
            timerTask?.cancel()
            timerTask = null

            cancelPending()
        }

        private fun cancelPending() {
            syncUpload?.cancel()
            syncUpload = null

            syncDownload?.cancel()
            syncDownload = null
        }

        private fun cancelTimer() {
            timer?.cancel()
            timer?.purge()
            timer = null
        }

        fun startTimer(
            onSyncProgress: (SyncProgress) -> Unit = {},
            onSessionCreated: (Boolean) -> Unit = {},
        ) {
            try {
                cancelPending()
                cancelTimer()

                timer = Timer()
                val interval =
                    prefsGetInt(Preference.acSyncInterval)

                timerTask = object : TimerTask() {
                    override fun run() {
                        try {
                            handler.post {
                                if (Statics.autoSend()) {
                                    syncUpload = SyncUpload(onSyncTaskProgress = onSyncProgress)
                                }

                                syncDownload = SyncDownload(
                                    onSyncTaskProgress = onSyncProgress,
                                    onSessionCreated = onSessionCreated
                                )
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