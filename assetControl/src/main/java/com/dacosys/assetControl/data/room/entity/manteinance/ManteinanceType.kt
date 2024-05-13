package com.dacosys.assetControl.data.room.entity.manteinance

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.entity.manteinance.ManteinanceType.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.MANTEINANCE_TYPE_GROUP_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.MANTEINANCE_TYPE_GROUP_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class ManteinanceType(
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.MANTEINANCE_TYPE_GROUP_ID) val manteinanceTypeGroupId: Long
) {
    object Entry {
        const val TABLE_NAME = "manteinance_type"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val MANTEINANCE_TYPE_GROUP_ID = "manteinance_type_group_id"
    }
}

