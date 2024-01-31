package com.dacosys.assetControl.data.model.review.async

import com.dacosys.assetControl.data.model.review.AssetReview
import com.dacosys.assetControl.data.model.review.AssetReviewContent

class StartReviewProgress(
    var msg: String = "",
    var taskStatus: Int = 0,
    var assetReview: AssetReview? = null,
    var arContArray: ArrayList<AssetReviewContent> = ArrayList(),
    var progress: Int? = null,
    var total: Int? = null,
)