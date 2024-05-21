package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.dacosys.assetControl.data.room.dto.user.UserPermission
import com.dacosys.assetControl.data.room.dto.user.UserPermission.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    primaryKeys = [Entry.USER_ID, Entry.PERMISSION_ID],
    indices = [
        Index(
            value = [Entry.USER_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.USER_ID}"
        ),
        Index(
            value = [Entry.PERMISSION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.PERMISSION_ID}"
        )
    ]
)
data class UserPermissionEntity(
    @ColumnInfo(name = Entry.USER_ID) var userId: Long = 0L,
    @ColumnInfo(name = Entry.PERMISSION_ID) var permissionId: Long = 0L,
) {
    constructor(u: UserPermission) : this(
        userId = u.userId,
        permissionId = u.permissionId
    )

    companion object {
        /**
         * Migration zero
         * Migración desde la base de datos SQLite (version 0) a la primera versión de Room.
         * No utilizar constantes para la definición de nombres para evitar incoherencias en el futuro.
         * @return
         */
        fun migrationZero(): List<String> {
            val r: ArrayList<String> = arrayListOf()
            r.add("ALTER TABLE user_permission RENAME TO user_permission_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `user_permission`
            (
                `user_id`       INTEGER NOT NULL,
                `permission_id` INTEGER NOT NULL,
                PRIMARY KEY (`user_id`, `permission_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO user_permission (
                `user_id`, `permission_id`
            )
            SELECT
                `user_id`, `permission_id`
            FROM user_permission_temp
        """.trimIndent()
            )
            r.add("DROP TABLE user_permission_temp")
            r.add("DROP INDEX IF EXISTS `IDX_user_permission_user_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_user_permission_permission_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_permission_user_id` ON `user_permission` (`user_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_user_permission_permission_id` ON `user_permission` (`permission_id`);")
            return r
        }
    }
}