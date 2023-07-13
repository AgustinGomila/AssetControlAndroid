package com.dacosys.assetControl.model.review.async

import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementDbHelper
import com.dacosys.assetControl.dataBase.review.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.common.SaveProgress
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.model.review.AssetReviewContentStatus
import com.dacosys.assetControl.model.review.AssetReviewStatus
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.sync.SyncUpload
import com.dacosys.assetControl.network.utils.Connection.Companion.autoSend
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.misc.UTCDataTime
import kotlinx.coroutines.*

class SaveReview {
    private lateinit var tempReview: AssetReview
    private var allAssets: ArrayList<AssetReviewContent> = ArrayList()
    private var onSaveProgress: (SaveProgress) -> Unit = {}
    private var onSyncProgress: (SyncProgress) -> Unit = {}

    fun addParams(
        assetReview: AssetReview,
        allAssetList: ArrayList<AssetReviewContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
        onSyncProgress: (SyncProgress) -> Unit = {},
    ) {
        this.tempReview = assetReview
        this.allAssets = allAssetList
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

        if (result && autoSend()) {
            SyncUpload(
                registryType = SyncRegistryType.AssetReview,
                onSyncTaskProgress = { onSyncProgress.invoke(it) })
        } else {
            onSyncProgress.invoke(SyncProgress(progressStatus = ProgressStatus.bigFinished))
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

        // Iteramos la lista de todos los activos, tanto los que fueron encotrados durante
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
        val db = DataBaseHelper.getWritableDb()

        // Hacer los movimientos y los cambios de estados de los activos sólo
        // cuando la revisión está completada
        if (tempReview.statusId == AssetReviewStatus.completed.id) {
            //////////// MOVEMENTS ////////////
            // Create a Array List with the differents
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

            val wmDbHelper = WarehouseMovementDbHelper()
            val wmContDbHelper = WarehouseMovementContentDbHelper()

            ///// Comienzo de una transacción /////
            db.beginTransaction()

            try {
                // Create Warehouse Movements Content by each Origin Warehouse Area
                total = waIdList.size
                for ((p, origWaId) in waIdList.withIndex()) {
                    val newWm =
                        wmDbHelper.insert(origWaId, tempReview.warehouseAreaId, tempReview.obs)

                    onSaveProgress.invoke(
                        SaveProgress(
                            msg = getContext().getString(R.string.making_movement),
                            taskStatus = ProgressStatus.running.id,
                            progress = p,
                            total = total
                        )
                    )

                    if (newWm != null) {
                        val l: ArrayList<AssetReviewContent> = ArrayList()
                        for (x in assetExternalList) {
                            if (x.warehouseAreaId == origWaId) {
                                l.add(x)
                            }
                        }

                        wmContDbHelper.insertAr(newWm, l)
                        val date = UTCDataTime.getUTCDateTimeAsString()

                        newWm.completed = true
                        newWm.obs =
                            "${getContext().getString(R.string.automatic_movement_during_the_revision_on_date)}: $date"
                        newWm.saveChanges()
                    }
                }

                db.setTransactionSuccessful()

            } catch (ex: Exception) {
                ex.printStackTrace()
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = "${getContext().getString(R.string.error_making_movements)}: ${ex.message}",
                        taskStatus = ProgressStatus.crashed.id,
                        progress = 0,
                        total = 0
                    )
                )
                return@withContext false
            } finally {
                db.endTransaction()
            }

            //////////// ASSET STATUS ////////////
            val assetDbHelper = AssetDbHelper()

            ///// Comienzo de una transacción /////
            db.beginTransaction()

            try {
                // Activos que no están en la revisión cambian de estado a Extraviados
                // Activos en la revisión o aparecidos en el área revisada cambian a En Inventario.

                assetDbHelper.setMissing(assetNotInReviewList)
                assetDbHelper.setOnInventoryFromArCont(tempReview, assetOnInventory)

                db.setTransactionSuccessful()
            } catch (ex: Exception) {
                ex.printStackTrace()
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = "${getContext().getString(R.string.error_updating_asset_status)}: ${ex.message}",
                        taskStatus = ProgressStatus.crashed.id,
                        progress = 0,
                        total = 0
                    )
                )
                return@withContext false
            } finally {
                db.endTransaction()
            }
        }

        ///// Comienzo de una transacción /////
        db.beginTransaction()

        val arContDbHelper = AssetReviewContentDbHelper()
        try {
            if (tempReview.saveChanges()) {
                // Limpiar el contenido de la revisión ANTIGUA

                arContDbHelper.deleteByAssetReviewId(tempReview.collectorAssetReviewId)

                // Agregar el contenido de la revisión
                // Se agregan todos los activos, el sincronizador se encarga después
                // de NO enviar aquellos que no fueron revisados
                arContDbHelper.insert(ar = tempReview,
                    arCont = allAssets.toTypedArray(),
                    onSaveProgress = { onSaveProgress.invoke(it) })
            } else {
                // No es necesario settear error = true, termina acá.
                onSaveProgress.invoke(
                    SaveProgress(
                        msg = getContext().getString(R.string.failed_to_save_the_revision),
                        taskStatus = ProgressStatus.crashed.id,
                        progress = 0,
                        total = 0
                    )
                )
                return@withContext false
            }

            db.setTransactionSuccessful()

        } catch (ex: Exception) {
            ex.printStackTrace()
            onSaveProgress.invoke(
                SaveProgress(
                    msg = "${getContext().getString(R.string.failed_to_do_the_review)}: ${ex.message}",
                    taskStatus = ProgressStatus.crashed.id,
                    progress = 0,
                    total = 0
                )
            )
            return@withContext false
        } finally {
            db.endTransaction()
        }
        return@withContext true
    }
}