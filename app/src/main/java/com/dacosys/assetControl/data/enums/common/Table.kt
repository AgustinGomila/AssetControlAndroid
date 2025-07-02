package com.dacosys.assetControl.data.enums.common

import com.dacosys.assetControl.AssetControlApp.Companion.context
import com.dacosys.assetControl.R

data class Table(val id: Int, val tableName: String, val description: String) {
    companion object {
        var actionLog = Table(1, "action_log", context.getString(R.string.action_logs))
        var asset = Table(2, "asset", context.getString(R.string.assets))
        var assetMaintenance = Table(3, "asset_manteinance", context.getString(R.string.asset_manteinance))
        var assetMaintenanceLog =
            Table(4, "asset_manteinance_log", context.getString(R.string.asset_manteinance_log))
        var assetMaintenanceProgramed =
            Table(5, "asset_manteinance_programed", context.getString(R.string.asset_manteinance_programed))
        var assetReview = Table(6, "asset_review", context.getString(R.string.asset_review))
        var assetReviewContent = Table(7, "asset_review_content", context.getString(R.string.asset_review_content))
        var itemCategory = Table(8, "item_category", context.getString(R.string.categories))
        var manteinanceType = Table(9, "manteinance_type", context.getString(R.string.manteinance_type))
        var manteinanceTypeGroup =
            Table(10, "manteinance_type_group", context.getString(R.string.manteinance_type_group))
        var provider = Table(11, "provider", context.getString(R.string.providers))
        var repairmanRepairshop =
            Table(12, "repairman_repairshop", context.getString(R.string.repairman_repairshop))
        var user = Table(13, "user", context.getString(R.string.user))
        var userPermission = Table(14, "user_permission", context.getString(R.string.permissions))
        var warehouse = Table(15, "warehouse", context.getString(R.string.warehouse))
        var warehouseArea = Table(16, "warehouse_area", context.getString(R.string.warehouse_area))
        var warehouseMovement = Table(17, "warehouse_movement", context.getString(R.string.movements))
        var warehouseMovementContent =
            Table(18, "warehouse_movement_content", context.getString(R.string.movement_contents))
        var costCentre = Table(19, "cost_centre", context.getString(R.string.cost_centre))
        var route = Table(20, "route", context.getString(R.string.route))
        var routeComposition = Table(21, "route_composition", context.getString(R.string.route_composition))
        var attribute = Table(22, "attribute", context.getString(R.string.attribute))
        var attributeComposition =
            Table(23, "attribute_composition", context.getString(R.string.attribute_composition))
        var attributeCategory = Table(24, "attribute_category", context.getString(R.string.attribute_categories))
        var dataCollectionRule =
            Table(25, "data_collection_rule", context.getString(R.string.data_collection_rules))
        var dataCollectionRuleContent =
            Table(26, "data_collection_rule_content", context.getString(R.string.data_collection_rule_contents))
        var dataCollectionRuleTarget =
            Table(27, "data_collection_rule_target", context.getString(R.string.data_collection_rule_target))
        var report = Table(28, "report", context.getString(R.string.reports))
        var reportContent = Table(29, "report_content", context.getString(R.string.report_contents))
        var dataCollection = Table(30, "data_collection", context.getString(R.string.data_collection))
        var dataCollectionContent =
            Table(31, "data_collection_content", context.getString(R.string.data_collection_contents))
        var routeProcess = Table(32, "route_process", context.getString(R.string.route_process))
        var routeProcessContent =
            Table(33, "route_process_content", context.getString(R.string.route_process_contents))
        var barcodeLabelCustom =
            Table(34, "barcode_label_custom", context.getString(R.string.barcode_label_custom))

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