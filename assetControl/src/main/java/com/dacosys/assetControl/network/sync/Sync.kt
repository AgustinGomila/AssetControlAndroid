package com.dacosys.assetControl.network.sync

import android.os.Handler
import android.os.Looper
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetInt
import com.dacosys.assetControl.utils.settings.Preference
import java.util.*

@Suppress("unused")
class Sync {
    companion object {
        private var timer: Timer? = null
        private val handler = Handler(Looper.getMainLooper())
        private var timerTask: TimerTask? = null

        @get:Synchronized
        private var ticks = 0

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
            onTimerTick: (Int) -> Unit = {},
        ) {
            try {
                cancelPending()
                cancelTimer()

                timer = Timer()
                val interval = prefsGetInt(Preference.acSyncInterval)

                timerTask = object : TimerTask() {
                    override fun run() {
                        ticks++
                        onTimerTick(ticks)

                        if (ticks < interval.toLong()) return
                        goSync(onSyncProgress, onSessionCreated)
                    }

                    override fun cancel(): Boolean {
                        cancelTimer()
                        return super.cancel()
                    }
                }
                (timer ?: return).scheduleAtFixedRate(
                    timerTask,
                    100,
                    1000
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            }
        }

        fun goSync(
            onSyncProgress: (SyncProgress) -> Unit = {},
            onSessionCreated: (Boolean) -> Unit = {},
        ) {
            ticks = 0

            try {
                handler.post {
                    if (autoSend()) {
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
    }
}