package com.dacosys.assetControl.data.room.entity.maintenance

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.enums.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenance.Entry
import com.dacosys.assetControl.data.room.repository.maintenance.AssetMaintenanceRepository

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ASSET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_ID}"
        ),
        Index(
            value = [Entry.MAINTENANCE_STATUS_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MAINTENANCE_STATUS_ID}"
        ),
        Index(
            value = [Entry.ASSET_MAINTENANCE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ASSET_MAINTENANCE_ID}"
        ),
        Index(
            value = [Entry.MAINTENANCE_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MAINTENANCE_TYPE_ID}"
        ),
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        )
    ]
)
data class AssetMaintenance(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = Entry.OBSERVATIONS) var observations: String? = null,
    @ColumnInfo(name = Entry.TRANSFERRED) var mTransferred: Int? = null,
    @ColumnInfo(name = Entry.MAINTENANCE_STATUS_ID) var maintenanceStatusId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_MAINTENANCE_ID) var assetMaintenanceId: Long = 0L,
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_ID) var maintenanceTypeId: Long = 0L,
    @Ignore var assetStr: String = "",
    @Ignore var assetCode: String = "",
    @Ignore var maintenanceTypeStr: String = "",
) : Parcelable {

    @Ignore
    var transferred: Boolean = mTransferred == 1
        set(value) {
            mTransferred = if (value) 1 else 0
            field = value
        }

    @Ignore
    var statusId: Int = maintenanceStatusId.toInt()
        set(value) {
            maintenanceStatusId = value.toLong()
            field = value
        }

    @Ignore
    val maintenanceStatus: MaintenanceStatus = MaintenanceStatus.getById(statusId) ?: MaintenanceStatus.unknown

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetId = parcel.readLong(),
        observations = parcel.readString(),
        mTransferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        maintenanceStatusId = parcel.readLong(),
        assetMaintenanceId = parcel.readLong(),
        maintenanceTypeId = parcel.readLong(),
        assetStr = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        maintenanceTypeStr = parcel.readString().orEmpty()
    )

    constructor(asset: Asset, obs: String, statusId: Int, maintenanceTypeId: Long) : this(
        assetId = asset.id,
        assetStr = asset.description,
        assetCode = asset.code,
        observations = obs,
        maintenanceStatusId = statusId.toLong(),
        maintenanceTypeId = maintenanceTypeId,
        mTransferred = 0,
        assetMaintenanceId = 0,
    )

    fun saveChanges() = AssetMaintenanceRepository().update(this)

    object Entry {
        const val TABLE_NAME = "asset_maintenance_collector"
        const val ID = "_id"
        const val ASSET_ID = "asset_id"
        const val OBSERVATIONS = "observations"
        const val TRANSFERRED = "transferred"
        const val MAINTENANCE_STATUS_ID = "maintenance_status_id"
        const val ASSET_MAINTENANCE_ID = "asset_maintenance_id"
        const val MAINTENANCE_TYPE_ID = "maintenance_type_id"

        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val MAINTENANCE_TYPE_STR = "maintenance_type_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(assetId)
        parcel.writeString(observations)
        parcel.writeValue(mTransferred)
        parcel.writeLong(maintenanceStatusId)
        parcel.writeLong(assetMaintenanceId)
        parcel.writeLong(maintenanceTypeId)
        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeString(maintenanceTypeStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetMaintenance> {
        override fun createFromParcel(parcel: Parcel): AssetMaintenance {
            return AssetMaintenance(parcel)
        }

        override fun newArray(size: Int): Array<AssetMaintenance?> {
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
            r.add("ALTER TABLE asset_manteinance_collector RENAME TO asset_manteinance_collector_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `asset_maintenance`
            (
                `_id`                   INTEGER NOT NULL,
                `asset_id`              INTEGER NOT NULL,
                `observations`          TEXT,
                `transferred`           INTEGER,
                `maintenance_status_id` INTEGER NOT NULL,
                `asset_maintenance_id`  INTEGER NOT NULL,
                `maintenance_type_id`   INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO asset_maintenance (
                _id, asset_id, observations, transferred,
                maintenance_status_id, asset_maintenance_id,
                maintenance_type_id
            )
            SELECT
                _id, asset_id, observations, transfered,
                manteinance_status_id, asset_manteinance_id,
                manteinance_type_id
            FROM asset_manteinance_collector_temp
        """.trimIndent()
            )
            r.add("DROP TABLE asset_manteinance_collector_temp")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_manteinance_status_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_asset_manteinance_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector_manteinance_type_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_asset_manteinance_collector__id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_id` ON `asset_maintenance` (`asset_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_status_id` ON `asset_maintenance` (`maintenance_status_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_asset_maintenance_id` ON `asset_maintenance` (`asset_maintenance_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance_maintenance_type_id` ON `asset_maintenance` (`maintenance_type_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_asset_maintenance__id` ON `asset_maintenance` (`_id`);")
            return r
        }
    }
}
