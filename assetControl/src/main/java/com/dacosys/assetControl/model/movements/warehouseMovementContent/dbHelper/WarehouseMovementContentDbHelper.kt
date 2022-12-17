package com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getReadableDb
import com.dacosys.assetControl.dataBase.DataBaseHelper.Companion.getWritableDb
import com.dacosys.assetControl.model.assets.asset.`object`.Asset
import com.dacosys.assetControl.model.assets.asset.dbHelper.AssetDbHelper
import com.dacosys.assetControl.model.movements.warehouseMovement.`object`.WarehouseMovement
import com.dacosys.assetControl.model.movements.warehouseMovementContent.`object`.WarehouseMovementContent
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.ASSET_ID
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.CODE
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.QTY
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.TABLE_NAME
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_CONTENT_ID
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.WarehouseMovementContentEntry.Companion.WAREHOUSE_MOVEMENT_ID
import com.dacosys.assetControl.model.movements.warehouseMovementContent.dbHelper.WarehouseMovementContentContract.getAllColumns
import com.dacosys.assetControl.model.reviews.assetReviewContent.`object`.AssetReviewContent
import com.dacosys.assetControl.utils.errorLog.ErrorLog

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

    fun insert(
        warehouseMovementId: Long,
        warehouseMovementContentId: Long,
        assetId: Long,
        code: String,
        qty: Float,
    ): Boolean {
        Log.i(this::class.java.simpleName, ": SQLite -> insert")

        val newWarehouseMovementContent = WarehouseMovementContent(
            warehouseMovementId,
            warehouseMovementContentId,
            assetId,
            code,
            qty
        )

        val sqLiteDatabase = getWritableDb()
        return try {
            return sqLiteDatabase.insert(
                TABLE_NAME, null,
                newWarehouseMovementContent.toContentValues()
            ) > 0
        } catch (ex: SQLException) {
            ex.printStackTrace()
            ErrorLog.writeLog(null, this::class.java.simpleName, ex)
            false
        }
    }

    fun insertAr(
        warehouseMovement: WarehouseMovement,
        arCont: ArrayList<AssetReviewContent>,
    ): Boolean {
        return insertAr(warehouseMovement, arCont, true)
    }

    private fun insertAr(
        warehouseMovement: WarehouseMovement,
        wmCont: ArrayList<AssetReviewContent>,
        setOnInventory: Boolean,
    ): Boolean {
        var isOk = false

        try {
            for (content in wmCont) {
                isOk = insert(
                    warehouseMovement.collectorWarehouseMovementId,
                    lastId,
                    content.assetId,
                    content.code,
                    1F
                )

                if (!isOk) {
                    break
                }
            }

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
        arCont: ArrayList<WarehouseMovementContent>,
    ): Boolean {
        return insertWm(warehouseMovement, arCont, true)
    }

    private fun insertWm(
        warehouseMovement: WarehouseMovement,
        wmCont: ArrayList<WarehouseMovementContent>,
        setOnInventory: Boolean,
    ): Boolean {
        var isOk = false

        try {
            for (content in wmCont) {
                isOk = insert(
                    warehouseMovement.collectorWarehouseMovementId,
                    lastId,
                    content.assetId,
                    content.code,
                    1F
                )

                if (!isOk) {
                    break
                }
            }

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

        val selection = "$ASSET_ID = ?" // WHERE code LIKE ?
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

        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?" // WHERE code LIKE ?
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

        val selection = "$WAREHOUSE_MOVEMENT_ID = ?" // WHERE code LIKE ?
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

        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?" // WHERE code LIKE ?
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
        val selection = "$WAREHOUSE_MOVEMENT_ID = ?" // WHERE code LIKE ?
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
        val selection = "$WAREHOUSE_MOVEMENT_CONTENT_ID = ?" // WHERE code LIKE ?
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

    fun selectByCode(code: String): ArrayList<WarehouseMovementContent> {
        Log.i(this::class.java.simpleName, ": SQLite -> selectByCode ($code)")

        val columns = getAllColumns()
        val selection = "$CODE LIKE ?" // WHERE code LIKE ?
        val selectionArgs = arrayOf("%$code%")
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

    private fun fromCursor(c: Cursor?): ArrayList<WarehouseMovementContent> {
        val result = ArrayList<WarehouseMovementContent>()
        c.use {
            if (it != null) {
                while (it.moveToNext()) {
                    val warehouseMovementId =
                        it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_ID))
                    val warehouseMovementContentId =
                        it.getLong(it.getColumnIndexOrThrow(WAREHOUSE_MOVEMENT_CONTENT_ID))
                    val assetId = it.getLong(it.getColumnIndexOrThrow(ASSET_ID))
                    val code = it.getString(it.getColumnIndexOrThrow(CODE))
                    val qty = it.getFloat(it.getColumnIndexOrThrow(QTY))

                    val temp = WarehouseMovementContent(
                        warehouseMovementId,
                        warehouseMovementContentId,
                        assetId,
                        code,
                        qty
                    )
                    result.add(temp)
                }
            }
        }
        return result
    }

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
    }
}