package com.dacosys.assetControl.data.enums.common

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class Table(val id: Int, val tableName: String, val description: String) {
    companion object {
        var actionLog = Table(1, "action_log", getContext().getString(R.string.action_logs))
        var asset = Table(2, "asset", getContext().getString(R.string.assets))
        var assetMaintenance = Table(3, "asset_manteinance", getContext().getString(R.string.asset_manteinance))
        var assetMaintenanceLog =
            Table(4, "asset_manteinance_log", getContext().getString(R.string.asset_manteinance_log))
        var assetMaintenanceProgramed =
            Table(5, "asset_manteinance_programed", getContext().getString(R.string.asset_manteinance_programed))
        var assetReview = Table(6, "asset_review", getContext().getString(R.string.asset_review))
        var assetReviewContent = Table(7, "asset_review_content", getContext().getString(R.string.asset_review_content))
        var itemCategory = Table(8, "item_category", getContext().getString(R.string.categories))
        var manteinanceType = Table(9, "manteinance_type", getContext().getString(R.string.manteinance_type))
        var manteinanceTypeGroup =
            Table(10, "manteinance_type_group", getContext().getString(R.string.manteinance_type_group))
        var provider = Table(11, "provider", getContext().getString(R.string.providers))
        var repairmanRepairshop =
            Table(12, "repairman_repairshop", getContext().getString(R.string.repairman_repairshop))
        var user = Table(13, "user", getContext().getString(R.string.user))
        var userPermission = Table(14, "user_permission", getContext().getString(R.string.permissions))
        var warehouse = Table(15, "warehouse", getContext().getString(R.string.warehouse))
        var warehouseArea = Table(16, "warehouse_area", getContext().getString(R.string.warehouse_area))
        var warehouseMovement = Table(17, "warehouse_movement", getContext().getString(R.string.movements))
        var warehouseMovementContent =
            Table(18, "warehouse_movement_content", getContext().getString(R.string.movement_contents))
        var costCentre = Table(19, "cost_centre", getContext().getString(R.string.cost_centre))
        var route = Table(20, "route", getContext().getString(R.string.route))
        var routeComposition = Table(21, "route_composition", getContext().getString(R.string.route_composition))
        var attribute = Table(22, "attribute", getContext().getString(R.string.attribute))
        var attributeComposition =
            Table(23, "attribute_composition", getContext().getString(R.string.attribute_composition))
        var attributeCategory = Table(24, "attribute_category", getContext().getString(R.string.attribute_categories))
        var dataCollectionRule =
            Table(25, "data_collection_rule", getContext().getString(R.string.data_collection_rules))
        var dataCollectionRuleContent =
            Table(26, "data_collection_rule_content", getContext().getString(R.string.data_collection_rule_contents))
        var dataCollectionRuleTarget =
            Table(27, "data_collection_rule_target", getContext().getString(R.string.data_collection_rule_target))
        var report = Table(28, "report", getContext().getString(R.string.reports))
        var reportContent = Table(29, "report_content", getContext().getString(R.string.report_contents))
        var dataCollection = Table(30, "data_collection", getContext().getString(R.string.data_collection))
        var dataCollectionContent =
            Table(31, "data_collection_content", getContext().getString(R.string.data_collection_contents))
        var routeProcess = Table(32, "route_process", getContext().getString(R.string.route_process))
        var routeProcessContent =
            Table(33, "route_process_content", getContext().getString(R.string.route_process_contents))
        var barcodeLabelCustom =
            Table(34, "barcode_label_custom", getContext().getString(R.string.barcode_label_custom))

        fun getAll(): List<Table> {
            return listOf(
                actionLog,
                asset,
                assetMaintenance,
                assetMaintenanceLog,
                assetMaintenanceProgramed,
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
        }

        fun getById(id: Int): Table? {
            return getAll().firstOrNull { it.id == id }
        }
    }
}