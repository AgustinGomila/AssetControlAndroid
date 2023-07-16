package com.dacosys.assetControl.dataBase.review

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_CONTENT_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ASSET_REVIEW_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.CODE
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.CONTENT_STATUS_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.EAN
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ITEM_CATEGORY_STR
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.LABEL_NUMBER
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.MANUFACTURER
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.MODEL
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.ORIGIN_WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.OWNERSHIP_STATUS
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.PARENT_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.QTY
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.SERIAL_NUMBER
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.STATUS
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.AssetReviewContentEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.dataBase.review.AssetReviewContentContract.getAllTempColumns
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.common.SaveProgress
import com.dacosys.assetControl.model.review.AssetReview
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.network.utils.ProgressStatus
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList
import com.dacosys.assetControl.dataBase.asset.AssetContract.AssetEntry.Companion.ASSET_ID as A_ID
import com.dacosys.assetControl.dataBase.asset.AssetContract.AssetEntry.Companion.TABLE_NAME as A_TABLE_NAME
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.DESCRIPTION as IC_DESCRIPTION
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.ITEM_CATEGORY_ID as IC_ID
import com.dacosys.assetControl.dataBase.category.ItemCategoryContract.ItemCategoryEntry.Companion.TABLE_NAME as IC_TABLE_NAME
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.DESCRIPTION as WA_DESCRIPTION
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.TABLE_NAME as WA_TABLE_NAME
import com.dacosys.assetControl.dataBase.location.WarehouseAreaContract.WarehouseAreaEntry.Companion.WAREHOUSE_AREA_ID as WA_ID
import com.dacosys.assetControl.dataBase.location.WarehouseContract.WarehouseEntry.Companion.DESCRIPTION as W_DESCRIPTION
import com.dacosys.assetControl.dataBase.location.WarehouseContract.WarehouseEntry.Companion.TABLE_NAME as W_TABLE_NAME
import com.dacosys.assetControl.dataBase.location.WarehouseContract.WarehouseEntry.Companion.WAREHOUSE_ID as W_ID

/**
 * Created by Agustin on 28/12/2016.
 */

class AssetReviewContentDbHelper {
    fun insert(
        ar: AssetReview,
        arCont: Array<AssetReviewContent>,
        onSaveProgress: (SaveProgress) -> Unit = {},
    ): Boolean {
        if (arCont.isEmpty()) {
            return false
        }

        onSaveProgress.invoke(
            SaveProgress(
                msg = getContext()
                    .getString(R.string.adding_content_to_the_review),
                taskStatus = ProgressStatus.starting.id,
                progress = 0,
                total = 0
            )
        )

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
                    onSaveProgress.invoke(
                        SaveProgress(
                            msg = String.format(
                                getContext().getString(R.string.adding_asset_),
                                asset.code
                            ),
                            taskStatus = ProgressStatus.running.id,
                            progress = p,
                            total = t
                        )
                    )

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
            onSaveProgress.invoke(
                SaveProgress(
                    msg = getContext()
                        .getString(R.string.error_inserting_assets_to_review),
                    taskStatus = ProgressStatus.crashed.id,
                    progress = 0,
                    total = 0
                )
            )
        } else {
            onSaveProgress.invoke(
                SaveProgress(
                    msg = getContext().getString(R.string.insert_ok),
                    taskStatus = ProgressStatus.finished.id,
                    progress = 0,
                    total = 0
                )
            )
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

        val selection = "$ASSET_ID = ?"
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

        val selection = "$ASSET_REVIEW_CONTENT_ID = ?"
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

        val selection = "$ASSET_REVIEW_ID = ?"
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

        val selection = "$ASSET_REVIEW_CONTENT_ID = ?"
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

        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectById(id: Long): AssetReviewContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val where = " WHERE ($TABLE_NAME.$ASSET_REVIEW_CONTENT_ID = $id)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

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

    fun selectByAssetReviewCollectorId(id: Long): ArrayList<AssetReviewContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByAssetReviewCollectorId ($id)")

        val where = " WHERE ($TABLE_NAME.$ASSET_REVIEW_ID = $id)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    fun selectByDescription(description: String): ArrayList<AssetReviewContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByDescription ($description)")

