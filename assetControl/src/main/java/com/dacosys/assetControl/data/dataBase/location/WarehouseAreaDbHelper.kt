package com.dacosys.assetControl.data.dataBase.location

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.AssetControlApp.Companion.getUserId
import com.dacosys.assetControl.BuildConfig
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.ACTIVE
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.data.model.location.WarehouseArea
import com.dacosys.assetControl.data.webservice.location.WarehouseAreaObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList
import com.dacosys.assetControl.data.dataBase.user.UserWarehouseAreaContract.UserWarehouseAreaEntry as uWa

/**
 * Created by Agustin on 28/12/2016.
 */

class WarehouseAreaDbHelper {
    fun sync(
        objArray: Array<WarehouseAreaObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
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

        val sqLiteDatabase = getWritableDb()
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
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = countTotal,
                        completedTask = currentCount + count,
                        msg = getContext()
                            .getString(R.string.synchronizing_warehouse_areas),
                        registryType = SyncRegistryType.WarehouseArea,
                        progressStatus = ProgressStatus.running
                    )
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

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newWarehouseArea.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(warehouseArea: WarehouseArea): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                warehouseArea.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    private fun getChangesCount(): Long {
        val db = getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateWarehouseAreaId(newWarehouseAreaId: Long, oldWarehouseAreaId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateWarehouseAreaId")

        val selection = "$WAREHOUSE_AREA_ID = ?"
        val selectionArgs = arrayOf(oldWarehouseAreaId.toString())
        val values = ContentValues()
        values.put(WAREHOUSE_AREA_ID, newWarehouseAreaId)

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun updateTransferred(itemIdArray: Array<Long>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")
        if (itemIdArray.isEmpty()) return false

        val sqLiteDatabase = getWritableDb()

        var error = false
        try {
            for (id in itemIdArray) {

                val values = ContentValues()
                values.put(TRANSFERRED, 1)

                val selection = "$WAREHOUSE_AREA_ID = ?"
                val args = arrayOf(id.toString())

                val updatedRows = sqLiteDatabase.update(TABLE_NAME, values, selection, args)

                if (BuildConfig.DEBUG) {
                    if (updatedRows > 0) Log.d(javaClass.simpleName, "Area ID: $id Updated")
                    else Log.e(javaClass.simpleName, "Area ID: $id NOT Updated!!!")
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
        }

        return !error
    }

    fun updateWarehouseId(newWarehouseId: Long, oldWarehouseId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateWarehouseId")

        val selection = "$WAREHOUSE_ID = ?"
        val selectionArgs = arrayOf(oldWarehouseId.toString())
        val values = ContentValues()
        values.put(WAREHOUSE_ID, newWarehouseId)

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun update(wa: WarehouseAreaObject): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$WAREHOUSE_AREA_ID = ?"
        val selectionArgs = arrayOf(wa.warehouse_area_id.toString())

        val values = ContentValues()
        values.put(WAREHOUSE_AREA_ID, wa.warehouse_area_id)
        values.put(DESCRIPTION, wa.description)
        values.put(ACTIVE, wa.active)
        values.put(WAREHOUSE_ID, wa.warehouse_id)
        values.put(TRANSFERRED, 0)

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun update(warehouseArea: WarehouseArea): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$WAREHOUSE_AREA_ID = ?"
        val selectionArgs = arrayOf(warehouseArea.warehouseAreaId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                warehouseArea.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(warehouseArea: WarehouseArea): Boolean {
        return deleteById(warehouseArea.warehouseAreaId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$WAREHOUSE_AREA_ID = ?"
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

    fun select(): ArrayList<WarehouseArea> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where = " WHERE ( " +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " IN (SELECT " +
                uWa.TABLE_NAME + "." + uWa.WAREHOUSE_AREA_ID +
                " FROM " + uWa.TABLE_NAME +
                " WHERE ( " +
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + getUserId() + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
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
            ArrayList()
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

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectById(id: Long?): WarehouseArea? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById (${id?.toString()})")

        if (id == null) {
            return null
        }

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
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + getUserId() + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
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
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + getUserId() + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
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
            ArrayList()
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
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + getUserId() + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
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
            ArrayList()
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
                uWa.TABLE_NAME + "." + uWa.USER_ID + " = " + getUserId() + " AND " +
                uWa.TABLE_NAME + "." + uWa.SEE + " = 1 )))"
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
            ArrayList()
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

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount =
                    sqLiteDatabase.rawQuery("SELECT MIN($WAREHOUSE_AREA_ID) FROM $TABLE_NAME", null)
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                if (count > 0) -1 else count - 1
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            }
        }

    // Funciones que guardan y recuperan Id entre actividades
    // y evitar el error: !!! FAILED BINDER TRANSACTION !!!
    // cuando se pasa un objeto demasiado grande
    private fun createTempTable() {
        val allCommands: ArrayList<String> = ArrayList()
        allCommands.add(CREATE_TEMP_TABLE)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            for (sql in allCommands) {
                sqLiteDatabase.execSQL(sql)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } finally {
            sqLiteDatabase.endTransaction()
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

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun insertTempId(arrayId: ArrayList<Long>): Boolean {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val splitList = splitList(arrayId.toTypedArray(), 100)

        val sqLiteDatabase = getWritableDb()

        try {
            sqLiteDatabase.delete("$temp${TABLE_NAME}", null, null)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        }

        try {
            sqLiteDatabase.beginTransaction()
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