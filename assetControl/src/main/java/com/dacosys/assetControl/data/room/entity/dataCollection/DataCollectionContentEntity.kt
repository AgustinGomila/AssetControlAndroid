package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionContent
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionContent.Entry
import java.util.*

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}"
        ),
        Index(
            value = [Entry.LEVEL],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}"
        ),
        Index(
            value = [Entry.POSITION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.POSITION}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}"
        ),
        Index(
            value = [Entry.ATTRIBUTE_COMPOSITION_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_CONTENT_ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}"
        )
    ]
)
data class DataCollectionContentEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_CONTENT_ID) var dataCollectionContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.LEVEL) var level: Int? = null,
    @ColumnInfo(name = Entry.POSITION) var position: Int? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long? = null,
    @ColumnInfo(name = Entry.RESULT) var result: Int? = null,
    @ColumnInfo(name = Entry.VALUE_STR) var valueStr: String = "",
    @ColumnInfo(name = Entry.DATA_COLLECTION_DATE) var dataCollectionDate: Date = Date(),
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) var dataCollectionRuleContentId: Long = 0L,
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