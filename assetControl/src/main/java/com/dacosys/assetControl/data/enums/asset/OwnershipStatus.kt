package com.dacosys.assetControl.data.enums.asset

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class OwnershipStatus(val id: Int, val description: String) {
    companion object {
        var unknown = OwnershipStatus(0, getContext().getString(R.string.ownership_status_unknown))
        var owned = OwnershipStatus(1, getContext().getString(R.string.ownership_status_owned))
        var rented = OwnershipStatus(2, getContext().getString(R.string.ownership_status_rented))
        var leased = OwnershipStatus(3, getContext().getString(R.string.ownership_status_leased))
        var someoneElse = OwnershipStatus(4, getContext().getString(R.string.ownership_status_someone_else))

        fun getAll(): List<OwnershipStatus> {
            return listOf(
                unknown,
                owned,
                rented,
                leased,
                someoneElse
            )
        }

        fun getById(id: Int): OwnershipStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}