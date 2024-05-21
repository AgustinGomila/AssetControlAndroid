package com.dacosys.assetControl.data.async.review

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.enums.review.AssetReviewContentStatus
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.dacosys.assetControl.data.room.dto.review.AssetReview
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContent
import com.dacosys.assetControl.data.room.repository.asset.AssetRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementContentRepository
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.data.room.repository.review.AssetReviewContentRepository
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.misc.UTCDataTime
import com.dacosys.imageControl.network.upload.UploadImagesProgress
import kotlinx.coroutines.*

class SaveReview {
    private lateinit var tempReview: AssetReview
    private var allAssets: ArrayList<AssetReviewContent> = ArrayList()
    private var onSaveProgress: (SaveProgress) -> Unit = {}
    private var onSyncProgress: (SyncProgress) -> Unit = {}
    private var onUploadImageProgress: (UploadImagesProgress) -> Unit = {}

    fun addParams(
        assetReview: AssetReview,
        contents: ArrayList<AssetReviewContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
        onSyncProgress: (SyncProgress) -> Unit = {},
        onUploadImageProgress: (UploadImagesProgress) -> Unit = {},
    ) {
        this.tempReview = assetReview
        this.allAssets = contents
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
            result = deferred?.await() ?: false
        }

        if (result && autoSend()) {
            SyncUpload(
                registryType = SyncRegistryType.AssetReview,
                onSyncTaskProgress = { onSyncProgress.invoke(it) },
                onUploadImageProgress = { onUploadImageProgress.invoke(it) },
            )
        } else {
            onSyncProgress.invoke(
                SyncProgress(progressStatus = ProgressStatus.bigFinished)
            )
        }
    }

    private suspend fun suspendFunction(): Boolean = withContext(Dispatchers.IO) {
        /////////////////////////////////////
        ///// Construir las colecciones /////

        // Lista de activos que provienen de otra área
        val assetExternalList: ArrayList<AssetReviewContent> = ArrayList()

        // Listas de activos que cambiarán de estado cuando la revisión esté completada
        val assetNotInReviewList: ArrayList<AssetReviewContent> = ArrayList()
        val assetOnInventory: ArrayList<AssetReviewContent> = ArrayList()

        // Lista de activos desconocidos que no están la base de datos
        val assetUnknownList: ArrayList<AssetReviewContent> = ArrayList()

        // Iteramos la lista de todos los activos, tanto los que fueron encontrados durante
        // la revisión como los que no y los que no existen en la base de datos.
        for (arCont in allAssets) {
            var msg = ""
            when (arCont.contentStatusId) {
                AssetReviewContentStatus.external.id -> {
                    assetOnInventory.add(arCont)
                    assetExternalList.add(arCont)
                    msg =
                        "${getContext().getString(R.string.processing_external_asset)} ${arCont.code}"
                }

                AssetReviewContentStatus.revised.id -> {
                    assetOnInventory.add(arCont)
                    msg =
                        "${getContext().getString(R.string.processing_revised_asset)} ${arCont.code}"
                }

                AssetReviewContentStatus.newAsset.id -> {
                    assetOnInventory.add(arCont)
                    msg = "${getContext().getString(R.string.processing_new_asset)} ${arCont.code}"
                }

                AssetReviewContentStatus.appeared.id -> {
                    assetOnInventory.add(arCont)
                    msg =
                        "${getContext().getString(R.string.processing_appeared_asset)} ${arCont.code}"
                }

                AssetReviewContentStatus.unknown.id -> {
                    assetUnknownList.add(arCont)
                    msg =
                        "${getContext().getString(R.string.processing_unknown_asset)} ${arCont.code}"
                }

                AssetReviewContentStatus.notInReview.id -> {
                    // Activos faltantes en la revisión
                    assetNotInReviewList.add(arCont)
                    msg =
                        "${getContext().getString(R.string.processing_missing_asset)} ${arCont.code}"
                }
            }

            // makeText(binding.root, msg, LENGTH_SHORT)
            Log.d(this::class.java.simpleName, msg)
        }

        ///////////////////////////////////
        // Para controlar la transacción //
        // TODO: Eliminar val db = DataBaseHelper.getWritableDb()

        // Hacer los movimientos y los cambios de estados de los activos solo
        // cuando la revisión está completada
        if (tempReview.statusId == AssetReviewStatus.completed.id) {
            //////////// MOVEMENTS ////////////
            // Create an Array List with the different
            // Origin Warehouse Areas to select the number of movements to do
            val waIdList = ArrayList<Long>()

            // Traer todos los orígenes únicos
            var total = assetExternalList.size
            for ((p, tempAsset) in assetExternalList.withIndex()) {
                // Omitir activos desconocidos que no tiene área de origen válida
                if (tempAsset.originWarehouseAreaId <= 0) continue

                if (!waIdList.contains(tempAsset.originWarehouseAreaId)) {
                    waIdList.add(tempAsset.originWarehouseAreaId)
                }

                onSaveProgress.invoke(
                    SaveProgress(
                        msg = "${getContext().getString(R.string.processing_external_asset)} ${tempAsset.code}",
                        taskStatus = ProgressStatus.running.id,
                        progress = p,
                        total = total
                    )
                )
            }

            val movementRepository = WarehouseMovementRepository()
            val contentRepository = WarehouseMovementContentRepository()

            ///// Comienzo de una transacción /////
            // TODO: Eliminar db.beginTransaction()

            try {
                // Create Warehouse Movements Content by each Origin Warehouse Area
                total = waIdList.size
                for ((p, origWaId) in waIdList.withIndex()) {
                    val newWm = WarehouseMovement(
                        originWarehouseAreaId = origWaId,
                        destinationWarehouseAreaId = tempReview.warehouseAreaId,
                        obs = tempReview.obs
                    )
                    movementRepository.insert(newWm)

                    onSaveProgress.invoke(
                        SaveProgress(
                            msg = getContext().getString(R.string.making_movement),
                            taskStatus = ProgressStatus.running.id,
                            progress = p,
                            total = total
                        )
                    )

                    val l: ArrayList<WarehouseMovementContent> = ArrayList()
                    for (x in assetExternalList) {
                        if (x.warehouseAreaId == origWaId) {
                            l.add(WarehouseMovementContent(x))
                        }
                    }

                    contentRepository.insert(
                        movement = newWm,
                        contents = l.toList(),
                        progress = onSaveProgress
                    )
                    val date = UTCDataTime.getUTCDateTimeAsString()

                    newWm.completed = 1
                    newWm.obs =
                        "${getContext().getString(R.string.automatic_movement_during_the_revision_on_date)}: $date"
                    newWm.saveChanges()
                }

                // TODO: Eliminar db.setTransactionSuccessful()

            } catch (ex: Exception) {
                ex.printStackTrace()
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = "${getContext().getString(R.string.error_making_movements)}: ${ex.message}",
                        taskStatus = ProgressStatus.crashed.id,
                    )
                )
                return@withContext false
            } finally {
                // TODO: Eliminar db.endTransaction()
            }

            //////////// ASSET STATUS ////////////
            val assetRepository = AssetRepository()

            ///// Comienzo de una transacción /////
            // TODO: Eliminar db.beginTransaction()

            try {
                // Activos que no están en la revisión cambian de estado a Extraviados
                // Activos en la revisión o aparecidos en el área revisada cambian a En Inventario.

                assetRepository.setMissing(assetNotInReviewList)
                assetRepository.setOnInventoryFromArCont(tempReview, assetOnInventory)

                // TODO: Eliminar db.setTransactionSuccessful()
            } catch (ex: Exception) {
                ex.printStackTrace()
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = "${getContext().getString(R.string.error_updating_asset_status)}: ${ex.message}",
                        taskStatus = ProgressStatus.crashed.id,
                    )
                )
                return@withContext false
            } finally {
                // TODO: Eliminar db.endTransaction()
            }
        }

        ///// Comienzo de una transacción /////
        // TODO: Eliminar db.beginTransaction()

        val contentRepository = AssetReviewContentRepository()
        try {
            // Limpiar el contenido de la revisión ANTIGUA
            contentRepository.deleteByAssetReviewId(tempReview.id)

            // Agregar el contenido de la revisión
            // Se agregan todos los activos, el sincronizador se encarga después
            // de NO enviar aquellos que no fueron revisados
            contentRepository.insert(
                id = tempReview.id,
                contents = allAssets,
                progress = onSaveProgress
            )

            // TODO: Eliminar db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            onSaveProgress.invoke(
                SaveProgress(
                    msg = "${getContext().getString(R.string.failed_to_do_the_review)}: ${ex.message}",
                    taskStatus = ProgressStatus.crashed.id,
                )
            )
            return@withContext false
        } finally {
            // TODO: Eliminar db.endTransaction()
        }
        return@withContext true
    }
}