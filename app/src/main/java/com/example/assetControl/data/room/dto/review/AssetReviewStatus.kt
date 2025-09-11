package com.example.assetControl.data.room.dto.review

import androidx.room.ColumnInfo

abstract class AssetReviewStatusEntry {
    companion object {
        const val TABLE_NAME = "status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}

class AssetReviewStatus(
    @ColumnInfo(name = AssetReviewStatusEntry.ID) val id: Int = 0,
    @ColumnInfo(name = AssetReviewStatusEntry.DESCRIPTION) val description: String = ""
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AssetReviewStatus

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }

    constructor(status: AssetReviewStatus) : this(
        id = status.id,
        description = status.description
    )
}
