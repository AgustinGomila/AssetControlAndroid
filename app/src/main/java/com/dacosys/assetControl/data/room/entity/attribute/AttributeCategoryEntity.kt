package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.attribute.AttributeCategory
import com.dacosys.assetControl.data.room.dto.attribute.AttributeCategoryEntry

@Entity(
    tableName = AttributeCategoryEntry.TABLE_NAME,
    indices = [
        Index(
            value = [AttributeCategoryEntry.PARENT_ID],
            name = "IDX_${AttributeCategoryEntry.TABLE_NAME}_${AttributeCategoryEntry.PARENT_ID}"
        ),
        Index(
            value = [AttributeCategoryEntry.DESCRIPTION],
            name = "IDX_${AttributeCategoryEntry.TABLE_NAME}_${AttributeCategoryEntry.DESCRIPTION}"
        )
    ]
)
data class AttributeCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = AttributeCategoryEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = AttributeCategoryEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = AttributeCategoryEntry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = AttributeCategoryEntry.PARENT_ID) val parentId: Long = 0
) {
    constructor(a: AttributeCategory) : this(
        id = a.id,
        description = a.description,
        active = a.active,
        parentId = a.parentId
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
            r.add("ALTER TABLE attribute_category RENAME TO attribute_category_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `attribute_category`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                `parent_id`   INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO attribute_category (
                _id, description, active,
                parent_id
            )
            SELECT
                _id, description, active,
                parent_id
            FROM attribute_category_temp
        """.trimIndent()
            )
            r.add("DROP TABLE attribute_category_temp")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_category_parent_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_category_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_category_parent_id` ON `attribute_category` (`parent_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_category_description` ON `attribute_category` (`description`);")
            return r
        }
    }
}