        val where = " WHERE ($TABLE_NAME.$DESCRIPTION LIKE $description)"
        val rawQuery = basicSelect +
                "," +
                basicStrFields +
                " FROM " + TABLE_NAME +
                basicLeftJoin +
                where +
                " ORDER BY " + TABLE_NAME + "." + DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        return try {
            val c = sqLiteDatabase.rawQuery(rawQuery, null)
            fromCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        }
    }

    private fun fromCursor(c: Cursor?): ArrayList<AssetReviewContent> {
        val result = ArrayList<AssetReviewContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val assetReviewId = it.getLong(it.getColumnIndexOrThrow(ASSET_REVIEW_ID))
                    val assetReviewContentId = it.getLong(it.getColumnIndexOrThrow(ASSET_REVIEW_CONTENT_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION))
                    val qty = it.getFloat(it.getColumnIndexOrThrow(QTY))
                    val contentStatusId = it.getInt(it.getColumnIndexOrThrow(CONTENT_STATUS_ID))
                    val originWarehouseAreaId = it.getLong(it.getColumnIndexOrThrow(ORIGIN_WAREHOUSE_AREA_ID))
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val ownershipStatus = it.getInt(it.getColumnIndexOrThrow(OWNERSHIP_STATUS))
                    val status = it.getInt(it.getColumnIndexOrThrow(STATUS))
                    val itemCategoryId = it.getLong(it.getColumnIndexOrThrow(ITEM_CATEGORY_ID))
                    val labelNumber = it.getInt(it.getColumnIndexOrThrow(LABEL_NUMBER))
                    val manufacturer = it.getString(it.getColumnIndexOrThrow(MANUFACTURER)) ?: ""
                    val model = it.getString(it.getColumnIndexOrThrow(MODEL)) ?: ""
                    val serialNumber = it.getString(it.getColumnIndexOrThrow(SERIAL_NUMBER)) ?: ""
                    val parentId = it.getLong(it.getColumnIndexOrThrow(PARENT_ID))
                    val ean = it.getString(it.getColumnIndexOrThrow(EAN)) ?: ""
                    val itemCategoryStr = it.getString(it.getColumnIndexOrThrow(ITEM_CATEGORY_STR)) ?: ""
                    val warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""
                    val warehouseAreaStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR)) ?: ""

                    val temp = AssetReviewContent(
                        assetReviewId = assetReviewId,
                        assetReviewContentId = assetReviewContentId,
                        assetId = assetId,
                        code = code,
                        description = description,
                        qty = qty,
                        contentStatusId = contentStatusId,
                        originWarehouseAreaId = originWarehouseAreaId,
                        warehouseAreaId = warehouseAreaId,
                        ownershipStatusId = ownershipStatus,
                        assetStatusId = status,
                        itemCategoryId = itemCategoryId,
                        labelNumber = labelNumber,
                        manufacturer = manufacturer,
                        model = model,
                        serialNumber = serialNumber,
                        parentId = parentId,
                        ean = ean,
                        itemCategoryStr = itemCategoryStr,
                        warehouseStr = warehouseStr,
                        warehouseAreaStr = warehouseAreaStr
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

    // region TABLA E IDS TEMPORALES

    // Utilizamos tablas temporales para guardar listas largas de movimientos y revisiones
    // y así evitar errores entre actividades o en cambios de configuración:
    // "FAILED BINDER TRANSACTION"
    private fun createTempTable() {
        val allCommands: ArrayList<String> = ArrayList()
        allCommands.add(CREATE_TEMP_TABLE)
        for (sql in CREATE_TEMP_INDEX) {
            allCommands.add(sql)
        }

        val sqLiteDatabase = getWritableDb()
        sqLiteDatabase.beginTransaction()
        try {
            for (sql in allCommands) {
                sqLiteDatabase.execSQL(sql)
            }
            sqLiteDatabase.setTransactionSuccessful()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun deleteTemp(): Boolean {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> delete")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete("$temp$TABLE_NAME", null, null) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    private fun deleteTempByAssetReviewId(id: Long): Boolean {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> deleteByAssetReviewId ($id)")

        val selection = "$ASSET_REVIEW_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.delete(
                "$temp$TABLE_NAME",
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun selectByTempId(id: Long): ArrayList<AssetReviewContent> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllTempColumns()
        val selection = "$ASSET_REVIEW_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val order = DESCRIPTION

        val sqLiteDatabase = getReadableDb()
        sqLiteDatabase.beginTransaction()
        try {
            val c = sqLiteDatabase.query(
                "$temp$TABLE_NAME", // Nombre de la tabla
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

    fun insertTempList(arId: Long, reviewList: ArrayList<AssetReviewContent>): Boolean {
        createTempTable()
        deleteTempByAssetReviewId(arId)

        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val splitList = splitList(reviewList.toTypedArray(), 100)

        val sqLiteDatabase = getWritableDb()

        try {
            sqLiteDatabase.delete("$temp$TABLE_NAME", null, null)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        }

        try {
            sqLiteDatabase.beginTransaction()
            var arContId = 0
            for (part in splitList) {
                var insertQ =
                    ("INSERT INTO " + "$temp$TABLE_NAME" + " (" +
                            ASSET_REVIEW_ID + "," +
                            ASSET_REVIEW_CONTENT_ID + "," +
                            ASSET_ID + "," +
                            CODE + "," +
                            DESCRIPTION + "," +
                            QTY + "," +
                            CONTENT_STATUS_ID + "," +
                            ORIGIN_WAREHOUSE_AREA_ID + "," +
                            STATUS + "," +
                            LABEL_NUMBER + "," +
                            PARENT_ID + "," +
                            WAREHOUSE_AREA_ID + "," +
                            WAREHOUSE_AREA_STR + "," +
                            WAREHOUSE_STR + "," +
                            ITEM_CATEGORY_ID + "," +
                            ITEM_CATEGORY_STR + "," +
                            OWNERSHIP_STATUS + "," +
                            MANUFACTURER + "," +
                            MODEL + "," +
                            SERIAL_NUMBER + "," +
                            EAN + ")" +
                            " VALUES ")

                for (i in part) {
                    arContId++
                    val values =
                        "(${arId}," +
                                "${arContId}," +
                                "${i.assetId}," +
                                "'${i.code.replace("'", "''")}'," +
                                "'${i.description.replace("'", "''")}'," +
                                "${i.qty}," +
                                "${i.contentStatusId}," +
                                "${i.originWarehouseAreaId}," +
                                "${i.assetStatusId}," +
                                "${i.labelNumber}," +
                                "${i.parentId}," +
                                "${i.warehouseAreaId}," +
                                "'${i.warehouseAreaStr.replace("'", "''")}'," +
                                "'${i.warehouseStr.replace("'", "''")}'," +
                                "${i.itemCategoryId}," +
                                "'${i.itemCategoryStr.replace("'", "''")}'," +
                                "${i.ownershipStatusId}," +
                                "'${i.manufacturer.replace("'", "''")}'," +
                                "'${i.model.replace("'", "''")}'," +
                                "'${i.serialNumber.replace("'", "''")}'," +
                                "'${i.ean.replace("'", "''")}'),"

                    insertQ = "$insertQ$values"
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                Log.i(this.javaClass.simpleName, insertQ)

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

    private val basicSelect = "SELECT " +
            TABLE_NAME + "." + ASSET_REVIEW_ID + "," +
            TABLE_NAME + "." + ASSET_REVIEW_CONTENT_ID + "," +
            TABLE_NAME + "." + ASSET_ID + "," +
            TABLE_NAME + "." + CODE + "," +
            TABLE_NAME + "." + DESCRIPTION + "," +
            TABLE_NAME + "." + QTY + "," +
            TABLE_NAME + "." + CONTENT_STATUS_ID + "," +
            TABLE_NAME + "." + ORIGIN_WAREHOUSE_AREA_ID

    private val basicStrFields =
        A_TABLE_NAME + "." + WAREHOUSE_AREA_ID + " AS " + WAREHOUSE_AREA_ID + "," +
                A_TABLE_NAME + "." + ITEM_CATEGORY_ID + " AS " + ITEM_CATEGORY_ID + "," +
                A_TABLE_NAME + "." + STATUS + " AS " + STATUS + "," +
                A_TABLE_NAME + "." + PARENT_ID + " AS " + PARENT_ID + "," +
                A_TABLE_NAME + "." + LABEL_NUMBER + " AS " + LABEL_NUMBER + "," +
                A_TABLE_NAME + "." + OWNERSHIP_STATUS + " AS " + OWNERSHIP_STATUS + "," +
                A_TABLE_NAME + "." + MANUFACTURER + " AS " + MANUFACTURER + "," +
                A_TABLE_NAME + "." + MODEL + " AS " + MODEL + "," +
                A_TABLE_NAME + "." + SERIAL_NUMBER + " AS " + SERIAL_NUMBER + "," +
                A_TABLE_NAME + "." + EAN + " AS " + EAN + "," +
                W_TABLE_NAME + "." + W_DESCRIPTION + " AS " + W_TABLE_NAME + "_str" + "," +
                WA_TABLE_NAME + "." + WA_DESCRIPTION + " AS " + WA_TABLE_NAME + "_str" + "," +
                IC_TABLE_NAME + "." + IC_DESCRIPTION + " AS " + IC_TABLE_NAME + "_str"


    private val basicLeftJoin =
        " LEFT OUTER JOIN " + A_TABLE_NAME + " ON " + TABLE_NAME + "." + ASSET_ID + " = " + A_TABLE_NAME + "." + A_ID +
                " LEFT OUTER JOIN " + WA_TABLE_NAME + " ON " + TABLE_NAME + "." + ORIGIN_WAREHOUSE_AREA_ID + " = " + WA_TABLE_NAME + "." + WA_ID +
                " LEFT OUTER JOIN " + W_TABLE_NAME + " ON " + WA_TABLE_NAME + "." + W_ID + " = " + W_TABLE_NAME + "." + W_ID +
                " LEFT OUTER JOIN " + IC_TABLE_NAME + " ON " + A_TABLE_NAME + "." + ITEM_CATEGORY_ID + " = " + IC_TABLE_NAME + "." + IC_ID

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

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + "$temp$TABLE_NAME" + "]"
                + "( [" + ASSET_REVIEW_ID + "] BIGINT NOT NULL, "
                + " [" + ASSET_REVIEW_CONTENT_ID + "] BIGINT NOT NULL UNIQUE, "
                + " [" + ASSET_ID + "] BIGINT, "
                + " [" + CODE + "] NVARCHAR ( 45 ) NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + QTY + "] DECIMAL ( 12,4 ), "
                + " [" + CONTENT_STATUS_ID + "] INT ( 1 ) NOT NULL, "
                + " [" + ORIGIN_WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + STATUS + "] INT NOT NULL DEFAULT 1, "
                + " [" + LABEL_NUMBER + "] INT, "
                + " [" + PARENT_ID + "] BIGINT, "
                + " [" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + WAREHOUSE_AREA_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + WAREHOUSE_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ITEM_CATEGORY_ID + "] BIGINT NOT NULL DEFAULT 0, "
                + " [" + ITEM_CATEGORY_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + OWNERSHIP_STATUS + "] INT NOT NULL DEFAULT 1, "
                + " [" + MANUFACTURER + "] NVARCHAR ( 255 ), "
                + " [" + MODEL + "] NVARCHAR ( 255 ), "
                + " [" + SERIAL_NUMBER + "] NVARCHAR ( 255 ), "
                + " [" + EAN + "] NVARCHAR ( 100 ), "
                + " PRIMARY KEY( [" + ASSET_REVIEW_CONTENT_ID + "]) )")

        val CREATE_TEMP_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ASSET_REVIEW_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$CODE]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ITEM_CATEGORY_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$SERIAL_NUMBER]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$EAN]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID]",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ASSET_REVIEW_ID] ON [${temp}${TABLE_NAME}] ([$ASSET_REVIEW_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ASSET_ID] ON [$temp$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$CODE] ON [$temp$TABLE_NAME] ([$CODE])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$DESCRIPTION] ON [$temp$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ITEM_CATEGORY_ID] ON [$temp$TABLE_NAME] ([$ITEM_CATEGORY_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$temp$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$SERIAL_NUMBER] ON [$temp$TABLE_NAME] ([$SERIAL_NUMBER])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$EAN] ON [$temp$TABLE_NAME] ([$EAN])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ORIGIN_WAREHOUSE_AREA_ID] ON [${temp}${TABLE_NAME}] ([$ORIGIN_WAREHOUSE_AREA_ID])"
        )
    }
}