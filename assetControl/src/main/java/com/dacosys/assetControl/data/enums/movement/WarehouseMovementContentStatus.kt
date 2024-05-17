package com.dacosys.assetControl.data.enums.movement

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class WarehouseMovementContentStatus(val id: Int, val description: String) {
    companion object {
        var toMove = WarehouseMovementContentStatus(
            id = 0,
            description = getContext().getString(R.string.to_move)
        )
        var noNeedToMove = WarehouseMovementContentStatus(
            id = 3,
            description = getContext().getString(R.string.no_need_to_move)
        )

        fun getAll(): List<WarehouseMovementContentStatus> {
            return listOf(toMove, noNeedToMove)
        }

        fun getById(id: Int): WarehouseMovementContentStatus? {
            return getAll().firstOrNull { it.id == id }
        }
    }
}