package com.dacosys.assetControl.dataBase.route

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.COLLECTOR_ROUTE_PROCESS_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.COMPLETED
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_PROCESS_DATE
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_PROCESS_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.ROUTE_STR
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.TRANSFERED
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.TRANSFERED_DATE
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.USER_ID
import com.dacosys.assetControl.dataBase.route.RouteProcessContract.RouteProcessEntry.Companion.USER_STR
import com.dacosys.assetControl.dataBase.user.UserContract
import com.dacosys.assetControl.model.route.Route
import com.dacosys.assetControl.model.route.RouteProcess
import com.dacosys.assetControl.utils.Statics
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.UTCDataTime
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Agustin on 28/12/2016.
 */

class RouteProcessDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($COLLECTOR_ROUTE_PROCESS_ID) FROM $TABLE_NAME",
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

    fun insert(route: Route): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newId = lastId
        val newRouteProcess = RouteProcess(
            userId = Statics.currentUserId!!,
            routeId = route.routeId,
            routeProcessDate = UTCDataTime.getUTCDateTimeAsString(),
            completed = false,
            transferred = false,
            transferredDate = null,
            routeProcessId = null,
            collectorRouteProcessId = lastId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return if (sqLiteDatabase.insertOrThrow(
                    TABLE_NAME,
                    null,
                    newRouteProcess.toContentValues()
                ) > 0
            ) {
                newId
            } else {
                0
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun insert(
        userId: Long,
        routeId: Long,
        routeProcessDate: String,
        completed: Boolean,
        transferred: Boolean,
        transferredDate: String?,
        routeProcessId: Long?,
        collectorRouteProcessId: Long,
    ): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newId = lastId
        val newRouteProcess = RouteProcess(
            userId = userId,
            routeId = routeId,
            routeProcessDate = routeProcessDate,
            completed = completed,
            transferred = transferred,
            transferredDate = transferredDate,
            routeProcessId = routeProcessId,
            collectorRouteProcessId = collectorRouteProcessId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return if (sqLiteDatabase.insertOrThrow(
                    TABLE_NAME,
                    null,
                    newRouteProcess.toContentValues()
                ) > 0
            ) {
                newId
            } else {
                0
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun insert(routeProcess: RouteProcess): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                routeProcess.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0L
        }
    }

    fun update(routeProcess: RouteProcess): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$COLLECTOR_ROUTE_PROCESS_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(routeProcess.collectorRouteProcessId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                routeProcess.toContentValues(),
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

    fun updateTransfered(routeProcessId: Long, collectorRouteProcessId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateTransfered")

        /*
        UPDATE route_process
        SET
            modification_date = DATETIME('now', 'localtime'),
            route_process_id = @route_process_id,
            status_id = 3
        WHERE (collector_route_process_id = @collector_route_process_id)
         */

        val updateQ =
            "UPDATE " + TABLE_NAME +
                    " SET " +
                    TRANSFERED_DATE + " = DATETIME('now', 'localtime'), " +
                    ROUTE_PROCESS_ID + " = " + routeProcessId +
                    " WHERE (" + COLLECTOR_ROUTE_PROCESS_ID + " = " + collectorRouteProcessId + ")"

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

    fun delete(routeProcess: RouteProcess): Boolean {
        return deleteById(routeProcess.collectorRouteProcessId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$COLLECTOR_ROUTE_PROCESS_ID = ?" // WHERE code LIKE ?
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
        val rp: ArrayList<RouteProcess> = selectTransferred()
        if (rp.isEmpty()) {
            return true
        }

        // Necesito los ID's de las rutas que tienen recolecciones
        val routeIdList: ArrayList<Long> = ArrayList()
        for (r in ArrayList(rp.sortedWith(compareBy { it.routeProcessDate }).reversed())) {
            if (routeIdList.contains(r.routeId)) {
                continue
            }
            routeIdList.add(r.routeId)
        }

        // Esta una forma de obtener la hecha actual y restarle 7 días
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, -7)

        val sdf = SimpleDateFormat("dd'/'MM'/'yyyy HH:mm:ss a", Locale.getDefault())
        sdf.calendar = c
        var minDate = sdf.format(c.time).toString()

        val rpContDbHelper = RouteProcessContentDbHelper()
        val rpsDbHelper = RouteProcessStepsDbHelper()

        val res = try {
            for (rId in routeIdList) {
                var a = 0
                for (r in ArrayList(rp.sortedWith(compareBy { it.routeProcessDate }).reversed())) {
                    if (r.routeId == rId) {
                        a++
                        if (a <= 4) {
                            continue
                        }

                        minDate = r.routeProcessDate
                        break
                    }
                }

                if (a > 4) {
                    rpContDbHelper.deleteByRouteIdRouteProcessDate(minDate, rId)
                    rpsDbHelper.deleteByRouteIdRouteProcessDate(minDate, rId)
                    deleteByRouteIdRouteProcessDate(minDate, rId)
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

    private fun deleteByRouteIdRouteProcessDate(minDate: String, routeId: Long) {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> deleteByRouteIdRouteProcessDate (R:$routeId/D:$minDate)"
        )

        /*
        DELETE FROM route_process
        WHERE (route_process_id IN
                (SELECT
                    collector_route_process_id
                FROM route_process route_process_1
                WHERE
                    (route_process_date < @route_process_date) AND
                    (transfered_date IS NOT NULL) AND
                    (route_id = @route_id)))
        */

        val deleteQ: String = "DELETE FROM [" + TABLE_NAME + "] WHERE ( " +
                ROUTE_PROCESS_ID + " IN (SELECT " +
                COLLECTOR_ROUTE_PROCESS_ID + " FROM " +
                TABLE_NAME + " temp_" + TABLE_NAME +
                " WHERE (" + ROUTE_PROCESS_DATE + " < '" + minDate + "') AND " +
                "(" + TRANSFERED_DATE + " IS NOT NULL) AND " +
                "(" + ROUTE_ID + " = " + routeId + ")))"

        val sqLiteDatabase = getWritableDb()
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

    fun select(): ArrayList<RouteProcess> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

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

    fun selectById(collectorRouteProcessId: Long): RouteProcess? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($collectorRouteProcessId)")

        val where = " WHERE ($TABLE_NAME.$COLLECTOR_ROUTE_PROCESS_ID = $collectorRouteProcessId)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

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

    fun selectByNoTransferred(): ArrayList<RouteProcess> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByNoTransferred")

        /*
        SELECT
            route_process.user_id,
            route_process.route_id,
            route_process.route_process_date,
            route_process.completed,
            route_process.transfered,
            route_process.transfered_date,
            route_process.route_process_id,
            route_process.collector_route_process_id,
            [user].name AS user_str,
            route.description AS route_str
        FROM route_process
        LEFT OUTER JOIN [user] ON [user].user_id = route_process.user_id
        LEFT OUTER JOIN route ON route.route_id = route_process.route_id
        WHERE
            (route_process.transfered_date IS NULL) AND
            (route_process.completed = 1)
         */

        val where = " WHERE ($TABLE_NAME.$TRANSFERED_DATE IS NULL) AND ($TABLE_NAME.$COMPLETED = 1)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

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

    private fun selectTransferred(): ArrayList<RouteProcess> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectTransferred")

        /*
        SELECT
            route_process.user_id,
            route_process.route_id,
            route_process.route_process_date,
            route_process.completed,
            route_process.transfered,
            route_process.transfered_date,
            route_process.route_process_id,
            route_process.collector_route_process_id,
            [user].name AS user_str,
            route.description AS route_str
        FROM route_process
        LEFT OUTER JOIN [user] ON [user].user_id = route_process.user_id
        LEFT OUTER JOIN route ON route.route_id = route_process.route_id
        WHERE (route_process.transfered_date IS NOT NULL)
         */

        val where = " WHERE ($TABLE_NAME.$TRANSFERED_DATE IS NOT NULL)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

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

    fun selectByRouteIdNoCompleted(routeId: Long): ArrayList<RouteProcess> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByRouteIdNoCompleted ($routeId)")

        /*
        SELECT
            route_process.user_id,
            route_process.route_id,
            route_process.route_process_date,
            route_process.completed,
            route_process.transfered,
            route_process.transfered_date,
            route_process.route_process_id,
            route_process.collector_route_process_id,
            [user].name AS user_str,
            route.description AS route_str
        FROM route_process
        LEFT OUTER JOIN [user] ON [user].user_id = route_process.user_id
        LEFT OUTER JOIN route ON route.route_id = route_process.route_id
        WHERE
            (route_process.route_id = @route_id) AND
            (route_process.completed = 0)
         */

        val where = " WHERE ($TABLE_NAME.$ROUTE_ID = $routeId) AND ($TABLE_NAME.$COMPLETED = 0)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

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

    fun selectByNoCompleted(): ArrayList<RouteProcess> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByNoCompleted")

        /*
        SELECT
            route_process.user_id,
            route_process.route_id,
            route_process.route_process_date,
            route_process.completed,
            route_process.transfered,
            route_process.transfered_date,
            route_process.route_process_id,
            route_process.collector_route_process_id,
            [user].name AS user_str,
            route.description AS route_str
        FROM route_process
        LEFT OUTER JOIN [user] ON [user].user_id = route_process.user_id
        LEFT OUTER JOIN route ON route.route_id = route_process.route_id
        WHERE
            (route_process.route_id = @route_id) AND
            (route_process.completed = 0)
         */

        val where = " WHERE ($TABLE_NAME.$COMPLETED = 0)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<RouteProcess> {
        val result = ArrayList<RouteProcess>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val userId = it.getLong(it.getColumnIndexOrThrow(USER_ID))
                    val routeId = it.getLong(it.getColumnIndexOrThrow(ROUTE_ID))
                    val routeProcessDate =
                        it.getString(it.getColumnIndexOrThrow(ROUTE_PROCESS_DATE))
                    val completed = it.getInt(it.getColumnIndexOrThrow(COMPLETED)) == 1
                    val transfered = it.getInt(it.getColumnIndexOrThrow(TRANSFERED)) == 1
                    val transferedDate = it.getString(it.getColumnIndexOrThrow(TRANSFERED_DATE))
                    val routeProcessId = it.getLong(it.getColumnIndexOrThrow(ROUTE_PROCESS_ID))
                    val collectorRouteProcessId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_ROUTE_PROCESS_ID))

                    val temp = RouteProcess(
                        userId = userId,
                        routeId = routeId,
                        routeProcessDate = routeProcessDate,
                        completed = completed,
                        transferred = transfered,
                        transferredDate = transferedDate,
                        routeProcessId = routeProcessId,
                        collectorRouteProcessId = collectorRouteProcessId
                    )

                    temp.userStr = it.getString(it.getColumnIndexOrThrow(USER_STR)) ?: ""
                    temp.routeStr = it.getString(it.getColumnIndexOrThrow(ROUTE_STR)) ?: ""

                    result.add(temp)
                }
            }
        }
        return result
    }

    /**
    SELECT
    route_process.user_id,
    route_process.route_id,
    route_process.route_process_date,
    route_process.completed,
    route_process.transfered,
    route_process.transfered_date,
    route_process.route_process_id,
    route_process.collector_route_process_id
     */
    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + USER_ID + "," +
            TABLE_NAME + "." + ROUTE_ID + "," +
            TABLE_NAME + "." + ROUTE_PROCESS_DATE + "," +
            TABLE_NAME + "." + COMPLETED + "," +
            TABLE_NAME + "." + TRANSFERED + "," +
            TABLE_NAME + "." + TRANSFERED_DATE + "," +
            TABLE_NAME + "." + ROUTE_PROCESS_ID + "," +
            TABLE_NAME + "." + COLLECTOR_ROUTE_PROCESS_ID

    /**
     * LEFT OUTER JOIN user ON user.user_id = route_process.user_id
     * LEFT OUTER JOIN route ON route.route_id = route_process.route_id
     */
    private val basicLeftJoin = " LEFT JOIN [" + UserContract.UserEntry.TABLE_NAME + "] ON [" +
            UserContract.UserEntry.TABLE_NAME + "]." +
            UserContract.UserEntry.USER_ID + " = " + TABLE_NAME + "." + USER_ID +

            " LEFT JOIN " + RouteContract.RouteEntry.TABLE_NAME + " ON " +
            RouteContract.RouteEntry.TABLE_NAME + "." +
            RouteContract.RouteEntry.ROUTE_ID + " = " + TABLE_NAME + "." + ROUTE_ID

    /**
     * user.name AS user_str,
     * route.description AS route_str
     */
    private val basicStrFields = "[" + UserContract.UserEntry.TABLE_NAME + "]." +
            UserContract.UserEntry.NAME + " AS " + USER_STR + "," +

            RouteContract.RouteEntry.TABLE_NAME + "." +
            RouteContract.RouteEntry.DESCRIPTION + " AS " + ROUTE_STR

    companion object {
        /*
        CREATE TABLE "route_process" (
            `user_id` bigint NOT NULL,
            `route_id` bigint NOT NULL,
            `route_process_date` datetime NOT NULL,
            `completed` int NOT NULL,
            `transfered` int,
            `transfered_date` datetime,
            `route_process_id` bigint,
            `_id` bigint NOT NULL,
            CONSTRAINT `PK__route_process__00000000000007FD` PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "] "
                + "( [" + USER_ID + "] BIGINT NOT NULL, "
                + " [" + ROUTE_ID + "] BIGINT NOT NULL, "
                + " [" + ROUTE_PROCESS_DATE + "] DATETIME NOT NULL, "
                + " [" + COMPLETED + "] INT NOT NULL, "
                + " [" + TRANSFERED + "] INT, "
                + " [" + TRANSFERED_DATE + "] DATETIME, "
                + " [" + ROUTE_PROCESS_ID + "] BIGINT, "
                + " [" + COLLECTOR_ROUTE_PROCESS_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + COLLECTOR_ROUTE_PROCESS_ID + "] PRIMARY KEY ([" + COLLECTOR_ROUTE_PROCESS_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$USER_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$USER_ID] ON [$TABLE_NAME] ([$USER_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_ID] ON [$TABLE_NAME] ([$ROUTE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ROUTE_PROCESS_ID] ON [$TABLE_NAME] ([$ROUTE_PROCESS_ID])"
        )
    }
}