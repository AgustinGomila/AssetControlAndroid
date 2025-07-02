package com.dacosys.assetControl.viewModel.route

import com.dacosys.assetControl.data.room.dto.route.RouteProcessContent

data class RouteProcessContentResult(
    val contents: ArrayList<RouteProcessContent> = ArrayList(),
    val level: Int = 0,
)