package com.example.assetControl.data.model.common

import com.dacosys.imageControl.network.upload.UploadImagesProgress
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.enums.route.RouteProcessStatus
import com.example.assetControl.data.room.dto.route.RouteProcess
import com.example.assetControl.data.room.dto.route.RouteProcessContent
import com.example.assetControl.data.room.dto.route.RouteProcessSteps
import com.example.assetControl.data.room.repository.route.RouteProcessContentRepository
import com.example.assetControl.data.room.repository.route.RouteProcessStepsRepository
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.sync.SyncUpload
import com.example.assetControl.network.utils.Connection.Companion.autoSend
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

class SaveRouteProcess {
    private var allRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList()
    private lateinit var routeProcess: RouteProcess
    private var onSaveProgress: (SaveProgress) -> Unit = {}
    private var onSyncProgress: (SyncProgress) -> Unit = {}
    private var onUploadImageProgress: (UploadImagesProgress) -> Unit = {}

    fun addParams(
        routeProcess: RouteProcess,
        allRouteProcessContent: ArrayList<RouteProcessContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
        onSyncProgress: (SyncProgress) -> Unit = {},
        onUploadImageProgress: (UploadImagesProgress) -> Unit = {},
    ) {
        this.routeProcess = routeProcess
        this.allRouteProcessContent = allRouteProcessContent
        this.onSaveProgress = onSaveProgress
        this.onSyncProgress = onSyncProgress
        this.onUploadImageProgress = onUploadImageProgress
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
            result = deferred?.await() == true
        }

        if (result) {
            routeProcess.completed = true
            if (routeProcess.saveChanges()) {
                if (autoSend()) {
                    SyncUpload(
                        registryType = SyncRegistryType.RouteProcess,
                        onSyncTaskProgress = { onSyncProgress.invoke(it) },
                        onUploadImageProgress = { onUploadImageProgress.invoke(it) })
                } else {
                    onSyncProgress.invoke(
                        SyncProgress(progressStatus = ProgressStatus.bigFinished)
                    )
                }
            } else {
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = context.getString(R.string.error_saving_route),
                        taskStatus = ProgressStatus.crashed.id,
                    )
                )
            }
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        try {
            val total = allRouteProcessContent.size

            onSaveProgress.invoke(
                SaveProgress(
                    msg = context.getString(R.string.saving_route_process),
                    taskStatus = ProgressStatus.starting.id,
                    progress = 0,
                    total = total
                )
            )

            val contentRepository = RouteProcessContentRepository()
            val stepsRepository = RouteProcessStepsRepository()

            for ((index, rpc) in allRouteProcessContent.withIndex()) {
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = context.getString(R.string.saving_),
                        taskStatus = ProgressStatus.running.id,
                        progress = index,
                        total = total
                    )
                )

                var statusId = rpc.routeProcessStatusId
                if (rpc.routeProcessStatusId == RouteProcessStatus.unknown.id) {
                    statusId = RouteProcessStatus.notProcessed.id
                }

                val routeProcessId = routeProcess.id

                if (rpc.routeProcessId == routeProcessId) {
                    rpc.routeProcessStatusId = statusId
                    contentRepository.updateStatus(rpc)
                } else {

                    val level = rpc.level
                    val position = rpc.position
                    val dataCollectionId = rpc.dataCollectionId
                    val ruleId = rpc.dataCollectionRuleId

                    val newId = contentRepository.insert(
                        RouteProcessContent(
                            routeProcessId = routeProcessId,
                            dataCollectionRuleId = ruleId,
                            level = level,
                            position = position,
                            routeProcessStatusId = statusId,
                            dataCollectionId = null
                        )
                    )

                    // Agregar el paso a la colección de pasos
                    stepsRepository.insert(
                        RouteProcessSteps(
                            routeProcessId = routeProcessId,
                            routeProcessContentId = newId,
                            level = level,
                            position = position,
                            dataCollectionId = dataCollectionId
                        )
                    )
                }
            }

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
        }
    }
}