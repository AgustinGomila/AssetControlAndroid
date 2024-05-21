package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollection.Entry
import java.util.*

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
data class DataCollectionEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long? = null,
    @ColumnInfo(name = Entry.WAREHOUSE_AREA_ID) var warehouseAreaId: Long? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.DATE_START) var dateStart: Date? = null,
    @ColumnInfo(name = Entry.DATE_END) var dateEnd: Date? = null,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int = 0,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.COLLECTOR_ROUTE_PROCESS_ID) var collectorRouteProcessId: Long = 0L,
) {
    constructor(d: DataCollection) : this(
        dataCollectionId = d.dataCollectionId,
        assetId = d.assetId,
        warehouseId = d.warehouseId,
        warehouseAreaId = d.warehouseAreaId,
        userId = d.userId,
        dateStart = d.dateStart,
        dateEnd = d.dateEnd,
        completed = d.completed,
        transferredDate = d.transferredDate,
        id = d.id,
        collectorRouteProcessId = d.collectorRouteProcessId,
    )

    companion object {
        /**
         * Migration zero
         * Migración desde la base de datos SQLite (version 0) a la primera versión de Room.
         * No utilizar constantes para la definición de nombres para evitar incoherencias en el futuro.
         * @return
         */
        fun migrationZero(): List<String> {
            val r: ArrayList<String> = arrayListOf()
            r.add("ALTER TABLE data_collection RENAME TO data_collection_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `data_collection`
            (
                `data_collection_id`         INTEGER NOT NULL,
                `asset_id`                   INTEGER,
                `warehouse_id`               INTEGER,
                `warehouse_area_id`          INTEGER,
                `user_id`                    INTEGER NOT NULL,
                `date_start`                 INTEGER,
                `date_end`                   INTEGER,
                `completed`                  INTEGER NOT NULL,
                `transferred_date`           INTEGER,
                `_id`                        INTEGER NOT NULL,
                `collector_route_process_id` INTEGER NOT NULL,
                PRIMARY KEY (`data_collection_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO data_collection (
                data_collection_id, asset_id, warehouse_id, warehouse_area_id,
                user_id, date_start, date_end,
                completed, transferred_date, _id,
                collector_route_process_id
            )
            SELECT
               data_collection_id, asset_id, warehouse_id, warehouse_area_id,
                user_id, date_start, date_end,
                completed, transfered_date, _id,
                collector_route_process_id
            FROM data_collection_temp
        """.trimIndent()
            )
            r.add("DROP TABLE data_collection_temp")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_data_collection_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_asset_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_collector_route_process_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_data_collection_id` ON `data_collection` (`data_collection_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_asset_id` ON `data_collection` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_warehouse_id` ON `data_collection` (`warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_warehouse_area_id` ON `data_collection` (`warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_user_id` ON `data_collection` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection__id` ON `data_collection` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_collector_route_process_id` ON `data_collection` (`collector_route_process_id`);")
            return r
        }
    }
}