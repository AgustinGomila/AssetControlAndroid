package com.dacosys.assetControl.data.room.entity.user

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.AssetControlApp.Companion.isLogged
import com.dacosys.assetControl.data.enums.permission.PermissionEntry
import com.dacosys.assetControl.data.room.entity.user.User.Entry
import com.dacosys.assetControl.data.room.repository.user.UserPermissionRepository
import com.dacosys.assetControl.data.webservice.user.UserObject

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
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.NAME) val name: String = "",
    @ColumnInfo(name = Entry.EXTERNAL_ID) val externalId: String? = null,
    @ColumnInfo(name = Entry.EMAIL) val email: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.PASSWORD) val password: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        name = parcel.readString().orEmpty(),
        externalId = parcel.readString(),
        email = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        password = parcel.readString()
    )

    constructor(uObj: UserObject) : this(
        id = uObj.user_id,
        name = uObj.name,
        externalId = uObj.external_id,
        email = uObj.email,
        active = uObj.active,
        password = uObj.password
    )

    object Entry {
        const val TABLE_NAME = "user"
        const val ID = "_id"
        const val NAME = "name"
        const val EXTERNAL_ID = "external_id"
        const val EMAIL = "email"
        const val ACTIVE = "active"
        const val PASSWORD = "password"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(externalId)
        parcel.writeString(email)
        parcel.writeInt(active)
        parcel.writeString(password)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

        fun hasPermission(permission: PermissionEntry): Boolean {
            return if (isLogged()) {
                UserPermissionRepository().selectByUserIdUserPermissionId(
                    userId = getUserId() ?: 0,
                    permissionId = permission.id
                ) != null
            } else {
                false
            }
        }

        /**
         * Migration zero
         * Migración desde la base de datos SQLite (version 0) a la primera versión de Room.
         * No utilizar constantes para la definición de nombres para evitar incoherencias en el futuro.
         * @return
         */
        fun migrationZero(): List<String> {
            val r: ArrayList<String> = arrayListOf()
            r.add("ALTER TABLE user RENAME TO user_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `user`
            (
                `_id`         INTEGER NOT NULL,
                `name`        TEXT    NOT NULL,
                `external_id` TEXT,
                `email`       TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                `password`    TEXT,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO user (
                `_id`, `name`, `external_id`,
                `email`, `active`, `password`
            )
            SELECT
                `_id`, `name`, `external_id`,
                `email`, `active`, `password`
            FROM user_temp
        """.trimIndent()
            )
            r.add("DROP TABLE user_temp")
            r.add("DROP INDEX IF EXISTS `IDX_user_name`;")
            r.add("DROP INDEX IF EXISTS `IDX_user_external_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_name` ON `user` (`name`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_external_id` ON `user` (`external_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_email` ON `user` (`email`);")
            return r
        }
    }
}
