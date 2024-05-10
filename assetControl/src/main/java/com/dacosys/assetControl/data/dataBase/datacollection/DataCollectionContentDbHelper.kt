package com.dacosys.assetControl.data.dataBase.datacollection

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.data.model.dataCollection.DataCollectionContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.data.dataBase.attribute.AttributeCompositionContract.AttributeCompositionEntry as AttrCompEntry
import com.dacosys.assetControl.data.dataBase.attribute.AttributeContract.AttributeEntry as AttrEntry
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry as Entry
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionContract.DataCollectionEntry as DcEntry
import com.dacosys.assetControl.data.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry as DcrcEntry

/**
 * Created by Agustin on 28/12/2016.
 */

class DataCollectionContentDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX(${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}) $BASIC_FROM",
                    null
                )
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                count + 1
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            }
        }

    fun insert(
        dataCollectionId: Long,
        level: Int,
        position: Int,
        attributeId: Long,
        attributeCompositionId: Long,
        result: Int,
        valueStr: String,
        dataCollectionDate: String,
        dataCollectionContentId: Long,
        dataCollectionRuleContentId: Long,
    ): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newId = lastId
        val newDataCollectionContent = DataCollectionContent(
            dataCollectionId = dataCollectionId,
            level = level,
            position = position,
            attributeId = attributeId,
            attributeCompositionId = attributeCompositionId,
            result = result,
            valueStr = valueStr,
            dataCollectionDate = dataCollectionDate,
            dataCollectionContentId = dataCollectionContentId,
            collectorDataCollectionContentId = newId,
            dataCollectionRuleContentId = dataCollectionRuleContentId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return if (sqLiteDatabase.insertOrThrow(
                    Entry.TABLE_NAME,
                    null,
                    newDataCollectionContent.toContentValues()
                ) > 0
            ) {
                newId
            } else {
                0
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun insert(collectorDcId: Long, dcc: DataCollectionContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO data_collection_content (
            data_collection_id,
            level,
            position,
            attribute_id,
            attribute_composition_id,
            result,
            value_str,
            data_collection_date,
            collector_data_collection_content_id,
            data_collection_rule_content_id)
        VALUES (
            @p1,
            @p2,
            @p3,
            @p4,
            @p5,
            @p6,
            @p8,
            DATETIME('now', 'localtime'),
            @p10,
            @p11)
        */

        val newId = lastId
        val insertQ: String =
            "INSERT INTO [${Entry.TABLE_NAME}] (" +
                    "${Entry.DATA_COLLECTION_ID}," +
                    "${Entry.LEVEL}," +
                    "${Entry.POSITION}," +
                    "${Entry.ATTRIBUTE_ID}," +
                    "${Entry.ATTRIBUTE_COMPOSITION_ID}," +
                    "${Entry.RESULT}," +
                    "${Entry.VALUE_STR}," +
                    "${Entry.DATA_COLLECTION_DATE}," +
                    "${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}," +
                    "${Entry.DATA_COLLECTION_RULE_CONTENT_ID})"
        val valuesQ: String =
            "VALUES (" +
                    "$collectorDcId," +
                    "${dcc.level}," +
                    "${dcc.position}," +
                    "${dcc.attributeId}," +
                    "${dcc.attributeCompositionId}," +
                    "${dcc.result}," +
                    "'${dcc.valueStr}'," +
                    "DATETIME('now', 'localtime')," +
                    "$newId," +
                    "${dcc.dataCollectionRuleContentId})"

        val q = "$insertQ $valuesQ"

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            sqLiteDatabase.execSQL(q)
            sqLiteDatabase.setTransactionSuccessful()
            true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun update(dataCollectionContent: DataCollectionContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "${Entry.DATA_COLLECTION_CONTENT_ID} = ?"
        val selectionArgs = arrayOf(dataCollectionContent.dataCollectionContentId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                Entry.TABLE_NAME,
                dataCollectionContent.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteByDataCollectionId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByDataCollectionId ($id)")

        val selection = "${Entry.DATA_COLLECTION_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                Entry.TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "${Entry.DATA_COLLECTION_CONTENT_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                Entry.TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteOrphans() {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteOrphans")

        /*
        DELETE FROM data_collection_content
        WHERE (
            data_collection_id NOT IN
            (SELECT
                collector_data_collection_id
            FROM data_collection))
        */

        val deleteQ: String =
            "DELETE FROM [${Entry.TABLE_NAME}] " +
                    "WHERE (${Entry.DATA_COLLECTION_ID} NOT IN (" +
                    "SELECT ${DcEntry.COLLECTOR_DATA_COLLECTION_ID} FROM ${DcEntry.TABLE_NAME}))"

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(deleteQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteAll(): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteAll")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                Entry.TABLE_NAME,
                null,
                null
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun select(): ArrayList<DataCollectionContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectById(id: Long): DataCollectionContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val where = " WHERE (${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_CONTENT_ID} = $id)"
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c).firstOrNull()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return null
        }
    }

    fun selectByDataCollectionRuleContentIdAssetId(
        dcrContId: Long,
        assetId: Long,
    ): ArrayList<DataCollectionContent> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDataCollectionRuleContentIdAssetId (D:$dcrContId/A:$assetId)"
        )

        /*
        SELECT
            data_collection_content.attribute_composition_id,
            data_collection_content.attribute_id,
            data_collection_content.collector_data_collection_content_id,
            data_collection_content.data_collection_content_id,
            data_collection_content.data_collection_date,
            data_collection_content.data_collection_id,
            data_collection_content.data_collection_rule_content_id,
            data_collection_content.level,
            data_collection_content.position,
            data_collection_content.result,
            data_collection_content.value_str,
            attribute_composition.attribute_composition_type_id
        FROM
            data_collection_content
        LEFT OUTER JOIN attribute_composition ON attribute_composition.attribute_composition_id = data_collection_content.attribute_composition_id
        LEFT OUTER JOIN data_collection ON data_collection.collector_data_collection_id = data_collection_content.data_collection_id
        WHERE
            (data_collection_content.data_collection_rule_content_id = @data_collection_rule_content_id) AND
            (data_collection.asset_id = @asset_id)
         */

        if (dcrContId <= 0 || assetId <= 0) {
            return ArrayList()
        }

        val where =
            " WHERE (${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = $dcrContId) AND " +
                    "(${DcEntry.TABLE_NAME}.${DcEntry.ASSET_ID} = $assetId)"
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByDataCollectionRuleContentIdWarehouseId(
        dcrContId: Long,
        warehouseId: Long,
    ): ArrayList<DataCollectionContent> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDataCollectionRuleContentIdWarehouseId (D:$dcrContId/W:$warehouseId)"
        )

        /*
        SELECT
            data_collection_content.attribute_composition_id,
            data_collection_content.attribute_id,
            data_collection_content.collector_data_collection_content_id,
            data_collection_content.data_collection_content_id,
            data_collection_content.data_collection_date,
            data_collection_content.data_collection_id,
            data_collection_content.data_collection_rule_content_id,
            data_collection_content.level,
            data_collection_content.position,
            data_collection_content.result,
            data_collection_content.value_str,
            attribute_composition.attribute_composition_type_id
        FROM
            data_collection_content
        LEFT OUTER JOIN attribute_composition ON attribute_composition.attribute_composition_id = data_collection_content.attribute_composition_id
        LEFT OUTER JOIN data_collection ON data_collection.collector_data_collection_id = data_collection_content.data_collection_id
        WHERE
            (data_collection_content.data_collection_rule_content_id = @data_collection_rule_content_id) AND
            (data_collection.warehouse_id = @warehouse_id)
         */

        if (dcrContId <= 0 || warehouseId <= 0) {
            return ArrayList()
        }

        val where =
            " WHERE (${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = $dcrContId) AND " +
                    "(${DcEntry.TABLE_NAME}.${DcEntry.WAREHOUSE_ID} = $warehouseId)"
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByDataCollectionRuleContentIdWarehouseAreaId(
        dcrContId: Long,
        warehouseAreaId: Long,
    ): ArrayList<DataCollectionContent> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDataCollectionRuleContentIdWarehouseAreaId (D:$dcrContId/W:$warehouseAreaId)"
        )

        /*
        SELECT
            data_collection_content.attribute_composition_id,
            data_collection_content.attribute_id,
            data_collection_content.collector_data_collection_content_id,
            data_collection_content.data_collection_content_id,
            data_collection_content.data_collection_date,
            data_collection_content.data_collection_id,
            data_collection_content.data_collection_rule_content_id,
            data_collection_content.level,
            data_collection_content.position,
            data_collection_content.result,
            data_collection_content.value_str,
            attribute_composition.attribute_composition_type_id
        FROM
            data_collection_content
        LEFT OUTER JOIN attribute_composition ON attribute_composition.attribute_composition_id = data_collection_content.attribute_composition_id
        LEFT OUTER JOIN data_collection ON data_collection.collector_data_collection_id = data_collection_content.data_collection_id
        WHERE
            (data_collection_content.data_collection_rule_content_id = @data_collection_rule_content_id) AND
            (data_collection.warehouse_area_id = @warehouse_area_id)
         */

        if (dcrContId <= 0 || warehouseAreaId <= 0) {
            return ArrayList()
        }

        val where =
            " WHERE (${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} = $dcrContId) AND " +
                    "(${DcEntry.TABLE_NAME}.${DcEntry.WAREHOUSE_AREA_ID} = $warehouseAreaId)"
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByCollectorRouteProcessId(crpId: Long): ArrayList<DataCollectionContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCollectorRouteProcessId ($crpId)")

        /*
        SELECT
            data_collection_content.attribute_composition_id,
            data_collection_content.attribute_id,
            data_collection_content.collector_data_collection_content_id,
            data_collection_content.data_collection_content_id,
            data_collection_content.data_collection_date,
            data_collection_content.data_collection_id,
            data_collection_content.data_collection_rule_content_id,
            data_collection_content.level,
            data_collection_content.position,
            data_collection_content.result,
            data_collection_content.value_str,
            attribute_composition.attribute_composition_type_id
        FROM data_collection_content
        LEFT OUTER JOIN attribute_composition ON attribute_composition.attribute_composition_id = data_collection_content.attribute_composition_id
        LEFT OUTER JOIN data_collection ON data_collection.collector_data_collection_id = data_collection_content.data_collection_id
        WHERE (data_collection.collector_route_process_id = @collector_route_process_id)
         */

        if (crpId <= 0) {
            return ArrayList()
        }

        val where = " WHERE (${DcEntry.TABLE_NAME}.${DcEntry.COLLECTOR_ROUTE_PROCESS_ID} = $crpId)"
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByCollectorDataCollectionId(id: Long): ArrayList<DataCollectionContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCollectorDataCollectionId ($id)")

        val where = "WHERE (${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} = $id) "
        val basicSelect = "SELECT $BASIC_SELECT, $JOIN_SELECT $BASIC_FROM $LEFT_JOIN $where $BASIC_ORDER"

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(basicSelect, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<DataCollectionContent> {
        val res = ArrayList<DataCollectionContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dataCollectionId = it.getLong(it.getColumnIndexOrThrow(Entry.DATA_COLLECTION_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(Entry.LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(Entry.POSITION))
                    val attributeId = it.getLong(it.getColumnIndexOrThrow(Entry.ATTRIBUTE_ID))
                    val attributeCompositionId = it.getLong(it.getColumnIndexOrThrow(Entry.ATTRIBUTE_COMPOSITION_ID))
                    val result = it.getInt(it.getColumnIndexOrThrow(Entry.RESULT))
                    val valueStr = it.getString(it.getColumnIndexOrThrow(Entry.VALUE_STR))
                    val dataCollectionDate = it.getString(it.getColumnIndexOrThrow(Entry.DATA_COLLECTION_DATE))
                    val dataCollectionContentId = it.getLong(it.getColumnIndexOrThrow(Entry.DATA_COLLECTION_CONTENT_ID))
                    val collectorDataCollectionContentId =
                        it.getLong(it.getColumnIndexOrThrow(Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID))
                    val dataCollectionRuleContentId =
                        it.getLong(it.getColumnIndexOrThrow(Entry.DATA_COLLECTION_RULE_CONTENT_ID))

                    val attributeStr = it.getString(it.getColumnIndexOrThrow(Entry.ATTRIBUTE_STR))
                    val attributeCompStr = it.getString(it.getColumnIndexOrThrow(Entry.ATTRIBUTE_COMPOSITION_STR))
                    val dcrContStr = it.getString(it.getColumnIndexOrThrow(Entry.DATA_COLLECTION_RULE_CONTENT_STR))

                    val temp = DataCollectionContent(
                        dataCollectionId = dataCollectionId,
                        level = level,
                        position = position,
                        attributeId = attributeId,
                        attributeCompositionId = attributeCompositionId,
                        result = result,
                        valueStr = valueStr,
                        dataCollectionDate = dataCollectionDate,
                        dataCollectionContentId = dataCollectionContentId,
                        collectorDataCollectionContentId = collectorDataCollectionContentId,
                        dataCollectionRuleContentId = dataCollectionRuleContentId
                    )
                    temp.attributeStr = attributeStr
                    temp.attributeCompositionStr = attributeCompStr
                    temp.dataCollectionRuleContentStr = dcrContStr
                    res.add(temp)
                }
            }
        }
        return res
    }

    companion object {
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${Entry.TABLE_NAME}.${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}, ${Entry.TABLE_NAME}.${Entry.LEVEL}, ${Entry.TABLE_NAME}.${Entry.POSITION}"

        const val BASIC_SELECT =
            "${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_CONTENT_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_DATE}," +
                    "${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID}," +
                    "${Entry.TABLE_NAME}.${Entry.LEVEL}," +
                    "${Entry.TABLE_NAME}.${Entry.POSITION}," +
                    "${Entry.TABLE_NAME}.${Entry.RESULT}," +
                    "${Entry.TABLE_NAME}.${Entry.VALUE_STR}"

        const val JOIN_SELECT =
            "${AttrCompEntry.TABLE_NAME}.${AttrCompEntry.ATTRIBUTE_COMPOSITION_TYPE_ID}," +
                    "${AttrCompEntry.TABLE_NAME}.${AttrCompEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_COMPOSITION_STR}," +
                    "${AttrEntry.TABLE_NAME}.${AttrEntry.DESCRIPTION} AS ${Entry.ATTRIBUTE_STR}," +
                    "${DcrcEntry.TABLE_NAME}.${DcrcEntry.DESCRIPTION} AS ${Entry.DATA_COLLECTION_RULE_CONTENT_STR}"

        const val LEFT_JOIN =
            "LEFT JOIN ${DcEntry.TABLE_NAME} ON ${DcEntry.TABLE_NAME}.${DcEntry.COLLECTOR_DATA_COLLECTION_ID} = ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_ID} " +
                    "LEFT JOIN ${AttrEntry.TABLE_NAME} ON ${AttrEntry.TABLE_NAME}.${AttrEntry.ATTRIBUTE_ID} = ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_ID} " +
                    "LEFT JOIN ${AttrCompEntry.TABLE_NAME} ON ${AttrCompEntry.TABLE_NAME}.${AttrCompEntry.ATTRIBUTE_COMPOSITION_ID} = ${Entry.TABLE_NAME}.${Entry.ATTRIBUTE_COMPOSITION_ID} " +
                    "LEFT JOIN ${DcrcEntry.TABLE_NAME} ON ${DcrcEntry.TABLE_NAME}.${DcrcEntry.DATA_COLLECTION_RULE_CONTENT_ID} = ${Entry.TABLE_NAME}.${Entry.DATA_COLLECTION_RULE_CONTENT_ID} "

        /*
        CREATE TABLE "data_collection_content" (
            `data_collection_id` bigint,
            `level` int,
            `position` int,
            `attribute_id` bigint,
            `attribute_composition_id` bigint,
            `result` int,
            `value_str` TEXT NOT NULL,
            `data_collection_date` datetime NOT NULL,
            `data_collection_content_id` bigint,
            `_id` bigint NOT NULL,
            `data_collection_rule_content_id` bigint NOT NULL,
        PRIMARY KEY(`_id`) )
         */

        const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS [${Entry.TABLE_NAME}] (" +
                "[${Entry.DATA_COLLECTION_ID}] BIGINT," +
                "[${Entry.LEVEL}] INT," +
                "[${Entry.POSITION}] INT," +
                "[${Entry.ATTRIBUTE_ID}] BIGINT," +
                "[${Entry.ATTRIBUTE_COMPOSITION_ID}] BIGINT," +
                "[${Entry.RESULT}] INT," +
                "[${Entry.VALUE_STR}] TEXT NOT NULL," +
                "[${Entry.DATA_COLLECTION_DATE}] DATETIME NOT NULL," +
                "[${Entry.DATA_COLLECTION_CONTENT_ID}] BIGINT," +
                "[${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}] BIGINT NOT NULL," +
                "[${Entry.DATA_COLLECTION_RULE_CONTENT_ID}] BIGINT NOT NULL," +
                "PRIMARY KEY( [${Entry.DATA_COLLECTION_CONTENT_ID}]))"

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.POSITION}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_ID}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}]",
            "DROP INDEX IF EXISTS [IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}]",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_ID}] ON [${Entry.TABLE_NAME}] ([${Entry.DATA_COLLECTION_ID}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.LEVEL}] ON [${Entry.TABLE_NAME}] ([${Entry.LEVEL}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.POSITION}] ON [${Entry.TABLE_NAME}] ([${Entry.POSITION}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_ID}] ON [${Entry.TABLE_NAME}] ([${Entry.ATTRIBUTE_ID}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.ATTRIBUTE_COMPOSITION_ID}] ON [${Entry.TABLE_NAME}] ([${Entry.ATTRIBUTE_COMPOSITION_ID}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}] ON [${Entry.TABLE_NAME}] ([${Entry.COLLECTOR_DATA_COLLECTION_CONTENT_ID}])",
            "CREATE INDEX [IDX_${Entry.TABLE_NAME}_${Entry.DATA_COLLECTION_RULE_CONTENT_ID}] ON [${Entry.TABLE_NAME}] ([${Entry.DATA_COLLECTION_RULE_CONTENT_ID}])"
        )
    }
}