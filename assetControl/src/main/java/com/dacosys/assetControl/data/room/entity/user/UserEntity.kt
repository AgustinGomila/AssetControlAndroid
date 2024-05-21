package com.dacosys.assetControl.data.room.entity.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.user.User
import com.dacosys.assetControl.data.room.dto.user.User.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(value = [Entry.NAME], name = "IDX_${Entry.TABLE_NAME}_${Entry.NAME}"),
        Index(value = [Entry.EXTERNAL_ID], name = "IDX_${Entry.TABLE_NAME}_${Entry.EXTERNAL_ID}"),
        Index(value = [Entry.EMAIL], name = "IDX_${Entry.TABLE_NAME}_${Entry.EMAIL}")
    ]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.NAME) val name: String = "",
    @ColumnInfo(name = Entry.EXTERNAL_ID) val externalId: String? = null,
    @ColumnInfo(name = Entry.EMAIL) val email: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.PASSWORD) val password: String? = null
) {
    constructor(u: User) : this(
        id = u.id,
        name = u.name,
        externalId = u.externalId,
        email = u.email,
        active = u.active,
        password = u.password
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