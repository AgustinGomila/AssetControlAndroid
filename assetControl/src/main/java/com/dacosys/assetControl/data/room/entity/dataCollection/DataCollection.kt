package com.dacosys.assetControl.data.room.entity.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollection.Entry
import com.dacosys.assetControl.data.room.repository.dataCollection.DataCollectionContentRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
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
data class DataCollection(
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
    @Ignore var assetStr: String = "",
    @Ignore var assetCode: String = "",
    @Ignore var warehouseStr: String = "",
    @Ignore var warehouseAreaStr: String = "",
) : Parcelable {

    @Ignore
    private var contentsRead: Boolean = false

    @Ignore
    private var mContents: ArrayList<DataCollectionContent> = arrayListOf()

    fun contents(): ArrayList<DataCollectionContent> {
        if (contentsRead) return mContents
        else {
            mContents = ArrayList(DataCollectionContentRepository().selectByDataCollectionId(this.id))
            contentsRead = true
            return mContents
        }
    }

    constructor(parcel: Parcel) : this(
        dataCollectionId = parcel.readLong(),
        assetId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseId = parcel.readValue(Long::class.java.classLoader) as? Long,
        warehouseAreaId = parcel.readValue(Long::class.java.classLoader) as? Long,
        userId = parcel.readLong(),
        dateStart = parcel.readString().orEmpty().toDate(),
        dateEnd = parcel.readString().orEmpty().toDate(),
        completed = parcel.readInt(),
        transferredDate = parcel.readString().orEmpty().toDate(),
        id = parcel.readLong(),
        collectorRouteProcessId = parcel.readLong(),
        assetStr = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        warehouseStr = parcel.readString().orEmpty(),
        warehouseAreaStr = parcel.readString().orEmpty()
    )

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
        const val TRANSFERRED_DATE = "transferred_date"
        const val ID = "_id"
        const val COLLECTOR_ROUTE_PROCESS_ID = "collector_route_process_id"

        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val WAREHOUSE_STR = "warehouse_str"
        const val WAREHOUSE_AREA_STR = "warehouse_area_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionId)
        parcel.writeValue(assetId)
        parcel.writeValue(warehouseId)
        parcel.writeValue(warehouseAreaId)
        parcel.writeLong(userId)
        parcel.writeString(dateStart?.toString())
        parcel.writeString(dateEnd?.toString())
        parcel.writeInt(completed)
        parcel.writeString(transferredDate?.toString())
        parcel.writeLong(id)
        parcel.writeLong(collectorRouteProcessId)
        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeString(warehouseStr)
        parcel.writeString(warehouseAreaStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollection> {
        override fun createFromParcel(parcel: Parcel): DataCollection {
            return DataCollection(parcel)
        }

        override fun newArray(size: Int): Array<DataCollection?> {
            return arrayOfNulls(size)
        }

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

