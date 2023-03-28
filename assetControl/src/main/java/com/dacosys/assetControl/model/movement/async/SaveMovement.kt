package com.dacosys.assetControl.model.movement.async

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementDbHelper
import com.dacosys.assetControl.model.asset.AssetStatus
import com.dacosys.assetControl.model.common.SaveProgress
import com.dacosys.assetControl.model.movement.WarehouseMovementContent
import com.dacosys.assetControl.model.movement.WarehouseMovementContentStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.imageControl.room.database.IcDatabase
import kotlinx.coroutines.*

class SaveMovement {
    private var allMovementContent: ArrayList<WarehouseMovementContent> = ArrayList()
    private var destWarehouseAreaId: Long? = null
    private var obs: String = ""
    private var collectorMovementId: Long? = null
    private var onProgress: (SaveProgress) -> Unit = {}

    fun addParams(
        destWarehouseAreaId: Long,
        obs: String,
        allMovementContent: ArrayList<WarehouseMovementContent>,
        onProgress: (SaveProgress) -> Unit = {},
    ) {
        this.onProgress = onProgress
        this.destWarehouseAreaId = destWarehouseAreaId
        this.obs = obs
        this.allMovementContent = allMovementContent
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
            // Por el momento no se están escuchando los eventos de sincroinización
            SyncUpload(SyncRegistryType.WarehouseMovement)
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        if (destWarehouseAreaId == null || !allMovementContent.any()) {
            return@withContext false
        }

        ///////////////////////////////////
        // Para controlar la transacción //
        val db = DataBaseHelper.getWritableDb()

        try {
            onProgress.invoke(
                SaveProgress(
                    msg = getContext().getString(R.string.saving_warehouse_movement),
                    taskStatus = ProgressStatus.starting.id,
                    progress = 0,
                    total = 0
                )
            )

            // Listas de activos a mover
            val assetInMovementList: ArrayList<WarehouseMovementContent> = ArrayList()

            // Activos para dar de alta. Aquellos activos que ya pertenecen
            // al área de destino pero se encuentran extraviados no forman parte
            // del contenido del movimiento pero vuelve a estar En Inventario
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
                    wmCont.contentStatusId == WarehouseMovementContentStatus.noNeedToMove.id && wmCont.assetStatusId == AssetStatus.missing.id -> {
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

            ///// Comienzo de una transacción /////
            db.beginTransaction()

            // Dar de alta los activos que se encontraron y que ya
            // pertenecían al área de destino.
            AssetDbHelper().setOnInventoryFromArea(
                warehouseAreaId = destWarehouseAreaId!!, assets = assetFoundedList
            )

            // Hacer los movimientos y los cambios de estados de los activos sólo
            // cuando el movimiento está completada
            if (assetInMovementList.size > 0) {

                //////////// MOVEMENTS ////////////
                val wmDbHelper = WarehouseMovementDbHelper()
                val wmContDbHelper = WarehouseMovementContentDbHelper()

                try {
                    // Create a Array List with the differents
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

                        val newWm = wmDbHelper.insert(
                            origWaId, destWarehouseAreaId!!, obs
                        )

                        if (newWm != null) {
                            val l: ArrayList<WarehouseMovementContent> = ArrayList()
                            for (x in assetInMovementList) {
                                if (x.warehouseAreaId == origWaId) {
                                    l.add(x)
                                }
                            }

                            wmContDbHelper.insertWm(newWm, l)

                            try {
                                // Activos que están en el movimiento cambian de estado a En Inventario.
                                AssetDbHelper().setOnInventoryFromWmCont(newWm, l)
                            } catch (ex: Exception) {
                                msg = "${
                                    getContext().getString(R.string.error_updating_asset_status)
                                }: ${ex.message}"
                                onProgress.invoke(
                                    SaveProgress(
                                        msg = msg,
                                        taskStatus = ProgressStatus.crashed.id,
                                        progress = 0,
                                        total = 0
                                    )
                                )
                                return@withContext false
                            }

                            newWm.completed = true
                            if (!newWm.saveChanges()) {
                                collectorMovementId = null
                                break
                            }

                            collectorMovementId = newWm.collectorWarehouseMovementId
                        }
                    }

                    if (collectorMovementId != null) {
                        // ACTUALIZAR EL ID DEL MOVIMIENTO EN LA BASE DE IMAGECONTROL
                        IcDatabase.getDatabase().imageDao().updateImage(
                            programObjectId = Table.warehouseMovement.tableId.toLong(),
                            newObjectId1 = collectorMovementId.toString(),
                            oldObjectId1 = "0"
                        )
                    } else {
                        msg = getContext().getString(R.string.error_updating_movement)
                        onProgress.invoke(
                            SaveProgress(
                                msg = msg,
                                taskStatus = ProgressStatus.crashed.id,
                                progress = 0,
                                total = 0
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
                            progress = 0,
                            total = 0
                        )
                    )
                    return@withContext false
                }
            } else if (assetFoundedList.size > 0) {
                // Si no había contenidos en el movimiento, pero sí cambios de
                // estado de algunos activos el proceso termina correctamente.
                msg = getContext().getString(R.string.movement_performed_correctly)
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.finished.id, progress = 0, total = 0
                    )
                )
                return@withContext true
            }

            db.setTransactionSuccessful()

            if (collectorMovementId != null) {
                msg = getContext().getString(R.string.movement_performed_correctly)
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.finished.id, progress = 0, total = 0
                    )
                )
                return@withContext true
            } else {
                onProgress.invoke(
                    SaveProgress(
                        msg = msg, taskStatus = ProgressStatus.crashed.id, progress = 0, total = 0
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
                    progress = 0,
                    total = 0
                )
            )
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return@withContext false
        } finally {
            db.endTransaction()
        }
    }
}