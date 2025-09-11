package com.example.assetControl.data.model.common

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.route.RouteProcessStatus
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.network.utils.ProgressStatus
import com.example.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkipAll(
    private var allRouteProcessContent: ArrayList<RouteProcessContent>,
    private var onProgress: (SaveProgress) -> Unit = {},
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    init {
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(): Boolean {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() == true
        }
        return result
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        try {
            val total = allRouteProcessContent.size
            onProgress.invoke(
                SaveProgress(
                    msg = context
                        .getString(R.string.skipping_remaining_steps),
                    taskStatus = ProgressStatus.starting.id,
                    progress = 0,
                    total = total
                )
            )

            for ((index, rpc) in allRouteProcessContent.withIndex()) {
                onProgress.invoke(
                    SaveProgress(
                        msg = context
                            .getString(R.string.skipping_remaining_steps),
                        taskStatus = ProgressStatus.running.id,
                        progress = index,
                        total = total
                    )
                )

                if (rpc.routeProcessStatusId != RouteProcessStatus.processed.id) {
                    if (!updateStatus(rpc = rpc)) {
                        onProgress.invoke(
                            SaveProgress(
                                msg = context
                                    .getString(R.string.error_updating_registered_data),
                                taskStatus = ProgressStatus.crashed.id,
                            )
                        )
                        return@withContext false
                    }
                }
            }
            onProgress.invoke(
                SaveProgress(taskStatus = ProgressStatus.finished.id)
            )
            return@withContext true
        } catch (ex: Exception) {
            ex.printStackTrace()
            onProgress.invoke(
                SaveProgress(
                    msg = ex.message.toString(),
                    taskStatus = ProgressStatus.crashed.id,
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return@withContext false
        }
    }

    private fun updateStatus(rpc: RouteProcessContent): Boolean {
        rpc.routeProcessStatusId = RouteProcessStatus.skipped.id

        // Actualizar la base de datos
        return rpc.saveChanges()
    }
}