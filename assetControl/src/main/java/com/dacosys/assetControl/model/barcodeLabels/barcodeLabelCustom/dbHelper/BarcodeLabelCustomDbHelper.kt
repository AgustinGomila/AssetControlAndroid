package com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper

import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.`object`.BarcodeLabelCustom
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.ACTIVE
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.BARCODE_LABEL_CUSTOM_ID
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.BARCODE_LABEL_TARGET_ID
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.BarcodeLabelCustomEntry.Companion.TEMPLATE
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.dbHelper.BarcodeLabelCustomContract.getAllColumns
import com.dacosys.assetControl.model.barcodeLabels.barcodeLabelCustom.wsObject.BarcodeLabelCustomObject
import com.dacosys.assetControl.network.sync.SyncProgress
import com.dacosys.assetControl.network.sync.SyncRegistryType
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog

/**
 * Created by Agustin on 28/12/2016.
 */

class BarcodeLabelCustomDbHelper {
    fun sync(
        objArray: Array<BarcodeLabelCustomObject>,
        onSyncProgress: (SyncProgress) -> Unit = {},
        currentCount: Int,
        countTotal: Int,
    ): Boolean {
        var query = ("DELETE FROM [$TABLE_NAME] WHERE ")
        for (obj in objArray) {
            Log.i(
                this::class.java.simpleName,
                String.format(": SQLite -> delete: id:%s", obj.barcode_label_custom_id)
            )

            val values = "($BARCODE_LABEL_CUSTOM_ID = ${obj.barcode_label_custom_id}) OR "
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
                    BARCODE_LABEL_CUSTOM_ID + "," +
                    DESCRIPTION + "," +
                    BARCODE_LABEL_TARGET_ID + "," +
                    ACTIVE + "," +
                    TEMPLATE + ")" +
                    " VALUES "

            var count = 0
            for (obj in objArray) {
                Log.i(
                    this::class.java.simpleName,
                    String.format(": SQLite -> insert: id:%s", obj.barcode_label_custom_id)
                )
                count++
                onSyncProgress.invoke(SyncProgress(
                    totalTask = countTotal,
                    completedTask = currentCount + count,
                    msg = getContext()
                        .getString(R.string.synchronizing_barcode_labels),
                    registryType = SyncRegistryType.BarcodeLabelCustom,
                    progressStatus = ProgressStatus.running
                ))

                val values = "(" +
                        obj.barcode_label_custom_id + "," +
                        "'" + obj.description.replace("'", "''") + "'," +
                        obj.barcode_label_target_id + "," +
                        obj.active + "," +
                        "'" + obj.template.replace("'", "''") + "'),"

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
        barcodeLabelCustomId: Long,
        description: String,
        active: Boolean,
        barcodeLabelTargetId: Long,
        template: String,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newBarcodeLabelCustom = BarcodeLabelCustom(
            barcodeLabelCustomId,
            description,
            active,
            barcodeLabelTargetId,
            template
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newBarcodeLabelCustom.toContentValues()
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

    fun insert(barcodeLabelCustom: BarcodeLabelCustom): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME,
                null,
                barcodeLabelCustom.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun update(barcodeLabelCustom: BarcodeLabelCustom): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$BARCODE_LABEL_CUSTOM_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(barcodeLabelCustom.barcodeLabelCustomId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                barcodeLabelCustom.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(barcodeLabelCustom: BarcodeLabelCustom): Boolean {
        return deleteById(barcodeLabelCustom.barcodeLabelCustomId)
    }

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$BARCODE_LABEL_CUSTOM_ID = ?" // WHERE code LIKE ?
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

    fun select(onlyActive: Boolean): ArrayList<BarcodeLabelCustom> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = DESCRIPTION
        var selection = ""
        if (onlyActive) {
            selection = "${TABLE_NAME}.${ACTIVE} = 1"
        }

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

    fun selectById(id: Long): BarcodeLabelCustom? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$BARCODE_LABEL_CUSTOM_ID = ?" // WHERE code LIKE ?
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

    fun selectByDescription(description: String): ArrayList<BarcodeLabelCustom> {
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

    fun selectByBarcodeLabelTargetIdDescription(
        barcodeLabelTargetId: Long,
        description: String,
    ): ArrayList<BarcodeLabelCustom> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByBarcodeLabelTargetIdDescription (B:$barcodeLabelTargetId/D:$description)"
        )

        val columns = getAllColumns()
        val selection = "$DESCRIPTION LIKE ? AND $BARCODE_LABEL_TARGET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$description%", barcodeLabelTargetId.toString())
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

    fun selectByBarcodeLabelTargetId(
        barcodeLabelTargetId: Long,
        onlyActive: Boolean,
    ): ArrayList<BarcodeLabelCustom> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByBarcodeLabelTargetId ($barcodeLabelTargetId)"
        )

        val columns = getAllColumns()
        var selection = "$BARCODE_LABEL_TARGET_ID = ?" // WHERE code LIKE ?
        if (onlyActive) {
            selection += " AND ${TABLE_NAME}.${ACTIVE} = 1"
        }
        val selectionArgs = arrayOf(barcodeLabelTargetId.toString())
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

    private fun fromCursor(c: Cursor?): ArrayList<BarcodeLabelCustom> {
        val result = ArrayList<BarcodeLabelCustom>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(BARCODE_LABEL_CUSTOM_ID))
                    val active = it.getInt(it.getColumnIndexOrThrow(ACTIVE)) == 1
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val barcodeLabelTargetId =
                        it.getLong(it.getColumnIndexOrThrow(BARCODE_LABEL_TARGET_ID))
                    val template = it.getString(it.getColumnIndexOrThrow(TEMPLATE))

                    val temp = BarcodeLabelCustom(
                        id,
                        description,
                        active,
                        barcodeLabelTargetId,
                        template
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "barcode_label_custom" (
            `_id` INTEGER ( 11 ) NOT NULL,
            `description` varchar ( 255 ) NOT NULL,
            `barcode_label_target_id` INTEGER ( 11 ) NOT NULL,
            `active` INTEGER ( 1 ) NOT NULL,
            `template` TEXT NOT NULL,
        PRIMARY KEY(`_id`) )
        */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + BARCODE_LABEL_CUSTOM_ID + "] INTEGER ( 11 ) NOT NULL, "
                + " [" + DESCRIPTION + "] VARCHAR ( 255 ) NOT NULL, "
                + " [" + ACTIVE + "] INTEGER ( 1 ) NOT NULL, "
                + " [" + BARCODE_LABEL_TARGET_ID + "] INTEGER ( 11 ) NOT NULL, "
                + " [" + TEMPLATE + "] TEXT NOT NULL, "
                + " CONSTRAINT [PK_" + BARCODE_LABEL_CUSTOM_ID + "] PRIMARY KEY ([" + BARCODE_LABEL_CUSTOM_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$BARCODE_LABEL_TARGET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$BARCODE_LABEL_TARGET_ID] ON [$TABLE_NAME] ([$BARCODE_LABEL_TARGET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])"
        )
    }
}