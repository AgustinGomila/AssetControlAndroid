package com.dacosys.assetControl.network.sync

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.enums.review.AssetReviewContentStatus
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.category.ItemCategoryRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionContentRepository
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseRepository
import com.dacosys.assetControl.data.room.repository.maintenance.AssetMaintenanceRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementContentRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.dacosys.assetControl.data.room.repository.review.AssetReviewRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessContentRepository
import com.dacosys.assetControl.data.room.repository.route.RouteProcessRepository
import com.dacosys.assetControl.data.room.repository.user.UserRepository
import com.dacosys.assetControl.data.room.repository.user.UserWarehouseAreaRepository
import com.dacosys.assetControl.data.webservice.asset.AssetObject
import com.dacosys.assetControl.data.webservice.asset.AssetWs
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject
import com.dacosys.assetControl.data.webservice.category.ItemCategoryWs
import com.dacosys.assetControl.data.webservice.common.Webservice.Companion.getWebservice
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionContentObject
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionObject
import com.dacosys.assetControl.data.webservice.dataCollection.DataCollectionWs
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaWs
import com.dacosys.assetControl.data.webservice.location.WarehouseObject
import com.dacosys.assetControl.data.webservice.location.WarehouseWs
import com.dacosys.assetControl.data.webservice.maintenance.AssetMaintenanceLogObject
import com.dacosys.assetControl.data.webservice.maintenance.AssetMaintenanceObject
import com.dacosys.assetControl.data.webservice.maintenance.AssetMaintenanceWs
import com.dacosys.assetControl.data.webservice.movement.WarehouseMovementContentObject
import com.dacosys.assetControl.data.webservice.movement.WarehouseMovementObject
import com.dacosys.assetControl.data.webservice.movement.WarehouseMovementWs
import com.dacosys.assetControl.data.webservice.review.AssetReviewContentObject
import com.dacosys.assetControl.data.webservice.review.AssetReviewObject
import com.dacosys.assetControl.data.webservice.review.AssetReviewWs
import com.dacosys.assetControl.data.webservice.route.RouteProcessContentObject
import com.dacosys.assetControl.data.webservice.route.RouteProcessObject
import com.dacosys.assetControl.data.webservice.route.RouteProcessWs
import com.dacosys.assetControl.data.webservice.user.UserObject
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaObject
import com.dacosys.assetControl.data.webservice.user.UserWarehouseAreaWs
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.settings.config.Preference
import com.dacosys.assetControl.utils.settings.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.preferences.Repository.Companion.connectionTimeout
import com.dacosys.imageControl.network.common.ProgressStatus.CREATOR.finishStates
import com.dacosys.imageControl.network.upload.SendPending
import com.dacosys.imageControl.network.upload.UpdateIdImages
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import kotlinx.coroutines.*

