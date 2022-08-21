package com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.dataBase.StaticDbHelper
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetContract
import com.dacosys.assetControl.model.locations.warehouse.dbHelper.WarehouseContract
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea
import com.dacosys.assetControl.model.locations.warehouseArea.dbHelper.WarehouseAreaContract
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.`object`.DataCollection
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.ASSET_CODE
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.ASSET_STR
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.COLLECTOR_DATA_COLLECTION_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.COLLECTOR_ROUTE_PROCESS_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.COMPLETED
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.DATE_END
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.DATE_START
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.USER_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.model.routes.dataCollections.dataCollection.dbHelper.DataCollectionContract.DataCollectionEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.model.routes.dataCollections.dataCollectionContent.dbHelper.DataCollectionContentDbHelper
import com.dacosys.assetControl.model.routes.routeProcess.dbHelper.RouteProcessContract
import com.dacosys.assetControl.model.routes.routeProcessContent.`object`.RouteProcessContent

/**
 * Created by Agustin on 28/12/2016.
 */

class DataCollectionDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = StaticDbHelper.getReadableDb()
            sqLiteDatabase.beginTransaction()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($COLLECTOR_DATA_COLLECTION_ID) FROM $TABLE_NAME",
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

    fun insert(
        rpc: RouteProcessContent,
        dataStart: String,
        fakeId: Long,
    ): DataCollection? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO data_collection (
            asset_id,
            warehouse_id,
            warehouse_area_id,
            user_id,
            collector_data_collection_id,
            date_start,
            date_end,
            completed,
            collector_route_process_id)
        VALUES (
            @asset_id,
            @warehouse_id,
            @warehouse_area_id,
            @user_id,
            @collector_data_collection_id,
            @date_start,
            DATETIME('now', 'localtime'),
            1,
            @collector_route_process_id)
         */

        val userId = Statics.currentUserId ?: return null
        var assetId = 0L
        if (rpc.assetId != null) {
            assetId = (rpc.assetId ?: return null)
        }

        var warehouseId = 0L
        if (rpc.warehouseId != null) {
            warehouseId = (rpc.warehouseId ?: return null)
        }

        var warehouseAreaId = 0L
        if (rpc.warehouseAreaId != null) {
            warehouseAreaId = (rpc.warehouseAreaId ?: return null)
        }

        /*
        var itemCategoryId = 0L
        if (rpc.itemCategoryId != null) {
            itemCategoryId = rpc.itemCategoryId!!
        }
        */

        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                ASSET_ID + ", " +
                WAREHOUSE_ID + ", " +
                WAREHOUSE_AREA_ID + ", " +
                USER_ID + ", " +
                COLLECTOR_DATA_COLLECTION_ID + ", " +
                DATE_START + ", " +
                DATE_END + ", " +
                COMPLETED + ", " +
                COLLECTOR_ROUTE_PROCESS_ID + ")" +
                " VALUES (" +
                assetId + ", " +
                warehouseId + ", " +
                warehouseAreaId + ", " +
                userId + ", " +
                newId + ", " +
                "'" + dataStart + "', " +
                "DATETIME('now', 'localtime'), " +
                "1, " +
                fakeId + ")"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(insertQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }
        return selectByCollectorId(newId)
    }

    fun insert(
        asset: Asset,
        dataStart: String,
        fakeId: Long,
    ): DataCollection? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO data_collection (
            asset_id,
            warehouse_id,
            warehouse_area_id,
            user_id,
            collector_data_collection_id,
            date_start,
            date_end,
            completed,
            collector_route_process_id)
        VALUES (
            @asset_id,
            @warehouse_id,
            @warehouse_area_id,
            @user_id,
            @collector_data_collection_id,
            @date_start,
            DATETIME('now', 'localtime'),
            1,
            @collector_route_process_id)
         */

        val userId = Statics.currentUserId ?: return null
        val assetId = asset.assetId
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                ASSET_ID + ", " +
                WAREHOUSE_ID + ", " +
                WAREHOUSE_AREA_ID + ", " +
                USER_ID + ", " +
                COLLECTOR_DATA_COLLECTION_ID + ", " +
                DATE_START + ", " +
                DATE_END + ", " +
                COMPLETED + ", " +
                COLLECTOR_ROUTE_PROCESS_ID + ")" +
                " VALUES (" +
                assetId + ", " +
                "NULL, " +
                "NULL, " +
                userId + ", " +
                newId + ", " +
                "'" + dataStart + "', " +
                "DATETIME('now', 'localtime'), " +
                "1, " +
                fakeId + ")"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(insertQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }
        return selectByCollectorId(newId)
    }

    fun insert(
        warehouseArea: WarehouseArea,
        dataStart: String,
        fakeId: Long,
    ): DataCollection? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO data_collection (
            asset_id,
            warehouse_id,
            warehouse_area_id,
            user_id,
            collector_data_collection_id,
            date_start,
            date_end,
            completed,
            collector_route_process_id)
        VALUES (
            @asset_id,
            @warehouse_id,
            @warehouse_area_id,
            @user_id,
            @collector_data_collection_id,
            @date_start,
            DATETIME('now', 'localtime'),
            1,
            @collector_route_process_id)
         */

        val userId = Statics.currentUserId ?: return null
        val warehouseAreaId = warehouseArea.warehouseAreaId
        val warehouseId = warehouseArea.warehouseId
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                ASSET_ID + ", " +
                WAREHOUSE_ID + ", " +
                WAREHOUSE_AREA_ID + ", " +
                USER_ID + ", " +
                COLLECTOR_DATA_COLLECTION_ID + ", " +
                DATE_START + ", " +
                DATE_END + ", " +
                COMPLETED + ", " +
                COLLECTOR_ROUTE_PROCESS_ID + ")" +
                " VALUES (" +
                "NULL, " +
                warehouseId + ", " +
                warehouseAreaId + ", " +
                userId + ", " +
                newId + ", " +
                "'" + dataStart + "', " +
                "DATETIME('now', 'localtime'), " +
                "1, " +
                fakeId + ")"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(insertQ)
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        } finally {
            sqLiteDatabase.endTransaction()
        }
        return selectByCollectorId(newId)
    }

    fun update(dataCollection: DataCollection): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$COLLECTOR_DATA_COLLECTION_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(dataCollection.collectorDataCollectionId.toString())

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val r = sqLiteDatabase.update(
                TABLE_NAME,
                dataCollection.toContentValues(),
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

    fun updateTransferred(dataCollectionId: Long, collectorDataCollectionId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransferred")

        /*
        UPDATE data_collection
        SET
            modification_date = DATETIME('now', 'localtime'),
            data_collection_id = @data_collection_id,
            status_id = 3
        WHERE (collector_data_collection_id = @collector_data_collection_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERED_DATE + " = DATETIME('now', 'localtime'), " +
                    DATA_COLLECTION_ID + " = " + dataCollectionId +
                    " WHERE (" + COLLECTOR_DATA_COLLECTION_ID + " = " + collectorDataCollectionId + ")"

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

    fun delete(dataCollection: DataCollection): Boolean {
        return deleteById(dataCollection.collectorDataCollectionId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$COLLECTOR_DATA_COLLECTION_ID = ?" // WHERE code LIKE ?
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

    fun deleteOrphansTransferred(): Boolean = try {
        deleteOrphans()
        DataCollectionContentDbHelper().deleteOrphans()
        true
    } catch (ex: SQLException) {
        ex.printStackTrace()
        ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        false
    }

    private fun deleteOrphans() {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteOrphans")

        /*
        DELETE FROM data_collection
            WHERE (collector_route_process_id NOT IN
            (SELECT
                collector_route_process_id
            FROM route_process))
        */

        val deleteQ: String = "DELETE FROM [" + TABLE_NAME + "] WHERE ( " +
                COLLECTOR_ROUTE_PROCESS_ID + " NOT IN (SELECT " +
                COLLECTOR_ROUTE_PROCESS_ID + " FROM " +
                RouteProcessContract.RouteProcessEntry.TABLE_NAME + "))"

        val sqLiteDatabase = StaticDbHelper.getWritableDb()
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

    fun select(): ArrayList<DataCollection> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery =
            basicSelect +
                    "," +
                    basicStrFields +
                    " FROM " + TABLE_NAME +
                    basicLeftJoin +
                    " ORDER BY " + TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_ID

        val sqLiteDatabase = StaticDbHelper.getReadableDb()
        sqLiteDatabase.beginTransaction()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            sqLiteDatabase.endTransaction()
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectByNoTransferred(): ArrayList<DataCollection> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByNoTransferred")

        val rpEntry = RouteProcessContract.RouteProcessEntry
        val where = " WHERE " +
                "(" + TABLE_NAME + "." + TRANSFERED_DATE + " IS NULL) AND " +
                "(" + TABLE_NAME + "." + COMPLETED + " = 1) AND " +
                "(((" + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID + " IS NOT NULL) AND" +
                "(" + rpEntry.TABLE_NAME + "." + rpEntry.COMPLETED + " = 1) AND " +
                "(" + rpEntry.TABLE_NAME + "." + rpEntry.TRANSFERED_DATE + " IS NULL)) OR " +
                "(" + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID + " IS NULL))"
        val specialLeftJoin = " LEFT OUTER JOIN " + rpEntry.TABLE_NAME + " ON " +
                rpEntry.TABLE_NAME + "." + rpEntry.COLLECTOR_ROUTE_PROCESS_ID + " = " +
                TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID + " OR (" +
                TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID + " IS NULL)"
        val rawQuery =
            basicSelect +
                    "," +
                    basicStrFields +
                    " FROM " + TABLE_NAME +
                    basicLeftJoin +
                    specialLeftJoin +
                    where +
                    " ORDER BY " + TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_ID

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

    fun selectByCollectorId(collectorDataCollectionId: Long): DataCollection? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($collectorDataCollectionId)")

        val where =
            " WHERE ${TABLE_NAME}.${COLLECTOR_DATA_COLLECTION_ID} = $collectorDataCollectionId"
        val rawQuery =
            basicSelect +
                    "," +
                    basicStrFields +
                    " FROM " + TABLE_NAME +
                    basicLeftJoin +
                    where +
                    " ORDER BY " + TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_ID

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

    private fun fromCursor(c: Cursor?): ArrayList<DataCollection> {
        val result = ArrayList<DataCollection>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val dataCollectionId = it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val dateStart = it.getString(it.getColumnIndexOrThrow(DATE_START))
                    val dateEnd = it.getString(it.getColumnIndexOrThrow(DATE_END))
                    val completed = it.getInt(it.getColumnIndexOrThrow(COMPLETED)) == 1
                    val transferredDate = it.getString(it.getColumnIndexOrThrow(TRANSFERED_DATE))
                    val collectorDataCollectionId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_DATA_COLLECTION_ID))
                    val collectorRouteProcessId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_ROUTE_PROCESS_ID))

                    val temp = DataCollection(
                        dataCollectionId = dataCollectionId,
                        assetId = assetId,
                        warehouseId = warehouseId,
                        warehouseAreaId = warehouseAreaId,
                        userId = userId,
                        dateStart = dateStart,
                        dateEnd = dateEnd,
                        completed = completed,
                        transferedDate = transferredDate,
                        collectorDataCollectionId = collectorDataCollectionId,
                        collectorRouteProcessId = collectorRouteProcessId
                    )

                    temp.assetStr = it.getString(it.getColumnIndexOrThrow(ASSET_STR)) ?: ""
                    temp.assetCode = it.getString(it.getColumnIndexOrThrow(ASSET_CODE)) ?: ""
                    temp.warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""
                    temp.warehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR))
                            ?: ""

                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "data_collection" (
        `data_collection_id` bigint,
        `asset_id` bigint,
        `warehouse_id` bigint,
        `warehouse_area_id` bigint,
        `user_id` bigint NOT NULL,
        `date_start` datetime,
        `date_end` datetime,
        `completed` int,
        `transfered_date` datetime,
        `_id` bigint NOT NULL,
        `collector_route_process_id` bigint NOT NULL,
        CONSTRAINT `PK__data_collection__0000000000000877` PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] "
                + "( [" + DATA_COLLECTION_ID + "] BIGINT, "
                + " [" + ASSET_ID + "] BIGINT, "
                + " [" + WAREHOUSE_ID + "] BIGINT, "
                + " [" + WAREHOUSE_AREA_ID + "] BIGINT, "
                + " [" + USER_ID + "] BIGINT NOT NULL, "
                + " [" + DATE_START + "] DATETIME, "
                + " [" + DATE_END + "] DATETIME, "
                + " [" + COMPLETED + "] INT, "
                + " [" + TRANSFERED_DATE + "] DATETIME, "
                + " [" + COLLECTOR_DATA_COLLECTION_ID + "] BIGINT NOT NULL, "
                + " [" + COLLECTOR_ROUTE_PROCESS_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + COLLECTOR_DATA_COLLECTION_ID + "] PRIMARY KEY ([" + COLLECTOR_DATA_COLLECTION_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$COLLECTOR_DATA_COLLECTION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$COLLECTOR_ROUTE_PROCESS_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_ID] ON [$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_ID] ON [$TABLE_NAME] ([$WAREHOUSE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$COLLECTOR_DATA_COLLECTION_ID] ON [$TABLE_NAME] ([$COLLECTOR_DATA_COLLECTION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$COLLECTOR_ROUTE_PROCESS_ID] ON [$TABLE_NAME] ([$COLLECTOR_ROUTE_PROCESS_ID])"
        )

        private const val basicSelect = "SELECT " +
                TABLE_NAME + "." + DATA_COLLECTION_ID + "," +
                TABLE_NAME + "." + ASSET_ID + "," +
                TABLE_NAME + "." + WAREHOUSE_ID + "," +
                TABLE_NAME + "." + WAREHOUSE_AREA_ID + "," +
                TABLE_NAME + "." + USER_ID + "," +
                TABLE_NAME + "." + DATE_START + "," +
                TABLE_NAME + "." + DATE_END + "," +
                TABLE_NAME + "." + COMPLETED + "," +
                TABLE_NAME + "." + TRANSFERED_DATE + "," +
                TABLE_NAME + "." + COLLECTOR_DATA_COLLECTION_ID + "," +
                TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

        private const val basicLeftJoin = " LEFT JOIN " +
                WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + TABLE_NAME + "." + WAREHOUSE_AREA_ID +

                " LEFT JOIN " + AssetContract.AssetEntry.TABLE_NAME + " ON " + AssetContract.AssetEntry.TABLE_NAME + "." +
                AssetContract.AssetEntry.ASSET_ID + " = " + TABLE_NAME + "." + ASSET_ID +

                " LEFT JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " + WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + TABLE_NAME + "." + WAREHOUSE_ID

        private const val basicStrFields =
            WarehouseContract.WarehouseEntry.TABLE_NAME + "." + WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + WAREHOUSE_STR + "," +
                    WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." + WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + WAREHOUSE_AREA_STR + "," +
                    AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.DESCRIPTION + " AS " + ASSET_STR + "," +
                    AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.CODE + " AS " + ASSET_CODE
    }
}