package com.dacosys.assetControl.data.room.repository.maintenance

import com.dacosys.assetControl.data.room.dao.maintenance.AssetMaintenanceDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.dto.maintenance.AssetMaintenance
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenanceEntity
import kotlinx.coroutines.runBlocking

class AssetMaintenanceRepository {
    private val dao: AssetMaintenanceDao
        get() = database.assetMaintenanceCollectorDao()

    fun select(onlyActive: Boolean): List<AssetMaintenance> = runBlocking {
        if (onlyActive) dao.selectActive()
        else dao.select()
    }

    fun selectNoTransferred() = runBlocking { dao.selectNoTransferred() }

    fun selectByAssetIdNotTransferred(assetId: Long) = runBlocking { dao.selectByAssetIdNotTransferred(assetId) }

    private val nextId: Long
        get() = runBlocking { (dao.selectMaxId() ?: 0) + 1 }


    fun insert(maintenance: AssetMaintenance) = runBlocking {
        maintenance.id = nextId
        maintenance.mTransferred = 0
        dao.insert(AssetMaintenanceEntity(maintenance))
    }


    fun update(maintenance: AssetMaintenance) = runBlocking {
        maintenance.mTransferred = 0
        dao.update(AssetMaintenanceEntity(maintenance))
    }

    fun updateTransferred(id: Long) = runBlocking {
        dao.updateTransferred(id)
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
    ) = runBlocking {
        dao.getMultiQuery(
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
    }
}
