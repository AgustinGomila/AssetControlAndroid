package com.dacosys.assetControl.dataBase.route

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.route.RouteContract.RouteEntry.Companion.ACTIVE
import com.dacosys.assetControl.dataBase.route.RouteContract.RouteEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.route.RouteContract.RouteEntry.Companion.ROUTE_ID
import com.dacosys.assetControl.dataBase.route.RouteContract.RouteEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.route.RouteContract.getAllColumns
import com.dacosys.assetControl.model.route.Route
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList
import com.dacosys.assetControl.webservice.route.RouteObject

/**
 * Created by Agustin on 28/12/2016.
 */

class RouteDbHelper {
    fun sync(
        objArray: Array<RouteObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.route_id)
            )

            val values = "($ROUTE_ID = ${obj.route_id}) OR "
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
                    ROUTE_ID + "," +
                    DESCRIPTION + "," +
                    ACTIVE + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.route_id)
                )
                count++
                onSyncProgress.invoke(
                    SyncProgress(
                        totalTask = countTotal,
                        completedTask = currentCount + count,
                        msg = getContext()
                            .getString(R.string.synchronizing_routes),
                        registryType = SyncRegistryType.Route,
                        progressStatus = ProgressStatus.running
                    )
                )

                val values = "(" +
                        obj.route_id + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.active + "),"

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
        routeId: Long,
        description: String,
        active: Boolean,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newRoute = Route(
            routeId,
            description,
            active
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newRoute.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(route: Route): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                route.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun update(route: Route): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ROUTE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(route.routeId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                route.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun select(onlyActive: Boolean): ArrayList<Route> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        var selection = ""
        if (onlyActive) {
            selection = "$ACTIVE = 1"
        }
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                selection,// Columnas para la cláusula WHERE
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

    fun selectById(id: Long): Route? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$ROUTE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
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

    fun selectByDescription(
        searchText: String,
        onlyActive: Boolean,
    ): ArrayList<Route> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByDescription (T:$searchText)"
        )

        var where = ""
        if (searchText.isNotEmpty()) {
            where = " WHERE (" +
                    TABLE_NAME + "." + DESCRIPTION + " LIKE '%" + searchText + "%')"
        }
        if (onlyActive) {
            where = if (where.isNotEmpty()) "$where AND (" else " WHERE ("
            where += "$TABLE_NAME.$ACTIVE = 1)"
        }
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME + "" +
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

    private fun fromCursor(c: Cursor?): ArrayList<Route> {
        val result = ArrayList<Route>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(ROUTE_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))

                    val temp = Route(
                        id,
                        description,
                        active
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    // Funciones que guardan y recuperan IDs entre actividades
    // y evitar el error: !!! FAILED BINDER TRANSACTION !!!
    // cuando se pasa un objeto demasiado grande
    private fun createTempTable() {
        val allCommands: ArrayList<String> = ArrayList()
        allCommands.add(CREATE_TEMP_TABLE)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            for (sql in allCommands) {
                println("$sql;")
                sqLiteDatabase.execSQL(sql)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun selectTempId(): ArrayList<Route> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val where =
            " WHERE $TABLE_NAME.$ROUTE_ID IN (SELECT $temp$TABLE_NAME.$temp$ROUTE_ID FROM $temp$TABLE_NAME)"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME + "" +
                where +
                " ORDER BY " + TABLE_NAME + "." + ROUTE_ID

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
                    "INSERT INTO " + temp + TABLE_NAME + " (" + temp + ROUTE_ID + ") VALUES "

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

    companion object {
        /*
        CREATE TABLE "route" (
        `_id` bigint NOT NULL,
        `description` nvarchar ( 100 ) NOT NULL,
        `active` int NOT NULL,
         CONSTRAINT `PK_route` PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ROUTE_ID + "] BIGINT NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INT NOT NULL, "
                + " CONSTRAINT [PK_" + ROUTE_ID + "] PRIMARY KEY ([" + ROUTE_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + temp + TABLE_NAME + "]"
                + "( [" + temp + ROUTE_ID + "] BIGINT NOT NULL, "
                + " CONSTRAINT [PK_" + temp + ROUTE_ID + "] PRIMARY KEY ([" + temp + ROUTE_ID + "]) )")


        private const val basicSelect = "SELECT " +
                TABLE_NAME + "." + ROUTE_ID + "," +
                TABLE_NAME + "." + DESCRIPTION + "," +
                TABLE_NAME + "." + ACTIVE
    }
}