class SyncUpload(
    private var registryType: SyncRegistryType? = null,
    private var onSyncTaskProgress: (SyncProgress) -> Unit = {},
    private var onUploadImageProgress: (UploadImagesProgress) -> Unit = {},
) {
    private val tag = this::class.java.simpleName

    private var registryOnProcess: ArrayList<SyncRegistryType> = ArrayList()

    private var userId: Long = 0

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            if (it.status == ProgressStatus.finished) {
                val userId = getUserId()
                if (userId == null) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().resources.getString(R.string.invalid_user),
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    return
                }

                scope.launch {
                    doInBackground(userId) { onSyncFinish(it) }
                }
            } else if (it.status == ProgressStatus.crashed || it.status == ProgressStatus.canceled) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = it.msg,
                        progressStatus = it.status
                    )
                )
            }
        }
        GetMySqlDate(getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun onSyncFinish(result: Boolean) {
        if (result) {
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.synchronization_finished),
                    progressStatus = ProgressStatus.bigFinished
                )
            )
        } else {
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.synchronization_failed),
                    progressStatus = ProgressStatus.crashed
                )
            )
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(userId: Long, onResult: (Boolean) -> Unit) {
        this.userId = userId
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() == true
        }
        onResult.invoke(result)
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        onSyncTaskProgress.invoke(
            SyncProgress(
                msg = getContext().getString(R.string.synchronization_starting),
                progressStatus = ProgressStatus.bigStarting
            )
        )

        // El orden está dado por la dependencia de los ID
        // en las subsiguientes tablas
        if (registryType == null) {
            itemCategory()
            warehouse()
            warehouseArea()
            asset()
            assetReview()
            warehouseMovement()
            dataCollection()
            routeProcess()

            if (prefsGetBoolean(Preference.useAssetControlManteinance)) {
                assetMaintenance()
            }
        } else {
            when (registryType) {
                SyncRegistryType.ItemCategory -> itemCategory()
                SyncRegistryType.Warehouse -> warehouse()
                SyncRegistryType.WarehouseArea -> {
                    warehouse()
                    warehouseArea()
                }

                SyncRegistryType.Asset -> {
                    itemCategory()
                    warehouse()
                    warehouseArea()
                    asset()
                }

                SyncRegistryType.AssetReview -> {
                    itemCategory()
                    warehouse()
                    warehouseArea()
                    asset()
                    assetReview()
                    warehouseMovement()
                }

                SyncRegistryType.WarehouseMovement -> {
                    itemCategory()
                    warehouse()
                    warehouseArea()
                    asset()
                    warehouseMovement()
                }

                SyncRegistryType.AssetMaintenance -> {
                    if (prefsGetBoolean(Preference.useAssetControlManteinance)) {
                        itemCategory()
                        warehouse()
                        warehouseArea()
                        asset()
                        assetMaintenance()
                    }
                }

                SyncRegistryType.DataCollection,
                SyncRegistryType.RouteProcess,
                    -> {
                    itemCategory()
                    warehouse()
                    warehouseArea()
                    asset()
                    dataCollection()
                    routeProcess()
                }
            }
        }

        // Enviar imágenes
        sendPendingImages()

        // Eliminar datos enviados
        removeOldData()
        return@withContext true
    }

    @get:Synchronized
    private var isProcessDone = false

    @Synchronized
    private fun getProcessState(): Boolean {
        return isProcessDone
    }

    @Synchronized
    private fun setProcessState(state: Boolean) {
        isProcessDone = state
    }


    private fun sendPendingImages() {
        setProcessState(false)

        var startTime = 0L
        var lastState = UploadImagesProgress()

        SendPending(context = getContext()) {
            // Still running
            startTime = System.currentTimeMillis()
            lastState = it

            val res = it.result

            if (BuildConfig.DEBUG) {
                Log.i(tag, "Progreso de subida: ${res.description}")
            }

            if (res !in finishStates()) {
                // Reportamos solo los progresos
                onUploadImageProgress.invoke(it)
            }

            setProcessState(res in finishStates())
        }

        while (!getProcessState()) {
            if (System.currentTimeMillis() - startTime == (connectionTimeout * 1000).toLong()) {
                setProcessState(true)
            }
        }

        onUploadImageProgress.invoke(lastState)
    }

    private fun removeOldData() {
        RouteProcessRepository().deleteTransferred()
        DataCollectionRepository().deleteOrphansTransferred()
        AssetReviewRepository().deleteTransferred()
        WarehouseMovementRepository().deleteTransferred()
    }

    private fun assetReview() {
        val registryType = SyncRegistryType.AssetReview
        registryOnProcess.add(registryType)

        val arWs = AssetReviewWs()

        val reviewRepository = AssetReviewRepository()
        val contentRepository = AssetReviewContentRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_asset_review_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val arAl = reviewRepository.selectByCompleted(userId)
            if (arAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_asset_review_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val totalTask = arAl.size
            for ((currentTask, ar) in arAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = ar.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_asset_reviews),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_asset_review_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                val arcAl = contentRepository.selectByAssetReviewId(ar.id)
                if (arcAl.isEmpty()) {
                    reviewRepository.deleteById(ar.id)
                    continue
                }

                val arcObjArray = ArrayList<AssetReviewContentObject>()
                for (arc in arcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    // Los activos extraviados no deben aparecer en la revisión,
                    // pero el estado de los activos sí cambia al estado extraviado.
                    if (arc.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                        continue
                    }

                    val x = AssetReviewContentObject(arc)

                    arcObjArray.add(x)
                }

                val arObj = AssetReviewObject(ar)

                val arId = arWs.assetReviewAdd(
                    assetReview = arObj,
                    assetReviewContent = arcObjArray
                )

                if (arId > 0) {
                    reviewRepository.updateTransferred(arId, ar.id)
                    contentRepository.updateAssetReviewId(arId, ar.id)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.assetReview.id.toLong(),
                        newObjectId1 = arId,
                        localObjectId1 = ar.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_asset_reviews)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_asset_reviews),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )

            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_asset_reviews)
                        else -> getContext().getString(R.string.asset_review_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun warehouseMovement() {
        val registryType = SyncRegistryType.WarehouseMovement
        registryOnProcess.add(registryType)

        val wmWs = WarehouseMovementWs()

        val movementRepository = WarehouseMovementRepository()
        val contentRepository = WarehouseMovementContentRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_movement_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val wmAl = movementRepository.selectByNoTransferred()
            if (wmAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_movement_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val totalTask = wmAl.size
            for ((currentTask, wm) in wmAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = wm.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_movements),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_movement_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                val wmcAl = contentRepository.selectByWarehouseMovementId(wm.id)
                if (wmcAl.isEmpty()) {
                    movementRepository.deleteById(wm.id)
                    continue
                }

                val wmcObjArray = ArrayList<WarehouseMovementContentObject>()
                for (wmc in wmcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    val x = WarehouseMovementContentObject(wmc)

                    wmcObjArray.add(x)
                }

                val wmObj = WarehouseMovementObject(wm)

                val wmId = wmWs.warehouseMovementAdd(
                    warehouseMovement = wmObj,
                    warehouseMovementContent = wmcObjArray
                )

                if (wmId > 0) {
                    movementRepository.updateTransferred(wmId, wm.id)
                    contentRepository.updateMovementId(wmId, wm.id)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.warehouseMovement.id.toLong(),
                        newObjectId1 = wmId,
                        localObjectId1 = wm.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_movements)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_movements),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_movements)
                        else -> getContext().getString(R.string.movement_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun asset() {
        val registryType = SyncRegistryType.Asset
        registryOnProcess.add(registryType)

        val assetWs = AssetWs()

        val assetRepository = AssetRepository()
        val reviewContentRepository = AssetReviewContentRepository()
        val movementContentRepository = WarehouseMovementContentRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_asset_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val aAl = assetRepository.selectNoTransferred()
            if (aAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_asset_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val allRealId: ArrayList<Long> = ArrayList()
            val totalTask = aAl.size
            for ((currentTask, a) in aAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = a.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_assets),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_asset_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                var realAssetId: Long
                if (a.id > 0) {
                    realAssetId = assetWs.assetCollectorModify(
                        userId,
                        com.dacosys.assetControl.data.webservice.asset.AssetCollectorObject(a)
                    )
                } else {
                    realAssetId = assetWs.assetAdd(userId, AssetObject(a))
                    if (realAssetId > 0) {

                        // Actualizar el propio activo
                        assetRepository.updateId(realAssetId, a.id)
                        val realAsset = assetRepository.selectById(realAssetId)
                        if (realAsset != null) {
                            // Actualizar los movimientos asociados
                            movementContentRepository.updateAssetId(realAssetId, a.id)

                            // Actualizar las revisiones asociadas
                            reviewContentRepository.updateAssetId(realAssetId, a.id)
                        }
                    }
                }

                if (realAssetId > 0) {
                    allRealId.add(realAssetId)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.asset.id.toLong(),
                        newObjectId1 = realAssetId,
                        localObjectId1 = a.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_assets)
                    )
                }
            }

            // Actualizamos todos los activos en una sola consulta.
            if (!error) {
                error = !assetRepository.updateTransferred(allRealId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_assets),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_assets)
                        else -> getContext().getString(R.string.asset_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun warehouseArea() {
        val registryType = SyncRegistryType.WarehouseArea
        registryOnProcess.add(registryType)

        val warehouseAreaWs = WarehouseAreaWs()

        val areaRepository = WarehouseAreaRepository()
        val assetRepository = AssetRepository()
        val userWarehouseAreaRepository = UserWarehouseAreaRepository()
        val reviewRepository = AssetReviewRepository()
        val movementRepository = WarehouseMovementRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_warehouse_area_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val waAl = areaRepository.selectNoTransferred()
            if (waAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_warehouse_area_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val allRealId: ArrayList<Long> = ArrayList()
            val totalTask = waAl.size
            for ((currentTask, wa) in waAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = wa.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_warehouse_areas),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_warehouse_area_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                var realWarehouseAreaId: Long
                if (wa.id > 0) {
                    realWarehouseAreaId = warehouseAreaWs.warehouseAreaModify(
                        userId, WarehouseAreaObject(wa)
                    )
                } else {
                    realWarehouseAreaId = warehouseAreaWs.warehouseAreaAdd(
                        userId, WarehouseAreaObject(wa)
                    )

                    if (realWarehouseAreaId > 0) {
                        // Actualizar la propia área
                        areaRepository.updateWarehouseAreaId(realWarehouseAreaId, wa.id)

                        // Actualizar los activos fijos asociados
                        assetRepository.updateWarehouseAreaId(realWarehouseAreaId, wa.id)

                        // Actualizar las áreas de usuario
                        userWarehouseAreaRepository.updateWarehouseAreaId(realWarehouseAreaId, wa.id)

                        // Actualizar las revisiones asociadas
                        reviewRepository.updateWarehouseAreaId(realWarehouseAreaId, wa.id)

                        // Actualizar los movimientos asociados
                        movementRepository.updateOriginWarehouseAreaId(realWarehouseAreaId, wa.id)
                        movementRepository.updateDestinationWarehouseAreaId(realWarehouseAreaId, wa.id)

                        // Enviar las áreas del usuario
                        val user = UserRepository().selectById(userId) ?: return
                        val uObj = UserObject().getByUser(user)

                        val uwaObj = UserWarehouseAreaObject()
                        uwaObj.warehouse_area_id = realWarehouseAreaId
                        uwaObj.user_id = userId
                        uwaObj.check = 1
                        uwaObj.count = 1
                        uwaObj.move = 1
                        uwaObj.see = 1

                        UserWarehouseAreaWs().userWarehouseAreaAdd(
                            userId, uObj, arrayListOf(uwaObj)
                        )
                    }
                }

                if (realWarehouseAreaId > 0) {
                    allRealId.add(realWarehouseAreaId)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.warehouseArea.id.toLong(),
                        newObjectId1 = realWarehouseAreaId,
                        localObjectId1 = wa.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas)
                    )
                }
            }

            // Actualizamos todos las áreas en una sola consulta.
            if (!error) {
                error = !areaRepository.updateTransferred(allRealId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas)
                        else -> getContext().getString(R.string.warehouse_area_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun warehouse() {
        val registryType = SyncRegistryType.Warehouse
        registryOnProcess.add(registryType)

        val warehouseWs = WarehouseWs()

        val wRepository = WarehouseRepository()
        val waRepository = WarehouseAreaRepository()
        val aRepository = AssetRepository()
        val arRepository = AssetReviewRepository()
        val wmRepository = WarehouseMovementRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_warehouse_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val wAl = wRepository.selectNoTransferred()
            if (wAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_warehouse_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val allRealId: ArrayList<Long> = ArrayList()
            val totalTask = wAl.size
            for ((currentTask, w) in wAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = w.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_warehouses),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_warehouse_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                var realWarehouseId: Long
                if (w.id > 0) {
                    realWarehouseId = warehouseWs.warehouseModify(
                        userId, WarehouseObject(w)
                    )
                } else {
                    realWarehouseId = warehouseWs.warehouseAdd(
                        userId, WarehouseObject(w)
                    )

                    if (realWarehouseId > 0) {
                        // Actualizar el propio depósito
                        wRepository.updateWarehouseId(realWarehouseId, w.id)

                        // Actualizar las áreas asociadas
                        waRepository.updateWarehouseId(realWarehouseId, w.id)

                        // Actualizar los activos fijos asociados
                        aRepository.updateWarehouseId(realWarehouseId, w.id)

                        // Actualizar las revisiones asociadas
                        arRepository.updateWarehouseId(realWarehouseId, w.id)

                        // Actualizar los movimientos asociados
                        wmRepository.updateOriginWarehouseId(realWarehouseId, w.id)
                        wmRepository.updateDestinationWarehouseId(realWarehouseId, w.id)
                    }
                }

                if (realWarehouseId > 0) {
                    allRealId.add(realWarehouseId)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.warehouse.id.toLong(),
                        newObjectId1 = realWarehouseId,
                        localObjectId1 = w.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_warehouses)
                    )
                }
            }

            // Actualizamos todos las áreas en una sola consulta.
            if (!error) {
                error = !wRepository.updateTransferred(allRealId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_warehouses),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_warehouses)
                        else -> getContext().getString(R.string.warehouse_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun itemCategory() {
        val registryType = SyncRegistryType.ItemCategory
        registryOnProcess.add(registryType)

        val itemCategoryWs = ItemCategoryWs()

        val categoryRepository = ItemCategoryRepository()
        val assetRepository = AssetRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_category_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val icAl = categoryRepository.selectNoTransferred()
            if (icAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_category_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val allRealId: ArrayList<Long> = ArrayList()
            val totalTask = icAl.size
            for ((currentTask, ic) in icAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = ic.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_categories),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_category_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                var realItemCategoryId: Long
                if (ic.id > 0) {
                    realItemCategoryId = itemCategoryWs.itemCategoryModify(
                        userId, ItemCategoryObject(ic)
                    )
                } else {
                    realItemCategoryId = itemCategoryWs.itemCategoryAdd(
                        userId, ItemCategoryObject(ic)
                    )

                    if (realItemCategoryId > 0) {
                        // Actualizar la propia categoría
                        categoryRepository.updateId(realItemCategoryId, ic.id)

                        // Actualizar los activos fijos asociados
                        assetRepository.updateItemCategoryId(realItemCategoryId, ic.id)
                    }
                }

                if (realItemCategoryId > 0) {
                    allRealId.add(realItemCategoryId)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.itemCategory.id.toLong(),
                        newObjectId1 = realItemCategoryId,
                        localObjectId1 = ic.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_categories)
                    )
                }
            }

            // Actualizamos todos las áreas en una sola consulta.
            if (!error) {
                error = !categoryRepository.updateTransferred(allRealId)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_categories),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_categories)
                        else -> getContext().getString(R.string.category_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun dataCollection() {
        val registryType = SyncRegistryType.DataCollection
        registryOnProcess.add(registryType)

        val dcWs = DataCollectionWs()

        val collectionRepository = DataCollectionRepository()
        val contentRepository = DataCollectionContentRepository()
        val processContentRepository = RouteProcessContentRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_data_collection_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val dcAl = collectionRepository.selectByNoTransferred()
            if (dcAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_data_collection_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val totalTask = dcAl.size
            for ((currentTask, dc) in dcAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = dc.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_data_collections),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_data_collection_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                val dccAl = contentRepository.selectByDataCollectionId(dc.id)
                if (dccAl.isEmpty()) {
                    continue
                }

                val dccObjArray = ArrayList<DataCollectionContentObject>()
                for (dcc in dccAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    val x = DataCollectionContentObject(dcc)

                    dccObjArray.add(x)
                }

                val dcObj = DataCollectionObject(dc)

                val dcId = dcWs.dataCollectionAdd(
                    dataCollection = dcObj,
                    dataCollectionContent = dccObjArray
                )

                if (dcId > 0) {
                    collectionRepository.updateDataCollectionId(dcId, dc.id)
                    contentRepository.updateDataCollectionId(dcId, dc.id)
                    processContentRepository.updateDataCollectionId(dcId, dc.id)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.dataCollection.id.toLong(),
                        newObjectId1 = dcId,
                        localObjectId1 = dc.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_data_collections)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_data_collections),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_data_collections)
                        else -> getContext().getString(R.string.data_collection_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun routeProcess() {
        val registryType = SyncRegistryType.RouteProcess
        registryOnProcess.add(registryType)

        val rpWs = RouteProcessWs()

        val processRepository = RouteProcessRepository()
        val contentRepository = RouteProcessContentRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_route_process_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val rpAl = processRepository.selectByNoTransferred()
            if (rpAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_route_process_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val totalTask = rpAl.size
            for ((currentTask, rp) in rpAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = rp.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_route_process),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_route_process_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                val rpcAl = contentRepository.selectByRouteProcessId(rp.id)

                if (rpcAl.isEmpty()) {
                    continue
                }

                val rpcObjArray = ArrayList<RouteProcessContentObject>()
                for (rpc in rpcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    val tempDcId = rpc.dataCollectionId

                    // Tiene un ID negativo, por lo tanto todavía no se envió su recolección de datos.
                    if (tempDcId != null && tempDcId < 0) continue

                    rpcObjArray.add(RouteProcessContentObject(rpc))
                }

                val rpObj = RouteProcessObject(rp)

                val rpId = rpWs.routeProcessAdd(
                    routeProcess = rpObj,
                    routeProcessContent = rpcObjArray
                )

                if (rpId > 0) {
                    processRepository.updateRouteProcessId(rpId, rp.id)
                    contentRepository.updateRouteProcessId(rpId, rp.id)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.routeProcess.id.toLong(),
                        newObjectId1 = rpId,
                        localObjectId1 = rp.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_route_process)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_route_process),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_route_process)
                        else -> getContext().getString(R.string.route_process_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    private fun assetMaintenance() {
        val registryType = SyncRegistryType.AssetMaintenance
        registryOnProcess.add(registryType)

        val amWs = AssetMaintenanceWs()

        val maintenanceRepository = AssetMaintenanceRepository()

        var error = false

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        msg = getContext().getString(R.string.canceling_maintenance_type_synchronization),
                        registryType = registryType,
                        progressStatus = ProgressStatus.canceled
                    )
                )
                return
            }

            val amAl = maintenanceRepository.selectNoTransferred()
            if (amAl.isEmpty()) {
                return
            }

            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.starting_maintenance_type_synchronization),
                    registryType = registryType,
                    progressStatus = ProgressStatus.starting
                )
            )

            val totalTask = amAl.size
            for ((currentTask, am) in amAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(
                        totalTask = totalTask,
                        completedTask = currentTask,
                        uniqueId = am.id.toString(),
                        msg = getContext().getString(R.string.synchronizing_maintenance_types),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(
                        SyncProgress(
                            msg = getContext().getString(R.string.canceling_maintenance_type_synchronization),
                            registryType = registryType,
                            progressStatus = ProgressStatus.canceled
                        )
                    )
                    break
                }

                val amObj = AssetMaintenanceObject(am, userId)

                val amLogObj = AssetMaintenanceLogObject(am, userId)

                val amId =
                    if (am.id <= 0L) {
                        amWs.assetMaintenanceAdd(
                            userId = userId,
                            assetMaintenance = amObj,
                            assetMaintenanceLog = amLogObj
                        )
                    } else {
                        amWs.assetMaintenanceModify(
                            userId = userId,
                            assetMaintenance = amObj,
                            assetMaintenanceLog = amLogObj
                        )
                    }

                if (amId > 0) {
                    maintenanceRepository.updateTransferred(amId)

                    // Actualizar los Ids del colector con los Ids reales
                    UpdateIdImages(
                        context = getContext(),
                        programObjectId = Table.assetMaintenance.id.toLong(),
                        newObjectId1 = amId,
                        localObjectId1 = am.id,
                        onUploadProgress = {
                            if (it.result !in finishStates()) {
                                // Reportamos solo los progresos
                                onUploadImageProgress.invoke(it)
                            }
                        }
                    ).execute()
                } else {
                    error = true
                    Log.d(
                        tag,
                        getContext().getString(R.string.failed_to_synchronize_the_maintenance_types)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = getContext().getString(R.string.failed_to_synchronize_the_maintenance_types),
                    registryType = registryType,
                    progressStatus = ProgressStatus.crashed
                )
            )
            ErrorLog.writeLog(null, tag, ex)
            return
        } finally {
            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(
                SyncProgress(
                    msg = when {
                        error -> getContext().getString(R.string.failed_to_synchronize_the_maintenance_types)
                        else -> getContext().getString(R.string.maintenance_type_synchronization_completed)
                    },
                    registryType = registryType,
                    progressStatus = when {
                        error -> ProgressStatus.crashed
                        else -> ProgressStatus.success
                    }
                )
            )
        }
    }

    init {
        if (!isLogged()) {
            onSyncFinish(true)
        } else {
            if (registryOnProcess.isNotEmpty()) {
                onSyncTaskProgress.invoke(
                    SyncProgress(progressStatus = ProgressStatus.canceled)
                )
            } else {
                checkConnection()
            }
        }
    }
}