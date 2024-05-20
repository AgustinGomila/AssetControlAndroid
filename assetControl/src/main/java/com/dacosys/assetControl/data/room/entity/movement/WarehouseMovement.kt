package com.dacosys.assetControl.data.room.entity.movement

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.movement.WarehouseMovement.Entry
import com.dacosys.assetControl.data.room.repository.movement.WarehouseMovementRepository
import com.dacosys.assetControl.utils.Statics.Companion.toDate
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
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_ID) var warehouseMovementId: Long = 0L,
    @ColumnInfo(name = Entry.WAREHOUSE_MOVEMENT_DATE) var warehouseMovementDate: Date = Date(),
    @ColumnInfo(name = Entry.OBS) var obs: String? = null,
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_AREA_ID) var originWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.ORIGIN_WAREHOUSE_ID) var originWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED_DATE) var transferredDate: Date? = null,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_AREA_ID) var destinationWarehouseAreaId: Long = 0L,
    @ColumnInfo(name = Entry.DESTINATION_WAREHOUSE_ID) var destinationWarehouseId: Long = 0L,
    @ColumnInfo(name = Entry.COMPLETED) var completed: Int? = null,
    @Ignore var origWarehouseAreaStr: String = "",
    @Ignore var destWarehouseAreaStr: String = "",
) : Parcelable {

    fun saveChanges() = WarehouseMovementRepository().update(this)

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        warehouseMovementId = parcel.readLong(),
        warehouseMovementDate = parcel.readString().orEmpty().toDate(),
        obs = parcel.readString(),
        userId = parcel.readLong(),
        originWarehouseAreaId = parcel.readLong(),
        originWarehouseId = parcel.readLong(),
        transferredDate = parcel.readString().orEmpty().toDate(),
        destinationWarehouseAreaId = parcel.readLong(),
        destinationWarehouseId = parcel.readLong(),
        completed = parcel.readValue(Int::class.java.classLoader) as? Int,
        origWarehouseAreaStr = parcel.readString().orEmpty(),
        destWarehouseAreaStr = parcel.readString().orEmpty()
    )

    object Entry {
        const val TABLE_NAME = "warehouse_movement"
        const val ID = "_id"
        const val WAREHOUSE_MOVEMENT_ID = "warehouse_movement_id"
        const val WAREHOUSE_MOVEMENT_DATE = "warehouse_movement_date"
        const val OBS = "obs"
        const val USER_ID = "user_id"
        const val ORIGIN_WAREHOUSE_AREA_ID = "origin_warehouse_area_id"
        const val ORIGIN_WAREHOUSE_ID = "origin_warehouse_id"
        const val TRANSFERRED_DATE = "transferred_date"
        const val DESTINATION_WAREHOUSE_AREA_ID = "destination_warehouse_area_id"
        const val DESTINATION_WAREHOUSE_ID = "destination_warehouse_id"
        const val COMPLETED = "completed"

        const val ORIGIN_WAREHOUSE_AREA_STR = "origin_warehouse_area_str"
        const val ORIGIN_WAREHOUSE_STR = "origin_warehouse_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(warehouseMovementId)
        parcel.writeString(obs)
        parcel.writeLong(userId)
        parcel.writeLong(originWarehouseAreaId)
        parcel.writeLong(originWarehouseId)
        parcel.writeLong(destinationWarehouseAreaId)
        parcel.writeLong(destinationWarehouseId)
        parcel.writeValue(completed)
        parcel.writeString(origWarehouseAreaStr)
        parcel.writeString(destWarehouseAreaStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseMovement> {
        override fun createFromParcel(parcel: Parcel): WarehouseMovement {
            return WarehouseMovement(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseMovement?> {
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
            r.add("ALTER TABLE warehouse_movement RENAME TO warehouse_movement_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_movement`
            (
                `_id`                           INTEGER NOT NULL,
                `warehouse_movement_id`         INTEGER NOT NULL,
                `warehouse_movement_date`       INTEGER NOT NULL,
                `obs`                           TEXT,
                `user_id`                       INTEGER NOT NULL,
                `origin_warehouse_area_id`      INTEGER NOT NULL,
                `origin_warehouse_id`           INTEGER NOT NULL,
                `transferred_date`              INTEGER,
                `destination_warehouse_area_id` INTEGER NOT NULL,
                `destination_warehouse_id`      INTEGER NOT NULL,
                `completed`                     INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_movement (
                `_id`, `warehouse_movement_id`, `warehouse_movement_date`, `obs`, `user_id`,
                `origin_warehouse_area_id`, `origin_warehouse_id`, `transferred_date`,
                `destination_warehouse_area_id`, `destination_warehouse_id`, `completed`
            )
            SELECT
                `_id`, `warehouse_movement_id`, `warehouse_movement_date`, `obs`, `user_id`,
                `origin_warehouse_area_id`, `origin_warehouse_id`, `transfered_date`,
                `destination_warehouse_area_id`, `destination_warehouse_id`, `completed`
            FROM warehouse_movement_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_movement_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_warehouse_movement_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_origin_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_origin_warehouse_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_destination_warehouse_area_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_movement_destination_warehouse_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_warehouse_movement_id` ON `warehouse_movement` (`warehouse_movement_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_user_id` ON `warehouse_movement` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_area_id` ON `warehouse_movement` (`origin_warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_origin_warehouse_id` ON `warehouse_movement` (`origin_warehouse_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_area_id` ON `warehouse_movement` (`destination_warehouse_area_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_movement_destination_warehouse_id` ON `warehouse_movement` (`destination_warehouse_id`);")
            return r
        }
    }
}
