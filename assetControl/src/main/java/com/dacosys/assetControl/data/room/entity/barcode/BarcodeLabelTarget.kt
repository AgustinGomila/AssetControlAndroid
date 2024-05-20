package com.dacosys.assetControl.data.room.entity.barcode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.enums.barcode.BarcodeLabelTarget
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelTarget.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelTarget(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = ""
) {
    object Entry {
        const val TABLE_NAME = "barcode_label_target"
        const val ID = "_id"
        const val DESCRIPTION = "description"
    }

    constructor(target: BarcodeLabelTarget) : this(
        id = target.id,
        description = target.description
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

