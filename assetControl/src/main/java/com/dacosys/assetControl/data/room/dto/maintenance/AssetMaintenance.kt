package com.dacosys.assetControl.data.room.dto.maintenance

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.dacosys.assetControl.data.enums.maintenance.MaintenanceStatus
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.repository.maintenance.AssetMaintenanceRepository

class AssetMaintenance(
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_ID) var assetId: Long = 0L,
    @ColumnInfo(name = Entry.OBSERVATIONS) var observations: String? = null,
    @ColumnInfo(name = Entry.TRANSFERRED) var mTransferred: Int? = null,
    @ColumnInfo(name = Entry.MAINTENANCE_STATUS_ID) var maintenanceStatusId: Int = 0,
    @ColumnInfo(name = Entry.ASSET_MAINTENANCE_ID) var assetMaintenanceId: Long = 0L,
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_ID) var maintenanceTypeId: Long = 0L,
    @ColumnInfo(name = Entry.ASSET_STR) var assetStr: String? = null,
    @ColumnInfo(name = Entry.ASSET_CODE) var assetCode: String = "",
    @ColumnInfo(name = Entry.MAINTENANCE_TYPE_STR) var maintenanceTypeStr: String? = null,
) : Parcelable {

    @Ignore
    var transferred: Boolean = mTransferred == 1
        set(value) {
            mTransferred = if (value) 1 else 0
            field = value
        }

    @Ignore
    val maintenanceStatus: MaintenanceStatus =
        MaintenanceStatus.getById(maintenanceStatusId) ?: MaintenanceStatus.unknown

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        assetId = parcel.readLong(),
        observations = parcel.readString(),
        mTransferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        maintenanceStatusId = parcel.readInt(),
        assetMaintenanceId = parcel.readLong(),
        maintenanceTypeId = parcel.readLong(),
        assetStr = parcel.readString().orEmpty(),
        assetCode = parcel.readString().orEmpty(),
        maintenanceTypeStr = parcel.readString().orEmpty()
    )

    constructor(asset: Asset, obs: String, statusId: Int, maintenanceTypeId: Long) : this(
        assetId = asset.id,
        observations = obs,
        mTransferred = 0,
        maintenanceStatusId = statusId,
        assetMaintenanceId = 0,
        maintenanceTypeId = maintenanceTypeId,
        assetStr = asset.description,
        assetCode = asset.code,
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
        parcel.writeInt(maintenanceStatusId)
        parcel.writeLong(assetMaintenanceId)
        parcel.writeLong(maintenanceTypeId)
        parcel.writeString(assetStr)
        parcel.writeString(assetCode)
        parcel.writeString(maintenanceTypeStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetMaintenance

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
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
