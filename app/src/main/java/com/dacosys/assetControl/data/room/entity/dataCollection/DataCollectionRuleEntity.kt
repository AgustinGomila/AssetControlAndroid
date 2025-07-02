package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRule
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleEntry

@Entity(
    tableName = DataCollectionRuleEntry.TABLE_NAME,
    indices = [
        Index(
            value = [DataCollectionRuleEntry.DESCRIPTION],
            name = "IDX_${DataCollectionRuleEntry.TABLE_NAME}_${DataCollectionRuleEntry.DESCRIPTION}"
        )
    ]
)
data class DataCollectionRuleEntity(
    @PrimaryKey
    @ColumnInfo(name = DataCollectionRuleEntry.ID) val id: Long = 0L,
    @ColumnInfo(name = DataCollectionRuleEntry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = DataCollectionRuleEntry.ACTIVE) val active: Int = 0
) {
    constructor(d: DataCollectionRule) : this(
        id = d.id,
        description = d.description,
        active = d.active
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
            r.add("ALTER TABLE data_collection_rule RENAME TO data_collection_rule_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `data_collection_rule`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO data_collection_rule (
                `_id`, `description`, `active`
            )
            SELECT
                `_id`, `description`, `active`
            FROM data_collection_rule_temp
        """.trimIndent()
            )
            r.add("DROP TABLE data_collection_rule_temp")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_description` ON `data_collection_rule` (`description`);")
            return r
        }
    }
}