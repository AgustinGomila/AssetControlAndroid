package com.dacosys.assetControl.dataBase.datacollection

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.attribute.AttributeCompositionContract
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.ATTRIBUTE_COMPOSITION_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.ATTRIBUTE_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.COLLECTOR_DATA_COLLECTION_CONTENT_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_CONTENT_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_DATE
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.DATA_COLLECTION_RULE_CONTENT_ID
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.LEVEL
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.POSITION
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.RESULT
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.DataCollectionContentEntry.Companion.VALUE_STR
import com.dacosys.assetControl.dataBase.datacollection.DataCollectionContentContract.getAllColumns
import com.dacosys.assetControl.model.dataCollection.DataCollectionContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog

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
                    "SELECT MAX($COLLECTOR_DATA_COLLECTION_CONTENT_ID) FROM $TABLE_NAME",
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
            dataCollectionId,
            level,
            position,
            attributeId,
            attributeCompositionId,
            result,
            valueStr,
            dataCollectionDate,
            dataCollectionContentId,
            newId,
            dataCollectionRuleContentId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return if (sqLiteDatabase.insertOrThrow(
                    TABLE_NAME,
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
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                DATA_COLLECTION_ID + ", " +
                LEVEL + ", " +
                POSITION + ", " +
                ATTRIBUTE_ID + ", " +
                ATTRIBUTE_COMPOSITION_ID + ", " +
                RESULT + ", " +
                VALUE_STR + ", " +
                DATA_COLLECTION_DATE + ", " +
                COLLECTOR_DATA_COLLECTION_CONTENT_ID + ", " +
                DATA_COLLECTION_RULE_CONTENT_ID + ")" +
                " VALUES (" +
                collectorDcId + ", " +
                dcc.level + ", " +
                dcc.position + ", " +
                dcc.attributeId + ", " +
                dcc.attributeCompositionId + ", " +
                dcc.result + ", " +
                "'" + dcc.valueStr + "', " +
                "DATETIME('now', 'localtime'), " +
                newId + ", " +
                dcc.dataCollectionRuleContentId + ")"

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            sqLiteDatabase.execSQL(insertQ)
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

        val selection = "$DATA_COLLECTION_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(dataCollectionContent.dataCollectionContentId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
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

        val selection = "$DATA_COLLECTION_ID = ?" // WHERE code LIKE ?
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

        val selection = "$DATA_COLLECTION_CONTENT_ID = ?" // WHERE code LIKE ?
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

        val deleteQ: String = "DELETE FROM [" + TABLE_NAME + "] WHERE ( " +
                DATA_COLLECTION_ID + " NOT IN (SELECT " +
                DataCollectionContract.DataCollectionEntry.COLLECTOR_DATA_COLLECTION_ID + " FROM " +
                DataCollectionContract.DataCollectionEntry.TABLE_NAME + "))"

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

    fun select(): ArrayList<DataCollectionContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = LEVEL

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                null,// Columnas para la cláusula WHERE
                null,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectById(id: Long): DataCollectionContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$DATA_COLLECTION_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = LEVEL

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            val result = fromCursor(c)
            return when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return null
        } finally {
            sqLiteDatabase.endTransaction()
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

        val attrComp = AttributeCompositionContract.AttributeCompositionEntry
        val dc =
            DataCollectionContract.DataCollectionEntry

        val basicSelect = "SELECT " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + ", " +
                TABLE_NAME + "." + ATTRIBUTE_ID + ", " +
                TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_DATE + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + ", " +
                TABLE_NAME + "." + LEVEL + ", " +
                TABLE_NAME + "." + POSITION + ", " +
                TABLE_NAME + "." + RESULT + ", " +
                TABLE_NAME + "." + VALUE_STR + ", " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_TYPE_ID +
                " FROM " + TABLE_NAME +
                " LEFT OUTER JOIN " + attrComp.TABLE_NAME + " ON " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_ID + " = " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID +
                " LEFT OUTER JOIN " + dc.TABLE_NAME + " ON " +
                dc.TABLE_NAME + "." + dc.COLLECTOR_DATA_COLLECTION_ID + " = " +
                TABLE_NAME + "." + DATA_COLLECTION_ID +
                " WHERE (" + TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + " = " + dcrContId + ") AND " +
                "(" + dc.TABLE_NAME + "." + dc.ASSET_ID + " = " + assetId + ")"

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

        val attrComp = AttributeCompositionContract.AttributeCompositionEntry
        val dc =
            DataCollectionContract.DataCollectionEntry

        val basicSelect = "SELECT " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + ", " +
                TABLE_NAME + "." + ATTRIBUTE_ID + ", " +
                TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_DATE + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + ", " +
                TABLE_NAME + "." + LEVEL + ", " +
                TABLE_NAME + "." + POSITION + ", " +
                TABLE_NAME + "." + RESULT + ", " +
                TABLE_NAME + "." + VALUE_STR + ", " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_TYPE_ID +
                " FROM " + TABLE_NAME +
                " LEFT OUTER JOIN " + attrComp.TABLE_NAME + " ON " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_ID + " = " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID +
                " LEFT OUTER JOIN " + dc.TABLE_NAME + " ON " +
                dc.TABLE_NAME + "." + dc.COLLECTOR_DATA_COLLECTION_ID + " = " +
                TABLE_NAME + "." + DATA_COLLECTION_ID +
                " WHERE (" + TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + " = " + dcrContId + ") AND " +
                "(" + dc.TABLE_NAME + "." + dc.WAREHOUSE_ID + " = " + warehouseId + ")"

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

        val attrComp = AttributeCompositionContract.AttributeCompositionEntry
        val dc =
            DataCollectionContract.DataCollectionEntry

        val basicSelect = "SELECT " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + ", " +
                TABLE_NAME + "." + ATTRIBUTE_ID + ", " +
                TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_DATE + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + ", " +
                TABLE_NAME + "." + LEVEL + ", " +
                TABLE_NAME + "." + POSITION + ", " +
                TABLE_NAME + "." + RESULT + ", " +
                TABLE_NAME + "." + VALUE_STR + ", " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_TYPE_ID +
                " FROM " + TABLE_NAME +
                " LEFT OUTER JOIN " + attrComp.TABLE_NAME + " ON " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_ID + " = " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID +
                " LEFT OUTER JOIN " + dc.TABLE_NAME + " ON " +
                dc.TABLE_NAME + "." + dc.COLLECTOR_DATA_COLLECTION_ID + " = " +
                TABLE_NAME + "." + DATA_COLLECTION_ID +
                " WHERE (" + TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + " = " + dcrContId + ") AND " +
                "(" + dc.TABLE_NAME + "." + dc.WAREHOUSE_AREA_ID + " = " + warehouseAreaId + ")"

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

        val attrComp = AttributeCompositionContract.AttributeCompositionEntry
        val dc =
            DataCollectionContract.DataCollectionEntry

        val basicSelect = "SELECT " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID + ", " +
                TABLE_NAME + "." + ATTRIBUTE_ID + ", " +
                TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_CONTENT_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_DATE + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_ID + ", " +
                TABLE_NAME + "." + DATA_COLLECTION_RULE_CONTENT_ID + ", " +
                TABLE_NAME + "." + LEVEL + ", " +
                TABLE_NAME + "." + POSITION + ", " +
                TABLE_NAME + "." + RESULT + ", " +
                TABLE_NAME + "." + VALUE_STR + ", " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_TYPE_ID +
                " FROM " + TABLE_NAME +
                " LEFT OUTER JOIN " + attrComp.TABLE_NAME + " ON " +
                attrComp.TABLE_NAME + "." + attrComp.ATTRIBUTE_COMPOSITION_ID + " = " +
                TABLE_NAME + "." + ATTRIBUTE_COMPOSITION_ID +
                " LEFT OUTER JOIN " + dc.TABLE_NAME + " ON " +
                dc.TABLE_NAME + "." + dc.COLLECTOR_DATA_COLLECTION_ID + " = " +
                TABLE_NAME + "." + DATA_COLLECTION_ID +
                " WHERE (" + dc.TABLE_NAME + "." + dc.COLLECTOR_ROUTE_PROCESS_ID + " = " + crpId + ")"

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

        val columns = getAllColumns()
        val selection = "$DATA_COLLECTION_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = LEVEL

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns, // Lista de Columnas a consultar
                selection, // Columnas para la cláusula WHERE
                selectionArgs,// Valores a comparar con las columnas del WHERE
                null,// Agrupar con GROUP BY
                null, // Condición HAVING para GROUP BY
                order  // Cláusula ORDER BY
            )
            sqLiteDatabase.setTransactionSuccessful()
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<DataCollectionContent> {
        val res = ArrayList<DataCollectionContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dataCollectionId = it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(POSITION))
                    val attributeId = it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_ID))
                    val attributeCompositionId =
                        it.getLong(it.getColumnIndexOrThrow(ATTRIBUTE_COMPOSITION_ID))
                    val result = it.getInt(it.getColumnIndexOrThrow(RESULT))
                    val valueStr = it.getString(it.getColumnIndexOrThrow(VALUE_STR))
                    val dataCollectionDate =
                        it.getString(it.getColumnIndexOrThrow(DATA_COLLECTION_DATE))
                    val dataCollectionContentId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_CONTENT_ID))
                    val collectorDataCollectionContentId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_DATA_COLLECTION_CONTENT_ID))
                    val dataCollectionRuleContentId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_CONTENT_ID))

                    val temp = DataCollectionContent(
                        dataCollectionId,
                        level,
                        position,
                        attributeId,
                        attributeCompositionId,
                        result,
                        valueStr,
                        dataCollectionDate,
                        dataCollectionContentId,
                        collectorDataCollectionContentId,
                        dataCollectionRuleContentId
                    )
                    res.add(temp)
                }
            }
        }
        return res
    }

    companion object {
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

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + DATA_COLLECTION_ID + "] BIGINT, "
                + " [" + LEVEL + "] INT, "
                + " [" + POSITION + "] INT, "
                + " [" + ATTRIBUTE_ID + "] BIGINT, "
                + " [" + ATTRIBUTE_COMPOSITION_ID + "] BIGINT, "
                + " [" + RESULT + "] INT, "
                + " [" + VALUE_STR + "] TEXT NOT NULL, "
                + " [" + DATA_COLLECTION_DATE + "] DATETIME NOT NULL, "
                + " [" + DATA_COLLECTION_CONTENT_ID + "] BIGINT, "
                + " [" + COLLECTOR_DATA_COLLECTION_CONTENT_ID + "] BIGINT NOT NULL, "
                + " [" + DATA_COLLECTION_RULE_CONTENT_ID + "] BIGINT NOT NULL, "
                + " PRIMARY KEY( [" + DATA_COLLECTION_CONTENT_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$LEVEL]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$POSITION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$COLLECTOR_DATA_COLLECTION_CONTENT_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_CONTENT_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$LEVEL] ON [$TABLE_NAME] ([$LEVEL])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$POSITION] ON [$TABLE_NAME] ([$POSITION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ATTRIBUTE_COMPOSITION_ID] ON [$TABLE_NAME] ([$ATTRIBUTE_COMPOSITION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$COLLECTOR_DATA_COLLECTION_CONTENT_ID] ON [$TABLE_NAME] ([$COLLECTOR_DATA_COLLECTION_CONTENT_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_CONTENT_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_CONTENT_ID])"
        )
    }
}