package com.dacosys.assetControl.model.table

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import java.util.*

class Table : Parcelable {
    var tableId: Int = 0
    var tableName: String = ""

    constructor(tableId: Int, tableName: String) {
        this.tableName = tableName
        this.tableId = tableId
    }

    override fun toString(): String {
        return tableName
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Table) {
            false
        } else this.tableId == other.tableId
    }

    override fun hashCode(): Int {
        return this.tableId.hashCode()
    }

    constructor(parcel: Parcel) {
        tableId = parcel.readInt()
        tableName = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(tableId)
        parcel.writeString(tableName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Table> {
        override fun createFromParcel(parcel: Parcel): Table {
            return Table(parcel)
        }

        override fun newArray(size: Int): Array<Table?> {
            return arrayOfNulls(size)
        }

        var actionLog = Table(1, Statics.AssetControl.getContext().getString(R.string.action_logs))
        var asset = Table(2, Statics.AssetControl.getContext().getString(R.string.assets))
        var assetManteinance =
            Table(3, Statics.AssetControl.getContext().getString(R.string.asset_manteinance))
        var assetManteinanceLog =
            Table(4, Statics.AssetControl.getContext().getString(R.string.asset_manteinance_log))
        var assetManteinanceProgramed = Table(
            5,
            Statics.AssetControl.getContext().getString(R.string.asset_manteinance_programed)
        )
        var assetReview =
            Table(6, Statics.AssetControl.getContext().getString(R.string.asset_review))
        var assetReviewContent =
            Table(7, Statics.AssetControl.getContext().getString(R.string.asset_review_content))
        var itemCategory =
            Table(8, Statics.AssetControl.getContext().getString(R.string.categories))
        var manteinanceType =
            Table(9, Statics.AssetControl.getContext().getString(R.string.manteinance_type))
        var manteinanceTypeGroup =
            Table(10, Statics.AssetControl.getContext().getString(R.string.manteinance_type_group))
        var provider = Table(11, Statics.AssetControl.getContext().getString(R.string.providers))
        var repairmanRepairshop =
            Table(12, Statics.AssetControl.getContext().getString(R.string.repairman_repairshop))
        var user = Table(13, Statics.AssetControl.getContext().getString(R.string.user))
        var userPermission =
            Table(14, Statics.AssetControl.getContext().getString(R.string.permissions))
        var warehouse = Table(15, Statics.AssetControl.getContext().getString(R.string.warehouse))
        var warehouseArea =
            Table(16, Statics.AssetControl.getContext().getString(R.string.warehouse_area))
        var warehouseMovement =
            Table(17, Statics.AssetControl.getContext().getString(R.string.movements))
        var warehouseMovementContent =
            Table(18, Statics.AssetControl.getContext().getString(R.string.movement_contents))
        var costCentre =
            Table(19, Statics.AssetControl.getContext().getString(R.string.cost_centre))
        var route = Table(20, Statics.AssetControl.getContext().getString(R.string.route))
        var routeComposition =
            Table(21, Statics.AssetControl.getContext().getString(R.string.route_composition))
        var attribute = Table(22, Statics.AssetControl.getContext().getString(R.string.attribute))
        var attributeComposition =
            Table(23, Statics.AssetControl.getContext().getString(R.string.attribute_composition))
        var attributeCategory =
            Table(24, Statics.AssetControl.getContext().getString(R.string.attribute_categories))
        var dataCollectionRule =
            Table(25, Statics.AssetControl.getContext().getString(R.string.data_collection_rules))
        var dataCollectionRuleContent = Table(
            26,
            Statics.AssetControl.getContext().getString(R.string.data_collection_rule_contents)
        )
        var dataCollectionRuleTarget = Table(
            27,
            Statics.AssetControl.getContext().getString(R.string.data_collection_rule_target)
        )
        var report = Table(28, Statics.AssetControl.getContext().getString(R.string.reports))
        var reportContent =
            Table(29, Statics.AssetControl.getContext().getString(R.string.report_contents))
        var dataCollection =
            Table(30, Statics.AssetControl.getContext().getString(R.string.data_collection))
        var dataCollectionContent = Table(
            31,
            Statics.AssetControl.getContext().getString(R.string.data_collection_contents)
        )
        var routeProcess =
            Table(32, Statics.AssetControl.getContext().getString(R.string.route_process))
        var routeProcessContent =
            Table(33, Statics.AssetControl.getContext().getString(R.string.route_process_contents))
        var barcodeLabelCustom =
            Table(34, Statics.AssetControl.getContext().getString(R.string.barcode_label_custom))

        fun getAll(): ArrayList<Table> {
            val allSections = ArrayList<Table>()
            Collections.addAll(
                allSections,
                actionLog,
                asset,
                assetManteinance,
                assetManteinanceLog,
                assetManteinanceProgramed,
                assetReview,
                assetReviewContent,
                barcodeLabelCustom,
                costCentre,
                itemCategory,
                manteinanceType,
                manteinanceTypeGroup,
                provider,
                repairmanRepairshop,
                user,
                userPermission,
                warehouse,
                warehouseArea,
                warehouseMovement,
                warehouseMovementContent,
                route,
                routeComposition,
                attribute,
                attributeCategory,
                attributeComposition,
                dataCollectionRule,
                dataCollectionRuleContent,
                dataCollectionRuleTarget,
                report,
                reportContent,
                dataCollection,
                dataCollectionContent,
                routeProcess,
                routeProcessContent
            )

            return ArrayList(allSections.sortedWith(compareBy { it.tableId }))
        }

        fun getById(tableId: Int): Table? {
            return getAll().firstOrNull { it.tableId == tableId }
        }
    }
}