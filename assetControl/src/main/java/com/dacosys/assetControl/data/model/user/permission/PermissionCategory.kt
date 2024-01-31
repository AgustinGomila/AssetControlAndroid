package com.dacosys.assetControl.data.model.user.permission

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

/**
 * Created by Agustin on 16/01/2017.
 */

class PermissionCategory(permissionCategoryId: Long, description: String) {
    var id: Long = 0
    var description: String = ""

    init {
        this.description = description
        this.id = permissionCategoryId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is PermissionCategory) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    class CustomComparator : Comparator<PermissionCategory> {
        override fun compare(o1: PermissionCategory, o2: PermissionCategory): Int {
            if (o1.id < o2.id) {
                return -1
            } else if (o1.id > o2.id) {
                return 1
            }
            return 0
        }
    }

    companion object {
        var DeskMenu = PermissionCategory(
            1,
            getContext().getString(R.string.desk_menu)
        )
        var Asset = PermissionCategory(
            2,
            getContext().getString(R.string.assets)
        )
        var ItemCategory = PermissionCategory(
            3,
            getContext().getString(R.string.categories)
        )
        var Warehouse = PermissionCategory(
            4,
            getContext().getString(R.string.warehouses)
        )
        var Entity = PermissionCategory(
            5,
            getContext().getString(R.string.entities)
        )
        var Movement = PermissionCategory(
            6,
            getContext().getString(R.string.movements)
        )
        var AssetReview = PermissionCategory(
            7,
            getContext().getString(R.string.asset_reviews)
        )
        var Configuration = PermissionCategory(
            8,
            getContext().getString(R.string.configuration)
        )
        var CollMenu = PermissionCategory(
            9,
            getContext().getString(R.string.collector_buttons)
        )
        var Print = PermissionCategory(
            10,
            getContext().getString(R.string.print_options)
        )
        var User = PermissionCategory(
            11,
            getContext().getString(R.string.users)
        )
        var DeskPage = PermissionCategory(
            12,
            getContext().getString(R.string.desk_page)
        )
        var AssetManteinance = PermissionCategory(
            13,
            getContext().getString(R.string.asset_manteinance)
        )
        var Repairman = PermissionCategory(
            14,
            getContext().getString(R.string.repairmans)
        )
        var Repairshop = PermissionCategory(
            15,
            getContext().getString(R.string.repairshop)
        )
        var DeskMenuAssetManteinance = PermissionCategory(
            16,
            getContext().getString(R.string.desk_menu_asset_manteinance)
        )
        var DeskPageAssetManteinance = PermissionCategory(
            17,
            getContext().getString(R.string.desk_page_asset_manteinance)
        )
        var CostCentre = PermissionCategory(
            18,
            getContext().getString(R.string.cost_centre)
        )
        var Attribute = PermissionCategory(
            19,
            getContext().getString(R.string.attributes)
        )
        var Route = PermissionCategory(
            20,
            getContext().getString(R.string.routes)
        )
        var DataCollectionRule = PermissionCategory(
            21,
            getContext().getString(R.string.data_collection_rule)
        )
        var DataCollectionReport = PermissionCategory(
            22,
            getContext().getString(R.string.data_collection_report)
        )
        var RouteProcess = PermissionCategory(
            23,
            getContext().getString(R.string.route_process)
        )

        fun getAll(): ArrayList<PermissionCategory> {
            val allSections = ArrayList<PermissionCategory>()
            Collections.addAll(
                allSections,
                Configuration,
                Asset,
                ItemCategory,
                DeskMenu,
                Movement,
                Entity,
                AssetReview,
                Warehouse,
                Repairshop,
                CollMenu,
                Print,
                User,
                Repairman,
                DeskPage,
                AssetManteinance,
                DeskMenuAssetManteinance,
                DeskPageAssetManteinance,
                CostCentre,
                Attribute,
                Route,
                DataCollectionRule,
                DataCollectionReport,
                RouteProcess
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(mainButtonId: Long): PermissionCategory? {
            return getAll().firstOrNull { it.id == mainButtonId }
        }
    }
}