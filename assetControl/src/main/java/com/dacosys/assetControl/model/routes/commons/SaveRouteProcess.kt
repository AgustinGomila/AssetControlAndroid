package com.dacosys.assetControl.model.routes.commons

import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcessStatus.`object`.RouteProcessStatus
import com.dacosys.assetControl.sync.functions.ProgressStatus
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class SaveRouteProcess {
    interface SaveRouteProcessListener {
        // Define data you like to return from AysncTask
        fun onSaveRouteProcessProgress(
            msg: String,
            taskStatus: Int,
            progress: Int? = null,
            total: Int? = null,
        )
    }

    private var weakRef: WeakReference<SaveRouteProcessListener>? = null
    private var listener: SaveRouteProcessListener?
        get() {
            return weakRef?.get()
        }
        set(value) {
            weakRef = if (value != null) WeakReference(value) else null
        }

    private var allRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList()
    private var collectorRouteProcessId: Long? = null

    private fun preExecute() {
        // TODO: JotterListener.lockScanner(this, true)
    }

    private fun postExecute(result: Boolean): Boolean {
        // TODO: JotterListener.lockScanner(this, false)
        return result
    }

    fun addParams(
        callback: SaveRouteProcessListener,
        collectorRouteProcessId: Long,
        allRouteProcessContent: ArrayList<RouteProcessContent>,
    ) {
        this.listener = callback
        this.allRouteProcessContent = allRouteProcessContent
        this.collectorRouteProcessId = collectorRouteProcessId
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

            val rpcDbHelper = RouteProcessContentDbHelper()

            if (listener != null)
                listener!!.onSaveRouteProcessProgress(
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.saving_route_process),
                    taskStatus = ProgressStatus.starting.id,
                    progress = 0,
                    total = total
                )

            for ((index, rpc) in allRouteProcessContent.withIndex()) {
                if (listener != null) {
                    listener!!.onSaveRouteProcessProgress(
                        msg = Statics.AssetControl.getContext().getString(R.string.saving_),
                        taskStatus = ProgressStatus.running.id,
                        progress = index,
                        total = total
                    )
                }

                var statusId = rpc.routeProcessStatusId
                if (rpc.routeProcessStatusId == RouteProcessStatus.unknown.id) {
                    statusId = RouteProcessStatus.notProcessed.id
                }

                if (rpc.routeProcessId == collectorRouteProcessId) {
                    rpc.routeProcessStatusId = statusId
                    rpcDbHelper.updateStatus(rpc)
                } else {
                    rpcDbHelper.insert(
                        collectorRouteProcessId!!,
                        rpc.dataCollectionRuleId,
                        rpc.level,
                        rpc.position,
                        statusId,
                        null,
                        true
                    )
                }
            }

            listener?.onSaveRouteProcessProgress(
                msg = "",
                taskStatus = ProgressStatus.finished.id,
                progress = 0,
                total = 0
            )
            return@withContext true
        } catch (ex: Exception) {
            ex.printStackTrace()
            listener?.onSaveRouteProcessProgress(
                msg = ex.message.toString(),
                taskStatus = ProgressStatus.crashed.id,
                progress = 0,
                total = 0
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return@withContext false
        }
    }
}