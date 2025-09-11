package com.example.assetControl.data.room.entity.review

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.review.AssetReviewStatus
import com.example.assetControl.data.room.dto.review.AssetReviewStatusEntry
import com.example.assetControl.data.enums.review.AssetReviewStatus as AssetReviewStatusEnum

@Entity(
    tableName = AssetReviewStatusEntry.TABLE_NAME,
    indices = [
        Index(
            value = [AssetReviewStatusEntry.ID],
            name = "IDX_${AssetReviewStatusEntry.TABLE_NAME}_${AssetReviewStatusEntry.ID}"
        ),
        Index(
            value = [AssetReviewStatusEntry.DESCRIPTION],
            name = "IDX_${AssetReviewStatusEntry.TABLE_NAME}_${AssetReviewStatusEntry.DESCRIPTION}"
        )
    ]
)
data class AssetReviewStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = AssetReviewStatusEntry.ID) val id: Int = 0,
    @ColumnInfo(name = AssetReviewStatusEntry.DESCRIPTION) val description: String = ""
) {
    constructor(a: AssetReviewStatus) : this(
        id = a.id,
        description = a.description
    )

    constructor(a: AssetReviewStatusEnum) : this(
        id = a.id,
        description = a.description
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
            r.add("ALTER TABLE status RENAME TO status_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `status`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO status (
                _id, description
            )
            SELECT
                _id, description
            FROM status_temp
        """.trimIndent()
            )
            r.add("DROP TABLE status_temp")
            r.add("DROP INDEX IF EXISTS `IDX_status__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_status_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_status__id` ON `status` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_status_description` ON `status` (`description`);")
            return r
        }
    }
}