package com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.`object`.DataCollectionRuleTarget
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.DataCollectionRuleTargetEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionRuleTarget.dbHelper.DataCollectionRuleTargetContract.getAllColumns

/**
 * Created by Agustin on 28/12/2016.
 */

class DataCollectionRuleTargetDbHelper {
    fun insert(
        dataCollectionRuleId: Long,
        assetId: Long?,
        warehouseId: Long?,
        warehouseAreaId: Long?,
        itemCategoryId: Long?,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newDataCollectionRuleTarget = DataCollectionRuleTarget(
            dataCollectionRuleId,
            assetId,
            warehouseId,
            warehouseAreaId,
            itemCategoryId
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newDataCollectionRuleTarget.toContentValues()
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            return r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(objArray: ArrayList<DataCollectionRuleTarget>): Boolean {
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

    private fun insertChunked(objArray: List<DataCollectionRuleTarget>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        var query = "INSERT INTO [" + TABLE_NAME + "] (" +
                ITEM_CATEGORY_ID + "," +
                DATA_COLLECTION_RULE_ID + "," +
                WAREHOUSE_ID + "," +
                ASSET_ID + "," +
                WAREHOUSE_AREA_ID + ")" +
                " VALUES "

        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> insert")
            )
            val values = "(${obj.itemCategoryId}," +
                    "${obj.dataCollectionRuleId}," +
                    "${obj.warehouseId}," +
                    "${obj.assetId}," +
                    "${obj.warehouseAreaId}),"
            query = "$query$values"
        }

        if (query.endsWith(",")) {
            query = query.substring(0, query.length - 1)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
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

    fun deleteByDataCollectionRuleId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByDataCollectionRuleId ($id)")

        val selection = "$DATA_COLLECTION_RULE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteAll(): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteAll")

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.delete(
                TABLE_NAME,
                null,
                null
            ) > 0
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun select(): ArrayList<DataCollectionRuleTarget> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = DATA_COLLECTION_RULE_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
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

    private fun fromCursor(c: Cursor?): ArrayList<DataCollectionRuleTarget> {
        val result = ArrayList<DataCollectionRuleTarget>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dataCollectionRuleId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val itemCategoryId = it.getLong(it.getColumnIndexOrThrow(ITEM_CATEGORY_ID))

                    val temp = DataCollectionRuleTarget(
                        dataCollectionRuleId,
                        assetId,
                        warehouseId,
                        warehouseAreaId,
                        itemCategoryId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE [data_collection_rule_target] (
        [data_collection_rule_id] bigint NOT NULL ,
        [asset_id] bigint NULL ,
        [warehouse_id] bigint NULL ,
        [warehouse_area_id] bigint NULL ,
        [item_category_id] bigint NULL )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + DATA_COLLECTION_RULE_ID + "] BIGINT NOT NULL, "
                + " [" + ASSET_ID + "] BIGINT NULL ,"
                + " [" + WAREHOUSE_ID + "] BIGINT NULL ,"
                + " [" + WAREHOUSE_AREA_ID + "] BIGINT NULL ,"
                + " [" + ITEM_CATEGORY_ID + "] BIGINT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ITEM_CATEGORY_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_ID] ON [$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ITEM_CATEGORY_ID] ON [$TABLE_NAME] ([$ITEM_CATEGORY_ID])"
        )
    }
}