package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.dao.review.AssetReviewDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import kotlinx.coroutines.runBlocking
import java.util.*

class AssetReviewRepository {
    private val dao: AssetReviewDao
        get() = database.assetReviewDao()

    fun selectById(id: Long) = dao.selectById(id)

    fun selectByDescription(wDescription: String, waDescription: String, userId: Long, onlyActive: Boolean) =
        if (onlyActive) dao.selectByDescriptionActive(wDescription, waDescription, userId)
        else dao.selectByDescription(wDescription, waDescription, userId)

    fun selectByCompleted(userId: Long) = dao.selectByCompleted(userId = userId)


    private val nextId: Long
        get() = (dao.selectMaxId() ?: 0) + 1


    fun insert(warehouseArea: WarehouseArea): Long {
        val nextId = nextId
        val userId = getUserId() ?: return 0

        runBlocking {
            val assetReview = AssetReview(
                id = nextId,
                assetReviewDate = Date(),
                obs = "",
                userId = userId,
                warehouseAreaId = warehouseArea.id,
                warehouseId = warehouseArea.warehouseId,
                modificationDate = Date(),
                statusId = AssetReviewStatus.onProcess.id,
                warehouseAreaStr = warehouseArea.description,
                warehouseStr = warehouseArea.warehouseStr,
            )
            dao.insert(assetReview)
        }

        return nextId
    }


    fun updateWarehouseAreaId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateWarehouseAreaId(newValue, oldValue)
        }
    }

    fun updateWarehouseId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateWarehouseId(newValue, oldValue)
        }
    }

    fun updateTransferredNew(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateId(newValue, oldValue)
        }
    }


    fun deleteById(id: Long) = runBlocking {
        dao.deleteById(id)
    }

    fun deleteTransferred() = runBlocking {
        AssetReviewContentRepository().deleteTransferred()
        dao.deleteTransferred()
    }
}
