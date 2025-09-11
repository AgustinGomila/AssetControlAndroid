package com.example.assetControl.data.room.repository.movement

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.room.dao.movement.WarehouseMovementContentDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.movement.WarehouseMovement
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.runBlocking

class WarehouseMovementContentRepository {
    private val dao: WarehouseMovementContentDao
        get() = database.warehouseMovementContentDao()

    fun selectByWarehouseMovementId(wmId: Long) = runBlocking { dao.selectByWarehouseMovementId(wmId) }

    val maxId get() = runBlocking { dao.selectMaxId() ?: 0 }


    fun insert(
        movement: WarehouseMovement,
        contents: List<WarehouseMovementContent>,
        progress: (SaveProgress) -> Unit
    ) {
        insert(movement.id, contents, progress)
    }

    fun insert(id: Long, contents: List<WarehouseMovementContent>, progress: (SaveProgress) -> Unit) {
        runBlocking {
            // Set new ID
            contents.forEach { it.warehouseMovementId = id }

            val total = contents.size
            dao.insert(contents) {
                val asset = contents[it - 1]
                progress.invoke(
                    SaveProgress(
                        msg = String.format(
                            context.getString(R.string.adding_asset_),
                            asset.code
                        ),
                        taskStatus = ProgressStatus.running.id,
                        progress = it,
                        total = total
                    )
                )
            }
        }
    }


    fun updateAssetId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateAssetId(newValue, oldValue)
        }
    }

    fun updateMovementId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateMovementId(newValue, oldValue)
        }
    }


    fun deleteTransferred() = runBlocking {
        dao.deleteTransferred()
    }
}