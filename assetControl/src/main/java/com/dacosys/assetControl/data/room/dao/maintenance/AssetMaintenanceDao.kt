package com.dacosys.assetControl.data.room.dao.maintenance

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenance
import com.dacosys.assetControl.data.room.entity.maintenance.AssetMaintenance.Entry
import com.dacosys.assetControl.data.room.entity.maintenance.MaintenanceType

@Dao
interface AssetMaintenanceDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    fun select(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${aEntry.TABLE_NAME}.${aEntry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    fun selectActive(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED} = 0 " +
                BASIC_ORDER
    )
    fun selectNoTransferred(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.TRANSFERRED} = 0 " +
                "AND ${Entry.TABLE_NAME}.${Entry.ASSET_ID} = :assetId " +
                BASIC_ORDER
    )
    fun selectByAssetIdNotTransferred(assetId: Long): AssetMaintenance?

    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenance: AssetMaintenance)


    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.TRANSFERRED} = 1 WHERE ${Entry.ID} = :id")
    suspend fun updateTransferred(id: Long)

    @Update
    suspend fun update(maintenance: AssetMaintenance)


    /**
     * Get by formatted query
     *
     * @param query Ejemplo: [getMultiQuery]
     * @return Una lista de [AssetMaintenance]
     */
    @RawQuery
    fun getByQuery(query: SupportSQLiteQuery): List<AssetMaintenance>

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
    ): List<AssetMaintenance> {
        var where = String()
        val args: MutableList<Any> = ArrayList()
        var condAdded = false

        if (ean.isNotEmpty()) {
            where += "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.EAN} LIKE ?"
            args.add("${if (useLike) "%" else ""}$ean${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (description.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} LIKE ?"
            args.add("${if (useLike) "%" else ""}$description${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (code.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.CODE} LIKE ?"
            args.add("${if (useLike) "%" else ""}$code${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (serialNumber.isNotEmpty()) {
            where += if (condAdded) " OR " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.SERIAL_NUMBER} LIKE ?"
            args.add("${if (useLike) "%" else ""}$serialNumber${if (useLike) "%" else ""}")
            condAdded = true
        }

        if (itemCategoryId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.ITEM_CATEGORY_ID} = ?"
            args.add(itemCategoryId)
            condAdded = true
        }

        if (warehouseId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.WAREHOUSE_ID} = ?"
            args.add(warehouseId)
            condAdded = true
        }

        if (warehouseAreaId != null) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.WAREHOUSE_AREA_ID} = ?"
            args.add(warehouseAreaId)
            condAdded = true
        }

        if (onlyActive) {
            where += if (condAdded) " AND " else "WHERE "
            where += "${aEntry.TABLE_NAME}.${aEntry.ACTIVE} = 1"
        }

        val query = "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $where $BASIC_ORDER"

        return getByQuery(SimpleSQLiteQuery(query, args.toTypedArray()))
    }


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ASSET_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.MAINTENANCE_TYPE_ID}"

        private val aEntry = Asset.Entry
        private val mtEntry = MaintenanceType.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${aEntry.TABLE_NAME} ON ${aEntry.TABLE_NAME}.${aEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ASSET_ID} " +
                    "LEFT JOIN ${mtEntry.TABLE_NAME} ON ${mtEntry.TABLE_NAME}.${mtEntry.ID} = ${Entry.TABLE_NAME}.${Entry.MAINTENANCE_TYPE_ID} "

        const val BASIC_JOIN_FIELDS =
            "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} AS ${Entry.ASSET_STR}," +
                    "${aEntry.TABLE_NAME}.${aEntry.CODE} AS ${Entry.ASSET_CODE}," +
                    "${mtEntry.TABLE_NAME}.${mtEntry.DESCRIPTION} AS ${Entry.MAINTENANCE_TYPE_STR}"
    }
}
