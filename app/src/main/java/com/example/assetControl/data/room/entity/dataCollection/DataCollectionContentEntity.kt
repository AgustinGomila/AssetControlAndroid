package com.example.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.example.assetControl.data.room.dto.dataCollection.DataCollectionContentEntry
import java.util.*

@Entity(
    tableName = DataCollectionContentEntry.TABLE_NAME,
    indices = [
        Index(
            value = [DataCollectionContentEntry.DATA_COLLECTION_ID],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [DataCollectionContentEntry.LEVEL],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.LEVEL}"
        ),
        Index(
            value = [DataCollectionContentEntry.POSITION],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.POSITION}"
        ),
        Index(
            value = [DataCollectionContentEntry.ATTRIBUTE_ID],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.ATTRIBUTE_ID}"
        ),
        Index(
            value = [DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_ID],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_ID}"
        ),
        Index(
            value = [DataCollectionContentEntry.DATA_COLLECTION_CONTENT_ID],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.DATA_COLLECTION_CONTENT_ID}"
        ),
        Index(
            value = [DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${DataCollectionContentEntry.TABLE_NAME}_${DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID}"
        )
    ]
)
data class DataCollectionContentEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = DataCollectionContentEntry.ID) var id: Long = 0L,
    @ColumnInfo(name = DataCollectionContentEntry.DATA_COLLECTION_CONTENT_ID) var dataCollectionContentId: Long = 0L,
    @ColumnInfo(name = DataCollectionContentEntry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = DataCollectionContentEntry.LEVEL) var level: Int? = null,
    @ColumnInfo(name = DataCollectionContentEntry.POSITION) var position: Int? = null,
    @ColumnInfo(name = DataCollectionContentEntry.ATTRIBUTE_ID) var attributeId: Long? = null,
    @ColumnInfo(name = DataCollectionContentEntry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long? = null,
    @ColumnInfo(name = DataCollectionContentEntry.RESULT) var result: Int? = null,
    @ColumnInfo(name = DataCollectionContentEntry.VALUE_STR) var valueStr: String = "",
    @ColumnInfo(name = DataCollectionContentEntry.DATA_COLLECTION_DATE) var dataCollectionDate: Date = Date(),
    @ColumnInfo(name = DataCollectionContentEntry.DATA_COLLECTION_RULE_CONTENT_ID) var dataCollectionRuleContentId: Long = 0L,
) {
    constructor(d: DataCollectionContent) : this(
        id = d.id,
        dataCollectionContentId = d.dataCollectionContentId,
        dataCollectionId = d.dataCollectionId,
        level = d.level,
        position = d.position,
        attributeId = d.attributeId,
        attributeCompositionId = d.attributeCompositionId,
        result = d.result,
        valueStr = d.valueStr,
        dataCollectionDate = d.dataCollectionDate,
        dataCollectionRuleContentId = d.dataCollectionRuleContentId
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
            r.add("ALTER TABLE data_collection_content RENAME TO data_collection_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `data_collection_content`
            (
                `_id`                             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `data_collection_content_id`      INTEGER                           NOT NULL,
                `data_collection_id`              INTEGER,
                `level`                           INTEGER,
                `position`                        INTEGER,
                `attribute_id`                    INTEGER,
                `attribute_composition_id`        INTEGER,
                `result`                          INTEGER,
                `value_str`                       TEXT                              NOT NULL,
                `data_collection_date`            INTEGER                           NOT NULL,                
                `data_collection_rule_content_id` INTEGER                           NOT NULL
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO data_collection_content (
                `_id`, `data_collection_id`, `level`, `position`,
                `attribute_id`, `attribute_composition_id`,
                `result`, `value_str`, `data_collection_date`,
                `data_collection_content_id`, `data_collection_rule_content_id`
            )
            SELECT
                `_id`, `data_collection_id`, `level`, `position`,
                `attribute_id`, `attribute_composition_id`,
                `result`, `value_str`, `data_collection_date`,
                `data_collection_content_id`, `data_collection_rule_content_id`
            FROM data_collection_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE data_collection_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_data_collection_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_level`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_position`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_attribute_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_attribute_composition_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content__id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_content_data_collection_rule_content_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_id` ON `data_collection_content` (`data_collection_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_level` ON `data_collection_content` (`level`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_position` ON `data_collection_content` (`position`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_attribute_id` ON `data_collection_content` (`attribute_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_attribute_composition_id` ON `data_collection_content` (`attribute_composition_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_content_id` ON `data_collection_content` (`data_collection_content_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_rule_content_id` ON `data_collection_content` (`data_collection_rule_content_id`);")
            return r
        }
    }
}