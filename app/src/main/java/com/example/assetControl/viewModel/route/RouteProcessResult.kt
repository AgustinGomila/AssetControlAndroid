package com.example.assetControl.viewModel.route

import com.example.assetControl.data.room.dto.route.RouteProcess

data class RouteProcessResult(
    val routeProcess: RouteProcess? = null,
    val newProcess: Boolean = false,
    val error: ErrorResult? = null,
)