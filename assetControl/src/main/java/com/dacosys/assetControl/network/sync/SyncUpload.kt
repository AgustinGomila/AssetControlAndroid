package com.dacosys.assetControl.network.sync

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetCollectorObject
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetObject
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetWs
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryDbHelper
import com.dacosys.assetControl.model.assets.itemCategory.wsObject.ItemCategoryObject
import com.dacosys.assetControl.model.assets.itemCategory.wsObject.ItemCategoryWs
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.dbHelper.AssetManteinanceDbHelper
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.wsObject.AssetManteinanceLogObject
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.wsObject.AssetManteinanceObject
import com.dacosys.assetControl.model.assets.manteinances.assetMainteinance.wsObject.AssetManteinanceWs
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseDbHelper
import com.dacosys.assetControl.model.locations.warehouse.wsObject.WarehouseObject
import com.dacosys.assetControl.model.locations.warehouse.wsObject.WarehouseWs
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaDbHelper
import com.dacosys.assetControl.model.locations.warehouseArea.wsObject.WarehouseAreaObject
import com.dacosys.assetControl.model.locations.warehouseArea.wsObject.WarehouseAreaWs
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovement.wsObject.WarehouseMovementObject
import com.dacosys.assetControl.model.movements.warehouseMovement.wsObject.WarehouseMovementWs
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovementContent.wsObject.WarehouseMovementContentObject
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewDbHelper
import com.dacosys.assetControl.model.reviews.assetReview.wsObject.AssetReviewObject
import com.dacosys.assetControl.model.reviews.assetReview.wsObject.AssetReviewWs
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.reviews.assetReviewContent.wsObject.AssetReviewContentObject
import com.dacosys.assetControl.model.reviews.assetReviewContentStatus.AssetReviewContentStatus
import com.dacosys.assetControl.model.reviews.assetReviewStatus.`object`.AssetReviewStatus
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.wsObject.DataCollectionObject
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.wsObject.DataCollectionWs
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentDbHelper
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.wsObject.DataCollectionContentObject
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessDbHelper
import com.dacosys.assetControl.model.routes.routeProcess.wsObject.RouteProcessObject
import com.dacosys.assetControl.model.routes.routeProcess.wsObject.RouteProcessWs
import com.dacosys.assetControl.model.routes.routeProcessContent.dbHelper.RouteProcessContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcessContent.wsObject.RouteProcessContentObject
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.model.users.user.`object`.User
import com.dacosys.assetControl.model.users.user.wsObject.UserObject
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaDbHelper
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaObject
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaWs
import com.dacosys.assetControl.network.serverDate.GetMySqlDate
import com.dacosys.assetControl.network.serverDate.MySqlDateResult
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.Preference
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.Statics.Companion.isInitialized
import com.dacosys.imageControl.Statics.Companion.sendPendingImages
import com.dacosys.imageControl.dbHelper.DbCommands.Companion.uploadTempDocument
import com.dacosys.imageControl.main.UploadImagesProgress
import kotlinx.coroutines.*

