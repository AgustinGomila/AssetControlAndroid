package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.data.model.route.RouteProcess

data class RouteProcessResult(
    val routeProcess: RouteProcess? = null,
    val newProcess: Boolean = false,
    val error: ErrorResult? = null,
)