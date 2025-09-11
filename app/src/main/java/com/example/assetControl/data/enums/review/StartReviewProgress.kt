package com.example.assetControl.data.enums.review

import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent

data class StartReviewProgress(
    var msg: String = "",
    var taskStatus: Int = 0,
    var assetReview: AssetReview? = null,
    var arContArray: ArrayList<AssetReviewContent> = ArrayList(),
    var progress: Int? = null,
    var total: Int? = null,
)