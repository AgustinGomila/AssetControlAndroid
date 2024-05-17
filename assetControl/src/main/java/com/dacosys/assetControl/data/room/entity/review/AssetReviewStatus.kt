package com.dacosys.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.entity.review.AssetReviewStatus.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class AssetReviewStatus(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {
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
