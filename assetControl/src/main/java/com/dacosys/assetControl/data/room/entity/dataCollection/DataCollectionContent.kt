package com.dacosys.assetControl.data.room.entity.dataCollection

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.dataCollection.DataCollectionContent.Entry
import com.dacosys.assetControl.utils.Statics.Companion.toDate
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
            value = [Entry.ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.ID}"
        ),
        Index(
            value = [Entry.DATA_COLLECTION_RULE_CONTENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}"
        )
    ]
)
data class DataCollectionContent(
    @PrimaryKey
    @ColumnInfo(name = Entry.DATA_COLLECTION_CONTENT_ID) var dataCollectionContentId: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_ID) var dataCollectionId: Long? = null,
    @ColumnInfo(name = Entry.LEVEL) var level: Int? = null,
    @ColumnInfo(name = Entry.POSITION) var position: Int? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_ID) var attributeId: Long? = null,
    @ColumnInfo(name = Entry.ATTRIBUTE_COMPOSITION_ID) var attributeCompositionId: Long? = null,
    @ColumnInfo(name = Entry.RESULT) var result: Int? = null,
    @ColumnInfo(name = Entry.VALUE_STR) var valueStr: String = "",
    @ColumnInfo(name = Entry.DATA_COLLECTION_DATE) var dataCollectionDate: Date = Date(),
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DATA_COLLECTION_RULE_CONTENT_ID) var dataCollectionRuleContentId: Long = 0L,
    @Ignore var dataCollectionRuleContentStr: String = "",
    @Ignore var attributeCompositionStr: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        dataCollectionContentId = parcel.readLong(),
        dataCollectionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        level = parcel.readValue(Int::class.java.classLoader) as? Int,
        position = parcel.readValue(Int::class.java.classLoader) as? Int,
        attributeId = parcel.readValue(Long::class.java.classLoader) as? Long,
        attributeCompositionId = parcel.readValue(Long::class.java.classLoader) as? Long,
        result = parcel.readValue(Int::class.java.classLoader) as? Int,
        valueStr = parcel.readString().orEmpty(),
        dataCollectionDate = parcel.readString().orEmpty().toDate(),
        id = parcel.readLong(),
        dataCollectionRuleContentId = parcel.readLong(),
        dataCollectionRuleContentStr = parcel.readString().orEmpty(),
        attributeCompositionStr = parcel.readString().orEmpty()
    )

    constructor(
        ruleContent: DataCollectionRuleContent,
        virtualId: Long,
        result: Any?,
        valueStr: String,
    ) : this() {
        if (ruleContent.attributeCompositionId > 0) {
            this.attributeCompositionId = ruleContent.attributeCompositionId
        }

        if (ruleContent.attributeId > 0) {
            this.attributeId = ruleContent.attributeId
        }

        this.dataCollectionContentId = virtualId
        this.dataCollectionDate = Date()
        this.dataCollectionRuleContentId = ruleContent.id
        this.level = ruleContent.level
        this.position = ruleContent.position

        if (result != null) {
            if (result is Int) {
                this.result = result.toInt()
            } else if (result is Boolean) {
                this.result = if (result) 1 else 0
            }
        }
        this.valueStr = valueStr
    }

    object Entry {
        const val TABLE_NAME = "data_collection_content"
        const val DATA_COLLECTION_CONTENT_ID = "data_collection_content_id"
        const val DATA_COLLECTION_ID = "data_collection_id"
        const val LEVEL = "level"
        const val POSITION = "position"
        const val ATTRIBUTE_ID = "attribute_id"
        const val ATTRIBUTE_COMPOSITION_ID = "attribute_composition_id"
        const val RESULT = "result"
        const val VALUE_STR = "value_str"
        const val DATA_COLLECTION_DATE = "data_collection_date"
        const val ID = "_id"
        const val DATA_COLLECTION_RULE_CONTENT_ID = "data_collection_rule_content_id"

        const val DATA_COLLECTION_RULE_CONTENT_STR = "data_collection_rule_content_str"
        const val ATTRIBUTE_COMPOSITION_STR = "attribute_composition_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dataCollectionContentId)
        parcel.writeValue(dataCollectionId)
        parcel.writeValue(level)
        parcel.writeValue(position)
        parcel.writeValue(attributeId)
        parcel.writeValue(attributeCompositionId)
        parcel.writeValue(result)
        parcel.writeString(valueStr)
        parcel.writeString(dataCollectionDate.toString())
        parcel.writeLong(id)
        parcel.writeLong(dataCollectionRuleContentId)
        parcel.writeString(dataCollectionRuleContentStr)
        parcel.writeString(attributeCompositionStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataCollectionContent> {
        override fun createFromParcel(parcel: Parcel): DataCollectionContent {
            return DataCollectionContent(parcel)
        }

        override fun newArray(size: Int): Array<DataCollectionContent?> {
            return arrayOfNulls(size)
        }

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
                `data_collection_content_id`      INTEGER NOT NULL,
                `data_collection_id`              INTEGER,
                `level`                           INTEGER,
                `position`                        INTEGER,
                `attribute_id`                    INTEGER,
                `attribute_composition_id`        INTEGER,
                `result`                          INTEGER,
                `value_str`                       TEXT    NOT NULL,
                `data_collection_date`            INTEGER NOT NULL,
                `_id`                             INTEGER NOT NULL,
                `data_collection_rule_content_id` INTEGER NOT NULL,
                PRIMARY KEY (`data_collection_content_id`)
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
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content__id` ON `data_collection_content` (`_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_data_collection_content_data_collection_rule_content_id` ON `data_collection_content` (`data_collection_rule_content_id`);")
            return r
        }
    }
}

