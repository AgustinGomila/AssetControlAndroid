package com.dacosys.assetControl.data.model.table

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
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

        var actionLog = Table(1, getContext().getString(R.string.action_logs))
        var asset = Table(2, getContext().getString(R.string.assets))
        var assetManteinance = Table(3, getContext().getString(R.string.asset_manteinance))
        var assetManteinanceLog = Table(4, getContext().getString(R.string.asset_manteinance_log))
        var assetManteinanceProgramed = Table(5, getContext().getString(R.string.asset_manteinance_programed))
        var assetReview = Table(6, getContext().getString(R.string.asset_review))
        var assetReviewContent = Table(7, getContext().getString(R.string.asset_review_content))
        var itemCategory = Table(8, getContext().getString(R.string.categories))
        var manteinanceType = Table(9, getContext().getString(R.string.manteinance_type))
        var manteinanceTypeGroup = Table(10, getContext().getString(R.string.manteinance_type_group))
        var provider = Table(11, getContext().getString(R.string.providers))
        var repairmanRepairshop = Table(12, getContext().getString(R.string.repairman_repairshop))
        var user = Table(13, getContext().getString(R.string.user))
        var userPermission = Table(14, getContext().getString(R.string.permissions))
        var warehouse = Table(15, getContext().getString(R.string.warehouse))
        var warehouseArea = Table(16, getContext().getString(R.string.warehouse_area))
        var warehouseMovement = Table(17, getContext().getString(R.string.movements))
        var warehouseMovementContent = Table(18, getContext().getString(R.string.movement_contents))
        var costCentre = Table(19, getContext().getString(R.string.cost_centre))
        var route = Table(20, getContext().getString(R.string.route))
        var routeComposition = Table(21, getContext().getString(R.string.route_composition))
        var attribute = Table(22, getContext().getString(R.string.attribute))
        var attributeComposition = Table(23, getContext().getString(R.string.attribute_composition))
        var attributeCategory = Table(24, getContext().getString(R.string.attribute_categories))
        var dataCollectionRule = Table(25, getContext().getString(R.string.data_collection_rules))
        var dataCollectionRuleContent = Table(26, getContext().getString(R.string.data_collection_rule_contents))
        var dataCollectionRuleTarget = Table(27, getContext().getString(R.string.data_collection_rule_target))
        var report = Table(28, getContext().getString(R.string.reports))
        var reportContent = Table(29, getContext().getString(R.string.report_contents))
        var dataCollection = Table(30, getContext().getString(R.string.data_collection))
        var dataCollectionContent = Table(31, getContext().getString(R.string.data_collection_contents))
        var routeProcess = Table(32, getContext().getString(R.string.route_process))
        var routeProcessContent = Table(33, getContext().getString(R.string.route_process_contents))
        var barcodeLabelCustom = Table(34, getContext().getString(R.string.barcode_label_custom))

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