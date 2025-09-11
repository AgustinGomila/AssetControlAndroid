package com.example.assetControl.data.enums.route

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R

data class RouteProcessStatus(val id: Int, val description: String) {
    companion object {
        var unknown = RouteProcessStatus(0, context.getString(R.string.route_process_status_unknown))
        var processed = RouteProcessStatus(1, context.getString(R.string.route_process_status_processed))
        var skipped = RouteProcessStatus(2, context.getString(R.string.route_process_status_skipped))
        var notProcessed = RouteProcessStatus(3, context.getString(R.string.route_process_status_not_processed))

        fun getAll(): List<RouteProcessStatus> {
            return listOf(unknown, processed, skipped, notProcessed)
        }

        fun getById(id: Int): RouteProcessStatus {
            return getAll().firstOrNull { it.id == id } ?: unknown
        }
    }
}

