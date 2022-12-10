package com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.reviews.assetReviewStatus.`object`.AssetReviewStatus
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.STATUS_ID
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.AssetReviewStatusEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.reviews.assetReviewStatus.dbHelper.AssetReviewStatusContract.getAllColumns
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetReviewStatusDbHelper {
    fun insert(assetReviewStatusId: Int): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newAssetReviewStatus = AssetReviewStatus.getById(assetReviewStatusId) ?: return false

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newAssetReviewStatus.toContentValues()
            ) > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun update(assetReviewStatus: AssetReviewStatus): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$STATUS_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(assetReviewStatus.id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                assetReviewStatus.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: Exception) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    /**
     * Agrega los estados a la base de datos local para poder hacer
     * left joins y traer las descripciones. Primero intenta actualizar
     * los estados, si no hay filas afectadas, intenta hacer una inserción
     */
    fun sync(): Boolean {
        var query = "DELETE FROM $TABLE_NAME"
        Log.d(this::class.java.simpleName, query)

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            sqLiteDatabase.execSQL(query)

            query = "INSERT INTO " + TABLE_NAME + " (" +
                    STATUS_ID + "," +
                    DESCRIPTION + ")" +
                    " VALUES "

            val objArray = AssetReviewStatus.getAll()
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.id)
                )
                val values = "(${obj.id},'${obj.description}'),"
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

    fun select(): ArrayList<AssetReviewStatus> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = DESCRIPTION

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

    fun selectById(id: Long): AssetReviewStatus? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$STATUS_ID = ?" // WHERE code LIKE ?
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

    fun selectByDescription(description: String): ArrayList<AssetReviewStatus> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val columns = getAllColumns()
        val selection = "$DESCRIPTION LIKE ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$description%")
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
            return fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<AssetReviewStatus> {
        val result = ArrayList<AssetReviewStatus>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(STATUS_ID))
                    val temp = AssetReviewStatus.getById(id)
                    if (temp != null)
                        result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE
        "status" ( `status_id` int ( 11 ) NOT NULL UNIQUE,
        `description` varchar ( 255 ) NOT NULL )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + STATUS_ID + "] INT ( 11 ) NOT NULL UNIQUE ,"
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$STATUS_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$STATUS_ID] ON [$TABLE_NAME] ([$STATUS_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}