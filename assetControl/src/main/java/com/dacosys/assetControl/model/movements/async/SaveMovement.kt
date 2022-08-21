package com.dacosys.assetControl.model.movements.async

import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovementContentStatus.WarehouseMovementContentStatus
import com.dacosys.assetControl.model.table.Table
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.SyncRegistryType
import com.dacosys.assetControl.sync.functions.SyncUpload
import com.dacosys.imageControl.dbHelper.DbCommands.Companion.updateObjectId1
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class SaveMovement {
    interface SaveMovementListener {
        // Define data you like to return from AysncTask
        fun onSaveMovementProgress(
            msg: String,
            taskStatus: Int,
            progress: Int? = null,
            total: Int? = null,
        )
    }

    private var weakRef: WeakReference<SaveMovementListener>? = null
    private var listener: SaveMovementListener?
        get() {
            return weakRef?.get()
        }
        set(value) {
            weakRef = if (value != null) WeakReference(value) else null
        }

    private var allMovementContent: ArrayList<WarehouseMovementContent> = ArrayList()
    private var destWarehouseAreaId: Long? = null
    private var obs: String = ""
    private var collectorMovementId: Long? = null

    private fun preExecute() {
        // TODO: JotterListener.lockScanner(this, true)
    }

    private fun postExecute(result: Boolean): Boolean {
        // TODO: JotterListener.lockScanner(this, false)
        return result
    }

    fun addParams(
        callback: SaveMovementListener,
        destWarehouseAreaId: Long,
        obs: String,
        allMovementContent: ArrayList<WarehouseMovementContent>,
    ) {
        listener = callback
        this.destWarehouseAreaId = destWarehouseAreaId
        this.obs = obs
        this.allMovementContent = allMovementContent
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
        if (destWarehouseAreaId == null || !allMovementContent.any()) {
            return@withContext false
        }

        try {
            listener?.onSaveMovementProgress(
                msg = Statics.AssetControl.getContext()
                    .getString(R.string.saving_warehouse_movement),
                taskStatus = ProgressStatus.starting.id,
                progress = 0,
                total = 0
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
                            Statics.AssetControl.getContext()
                                .getString(R.string.processing_asset_to_move)
                        } ${wmCont.code}"
                    }
                    wmCont.contentStatusId == WarehouseMovementContentStatus.noNeedToMove.id &&
                            wmCont.assetStatusId == AssetStatus.missing.id -> {
                        assetFoundedList.add(wmCont)
                        msg = "${
                            Statics.AssetControl.getContext()
                                .getString(R.string.processing_asset_to_register)
                        } ${wmCont.code}"
                    }
                }

                listener?.onSaveMovementProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.running.id,
                    progress = partialCount,
                    total = allMovementContent.size
                )

                Log.d(this::class.java.simpleName, msg)
            }

            // Dar de alta los activos que se encontraron y que ya
            // pertenecían al área de destino.
            AssetDbHelper().setOnInventoryFromArea(
                warehouseAreaId = destWarehouseAreaId!!,
                assets = assetFoundedList
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
                            Statics.AssetControl.getContext().getString(R.string.processing_asset)
                        } ${tempAsset.code}"
                        listener?.onSaveMovementProgress(
                            msg = msg,
                            taskStatus = ProgressStatus.running.id,
                            progress = partialCount,
                            total = assetInMovementList.size
                        )
                    }

                    // Create Warehouse Movements Content by each Origin Warehouse Area
                    partialCount = 0
                    for (origWaId in waIdList) {
                        partialCount++
                        msg = Statics.AssetControl.getContext().getString(R.string.making_movement)
                        listener?.onSaveMovementProgress(
                            msg = msg,
                            taskStatus = ProgressStatus.running.id,
                            progress = partialCount,
                            total = waIdList.size
                        )

                        val newWm = wmDbHelper.insert(
                            origWaId,
                            destWarehouseAreaId!!,
                            obs
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
                                    Statics.AssetControl.getContext()
                                        .getString(R.string.error_updating_asset_status)
                                }: ${ex.message}"
                                listener?.onSaveMovementProgress(
                                    msg = msg,
                                    taskStatus = ProgressStatus.crashed.id,
                                    progress = 0,
                                    total = 0
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
                        updateObjectId1(
                            programObjectId = Table.warehouseMovement.tableId.toString(),
                            newObjectId1 = collectorMovementId.toString(),
                            oldObjectId1 = "0"
                        )
                    } else {
                        msg = Statics.AssetControl.getContext()
                            .getString(R.string.error_updating_movement)
                        listener?.onSaveMovementProgress(
                            msg = msg,
                            taskStatus = ProgressStatus.crashed.id,
                            progress = 0,
                            total = 0
                        )
                        return@withContext false
                    }
                } catch (ex: Exception) {
                    msg = "${
                        Statics.AssetControl.getContext().getString(R.string.error_making_movements)
                    }: ${ex.message}"
                    listener?.onSaveMovementProgress(
                        msg = msg,
                        taskStatus = ProgressStatus.crashed.id,
                        progress = 0,
                        total = 0
                    )
                    return@withContext false
                }
            } else if (assetFoundedList.size > 0) {
                // Si no había contenidos en el movimiento, pero sí cambios de
                // estado de algunos activos el proceso termina correctamente.
                msg = Statics.AssetControl.getContext()
                    .getString(R.string.movement_performed_correctly)
                listener?.onSaveMovementProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.finished.id,
                    progress = 0,
                    total = 0
                )
                return@withContext true
            }

            if (collectorMovementId != null) {
                if (Statics.autoSend()) {
                    thread {
                        val sync = SyncUpload()
                        sync.addRegistryToSync(SyncRegistryType.WarehouseMovement)
                        sync.execute()
                    }
                }
                msg = Statics.AssetControl.getContext()
                    .getString(R.string.movement_performed_correctly)
                listener?.onSaveMovementProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.finished.id,
                    progress = 0,
                    total = 0
                )
                return@withContext true
            } else {
                listener?.onSaveMovementProgress(
                    msg = msg,
                    taskStatus = ProgressStatus.crashed.id,
                    progress = 0,
                    total = 0
                )
                return@withContext false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            listener?.onSaveMovementProgress(
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