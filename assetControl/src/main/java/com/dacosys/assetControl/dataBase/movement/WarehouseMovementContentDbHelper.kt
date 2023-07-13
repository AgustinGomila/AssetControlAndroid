package com.dacosys.assetControl.dataBase.movement

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.dataBase.asset.AssetDbHelper
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.CODE
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.CONTENT_STATUS
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.DESCRIPTION
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.EAN
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.ITEM_CATEGORY_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.ITEM_CATEGORY_STR
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.LABEL_NUMBER
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.MANUFACTURER
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.MODEL
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.OWNERSHIP_STATUS
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.PARENT_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.QTY
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.SERIAL_NUMBER
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.STATUS
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_AREA_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_AREA_STR
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_CONTENT_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_STR
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.getAllColumns
import com.dacosys.assetControl.dataBase.movement.WarehouseMovementContentContract.getAllTempColumns
import com.dacosys.assetControl.model.asset.Asset
import com.dacosys.assetControl.model.movement.WarehouseMovement
import com.dacosys.assetControl.model.movement.WarehouseMovementContent
import com.dacosys.assetControl.model.review.AssetReviewContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog
import com.dacosys.assetControl.utils.misc.splitList

/**
 * Created by Agustin on 28/12/2016.
 */

class WarehouseMovementContentDbHelper {
    private val lastId: Long
        get() {
            Log.i(this::class.java.simpleName, ": SQLite -> lastId")

            val sqLiteDatabase = getReadableDb()
            return try {
                val mCount = sqLiteDatabase.rawQuery(
                    "SELECT MAX($WAREHOUSE_MOVEMENT_CONTENT_ID) FROM $TABLE_NAME",
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

    data class WmcInsertData(var assetId: Long, var code: String, var qty: Float)

    fun insert(warehouseMovementId: Long, itemIdArray: Array<WmcInsertData>): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        if (itemIdArray.isEmpty()) {
            return false
        }

        var tempLastId = lastId

        val sqLiteDatabase = getWritableDb()

        val splitList = splitList(itemIdArray, 20)
        var error = false
        try {
            sqLiteDatabase.beginTransaction()
            for (part in splitList) {
                var insertQ =
                    ("INSERT INTO $TABLE_NAME (" +
                            "$WAREHOUSE_MOVEMENT_ID," +
                            "$WAREHOUSE_MOVEMENT_CONTENT_ID," +
                            "$ASSET_ID," +
                            "$CODE," +
                            "$QTY) VALUES ")

                for (i in part) {
                    val values =
                        "(${warehouseMovementId}," +
                                "${tempLastId}," +
                                "${i.assetId}," +
                                "'${i.code}'," +
                                "${i.qty}" + "),"

                    insertQ = "$insertQ$values"
                    tempLastId++
                }

                if (insertQ.endsWith(",")) {
                    insertQ = insertQ.substring(0, insertQ.length - 1)
                }

                Log.i(this.javaClass.simpleName, insertQ)

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

        return !error
    }

    fun insertAr(
        warehouseMovement: WarehouseMovement,
        wmCont: ArrayList<AssetReviewContent>,
        setOnInventory: Boolean = true,
    ): Boolean {
        val isOk: Boolean

        try {
            val all: ArrayList<WmcInsertData> = ArrayList()
            for (content in wmCont) {
                all.add(
                    WmcInsertData(
                        assetId = content.assetId,
                        code = content.code,
                        qty = 1F
                    )
                )
            }

            isOk = insert(warehouseMovement.collectorWarehouseMovementId, all.toTypedArray())

            if (isOk) {
                if (setOnInventory) {
                    AssetDbHelper().setOnInventoryFromArCont(
                        warehouseMovement.destWarehouseArea!!,
                        wmCont
                    )
                } else {
                    AssetDbHelper().setNewLocationFromArCont(
                        warehouseMovement.destWarehouseArea!!,
                        wmCont
                    )
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }

        return isOk
    }

    fun insertWm(
        warehouseMovement: WarehouseMovement,
        wmCont: ArrayList<WarehouseMovementContent>,
        setOnInventory: Boolean = true,
    ): Boolean {
        val isOk: Boolean

        try {
            val all: ArrayList<WmcInsertData> = ArrayList()
            for (content in wmCont) {
                all.add(
                    WmcInsertData(
                        assetId = content.assetId,
                        code = content.code,
                        qty = 1F
                    )
                )
            }

            isOk = insert(warehouseMovement.collectorWarehouseMovementId, all.toTypedArray())

            if (isOk) {
                if (setOnInventory) {
                    AssetDbHelper().setOnInventoryFromWmCont(
                        warehouseMovement.destWarehouseArea!!,
                        wmCont
                    )
                } else {
                    AssetDbHelper().setNewLocationFromWmCont(
                        warehouseMovement.destWarehouseArea!!,
                        wmCont
                    )
                }
            }
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return false
        } finally {
        }

        return isOk
    }

    fun insert(warehouseMovementContent: WarehouseMovementContent): Long {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                warehouseMovementContent.toContentValues()
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

    fun update(warehouseMovementContent: WarehouseMovementContent): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> update")

        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?"
        val selectionArgs = arrayOf(warehouseMovementContent.warehouseMovementContentId.toString())

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.update(
                TABLE_NAME,
                warehouseMovementContent.toContentValues(),
                selection,
                selectionArgs
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun delete(warehouseMovementContent: WarehouseMovementContent): Boolean {
        return deleteById(warehouseMovementContent.warehouseMovementContentId)
    }

    fun deleteByWarehouseMovementId(id: Long): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> deleteByWarehouseMovementId ($id)")

        val selection = "$WAREHOUSE_MOVEMENT_ID = ?"
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

        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?"
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

    fun select(): ArrayList<WarehouseMovementContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllColumns()
        val order = CODE

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

    fun selectByCollectorWarehouseMovementId(wmId: Long): ArrayList<WarehouseMovementContent> {
        Log.i(
            this::class.java.simpleName,
            ": SQLite -> selectByCollectorWarehouseMovementId ($wmId)"
        )

        val columns = getAllColumns()
        val selection = "$WAREHOUSE_MOVEMENT_ID = ?"
        val selectionArgs = arrayOf(wmId.toString())
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

    fun selectById(id: Long): WarehouseMovementContent? {
        Log.i(this::class.java.simpleName, ": SQLite -> selectById ($id)")

        val columns = getAllColumns()
        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?"
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

    private fun fromCursor(c: Cursor?): ArrayList<WarehouseMovementContent> {
        val result = ArrayList<WarehouseMovementContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val warehouseMovementId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_ID))
                    val warehouseMovementContentId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_CONTENT_ID))

                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val qty = it.getFloat(it.getColumnIndexOrThrow(QTY))

                    val temp = WarehouseMovementContent(
                        warehouseMovementId = warehouseMovementId,
                        warehouseMovementContentId = warehouseMovementContentId,
                        assetId = assetId,
                        code = code,
                        qty = qty
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

    private fun deleteTempByWarehouseMovementId(id: Long): Boolean {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> deleteByWarehouseMovementId ($id)")

        val selection = "$WAREHOUSE_MOVEMENT_ID = ?"
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

    fun selectByTempId(id: Long): ArrayList<WarehouseMovementContent> {
        createTempTable()

        Log.i(this::class.java.simpleName, ": SQLite -> select")

        val columns = getAllTempColumns()
        val selection = "$WAREHOUSE_MOVEMENT_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val order = CODE

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
            return fromTempCursor(c)
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            return ArrayList()
        } finally {
            sqLiteDatabase.endTransaction()
        }
    }

    fun insertTempList(wmId: Long, movementList: ArrayList<WarehouseMovementContent>): Boolean {
        createTempTable()
        deleteTempByWarehouseMovementId(wmId)

        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val splitList = splitList(movementList.toTypedArray(), 100)

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
            var wmContId = 0
            for (part in splitList) {
                var insertQ =
                    ("INSERT INTO " + "$temp$TABLE_NAME" + " (" +
                            WAREHOUSE_MOVEMENT_ID + "," +
                            WAREHOUSE_MOVEMENT_CONTENT_ID + "," +
                            CONTENT_STATUS + "," +
                            ASSET_ID + "," +
                            CODE + "," +
                            DESCRIPTION + "," +
                            STATUS + "," +
                            WAREHOUSE_AREA_ID + "," +
                            QTY + "," +
                            LABEL_NUMBER + "," +
                            PARENT_ID + "," +
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
                    wmContId++
                    val values =
                        "(${wmId}," +
                                "${wmContId}," +
                                "${i.contentStatusId}," +
                                "${i.assetId}," +
                                "'${i.code.replace("'", "''")}'," +
                                "'${i.description.replace("'", "''")}'," +
                                "${i.assetStatusId}," +
                                "${i.warehouseAreaId}," +
                                "${i.qty}," +
                                "${i.labelNumber}," +
                                "${i.parentId}," +
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

    private fun fromTempCursor(c: Cursor?): ArrayList<WarehouseMovementContent> {
        val result = ArrayList<WarehouseMovementContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val warehouseMovementId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_ID))
                    val warehouseMovementContentId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_CONTENT_ID))
                    val contentStatus = it.getInt(it.getColumnIndexOrThrow(CONTENT_STATUS))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val qty = it.getFloat(it.getColumnIndexOrThrow(QTY))
                    val description = it.getString(it.getColumnIndexOrThrow(DESCRIPTION)) ?: ""
                    val warehouseAreaId = it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_AREA_ID))
                    val warehouseAreaStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_AREA_STR)) ?: ""
                    val warehouseStr = it.getString(it.getColumnIndexOrThrow(WAREHOUSE_STR)) ?: ""
                    val ownershipStatusId = it.getInt(it.getColumnIndexOrThrow(OWNERSHIP_STATUS))
                    val assetStatusId = it.getInt(it.getColumnIndexOrThrow(STATUS))
                    val itemCategoryId = it.getLong(it.getColumnIndexOrThrow(ITEM_CATEGORY_ID))
                    val itemCategoryStr = it.getString(it.getColumnIndexOrThrow(ITEM_CATEGORY_STR)) ?: ""
                    val labelNumber = it.getInt(it.getColumnIndexOrThrow(LABEL_NUMBER))
                    val manufacturer = it.getString(it.getColumnIndexOrThrow(MANUFACTURER)) ?: ""
                    val model = it.getString(it.getColumnIndexOrThrow(MODEL)) ?: ""
                    val serialNumber = it.getString(it.getColumnIndexOrThrow(SERIAL_NUMBER)) ?: ""
                    val parentId = it.getLong(it.getColumnIndexOrThrow(PARENT_ID))
                    val ean = it.getString(it.getColumnIndexOrThrow(EAN)) ?: ""

                    val temp = WarehouseMovementContent(
                        warehouseMovementId = warehouseMovementId,
                        warehouseMovementContentId = warehouseMovementContentId,
                        contentStatus = contentStatus,
                        assetId = assetId,
                        code = code,
                        qty = qty,
                        description = description,
                        warehouseAreaId = warehouseAreaId,
                        warehouseAreaStr = warehouseAreaStr,
                        warehouseStr = warehouseStr,
                        ownershipStatusId = ownershipStatusId,
                        assetStatusId = assetStatusId,
                        itemCategoryId = itemCategoryId,
                        itemCategoryStr = itemCategoryStr,
                        labelNumber = labelNumber,
                        manufacturer = manufacturer,
                        model = model,
                        serialNumber = serialNumber,
                        parentId = parentId,
                        ean = ean
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }
    // endregion TABLA E IDS TEMPORALES

    companion object {
        /*
        CREATE TABLE "warehouse_movement_content" (
        `warehouse_movement_id` bigint NOT NULL,
        `_id` bigint NOT NULL UNIQUE,
        `asset_id` bigint,
        `code` nvarchar ( 45 ) NOT NULL,
        `qty` numeric ( 12 , 4 ),
        PRIMARY KEY(`_id`) )
         */

        const val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS [" + TABLE_NAME + "]"
                + "( [" + WAREHOUSE_MOVEMENT_ID + "] BIGINT NOT NULL, "
                + " [" + WAREHOUSE_MOVEMENT_CONTENT_ID + "] BIGINT NOT NULL UNIQUE, "
                + " [" + ASSET_ID + "] BIGINT, "
                + " [" + CODE + "] NVARCHAR ( 45 ) NOT NULL, "
                + " [" + QTY + "] DECIMAL ( 12,4 ), "
                + " PRIMARY KEY( [" + WAREHOUSE_MOVEMENT_CONTENT_ID + "]) )")

        val CREATE_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_${TABLE_NAME}_$CODE]",
            "CREATE INDEX [IDX_${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID] ON [$TABLE_NAME] ([$WAREHOUSE_MOVEMENT_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$ASSET_ID] ON [$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_${TABLE_NAME}_$CODE] ON [$TABLE_NAME] ([$CODE])"
        )

        const val temp = "temp_"
        const val CREATE_TEMP_TABLE = ("CREATE TABLE IF NOT EXISTS [" + "$temp$TABLE_NAME" + "]"
                + "( [" + WAREHOUSE_MOVEMENT_ID + "] BIGINT NOT NULL, "
                + " [" + WAREHOUSE_MOVEMENT_CONTENT_ID + "] BIGINT NOT NULL UNIQUE, "
                + " [" + CONTENT_STATUS + "] INT ( 1 ) NOT NULL, "
                + " [" + ASSET_ID + "] BIGINT NOT NULL, "
                + " [" + CODE + "] NVARCHAR ( 45 ) NOT NULL, "
                + " [" + DESCRIPTION + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + STATUS + "] INT NOT NULL DEFAULT 1, "
                + " [" + WAREHOUSE_AREA_ID + "] BIGINT NOT NULL, "
                + " [" + LABEL_NUMBER + "] INT, "
                + " [" + PARENT_ID + "] BIGINT, "
                + " [" + QTY + "] DECIMAL ( 12,4 ), "
                + " [" + WAREHOUSE_AREA_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + WAREHOUSE_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + ITEM_CATEGORY_ID + "] BIGINT NOT NULL DEFAULT 0, "
                + " [" + ITEM_CATEGORY_STR + "] NVARCHAR ( 255 ) NOT NULL, "
                + " [" + OWNERSHIP_STATUS + "] INT NOT NULL DEFAULT 1, "
                + " [" + MANUFACTURER + "] NVARCHAR ( 255 ), "
                + " [" + MODEL + "] NVARCHAR ( 255 ), "
                + " [" + SERIAL_NUMBER + "] NVARCHAR ( 255 ), "
                + " [" + EAN + "] NVARCHAR ( 100 ), "
                + " CONSTRAINT [PK_" + ASSET_ID + "] PRIMARY KEY ([" + ASSET_ID + "]) )")

        val CREATE_TEMP_INDEX: ArrayList<String> = arrayListOf(
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$WAREHOUSE_MOVEMENT_CONTENT_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ASSET_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$CODE]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$DESCRIPTION]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$ITEM_CATEGORY_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$WAREHOUSE_AREA_ID]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$SERIAL_NUMBER]",
            "DROP INDEX IF EXISTS [IDX_$temp${TABLE_NAME}_$EAN]",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$WAREHOUSE_MOVEMENT_ID] ON [$temp$TABLE_NAME] ([$WAREHOUSE_MOVEMENT_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$WAREHOUSE_MOVEMENT_CONTENT_ID] ON [$temp$TABLE_NAME] ([$WAREHOUSE_MOVEMENT_CONTENT_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ASSET_ID] ON [$temp$TABLE_NAME] ([$ASSET_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$CODE] ON [$temp$TABLE_NAME] ([$CODE])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$DESCRIPTION] ON [$temp$TABLE_NAME] ([$DESCRIPTION])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$ITEM_CATEGORY_ID] ON [$temp$TABLE_NAME] ([$ITEM_CATEGORY_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$WAREHOUSE_AREA_ID] ON [$temp$TABLE_NAME] ([$WAREHOUSE_AREA_ID])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$SERIAL_NUMBER] ON [$temp$TABLE_NAME] ([$SERIAL_NUMBER])",
            "CREATE INDEX [IDX_$temp${TABLE_NAME}_$EAN] ON [$temp$TABLE_NAME] ([$EAN])",
        )
    }
}