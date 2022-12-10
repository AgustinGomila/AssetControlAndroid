package com.dacosys.assetControl.model.routes.routeComposition.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.routes.routeComposition.`object`.RouteComposition
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.EXPRESSION
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.FALSE_RESULT
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.LEVEL
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.POSITION
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.TRUE_RESULT
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.RouteCompositionEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.routes.routeComposition.dbHelper.RouteCompositionContract.getAllColumns
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class RouteCompositionDbHelper {
    fun insert(
        routeId: Long,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        assetId: Long,
        warehouseId: Long,
        warehouseAreaId: Long,
        expression: String,
        trueResult: Int,
        falseResult: Int,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newRouteComposition = RouteComposition(
            routeId,
            dataCollectionRuleId,
            level,
            position,
            assetId,
            warehouseId,
            warehouseAreaId,
            expression,
            trueResult,
            falseResult
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newRouteComposition.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(objArray: ArrayList<RouteComposition>): Boolean {
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

    private fun insertChunked(objArray: List<RouteComposition>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        var query = "INSERT INTO [" + TABLE_NAME + "] (" +
                ROUTE_ID + "," +
                DATA_COLLECTION_RULE_ID + "," +
                LEVEL + "," +
                POSITION + "," +
                ASSET_ID + "," +
                WAREHOUSE_ID + "," +
                WAREHOUSE_AREA_ID + "," +
                EXPRESSION + "," +
                TRUE_RESULT + "," +
                FALSE_RESULT + ")" +
                " VALUES "

        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> insert: id:%s", obj.routeId)
            )
            val values = "(${obj.routeId}," +
                    "${obj.dataCollectionRuleId}," +
                    "${obj.level}," +
                    "${obj.position}," +
                    "${obj.assetId}," +
                    "${obj.warehouseId}," +
                    "${obj.warehouseAreaId}," +
                    "'${obj.expression?.replace("'", "''")}'," +
                    "${obj.trueResult}," +
                    "${obj.falseResult}),"
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

    fun deleteByRouteId(routeId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByRouteId ($routeId)")

        val selection = "$ROUTE_ID = ?" // WHERE routeId LIKE ?
        val selectionArgs = arrayOf(routeId.toString())

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

    fun select(): ArrayList<RouteComposition> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = ROUTE_ID

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

    fun selectByRouteId(routeId: Long): ArrayList<RouteComposition> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByRouteId ($routeId)")

        val columns = getAllColumns()
        val selection = "$ROUTE_ID = ?" // WHERE routeId LIKE ?
        val selectionArgs = arrayOf(routeId.toString())
        val order = ROUTE_ID

        val sqLiteDatabase = getReadableDb()
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
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<RouteComposition> {
        val result = ArrayList<RouteComposition>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val routeId = it.getLong(it.getColumnIndexOrThrow(ROUTE_ID))
                    val dataCollectionRuleId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(POSITION))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val expression = it.getString(it.getColumnIndexOrThrow(EXPRESSION))
                    val trueResult = it.getInt(it.getColumnIndexOrThrow(TRUE_RESULT))
                    val falseResult = it.getInt(it.getColumnIndexOrThrow(FALSE_RESULT))

                    val temp = RouteComposition(
                        routeId,
                        dataCollectionRuleId,
                        level,
                        position,
                        assetId,
                        warehouseId,
                        warehouseAreaId,
                        expression,
                        trueResult,
                        falseResult
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]" +
                "( [" + ROUTE_ID + "] BIGINT NOT NULL, " +
                " [" + DATA_COLLECTION_RULE_ID + "] BIGINT NOT NULL, " +
                " [" + LEVEL + "] INT NOT NULL, " +
                " [" + POSITION + "] INT NOT NULL, " +
                " [" + ASSET_ID + "] BIGINT NULL, " +
                " [" + WAREHOUSE_ID + "] BIGINT NULL, " +
                " [" + WAREHOUSE_AREA_ID + "] BIGINT NULL, " +
                " [" + EXPRESSION + "] NVARCHAR(4000) NULL, " +
                " [" + TRUE_RESULT + "] INT NULL, " +
                " [" + FALSE_RESULT + "] INT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$LEVEL]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$POSITION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_ID] ON [$TABLE_NAME] ([$ROUTE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$LEVEL] ON [$TABLE_NAME] ([$LEVEL])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$POSITION] ON [$TABLE_NAME] ([$POSITION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_ID] ON [$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])"
        )
    }
}