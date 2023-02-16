package com.dacosys.assetControl.dataBase.datacollection

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.attribute.AttributeContract
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.ATTRIBUTE_STR
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DATA_COLLECTION_RULE_CONTENT_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.EXPRESSION
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.FALSE_RESULT
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.LEVEL
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.MANDATORY
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.POSITION
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionRuleContentContract.DataCollectionRuleContentEntry.Companion.TRUE_RESULT
import com.dacosys.assetControl.dataBase.route.RouteCompositionContract
import com.dacosys.assetControl.model.datacollection.DataCollectionRuleContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class DataCollectionRuleContentDbHelper {
    fun insert(
        dataCollectionRuleContentId: Long,
        description: String,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        attributeId: Long,
        attributeCompositionId: Long,
        expression: String,
        trueResult: Int,
        falseResult: Int,
        active: Boolean,
        mandatory: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newDataCollectionRuleContent = DataCollectionRuleContent(
            dataCollectionRuleContentId,
            description,
            dataCollectionRuleId,
            level,
            position,
            attributeId,
            attributeCompositionId,
            expression,
            trueResult,
            falseResult,
            active,
            mandatory
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newDataCollectionRuleContent.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(objArray: ArrayList<DataCollectionRuleContent>): Boolean {
        var result = false
        val r = objArray.chunked(10)
        for (s in r) {
            result = insertChunked(s.toList())
            if (!result) {
                break
            }
        }
        return result
    }

    private fun insertChunked(objArray: List<DataCollectionRuleContent>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        var query = "INSERT INTO [" + TABLE_NAME + "] (" +
                DATA_COLLECTION_RULE_CONTENT_ID + "," +
                DESCRIPTION + "," +
                DATA_COLLECTION_RULE_ID + "," +
                LEVEL + "," +
                POSITION + "," +
                ATTRIBUTE_ID + "," +
                ATTRIBUTE_COMPOSITION_ID + "," +
                EXPRESSION + "," +
                TRUE_RESULT + "," +
                FALSE_RESULT + "," +
                ACTIVE + "," +
                MANDATORY + ")" +
                " VALUES "

        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> insert")
            )
            val values = "(${obj.dataCollectionRuleContentId}," +
                    "'${obj.description.replace("'", "''")}'," +
                    "${obj.dataCollectionRuleId}," +
                    "${obj.level}," +
                    "${obj.position}," +
                    "${obj.attributeId}," +
                    "${obj.attributeCompositionId}," +
                    "'${obj.expression?.replace("'", "''")}'," +
                    "${obj.trueResult}," +
                    "${obj.falseResult}," +
                    "${if (obj.active) 1 else 0}," +
                    "${if (obj.mandatory) 1 else 0}),"
            query = "$query$values"
        }

        if (query.endsWith(",")) {
            query = query.substring(0, query.length - 1)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            sqLiteDatabase.execSQL(query)
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

    fun update(dataCollectionRuleContent: DataCollectionRuleContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$DATA_COLLECTION_RULE_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs =
            arrayOf(dataCollectionRuleContent.dataCollectionRuleContentId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                dataCollectionRuleContent.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(dataCollectionRuleContent: DataCollectionRuleContent): Boolean {
        return deleteById(dataCollectionRuleContent.dataCollectionRuleContentId)
    }

    fun deleteByDataCollectionRuleId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByDataCollectionRuleId ($id)")

        val selection = "$DATA_COLLECTION_RULE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
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

        val selection = "$DATA_COLLECTION_RULE_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteAll(): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteAll")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                null,
                null
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun select(): ArrayList<DataCollectionRuleContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectById(id: Long): DataCollectionRuleContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val where = " WHERE ( $TABLE_NAME.$DATA_COLLECTION_RULE_CONTENT_ID = $id)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            val result = fromCursor(c)
            when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        }
    }

    fun selectByDataCollectionRuleIdActive(id: Long): ArrayList<DataCollectionRuleContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDataCollectionRuleIdActive ($id)")

        val where =
            " WHERE ( $TABLE_NAME.$DATA_COLLECTION_RULE_ID = $id) AND ($TABLE_NAME.$ACTIVE = 1)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectByDataCollectionRuleId(id: Long): ArrayList<DataCollectionRuleContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDataCollectionRuleId ($id)")

        val where = " WHERE ( $TABLE_NAME.$DATA_COLLECTION_RULE_ID = $id)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectAttributeCompositionIdByRouteId(routeId: Long): ArrayList<Long> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectAttributeCompositionIdByRouteId ($routeId)"
        )

        /*
        SELECT data_collection_rule_content.attribute_composition_id
        FROM data_collection_rule_content
        LEFT OUTER JOIN data_collection_rule ON data_collection_rule.data_collection_rule_id = data_collection_rule_content.data_collection_rule_id
        LEFT OUTER JOIN route_composition ON route_composition.data_collection_rule_id = data_collection_rule.data_collection_rule_id
        WHERE
            (route_composition.route_id = @route_id) AND
            (data_collection_rule_content.attribute_composition_id IS NOT NULL) AND
            (data_collection_rule_content.attribute_composition_id > 0)
         */

        val rawQuery =
            "SELECT " + TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID +
                    " FROM " + TABLE_NAME +
                    " LEFT JOIN " + DataCollectionRuleContract.DataCollectionRuleEntry.TABLE_NAME + " ON " +
                    DataCollectionRuleContract.DataCollectionRuleEntry.TABLE_NAME + "." +
                    DataCollectionRuleContract.DataCollectionRuleEntry.DATA_COLLECTION_RULE_ID + " = " + TABLE_NAME + "." + DATA_COLLECTION_RULE_ID +
                    " LEFT JOIN " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + " ON " +
                    RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." +
                    RouteCompositionContract.RouteCompositionEntry.DATA_COLLECTION_RULE_ID + " = " + DataCollectionRuleContract.DataCollectionRuleEntry.TABLE_NAME + "." + DataCollectionRuleContract.DataCollectionRuleEntry.DATA_COLLECTION_RULE_ID +
                    " WHERE (" + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.ROUTE_ID + " = " + routeId + ") AND " +
                    "(" + TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + " IS NOT NULL) AND " +
                    "(" + TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + " > 0) AND " +
                    "(" + TABLE_NAME + "." + ACTIVE + " = 1)"

        val sqLiteDatabase = getReadableDb()
        try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            val result: ArrayList<Long> = ArrayList()
            c.use {
                if (it != null) {
                    while (it.moveToNext()) {
                        result.add(it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_ID)))
                    }
                }
            }
            return result
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectByDescription(description: String): ArrayList<DataCollectionRuleContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val where = " WHERE ( $DESCRIPTION LIKE %$description%)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<DataCollectionRuleContent> {
        val result = ArrayList<DataCollectionRuleContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dataCollectionRuleContentId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_CONTENT_ID))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val dataCollectionRuleId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(POSITION))
                    val attributeId = it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_ID))
                    val attributeCompositionId =
                        it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_ID))
                    val expression = it.getString(it.getColumnIndexOrThrow(EXPRESSION))
                    val trueResult = it.getInt(it.getColumnIndexOrThrow(TRUE_RESULT))
                    val falseResult = it.getInt(it.getColumnIndexOrThrow(FALSE_RESULT))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val mandatory = it.getInt(it.getColumnIndexOrThrow(MANDATORY)) == 1

                    val attributeStr = it.getString(it.getColumnIndexOrThrow(ATTRIBUTE_STR))

                    val temp = DataCollectionRuleContent(
                        dataCollectionRuleContentId,
                        description,
                        dataCollectionRuleId,
                        level,
                        position,
                        attributeId,
                        attributeCompositionId,
                        expression,
                        trueResult,
                        falseResult,
                        active,
                        mandatory
                    )

                    temp.attributeStr = attributeStr

                    result.add(temp)
                }
            }
        }
        return result
    }

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + "," +
            TABLE_NAME + "." + DESCRIPTION + "," +
            TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + "," +
            TABLE_NAME + "." + LEVEL + "," +
            TABLE_NAME + "." + POSITION + "," +
            TABLE_NAME + "." + ATTRIBUTE_ID + "," +
            TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + "," +
            TABLE_NAME + "." + EXPRESSION + "," +
            TABLE_NAME + "." + TRUE_RESULT + "," +
            TABLE_NAME + "." + FALSE_RESULT + "," +
            TABLE_NAME + "." + ACTIVE + "," +
            TABLE_NAME + "." + MANDATORY

    private val basicStrFields =
        AttributeContract.AttributeEntry.TABLE_NAME + "." +
                AttributeContract.AttributeEntry.DESCRIPTION + " AS " + AttributeContract.AttributeEntry.TABLE_NAME + "_str"

    private val basicLeftJoin =
        " LEFT OUTER JOIN " + AttributeContract.AttributeEntry.TABLE_NAME + " ON " + TABLE_NAME + "." + ATTRIBUTE_ID + " = " + AttributeContract.AttributeEntry.TABLE_NAME + "." + AttributeContract.AttributeEntry.ATTRIBUTE_ID

    companion object {
        /*
        CREATE TABLE "data_collection_rule_content" ( `_id` bigint NOT NULL,
        `data_collection_rule_id` bigint NOT NULL,
        `level` int NOT NULL,
        `position` int NOT NULL,
        `attribute_id` bigint,
        `attribute_composition_id` bigint,
        `expression` nvarchar ( 4000 ),
        `true_result` int,
        `false_result` int,
        `description` nvarchar ( 255 ) NOT NULL,
        `active` int NOT NULL,
        `mandatory` int NOT NULL,
        CONSTRAINT `PK_data_collection_rule_content` PRIMARY KEY(`_id`) )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + DATA_COLLECTION_RULE_CONTENT_ID + "] BIGINT NOT NULL, "
                + " [" + DATA_COLLECTION_RULE_ID + "] BIGINT NOT NULL, "
                + " [" + LEVEL + "] INT NOT NULL, "
                + " [" + POSITION + "] INT NOT NULL, "
                + " [" + ATTRIBUTE_ID + "] BIGINT, "
                + " [" + ATTRIBUTE_COMPOSITION_ID + "] BIGINT, "
                + " [" + EXPRESSION + "] NVARCHAR ( 4000 ), "
                + " [" + TRUE_RESULT + "] INT, "
                + " [" + FALSE_RESULT + "] INT, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " [" + MANDATORY + "] INT NOT NULL, "
                + " CONSTRAINT [PK_" + DATA_COLLECTION_RULE_CONTENT_ID + "] PRIMARY KEY ([" + DATA_COLLECTION_RULE_CONTENT_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$LEVEL]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$POSITION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$LEVEL] ON [$TABLE_NAME] ([$LEVEL])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$POSITION] ON [$TABLE_NAME] ([$POSITION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_COMPOSITION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}