package com.example.assetControl.data.room.repository.asset

import com.dacosys.imageControl.ui.activities.UTCDataTime
import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.asset.AssetStatus
import com.example.assetControl.data.room.dao.asset.AssetDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.asset.Asset
import com.example.assetControl.data.room.dto.location.WarehouseArea
import com.example.assetControl.data.room.dto.movement.WarehouseMovement
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.entity.asset.AssetEntity
import com.example.assetControl.data.room.repository.location.WarehouseAreaRepository
import com.example.assetControl.data.webservice.asset.AssetObject
import com.example.assetControl.network.sync.SyncProgress
import com.example.assetControl.network.sync.SyncRegistryType
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AssetRepository {
    private val dao: AssetDao
        get() = database.assetDao()

    fun selectByTempIds(): List<Asset> = runBlocking {
        val tempAssets = TempAssetRepository().select()
        val ids = tempAssets.map { it.tempId }.toList()
        dao.selectByTempIds(ids)
    }

    fun selectAllCodes() = runBlocking { dao.selectDistinctCodes() }

    fun selectAllCodesByWarehouseAreaId(warehouseAreaId: Long) = runBlocking {
        dao.selectDistinctCodesByWarehouseAreaId(warehouseAreaId)
    }

    fun selectAllSerials() = runBlocking { dao.selectDistinctSerials() }

    fun selectById(id: Long) = runBlocking { dao.selectById(id) }

    fun selectByCode(code: String) = runBlocking { dao.selectByCode(code) }

    fun selectBySerial(serial: String) = runBlocking { dao.selectBySerialNumber(serial) }

    fun selectByEan(ean: String) = runBlocking { dao.selectByEan(ean) }

    fun selectNoTransferred() = runBlocking { ArrayList(dao.selectNoTransferred()) }

    fun select(onlyActive: Boolean): List<Asset> = runBlocking {
        if (onlyActive) dao.selectActive()
        dao.select()
    }

    fun selectByWarehouseAreaIdActiveNotRemoved(warehouseAreaId: Long) = runBlocking {
        dao.selectByWarehouseAreaIdActiveNotRemoved(warehouseAreaId)
    }

    fun codeExists(code: String, assetId: Long): Boolean {
        val r = runBlocking {
            codeExistsSuspend(code, assetId)
        }
        return r
    }

    private suspend fun codeExistsSuspend(code: String, assetId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            dao.codeExists(code, assetId)
        } == 1
    }

    private val nextLastId: Long
        get() = runBlocking {
            val minId = dao.selectMinId() ?: 0
            if (minId > 0) -1 else minId - 1
        }


    fun insert(asset: Asset) = runBlocking {
        asset.transferred = 0
        asset.id = nextLastId
        dao.insert(AssetEntity(asset))
    }


    suspend fun sync(
        assetsObj: Array<AssetObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        count: Int = 0,
        total: Int = 0,
    ) {
        val registryType = SyncRegistryType.Asset

        val assets: ArrayList<Asset> = arrayListOf()
        assetsObj.mapTo(assets) { Asset(it) }
        val partial = assets.count()

        withContext(Dispatchers.IO) {
            dao.insert(assets) {
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = partial + total,
                        completedTask = it + count,
                        msg = context.getString(R.string.synchronizing_assets),
                        registryType = registryType,
                        progressStatus = ProgressStatus.running
                    )
                )
            }
        }
    }


    fun update(asset: Asset) = runBlocking {
        asset.transferred = 0
        dao.update(AssetEntity(asset))
    }

    fun updateTransferred(ids: List<Long>): Boolean {
        val r = runBlocking {
            dao.updateTransferred(ids.toTypedArray())
            true
        }
        return r
    }

    fun updateId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateId(newValue, oldValue)
    }

    fun updateWarehouseId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateWarehouseId(newValue, oldValue)
    }

    fun updateWarehouseAreaId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateWarehouseAreaId(newValue, oldValue)
    }

    fun updateItemCategoryId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateItemCategoryId(newValue, oldValue)
    }

    @Throws(Exception::class)
    fun getBy(
        ean: String = "",
        description: String = "",
        code: String = "",
        serialNumber: String = "",
        itemCategoryId: Long? = null,
        warehouseId: Long? = null,
        warehouseAreaId: Long? = null,
        useLike: Boolean = true,
        onlyActive: Boolean = true,
    ) = dao.getMultiQuery(
        ean = ean,
        description = description,
        code = code,
        serialNumber = serialNumber,
        itemCategoryId = itemCategoryId,
        warehouseId = warehouseId,
        warehouseAreaId = warehouseAreaId,
        useLike = useLike,
        onlyActive = onlyActive
    )


    fun setMissing(assets: ArrayList<AssetReviewContent>): Boolean {
        val assetMissing =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.missing.id && it.contentStatusId != AssetStatus.unknown.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        val r = runBlocking {
            dao.updateMissing(ids = assetMissing.toTypedArray(), date = date)
            true
        }
        return r
    }

    fun setOnInventoryFromArCont(
        ar: AssetReview,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        // Si los activos están eliminados (Dados de baja) no vuelven a estar en Inventario,
        // solo se actualiza su ubicación, pero no cambia su estado.
        val assetOnInventory =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.unknown.id && it.contentStatusId != AssetStatus.removed.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        runBlocking {
            dao.updateOnInventory(
                ids = assetOnInventory.toTypedArray(),
                warehouseId = ar.warehouseId,
                warehouseAreaId = ar.warehouseAreaId,
                date = date
            )
            true
        }

        val assetRemoved =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.unknown.id && it.contentStatusId == AssetStatus.removed.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateOnInventoryRemoved(
                ids = assetRemoved.toTypedArray(),
                warehouseId = ar.warehouseId,
                warehouseAreaId = ar.warehouseAreaId,
                date = date
            )
            true
        }

        return r
    }

    fun setNewLocationFromArCont(
        wa: WarehouseArea,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        // Actualizando ubicación de los activos
        val existingAssets =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.unknown.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateLocation(
                ids = existingAssets.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id
            )
            true
        }

        return r
    }

    fun setNewLocationFromWmCont(
        wa: WarehouseArea,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        // Actualizando ubicación de los activos
        val existingAssets =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId != AssetStatus.unknown.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateLocation(
                ids = existingAssets.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id
            )
            true
        }

        return r
    }

    fun setOnInventoryFromWmCont(
        wa: WarehouseArea,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        // Si los activos están eliminados (Dados de baja) no vuelven a estar en Inventario,
        // solo se actualiza su ubicación, pero no cambia su estado.
        val assetOnInventory =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId != AssetStatus.unknown.id && it.assetStatusId != AssetStatus.removed.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        runBlocking {
            dao.updateOnInventory(
                ids = assetOnInventory.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id,
                date = date
            )
            true
        }

        val assetRemoved =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId != AssetStatus.unknown.id && it.assetStatusId == AssetStatus.removed.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateOnInventoryRemoved(
                ids = assetRemoved.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id,
                date = date
            )
            true
        }

        return r
    }

    fun setOnInventoryFromArea(
        warehouseAreaId: Long,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        val wa = WarehouseAreaRepository().selectById(warehouseAreaId) ?: return false

        val assetOnInventory =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId == AssetStatus.missing.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        val r = runBlocking {
            dao.updateOnInventory(
                ids = assetOnInventory.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id,
                date = date
            )
            true
        }
        return r
    }

    fun setOnInventoryFromArCont(
        wa: WarehouseArea,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        // Si los activos están eliminados (Dados de baja) no vuelven a estar en Inventario,
        // solo se actualiza su ubicación, pero no cambia su estado.
        val assetOnInventory =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.unknown.id && it.contentStatusId != AssetStatus.removed.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        runBlocking {
            dao.updateOnInventory(
                ids = assetOnInventory.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id,
                date = date
            )
            true
        }

        val assetRemoved =
            ArrayList(assets.mapNotNull {
                if (it.contentStatusId != AssetStatus.unknown.id && it.contentStatusId == AssetStatus.removed.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateOnInventoryRemoved(
                ids = assetRemoved.toTypedArray(),
                warehouseId = wa.warehouseId,
                warehouseAreaId = wa.id,
                date = date
            )
            true
        }

        return r
    }

    fun setOnInventoryFromWmCont(
        ar: WarehouseMovement,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        // Si los activos están eliminados (Dados de baja) no vuelven a estar en Inventario,
        // solo se actualiza su ubicación, pero no cambia su estado.
        val assetOnInventory =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId != AssetStatus.unknown.id && it.assetStatusId != AssetStatus.removed.id) it.assetId
                else null
            })

        val date = UTCDataTime.getUTCDateTimeAsString()
        runBlocking {
            dao.updateOnInventory(
                ids = assetOnInventory.toTypedArray(),
                warehouseId = ar.destinationWarehouseId,
                warehouseAreaId = ar.destinationWarehouseAreaId,
                date = date
            )
            true
        }

        val assetRemoved =
            ArrayList(assets.mapNotNull {
                if (it.assetStatusId != AssetStatus.unknown.id && it.assetStatusId == AssetStatus.removed.id) it.assetId
                else null
            })

        val r = runBlocking {
            dao.updateOnInventoryRemoved(
                ids = assetRemoved.toTypedArray(),
                warehouseId = ar.destinationWarehouseId,
                warehouseAreaId = ar.destinationWarehouseAreaId,
                date = date
            )
            true
        }

        return r
    }
}