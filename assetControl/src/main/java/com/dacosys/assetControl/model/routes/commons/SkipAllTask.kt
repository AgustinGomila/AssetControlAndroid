package com.dacosys.assetControl.model.routes.commons

import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.routes.routeProcessStatus.`object`.RouteProcessStatus
import com.dacosys.assetControl.sync.functions.ProgressStatus
import kotlinx.coroutines.*

class SkipAll {
    interface SkipAllListener {
        // Define data you like to return from AysncTask
        fun onSkipAllProgress(
            msg: String,
            taskStatus: Int,
            progress: Int? = null,
            total: Int? = null,
        )
    }

    var listener: SkipAllListener? = null
    private var allRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList()

    private fun preExecute() {
        // TODO: JotterListener.lockScanner(this, true)
    }

    private fun postExecute(result: Boolean): Boolean {
        // TODO: JotterListener.lockScanner(this, false)
        return result
    }

    fun addParams(
        callback: SkipAllListener,
        allRouteProcessContent: ArrayList<RouteProcessContent>,
    ) {
        this.listener = callback
        this.allRouteProcessContent = allRouteProcessContent
    }

    fun execute(): Boolean {
        preExecute()
        val result = doInBackground()
        return postExecute(result)
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
        try {
            val total = allRouteProcessContent.size
            listener?.onSkipAllProgress(
                msg = Statics.AssetControl.getContext()
                    .getString(R.string.skipping_remaining_steps),
                taskStatus = ProgressStatus.starting.id,
                progress = 0,
                total = total
            )

            for ((index, rpc) in allRouteProcessContent.withIndex()) {
                listener?.onSkipAllProgress(
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.skipping_remaining_steps),
                    taskStatus = ProgressStatus.running.id,
                    progress = index,
                    total = total
                )

                if (rpc.routeProcessStatusId != RouteProcessStatus.processed.id) {
                    if (!updateStatus(rpc = rpc)) {
                        listener?.onSkipAllProgress(
                            msg = Statics.AssetControl.getContext()
                                .getString(R.string.error_updating_registered_data),
                            taskStatus = ProgressStatus.crashed.id,
                            progress = 0,
                            total = 0
                        )
                        return@withContext false
                    }
                }
            }
            listener?.onSkipAllProgress(
                msg = "",
                taskStatus = ProgressStatus.finished.id,
                progress = 0,
                total = 0
            )
            return@withContext true
        } catch (ex: Exception) {
            ex.printStackTrace()
            listener?.onSkipAllProgress(
                msg = ex.message.toString(),
                taskStatus = ProgressStatus.crashed.id,
                progress = 0,
                total = 0
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