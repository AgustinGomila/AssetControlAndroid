package com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract
import com.dacosys.assetControl.model.movements.warehouseMovement.`object`.WarehouseMovement
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.COLLECTOR_WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.COMPLETED
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.DESTINATION_WAREHOUSE_STR
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.OBS
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.ORIGIN_WAREHOUSE_STR
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.USER_ID
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.WAREHOUSE_MOVEMENT_DATE
import com.dacosys.assetControl.model.movements.warehouseMovement.dbHelper.WarehouseMovementContract.WarehouseMovementEntry.Companion.WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentDbHelper
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class WarehouseMovementDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($COLLECTOR_WAREHOUSE_MOVEMENT_ID) FROM $TABLE_NAME",
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
        origWarehouseAreaId: Long,
        destWarehouseAreaId: Long,
        obs: String,
    ): WarehouseMovement? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO warehouse_movement (
            warehouse_movement_date,
            user_id,
            origin_warehouse_area_id,
            origin_warehouse_id,
            destination_warehouse_area_id,
            destination_warehouse_id,
            collector_warehouse_movement_id,
            obs)
        VALUES (
            DATETIME('now', 'localtime'),
            @p1,
            @p2,
            (SELECT warehouse_area.warehouse_id FROM warehouse_area WHERE warehouse_area.warehouse_area_id = @p2),
            @p3,
            (SELECT warehouse_area.warehouse_id FROM warehouse_area WHERE warehouse_area.warehouse_area_id = @p3),
            @p4,
            @p5)
        */

        val userId = Statics.currentUserId ?: return null
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                WAREHOUSE_MOVEMENT_DATE + ", " +
                USER_ID + ", " +
                ORIGIN_WAREHOUSE_AREA_ID + ", " +
                ORIGIN_WAREHOUSE_ID + ", " +
                DESTINATION_WAREHOUSE_AREA_ID + ", " +
                DESTINATION_WAREHOUSE_ID + ", " +
                COLLECTOR_WAREHOUSE_MOVEMENT_ID + ", " +
                OBS + ")" +
                " VALUES (" +
                "DATETIME('now', 'localtime'), " +
                userId + ", " +
                origWarehouseAreaId + ", " +
                "(SELECT " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." + WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_ID +
                " FROM " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME +
                " WHERE " + WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + origWarehouseAreaId + "), " +
                destWarehouseAreaId + ", " +
                "(SELECT " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." + WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_ID +
                " FROM " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME +
                " WHERE " + WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + destWarehouseAreaId + "), " +
                newId + ", " +
                "'" + obs + "')"

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(insertQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return null
        } finally {
            sqLiteDatabase.endTransaction()
        }
        return selectById(newId)
    }

    fun insert(
        warehouseMovementId: Long,
        warehouseMovementDate: String,
        obs: String,
        userId: Long,
        origWarehouseAreaId: Long,
        origWarehouseId: Long,
        transferredDate: String?,
        destWarehouseAreaId: Long,
        destWarehouseId: Long,
        completed: Boolean,
    ): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newId = lastId
        val newWarehouseMovement = WarehouseMovement(
            warehouseMovementId,
            warehouseMovementDate,
            obs,
            userId,
            origWarehouseAreaId,
            origWarehouseId,
            transferredDate,
            destWarehouseAreaId,
            destWarehouseId,
            completed,
            newId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return if (sqLiteDatabase.insertOrThrow(
                    TABLE_NAME,
                    null,
                    newWarehouseMovement.toContentValues()
                ) > 0
            ) {
                newId
            } else {
                0L
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun insert(warehouseMovement: WarehouseMovement): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                warehouseMovement.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0L
        }
    }

    fun updateOriginWarehouseId(newWarehouseId: Long, oldWarehouseId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateOriginWarehouseId")

        val selection = "$ORIGIN_WAREHOUSE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseId.toString())
        val values = ContentValues()
        values.put(ORIGIN_WAREHOUSE_ID, newWarehouseId)

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

    fun updateOriginWarehouseAreaId(newWarehouseAreaId: Long, oldWarehouseAreaId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateOriginWarehouseAreaId")

        val selection = "$ORIGIN_WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseAreaId.toString())
        val values = ContentValues()
        values.put(ORIGIN_WAREHOUSE_AREA_ID, newWarehouseAreaId)

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

    fun updateDestWarehouseId(newWarehouseId: Long, oldWarehouseId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateDestWarehouseId")

        val selection = "$DESTINATION_WAREHOUSE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseId.toString())
        val values = ContentValues()
        values.put(DESTINATION_WAREHOUSE_ID, newWarehouseId)

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

    fun updateDestWarehouseAreaId(newWarehouseAreaId: Long, oldWarehouseAreaId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateDestWarehouseAreaId")

        val selection = "$DESTINATION_WAREHOUSE_AREA_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldWarehouseAreaId.toString())
        val values = ContentValues()
        values.put(DESTINATION_WAREHOUSE_AREA_ID, newWarehouseAreaId)

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

    fun update(warehouseMovement: WarehouseMovement): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$COLLECTOR_WAREHOUSE_MOVEMENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(warehouseMovement.collectorWarehouseMovementId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                warehouseMovement.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    private fun getChangesCount(): Long {
        val db = getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateTransferred(wmId: Long, collectorWmId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")

        /*
            UPDATE warehouse_movement
            SET
                transfered_date = DATETIME('now', 'localtime'),
                warehouse_movement_id = @warehouse_movement_id
            WHERE (collector_warehouse_movement_id = @collector_warehouse_movement_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERED_DATE + " = DATETIME('now', 'localtime'), " +
                    WAREHOUSE_MOVEMENT_ID + " = " + wmId +
                    " WHERE (" + COLLECTOR_WAREHOUSE_MOVEMENT_ID + " = " + collectorWmId + ")"

        val sqLiteDatabase = getWritableDb()
        val result: Boolean = try {
            val c = sqLiteDatabase.rawQuery(updateQ, null)
            c.moveToFirst()
            c.close()
            getChangesCount() > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
        return result
    }

    fun delete(warehouseMovement: WarehouseMovement): Boolean {
        return deleteById(warehouseMovement.collectorWarehouseMovementId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$COLLECTOR_WAREHOUSE_MOVEMENT_ID = ?" // WHERE code LIKE ?
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

    fun deleteTransferred(): Boolean {
        val res = try {
            val ar: ArrayList<WarehouseMovement> = selectTransferred()
            if (ar.size > 0) {
                val wmContDbHelper = WarehouseMovementContentDbHelper()
                for (a in ar) {
                    wmContDbHelper.deleteByWarehouseMovementId(a.collectorWarehouseMovementId)
                    deleteById(a.collectorWarehouseMovementId)
                }
            }
            true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }

        return res
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

    fun select(): ArrayList<WarehouseMovement> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_WAREHOUSE_MOVEMENT_ID

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

    fun selectById(collectorWarehouseMovementId: Long): WarehouseMovement? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($collectorWarehouseMovementId)")

        val where =
            " WHERE $TABLE_NAME.$COLLECTOR_WAREHOUSE_MOVEMENT_ID = $collectorWarehouseMovementId"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_WAREHOUSE_MOVEMENT_ID

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

    fun selectByNoTransferred(): ArrayList<WarehouseMovement> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByNoTransferred")

        /*
        WHERE
            (transfered_date IS NULL) AND
            (completed = 1)
        */

        val where = " WHERE $TABLE_NAME.$TRANSFERED_DATE IS NULL AND $TABLE_NAME.$COMPLETED = 1"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_WAREHOUSE_MOVEMENT_ID

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

    private fun selectTransferred(): ArrayList<WarehouseMovement> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectTransferred")

        val where = " WHERE $TABLE_NAME.$TRANSFERED_DATE IS NOT NULL"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_WAREHOUSE_MOVEMENT_ID

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

    private fun fromCursor(c: Cursor?): ArrayList<WarehouseMovement> {
        val result = ArrayList<WarehouseMovement>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val warehouseMovementId =
                        it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_ID))
                    val warehouseMovementDate =
                        it.getString(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_DATE))
                    val obs = it.getString(it.getColumnIndexOrThrow(OBS))
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val origWarehouseAreaId =
                        it.getLong(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_AREA_ID))
                    val origWarehouseId = it.getLong(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_ID))
                    val transferredDate = it.getString(it.getColumnIndexOrThrow(TRANSFERED_DATE))
                    val destWarehouseAreaId =
                        it.getLong(it.getColumnIndexOrThrow(DESTINATION_WAREHOUSE_AREA_ID))
                    val destWarehouseId =
                        it.getLong(it.getColumnIndexOrThrow(DESTINATION_WAREHOUSE_ID))
                    val completed = it.getInt(it.getColumnIndexOrThrow(COMPLETED)) == 1
                    val collectorWarehouseMovementId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_WAREHOUSE_MOVEMENT_ID))

                    val temp = WarehouseMovement(
                        warehouseMovementId = warehouseMovementId,
                        warehouseMovementDate = warehouseMovementDate,
                        obs = obs,
                        userId = userId,
                        origWarehouseAreaId = origWarehouseAreaId,
                        origWarehouseId = origWarehouseId,
                        transferredDate = transferredDate,
                        destWarehouseAreaId = destWarehouseAreaId,
                        destWarehouseId = destWarehouseId,
                        completed = completed,
                        collectorWarehouseMovementId = collectorWarehouseMovementId
                    )

                    temp.origWarehouseStr =
                        it.getString(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_STR))
                            ?: ""
                    temp.origWarehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_AREA_STR))
                            ?: ""
                    temp.destWarehouseStr =
                        it.getString(it.getColumnIndexOrThrow(DESTINATION_WAREHOUSE_STR))
                            ?: ""
                    temp.destWarehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(DESTINATION_WAREHOUSE_AREA_STR))
                            ?: ""

                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "warehouse_movement" (
        `warehouse_movement_id` bigint,
        `warehouse_movement_date` datetime NOT NULL,
        `obs` nvarchar ( 255 ),
        `user_id` bigint NOT NULL,
        `origin_warehouse_area_id` bigint NOT NULL,
        `origin_warehouse_id` bigint NOT NULL,
        `transfered_date` datetime,
        `destination_warehouse_area_id` bigint NOT NULL,
        `destination_warehouse_id` bigint NOT NULL,
        `completed` int,
        `_id` bigint NOT NULL,
        CONSTRAINT `PK__warehouse_movement__00000000000001D0` PRIMARY KEY(`_id`) ))
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] "
                + "( [" + WAREHOUSE_MOVEMENT_ID + "] BIGINT, "
                + " [" + WAREHOUSE_MOVEMENT_DATE + "] DATETIME NOT NULL, "
                + " [" + OBS + "] NVARCHAR ( 255 ), "
                + " [" + USER_ID + "] BIGINT NOT NULL, "
                + " [" + ORIGIN_WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + ORIGIN_WAREHOUSE_ID + "] BIGINT NOT NULL, "
                + " [" + TRANSFERED_DATE + "] DATETIME, "
                + " [" + DESTINATION_WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + DESTINATION_WAREHOUSE_ID + "] BIGINT NOT NULL, "
                + " [" + COMPLETED + "] INT, "
                + " [" + COLLECTOR_WAREHOUSE_MOVEMENT_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + COLLECTOR_WAREHOUSE_MOVEMENT_ID + "] PRIMARY KEY ([" + COLLECTOR_WAREHOUSE_MOVEMENT_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESTINATION_WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESTINATION_WAREHOUSE_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID] ON [$TABLE_NAME] ([$WAREHOUSE_MOVEMENT_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$ORIGIN_WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_ID] ON [$TABLE_NAME] ([$ORIGIN_WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESTINATION_WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$DESTINATION_WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESTINATION_WAREHOUSE_ID] ON [$TABLE_NAME] ([$DESTINATION_WAREHOUSE_ID])"
        )


        private const val basicSelect = "SELECT " +
                TABLE_NAME + "." + WAREHOUSE_MOVEMENT_ID + "," +
                TABLE_NAME + "." + WAREHOUSE_MOVEMENT_DATE + "," +
                TABLE_NAME + "." + OBS + "," +
                TABLE_NAME + "." + USER_ID + "," +
                TABLE_NAME + "." + ORIGIN_WAREHOUSE_AREA_ID + "," +
                TABLE_NAME + "." + ORIGIN_WAREHOUSE_ID + "," +
                TABLE_NAME + "." + DESTINATION_WAREHOUSE_AREA_ID + "," +
                TABLE_NAME + "." + DESTINATION_WAREHOUSE_ID + "," +
                TABLE_NAME + "." + TRANSFERED_DATE + "," +
                TABLE_NAME + "." + COMPLETED + "," +
                TABLE_NAME + "." + COLLECTOR_WAREHOUSE_MOVEMENT_ID

        private const val basicLeftJoin = " LEFT JOIN " +
                WarehouseContract.WarehouseEntry.TABLE_NAME + " AS orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " +
                "orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + TABLE_NAME + "." + ORIGIN_WAREHOUSE_ID +

                " LEFT JOIN " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " AS orig_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " +
                "orig_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + TABLE_NAME + "." + ORIGIN_WAREHOUSE_AREA_ID +

                " LEFT JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME + " AS dest_" + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " +
                "dest_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + TABLE_NAME + "." + DESTINATION_WAREHOUSE_ID +

                " LEFT JOIN " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " AS dest_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " +
                "dest_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + TABLE_NAME + "." + DESTINATION_WAREHOUSE_AREA_ID

        private const val basicStrFields =
            "orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                    WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + ORIGIN_WAREHOUSE_STR + "," +
                    "orig_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                    WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + ORIGIN_WAREHOUSE_AREA_STR + "," +
                    "dest_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                    WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + DESTINATION_WAREHOUSE_STR + "," +
                    "dest_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                    WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + DESTINATION_WAREHOUSE_AREA_STR
    }
}