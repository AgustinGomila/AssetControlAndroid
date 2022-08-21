package com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.splitList
import com.dacosys.assetControl.model.users.userWarehouseArea.`object`.UserWarehouseArea
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.CHECK
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.COUNT
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.MOVE
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.SEE
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.USER_ID
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.getAllColumns
import com.dacosys.assetControl.model.users.userWarehouseArea.wsObject.UserWarehouseAreaObject


/**
 * Created by Agustin on 28/12/2016.
 */

class UserWarehouseAreaDbHelper {
    fun insert(
        userId: Long,
        warehouse_areaId: Long,
        see: Boolean,
        count: Boolean,
        move: Boolean,
        check: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newUserWarehouseArea = UserWarehouseArea(
            userId,
            warehouse_areaId,
            see,
            move,
            count,
            check
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                newUserWarehouseArea.toContentValues()
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

    fun insert(userWarehouseArea: UserWarehouseArea): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                userWarehouseArea.toContentValues()
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

    fun insert(uwaArray: Array<UserWarehouseAreaObject>?): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        if (uwaArray == null || uwaArray.isEmpty()) {
            return false
        }

        val splitList = splitList(uwaArray, 100)
        var error = false

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            for (part in splitList) {
                var insertQ = ("INSERT INTO $TABLE_NAME " +
                        "($USER_ID," +
                        "$WAREHOUSE_AREA_ID," +
                        "$SEE," +
                        "$MOVE," +
                        "[$COUNT]," +
                        "[$CHECK]) VALUES ")

                for (uwa in part) {
                    Log.d(
                        this::class.java.simpleName,
                        "SQLITE-QUERY-INSERT-->" + uwa.user_id + "," + uwa.warehouse_area_id
                    )

                    val values =
                        "(${uwa.user_id},${uwa.warehouse_area_id},${uwa.see},${uwa.move},${uwa.count},${uwa.check}),"
                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                sqLiteDatabase.execSQL(insertQ)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
        } finally {
            sqLiteDatabase.endTransaction()
        }

        return !error
    }

    fun updateWarehouseAreaId(newWarehouseAreaId: Long, oldWarehouseAreaId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateWarehouseAreaId")

        val selection = "$WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseAreaId.toString())
        val values = ContentValues()
        values.put(WAREHOUSE_AREA_ID, newWarehouseAreaId)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                values,
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

    fun deleteByUserId(userId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByUserId ($userId)")

        val selection = "$USER_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(userId.toString())

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

    fun get(): ArrayList<UserWarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> get")

        val columns = getAllColumns()
        val order = "$USER_ID, $WAREHOUSE_AREA_ID"

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                null,// Columnas para la cláusula WHERE
                null, // Valores a comparar con las columnas del WHERE
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

    fun getByUserId(userId: Long): UserWarehouseArea? {
        Log.i(this::class.java.simpleName, ": SQLite -> getByUserId")

        val columns = getAllColumns()
        val selection = "$USER_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(userId.toString())
        val order = "$USER_ID, $WAREHOUSE_AREA_ID"

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

    private fun fromCursor(c: Cursor?): ArrayList<UserWarehouseArea> {
        val result = ArrayList<UserWarehouseArea>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val see = it.getInt(it.getColumnIndexOrThrow(SEE)) == 1
                    val move = it.getInt(it.getColumnIndexOrThrow(MOVE)) == 1
                    val count = it.getInt(it.getColumnIndexOrThrow(COUNT)) == 1
                    val check = it.getInt(it.getColumnIndexOrThrow(CHECK)) == 1

                    val temp = UserWarehouseArea(
                        userId,
                        warehouseAreaId,
                        see,
                        move,
                        count,
                        check
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] " +
                "( [" + USER_ID + "] BIGINT NOT NULL, " +
                "[" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, " +
                "[" + SEE + "] INT NULL, " +
                "[" + MOVE + "] INT NULL, " +
                "[" + COUNT + "] INT NULL, " +
                "[" + CHECK + "] INT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])"
        )
    }
}
