package com.example.assetControl.viewModel.route

import com.example.assetControl.data.room.dto.route.RouteProcessContent

data class RouteProcessContentResult(
    val contents: ArrayList<RouteProcessContent> = ArrayList(),
    val level: Int = 0,
)