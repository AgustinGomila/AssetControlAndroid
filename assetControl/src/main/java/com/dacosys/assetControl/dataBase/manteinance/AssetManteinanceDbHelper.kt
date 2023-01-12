package com.dacosys.assetControl.dataBase.manteinance

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.asset.AssetContract
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.ASSET_ID
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.ASSET_MANTEINANCE_ID
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.COLLECTOR_ASSET_MANTEINANCE_ID
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.MANTEINANCE_STATUS_ID
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.MANTEINANCE_TYPE_ID
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.OBSERVATIONS
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.AssetManteinanceEntry.Companion.TRANSFERRED
import com.dacosys.assetControl.dataBase.manteinance.AssetManteinanceContract.getAllColumns
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.manteinance.AssetManteinance
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetManteinanceDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($COLLECTOR_ASSET_MANTEINANCE_ID) FROM $TABLE_NAME",
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
        asset: Asset,
        obs: String,
        manteinanceStatusId: Int,
        manteinanceTypeId: Long,
    ): AssetManteinance? {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        /*
        INSERT INTO asset_manteinance_collector
            (asset_id,
            observations,
            transfered,
            manteinance_status_id,
            asset_manteinance_id,
            manteinance_type_id,
            collector_asset_manteinance_id)
        VALUES (
            @p1,
            @p2,
            @p3,
            @p4,
            @p5,
            @p6,
            @p7)
         */

        val assetId = asset.assetId
        val newId = lastId
        val insertQ: String = "INSERT INTO [" + TABLE_NAME + "] ( " +
                ASSET_ID + ", " +
                OBSERVATIONS + ", " +
                TRANSFERRED + ", " +
                MANTEINANCE_STATUS_ID + ", " +
                ASSET_MANTEINANCE_ID + ", " +
                MANTEINANCE_TYPE_ID + ", " +
                COLLECTOR_ASSET_MANTEINANCE_ID + ")" +
                " VALUES (" +
                assetId + ", " +
                "'" + obs + "', " +
                "0, " +
                manteinanceStatusId + ", " +
                "0, " +
                manteinanceTypeId + ", " +
                newId + ")"

        val sqLiteDatabase = getWritableDb()
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

    private fun getChangesCount(): Long {
        val db = getReadableDb()
        val statement = db.compileStatement("SELECT changes()")
        return statement.simpleQueryForLong()
    }

    fun updateTransferred(assetManteinanceId: Long): Boolean {
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
                    " WHERE (" + ASSET_MANTEINANCE_ID + " = " + assetManteinanceId + ")"

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

    fun selectNoTransfered(): ArrayList<AssetManteinance> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectNoTransfered")

        val columns = getAllColumns()
        val selection = "$TABLE_NAME.$TRANSFERRED = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(0.toString())
        val order = ASSET_ID

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
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insert(assetManteinance: AssetManteinance): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                assetManteinance.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun update(assetManteinance: AssetManteinance): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$COLLECTOR_ASSET_MANTEINANCE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(assetManteinance.collectorAssetManteinanceId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                assetManteinance.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(assetManteinance: AssetManteinance): Boolean {
        return deleteById(assetManteinance.collectorAssetManteinanceId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$COLLECTOR_ASSET_MANTEINANCE_ID = ?" // WHERE code LIKE ?
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

    fun deleteByAssetId(assetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByAssetId ($assetId)")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(assetId.toString())

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

    fun select(): ArrayList<AssetManteinance> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = ASSET_ID

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                TABLE_NAME, // Nombre de la tabla
                columns,// Lista de Columnas a consultar
                null,// Columnas para la cláusula WHERE
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

    fun select(onlyActive: Boolean): ArrayList<AssetManteinance> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        var selection = ""
        if (onlyActive) {
            selection =
                "${AssetContract.AssetEntry.TABLE_NAME}.${AssetContract.AssetEntry.ACTIVE} = 1"
        }
        val order = ASSET_ID

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

    fun selectById(id: Long): AssetManteinance? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$COLLECTOR_ASSET_MANTEINANCE_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = ASSET_ID

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

    fun selectByAssetIdNotTransferred(assetId: Long): AssetManteinance? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByAssetIdNotTransferred ($assetId)")

        /*
        SELECT
            asset_id,
            asset_manteinance_id,
            collector_asset_manteinance_id,
            manteinance_status_id,
            manteinance_type_id,
            observations,
            transfered
        FROM asset_manteinance_collector
        WHERE
            (asset_id = @asset_id) AND
            (transfered = 0)
        */

        val where = " WHERE (" +
                TABLE_NAME + "." + ASSET_ID + " = " + assetId + ") AND (" +
                TABLE_NAME + "." + TRANSFERRED + " = 0)"
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    fun selectByDescriptionCodeEan(
        searchText: String,
        onlyActive: Boolean,
    ): ArrayList<AssetManteinance> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescriptionCodeEan ($searchText)")

        var where = ""
        if (searchText.isNotEmpty()) {
            where = " WHERE " +
                    AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.DESCRIPTION + " LIKE '%" + searchText + "%' OR " +
                    AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.CODE + " LIKE '%" + searchText + "%' OR " +
                    AssetContract.AssetEntry.TABLE_NAME + "." + AssetContract.AssetEntry.EAN + " LIKE '%" + searchText + "%'"
        }
        if (onlyActive) {
            where = if (where.isNotEmpty()) "$where AND " else " WHERE "
            where += "${AssetContract.AssetEntry.TABLE_NAME}.${AssetContract.AssetEntry.ACTIVE} = 1"
        }
        val rawQuery = basicSelect +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + ASSET_ID

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

    private fun fromCursor(c: Cursor?): ArrayList<AssetManteinance> {
        val result = ArrayList<AssetManteinance>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val assetManteinanceId =
                        it.getLong(it.getColumnIndexOrThrow(ASSET_MANTEINANCE_ID))
                    val manteinanceTypeId =
                        it.getLong(it.getColumnIndexOrThrow(MANTEINANCE_TYPE_ID))
                    val manteinanceStatusId =
                        it.getInt(it.getColumnIndexOrThrow(MANTEINANCE_STATUS_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val observtions = it.getString(it.getColumnIndexOrThrow(OBSERVATIONS))
                    val transferred = it.getInt(it.getColumnIndexOrThrow(TRANSFERRED)) == 1
                    val collectorAssetManteinanceId =
                        it.getLong(it.getColumnIndexOrThrow(COLLECTOR_ASSET_MANTEINANCE_ID))

                    val temp = AssetManteinance(
                        assetManteinanceId,
                        manteinanceTypeId,
                        manteinanceStatusId,
                        assetId,
                        observtions,
                        transferred,
                        collectorAssetManteinanceId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ASSET_ID + "] BIGINT NOT NULL, "
                + " [" + OBSERVATIONS + "] NVARCHAR ( 255 ) ,"
                + " [" + TRANSFERRED + "] INT ,"
                + " [" + MANTEINANCE_STATUS_ID + "] BIGINT NOT NULL, "
                + " [" + ASSET_MANTEINANCE_ID + "] BIGINT NOT NULL, "
                + " [" + MANTEINANCE_TYPE_ID + "] BIGINT NOT NULL, "
                + " [" + COLLECTOR_ASSET_MANTEINANCE_ID + "] BIGINT NOT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$MANTEINANCE_STATUS_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_MANTEINANCE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$MANTEINANCE_TYPE_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$COLLECTOR_ASSET_MANTEINANCE_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$MANTEINANCE_STATUS_ID] ON [$TABLE_NAME] ([$MANTEINANCE_STATUS_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_MANTEINANCE_ID] ON [$TABLE_NAME] ([$ASSET_MANTEINANCE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$MANTEINANCE_TYPE_ID] ON [$TABLE_NAME] ([$MANTEINANCE_TYPE_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$COLLECTOR_ASSET_MANTEINANCE_ID] ON [$TABLE_NAME] ([$COLLECTOR_ASSET_MANTEINANCE_ID])"
        )
    }

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + COLLECTOR_ASSET_MANTEINANCE_ID + "," +
            TABLE_NAME + "." + ASSET_MANTEINANCE_ID + "," +
            TABLE_NAME + "." + MANTEINANCE_TYPE_ID + "," +
            TABLE_NAME + "." + MANTEINANCE_STATUS_ID + "," +
            TABLE_NAME + "." + ASSET_ID + "," +
            TABLE_NAME + "." + OBSERVATIONS + "," +
            TABLE_NAME + "." + TRANSFERRED

    private val basicLeftJoin = " LEFT JOIN " + AssetContract.AssetEntry.TABLE_NAME + " ON " +
            AssetContract.AssetEntry.TABLE_NAME + "." +
            AssetContract.AssetEntry.ASSET_ID + " = " + TABLE_NAME + "." + ASSET_ID
}