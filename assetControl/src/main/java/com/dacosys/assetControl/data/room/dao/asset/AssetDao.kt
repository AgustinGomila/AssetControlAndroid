package com.dacosys.assetControl.data.room.dao.asset

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.dacosys.assetControl.data.enums.asset.AssetStatus
import com.dacosys.assetControl.data.room.dto.asset.Asset
import com.dacosys.assetControl.data.room.dto.asset.Asset.Entry
import com.dacosys.assetControl.data.room.dto.category.ItemCategory
import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.asset.AssetEntity

@Dao
interface AssetDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} IN (:ids) " +
                BASIC_ORDER
    )
    suspend fun selectByTempIds(ids: List<Long>): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectActive(): List<Asset>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMinId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id"
    )
    suspend fun selectById(id: Long): Asset?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE '%' || :description || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByDescription(description: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.CODE} LIKE :code || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByCode(code: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.SERIAL_NUMBER} LIKE :serialNumber || '%' " +
                BASIC_ORDER
    )
    suspend fun selectBySerialNumber(serialNumber: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.EAN} LIKE :ean || '%' " +
                BASIC_ORDER
    )
    suspend fun selectByEan(ean: String): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1"
    )
    suspend fun selectByIdActive(id: Long): Asset?

    @Query(
        "SELECT COUNT(*) $BASIC_FROM WHERE (${Entry.CODE} = :code) " +
                "AND (${Entry.ID} != :assetId)"
    )
    suspend fun codeExists(code: String, assetId: Long): Int

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED} = 0 $BASIC_ORDER"
    )
    suspend fun selectNoTransferred(): List<Asset>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} = :warehouseAreaId " +
                "AND ${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1 " +
                "AND ${Entry.TABLE_NAME}.${Entry.STATUS} != :status $BASIC_ORDER"
    )
    suspend fun selectByWarehouseAreaIdActiveNotRemoved(
        warehouseAreaId: Long,
        status: Int = AssetStatus.removed.id
    ): List<Asset>

    @Query("SELECT DISTINCT ${Entry.CODE} $BASIC_FROM ORDER BY ${Entry.CODE}")
    suspend fun selectDistinctCodes(): List<String>

    @Query("SELECT DISTINCT ${Entry.SERIAL_NUMBER} $BASIC_FROM ORDER BY ${Entry.SERIAL_NUMBER}")
    suspend fun selectDistinctSerials(): List<String>

    @Query(
        "SELECT DISTINCT ${Entry.CODE} $BASIC_FROM " +
                "WHERE ${Entry.WAREHOUSE_AREA_ID} = :warehouseAreaId ORDER BY ${Entry.CODE}"
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
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue " +
                "WHERE ${Entry.ID} = :oldValue"
    )
    suspend fun updateId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :newValue " +
                "WHERE ${Entry.WAREHOUSE_ID} = :oldValue"
    )
    suspend fun updateWarehouseId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_AREA_ID} = :newValue " +
                "WHERE ${Entry.WAREHOUSE_AREA_ID} = :oldValue"
    )
    suspend fun updateWarehouseAreaId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.ITEM_CATEGORY_ID} = :newValue " +
                "WHERE ${Entry.ITEM_CATEGORY_ID} = :oldValue"
    )
    suspend fun updateItemCategoryId(oldValue: Long, newValue: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :warehouseId, " +
                "${Entry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${Entry.TRANSFERRED} = 0, " +
                "${Entry.LAST_ASSET_REVIEW_DATE} = :date " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateOnInventoryRemoved(ids: Array<Long>, warehouseId: Long, warehouseAreaId: Long, date: String)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :warehouseId, " +
                "${Entry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${Entry.TRANSFERRED} = 0 " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateLocation(ids: Array<Long>, warehouseId: Long, warehouseAreaId: Long)

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.TRANSFERRED} = 0, " +
                "${Entry.STATUS} = :status, " +
                "${Entry.MISSING_DATE} = :date " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateMissing(
        ids: Array<Long>,
        status: Int = AssetStatus.missing.id,
        date: String
    )

    @Query(
        "UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :warehouseId, " +
                "${Entry.WAREHOUSE_AREA_ID} = :warehouseAreaId, " +
                "${Entry.TRANSFERRED} = 0, " +
                "${Entry.STATUS} = :status, " +
                "${Entry.MISSING_DATE} = NULL, " +
                "${Entry.LAST_ASSET_REVIEW_DATE} = :date " +
                "WHERE ${Entry.ID} IN (:ids)"
    )
    suspend fun updateOnInventory(
        ids: Array<Long>,
        warehouseId: Long,
        warehouseAreaId: Long,
        status: Int = AssetStatus.onInventory.id,
        date: String
    )

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.TRANSFERRED} = 1 WHERE ${Entry.ID} IN (:ids)")
    suspend fun updateTransferred(
        ids: Array<Long>
    )


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(item: AssetEntity)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
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
            where += "${Entry.TABLE_NAME}.${Entry.EAN} LIKE ?"
            args.add("${if (useLike) "%" else ""}$ean${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (description.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.DESCRIPTION} LIKE ?"
            args.add("${if (useLike) "%" else ""}$description${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (code.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.CODE} LIKE ?"
            args.add("${if (useLike) "%" else ""}$code${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (serialNumber.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.SERIAL_NUMBER} LIKE ?"
            args.add("${if (useLike) "%" else ""}$serialNumber${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (itemCategoryId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.ITEM_CATEGORY_ID} = ?"
            args.add(itemCategoryId)
            condAdded = true
        }

        if (warehouseId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.WAREHOUSE_ID} = ?"
            args.add(warehouseId)
            condAdded = true
        }

        if (warehouseAreaId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} = ?"
            args.add(warehouseAreaId)
            condAdded = true
        }

        if (onlyActive) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${Entry.TABLE_NAME}.${Entry.ACTIVE} = 1"
        }

        val query = "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $where $BASIC_ORDER"

        return getByQuery(SimpleSQLiteQuery(query, args.toTypedArray()))
    }

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}, " +
                    "${Entry.TABLE_NAME}.${Entry.CODE}, " +
                    "${Entry.TABLE_NAME}.${Entry.ID}"

        private val catEntry = ItemCategory.Entry
        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry

        private const val ORIG_PREFIX = "orig"

        const val BASIC_LEFT_JOIN =
            " LEFT JOIN ${catEntry.TABLE_NAME} ON ${catEntry.TABLE_NAME}.${catEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ITEM_CATEGORY_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} ON ${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${waEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ORIGINAL_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} AS ${ORIG_PREFIX}_${wEntry.TABLE_NAME} " +
                    "ON ${ORIG_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.ID} = orig_${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} "


        const val BASIC_JOIN_FIELDS =
            "${catEntry.TABLE_NAME}.${catEntry.DESCRIPTION} AS ${Entry.ITEM_CATEGORY_STR}," +
                    "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}," +
                    "${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_AREA_STR}," +
                    "${ORIG_PREFIX}_${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.ORIGINAL_WAREHOUSE_STR}," +
                    "${ORIG_PREFIX}_${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.ORIGINAL_WAREHOUSE_AREA_STR}"
    }
}