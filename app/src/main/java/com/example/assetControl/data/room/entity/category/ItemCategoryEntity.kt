package com.example.assetControl.data.room.entity.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.category.ItemCategory
import com.example.assetControl.data.room.dto.category.ItemCategoryEntry

@Entity(
    tableName = ItemCategoryEntry.TABLE_NAME,
    indices = [
        Index(
            value = [ItemCategoryEntry.DESCRIPTION],
            name = "IDX_${ItemCategoryEntry.TABLE_NAME}_${ItemCategoryEntry.DESCRIPTION}"
        ),
        Index(
            value = [ItemCategoryEntry.PARENT_ID],
            name = "IDX_${ItemCategoryEntry.TABLE_NAME}_${ItemCategoryEntry.PARENT_ID}"
        )
    ]
)
data class ItemCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = ItemCategoryEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = ItemCategoryEntry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = ItemCategoryEntry.ACTIVE) var active: Int = 0,
    @ColumnInfo(name = ItemCategoryEntry.PARENT_ID) var parentId: Long = 0L,
    @ColumnInfo(name = ItemCategoryEntry.TRANSFERRED) var transferred: Int? = null,
) {
    constructor(i: ItemCategory) : this(
        id = i.id,
        description = i.description,
        active = i.mActive,
        parentId = i.parentId,
        transferred = i.transferred
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
            r.add("ALTER TABLE item_category RENAME TO item_category_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `item_category`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                `parent_id`   INTEGER NOT NULL,
                `transferred` INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO item_category (
                `_id`, `description`, `active`,
                `parent_id`, `transferred`
            )
            SELECT
                `_id`, `description`, `active`,
                `parent_id`, `transferred`
            FROM item_category_temp
        """.trimIndent()
            )
            r.add("DROP TABLE item_category_temp")
            r.add("DROP INDEX IF EXISTS `IDX_item_category_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_item_category_parent_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_item_category_description` ON `item_category` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_item_category_parent_id` ON `item_category` (`parent_id`);")
            return r
        }
    }
}