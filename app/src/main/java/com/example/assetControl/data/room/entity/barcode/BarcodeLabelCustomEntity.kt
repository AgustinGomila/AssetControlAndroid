package com.example.assetControl.data.room.entity.barcode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustom
import com.example.assetControl.data.room.dto.barcode.BarcodeLabelCustomEntry

@Entity(
    tableName = BarcodeLabelCustomEntry.TABLE_NAME,
    indices = [
        Index(
            value = [BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID],
            name = "IDX_${BarcodeLabelCustomEntry.TABLE_NAME}_${BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID}"
        ),
        Index(
            value = [BarcodeLabelCustomEntry.DESCRIPTION],
            name = "IDX_${BarcodeLabelCustomEntry.TABLE_NAME}_${BarcodeLabelCustomEntry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelCustomEntity(
    @PrimaryKey
    @ColumnInfo(name = BarcodeLabelCustomEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = BarcodeLabelCustomEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = BarcodeLabelCustomEntry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = BarcodeLabelCustomEntry.BARCODE_LABEL_TARGET_ID) val barcodeLabelTargetId: Long = 0L,
    @ColumnInfo(name = BarcodeLabelCustomEntry.TEMPLATE) val template: String = ""
) {
    constructor(b: BarcodeLabelCustom) : this(
        id = b.id,
        description = b.description,
        active = b.active,
        barcodeLabelTargetId = b.barcodeLabelTargetId,
        template = b.template
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
            r.add("ALTER TABLE barcode_label_custom RENAME TO barcode_label_custom_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `barcode_label_custom`
            (
                `_id`                     INTEGER NOT NULL,
                `description`             TEXT    NOT NULL,
                `active`                  INTEGER NOT NULL,
                `barcode_label_target_id` INTEGER NOT NULL,
                `template`                TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO barcode_label_custom (
                _id, description, active,
                barcode_label_target_id, template
            )
            SELECT
                _id, description, active,
                barcode_label_target_id, template
            FROM barcode_label_custom_temp
        """.trimIndent()
            )
            r.add("DROP TABLE barcode_label_custom_temp")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_custom_barcode_label_target_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_custom_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_barcode_label_target_id` ON `barcode_label_custom` (`barcode_label_target_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_description` ON `barcode_label_custom` (`description`);")
            return r
        }
    }
}