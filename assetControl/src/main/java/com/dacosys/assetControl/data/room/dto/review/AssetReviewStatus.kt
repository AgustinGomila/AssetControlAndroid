package com.dacosys.assetControl.data.room.dto.review

import androidx.room.ColumnInfo
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus

class AssetReviewStatus(
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {

    override fun toString(): String {
        return description
    }

    object Entry {
        const val TABLE_NAME = "status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }

    constructor(status: AssetReviewStatus) : this(
        id = status.id,
        description = status.description
    )
}
