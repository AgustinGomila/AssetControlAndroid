package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.user.User.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.NAME], name = "IDX_${Entry.TABLE_NAME}_${Entry.NAME}"),
        Index(value = [Entry.EXTERNAL_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.EXTERNAL_ID}"),
        Index(value = [Entry.EMAIL], name = "IDX_${Entry.TABLE_NAME}_${Entry.EMAIL}")
    ]
)
data class User(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long,
    @ColumnInfo(name = Entry.NAME) val name: String,
    @ColumnInfo(name = Entry.EXTERNAL_ID) val externalId: String?,
    @ColumnInfo(name = Entry.EMAIL) val email: String,
    @ColumnInfo(name = Entry.ACTIVE) val active: Int,
    @ColumnInfo(name = Entry.PASSWORD) val password: String?
) {
    object Entry {
        const val TABLE_NAME = "user"
        const val ID = "_id"
        const val NAME = "name"
        const val EXTERNAL_ID = "external_id"
        const val EMAIL = "email"
        const val ACTIVE = "active"
        const val PASSWORD = "password"
    }
}
