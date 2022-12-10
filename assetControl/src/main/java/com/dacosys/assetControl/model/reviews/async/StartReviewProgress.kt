package com.dacosys.assetControl.model.reviews.async

import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.reviews.assetReviewContent.`object`.AssetReviewContent

class StartReviewProgress(
    var msg: String = "",
    var taskStatus: Int = 0,
    var assetReview: AssetReview? = null,
    var arContArray: ArrayList<AssetReviewContent> = ArrayList(),
    var progress: Int? = null,
    var total: Int? = null,
)