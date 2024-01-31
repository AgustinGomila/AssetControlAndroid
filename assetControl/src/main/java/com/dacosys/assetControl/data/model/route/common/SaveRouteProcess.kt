package com.dacosys.assetControl.data.model.route.common

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentDbHelper
import com.dacosys.assetControl.data.model.common.SaveProgress
import com.dacosys.assetControl.data.model.route.RouteProcess
import com.dacosys.assetControl.data.model.route.RouteProcessContent
import com.dacosys.assetControl.data.model.route.RouteProcessStatus
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import kotlinx.coroutines.*

class SaveRouteProcess {
    private var allRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList()
    private lateinit var routeProcess: RouteProcess
    private var onSaveProgress: (SaveProgress) -> Unit = {}
    private var onSyncProgress: (SyncProgress) -> Unit = {}

    fun addParams(
        routeProcess: RouteProcess,
        allRouteProcessContent: ArrayList<RouteProcessContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
        onSyncProgress: (SyncProgress) -> Unit = {},
    ) {
        this.routeProcess = routeProcess
        this.allRouteProcessContent = allRouteProcessContent
        this.onSaveProgress = onSaveProgress
        this.onSyncProgress = onSyncProgress
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    fun execute() {
        scope.launch { doInBackground() }
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground() {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }

        if (result) {
            routeProcess.completed = true
            if (routeProcess.saveChanges()) {
                if (autoSend()) {
                    SyncUpload(registryType = SyncRegistryType.RouteProcess,
                        onSyncTaskProgress = { onSyncProgress.invoke(it) })
                } else {
                    onSyncProgress.invoke(
                        SyncProgress(progressStatus = ProgressStatus.bigFinished)
                    )
                }
            } else {
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = getContext().getString(R.string.error_saving_route),
                        taskStatus = ProgressStatus.crashed.id,
                    )
                )
            }
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getWritableDb()

        try {
            val total = allRouteProcessContent.size

            onSaveProgress.invoke(
                SaveProgress(
                    msg = getContext().getString(R.string.saving_route_process),
                    taskStatus = ProgressStatus.starting.id,
                    progress = 0,
                    total = total
                )
            )

            val rpcDbHelper = RouteProcessContentDbHelper()

            ///// Comienzo de una transacción /////
            db.beginTransaction()

            for ((index, rpc) in allRouteProcessContent.withIndex()) {
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = getContext().getString(R.string.saving_),
                        taskStatus = ProgressStatus.running.id,
                        progress = index,
                        total = total
                    )
                )

                var statusId = rpc.routeProcessStatusId
                if (rpc.routeProcessStatusId == RouteProcessStatus.unknown.id) {
                    statusId = RouteProcessStatus.notProcessed.id
                }

                if (rpc.routeProcessId == routeProcess.collectorRouteProcessId) {
                    rpc.routeProcessStatusId = statusId
                    rpcDbHelper.updateStatusNew(rpc)
                } else {
                    rpcDbHelper.insert(
                        routeProcess.collectorRouteProcessId,
                        rpc.dataCollectionRuleId,
                        rpc.level,
                        rpc.position,
                        statusId,
                        null,
                        true
                    )
                }
            }

            db.setTransactionSuccessful()

            onSaveProgress.invoke(
                SaveProgress(taskStatus = ProgressStatus.finished.id)
            )
            return@withContext true
        } catch (ex: Exception) {
            ex.printStackTrace()
            onSaveProgress.invoke(
                SaveProgress(
                    msg = ex.message.toString(),
                    taskStatus = ProgressStatus.crashed.id,
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return@withContext false
        } finally {
            db.endTransaction()
        }
    }
}