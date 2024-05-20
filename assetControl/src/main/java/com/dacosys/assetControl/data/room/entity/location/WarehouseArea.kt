package com.dacosys.assetControl.data.room.entity.location

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea.Entry
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.DESCRIPTION], name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"),
        Index(value = [Entry.WAREHOUSE_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.WAREHOUSE_ID}")
    ]
)
data class WarehouseArea(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.WAREHOUSE_ID) var warehouseId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @Ignore var warehouseStr: String = ""
) : Parcelable {

    @Ignore
    var active: Boolean = mActive == 1
        set(value) {
            mActive = if (value) 1 else 0
            field = value
        }

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        mActive = parcel.readInt(),
        warehouseId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        warehouseStr = parcel.readString().orEmpty()
    )

    constructor(waObj: WarehouseAreaObject) : this(
        id = waObj.warehouse_area_id,
        description = waObj.description,
        mActive = waObj.active,
        warehouseId = waObj.warehouse_id,
        transferred = 1,
    )

    object Entry {
        const val TABLE_NAME = "warehouse_area"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val WAREHOUSE_ID = "warehouse_id"
        const val TRANSFERRED = "transferred"

        const val WAREHOUSE_STR = "warehouse_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(mActive)
        parcel.writeLong(warehouseId)
        parcel.writeValue(transferred)
        parcel.writeString(warehouseStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WarehouseArea> {
        override fun createFromParcel(parcel: Parcel): WarehouseArea {
            return WarehouseArea(parcel)
        }

        override fun newArray(size: Int): Array<WarehouseArea?> {
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
            r.add("ALTER TABLE warehouse_area RENAME TO warehouse_area_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `warehouse_area`
            (
                `_id`          INTEGER NOT NULL,
                `description`  TEXT    NOT NULL,
                `active`       INTEGER NOT NULL,
                `warehouse_id` INTEGER NOT NULL,
                `transferred`  INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO warehouse_area (
                `_id`, `description`, `active`,
                `warehouse_id`, `transferred` 
            )
            SELECT
                `_id`, `description`, `active`,
                `warehouse_id`, `transferred`
            FROM warehouse_area_temp
        """.trimIndent()
            )
            r.add("DROP TABLE warehouse_area_temp")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_area_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_warehouse_area_warehouse_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_description` ON `warehouse_area` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_warehouse_area_warehouse_id` ON `warehouse_area` (`warehouse_id`)")
            return r
        }
    }
}

