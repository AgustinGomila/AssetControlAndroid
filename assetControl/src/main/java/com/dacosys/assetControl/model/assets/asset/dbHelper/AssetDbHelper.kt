package com.dacosys.assetControl.model.assets.asset.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.splitList
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.CODE
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.CONDITION
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.COST_CENTRE_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.EAN
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ITEM_CATEGORY_STR
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.LABEL_NUMBER
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.LAST_ASSET_REVIEW_DATE
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.MANUFACTURER
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.MISSING_DATE
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.MODEL
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.ORIGINAL_WAREHOUSE_STR
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.OWNERSHIP_STATUS
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.PARENT_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.SERIAL_NUMBER
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.STATUS
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.TRANSFERED
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract.AssetEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetCollectorObject
import com.dacosys.assetControl.model.assets.asset.wsObject.AssetObject
import com.dacosys.assetControl.model.assets.assetStatus.AssetStatus
import com.dacosys.assetControl.model.assets.itemCategory.dbHelper.ItemCategoryContract.ItemCategoryEntry
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract
import com.dacosys.assetControl.model.movements.warehouseMovement.`object`.WarehouseMovement
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.reviews.assetReviewContent.`object`.AssetReviewContent
import com.dacosys.assetControl.sync.functions.ProgressStatus
import com.dacosys.assetControl.sync.functions.Sync.Companion.SyncTaskProgress
import com.dacosys.assetControl.sync.functions.SyncRegistryType

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetDbHelper {
    fun insert(
        assetId: Long,
        code: String,
        description: String,
        warehouse_id: Long,
        warehouse_area_id: Long,
        active: Boolean,
        ownership_status: Int,
        status: Int,
        missing_date: String?,
        item_category_id: Long,
        transferred: Boolean,
        original_warehouse_id: Long,
        original_warehouse_area_id: Long,
        label_number: Int?,
        manufacturer: String?,
        model: String?,
        serial_number: String?,
        condition: Int,
        //cost_centre_id: Long,
        parent_id: Long,
        ean: String,
        last_asset_review_date: String?,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newAsset = Asset(
            assetId = assetId,
            code = code,
            description = description,
            warehouse_id = warehouse_id,
            warehouse_area_id = warehouse_area_id,
            active = active,
            ownership_status = ownership_status,
            status = status,
            missing_date = missing_date,
            item_category_id = item_category_id,
            transferred = transferred,
            original_warehouse_id = original_warehouse_id,
            original_warehouse_area_id = original_warehouse_area_id,
            label_number = label_number,
            manufacturer = manufacturer,
            model = model,
            serial_number = serial_number,
            condition = condition,
            //cost_centre_id,
            parent_id = parent_id,
            ean = ean,
            last_asset_review_date = last_asset_review_date
        )

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.insertOrThrow(
                TABLE_NAME,
                null,
                newAsset.toContentValues()
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

    fun sync(
        objArray: Array<AssetObject>,
        callback: SyncTaskProgress,
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.asset_id)
            )

            val values = "($ASSET_ID = ${obj.asset_id}) OR "
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
                    ASSET_ID + "," +
                    CODE + "," +
                    DESCRIPTION + "," +
                    WAREHOUSE_ID + "," +
                    WAREHOUSE_AREA_ID + "," +
                    ACTIVE + "," +
                    OWNERSHIP_STATUS + "," +
                    STATUS + "," +
                    MISSING_DATE + "," +
                    ITEM_CATEGORY_ID + "," +
                    TRANSFERED + "," +
                    ORIGINAL_WAREHOUSE_ID + "," +
                    ORIGINAL_WAREHOUSE_AREA_ID + "," +
                    LABEL_NUMBER + "," +
                    MANUFACTURER + "," +
                    MODEL + "," +
                    SERIAL_NUMBER + "," +
                    CONDITION + "," +
                    COST_CENTRE_ID + "," +
                    PARENT_ID + "," +
                    EAN + "," +
                    LAST_ASSET_REVIEW_DATE + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.asset_id)
                )
                count++
                callback.onSyncTaskProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = Statics.AssetControl.getContext()
                        .getString(R.string.synchronizing_assets),
                    registryType = SyncRegistryType.Asset,
                    progressStatus = ProgressStatus.running
                )

                var lastAssetReviewDate: String? = null
                if (obj.last_asset_review_date.isNotEmpty()) {
                    lastAssetReviewDate = obj.last_asset_review_date
                }

                var missingDate: String? = null
                if (obj.missing_date.isNotEmpty()) {
                    missingDate = obj.missing_date
                }

                var labelNumber: Int? = null
                if (obj.label_number > 0) {
                    labelNumber = obj.label_number
                }

                val values = "(" +
                        obj.asset_id + "," +
                        "'" + obj.code.replace("'", "''") + "'," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.warehouse_id + "," +
                        obj.warehouse_area_id + "," +
                        obj.active + "," +
                        obj.ownership_status + "," +
                        obj.status + "," +
                        (if (missingDate != null) {
                            "'$missingDate'"
                        } else {
                            "NULL"
                        }) + "," +
                        obj.item_category_id + "," +
                        1 + "," +
                        obj.original_warehouse_id + "," +
                        obj.original_warehouse_area_id + "," +
                        (labelNumber ?: "NULL") + "," +
                        "'" + obj.manufacturer.replace("'", "''") + "'," +
                        "'" + obj.model.replace("'", "''") + "'," +
                        "'" + obj.serial_number.replace("'", "''") + "'," +
                        obj.condition + "," +
                        obj.cost_centre_id + "," +
                        obj.parent_id + "," +
                        "'" + obj.ean + "'," +
                        (if (lastAssetReviewDate != null) {
                            "'$lastAssetReviewDate'"
                        } else {
                            "NULL"
                        }) + "),"

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

    fun insert(asset: Asset): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = if (sqLiteDatabase.insert(
                    TABLE_NAME,
                    null,
                    asset.toContentValues()
                ) > 0
            ) {
                asset.assetId
            } else {
                0
            }
            sqLiteDatabase.setTransactionSuccessful()
            r
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

    fun updateItemCategoryId(newItemCategoryId: Long, oldItemCategoryId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateItemCategoryId")

        val selection = "$ITEM_CATEGORY_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldItemCategoryId.toString())
        val values = ContentValues()
        values.put(ITEM_CATEGORY_ID, newItemCategoryId)

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

    fun update(asset: AssetCollectorObject): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(asset.asset_id.toString())

        val values = ContentValues()
        values.put(ACTIVE, asset.active)
        values.put(CONDITION, asset.condition)
        values.put(ASSET_ID, asset.asset_id)
        values.put(STATUS, asset.status)
        values.put(CODE, asset.code)
        values.put(DESCRIPTION, asset.description)
        values.put(EAN, asset.ean)
        values.put(ITEM_CATEGORY_ID, asset.item_category_id)
        values.put(LABEL_NUMBER, asset.label_number)
        values.put(LAST_ASSET_REVIEW_DATE, asset.last_asset_review_date)
        values.put(MISSING_DATE, asset.missing_date)
        values.put(ORIGINAL_WAREHOUSE_AREA_ID, asset.original_warehouse_area_id)
        values.put(ORIGINAL_WAREHOUSE_ID, asset.original_warehouse_id)
        values.put(OWNERSHIP_STATUS, asset.ownership_status)
        values.put(PARENT_ID, asset.parent_id)
        values.put(SERIAL_NUMBER, asset.serial_number)
        values.put(TRANSFERED, 0)
        values.put(WAREHOUSE_AREA_ID, asset.warehouse_area_id)
        values.put(WAREHOUSE_ID, asset.warehouse_id)

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

    fun update(asset: Asset): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(asset.assetId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                asset.toContentValues(),
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

    fun updateAssetId(newAssetId: Long, oldAssetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateAssetId")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldAssetId.toString())
        val values = ContentValues()
        values.put(ASSET_ID, newAssetId)

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

    private fun updateOnInventoryRemoved(wId: Long, waId: Long, assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateOnInventoryRemoved")

        /*
        UPDATE asset
        SET
            warehouse_id = @warehouse_id,
            warehouse_area_id = @warehouse_area_id,
            transfered = 0,
            last_asset_review_date = DATETIME('now', 'localtime')
        WHERE (asset_id = @asset_id)
        */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    WAREHOUSE_ID + " = " + wId + ", " +
                    WAREHOUSE_AREA_ID + " = " + waId + ", " +
                    TRANSFERED + " = 0, " +
                    LAST_ASSET_REVIEW_DATE + " = DATETIME('now', 'localtime')" +
                    " WHERE (" + ASSET_ID + " = " + assetId + ")"

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

    private fun updateLocation(wId: Long, waId: Long, assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateLocation")

        /*
        UPDATE asset
        SET
            warehouse_id = @warehouse_id,
            warehouse_area_id = @warehouse_area_id,
            transfered = 0
        WHERE (asset_id = @asset_id)
        */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    WAREHOUSE_ID + " = " + wId + ", " +
                    WAREHOUSE_AREA_ID + " = " + waId + ", " +
                    TRANSFERED + " = 0" +
                    " WHERE (" + ASSET_ID + " = " + assetId + ")"

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

    private fun updateOnInventory(wId: Long, waId: Long, assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateOnInventory")

        /*
        UPDATE asset
        SET
            warehouse_id = @warehouse_id,
            warehouse_area_id = @warehouse_area_id,
            transfered = 0,
            status = 1,
            missing_date = NULL,
            last_asset_review_date = DATETIME('now', 'localtime')
        WHERE (asset_id = @asset_id)
        */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    WAREHOUSE_ID + " = " + wId + ", " +
                    WAREHOUSE_AREA_ID + " = " + waId + ", " +
                    TRANSFERED + " = 0, " +
                    STATUS + " = " + AssetStatus.onInventory.id + ", " +
                    MISSING_DATE + " = NULL, " +
                    LAST_ASSET_REVIEW_DATE + " = DATETIME('now', 'localtime')" +
                    " WHERE (" + ASSET_ID + " = " + assetId + ")"

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

    private fun updateMissing(assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateMissing")

        /*
        UPDATE asset
        SET
            transfered = 0,
            status = 3,
            missing_date = DATETIME('now', 'localtime')
        WHERE (asset_id = @asset_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERED + " = 0, " +
                    STATUS + " = " + AssetStatus.missing.id + ", " +
                    MISSING_DATE + " = DATETIME('now', 'localtime')" +
                    " WHERE (" + ASSET_ID + " = " + assetId + ")"

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

    fun updateTransferred(assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")


        /*
        UPDATE asset
        SET transfered = 1
        WHERE (asset_id = @asset_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERED + " = 1 " +
                    " WHERE (" + ASSET_ID + " = " + assetId + ")"

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

    fun delete(asset: Asset): Boolean {
        return deleteById(asset.assetId)
    }

    fun deleteById(id: Long?): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id!!.toString())

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

    fun selectAllCodes(): ArrayList<String> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val result = ArrayList<String>()
        val rawQuery = "SELECT DISTINCT " + TABLE_NAME + "." + CODE +
                " FROM " + TABLE_NAME +
                " ORDER BY " + TABLE_NAME + "." + CODE

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            c.use {
                if (it != null) {
                    while (it.moveToNext()) {
                        result.add(it.getString(it.getColumnIndexOrThrow(CODE)))
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }

        return result
    }

    fun selectAllCodesByWarehouseAreaId(waId: Long): ArrayList<String> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val result = ArrayList<String>()
        val rawQuery = "SELECT DISTINCT " + TABLE_NAME + "." + CODE +
                " FROM " + TABLE_NAME +
                " WHERE " + TABLE_NAME + "." + WAREHOUSE_AREA_ID + " = " + waId +
                " ORDER BY " + TABLE_NAME + "." + CODE

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.setTransactionSuccessful()
            c.use {
                if (it != null) {
                    while (it.moveToNext()) {
                        result.add(it.getString(it.getColumnIndexOrThrow(CODE)))
                    }
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }

        return result
    }

    fun select(onlyActive: Boolean): ArrayList<Asset> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                if (onlyActive) {
                    " WHERE ${TABLE_NAME}.${ACTIVE} = 1"
                } else {
                    ""
                } +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectNoTransferred(): ArrayList<Asset> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectNoTransferred")

        val where = " WHERE $TABLE_NAME.$TRANSFERED = 0"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + CODE

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

    fun selectByWarehouseAreaIdActiveNotRemoved(waId: Long?): ArrayList<Asset> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByWarehouseAreaIdActiveNotRemoved (${waId?.toString()})"
        )

        if (waId == null || waId <= 0) {
            return ArrayList()
        }

        val where = " WHERE " +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + " = " + waId + " AND " +
                TABLE_NAME + "." + ACTIVE + " = 1 AND " +
                TABLE_NAME + "." + STATUS + " != " + AssetStatus.removed.id
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + CODE

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

    fun selectById(id: Long?): Asset? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById (${id?.toString()})")

        if (id == null) {
            return null
        }

        val where = " WHERE $TABLE_NAME.$ASSET_ID = $id"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectByDescriptionCodeEanItemCategoryIdWarehouseAreaId(
        searchText: String,
        itemCategoryId: Long?,
        warehouseAreaId: Long?,
        onlyActive: Boolean,
    ): ArrayList<Asset> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDescriptionCodeEanItemCategoryIdWarehouseAreaId (T:$searchText/IC:${
                itemCategoryId
                    ?: "NULL"
            }WA:${warehouseAreaId ?: "NULL"})"
        )

        var where = ""
        if (searchText.isNotEmpty()) {
            where = " WHERE (" +
                    TABLE_NAME + "." + DESCRIPTION + " LIKE '%" + searchText + "%' OR " +
                    TABLE_NAME + "." + CODE + " LIKE '%" + searchText + "%' OR " +
                    TABLE_NAME + "." + EAN + " LIKE '%" + searchText + "%')"
        }

        if (itemCategoryId != null) {
            where = if (where.isNotEmpty()) "$where AND (" else " WHERE ("
            where += "$TABLE_NAME.$ITEM_CATEGORY_ID = $itemCategoryId)"
        }

        if (warehouseAreaId != null) {
            where = if (where.isNotEmpty()) "$where AND (" else " WHERE ("
            where += "$TABLE_NAME.$WAREHOUSE_AREA_ID = $warehouseAreaId)"
        }

        if (onlyActive) {
            where = if (where.isNotEmpty()) "$where AND (" else " WHERE ("
            where += "$TABLE_NAME.$ACTIVE = 1)"
        }

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectByDescription(description: String): ArrayList<Asset> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        if (description.isEmpty()) {
            return ArrayList()
        }

        val where = " WHERE $TABLE_NAME.$DESCRIPTION LIKE '$description%'"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectByCode(code: String): ArrayList<Asset> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCode ($code)")

        if (code.isEmpty()) {
            return ArrayList()
        }

        val where = " WHERE $TABLE_NAME.$CODE = '$code'"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectBySerial(serial: String): ArrayList<Asset> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectBySerial ($serial)")

        if (serial.isEmpty()) {
            return ArrayList()
        }

        val where = " WHERE $TABLE_NAME.$SERIAL_NUMBER = '$serial'"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    private fun fromCursor(c: Cursor?): ArrayList<Asset> {
        val result = ArrayList<Asset>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))

                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val ownershipStatus = it.getInt(it.getColumnIndexOrThrow(OWNERSHIP_STATUS))
                    val status = it.getInt(it.getColumnIndexOrThrow(STATUS))
                    val missingDate = it.getString(it.getColumnIndexOrThrow(MISSING_DATE)) ?: ""
                    val itemCategoryId = it.getLong(it.getColumnIndexOrThrow(ITEM_CATEGORY_ID))
                    val transferred = it.getInt(it.getColumnIndexOrThrow(TRANSFERED)) == 1
                    val originalWarehouseId =
                        it.getLong(it.getColumnIndexOrThrow(ORIGINAL_WAREHOUSE_ID))
                    val originalWarehouseAreaId =
                        it.getLong(it.getColumnIndexOrThrow(ORIGINAL_WAREHOUSE_AREA_ID))
                    val labelNumber = it.getInt(it.getColumnIndexOrThrow(LABEL_NUMBER))
                    val manufacturer = it.getString(it.getColumnIndexOrThrow(MANUFACTURER)) ?: ""
                    val model = it.getString(it.getColumnIndexOrThrow(MODEL)) ?: ""
                    val serialNumber = it.getString(it.getColumnIndexOrThrow(SERIAL_NUMBER)) ?: ""
                    val condition = it.getInt(it.getColumnIndexOrThrow(CONDITION))
                    //val cost_centre_id = it.getInt(it.getColumnIndexOrThrow(COST_CENTRE_ID))
                    val parentId = it.getLong(it.getColumnIndexOrThrow(PARENT_ID))
                    val ean = it.getString(it.getColumnIndexOrThrow(EAN)) ?: ""
                    val lastAssetReviewDate =
                        it.getString(it.getColumnIndexOrThrow(LAST_ASSET_REVIEW_DATE))
                            ?: ""

                    val itemCategoryStr =
                        it.getString(it.getColumnIndexOrThrow(ITEM_CATEGORY_STR)) ?: ""
                    val warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""
                    val warehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR)) ?: ""
                    val originalWarehouseStr =
                        it.getString(it.getColumnIndexOrThrow(ORIGINAL_WAREHOUSE_STR))
                            ?: ""
                    val originalWarehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(ORIGINAL_WAREHOUSE_AREA_STR))
                            ?: ""

                    //val cost_centre_str = it.getString(it.getColumnIndexOrThrow(COST_CENTRE_STR))
                    //val status_str = it.getString(it.getColumnIndexOrThrow(STATUS_STR))
                    //val ownership_status_str = it.getString(it.getColumnIndexOrThrow(OWNERSHIP_STATUS_STR))
                    //val condition_str = it.getString(it.getColumnIndexOrThrow(CONDITION_STR))

                    val temp = Asset(
                        id,
                        code,
                        description,
                        warehouseId,
                        warehouseAreaId,
                        active,
                        ownershipStatus,
                        status,
                        missingDate.ifEmpty {
                            null
                        },
                        itemCategoryId,
                        transferred,
                        originalWarehouseId,
                        originalWarehouseAreaId,
                        labelNumber,
                        manufacturer,
                        model,
                        serialNumber,
                        condition,
                        //cost_centre_id,
                        parentId,
                        ean,
                        lastAssetReviewDate.ifEmpty {
                            null
                        }
                    )

                    temp.itemCategoryStr = itemCategoryStr
                    temp.warehouseStr = warehouseStr
                    temp.warehouseAreaStr = warehouseAreaStr
                    temp.originalWarehouseStr = originalWarehouseStr
                    temp.originalWarehouseAreaStr = originalWarehouseAreaStr

                    result.add(temp)
                }
            }
        }
        return result
    }

    fun codeExists(code: String, assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> codeExists")

        /*
        SELECT COUNT(*) AS Expr1
        FROM asset
        WHERE (code = @code) AND (asset_id <> @asset_id)
         */

        val countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME +
                " WHERE (" +
                CODE + " = '" + code + "') AND (" +
                ASSET_ID + " <> " + assetId + ")"

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        return DatabaseUtils.longForQuery(
            sqLiteDatabase,
            countQuery,
            null
        ) > 0
    }

    fun countAssets(warehouseAreaId: Long): Int {
        Log.i(this::class.java.simpleName, ": SQLite -> countAssets")

        /*
        SELECT COUNT(*) AS Expr1
        FROM asset
        WHERE (warehouse_area_id = @warehouse_area_id)
         */

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        return DatabaseUtils.queryNumEntries(
            sqLiteDatabase,
            TABLE_NAME,
            "$WAREHOUSE_AREA_ID = ?",
            arrayOf(warehouseAreaId.toString())
        ).toInt()
    }

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + ASSET_ID + "," +
            TABLE_NAME + "." + CODE + "," +
            TABLE_NAME + "." + DESCRIPTION + "," +
            TABLE_NAME + "." + WAREHOUSE_ID + "," +
            TABLE_NAME + "." + WAREHOUSE_AREA_ID + "," +
            TABLE_NAME + "." + ACTIVE + "," +
            TABLE_NAME + "." + OWNERSHIP_STATUS + "," +
            TABLE_NAME + "." + STATUS + "," +
            TABLE_NAME + "." + MISSING_DATE + "," +
            TABLE_NAME + "." + ITEM_CATEGORY_ID + "," +
            TABLE_NAME + "." + TRANSFERED + "," +
            TABLE_NAME + "." + ORIGINAL_WAREHOUSE_ID + "," +
            TABLE_NAME + "." + ORIGINAL_WAREHOUSE_AREA_ID + "," +
            TABLE_NAME + "." + LABEL_NUMBER + "," +
            TABLE_NAME + "." + MANUFACTURER + "," +
            TABLE_NAME + "." + MODEL + "," +
            TABLE_NAME + "." + SERIAL_NUMBER + "," +
            TABLE_NAME + "." + CONDITION + "," +
            TABLE_NAME + "." + COST_CENTRE_ID + "," +
            TABLE_NAME + "." + PARENT_ID + "," +
            TABLE_NAME + "." + EAN + "," +
            TABLE_NAME + "." + LAST_ASSET_REVIEW_DATE

    private val basicLeftJoin = " LEFT JOIN " + ItemCategoryEntry.TABLE_NAME + " ON " +
            ItemCategoryEntry.TABLE_NAME + "." +
            ItemCategoryEntry.ITEM_CATEGORY_ID + " = " + TABLE_NAME + "." + ITEM_CATEGORY_ID +

            " LEFT JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " +
            WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
            WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + TABLE_NAME + "." + WAREHOUSE_ID +

            " LEFT JOIN " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " +
            WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
            WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + TABLE_NAME + "." + WAREHOUSE_AREA_ID +

            " LEFT JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME +
            " AS orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " +
            "orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
            WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + TABLE_NAME + "." + ORIGINAL_WAREHOUSE_ID +

            " LEFT JOIN " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME +
            " AS orig_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " +
            "orig_" + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
            WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + TABLE_NAME + "." + ORIGINAL_WAREHOUSE_AREA_ID

    private val basicStrFields =
        ItemCategoryEntry.TABLE_NAME + "." +
                ItemCategoryEntry.DESCRIPTION + " AS " + ITEM_CATEGORY_STR + "," +

                WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + WAREHOUSE_STR + "," +

                WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + WAREHOUSE_AREA_STR + "," +

                "orig_" + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + ORIGINAL_WAREHOUSE_STR + "," +

                "orig_" +
                WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + ORIGINAL_WAREHOUSE_AREA_STR

    companion object {
        /*
        CREATE TABLE "asset" (
            `asset_id` BIGINT NOT NULL,
            `code` NVARCHAR ( 45 ) NOT NULL,
            `description` NVARCHAR ( 255 ) NOT NULL,
            `warehouse_id` BIGINT NOT NULL,
            `warehouse_area_id` BIGINT NOT NULL,
            `active` INT NOT NULL DEFAULT 1,
            `ownership_status` INT NOT NULL DEFAULT 1,
            `status` INT NOT NULL DEFAULT 1,
            `missing_date` DATETIME,
            `item_category_id` BIGINT NOT NULL DEFAULT 0,
            `transfered` INT,
            `original_warehouse_id` BIGINT NOT NULL,
            `original_warehouse_area_id` BIGINT NOT NULL,
            `label_number` INT,
            `manufacturer` NVARCHAR ( 255 ),
            `model` NVARCHAR ( 255 ),
            `serial_number` NVARCHAR ( 255 ),
            `condition` INT,
            `cost_centre_id` BIGINT,
            `parent_id` BIGINT,
            `ean` NVARCHAR ( 100 ),
            `last_asset_review_date` DATETIME,
        PRIMARY KEY(`asset_id`) )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]" +
                "( [" + ASSET_ID + "] BIGINT NOT NULL, " +
                " [" + CODE + "] NVARCHAR ( 45 ) NOT NULL, " +
                " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, " +
                " [" + WAREHOUSE_ID + "] BIGINT NOT NULL, " +
                " [" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, " +
                " [" + ACTIVE + "] INT NOT NULL DEFAULT 1, " +
                " [" + OWNERSHIP_STATUS + "] INT NOT NULL DEFAULT 1, " +
                " [" + STATUS + "] INT NOT NULL DEFAULT 1, " +
                " [" + MISSING_DATE + "] DATETIME, " +
                " [" + ITEM_CATEGORY_ID + "] BIGINT NOT NULL DEFAULT 0, " +
                " [" + TRANSFERED + "] INT, " +
                " [" + ORIGINAL_WAREHOUSE_ID + "] BIGINT NOT NULL, " +
                " [" + ORIGINAL_WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, " +
                " [" + LABEL_NUMBER + "] INT, " +
                " [" + MANUFACTURER + "] NVARCHAR ( 255 ), " +
                " [" + MODEL + "] NVARCHAR ( 255 ), " +
                " [" + SERIAL_NUMBER + "] NVARCHAR ( 255 ), " +
                " [" + CONDITION + "] INT, " +
                " [" + COST_CENTRE_ID + "] BIGINT, " +
                " [" + PARENT_ID + "] BIGINT, " +
                " [" + EAN + "] NVARCHAR ( 100 ), " +
                " [" + LAST_ASSET_REVIEW_DATE + "] DATETIME, " +
                " CONSTRAINT [PK_" + ASSET_ID + "] PRIMARY KEY ([" + ASSET_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$CODE]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ITEM_CATEGORY_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$SERIAL_NUMBER]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$EAN]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$CODE] ON [$TABLE_NAME] ([$CODE])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ITEM_CATEGORY_ID] ON [$TABLE_NAME] ([$ITEM_CATEGORY_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$SERIAL_NUMBER] ON [$TABLE_NAME] ([$SERIAL_NUMBER])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$EAN] ON [$TABLE_NAME] ([$EAN])"
        )

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + temp + TABLE_NAME + "]"
                + "( [" + temp + ASSET_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + temp + ASSET_ID + "] PRIMARY KEY ([" + temp + ASSET_ID + "]) )")

    }

    fun setMissing(assets: ArrayList<AssetReviewContent>): Boolean {
        try {
            val assetMissing: ArrayList<AssetReviewContent> = ArrayList()
            for (a in assets) {
                // Activos previamente extraviados y no existentes no cambian
                if (a.assetStatusId != AssetStatus.missing.id &&
                    a.assetStatusId != AssetStatus.unknown.id
                ) {
                    assetMissing.add(a)
                }
            }

            for (a in assetMissing) {
                // Actualizando activo
                updateMissing(a.assetId)
            }

            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventoryFromArCont(
        ar: AssetReview,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        try {
            val assetExists: ArrayList<AssetReviewContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo

                // Si el activo está eliminado no vuelve a estar en inventario,
                // sólo se actualiza su ubicación pero no cambia su estado.
                if (a.assetStatusId != AssetStatus.removed.id) {
                    updateOnInventory(
                        ar.warehouseId,
                        ar.warehouseAreaId,
                        a.assetId
                    )
                } else {
                    updateOnInventoryRemoved(
                        ar.warehouseId,
                        ar.warehouseAreaId,
                        a.assetId
                    )
                }
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setNewLocationFromArCont(
        wa: WarehouseArea,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        val w = wa.warehouse!!

        try {
            val assetExists: ArrayList<AssetReviewContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code
                updateLocation(
                    w.warehouseId,
                    wa.warehouseAreaId,
                    a.assetId
                )
            }

            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setNewLocationFromWmCont(
        wa: WarehouseArea,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        val w = wa.warehouse!!

        try {
            val assetExists: ArrayList<WarehouseMovementContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code
                updateLocation(
                    w.warehouseId,
                    wa.warehouseAreaId,
                    a.assetId
                )
            }

            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventoryFromWmCont(
        wa: WarehouseArea,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        val w = wa.warehouse!!

        try {
            val assetExists: ArrayList<WarehouseMovementContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code

                // Si el activo está eliminado no vuelve a estar en inventario,
                // sólo se actualiza su ubicación pero no cambia su estado.
                if (a.assetStatusId != AssetStatus.removed.id) {
                    updateOnInventory(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                } else {
                    updateOnInventoryRemoved(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                }
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventoryFromArea(
        warehouseAreaId: Long,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        val wa = WarehouseArea(warehouseAreaId, false)
        val w = wa.warehouse!!

        try {
            val assetExists: ArrayList<WarehouseMovementContent> = ArrayList()
            for (a in assets) {
                // Sólo los extraviados
                if (a.assetStatusId == AssetStatus.missing.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code
                // Los activos extraviados vuelven a inventario.
                updateOnInventory(
                    w.warehouseId,
                    wa.warehouseAreaId,
                    a.assetId
                )
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventoryFromArCont(
        wa: WarehouseArea,
        assets: ArrayList<AssetReviewContent>,
    ): Boolean {
        val w = wa.warehouse!!

        try {
            val assetExists: ArrayList<AssetReviewContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code

                // Si el activo está eliminado no vuelve a estar en inventario,
                // sólo se actualiza su ubicación pero no cambia su estado.
                if (a.assetStatusId != AssetStatus.removed.id) {
                    updateOnInventory(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                } else {
                    updateOnInventoryRemoved(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                }
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventoryFromWmCont(
        ar: WarehouseMovement,
        assets: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        val wa = ar.destWarehouseArea!!
        val w = ar.destWarehouse!!

        try {
            val assetExists: ArrayList<WarehouseMovementContent> = ArrayList()
            for (a in assets) {
                // Todos los activos, menos los no existentes
                if (a.assetStatusId != AssetStatus.unknown.id) {
                    assetExists.add(a)
                }
            }

            for (a in assetExists) {
                // Actualizando activo a.code

                // Si el activo está eliminado no vuelve a estar en inventario,
                // sólo se actualiza su ubicación pero no cambia su estado.
                if (a.assetStatusId != AssetStatus.removed.id) {
                    updateOnInventory(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                } else {
                    updateOnInventoryRemoved(
                        w.warehouseId,
                        wa.warehouseAreaId,
                        a.assetId
                    )
                }
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    fun setOnInventory(a: Asset): Boolean {
        try {
            // Si el activo está eliminado no vuelve a estar en inventario,
            // sólo se actualiza su ubicación pero no cambia su estado.
            if (a.assetStatusId != AssetStatus.removed.id) {
                updateOnInventory(
                    a.warehouseId,
                    a.warehouseAreaId,
                    a.assetId
                )
            } else {
                updateOnInventoryRemoved(
                    a.warehouseId,
                    a.warehouseAreaId,
                    a.assetId
                )
            }
            return true
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }
    }

    val minId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> minId")

            val sqLiteDatabase = StaticDbHelper.getReadableDb()
            sqLiteDatabase.beginTransaction()
            return try {
                val mCount = sqLiteDatabase.rawQuery("SELECT MIN($ASSET_ID) FROM $TABLE_NAME", null)
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

    fun selectTempId(): ArrayList<Asset> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where =
            " WHERE $TABLE_NAME.$ASSET_ID IN (SELECT $temp$TABLE_NAME.$temp$ASSET_ID FROM $temp$TABLE_NAME)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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
                    "INSERT INTO " + temp + TABLE_NAME + " (" + temp + ASSET_ID + ") VALUES "

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
}