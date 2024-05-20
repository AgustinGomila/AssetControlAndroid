package com.dacosys.assetControl.data.room.repository.movement

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.room.dao.movement.WarehouseMovementContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovementContent
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.runBlocking

class WarehouseMovementContentRepository {
    private val dao: WarehouseMovementContentDao
        get() = database.warehouseMovementContentDao()

    fun selectByWarehouseMovementId(wmId: Long) = runBlocking { dao.selectByWarehouseMovementId(wmId) }


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
                            getContext().getString(R.string.adding_asset_),
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


    fun update(content: WarehouseMovementContent) = runBlocking {
        dao.update(content)
    }

    fun updateAssetId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateAssetId(newValue, oldValue)
        }
    }

    fun deleteTransferred() = runBlocking {
        dao.deleteTransferred()
    }
}