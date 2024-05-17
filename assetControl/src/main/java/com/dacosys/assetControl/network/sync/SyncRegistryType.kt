package com.dacosys.assetControl.network.sync


import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.settings.entries.ConfEntry
import com.dacosys.assetControl.utils.settings.preferences.Repository
import java.util.*

class SyncRegistryType : Parcelable {
    var id: Int = 0
    var description: String = ""
    var confEntry: ConfEntry? = null

    constructor(id: Int, description: String) {
        this.description = description
        this.id = id
        this.confEntry = null
    }

    constructor(id: Int, description: String, confEntry: ConfEntry) {
        this.description = description
        this.id = id
        this.confEntry = confEntry
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is SyncRegistryType) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString().orEmpty()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SyncRegistryType> {
        override fun createFromParcel(parcel: Parcel): SyncRegistryType {
            return SyncRegistryType(parcel)
        }

        override fun newArray(size: Int): Array<SyncRegistryType?> {
            return arrayOfNulls(size)
        }

        val Warehouse =
            SyncRegistryType(1, getContext().getString(R.string.warehouses), ConfEntry.acLastUpdateWarehouse)
        val Asset = SyncRegistryType(2, getContext().getString(R.string.assets), ConfEntry.acLastUpdateAsset)
        val User = SyncRegistryType(3, getContext().getString(R.string.users), ConfEntry.acLastUpdateUser)
        val WarehouseArea =
            SyncRegistryType(4, getContext().getString(R.string.areas), ConfEntry.acLastUpdateWarehouseArea)
        val ItemCategory =
            SyncRegistryType(5, getContext().getString(R.string.categories), ConfEntry.acLastUpdateItemCategory)
        val AssetReview =
            SyncRegistryType(6, getContext().getString(R.string.asset_reviews), ConfEntry.acLastUpdateAssetReview)
        val WarehouseMovement =
            SyncRegistryType(7, getContext().getString(R.string.movements), ConfEntry.acLastUpdateWarehouseMovement)

        /* var ActionLog = SyncRegistryType(8, "Registro de acciones", ConfEntry.acLastUpdateActionLog) */
        val Provider = SyncRegistryType(9, getContext().getString(R.string.providers), ConfEntry.acLastUpdateProviders)
        val Repairman =
            SyncRegistryType(10, getContext().getString(R.string.manteinance_user), ConfEntry.acLastUpdateRepairman)
        val Repairshop =
            SyncRegistryType(11, getContext().getString(R.string.repairshop), ConfEntry.acLastUpdateRepairshop)
        val AssetMaintenance =
            SyncRegistryType(12, getContext().getString(R.string.manteinance), ConfEntry.acLastUpdateAssetMaintenance)
        val AssetMaintenanceLog = SyncRegistryType(
            13,
            getContext().getString(R.string.manteinance_log),
            ConfEntry.acLastUpdateAssetMaintenanceLog
        )
        val MaintenanceType = SyncRegistryType(
            14,
            getContext().getString(R.string.manteinance_type),
            ConfEntry.acLastUpdateMaintenanceType
        )
        val MaintenanceTypeGroup = SyncRegistryType(
            15,
            getContext().getString(R.string.manteinance_group),
            ConfEntry.acLastUpdateMaintenanceTypeGroup
        )
        val CostCentre =
            SyncRegistryType(16, getContext().getString(R.string.cost_centre), ConfEntry.acLastUpdateCostCentre)
        val AssetMaintenanceProgramed = SyncRegistryType(
            17,
            getContext().getString(R.string.programed_manteinance),
            ConfEntry.acLastUpdateAssetMaintenanceProgramed
        )
        val DataCollectionRule =
            SyncRegistryType(18, getContext().getString(R.string.rules), ConfEntry.acLastUpdateDataCollectionRule)
        val Attribute =
            SyncRegistryType(19, getContext().getString(R.string.attributes), ConfEntry.acLastUpdateAttribute)
        val AttributeCategory = SyncRegistryType(
            20,
            getContext().getString(R.string.attribute_categories),
            ConfEntry.acLastUpdateAttributeCategory
        )
        val DataCollection =
            SyncRegistryType(21, getContext().getString(R.string.data_collection), ConfEntry.acLastUpdateDataCollection)
        val Route = SyncRegistryType(22, getContext().getString(R.string.routes), ConfEntry.acLastUpdateRoute)
        val RouteProcess =
            SyncRegistryType(23, getContext().getString(R.string.route_process), ConfEntry.acLastUpdateRouteProcess)
        val BarcodeLabelCustom =
            SyncRegistryType(24, getContext().getString(R.string.labels), ConfEntry.acLastUpdateBarcodeLabelCustom)
        val UserWarehouseArea = SyncRegistryType(
            25,
            getContext().getString(R.string.user_warehouse_areas),
            ConfEntry.acLastUpdateUserWarehouseArea
        )
        val AttributeComposition = SyncRegistryType(26, getContext().getString(R.string.attribute_composition))
        val RouteComposition = SyncRegistryType(27, getContext().getString(R.string.route_composition))
        val DataCollectionRuleContent = SyncRegistryType(28, getContext().getString(R.string.rule_content))
        val DataCollectionRuleTarget = SyncRegistryType(29, getContext().getString(R.string.rule_target))
        val AssetReviewStatus = SyncRegistryType(100, getContext().getString(R.string.asset_review_status))
        val RouteProcessStatus = SyncRegistryType(101, getContext().getString(R.string.route_process_status))
        val ManteinanceStatus = SyncRegistryType(102, getContext().getString(R.string.manteinance_status))
        val BarcodeLabelTarget = SyncRegistryType(103, getContext().getString(R.string.label_target))
        val UserPermission = SyncRegistryType(104, getContext().getString(R.string.user_permissions))
        val Image = SyncRegistryType(999, getContext().getString(R.string.images))

        fun getAll(): ArrayList<SyncRegistryType> {
            val allSections = ArrayList<SyncRegistryType>()
            Collections.addAll(
                allSections,
                Warehouse,
                Asset,
                User,
                WarehouseArea,
                ItemCategory,
                AssetReview,
                WarehouseMovement,
                Provider,
                Repairman,
                Repairshop,
                AssetMaintenance,
                AssetMaintenanceLog,
                MaintenanceType,
                MaintenanceTypeGroup,
                CostCentre,
                AssetMaintenanceProgramed,
                DataCollectionRule,
                Attribute,
                AttributeCategory,
                DataCollection,
                Route,
                RouteProcess,
                BarcodeLabelCustom,
                UserWarehouseArea,
                AttributeComposition,
                RouteComposition,
                DataCollectionRuleContent,
                DataCollectionRuleTarget,

                // LOCALES
                UserPermission,

                // ESTATICAS
                AssetReviewStatus,
                RouteProcessStatus,
                ManteinanceStatus,
                BarcodeLabelTarget,
            )

            if (Repository.useImageControl) allSections.add(Image)

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(syncRegistryTypeId: Int): SyncRegistryType? {
            return getAll().firstOrNull { it.id == syncRegistryTypeId }
        }

        fun getSyncDownload(): ArrayList<SyncRegistryType> {
            val allSections = ArrayList<SyncRegistryType>()
            Collections.addAll(
                allSections,
                User,
                Asset,
                ItemCategory,
                Warehouse,
                WarehouseArea,
                Attribute,
                AttributeCategory,
                Route,
                DataCollectionRule,
                BarcodeLabelCustom
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getSyncUpload(): ArrayList<SyncRegistryType> {
            val allSections = ArrayList<SyncRegistryType>()
            Collections.addAll(
                allSections,
                Warehouse,
                Asset,
                WarehouseArea,
                ItemCategory,
                AssetReview,
                WarehouseMovement,
                AssetMaintenance,
                DataCollection,
                RouteProcess
            )

            if (Repository.useImageControl) allSections.add(Image)

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getSyncAsString(): ArrayList<String> {
            val allSections = ArrayList<String>()
            Collections.addAll(
                allSections,
                Warehouse.id.toString(),
                Asset.id.toString(),
                WarehouseArea.id.toString(),
                ItemCategory.id.toString(),
                AssetReview.id.toString(),
                WarehouseMovement.id.toString(),
                AssetMaintenance.id.toString(),
                DataCollection.id.toString(),
                RouteProcess.id.toString()
            )

            if (Repository.useImageControl) allSections.add(Image.id.toString())

            return ArrayList(allSections.sortedWith(compareBy { it }))
        }
    }
}