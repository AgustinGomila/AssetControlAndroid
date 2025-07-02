package com.dacosys.assetControl.data.room.dao.asset

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.room.dao.asset.AssetDao.AssetDaoEntry.Companion.BASIC_FROM
import com.dacosys.assetControl.data.room.dao.asset.AssetDao.AssetDaoEntry.Companion.BASIC_JOIN_FIELDS
import com.dacosys.assetControl.data.room.dao.asset.AssetDao.AssetDaoEntry.Companion.BASIC_LEFT_JOIN
import com.dacosys.assetControl.data.room.dao.asset.AssetDao.AssetDaoEntry.Companion.BASIC_ORDER
import com.dacosys.assetControl.data.room.dao.asset.AssetDao.AssetDaoEntry.Companion.BASIC_SELECT
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.asset.AssetEntry
import com.dacosys.assetControl.data.room.dto.category.ItemCategoryEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseEntry
import com.dacosys.assetControl.data.room.entity.asset.AssetEntity

@Dao
interface AssetDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} IN (:ids) " +
                BASIC_ORDER
    )
    suspend fun selectByTempIds(ids: List<Long>): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectActive(): List<Asset>

    @Query("SELECT MIN(${AssetEntry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): Asset?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} LIKE '%' || :description || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByDescription(description: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} LIKE :code || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByCode(code: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.SERIAL_NUMBER} LIKE :serialNumber || '%' " +
                BASIC_ORDER
    )
    suspend fun selectBySerialNumber(serialNumber: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.EAN} LIKE :ean || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByEan(ean: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = :id " +
                "AND ${AssetEntry.TABLE_NAME}.${AssetEntry.ACTIVE} = 1"
    )
    suspend fun selectByIdActive(id: Long): Asset?

    @Query(
        "SELECT COUNT(*) $BASIC_FROM WHERE (${AssetEntry.CODE} = :code) " +
                "AND (${AssetEntry.ID} != :assetId)"
    )
    suspend fun codeExists(code: String, assetId: Long): Int

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.TRANSFERRED} = 0 $BASIC_ORDER"
    )
    suspend fun selectNoTransferred(): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} = :warehouseAreaId " +
                "AND ${AssetEntry.TABLE_NAME}.${AssetEntry.ACTIVE} = 1 " +
                "AND ${AssetEntry.TABLE_NAME}.${AssetEntry.STATUS} != :status $BASIC_ORDER"
    )
    suspend fun selectByWarehouseAreaIdActiveNotRemoved(
        warehouseAreaId: Long,
        status: Int = AssetStatus.removed.id
    ): List<Asset>

    @Query("SELECT DISTINCT ${AssetEntry.CODE} $BASIC_FROM ORDER BY ${AssetEntry.CODE}")
    suspend fun selectDistinctCodes(): List<String>

    @Query("SELECT DISTINCT ${AssetEntry.SERIAL_NUMBER} $BASIC_FROM ORDER BY ${AssetEntry.SERIAL_NUMBER}")
    suspend fun selectDistinctSerials(): List<String>

    @Query(
        "SELECT DISTINCT ${AssetEntry.CODE} $BASIC_FROM " +
                "WHERE ${AssetEntry.WAREHOUSE_AREA_ID} = :warehouseAreaId ORDER BY ${AssetEntry.CODE}"
    )
    suspend fun selectDistinctCodesByWarehouseAreaId(warehouseAreaId: Long): List<String>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assets: List<AssetEntity>)

    @Transaction
    suspend fun insert(entities: List<Asset>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(AssetEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(asset: AssetEntity)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.ID} = :newValue " +
                "WHERE ${AssetEntry.ID} = :oldValue"
    )
    suspend fun updateId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.WAREHOUSE_ID} = :newValue " +
                "WHERE ${AssetEntry.WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${AssetEntry.WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.ITEM_CATEGORY_ID} = :newValue " +
                "WHERE ${AssetEntry.ITEM_CATEGORY_ID} = :oldValue"
    )
    suspend fun updateItemCategoryId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.WAREHOUSE_ID} = :warehouseId, " +
                "${AssetEntry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${AssetEntry.TRANSFERRED} = 0, " +
                "${AssetEntry.LAST_ASSET_REVIEW_DATE} = :date " +
                "WHERE ${AssetEntry.ID} IN (:ids)"
    )
    suspend fun updateOnInventoryRemoved(ids: Array<Long>, warehouseId: Long, warehouseAreaId: Long, date: String)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.WAREHOUSE_ID} = :warehouseId, " +
                "${AssetEntry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${AssetEntry.TRANSFERRED} = 0 " +
                "WHERE ${AssetEntry.ID} IN (:ids)"
    )
    suspend fun updateLocation(ids: Array<Long>, warehouseId: Long, warehouseAreaId: Long)

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.TRANSFERRED} = 0, " +
                "${AssetEntry.STATUS} = :status, " +
                "${AssetEntry.MISSING_DATE} = :date " +
                "WHERE ${AssetEntry.ID} IN (:ids)"
    )
    suspend fun updateMissing(
        ids: Array<Long>,
        status: Int = AssetStatus.missing.id,
        date: String
    )

    @Query(
        "UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.WAREHOUSE_ID} = :warehouseId, " +
                "${AssetEntry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${AssetEntry.TRANSFERRED} = 0, " +
                "${AssetEntry.STATUS} = :status, " +
                "${AssetEntry.MISSING_DATE} = NULL, " +
                "${AssetEntry.LAST_ASSET_REVIEW_DATE} = :date " +
                "WHERE ${AssetEntry.ID} IN (:ids)"
    )
    suspend fun updateOnInventory(
        ids: Array<Long>,
        warehouseId: Long,
        warehouseAreaId: Long,
        status: Int = AssetStatus.onInventory.id,
        date: String
    )

    @Query("UPDATE ${AssetEntry.TABLE_NAME} SET ${AssetEntry.TRANSFERRED} = 1 WHERE ${AssetEntry.ID} IN (:ids)")
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: AssetEntity)

    @Query("DELETE $BASIC_FROM WHERE ${AssetEntry.ID} = :id")
    suspend fun deleteById(id: Long): Int


    /**
     * Get by formatted query
     *
     * @param query Ejemplo: [getMultiQuery]
     * @return Una lista de [Asset]
     */
    @RawQuery
    fun getByQuery(query: SupportSQLiteQuery): List<Asset>

    /**
     * Get multi query
     *
     * @param ean
     * @param description
     * @param code
     * @param serialNumber
     * @param itemCategoryId
     * @param warehouseId
     * @param warehouseAreaId
     * @param useLike
     * @param onlyActive
     * @return
     */
    fun getMultiQuery(
        ean: String = "",
        description: String = "",
        code: String = "",
        serialNumber: String = "",
        itemCategoryId: Long? = null,
        warehouseId: Long? = null,
        warehouseAreaId: Long? = null,
        useLike: Boolean = false,
        onlyActive: Boolean = true,
    ): List<Asset> {
        var where = String()
        val args: MutableList<Any> = ArrayList()
        var condAdded = false

        if (ean.isNotEmpty()) {
            where += "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.EAN} LIKE ?"
            args.add("${if (useLike) "%" else ""}$ean${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (description.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} LIKE ?"
            args.add("${if (useLike) "%" else ""}$description${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (code.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} LIKE ?"
            args.add("${if (useLike) "%" else ""}$code${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (serialNumber.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.SERIAL_NUMBER} LIKE ?"
            args.add("${if (useLike) "%" else ""}$serialNumber${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (itemCategoryId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID} = ?"
            args.add(itemCategoryId)
            condAdded = true
        }

        if (warehouseId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_ID} = ?"
            args.add(warehouseId)
            condAdded = true
        }

        if (warehouseAreaId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} = ?"
            args.add(warehouseAreaId)
            condAdded = true
        }

        if (onlyActive) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${AssetEntry.TABLE_NAME}.${AssetEntry.ACTIVE} = 1"
        }

        val query = "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $where $BASIC_ORDER"

        return getByQuery(SimpleSQLiteQuery(query, args.toTypedArray()))
    }

    abstract class AssetDaoEntry {
        companion object {
            const val BASIC_SELECT = "SELECT ${AssetEntry.TABLE_NAME}.*"
            const val BASIC_FROM = "FROM ${AssetEntry.TABLE_NAME}"
            const val BASIC_ORDER =
                "ORDER BY ${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION}, " +
                        "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE}, " +
                        "${AssetEntry.TABLE_NAME}.${AssetEntry.ID}"


            private const val ORIG_PREFIX = "orig"

            const val BASIC_LEFT_JOIN =
                " LEFT JOIN ${ItemCategoryEntry.TABLE_NAME} ON ${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID} " +
                        "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} " +
                        "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} " +
                        "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME} " +
                        "ON ${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.ORIGINAL_WAREHOUSE_AREA_ID} " +
                        "LEFT JOIN ${WarehouseEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME} " +
                        "ON ${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = orig_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} "


            const val BASIC_JOIN_FIELDS =
                "${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.DESCRIPTION} AS ${AssetEntry.ITEM_CATEGORY_STR}," +
                        "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${AssetEntry.WAREHOUSE_STR}," +
                        "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${AssetEntry.WAREHOUSE_AREA_STR}," +
                        "${ORIG_PREFIX}_${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${AssetEntry.ORIGINAL_WAREHOUSE_STR}," +
                        "${ORIG_PREFIX}_${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${AssetEntry.ORIGINAL_WAREHOUSE_AREA_STR}"
        }
    }
}