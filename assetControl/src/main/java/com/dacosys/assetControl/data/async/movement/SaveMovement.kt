package com.dacosys.assetControl.data.async.movement

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.enums.common.Table
import com.dacosys.assetControl.data.enums.movement.WarehouseMovementContentStatus
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementContentRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import com.dacosys.imageControl.room.database.IcDatabase
import kotlinx.coroutines.*
import java.util.*

class SaveMovement {
    private var allMovementContent: ArrayList<WarehouseMovementContent> = ArrayList()
    private var destWarehouseAreaId: Long? = null
    private var obs: String = ""
    private var collectorMovementId: Long? = null
    private var onProgress: (SaveProgress) -> Unit = {}
    private var onSyncProgress: (SyncProgress) -> Unit = {}
    private var onUploadImageProgress: (UploadImagesProgress) -> Unit = {}

    fun addParams(
        destWarehouseAreaId: Long,
        obs: String,
        movementContents: ArrayList<WarehouseMovementContent>,
        onProgress: (SaveProgress) -> Unit = {},
        onSyncProgress: (SyncProgress) -> Unit = {},
        onUploadImageProgress: (UploadImagesProgress) -> Unit = {},
    ) {
        this.destWarehouseAreaId = destWarehouseAreaId
        this.obs = obs
        this.allMovementContent = movementContents
        this.onProgress = onProgress
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
            result = deferred?.await() ?: false
        }

        if (result && autoSend()) {
            SyncUpload(
                registryType = SyncRegistryType.WarehouseMovement,
                onSyncTaskProgress = { onSyncProgress.invoke(it) },
                onUploadImageProgress = { onUploadImageProgress.invoke(it) },
            )
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        val destWaId = destWarehouseAreaId ?: return@withContext false
        val destWa = WarehouseAreaRepository().selectById(destWaId) ?: return@withContext false

        if (!allMovementContent.any()) return@withContext false

        val userId = getUserId() ?: return@withContext false

        try {
            onProgress.invoke(
                SaveProgress(
                    msg = getContext().getString(R.string.saving_warehouse_movement),
                    taskStatus = ProgressStatus.starting.id,
                )
            )

            // Listas de activos a mover
            val assetInMovementList: ArrayList<WarehouseMovementContent> = ArrayList()

            // Activos para dar de alta. Aquellos activos que ya pertenecen
            // al área de destino, pero se encuentran extraviados no forman parte
            // del contenido del movimiento, pero vuelve a estar En Inventario
            val assetFoundedList: ArrayList<WarehouseMovementContent> = ArrayList()

            var partialCount = 0
            var msg = ""

            for (wmCont in allMovementContent) {
                partialCount++
                when {
                    wmCont.contentStatusId != WarehouseMovementContentStatus.noNeedToMove.id -> {
                        assetInMovementList.add(wmCont)
                        msg = "${
                            getContext().getString(R.string.processing_asset_to_move)
                        } ${wmCont.code}"
                    }

                    wmCont.contentStatusId == WarehouseMovementContentStatus.noNeedToMove.id &&
                            wmCont.assetStatusId == AssetStatus.missing.id -> {
                        assetFoundedList.add(wmCont)
                        msg = "${
                            getContext().getString(R.string.processing_asset_to_register)
                        } ${wmCont.code}"
                    }
                }

                onProgress.invoke(
                    SaveProgress(
                        msg = msg,
                        taskStatus = ProgressStatus.running.id,
                        progress = partialCount,
                        total = allMovementContent.size
                    )
                )

                Log.d(this::class.java.simpleName, msg)
            }

            // Dar de alta los activos que se encontraron y que ya
            // pertenecían al área de destino.
            AssetRepository().setOnInventoryFromArea(
                warehouseAreaId = destWaId, assets = assetFoundedList
            )

