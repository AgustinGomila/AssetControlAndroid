package com.dacosys.assetControl.data.room.entity.manteinance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceStatus.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class ManteinanceStatus(
    @ColumnInfo(name = Entry.ID) val id: Int,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String
) {
    object Entry {
        const val TABLE_NAME = "manteinance_status"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }
}