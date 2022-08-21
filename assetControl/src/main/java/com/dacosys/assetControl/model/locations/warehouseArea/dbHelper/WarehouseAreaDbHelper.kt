package com.dacosys.assetControl.model.locations.warehouseArea.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.splitList
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.model.locations.warehouseArea.wsObject.WarehouseAreaObject
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.sync.functions.SyncRegistryType
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry as uWa

/**
 * Created by Agustin on 28/12/2016.
 */

class WarehouseAreaDbHelper {
    fun sync(
        objArray: Array<WarehouseAreaObject>,
        callback: SyncTaskProgress,
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.warehouse_area_id)
            )

            val values = "($WAREHOUSE_AREA_ID = ${obj.warehouse_area_id}) OR "
            query = "$query$values"
        }

        if (query.endsWith(" OR ")) {
            query = query.substring(0, query.length - 4)
        }

        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(query)

            query = "INSERT INTO " + TABLE_NAME + " (" +
                    WAREHOUSE_AREA_ID + "," +
                    DESCRIPTION + "," +
                    ACTIVE + "," +
                    WAREHOUSE_ID + "," +
                    TRANSFERRED + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.warehouse_area_id)
                )
                count++
                callback.onSyncTaskProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.synchronizing_warehouse_areas),
                    registryType = SyncRegistryType.WarehouseArea,
                    progressStatus = ProgressStatus.running
                )

                val values = "(" +
                        obj.warehouse_area_id + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.active + "," +
                        obj.warehouse_id + "," +
                        1 + "),"

                query = "$query$values"
            }

            if (query.endsWith(",")) {
                query = query.substring(0, query.length - 1)
            }

            Log.d(this::class.java.simpleName, query)
            sqLiteDatabase.execSQL(query)
            sqLiteDatabase.setTransactionSuccessful()
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(
        warehouseAreaId: Long,
        description: String,
        active: Boolean,
        parentId: Long,
        transferred: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newWarehouseArea = WarehouseArea(
            warehouseAreaId,
            description,
            active,
            parentId,
            transferred
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                newWarehouseArea.toContentValues()
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

    fun insert(warehouseArea: WarehouseArea): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                warehouseArea.toContentValues()
            )
            sqLiteDatabase.setTransactionSuccessful()
            return r
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun getChangesCount(): Long {
        val db = StaticDbHelper.getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
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

    fun updateTransferred(warehouseAreaId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")

        /*
        UPDATE asset
        SET transfered = 1
        WHERE (asset_id = @asset_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERRED + " = 1 " +
                    " WHERE ( " + WAREHOUSE_AREA_ID + " = " + warehouseAreaId + ")"

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

    fun updateWarehouseId(newWarehouseId: Long, oldWarehouseId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateWarehouseId")

        val selection = "$WAREHOUSE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseId.toString())
        val values = ContentValues()
        values.put(WAREHOUSE_ID, newWarehouseId)

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

    fun update(wa: WarehouseAreaObject): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(wa.warehouse_area_id.toString())

        val values = ContentValues()
        values.put(WAREHOUSE_AREA_ID, wa.warehouse_area_id)
        values.put(DESCRIPTION, wa.description)
        values.put(ACTIVE, wa.active)
        values.put(WAREHOUSE_ID, wa.warehouse_id)
        values.put(TRANSFERRED, 0)

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

    fun update(warehouseArea: WarehouseArea): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(warehouseArea.warehouseAreaId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                warehouseArea.toContentValues(),
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

    fun delete(warehouseArea: WarehouseArea): Boolean {
        return deleteById(warehouseArea.warehouseAreaId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where = " WHERE ( " +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + Statics.currentUserId + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectNoTransfered(): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectNoTransfered")

        val where = " WHERE ( " +
                TABLE_NAME + "." + TRANSFERRED + " = 0)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectById(id: Long): WarehouseArea? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        /*
        SELECT
            warehouse_area.warehouse_area_id,
            warehouse_area.description,
            warehouse_area.active,
            warehouse_area.warehouse_id,
            warehouse.description AS warehouse_str,
            warehouse_area.transferred
        FROM warehouse_area
        LEFT OUTER JOIN [warehouse] ON [warehouse_area].warehouse_id = warehouse.warehouse_id
        WHERE
            (warehouse_area.warehouse_area_id = @warehouse_area_id) AND
            (warehouse_area.warehouse_area_id IN (
                SELECT user_warehouse_area.warehouse_area_id
                FROM   user_warehouse_area
                WHERE  user_warehouse_area.user_id = @user_id AND
                       user_warehouse_area.see = 1 )))
        ORDER BY warehouse_area.description
         */

        val where = " WHERE ( " +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " = " + id + ") AND (" +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + Statics.currentUserId + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
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

    fun selectByWarehouseId(wId: Long, onlyActive: Boolean): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByWarehouseId ($wId)")

        val where = " WHERE ( " +
                if (onlyActive) {
                    "$TABLE_NAME.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                TABLE_NAME + "." + WAREHOUSE_ID + " = " + wId + ") AND (" +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + Statics.currentUserId + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun select(onlyActive: Boolean): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where = " WHERE ( " +
                if (onlyActive) {
                    "$TABLE_NAME.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + Statics.currentUserId + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByDescription(
        waDescription: String,
        wDescription: String,
        onlyActive: Boolean,
    ): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($waDescription)")

        val wTable = WarehouseContract.WarehouseEntry.TABLE_NAME
        val wColDesc = WarehouseContract.WarehouseEntry.DESCRIPTION

        val where = " WHERE ( " +
                if (onlyActive) {
                    "$TABLE_NAME.$ACTIVE = 1) AND ("
                } else {
                    ""
                } +
                TABLE_NAME + "." + DESCRIPTION + " LIKE '%" + waDescription + "%') AND (" +
                wTable + "." + wColDesc + " LIKE '%" + wDescription + "%') AND (" +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + Statics.currentUserId + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<WarehouseArea> {
        val result = ArrayList<WarehouseArea>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val transferred = it.getInt(it.getColumnIndexOrThrow(TRANSFERRED)) == 1

                    val temp = WarehouseArea(
                        warehouseAreaId = id,
                        description = description,
                        active = active,
                        warehouseId = warehouseId,
                        transferred = transferred
                    )

                    temp.warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""
                    result.add(temp)
                }
            }
        }
        return result
    }

    val minId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> minId")

            val sqLiteDatabase = StaticDbHelper.getReadableDb()
            sqLiteDatabase.beginTransaction()
            return try {
                val mCount =
                    sqLiteDatabase.rawQuery("SELECT MIN($WAREHOUSE_AREA_ID) FROM $TABLE_NAME", null)
                sqLiteDatabase.setTransactionSuccessful()
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                if (count > 0) {
                    -1
                } else {
                    count - 1
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            } finally {
                sqLiteDatabase.endTransaction()
            }
        }

    // Funciones que guardan y recuperan IDs entre actividades
    // y evitar el error: !!! FAILED BINDER TRANSACTION !!!
    // cuando se pasa un objeto demasiado grande
    private fun createTempTable() {
        val allCommands: ArrayList<String> = ArrayList()
        allCommands.add(CREATE_TEMP_TABLE)

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        for (sql in allCommands) {
            println("$sql;")
            sqLiteDatabase.execSQL(sql)
        }
    }

    fun selectTempId(): ArrayList<WarehouseArea> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where =
            " WHERE $TABLE_NAME.$WAREHOUSE_AREA_ID IN (SELECT $temp$TABLE_NAME.$temp$WAREHOUSE_AREA_ID FROM $temp$TABLE_NAME)"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                where +
                " ORDER BY " + TABLE_NAME + "." + WAREHOUSE_AREA_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insertTempId(arrayId: ArrayList<Long>): Boolean {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val splitList = splitList(arrayId.toTypedArray(), 100)

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.delete("$temp${TABLE_NAME}", null, null)

            for (part in splitList) {
                var insertQ =
                    "INSERT INTO " + temp + TABLE_NAME + " (" + temp + WAREHOUSE_AREA_ID + ") VALUES "

                for (t in part) {
                    Log.d(this::class.java.simpleName, "SQLITE-QUERY-INSERT-->$t")

                    val values = "(${t}),"
                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }
                sqLiteDatabase.execSQL(insertQ)
            }
            sqLiteDatabase.setTransactionSuccessful()
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }
    // endregion TABLA E IDS TEMPORALES

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + WAREHOUSE_AREA_ID + "," +
            TABLE_NAME + "." + DESCRIPTION + "," +
            TABLE_NAME + "." + ACTIVE + "," +
            TABLE_NAME + "." + WAREHOUSE_ID + "," +
            TABLE_NAME + "." + TRANSFERRED

    private val basicStrFields =
        WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + WarehouseContract.WarehouseEntry.TABLE_NAME + "_str"

    private val basicLeftJoin =
        " LEFT OUTER JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " + TABLE_NAME + "." + WAREHOUSE_ID + " = " + WarehouseContract.WarehouseEntry.TABLE_NAME + "." + WarehouseContract.WarehouseEntry.WAREHOUSE_ID

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " [" + WAREHOUSE_ID + "] BIGINT NOT NULL, "
                + " [" + TRANSFERRED + "] INT ,"
                + " CONSTRAINT [PK_" + WAREHOUSE_AREA_ID + "] PRIMARY KEY ([" + WAREHOUSE_AREA_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])"
        )

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + temp + TABLE_NAME + "]"
                + "( [" + temp + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + temp + WAREHOUSE_AREA_ID + "] PRIMARY KEY ([" + temp + WAREHOUSE_AREA_ID + "]) )")

    }
}