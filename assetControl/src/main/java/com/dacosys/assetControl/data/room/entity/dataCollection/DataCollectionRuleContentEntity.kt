package com.dacosys.assetControl.data.room.entity.dataCollection

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionRuleContent.Entry

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DATA_COLLECTION_RULE_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_ID}"
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
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class DataCollectionRuleContentEntity(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_ID) var dataCollectionRuleId: Long = 0L,
    @ColumnInfo(name = Entry.LEVEL) var level: Int = 0,
    @ColumnInfo(name = Entry.POSITION) var position: Int = 0,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long = 0L,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long = 0L,
    @ColumnInfo(name = Entry.EXPRESSION) var expression: String? = null,
    @ColumnInfo(name = Entry.TRUE_RESULT) var trueResult: Int = 0,
    @ColumnInfo(name = Entry.FALSE_RESULT) var falseResult: Int = 0,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var mActive: Int = 0,
    @ColumnInfo(name = Entry.MANDATORY) var mMandatory: Int = 0,
) {
    constructor(d: DataCollectionRuleContent) : this(
        id = d.id,
        dataCollectionRuleId = d.dataCollectionRuleId,
        level = d.level,
        position = d.position,
        attributeId = d.attributeId,
        attributeCompositionId = d.attributeCompositionId,
        expression = d.expression,
        trueResult = d.trueResult,
        falseResult = d.falseResult,
        description = d.description,
        mActive = d.mActive,
        mMandatory = d.mMandatory,
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
            r.add("ALTER TABLE data_collection_rule_content RENAME TO data_collection_rule_content_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `data_collection_rule_content`
            (
                `_id`                      INTEGER NOT NULL,
                `data_collection_rule_id`  INTEGER NOT NULL,
                `level`                    INTEGER NOT NULL,
                `position`                 INTEGER NOT NULL,
                `attribute_id`             INTEGER NOT NULL,
                `attribute_composition_id` INTEGER NOT NULL,
                `expression`               TEXT,
                `true_result`              INTEGER NOT NULL,
                `false_result`             INTEGER NOT NULL,
                `description`              TEXT    NOT NULL,
                `active`                   INTEGER NOT NULL,
                `mandatory`                INTEGER NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO data_collection_rule_content (
                `_id`, `data_collection_rule_id`,
                `level`, `position`, `attribute_id`,
                `attribute_composition_id`,
                `expression`,
                `true_result`,
                `false_result`,
                `description`, `active`, `mandatory`
            )
            SELECT
                `_id`, `data_collection_rule_id`,
                `level`, `position`, `attribute_id`,
                COALESCE(`attribute_composition_id`, 0) AS `attribute_composition_id`,
                `expression`,
                COALESCE(`true_result`, 0) AS `true_result`,
                COALESCE(`false_result`, 0) AS `false_result`,
                `description`, `active`, `mandatory`
            FROM data_collection_rule_content_temp
        """.trimIndent()
            )
            r.add("DROP TABLE data_collection_rule_content_temp")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_data_collection_rule_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_level`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_position`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_attribute_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_attribute_composition_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_data_collection_rule_content_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_data_collection_rule_id` ON `data_collection_rule_content` (`data_collection_rule_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_level` ON `data_collection_rule_content` (`level`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_position` ON `data_collection_rule_content` (`position`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_attribute_id` ON `data_collection_rule_content` (`attribute_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_attribute_composition_id` ON `data_collection_rule_content` (`attribute_composition_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_rule_content_description` ON `data_collection_rule_content` (`description`);")
            return r
        }
    }
}