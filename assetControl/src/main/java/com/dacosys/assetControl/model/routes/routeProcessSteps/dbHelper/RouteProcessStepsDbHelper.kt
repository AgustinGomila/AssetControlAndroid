package com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract
import com.dacosys.assetControl.model.routes.routeProcessSteps.`object`.RouteProcessSteps
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.LEVEL
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.POSITION
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.ROUTE_PROCESS_CONTENT_ID
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.STEP
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.RouteProcessStepsEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.routes.routeProcessSteps.dbHelper.RouteProcessStepsContract.getAllColumns

/**
 * Created by Agustin on 28/12/2016.
 */

class RouteProcessStepsDbHelper {
    fun insert(
        routeProcessId: Long,
        routeProcessContentId: Long,
        level: Int,
        position: Int,
        dataCollectionId: Long?,
    ): Boolean {
        val newRouteProcessSteps = RouteProcessSteps(
            routeProcessId,
            routeProcessContentId,
            level,
            position,
            dataCollectionId,
            lastStep(routeProcessId)
        )

        return try {
            insert(newRouteProcessSteps) > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    private fun lastStep(routeProcessId: Long): Int {
        Log.i(this::class.java.simpleName, ": SQLite -> lastStep")

        /*
        SELECT MAX(step) AS Expr1
        FROM route_process_steps
        WHERE (route_process_id = @route_process_id)
         */

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        try {
            val mCount = sqLiteDatabase.rawQuery(
                "SELECT MAX(" + STEP + ")" +
                        " FROM " + TABLE_NAME +
                        " WHERE (" + ROUTE_PROCESS_ID + " = " + routeProcessId + ")", null
            )
            mCount.moveToFirst()
            val count = mCount.getInt(0)
            mCount.close()
            return count + 1
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return 0
        } finally {
        }
    }

    private fun insert(routeProcessSteps: RouteProcessSteps): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                routeProcessSteps.toStepsValues()
            )
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteByRouteProcessId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByRouteProcessId ($id)")

        val selection = "$ROUTE_PROCESS_ID = ?" // WHERE code LIKE ?
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
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteByRouteProcessIdStep(id: Long, step: Int): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByRouteProcessIdStep (I:$id/S:$step)")

        val selection = "$ROUTE_PROCESS_ID = ? AND $STEP = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString(), step.toString())

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
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteByCollectorDataCollectionId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByCollectorDataCollectionId ($id)")

        val selection = "$DATA_COLLECTION_ID = ?" // WHERE code LIKE ?
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
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteByRouteIdRouteProcessDate(minDate: String, routeId: Long) {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> deleteByRouteIdRouteProcessDate (R:$routeId/D:$minDate)"
        )

        /*
        DELETE FROM route_process_steps
            WHERE (
                route_process_id IN
            (SELECT
                collector_route_process_id
            FROM route_process
            WHERE
                (route_process_date < @route_process_date) AND
                (transfered_date IS NOT NULL) AND
                (route_id = @route_id)))
        */

        val deleteQ: String = "DELETE FROM [" + TABLE_NAME + "] WHERE ( " +
                ROUTE_PROCESS_ID + " IN (SELECT " +
                RouteProcessContract.RouteProcessEntry.COLLECTOR_ROUTE_PROCESS_ID + " FROM " +
                RouteProcessContract.RouteProcessEntry.TABLE_NAME +
                " WHERE (" + RouteProcessContract.RouteProcessEntry.ROUTE_PROCESS_DATE + " < '" + minDate + "') AND " +
                "(" + RouteProcessContract.RouteProcessEntry.TRANSFERED_DATE + " IS NOT NULL) AND " +
                "(" + RouteProcessContract.RouteProcessEntry.ROUTE_ID + " = " + routeId + ")))"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(deleteQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
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

    fun select(): ArrayList<RouteProcessSteps> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = LEVEL

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

    fun selectByRouteProcessId(routeProcessId: Long): ArrayList<RouteProcessSteps> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByRouteProcessId ($routeProcessId)")

        val columns = getAllColumns()
        val selection = "$ROUTE_PROCESS_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(routeProcessId.toString())
        val order = LEVEL

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
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
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun getChangesCount(): Long {
        val db = StaticDbHelper.getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun update(dataCollectionId: Long?, routeProcessSteps: RouteProcessSteps): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        /*
        UPDATE route_process_steps
        SET
            data_collection_id = @data_collection_id
        WHERE
            (route_process_id = @route_process_id) AND
            (route_process_content_id = @route_process_content_id) AND
            (level = @level) AND
            (position = @position) AND
            (step = @step)
        */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    DATA_COLLECTION_ID + " = " + (dataCollectionId ?: "NULL") +
                    " WHERE ( " + ROUTE_PROCESS_ID + " = " + routeProcessSteps.routeProcessId + " AND " +
                    ROUTE_PROCESS_CONTENT_ID + " = " + routeProcessSteps.routeProcessContentId + " AND " +
                    LEVEL + " = " + routeProcessSteps.level + " AND " +
                    POSITION + " = " + routeProcessSteps.position + " AND " +
                    STEP + " = " + routeProcessSteps.step + " )"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(updateQ, null)
            c.moveToFirst()
            c.close()
            sqLiteDatabase.setTransactionSuccessful()
            getChangesCount() > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByCollectorRouteProcessContentId(id: Long): ArrayList<RouteProcessSteps> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByCollectorRouteProcessContentId ($id)"
        )

        val columns = getAllColumns()
        val selection = "$ROUTE_PROCESS_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = LEVEL

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
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

    private fun fromCursor(c: Cursor?): ArrayList<RouteProcessSteps> {
        val res = ArrayList<RouteProcessSteps>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val routeProcessId = it.getLong(it.getColumnIndexOrThrow(ROUTE_PROCESS_ID))
                    val routeProcessContentId =
                        it.getLong(it.getColumnIndexOrThrow(ROUTE_PROCESS_CONTENT_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(POSITION))
                    val dataCollectionId = it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_ID))
                    val step = it.getInt(it.getColumnIndexOrThrow(STEP))

                    val temp = RouteProcessSteps(
                        routeProcessId,
                        routeProcessContentId,
                        level,
                        position,
                        dataCollectionId,
                        step
                    )
                    res.add(temp)
                }
            }
        }
        return res
    }

    companion object {
        /*
            CREATE TABLE [route_process_steps] (
            [route_process_id] bigint NULL ,
            [route_process_content_id] bigint NULL ,
            [level] int NULL ,
            [position] int NULL ,
            [data_collection_id] bigint NULL ,
            [step] int NULL )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ROUTE_PROCESS_ID + "] BIGINT NULL, "
                + " [" + ROUTE_PROCESS_CONTENT_ID + "] BIGINT NULL, "
                + " [" + LEVEL + "] INT NULL, "
                + " [" + POSITION + "] INT NULL, "
                + " [" + DATA_COLLECTION_ID + "] BIGINT NULL, "
                + " [" + STEP + "] INT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_CONTENT_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$LEVEL]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$POSITION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$STEP]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_CONTENT_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_CONTENT_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$LEVEL] ON [$TABLE_NAME] ([$LEVEL])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$POSITION] ON [$TABLE_NAME] ([$POSITION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$STEP] ON [$TABLE_NAME] ([$STEP])"
        )
    }
}