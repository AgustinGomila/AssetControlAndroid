package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}"
        ),
        Index(
            value = [Entry.WAREHOUSE_AREA_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_AREA_ID}"
        ),
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.COLLECTOR_ROUTE_PROCESS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.COLLECTOR_ROUTE_PROCESS_ID}"
        )
    ]
)
data class DataCollection(
    @PrimaryKey
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) val dataCollectionId: Long,
    @ColumnInfo(name = Entry.ASSET_ID) val assetId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) val warehouseId: Long,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) val warehouseAreaId: Long,
    @ColumnInfo(name = Entry.USER_ID) val userId: Long,
    @ColumnInfo(name = Entry.DATE_START) val dateStart: Long?,
    @ColumnInfo(name = Entry.DATE_END) val dateEnd: Long?,
    @ColumnInfo(name = Entry.COMPLETED) val completed: Int,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) val transferredDate: Long?,
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.COLLECTOR_ROUTE_PROCESS_ID) val collectorRouteProcessId: Long
) {
    object Entry {
        const val TABLE_NAME = "data_collection"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val ASSET_ID = "asset_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val WAREHOUSE_AREA_ID = "warehouse_area_id"
        const val USER_ID = "user_id"
        const val DATE_START = "date_start"
        const val DATE_END = "date_end"
        const val COMPLETED = "completed"
        const val TRANSFERRED_DATE = "transfered_date"
        const val ID = "_id"
        const val COLLECTOR_ROUTE_PROCESS_ID = "collector_route_process_id"
    }
}

