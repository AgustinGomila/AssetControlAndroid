package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.data.room.entity.route.RouteProcessContent

data class GetRouteProcessContentResult(
    val currentRouteProcessContent: ArrayList<RouteProcessContent> = ArrayList(),
    val level: Int = 0,
)