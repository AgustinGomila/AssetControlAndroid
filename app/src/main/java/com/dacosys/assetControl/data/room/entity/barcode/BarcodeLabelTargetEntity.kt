package com.dacosys.assetControl.data.room.entity.barcode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.dto.barcode.BarcodeLabelTargetEntry
import com.dacosys.assetControl.data.enums.barcode.BarcodeLabelTarget as BarcodeLabelTargetEnum

@Entity(
    tableName = BarcodeLabelTargetEntry.TABLE_NAME,
    indices = [
        Index(
            value = [BarcodeLabelTargetEntry.ID],
            name = "IDX_${BarcodeLabelTargetEntry.TABLE_NAME}_${BarcodeLabelTargetEntry.ID}"
        ),
        Index(
            value = [BarcodeLabelTargetEntry.DESCRIPTION],
            name = "IDX_${BarcodeLabelTargetEntry.TABLE_NAME}_${BarcodeLabelTargetEntry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelTargetEntity(
    @PrimaryKey
    @ColumnInfo(name = BarcodeLabelTargetEntry.ID) val id: Int = 0,
    @ColumnInfo(name = BarcodeLabelTargetEntry.DESCRIPTION) val description: String = ""
) {
    constructor(b: BarcodeLabelTarget) : this(
        id = b.id,
        description = b.description
    )

    constructor(b: BarcodeLabelTargetEnum) : this(
        id = b.id,
        description = b.description
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
            r.add("ALTER TABLE barcode_label_target RENAME TO barcode_label_target_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `barcode_label_target`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO barcode_label_target (
                _id, description
            )
            SELECT
                _id, description
            FROM barcode_label_target_temp
        """.trimIndent()
            )
            r.add("DROP TABLE barcode_label_target_temp")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_target__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_target_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_target__id` ON `barcode_label_target` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_target_description` ON `barcode_label_target` (`description`);")
            return r
        }
    }
}