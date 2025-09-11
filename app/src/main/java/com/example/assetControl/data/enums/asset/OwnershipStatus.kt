package com.example.assetControl.data.enums.asset

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class OwnershipStatus(val id: Int, val description: String) {
    companion object {
        var unknown = OwnershipStatus(0, context.getString(R.string.ownership_status_unknown))
        var owned = OwnershipStatus(1, context.getString(R.string.ownership_status_owned))
        var rented = OwnershipStatus(2, context.getString(R.string.ownership_status_rented))
        var leased = OwnershipStatus(3, context.getString(R.string.ownership_status_leased))
        var someoneElse = OwnershipStatus(4, context.getString(R.string.ownership_status_someone_else))

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