package com.dacosys.assetControl.sync.functions


import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.configuration.entries.ConfEntry
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
        description = parcel.readString() ?: ""
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

        var Warehouse = SyncRegistryType(
            1,
            Statics.AssetControl.getContext().getString(R.string.warehouses),
            ConfEntry.acLastUpdateWarehouse
        )
        var Asset = SyncRegistryType(
            2,
            Statics.AssetControl.getContext().getString(R.string.assets),
            ConfEntry.acLastUpdateAsset
        )
        var User = SyncRegistryType(
            3,
            Statics.AssetControl.getContext().getString(R.string.users),
            ConfEntry.acLastUpdateUser
        )
        var WarehouseArea = SyncRegistryType(
            4,
            Statics.AssetControl.getContext().getString(R.string.areas),
            ConfEntry.acLastUpdateWarehouseArea
        )
        var ItemCategory = SyncRegistryType(
            5,
            Statics.AssetControl.getContext().getString(R.string.categories),
            ConfEntry.acLastUpdateItemCategory
        )
        var AssetReview = SyncRegistryType(
            6,
            Statics.AssetControl.getContext().getString(R.string.asset_reviews),
            ConfEntry.acLastUpdateAssetReview
        )
        var WarehouseMovement = SyncRegistryType(
            7,
            Statics.AssetControl.getContext().getString(R.string.movements),
            ConfEntry.acLastUpdateWarehouseMovement
        )
        /*
        var ActionLog = SyncRegistryType(8, "Registro de acciones", ConfEntry.acLastUpdateActionLog) */

        var Provider = SyncRegistryType(
            9,
            Statics.AssetControl.getContext().getString(R.string.providers),
            ConfEntry.acLastUpdateProviders
        )
        var Repairman = SyncRegistryType(
            10,
            Statics.AssetControl.getContext().getString(R.string.manteinance_user),
            ConfEntry.acLastUpdateRepairman
        )
        var Repairshop = SyncRegistryType(
            11,
            Statics.AssetControl.getContext().getString(R.string.repairshop),
            ConfEntry.acLastUpdateRepairshop
        )
        var AssetManteinance = SyncRegistryType(
            12,
            Statics.AssetControl.getContext().getString(R.string.manteinance),
            ConfEntry.acLastUpdateAssetManteinance
        )
        var AssetManteinanceLog = SyncRegistryType(
            13,
            Statics.AssetControl.getContext().getString(R.string.manteinance_log),
            ConfEntry.acLastUpdateAssetManteinanceLog
        )
        var ManteinanceType = SyncRegistryType(
            14,
            Statics.AssetControl.getContext().getString(R.string.manteinance_type),
            ConfEntry.acLastUpdateManteinanceType
        )
        var ManteinanceTypeGroup = SyncRegistryType(
            15,
            Statics.AssetControl.getContext().getString(R.string.manteinance_group),
            ConfEntry.acLastUpdateManteinanceTypeGroup
        )
        var CostCentre = SyncRegistryType(
            16,
            Statics.AssetControl.getContext().getString(R.string.cost_centre),
            ConfEntry.acLastUpdateCostCentre
        )
        var AssetManteinanceProgramed = SyncRegistryType(
            17,
            Statics.AssetControl.getContext().getString(R.string.programed_manteinance),
            ConfEntry.acLastUpdateAssetManteinanceProgramed
        )
        var DataCollectionRule = SyncRegistryType(
            18,
            Statics.AssetControl.getContext().getString(R.string.rules),
            ConfEntry.acLastUpdateDataCollectionRule
        )
        var Attribute = SyncRegistryType(
            19,
            Statics.AssetControl.getContext().getString(R.string.attributes),
            ConfEntry.acLastUpdateAttribute
        )
        var AttributeCategory = SyncRegistryType(
            20,
            Statics.AssetControl.getContext().getString(R.string.attribute_categories),
            ConfEntry.acLastUpdateAttributeCategory
        )
        var DataCollection = SyncRegistryType(
            21,
            Statics.AssetControl.getContext().getString(R.string.data_collection),
            ConfEntry.acLastUpdateDataCollection
        )
        var Route = SyncRegistryType(
            22,
            Statics.AssetControl.getContext().getString(R.string.routes),
            ConfEntry.acLastUpdateRoute
        )
        var RouteProcess = SyncRegistryType(
            23,
            Statics.AssetControl.getContext().getString(R.string.route_process),
            ConfEntry.acLastUpdateRouteProcess
        )
        var BarcodeLabelCustom = SyncRegistryType(
            24,
            Statics.AssetControl.getContext().getString(R.string.labels),
            ConfEntry.acLastUpdateBarcodeLabelCustom
        )
        var UserWarehouseArea = SyncRegistryType(
            25,
            Statics.AssetControl.getContext().getString(R.string.user_warehouse_areas),
            ConfEntry.acLastUpdateUserWarehouseArea
        )
        var AttributeComposition = SyncRegistryType(
            26,
            Statics.AssetControl.getContext().getString(R.string.attribute_composition)
        )
        var RouteComposition = SyncRegistryType(
            27,
            Statics.AssetControl.getContext().getString(R.string.route_composition)
        )
        var DataCollectionRuleContent = SyncRegistryType(
            28,
            Statics.AssetControl.getContext().getString(R.string.rule_content)
        )
        var DataCollectionRuleTarget = SyncRegistryType(
            29,
            Statics.AssetControl.getContext().getString(R.string.rule_target)
        )
        var AssetReviewStatus = SyncRegistryType(
            100,
            Statics.AssetControl.getContext().getString(R.string.asset_review_status)
        )
        var RouteProcessStatus = SyncRegistryType(
            101,
            Statics.AssetControl.getContext().getString(R.string.route_process_status)
        )
        var ManteinanceStatus = SyncRegistryType(
            102,
            Statics.AssetControl.getContext().getString(R.string.manteinance_status)
        )
        var BarcodeLabelTarget = SyncRegistryType(
            103,
            Statics.AssetControl.getContext().getString(R.string.label_target)
        )

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
                AssetManteinance,
                AssetManteinanceLog,
                ManteinanceType,
                ManteinanceTypeGroup,
                CostCentre,
                AssetManteinanceProgramed,
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

                // ESTATICAS
                AssetReviewStatus,
                RouteProcessStatus,
                ManteinanceStatus,
                BarcodeLabelTarget
            )

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
                AssetManteinance,
                DataCollection,
                RouteProcess
            )

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
                AssetManteinance.id.toString(),
                DataCollection.id.toString(),
                RouteProcess.id.toString()
            )

            return ArrayList(allSections.sortedWith(compareBy { it }))
        }
    }
}