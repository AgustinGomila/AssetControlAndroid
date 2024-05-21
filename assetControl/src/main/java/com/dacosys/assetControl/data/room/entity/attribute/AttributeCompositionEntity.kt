package com.dacosys.assetControl.data.room.entity.attribute

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.attribute.AttributeComposition
import com.dacosys.assetControl.data.room.dto.attribute.AttributeComposition.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ATTRIBUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_COMPOSITION_TYPE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_TYPE_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class AttributeCompositionEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) val attributeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_TYPE_ID) val attributeCompositionTypeId: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String? = null,
    @ColumnInfo(name = Entry.COMPOSITION) val composition: String? = null,
    @ColumnInfo(name = Entry.USED) val used: Int = 0,
    @ColumnInfo(name = Entry.NAME) val name: String = "",
    @ColumnInfo(name = Entry.READ_ONLY) val readOnly: Int = 0,
    @ColumnInfo(name = Entry.DEFAULT_VALUE) val defaultValue: String = "",
) {
    constructor(a: AttributeComposition) : this(
        id = a.id,
        attributeId = a.attributeId,
        attributeCompositionTypeId = a.attributeCompositionTypeId,
        description = a.description,
        composition = a.composition,
        used = a.used,
        name = a.name,
        readOnly = a.readOnly,
        defaultValue = a.defaultValue,
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
            r.add("ALTER TABLE attribute_composition RENAME TO attribute_composition_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `attribute_composition`
            (
                `_id`                           INTEGER NOT NULL,
                `attribute_id`                  INTEGER NOT NULL,
                `attribute_composition_type_id` INTEGER NOT NULL,
                `description`                   TEXT,
                `composition`                   TEXT,
                `used`                          INTEGER NOT NULL,
                `name`                          TEXT    NOT NULL,
                `read_only`                     INTEGER NOT NULL,
                `default_value`                 TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO attribute_composition (
                _id, attribute_id, attribute_composition_type_id,
                description, composition, used,
                name, read_only, default_value
            )
            SELECT
                _id, attribute_id, attribute_composition_type_id,
                description, composition, used,
                name, read_only, default_value
            FROM attribute_composition_temp
        """.trimIndent()
            )
            r.add("DROP TABLE attribute_composition_temp")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_composition_attribute_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_composition_attribute_composition_type_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_attribute_composition_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_attribute_id` ON `attribute_composition` (`attribute_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_attribute_composition_type_id` ON `attribute_composition` (`attribute_composition_type_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_attribute_composition_description` ON `attribute_composition` (`description`);")
            return r
        }
    }
}