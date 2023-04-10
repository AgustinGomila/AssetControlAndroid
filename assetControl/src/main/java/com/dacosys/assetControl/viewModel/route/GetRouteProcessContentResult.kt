package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.model.route.RouteProcessContent

data class GetRouteProcessContentResult(
    val currentRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList(),
    val level: Int = 0,
)