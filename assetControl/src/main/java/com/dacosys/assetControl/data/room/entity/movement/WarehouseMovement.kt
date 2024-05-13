package com.dacosys.assetControl.data.room.entity.movement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.WAREHOUSE_MOVEMENT_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_MOVEMENT_ID}"),
        Index(value = [Entry.USER_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"),
        Index(
            value = [Entry.ORIGIN_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_AREA_ID}"
        ),
        Index(value = [Entry.ORIGIN_WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ORIGIN_WAREHOUSE_ID}"),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.DESTINATION_WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESTINATION_WAREHOUSE_ID}"
        )
    ]
)
data class WarehouseMovement(
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) val warehouseMovementId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_DATE) val warehouseMovementDate: Date,
    @ColumnInfo(name = Entry.OBS) val obs: String?,
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) val originWarehouseAreaId: Long,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_ID) val originWarehouseId: Long,
    @ColumnInfo(name = Entry.TRANSFERED_DATE) val transferedDate: Date?,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_ID) val destinationWarehouseAreaId: Long,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_ID) val destinationWarehouseId: Long,
    @ColumnInfo(name = Entry.COMPLETED) val completed: Int?,
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long
) {
    object Entry {
        const val TABLE_NAME = "warehouse_movement"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val WAREHOUSE_MOVEMENT_DATE = "warehouse_movement_date"
        const val OBS = "obs"
        const val USER_ID = "user_id"
        const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"
        const val ORIGIN_WAREHOUSE_ID = "origin_warehouse_id"
        const val TRANSFERED_DATE = "transfered_date"
        const val DESTINATION_WAREHOUSE_AREA_ID = "destination_warehouse_area_id"
        const val DESTINATION_WAREHOUSE_ID = "destination_warehouse_id"
        const val COMPLETED = "completed"
    }
}
