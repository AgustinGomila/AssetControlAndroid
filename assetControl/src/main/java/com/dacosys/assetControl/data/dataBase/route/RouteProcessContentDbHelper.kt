package com.dacosys.assetControl.data.dataBase.route

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.data.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.data.dataBase.asset.AssetContract
import com.dacosys.assetControl.data.dataBase.location.WarehouseAreaContract
import com.dacosys.assetControl.data.dataBase.location.WarehouseContract
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ASSET_CODE
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ASSET_STR
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.DATA_COLLECTION_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.DATA_COLLECTION_RULE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.LEVEL
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.POSITION
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_CONTENT_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_STATUS_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_PROCESS_STATUS_STR
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.ROUTE_STR
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.WAREHOUSE_ID
import com.dacosys.assetControl.data.dataBase.route.RouteProcessContentContract.RouteProcessContentEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.data.model.dataCollection.DataCollection
import com.dacosys.assetControl.data.model.route.RouteProcessContent
import com.dacosys.assetControl.data.model.route.RouteProcessStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog


/**
 * Created by Agustin on 28/12/2016.
 */

class RouteProcessContentDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($ROUTE_PROCESS_CONTENT_ID) FROM $TABLE_NAME",
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
        routeProcessId: Long,
        dataCollectionRuleId: Long,
        level: Int,
        position: Int,
        routeProcessStatusId: Int,
        dataCollectionId: Long?,
        addSteps: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO route_process_content (
            route_process_id,
            data_collection_rule_id,
            level,
            position,
            route_process_status_id,
            data_collection_id,
            route_process_content_id)
        VALUES (
            @route_process_id,
            @data_collection_rule_id,
            @level,
            @position,
            @route_process_status_id,
            @data_collection_id,
            @route_process_content_id)
        */

        var res = true
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                ROUTE_PROCESS_ID + ", " +
                DATA_COLLECTION_RULE_ID + ", " +
                LEVEL + ", " +
                POSITION + ", " +
                ROUTE_PROCESS_STATUS_ID + ", " +
                DATA_COLLECTION_ID + ", " +
                ROUTE_PROCESS_CONTENT_ID + ")" +
                " VALUES (" +
                routeProcessId + ", " +
                dataCollectionRuleId + ", " +
                level + ", " +
                position + ", " +
                routeProcessStatusId + ", " +
                (dataCollectionId ?: "NULL") + ", " +
                newId + ")"

        val sqLiteDatabase = getWritableDb()
        try {
            sqLiteDatabase.execSQL(insertQ)
            res = getChangesCount() > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
        }

        if (res) {
            if (addSteps) {
                // Agregar el paso a la colección de pasos
                RouteProcessStepsDbHelper().insert(
                    routeProcessId,
                    newId,
                    level,
                    position,
                    dataCollectionId
                )
            }
        }
        return res
    }

    private fun getChangesCount(): Long {
        val db = getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateStatusNew(rpc: RouteProcessContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateStatus")

        val selection =
            "$ROUTE_PROCESS_ID = ? AND $DATA_COLLECTION_RULE_ID = ? AND $LEVEL = ? AND $POSITION = ? AND $ROUTE_PROCESS_CONTENT_ID = ?"
        val selectionArgs = arrayOf(
            rpc.routeProcessId.toString(),
            rpc.dataCollectionRuleId.toString(),
            rpc.level.toString(),
            rpc.position.toString(),
            rpc.routeProcessContentId.toString()
        )

        val values = ContentValues()
        values.put(ROUTE_PROCESS_STATUS_ID, rpc.routeProcessStatusId)
        if (rpc.dataCollectionId == null) values.putNull(DATA_COLLECTION_ID)
        else values.put(DATA_COLLECTION_ID, rpc.dataCollectionId)

        val sqLiteDatabase = getWritableDb()
        return try {
            val res = sqLiteDatabase.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs
            ) > 0
            if (!res) false
            else updateRouteProcessSteps(rpc)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun updateStatus(rpc: RouteProcessContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateStatus")

        /*
        UPDATE route_process_content
        SET
            route_process_status_id = @route_process_status_id,
            data_collection_id = @data_collection_id
        WHERE
            (route_process_id = @route_process_id) AND
            (data_collection_rule_id = @data_collection_rule_id) AND
            (level = @level) AND
            (position = @position) AND
            (route_process_content_id = @route_process_content_id)
         */
        var res: Boolean
        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    ROUTE_PROCESS_STATUS_ID + " = " + rpc.routeProcessStatusId + ", " +
                    DATA_COLLECTION_ID + " = " + (rpc.dataCollectionId ?: "NULL") +
                    " WHERE (" + ROUTE_PROCESS_ID + " = " + rpc.routeProcessId + ") AND " +
                    "(" + DATA_COLLECTION_RULE_ID + " = " + rpc.dataCollectionRuleId + ") AND " +
                    "(" + LEVEL + " = " + rpc.level + ") AND " +
                    "(" + POSITION + " = " + rpc.position + ") AND " +
                    "(" + ROUTE_PROCESS_CONTENT_ID + " = " + rpc.routeProcessContentId + ")"

        val sqLiteDatabase = getWritableDb()
        res = try {
            val c = sqLiteDatabase.rawQuery(updateQ, null)
            c.moveToFirst()
            c.close()
            getChangesCount() > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }

        return if (!res) false
        else updateRouteProcessSteps(rpc)
    }

    private fun updateRouteProcessSteps(rpc: RouteProcessContent): Boolean {
        var res: Boolean
        val rpStepDbH = RouteProcessStepsDbHelper()
        val x = rpStepDbH.selectByCollectorRouteProcessContentId(rpc.routeProcessContentId)

        try {
            if (x.size > 0) {
                val rpStep = x[0]
                rpStepDbH.updateNew(rpc.dataCollectionId, rpStep)
            } else {
                rpStepDbH.insert(
                    rpc.routeProcessId,
                    rpc.routeProcessContentId,
                    rpc.level,
                    rpc.position,
                    rpc.dataCollectionId
                )
            }
            res = true
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            res = false
        }
        return res
    }

    fun delete(routeProcessContent: RouteProcessContent): Boolean {
        return deleteById(routeProcessContent.routeProcessContentId)
    }

    fun deleteByRouteProcessId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByRouteProcessId ($id)")

        val selection = "$ROUTE_PROCESS_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun deleteByRouteIdRouteProcessDate(minDate: String, routeId: Long) {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> deleteByRouteIdRouteProcessDate (R:$routeId/D:$minDate)"
        )

        /*
        DELETE FROM route_process_content
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

        val sqLiteDatabase = getWritableDb()
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

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ROUTE_PROCESS_CONTENT_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                TABLE_NAME,
                selection,
                selectionArgs
            ) > 0
        } catch (ex: Exception) {
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

    fun select(): ArrayList<RouteProcessContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        /*
        SELECT
            route_process.route_id,
            route.description AS route_str,
            route_process_content.route_process_id,
            route_process.collector_route_process_id,
            route_process_content.data_collection_rule_id,
            route_process_content.level,
            route_process_content.position,
            route_composition.asset_id,
            asset.description AS asset_str,
            asset.code AS asset_code,
            route_composition.warehouse_area_id,
            warehouse_area.description AS warehouse_area_str,
            route_composition.warehouse_id,
            warehouse.description AS warehouse_str,
            route_process_content.route_process_status_id,
            route_process_status.description AS route_process_status_str,
            route_process_content.data_collection_id,
            route_process_content.route_process_content_id
        FROM route_process_content
        LEFT OUTER JOIN route_process ON route_process.collector_route_process_id = route_process_content.route_process_id
        LEFT OUTER JOIN route_composition ON route_composition.route_id = route_process.route_id AND
                        route_process_content.data_collection_rule_id = route_composition.data_collection_rule_id AND
                        route_process_content.level = route_composition.level AND
                        route_process_content.position = route_composition.position
        LEFT OUTER JOIN route ON route.route_id = route_process.route_id
        LEFT OUTER JOIN asset ON asset.asset_id = route_composition.asset_id
        LEFT OUTER JOIN warehouse_area ON warehouse_area.warehouse_area_id = route_composition.warehouse_area_id
        LEFT OUTER JOIN warehouse ON warehouse.warehouse_id = route_composition.warehouse_id
        LEFT OUTER JOIN route_process_status ON route_process_status.route_process_status_id = route_process_content.route_process_status_id
         */

        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByRouteProcessComplete(
        routeId: Long,
        routeProcessId: Long,
        dataCollection: DataCollection?,
    ): ArrayList<RouteProcessContent> {
        val rpContArray = selectByCollectorRouteProcessId(routeProcessId)
        val rCompArray = RouteCompositionDbHelper().selectByRouteId(routeId)

        var anyInsert = false

        if (rCompArray.size > 0) {
            for (rComp in rCompArray) {
                var isIn = false

                if (rpContArray.size > 0) {
                    for (rpCont in rpContArray) {
                        if (
                            rComp.dataCollectionRuleId == rpCont.dataCollectionRuleId &&
                            rComp.routeId == rpCont.routeId &&
                            rComp.level == rpCont.level &&
                            rComp.position == rpCont.position
                        ) {
                            isIn = true
                            continue
                        }
                    }
                }

                if (!isIn) {
                    anyInsert = true

                    var dataCollectionId: Long? = null
                    if (dataCollection != null) {
                        dataCollectionId = dataCollection.dataCollectionId
                    }

                    insert(
                        routeProcessId,
                        rComp.dataCollectionRuleId,
                        rComp.level,
                        rComp.position,
                        RouteProcessStatus.notProcessed.id,
                        dataCollectionId,
                        false
                    )
                }
            }
        }

        return if (anyInsert) {
            selectByRouteProcessId(routeProcessId)
        } else rpContArray
    }

    fun selectById(id: Long): RouteProcessContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        if (id <= 0) {
            return null
        }

        val where = " WHERE $TABLE_NAME.$ROUTE_PROCESS_CONTENT_ID = $id"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            val result = fromCursor(c)
            when {
                result.size > 0 -> result[0]
                else -> null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            null
        }
    }

    fun selectByRouteProcessId(rpId: Long): ArrayList<RouteProcessContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByRouteProcessId ($rpId)")

        if (rpId <= 0) {
            return ArrayList()
        }

        val where = " WHERE $TABLE_NAME.$ROUTE_PROCESS_ID = $rpId"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    fun selectByCollectorRouteProcessId(id: Long): ArrayList<RouteProcessContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCollectorRouteProcessId ($id)")

        val where = " WHERE $TABLE_NAME.$ROUTE_PROCESS_ID = $id"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<RouteProcessContent> {
        val res = ArrayList<RouteProcessContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val routeProcessId = it.getLong(it.getColumnIndexOrThrow(ROUTE_PROCESS_ID))
                    val dataCollectionRuleId =
                        it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_RULE_ID))
                    val level = it.getInt(it.getColumnIndexOrThrow(LEVEL))
                    val position = it.getInt(it.getColumnIndexOrThrow(POSITION))
                    val routeProcessStatusId =
                        it.getInt(it.getColumnIndexOrThrow(ROUTE_PROCESS_STATUS_ID))
                    val dataCollectionId = it.getLong(it.getColumnIndexOrThrow(DATA_COLLECTION_ID))
                    val routeProcessContentId =
                        it.getLong(it.getColumnIndexOrThrow(ROUTE_PROCESS_CONTENT_ID))

                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val assetStr = it.getString(it.getColumnIndexOrThrow(ASSET_STR))
                    val assetCode = it.getString(it.getColumnIndexOrThrow(ASSET_CODE))
                    val warehouseId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_ID))
                    val warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val warehouseAreaStr =
                        it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR))
                    val routeId = it.getLong(it.getColumnIndexOrThrow(ROUTE_ID))
                    val routeStr = it.getString(it.getColumnIndexOrThrow(ROUTE_STR))

                    val temp = RouteProcessContent(
                        routeProcessId,
                        dataCollectionRuleId,
                        level,
                        position,
                        routeProcessStatusId,
                        dataCollectionId,
                        routeProcessContentId
                    )

                    temp.assetId = assetId
                    temp.assetStr = assetStr
                    temp.assetCode = assetCode
                    temp.warehouseId = warehouseId
                    temp.warehouseStr = warehouseStr
                    temp.warehouseAreaId = warehouseAreaId
                    temp.warehouseAreaStr = warehouseAreaStr
                    temp.routeId = routeId
                    temp.routeStr = routeStr

                    res.add(temp)
                }
            }
        }
        return res
    }

    companion object {
        /*
            CREATE TABLE [route_process_content] (
            [route_process_id] bigint NULL ,
            [data_collection_rule_id] bigint NULL ,
            [level] int NULL ,
            [position] int NULL ,
            [route_process_status_id] bigint NULL ,
            [data_collection_id] bigint NULL ,
            [route_process_content_id] bigint NULL )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ROUTE_PROCESS_ID + "] BIGINT NULL, "
                + " [" + DATA_COLLECTION_RULE_ID + "] BIGINT NULL, "
                + " [" + LEVEL + "] INT NULL, "
                + " [" + POSITION + "] INT NULL, "
                + " [" + ROUTE_PROCESS_STATUS_ID + "] BIGINT NULL, "
                + " [" + DATA_COLLECTION_ID + "] BIGINT NULL, "
                + " [" + ROUTE_PROCESS_CONTENT_ID + "] BIGINT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$LEVEL]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$POSITION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_STATUS_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_CONTENT_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_RULE_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_RULE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$LEVEL] ON [$TABLE_NAME] ([$LEVEL])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$POSITION] ON [$TABLE_NAME] ([$POSITION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_STATUS_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_STATUS_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DATA_COLLECTION_ID] ON [$TABLE_NAME] ([$DATA_COLLECTION_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_CONTENT_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_CONTENT_ID])"
        )
    }

    /**
    route_process.route_id,
    route.description AS route_str,
    route_process_content.route_process_id,
    route_process.collector_route_process_id,
    route_process_content.data_collection_rule_id,
    route_process_content.level,
    route_process_content.position,
    route_composition.asset_id,
    asset.description AS asset_str,
    asset.code AS asset_code,
    route_composition.warehouse_area_id,
    warehouse_area.description AS warehouse_area_str,
    route_composition.warehouse_id,
    warehouse.description AS warehouse_str,
    route_process_content.route_process_status_id,
    route_process_status.description AS route_process_status_str,
    route_process_content.data_collection_id,
    route_process_content.route_process_content_id
     */
    private val basicSelect = "SELECT " +
            RouteProcessContract.RouteProcessEntry.TABLE_NAME + "." + RouteProcessContract.RouteProcessEntry.ROUTE_ID + ", " +
            RouteContract.RouteEntry.TABLE_NAME + "." + RouteContract.RouteEntry.DESCRIPTION + " AS " + ROUTE_STR + ", " +
            TABLE_NAME + "." + ROUTE_PROCESS_ID + "," +
            RouteProcessContract.RouteProcessEntry.TABLE_NAME + "." + RouteProcessContract.RouteProcessEntry.COLLECTOR_ROUTE_PROCESS_ID + ", " +
            TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + "," +
            TABLE_NAME + "." + LEVEL + "," +
            TABLE_NAME + "." + POSITION + "," +
            RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.ASSET_ID + ", " +
            AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.DESCRIPTION + " AS " + ASSET_STR + ", " +
            AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.CODE + " AS " + ASSET_CODE + ", " +
            RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.WAREHOUSE_AREA_ID + ", " +
            WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." + WarehouseAreaContract.WarehouseAreaEntry.DESCRIPTION + " AS " + WAREHOUSE_AREA_STR + ", " +
            RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.WAREHOUSE_ID + ", " +
            WarehouseContract.WarehouseEntry.TABLE_NAME + "." + WarehouseContract.WarehouseEntry.DESCRIPTION + " AS " + WAREHOUSE_STR + ", " +
            TABLE_NAME + "." + ROUTE_PROCESS_STATUS_ID + ", " +
            RouteProcessStatusContract.RouteProcessStatusEntry.TABLE_NAME + "." + RouteProcessStatusContract.RouteProcessStatusEntry.DESCRIPTION + " AS " + ROUTE_PROCESS_STATUS_STR + ", " +
            TABLE_NAME + "." + DATA_COLLECTION_ID + ", " +
            TABLE_NAME + "." + ROUTE_PROCESS_CONTENT_ID

    /**
    LEFT OUTER JOIN route_process ON route_process.collector_route_process_id = route_process_content.route_process_id
    LEFT OUTER JOIN route_composition ON route_composition.route_id = route_process.route_id AND
    route_process_content.data_collection_rule_id = route_composition.data_collection_rule_id AND
    route_process_content.level = route_composition.level AND
    route_process_content.position = route_composition.position
    LEFT OUTER JOIN route ON route.route_id = route_process.route_id
    LEFT OUTER JOIN asset ON asset.asset_id = route_composition.asset_id
    LEFT OUTER JOIN warehouse_area ON warehouse_area.warehouse_area_id = route_composition.warehouse_area_id
    LEFT OUTER JOIN warehouse ON warehouse.warehouse_id = route_composition.warehouse_id
    LEFT OUTER JOIN route_process_status ON route_process_status.route_process_status_id = route_process_content.route_process_status_id
     */
    private val basicLeftJoin =
        " LEFT OUTER JOIN " + RouteProcessContract.RouteProcessEntry.TABLE_NAME + " ON " +
                RouteProcessContract.RouteProcessEntry.TABLE_NAME + "." +
                RouteProcessContract.RouteProcessEntry.COLLECTOR_ROUTE_PROCESS_ID + " = " + TABLE_NAME + "." + ROUTE_PROCESS_ID +

                " LEFT OUTER JOIN " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + " ON " +
                RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." +
                RouteCompositionContract.RouteCompositionEntry.ROUTE_ID + " = " + RouteProcessContract.RouteProcessEntry.TABLE_NAME + "." + RouteProcessContract.RouteProcessEntry.ROUTE_ID + " AND " +

                TABLE_NAME + "." + DATA_COLLECTION_RULE_ID + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.DATA_COLLECTION_RULE_ID + " AND " +
                TABLE_NAME + "." + LEVEL + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.LEVEL + " AND " +
                TABLE_NAME + "." + POSITION + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.POSITION +

                " LEFT OUTER JOIN " + RouteContract.RouteEntry.TABLE_NAME + " ON " +
                RouteContract.RouteEntry.TABLE_NAME + "." +
                RouteContract.RouteEntry.ROUTE_ID + " = " + RouteProcessContract.RouteProcessEntry.TABLE_NAME + "." + RouteProcessContract.RouteProcessEntry.ROUTE_ID +

                " LEFT OUTER JOIN " + AssetContract.AssetEntry.TABLE_NAME + " ON " +
                AssetContract.AssetEntry.TABLE_NAME + "." +
                AssetContract.AssetEntry.ASSET_ID + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.ASSET_ID +

                " LEFT OUTER JOIN " + WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + " ON " +
                WarehouseAreaContract.WarehouseAreaEntry.TABLE_NAME + "." +
                WarehouseAreaContract.WarehouseAreaEntry.WAREHOUSE_AREA_ID + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.WAREHOUSE_AREA_ID +

                " LEFT OUTER JOIN " + WarehouseContract.WarehouseEntry.TABLE_NAME + " ON " +
                WarehouseContract.WarehouseEntry.TABLE_NAME + "." +
                WarehouseContract.WarehouseEntry.WAREHOUSE_ID + " = " + RouteCompositionContract.RouteCompositionEntry.TABLE_NAME + "." + RouteCompositionContract.RouteCompositionEntry.WAREHOUSE_ID +

                " LEFT OUTER JOIN " + RouteProcessStatusContract.RouteProcessStatusEntry.TABLE_NAME + " ON " +
                RouteProcessStatusContract.RouteProcessStatusEntry.TABLE_NAME + "." +
                RouteProcessStatusContract.RouteProcessStatusEntry.ROUTE_PROCESS_STATUS_ID + " = " + TABLE_NAME + "." + ROUTE_PROCESS_STATUS_ID
}