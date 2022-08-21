package com.dacosys.assetControl.model.reviews.assetReview.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.splitList
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.ASSET_REVIEW_DATE
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.ASSET_REVIEW_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.COLLECTOR_ASSET_REVIEW_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.MODIFICATION_DATE
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.OBS
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.STATUS_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.USER_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.USER_STR
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.reviews.assetReview.dbHelper.AssetReviewContract.AssetReviewEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentDbHelper
import com.dacosys.assetControl.model.reviews.assetReviewStatus.`object`.AssetReviewStatus
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract.WarehouseEntry.Companion.DESCRIPTION as W_DESCRIPTION
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract.WarehouseEntry.Companion.TABLE_NAME as W_TABLE_NAME
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract.WarehouseEntry.Companion.WAREHOUSE_ID as W_ID
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.DESCRIPTION as WA_DESCRIPTION
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.TABLE_NAME as WA_TABLE_NAME
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID as WA_ID
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.DESCRIPTION as AS_DESCRIPTION
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.STATUS_ID as AS_ID
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.TABLE_NAME as AS_TABLE_NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.NAME as USER_NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.TABLE_NAME as U_TABLE_NAME
import com.dacosys.assetControl.model.users.user.dbHelper.UserContract.UserEntry.Companion.USER_ID as U_ID
import com.dacosys.assetControl.model.users.userWarehouseArea.dbHelper.UserWarehouseAreaContract.UserWarehouseAreaEntry as uWa

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetReviewDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = StaticDbHelper.getReadableDb()
            sqLiteDatabase.beginTransaction()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($COLLECTOR_ASSET_REVIEW_ID) FROM $TABLE_NAME",
                    null
                )
                sqLiteDatabase.setTransactionSuccessful()
                mCount.moveToFirst()
                val count = mCount.getLong(0)
                mCount.close()
                count + 1
            } catch (ex: SQLException) {
                ex.printStackTrace()
                ErrorLog.writeLog(null, this::class.java.simpleName, ex)
                0
            } finally {
                sqLiteDatabase.endTransaction()
            }
        }

    fun insert(warehouseArea: WarehouseArea): AssetReview? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")
        /*
        INSERT INTO asset_review (
            collector_asset_review_id,
            warehouse_area_id,
            warehouse_id,
            user_id,
            asset_review_date,
            obs,
            status_id,
            modification_date)
        VALUES (
            @p1,
            @p2,
            @p3,
            @p4,
            @p5,
            @p6,
            @p7,
            DATETIME('now', 'localtime'))
         */

        val obs = ""
        val userId = Statics.currentUserId ?: return null
        val warehouseAreaId = warehouseArea.warehouseAreaId
        val warehouseId = warehouseArea.warehouseId
        val statusId = AssetReviewStatus.onProcess.id
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                COLLECTOR_ASSET_REVIEW_ID + ", " +
                WAREHOUSE_AREA_ID + ", " +
                WAREHOUSE_ID + ", " +
                USER_ID + ", " +
                ASSET_REVIEW_DATE + ", " +
                OBS + ", " +
                STATUS_ID + ", " +
                MODIFICATION_DATE + ")" +
                " VALUES (" +
                newId + ", " +
                warehouseAreaId + ", " +
                warehouseId + ", " +
                userId + ", " +
                "DATETIME('now', 'localtime'), " +
                "'" + obs + "', " +
                statusId + ", " +
                "DATETIME('now', 'localtime'))"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(insertQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return null
        } finally {
            sqLiteDatabase.endTransaction()
        }
        return selectById(newId)
    }

    fun insert(
        assetReviewId: Long,
        assetReviewDate: String,
        obs: String,
        userId: Long,
        warehouseAreaId: Long,
        warehouseId: Long,
        modificationDate: String,
        statusId: Int,
    ): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newId = lastId
        val newAssetReview = AssetReview(
            assetReviewId,
            assetReviewDate,
            obs,
            userId,
            warehouseAreaId,
            warehouseId,
            modificationDate,
            newId,
            statusId
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = if (sqLiteDatabase.insertOrThrow(
                    TABLE_NAME,
                    null,
                    newAssetReview.toContentValues()
                ) > 0
            ) {
                newId
            } else {
                0
            }
            sqLiteDatabase.setTransactionSuccessful()
            r
        } catch (ex: SQLException) {
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(assetReview: AssetReview): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insert(
                TABLE_NAME, null,
                assetReview.toContentValues()
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

    fun update(assetReview: AssetReview): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$COLLECTOR_ASSET_REVIEW_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(assetReview.collectorAssetReviewId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                assetReview.toContentValues(),
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

    private fun getChangesCount(): Long {
        val db = StaticDbHelper.getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateTransferred(assetReviewId: Long, collectorAssetReviewId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")

        /*
        UPDATE asset_review
        SET
            modification_date = DATETIME('now', 'localtime'),
            asset_review_id = @asset_review_id,
            status_id = 3
        WHERE (collector_asset_review_id = @collector_asset_review_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    MODIFICATION_DATE + " = DATETIME('now', 'localtime'), " +
                    ASSET_REVIEW_ID + " = " + assetReviewId + ", " +
                    STATUS_ID + " = " + AssetReviewStatus.transferred.id +
                    " WHERE (" + COLLECTOR_ASSET_REVIEW_ID + " = " + collectorAssetReviewId + ")"

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

    fun delete(assetReview: AssetReview): Boolean {
        return deleteById(assetReview.collectorAssetReviewId)
    }

    fun deleteTransferred(): Boolean {
        val res = try {
            val ar: ArrayList<AssetReview> = selectTransferred()
            if (ar.size > 0) {
                val arContDbHelper = AssetReviewContentDbHelper()
                for (a in ar) {
                    arContDbHelper.deleteByAssetReviewId(a.collectorAssetReviewId)
                    deleteById(a.collectorAssetReviewId)
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

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$COLLECTOR_ASSET_REVIEW_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectById(collectorAssetReviewId: Long): AssetReview? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($collectorAssetReviewId)")

        val where = " WHERE ($TABLE_NAME.$COLLECTOR_ASSET_REVIEW_ID = $collectorAssetReviewId)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            val result = fromCursor(c)
            when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByWarehouseId(warehouseId: Long): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByWarehouseId ($warehouseId)")

        val where = " WHERE ($TABLE_NAME.$WAREHOUSE_ID = $warehouseId)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByWarehouseAreaId(warehouseAreaId: Long): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByWarehouseAreaId ($warehouseAreaId)")

        val where = " WHERE ($TABLE_NAME.$WAREHOUSE_AREA_ID = $warehouseAreaId)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByDescription(
        wDescription: String,
        waDescription: String,
        onlyActive: Boolean,
    ): ArrayList<AssetReview> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDescription (W:$wDescription/WA:$waDescription)"
        )

        val wColDesc = WarehouseContract.WarehouseEntry.DESCRIPTION
        val wColActive = WarehouseContract.WarehouseEntry.ACTIVE
        val waColDesc = WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION
        val waColActive = WarehouseAreaContract.WarehouseAreaEntry.ACTIVE
        val where = " WHERE ( " +
                if (onlyActive) {
                    "$WA_TABLE_NAME.$waColActive = 1 AND $W_TABLE_NAME.$wColActive = 1) AND ("
                } else {
                    ""
                } +
                WA_TABLE_NAME + "." + waColDesc + " LIKE '%" + waDescription + "%') AND (" +
                W_TABLE_NAME + "." + wColDesc + " LIKE '%" + wDescription + "%') AND (" +
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
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

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

    private fun selectTransferred(): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectTransferred")

        val where = " WHERE ($TABLE_NAME.$STATUS_ID = ${AssetReviewStatus.transferred.id})"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByCompleted(): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCompleted")

        val where = " WHERE ($TABLE_NAME.$STATUS_ID = ${AssetReviewStatus.completed.id})"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByUserWarehouseDate(userId: Long, warehouseId: Long, date: String): AssetReview? {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByUserWarehouseDate (U:$userId/W:$warehouseId/D:$date)"
        )

        val where = " WHERE (" + TABLE_NAME + "." + USER_ID + " = " + userId + ")" +
                " AND (" + TABLE_NAME + "." + WAREHOUSE_ID + " = " + warehouseId + ")" +
                " AND (" + TABLE_NAME + "." + ASSET_REVIEW_DATE + " = " + date + ")"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            val result = fromCursor(c)
            when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectOnProcess(): ArrayList<AssetReview> {
        return selectOnProcess(null)
    }

    private fun selectOnProcess(warehouseAreaId: Long?): ArrayList<AssetReview> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectOnProcess")

        /*
        SELECT
            asset_review.asset_review_id,
            asset_review.asset_review_date,
            asset_review.obs,
            asset_review.user_id,
            asset_review.warehouse_area_id,
            asset_review.warehouse_id,
            asset_review.modification_date,
            [user].name AS user_str,
            warehouse_area.description AS warehouse_area_str,
            warehouse.description AS warehouse_str,
            asset_review.status_id,
            status.description AS status_str,
            asset_review.collector_asset_review_id
        FROM asset_review
        LEFT OUTER JOIN warehouse ON asset_review.warehouse_id = warehouse.warehouse_id
        LEFT OUTER JOIN warehouse_area ON asset_review.warehouse_area_id = warehouse_area.warehouse_area_id
        LEFT OUTER JOIN [user] ON asset_review.user_id = [user].user_id
        LEFT OUTER JOIN status ON asset_review.status_id = status.status_id
        WHERE (asset_review.status_id = 1) AND (asset_review.warehouse_area_id = @warehouseAreaId)
         */

        val where =
            " WHERE (" + TABLE_NAME + "." + STATUS_ID + " = " + AssetReviewStatus.onProcess.id + ")" +
                    if (warehouseAreaId != null) {
                        " AND ($TABLE_NAME.$WAREHOUSE_AREA_ID = $warehouseAreaId)"
                    } else {
                        ""
                    }
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_REVIEW_DATE

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByWarehouseDate(warehouseId: Long, date: String): ArrayList<AssetReview> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByWarehouseDate (W:$warehouseId/D:$date)"
        )

        val where = " WHERE (" + TABLE_NAME + "." + WAREHOUSE_ID + " = " + warehouseId + ")" +
                " AND (" + TABLE_NAME + "." + ASSET_REVIEW_DATE + " = " + date + ")"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<AssetReview> {
        val result = ArrayList<AssetReview>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val assetReviewId = it.getLong(it.getColumnIndexOrThrow(ASSET_REVIEW_ID))
                    val assetReviewDate = it.getString(it.getColumnIndexOrThrow(ASSET_REVIEW_DATE))
                    val obs = it.getString(it.getColumnIndexOrThrow(OBS))
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val modificationDate = it.getString(it.getColumnIndexOrThrow(MODIFICATION_DATE))
                    val collectorAssetReviewId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_ASSET_REVIEW_ID))
                    val statusId = it.getInt(it.getColumnIndexOrThrow(STATUS_ID))

                    val temp = AssetReview(
                        assetReviewId = assetReviewId,
                        assetReviewDate = assetReviewDate,
                        obs = obs,
                        userId = userId,
                        warehouseAreaId = warehouseAreaId,
                        warehouseId = warehouseId,
                        modificationDate = modificationDate,
                        collectorAssetReviewId = collectorAssetReviewId,
                        statusId = statusId
                    )

                    temp.userStr = it.getString(it.getColumnIndexOrThrow(USER_STR)) ?: ""
                    temp.warehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR))
                            ?: ""
                    temp.warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""

                    result.add(temp)
                }
            }
        }
        return result
    }

    // region TABLA E IDS TEMPORALES

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

    fun selectTempId(): ArrayList<AssetReview> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where =
            " WHERE ${TABLE_NAME}.${COLLECTOR_ASSET_REVIEW_ID} IN (SELECT ${temp}${TABLE_NAME}.${temp}${COLLECTOR_ASSET_REVIEW_ID} FROM ${temp}${TABLE_NAME})"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID

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
            sqLiteDatabase.delete("${temp}${TABLE_NAME}", null, null)

            for (part in splitList) {
                var insertQ =
                    "INSERT INTO " + temp + TABLE_NAME + " (" + temp + COLLECTOR_ASSET_REVIEW_ID + ") VALUES "

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

    /*
    AssetReviewEntry.ASSET_REVIEW_ID,
                AssetReviewEntry.ASSET_REVIEW_DATE,
                AssetReviewEntry.OBS,
                AssetReviewEntry.USER_ID,
                AssetReviewEntry.WAREHOUSE_AREA_ID,
                AssetReviewEntry.WAREHOUSE_ID,
                AssetReviewEntry.MODIFICATION_DATE,
                AssetReviewEntry.COLLECTOR_ASSET_REVIEW_ID,
                AssetReviewEntry.STATUS_ID
     */

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + ASSET_REVIEW_ID + "," +
            TABLE_NAME + "." + ASSET_REVIEW_DATE + "," +
            TABLE_NAME + "." + OBS + "," +
            TABLE_NAME + "." + USER_ID + "," +
            TABLE_NAME + "." + WAREHOUSE_AREA_ID + "," +
            TABLE_NAME + "." + WAREHOUSE_ID + "," +
            TABLE_NAME + "." + MODIFICATION_DATE + "," +
            TABLE_NAME + "." + COLLECTOR_ASSET_REVIEW_ID + "," +
            TABLE_NAME + "." + STATUS_ID

    private val basicStrFields =
        "[" + U_TABLE_NAME + "]." + USER_NAME + " AS " + U_TABLE_NAME + "_str" + "," +
                WA_TABLE_NAME + "." + WA_DESCRIPTION + " AS " + WA_TABLE_NAME + "_str" + "," +
                W_TABLE_NAME + "." + W_DESCRIPTION + " AS " + W_TABLE_NAME + "_str" + "," +
                AS_TABLE_NAME + "." + AS_DESCRIPTION + " AS " + AS_TABLE_NAME + "_str"

    private val basicLeftJoin =
        " LEFT OUTER JOIN " + W_TABLE_NAME + " ON " + TABLE_NAME + "." + WAREHOUSE_ID + " = " + W_TABLE_NAME + "." + W_ID +
                " LEFT OUTER JOIN " + WA_TABLE_NAME + " ON " + TABLE_NAME + "." + WAREHOUSE_AREA_ID + " = " + WA_TABLE_NAME + "." + WA_ID +
                " LEFT OUTER JOIN [" + U_TABLE_NAME + "] ON " + TABLE_NAME + "." + USER_ID + " = [" + U_TABLE_NAME + "]." + U_ID +
                " LEFT OUTER JOIN " + AS_TABLE_NAME + " ON " + TABLE_NAME + "." + STATUS_ID + " = " + AS_TABLE_NAME + "." + AS_ID

    companion object {
        /*
        CREATE TABLE "asset_review" (
        `asset_review_id` bigint,
        `asset_review_date` datetime NOT NULL,
        `obs` nvarchar ( 255 ),
        `user_id` bigint NOT NULL,
        `warehouse_area_id` bigint NOT NULL,
        `warehouse_id` bigint NOT NULL,
        `modification_date` datetime NOT NULL,
        `collector_asset_review_id` bigint NOT NULL,
        `status_id` int NOT NULL,
        CONSTRAINT `PK__asset_review__00000000000001AD` PRIMARY KEY(`collector_asset_review_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] "
                + "( [" + ASSET_REVIEW_ID + "] BIGINT, "
                + " [" + ASSET_REVIEW_DATE + "] DATETIME NOT NULL, "
                + " [" + OBS + "] NVARCHAR ( 255 ), "
                + " [" + USER_ID + "] BIGINT NOT NULL, "
                + " [" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + WAREHOUSE_ID + "] BIGINT NOT NULL, "
                + " [" + MODIFICATION_DATE + "] DATETIME NOT NULL, "
                + " [" + COLLECTOR_ASSET_REVIEW_ID + "] BIGINT NOT NULL, "
                + " [" + STATUS_ID + "] INT NOT NULL, "
                + " CONSTRAINT [PK_" + COLLECTOR_ASSET_REVIEW_ID + "] PRIMARY KEY ([" + COLLECTOR_ASSET_REVIEW_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$COLLECTOR_ASSET_REVIEW_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$COLLECTOR_ASSET_REVIEW_ID] ON [$TABLE_NAME] ([$COLLECTOR_ASSET_REVIEW_ID])"
        )

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + temp + TABLE_NAME + "]"
                + "( [" + temp + COLLECTOR_ASSET_REVIEW_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + temp + COLLECTOR_ASSET_REVIEW_ID + "] PRIMARY KEY ([" + temp + COLLECTOR_ASSET_REVIEW_ID + "]) )")

    }
}