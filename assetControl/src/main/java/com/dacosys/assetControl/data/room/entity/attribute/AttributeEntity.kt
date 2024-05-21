package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.attribute.Attribute
import com.dacosys.assetControl.data.room.dto.attribute.Attribute.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ATTRIBUTE_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_TYPE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_CATEGORY_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_CATEGORY_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class AttributeEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.ATTRIBUTE_TYPE_ID) val attributeTypeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_CATEGORY_ID) val attributeCategoryId: Long = 0L,
) {
    constructor(a: Attribute) : this(
        id = a.id,
        description = a.description,
        active = a.active,
        attributeTypeId = a.attributeTypeId,
        attributeCategoryId = a.attributeCategoryId
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
            r.add("ALTER TABLE attribute RENAME TO attribute_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `attribute`
            (
                `_id`                   INTEGER NOT NULL,
                `description`           TEXT    NOT NULL,
                `active`                INTEGER NOT NULL,
                `attribute_type_id`     INTEGER NOT NULL,
                `attribute_category_id` INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO attribute (
                _id, description, active,
                attribute_type_id, attribute_category_id
            )
            SELECT
                _id, description, active,
                attribute_type_id, attribute_category_id
            FROM attribute_temp
        """.trimIndent()
            )
            r.add("DROP TABLE attribute_temp")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_attribute_type_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_attribute_category_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_attribute_type_id` ON `attribute` (`attribute_type_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_attribute_category_id` ON `attribute` (`attribute_category_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_description` ON `attribute` (`description`);")
            return r
        }
    }
}