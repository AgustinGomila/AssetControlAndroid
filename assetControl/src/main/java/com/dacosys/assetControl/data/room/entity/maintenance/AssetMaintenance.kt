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
        const val TRANSFERRED = "transfered"
        const val MAINTENANCE_STATUS_ID = "maintenance_status_id"
        const val ASSET_MAINTENANCE_ID = "asset_maintenance_id"
        const val MAINTENANCE_TYPE_ID = "maintenance_type_id"

        const val ASSET_STR = "asset_str"
        const val ASSET_CODE = "asset_code"
        const val MANTEINANCE_TYPE_STR = "manteinance_type_str"
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
    }
}
