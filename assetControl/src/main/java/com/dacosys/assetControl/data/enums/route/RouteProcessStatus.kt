package com.dacosys.assetControl.data.enums.route

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R

data class RouteProcessStatus(val id: Int, val description: String) {
    companion object {
        var unknown = RouteProcessStatus(0, getContext().getString(R.string.route_process_status_unknown))
        var processed = RouteProcessStatus(1, getContext().getString(R.string.route_process_status_processed))
        var skipped = RouteProcessStatus(2, getContext().getString(R.string.route_process_status_skipped))
        var notProcessed = RouteProcessStatus(3, getContext().getString(R.string.route_process_status_not_processed))

        fun getAll(): List<RouteProcessStatus> {
            return listOf(unknown, processed, skipped, notProcessed)
        }

        fun getById(id: Int): RouteProcessStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}