class SyncUpload(
    private var registryType: SyncRegistryType? = null,
    private var onSyncTaskProgress: (SyncProgress) -> Unit = {},
    private var onUploadProgress: (UploadImagesProgress) -> Unit = {},
) {
    private var registryOnProcess: ArrayList<SyncRegistryType> = ArrayList()

    private fun checkConnection() {
        fun onConnectionResult(it: MySqlDateResult) {
            if (it.status == ProgressStatus.finished) {
                scope.launch {
                    doInBackground { onSyncFinish(it) }
                }
            } else if (
                it.status == ProgressStatus.crashed ||
                it.status == ProgressStatus.canceled
            ) {
                onSyncTaskProgress.invoke(SyncProgress(0,
                    0,
                    it.msg,
                    null,
                    it.status))
            }
        }
        GetMySqlDate(Statics.getWebservice()) { onConnectionResult(it) }.execute()
    }

    private fun onSyncFinish(result: Boolean) {
        if (result) {
            onSyncTaskProgress.invoke(SyncProgress(
                totalTask = 0,
                completedTask = 0,
                msg = getContext().getString(R.string.synchronization_finished),
                registryType = null,
                progressStatus = ProgressStatus.bigFinished
            ))
        } else {
            onSyncTaskProgress.invoke(SyncProgress(
                totalTask = 0,
                completedTask = 0,
                msg = getContext().getString(R.string.synchronization_failed),
                registryType = null,
                progressStatus = ProgressStatus.crashed
            ))
        }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    fun cancel() {
        scope.cancel()
    }

    private var deferred: Deferred<Boolean>? = null
    private suspend fun doInBackground(onResult: (Boolean) -> Unit) {
        var result = false
        coroutineScope {
            deferred = async { suspendFunction() }
            result = deferred?.await() ?: false
        }
        onResult.invoke(result)
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        onSyncTaskProgress.invoke(SyncProgress(
            totalTask = 0,
            completedTask = 0,
            msg = getContext().getString(R.string.synchronization_starting),
            registryType = null,
            progressStatus = ProgressStatus.bigStarting
        ))
        return@withContext getUploadTaskResult()
    }

    private fun getUploadTaskResult(): Boolean {
        // Enviar imágenes pendientes que tienen IDs reales,
        // por ejemplo: Activos, categorías, ubicaciones
        // existentes modificadas desde los CRUDs o desde el
        // buscador de activos.

        // Las imágenes con IDs locales se enviarán después de
        // obtener los IDs reales.
        if (isInitialized()) {
            sendPendingImages { onUploadProgress.invoke(it) }
        }

        // El orden está dado por la dependencia de los IDs
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

            if (Statics.prefsGetBoolean(Preference.useAssetControlManteinance)) {
                assetManteinance()
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
                SyncRegistryType.AssetManteinance -> {
                    if (Statics.prefsGetBoolean(Preference.useAssetControlManteinance)) {
                        itemCategory()
                        warehouse()
                        warehouseArea()
                        asset()
                        assetManteinance()
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

        // Eliminar datos enviados
        removeOldData()
        return true
    }

    private fun removeOldData() {
        RouteProcessDbHelper().deleteTransferred()
        DataCollectionDbHelper()
            .deleteOrphansTransferred()
        AssetReviewDbHelper().deleteTransferred()
        WarehouseMovementDbHelper().deleteTransferred()
    }

    private fun assetReview() {
        val registryType = SyncRegistryType.AssetReview
        registryOnProcess.add(registryType)

        val arWs = AssetReviewWs()

        val arDb = AssetReviewDbHelper()
        val arcDb = AssetReviewContentDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_asset_review_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val arAl = arDb.selectByCompleted()
            if (arAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_asset_review_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = arAl.size
            for ((currentTask, ar) in arAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_asset_reviews),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_asset_review_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                val arcAl = arcDb.selectByAssetReviewCollectorId(ar.collectorAssetReviewId)
                if (arcAl.size < 1) {
                    arDb.deleteById(ar.collectorAssetReviewId)
                    continue
                }

                val arcObjArray = ArrayList<AssetReviewContentObject>()
                for (arc in arcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    // Los activos extraviados no deben aparecer en la revisión,
                    // pero el estado de los activos sí cambia a extraviado.
                    if (arc.contentStatusId == AssetReviewContentStatus.notInReview.id) {
                        continue
                    }

                    val x = AssetReviewContentObject()
                    x.assetId = arc.assetId
                    x.qty = 1F
                    x.code = arc.code
                    if (arc.description.isNotEmpty()) {
                        x.description = arc.description
                    }

                    arcObjArray.add(x)
                }

                val arObj = AssetReviewObject()

                arObj.warehouseId = ar.warehouseId
                arObj.warehouseAreaId = ar.warehouseAreaId
                arObj.assetReviewId = ar.collectorAssetReviewId
                arObj.userId = ar.userId
                arObj.assetReviewDate = ar.assetReviewDate
                arObj.obs = ar.obs
                arObj.modificationDate = ar.modificationDate
                arObj.statusId = AssetReviewStatus.transferred.id

                val arId = arWs.assetReviewAdd(
                    arObj,
                    arcObjArray
                )

                if (arId > 0) {
                    arDb.updateTransferred(arId, ar.collectorAssetReviewId)

                    // Enviar imágenes si existen
                    uploadTempDocument(
                        programObjectId = Table.assetReview.tableId.toString(),
                        newObjectId1 = arId.toString(),
                        newObjectId2 = "",
                        localObjectId1 = ar.collectorAssetReviewId.toString(),
                        localObjectId2 = ""
                    ) { onUploadProgress.invoke(it) }
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_asset_reviews)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_asset_reviews),
                registryType,
                ProgressStatus.crashed
            ))

            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_asset_reviews)
                    else -> getContext().getString(R.string.asset_review_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun warehouseMovement() {
        val registryType = SyncRegistryType.WarehouseMovement
        registryOnProcess.add(registryType)

        val wmWs = WarehouseMovementWs()

        val wmDb = WarehouseMovementDbHelper()
        val wmcDb = WarehouseMovementContentDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_movement_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val wmAl = wmDb.selectByNoTransferred()
            if (wmAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_movement_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = wmAl.size
            for ((currentTask, wm) in wmAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_movements),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_movement_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                val wmcAl =
                    wmcDb.selectByCollectorWarehouseMovementId(wm.collectorWarehouseMovementId)
                if (wmcAl.size < 1) {
                    wmDb.deleteById(wm.collectorWarehouseMovementId)
                    continue
                }

                val wmcObjArray = ArrayList<WarehouseMovementContentObject>()
                for (wmc in wmcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    val x = WarehouseMovementContentObject()
                    x.assetId = wmc.assetId
                    x.qty = wmc.qty
                    x.code = wmc.code

                    wmcObjArray.add(x)
                }

                val wmObj = WarehouseMovementObject()

                wmObj.destWarehouseAreaId = wm.destWarehouseAreaId
                wmObj.destWarehouseId = wm.destWarehouseId
                wmObj.origWarehouseAreaId = wm.origWarehouseAreaId
                wmObj.origWarehouseId = wm.origWarehouseId
                wmObj.warehouseMovementId = wm.collectorWarehouseMovementId
                wmObj.userId = wm.userId
                wmObj.warehouseMovementDate = wm.warehouseMovementDate
                wmObj.obs = wm.obs

                val wmId = wmWs.warehouseMovementAdd(
                    wmObj,
                    wmcObjArray
                )

                if (wmId > 0) {
                    wmDb.updateTransferred(wmId, wm.collectorWarehouseMovementId)

                    // Enviar imágenes si existen
                    uploadTempDocument(
                        programObjectId = Table.warehouseMovement.tableId.toString(),
                        newObjectId1 = wmId.toString(),
                        newObjectId2 = "",
                        localObjectId1 = wm.collectorWarehouseMovementId.toString(),
                        localObjectId2 = ""
                    ) { onUploadProgress.invoke(it) }
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_movements)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_movements),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_movements)
                    else -> getContext().getString(R.string.movement_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun asset() {
        val registryType = SyncRegistryType.Asset
        registryOnProcess.add(registryType)

        val assetWs = AssetWs()

        val assetDb = AssetDbHelper()
        val arcDb = AssetReviewContentDbHelper()
        val wmcDb = WarehouseMovementContentDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_asset_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val aAl = assetDb.selectNoTransferred()
            if (aAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_asset_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = aAl.size
            for ((currentTask, a) in aAl.toTypedArray().withIndex()) {
                a.setDataRead()
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_assets),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_asset_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                var realAssetId: Long
                if (a.assetId > 0) {
                    realAssetId = assetWs.assetCollectorModify(
                        Statics.currentUserId ?: return,
                        AssetCollectorObject(a)
                    )
                } else {
                    realAssetId =
                        assetWs.assetAdd(Statics.currentUserId ?: return, AssetObject(a))
                    if (realAssetId > 0) {

                        // Actualizar el propio activo
                        assetDb.updateAssetId(realAssetId, a.assetId)
                        val realAsset = assetDb.selectById(realAssetId)
                        if (realAsset != null) {
                            // Actualizar los movimientos asociados
                            wmcDb.updateAssetId(realAsset, a.assetId)

                            // Actualizar las revisiones asociadas
                            arcDb.updateAssetId(realAsset, a.assetId)
                        }

                        // Enviar imágenes si existen
                        uploadTempDocument(
                            programObjectId = Table.asset.tableId.toString(),
                            newObjectId1 = realAssetId.toString(),
                            newObjectId2 = "",
                            localObjectId1 = a.assetId.toString(),
                            localObjectId2 = ""
                        ) { onUploadProgress.invoke(it) }
                    }
                }

                if (realAssetId > 0) {
                    assetDb.updateTransferred(realAssetId)
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_assets)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_assets),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_assets)
                    else -> getContext().getString(R.string.asset_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun warehouseArea() {
        val registryType = SyncRegistryType.WarehouseArea
        registryOnProcess.add(registryType)

        val warehouseAreaWs = WarehouseAreaWs()

        val waDb = WarehouseAreaDbHelper()
        val aDb = AssetDbHelper()
        val uwaDb = UserWarehouseAreaDbHelper()
        val arDb = AssetReviewDbHelper()
        val wmDb = WarehouseMovementDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_warehouse_area_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val waAl = waDb.selectNoTransfered()
            if (waAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_warehouse_area_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = waAl.size
            for ((currentTask, wa) in waAl.toTypedArray().withIndex()) {
                wa.setDataRead()
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_warehouse_areas),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_warehouse_area_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                var realWarehouseAreaId: Long
                if (wa.warehouseAreaId > 0) {
                    realWarehouseAreaId = warehouseAreaWs.warehouseAreaModify(
                        Statics.currentUserId ?: return,
                        WarehouseAreaObject(wa)
                    )
                } else {
                    realWarehouseAreaId = warehouseAreaWs.warehouseAreaAdd(
                        Statics.currentUserId ?: return,
                        WarehouseAreaObject(wa)
                    )

                    if (realWarehouseAreaId > 0) {
                        // Actualizar la propia área
                        waDb.updateWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )

                        // Actualizar los activos fijos asociados
                        aDb.updateWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )

                        // Actualizar las áreas de usuario
                        uwaDb.updateWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )

                        // Actualizar las revisiones asociadas
                        arDb.updateWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )

                        // Actualizar los movimientos asociados
                        wmDb.updateOriginWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )
                        wmDb.updateDestWarehouseAreaId(
                            realWarehouseAreaId,
                            wa.warehouseAreaId
                        )

                        // Enviar las áreas del usuario
                        val uObj = UserObject().getByUser(
                            User(Statics.currentUserId ?: return, false)
                        )

                        val uwaObj = UserWarehouseAreaObject()
                        uwaObj.warehouse_area_id = realWarehouseAreaId
                        uwaObj.user_id = (Statics.currentUserId ?: return)
                        uwaObj.check = 1
                        uwaObj.count = 1
                        uwaObj.move = 1
                        uwaObj.see = 1

                        UserWarehouseAreaWs().userWarehouseAreaAdd(
                            Statics.currentUserId ?: return,
                            uObj,
                            arrayListOf(uwaObj)
                        )

                        // Enviar imágenes si existen
                        uploadTempDocument(
                            programObjectId = Table.warehouseArea.tableId.toString(),
                            newObjectId1 = realWarehouseAreaId.toString(),
                            newObjectId2 = "",
                            localObjectId1 = wa.warehouseAreaId.toString(),
                            localObjectId2 = ""
                        ) { onUploadProgress.invoke(it) }
                    }
                }

                if (realWarehouseAreaId > 0) {
                    waDb.updateTransferred(realWarehouseAreaId)
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_warehouse_areas)
                    else -> getContext().getString(R.string.warehouse_area_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun warehouse() {
        val registryType = SyncRegistryType.Warehouse
        registryOnProcess.add(registryType)

        val warehouseWs = WarehouseWs()

        val wDb = WarehouseDbHelper()
        val waDb = WarehouseAreaDbHelper()
        val aDb = AssetDbHelper()
        val arDb = AssetReviewDbHelper()
        val wmDb = WarehouseMovementDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_warehouse_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val wAl = wDb.selectNoTransfered()
            if (wAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_warehouse_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = wAl.size
            for ((currentTask, w) in wAl.toTypedArray().withIndex()) {
                w.setDataRead()
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_warehouses),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_warehouse_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                var realWarehouseId: Long
                if (w.warehouseId > 0) {
                    realWarehouseId = warehouseWs.warehouseModify(
                        Statics.currentUserId ?: return,
                        WarehouseObject(w)
                    )
                } else {
                    realWarehouseId = warehouseWs.warehouseAdd(
                        Statics.currentUserId ?: return,
                        WarehouseObject(w)
                    )

                    if (realWarehouseId > 0) {
                        // Actualizar el propio depósito
                        wDb.updateWarehouseId(realWarehouseId, w.warehouseId)

                        // Actualizar las áreas asociadas
                        waDb.updateWarehouseId(realWarehouseId, w.warehouseId)

                        // Actualizar los activos fijos asociados
                        aDb.updateWarehouseId(realWarehouseId, w.warehouseId)

                        // Actualizar las revisiones asociadas
                        arDb.updateWarehouseId(realWarehouseId, w.warehouseId)

                        // Actualizar los movimientos asociados
                        wmDb.updateOriginWarehouseId(realWarehouseId, w.warehouseId)
                        wmDb.updateDestWarehouseId(realWarehouseId, w.warehouseId)

                        // Enviar imágenes si existen
                        uploadTempDocument(
                            programObjectId = Table.warehouse.tableId.toString(),
                            newObjectId1 = realWarehouseId.toString(),
                            newObjectId2 = "",
                            localObjectId1 = w.warehouseId.toString(),
                            localObjectId2 = ""
                        ) { onUploadProgress.invoke(it) }
                    }
                }

                if (realWarehouseId > 0) {
                    wDb.updateTransferred(realWarehouseId)
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_warehouses)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_warehouses),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_warehouses)
                    else -> getContext().getString(R.string.warehouse_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun itemCategory() {
        val registryType = SyncRegistryType.ItemCategory
        registryOnProcess.add(registryType)

        val itemCategoryWs = ItemCategoryWs()

        val itemCategoryDb = ItemCategoryDbHelper()
        val aDb = AssetDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_category_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val icAl = itemCategoryDb.selectNoTransfered()
            if (icAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_category_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = icAl.size
            for ((currentTask, ic) in icAl.toTypedArray().withIndex()) {
                ic.setDataRead()
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_categories),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_category_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                var realItemCategoryId: Long
                if (ic.itemCategoryId > 0) {
                    realItemCategoryId = itemCategoryWs.itemCategoryModify(
                        Statics.currentUserId ?: return,
                        ItemCategoryObject(ic)
                    )
                } else {
                    realItemCategoryId = itemCategoryWs.itemCategoryAdd(
                        Statics.currentUserId ?: return,
                        ItemCategoryObject(ic)
                    )

                    if (realItemCategoryId > 0) {
                        // Actualizar la propia categoría
                        itemCategoryDb.updateItemCategoryId(
                            realItemCategoryId,
                            ic.itemCategoryId
                        )

                        // Actualizar los activos fijos asociados
                        aDb.updateItemCategoryId(realItemCategoryId, ic.itemCategoryId)

                        // Enviar imágenes si existen
                        uploadTempDocument(
                            programObjectId = Table.itemCategory.tableId.toString(),
                            newObjectId1 = realItemCategoryId.toString(),
                            newObjectId2 = "",
                            localObjectId1 = ic.itemCategoryId.toString(),
                            localObjectId2 = ""
                        ) { onUploadProgress.invoke(it) }
                    }
                }

                if (realItemCategoryId > 0) {
                    itemCategoryDb.updateTransferred(realItemCategoryId)
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_categories)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_categories),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_categories)
                    else -> getContext().getString(R.string.category_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun dataCollection() {
        val registryType = SyncRegistryType.DataCollection
        registryOnProcess.add(registryType)

        val dcWs = DataCollectionWs()

        val dcDb = DataCollectionDbHelper()
        val dccDb = DataCollectionContentDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_data_collection_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val dcAl = dcDb.selectByNoTransferred()
            if (dcAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_data_collection_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = dcAl.size
            for ((currentTask, dc) in dcAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_data_collections),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_data_collection_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                val dccAl = dccDb.selectByCollectorDataCollectionId(dc.collectorDataCollectionId)
                if (dccAl.size < 1) {
                    continue
                }

                val dccObjArray = ArrayList<DataCollectionContentObject>()
                for (dcc in dccAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    val x = DataCollectionContentObject()
                    x.attributeId = dcc.attributeId
                    x.attributeCompositionId = dcc.attributeCompositionId
                    x.dataCollectionDate = dcc.dataCollectionDate
                    x.dataCollectionId = 0L
                    x.dataCollectionContentId = 0L
                    x.dataCollectionRuleContentId = dcc.dataCollectionRuleContentId
                    x.level = dcc.level
                    x.position = dcc.position
                    x.result = dcc.result
                    x.valueStr = dcc.valueStr

                    dccObjArray.add(x)
                }

                val dcObj = DataCollectionObject()

                dcObj.assetId = dc.assetId
                dcObj.dataCollectionId = dc.collectorDataCollectionId
                dcObj.dateEnd = dc.dateEnd
                dcObj.dateStart = dc.dateStart
                dcObj.userId = dc.userId
                dcObj.warehouseAreaId = dc.warehouseAreaId
                dcObj.warehouseId = dc.warehouseId

                val dcId = dcWs.dataCollectionAdd(
                    dcObj,
                    dccObjArray
                )

                if (dcId > 0) {
                    dcDb.updateTransferred(dcId, dc.collectorDataCollectionId)

                    // Enviar imágenes si existen
                    uploadTempDocument(
                        programObjectId = Table.dataCollection.tableId.toString(),
                        newObjectId1 = dcId.toString(),
                        newObjectId2 = "",
                        localObjectId1 = dc.collectorDataCollectionId.toString(),
                        localObjectId2 = ""
                    ) { onUploadProgress.invoke(it) }
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_data_collections)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_data_collections),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_data_collections)
                    else -> getContext().getString(R.string.data_collection_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun routeProcess() {
        val registryType = SyncRegistryType.RouteProcess
        registryOnProcess.add(registryType)

        val rpWs = RouteProcessWs()

        val rpDb = RouteProcessDbHelper()
        val rpcDb = RouteProcessContentDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_route_process_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val rpAl = rpDb.selectByNoTransferred()
            if (rpAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_route_process_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = rpAl.size
            for ((currentTask, rp) in rpAl.toTypedArray().withIndex()) {
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_route_process),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_route_process_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                val rpcAl = rpcDb.selectByCollectorRouteProcessId(rp.collectorRouteProcessId)
                if (rpcAl.size < 1) {
                    continue
                }

                val rpcObjArray = ArrayList<RouteProcessContentObject>()
                for (rpc in rpcAl.toTypedArray()) {
                    if (!scope.isActive) {
                        break
                    }

                    var dataCollectionId = 0L
                    if (rpc.dataCollectionId != null && (rpc.dataCollectionId ?: return) > 0) {
                        val dc =
                            DataCollectionDbHelper()
                                .selectByCollectorId(
                                    rpc.dataCollectionId ?: return
                                )
                        if (dc != null && dc.dataCollectionId > 0) {
                            // Actualizamos todos los Id temporales
                            dataCollectionId = dc.dataCollectionId
                            rpc.dataCollectionId = dc.dataCollectionId
                        } else {
                            // No enviar contenidos sin datos recolectados.
                            continue
                        }
                    }

                    val x = RouteProcessContentObject()

                    x.dataCollectionRuleId = rpc.dataCollectionRuleId
                    x.level = rpc.level
                    x.position = rpc.position
                    x.routeProcessStatusId = rpc.routeProcessStatusId
                    x.dataCollectionId = dataCollectionId

                    rpcObjArray.add(x)
                }

                val rpObj = RouteProcessObject()

                rpObj.routeId = rp.routeId
                rpObj.routeProcessDate = rp.routeProcessDate
                rpObj.completed = if (rp.completed) {
                    1
                } else {
                    0
                }
                rpObj.routeProcessId = rp.collectorRouteProcessId
                rpObj.userId = rp.userId

                val rpId = rpWs.routeProcessAdd(
                    rpObj,
                    rpcObjArray
                )

                if (rpId > 0) {
                    rpDb.updateTransfered(rpId, rp.collectorRouteProcessId)

                    // Enviar imágenes si existen
                    uploadTempDocument(
                        programObjectId = Table.routeProcess.tableId.toString(),
                        newObjectId1 = rpId.toString(),
                        newObjectId2 = "",
                        localObjectId1 = rp.collectorRouteProcessId.toString(),
                        localObjectId2 = ""
                    ) { onUploadProgress.invoke(it) }
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_route_process)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_route_process),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_route_process)
                    else -> getContext().getString(R.string.route_process_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    private fun assetManteinance() {
        val registryType = SyncRegistryType.AssetManteinance
        registryOnProcess.add(registryType)

        val amWs = AssetManteinanceWs()

        val amDb = AssetManteinanceDbHelper()

        var error = false

        ///// Comienzo de una transacción /////
        val db = getWritableDb()
        db.beginTransaction()

        try {
            if (!scope.isActive) {
                onSyncTaskProgress.invoke(SyncProgress(
                    0,
                    0,
                    getContext().getString(R.string.canceling_maintenance_type_synchronization),
                    registryType,
                    ProgressStatus.canceled
                ))
                return
            }

            val amAl = amDb.selectNoTransfered()
            if (amAl.size < 1) {
                return
            }

            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.starting_maintenance_type_synchronization),
                registryType,
                ProgressStatus.starting
            ))

            val totalTask = amAl.size
            for ((currentTask, am) in amAl.toTypedArray().withIndex()) {
                am.setDataRead()
                onSyncTaskProgress.invoke(SyncProgress(
                    totalTask,
                    currentTask,
                    getContext().getString(R.string.synchronizing_maintenance_types),
                    registryType,
                    ProgressStatus.running
                ))

                if (!scope.isActive) {
                    onSyncTaskProgress.invoke(SyncProgress(
                        0,
                        0,
                        getContext().getString(R.string.canceling_maintenance_type_synchronization),
                        registryType,
                        ProgressStatus.canceled
                    ))
                    break
                }

                //Thread.sleep(250)

                val amObj = AssetManteinanceObject()
                amObj.asset_id = am.assetId
                amObj.asset_manteinance_id = am.assetManteinanceId
                amObj.manteinance_type_id = am.manteinanceTypeId
                amObj.manteinance_status_id = am.manteinanceStatusId
                amObj.repairman_id = (Statics.currentUserId ?: return)

                val amLogObj = AssetManteinanceLogObject()
                amLogObj.description = am.observations
                amLogObj.asset_manteinance_id = am.assetManteinanceId
                amLogObj.manteinance_status_id = am.manteinanceStatusId
                amLogObj.repairman_id = (Statics.currentUserId ?: return)

                val assetManteinanceId =
                    if (am.assetManteinanceId == 0L) {
                        amWs.assetManteinanceAdd(
                            Statics.currentUserId ?: return,
                            amObj,
                            amLogObj
                        )
                    } else {
                        amWs.assetManteinanceModify(
                            Statics.currentUserId ?: return,
                            amObj,
                            amLogObj
                        )
                    }

                if (assetManteinanceId > 0) {
                    amDb.updateTransferred(assetManteinanceId)

                    // Enviar imágenes si existen
                    uploadTempDocument(
                        programObjectId = Table.assetManteinance.tableId.toString(),
                        newObjectId1 = assetManteinanceId.toString(),
                        newObjectId2 = "",
                        localObjectId1 = am.assetManteinanceId.toString(),
                        localObjectId2 = ""
                    ) { onUploadProgress.invoke(it) }
                } else {
                    error = true
                    Log.d(
                        this::class.java.simpleName,
                        getContext().getString(R.string.failed_to_synchronize_the_maintenance_types)
                    )
                }
            }

            // La transacción terminó correctamente
            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            // Error remoto
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                getContext().getString(R.string.failed_to_synchronize_the_maintenance_types),
                registryType,
                ProgressStatus.crashed
            ))
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return
        } finally {
            db.endTransaction()

            registryOnProcess.remove(registryType)
            onSyncTaskProgress.invoke(SyncProgress(
                0,
                0,
                when {
                    error -> getContext().getString(R.string.failed_to_synchronize_the_maintenance_types)
                    else -> getContext().getString(R.string.maintenance_type_synchronization_completed)
                },
                registryType,
                when {
                    error -> ProgressStatus.crashed
                    else -> ProgressStatus.success
                }
            ))
        }
    }

    init {
        if (registryOnProcess.size > 0) {
            onSyncTaskProgress.invoke(SyncProgress(
                totalTask = 0,
                completedTask = 0,
                msg = "",
                registryType = null,
                progressStatus = ProgressStatus.canceled
            ))
        } else {
            checkConnection()
        }
    }
}