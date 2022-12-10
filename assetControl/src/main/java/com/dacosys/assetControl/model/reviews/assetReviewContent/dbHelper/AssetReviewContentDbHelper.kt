package com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.commons.SaveProgress
import com.dacosys.assetControl.model.reviews.assetReview.`object`.AssetReview
import com.dacosys.assetControl.model.reviews.assetReviewContent.`object`.AssetReviewContent
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_CONTENT_ID
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_ID
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.CODE
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.CONTENT_STATUS_ID
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.ORIGIN_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.QTY
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.AssetReviewContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.reviews.assetReviewContent.dbHelper.AssetReviewContentContract.getAllColumns
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetReviewContentDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($ASSET_REVIEW_CONTENT_ID) FROM $TABLE_NAME",
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
        assetReviewId: Long,
        assetReviewContentId: Long,
        assetId: Long,
        code: String,
        description: String,
        qty: Float,
        contentStatusId: Int,
        originWarehouseAreaId: Long,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newAssetReviewContent = AssetReviewContent(
            assetReviewId,
            assetReviewContentId,
            assetId,
            code,
            description,
            qty,
            contentStatusId,
            originWarehouseAreaId
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newAssetReviewContent.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insert(
        ar: AssetReview,
        arCont: Array<AssetReviewContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
    ): Boolean {
        if (arCont.isEmpty()) {
            return false
        }

        onSaveProgress.invoke(SaveProgress(
            msg = getContext()
                .getString(R.string.adding_content_to_the_review),
            taskStatus = ProgressStatus.starting.id,
            progress = 0,
            total = 0
        ))

        Log.i(this::class.java.simpleName, ": SQLite -> insert")
        val splitList = splitList(arCont, 50)

        val sqLiteDatabase = getReadableDb()
        var lastId = try {
            val mCount =
                sqLiteDatabase.rawQuery(
                    "SELECT MAX($ASSET_REVIEW_CONTENT_ID) FROM $TABLE_NAME",
                    null
                )
            mCount.moveToFirst()
            val count = mCount.getLong(0)
            mCount.close()
            count
        } catch (ex: Exception) {
            0
        }

        val arId = ar.collectorAssetReviewId

        var error = false
        var p = 0
        val t = arCont.size

        sqLiteDatabase.beginTransaction()
        try {
            for (part in splitList) {
                /*
                        values.put(ASSET_REVIEW_ID, arId)
                            values.put(ASSET_REVIEW_CONTENT_ID, lastId)
                            values.put(ASSET_ID, x.assetId)
                            values.put(CODE, x.code)
                            values.put(DESCRIPTION, x.description)
                            values.put(QTY, x.qty)
                            values.put(CONTENT_STATUS_ID, x.contentStatusId)
                            values.put(ORIGIN_WAREHOUSE_AREA_ID, x.warehouseAreaId!!)
                         */

                var insertQ = ("INSERT INTO $TABLE_NAME" +
                        "($ASSET_REVIEW_ID," +
                        "$ASSET_REVIEW_CONTENT_ID," +
                        "$ASSET_ID," +
                        "$CODE," +
                        "$DESCRIPTION," +
                        "$QTY," +
                        "$CONTENT_STATUS_ID," +
                        "$ORIGIN_WAREHOUSE_AREA_ID) VALUES ")

                for (asset in part) {
                    p++
                    onSaveProgress.invoke(SaveProgress(
                        msg = String.format(
                            getContext().getString(R.string.adding_asset_),
                            asset.code
                        ),
                        taskStatus = ProgressStatus.running.id,
                        progress = p,
                        total = t
                    ))

                    Log.d(this::class.java.simpleName, "SQLITE-QUERY-INSERT-->" + asset.assetId)

                    lastId++

                    val values = "(" +
                            "$arId," +
                            "$lastId," +
                            "${asset.assetId}," +
                            "'${asset.code.replace("'", "''")}'," +
                            "'${asset.description.replace("'", "''")}'," +
                            "${asset.qty}," +
                            "${asset.contentStatusId}," +
                            "${asset.originWarehouseAreaId}),"
                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                sqLiteDatabase.execSQL(insertQ)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            error = true
        } finally {
            sqLiteDatabase.endTransaction()
        }

        if (error) {
            deleteByAssetReviewId(ar.collectorAssetReviewId)
        }

        if (error) {
            onSaveProgress.invoke(SaveProgress(
                msg = getContext()
                    .getString(R.string.error_inserting_assets_to_review),
                taskStatus = ProgressStatus.crashed.id,
                progress = 0,
                total = 0
            ))
        } else {
            onSaveProgress.invoke(SaveProgress(
                msg = getContext().getString(R.string.insert_ok),
                taskStatus = ProgressStatus.finished.id,
                progress = 0,
                total = 0
            ))
        }
        return !error
    }

    fun insert(assetReviewContent: AssetReviewContent): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                assetReviewContent.toContentValues()
            )
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            0
        }
    }

    fun updateAssetId(newAsset: Asset, oldAssetId: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> updateAssetId")

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(oldAssetId.toString())
        val values = ContentValues()
        values.put(ASSET_ID, newAsset.assetId)
        values.put(DESCRIPTION, newAsset.description)

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

    fun update(assetReviewContent: AssetReviewContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$ASSET_REVIEW_CONTENT_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(assetReviewContent.assetReviewContentId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                assetReviewContent.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(assetReviewContent: AssetReviewContent): Boolean {
        return deleteById(assetReviewContent.assetReviewContentId)
    }

    fun deleteByAssetReviewId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByAssetReviewId ($id)")

        val selection = "$ASSET_REVIEW_ID = ?" // WHERE code LIKE ?
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

    fun deleteById(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteById ($id)")

        val selection = "$ASSET_REVIEW_CONTENT_ID = ?" // WHERE code LIKE ?
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

    fun select(): ArrayList<AssetReviewContent> {
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

    fun selectById(id: Long): AssetReviewContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$ASSET_REVIEW_CONTENT_ID = ?" // WHERE code LIKE ?
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

    fun selectByAssetReviewCollectorId(id: Long): ArrayList<AssetReviewContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByAssetReviewCollectorId ($id)")

        val columns = getAllColumns()
        val selection = "$ASSET_REVIEW_ID = ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf(id.toString())
        val order = CODE

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

    fun selectByDescription(description: String): ArrayList<AssetReviewContent> {
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

    private fun fromCursor(c: Cursor?): ArrayList<AssetReviewContent> {
        val result = ArrayList<AssetReviewContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val assetReviewId = it.getLong(it.getColumnIndexOrThrow(ASSET_REVIEW_ID))
                    val assetReviewContentId =
                        it.getLong(it.getColumnIndexOrThrow(ASSET_REVIEW_CONTENT_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val qty = it.getFloat(it.getColumnIndexOrThrow(QTY))
                    val contentStatusId = it.getInt(it.getColumnIndexOrThrow(CONTENT_STATUS_ID))
                    val originWarehouseAreaId =
                        it.getLong(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_AREA_ID))

                    val temp = AssetReviewContent(
                        assetReviewId,
                        assetReviewContentId,
                        assetId,
                        code,
                        description,
                        qty,
                        contentStatusId,
                        originWarehouseAreaId
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    companion object {
        /*
        CREATE TABLE "asset_review_content" (
        `asset_review_id` bigint NOT NULL,
        `asset_review_content_id` bigint NOT NULL UNIQUE,
        `asset_id` bigint,
        `code` nvarchar ( 45 ) NOT NULL,
        `description` nvarchar ( 255 ) NOT NULL,
        `qty` numeric ( 12 , 4 ),
        `content_status_id` int ( 1 ) NOT NULL,
        `origin_warehouse_area_id` bigint NOT NULL,
        PRIMARY KEY(`asset_review_content_id`) )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + ASSET_REVIEW_ID + "] BIGINT NOT NULL, "
                + " [" + ASSET_REVIEW_CONTENT_ID + "] BIGINT NOT NULL UNIQUE, "
                + " [" + ASSET_ID + "] BIGINT, "
                + " [" + CODE + "] NVARCHAR ( 45 ) NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + QTY + "] DECIMAL ( 12,4 ), "
                + " [" + CONTENT_STATUS_ID + "] INT ( 1 ) NOT NULL, "
                + " [" + ORIGIN_WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " PRIMARY KEY( [" + ASSET_REVIEW_CONTENT_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_REVIEW_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$CODE]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_REVIEW_ID] ON [$TABLE_NAME] ([$ASSET_REVIEW_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_ID] ON [$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$CODE] ON [$TABLE_NAME] ([$CODE])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$DESCRIPTION] ON [$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID] ON [$TABLE_NAME] ([$ORIGIN_WAREHOUSE_AREA_ID])"
        )
    }
}