            // Hacer los movimientos y los cambios de estados de los activos solo
            // cuando el movimiento está completada
            if (assetInMovementList.isNotEmpty()) {

                //////////// MOVEMENTS ////////////
                val movementRepository = WarehouseMovementRepository()
                val contentRepository = WarehouseMovementContentRepository()

                try {
                    // Create an Array List with the different
                    // Origin Warehouse Areas to select the number of movements to do
                    val waIdList = ArrayList<Long>()

                    // Traer todos los orígenes únicos
                    partialCount = 0
                    for (tempAsset in assetInMovementList) {
                        partialCount++

                        if (!waIdList.contains(tempAsset.warehouseAreaId)) {
                            waIdList.add(tempAsset.warehouseAreaId)
                        }

                        msg = "${
                            getContext().getString(R.string.processing_asset)
                        } ${tempAsset.code}"
                        onProgress.invoke(
                            SaveProgress(
                                msg = msg,
                                taskStatus = ProgressStatus.running.id,
                                progress = partialCount,
                                total = assetInMovementList.size
                            )
                        )
                    }

                    // Valor inicial de ID para contenidos reemplazando los negativos
                    var lastId = WarehouseMovementContentRepository().maxId

                    // Create Warehouse Movements Content by each Origin Warehouse Area
                    partialCount = 0
                    for (origWaId in waIdList) {
                        partialCount++
                        msg = getContext().getString(R.string.making_movement)
                        onProgress.invoke(
                            SaveProgress(
                                msg = msg,
                                taskStatus = ProgressStatus.running.id,
                                progress = partialCount,
                                total = waIdList.size
                            )
                        )

                        val origWa = WarehouseAreaRepository().selectById(origWaId) ?: return@withContext false
                        val origWId = origWa.warehouseId
                        val destWId = destWa.warehouseId

                        val wm = WarehouseMovement(
                            originWarehouseId = origWId,
                            originWarehouseAreaId = origWaId,
                            destinationWarehouseId = destWId,
                            destinationWarehouseAreaId = destWaId,
                            warehouseMovementDate = Date(),
                            obs = obs,
                            userId = userId
                        )

                        val newId = movementRepository.insert(wm)
                        wm.id = newId

                        val assetsInArea: ArrayList<WarehouseMovementContent> = ArrayList()
                        assetInMovementList
                            .filterTo(assetsInArea) { it.warehouseAreaId == origWaId }
                            .map {
                                lastId++
                                it.warehouseMovementId = newId
                                it.id = lastId
                            }

                        contentRepository.insert(
                            movement = wm,
                            contents = assetsInArea,
                            progress = onProgress
                        )

                        try {
                            // Activos que están en el movimiento cambian de estado a En Inventario.
                            AssetRepository().setOnInventoryFromWmCont(wm, assetsInArea)
                        } catch (ex: Exception) {
                            msg = "${
                                getContext().getString(R.string.error_updating_asset_status)
                            }: ${ex.message}"
                            onProgress.invoke(
                                SaveProgress(
                                    msg = msg,
                                    taskStatus = ProgressStatus.crashed.id,
                                )
                            )
                            return@withContext false
                        }

                        wm.completed = 1
                        wm.saveChanges()

                        collectorMovementId = wm.id
                    }

                    if (collectorMovementId != null) {
                        // ACTUALIZAR EL ID DEL MOVIMIENTO EN LA BASE DE IMAGECONTROL
                        IcDatabase.getDatabase(context = getContext()).imageDao().updateImage(
                            programObjectId = Table.warehouseMovement.id.toLong(),
                            newObjectId1 = collectorMovementId.toString(),
                            oldObjectId1 = "0"
                        )
                    } else {
                        msg = getContext().getString(R.string.error_updating_movement)
                        onProgress.invoke(
                            SaveProgress(
                                msg = msg,
                                taskStatus = ProgressStatus.crashed.id,
                            )
                        )
                        return@withContext false
                    }
                } catch (ex: Exception) {
                    msg = "${
                        getContext().getString(R.string.error_making_movements)
                    }: ${ex.message}"
                    onProgress.invoke(
                        SaveProgress(
                            msg = msg,
                            taskStatus = ProgressStatus.crashed.id,
                        )
                    )
                    return@withContext false
                }
            } else if (assetFoundedList.isNotEmpty()) {
                // Si no había contenidos en el movimiento, pero sí cambios de
                // estado de algunos activos el proceso termina correctamente.
                msg = getContext().getString(R.string.movement_performed_correctly)
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.finished.id
                    )
                )
                return@withContext true
            }

            if (collectorMovementId != null) {
                msg = getContext().getString(R.string.movement_performed_correctly)
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.finished.id
                    )
                )
                return@withContext true
            } else {
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.crashed.id
                    )
                )
                return@withContext false
            }
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
}