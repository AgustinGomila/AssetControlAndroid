package com.example.assetControl.data.room.dao.maintenance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.assetControl.data.room.dto.asset.AssetEntry
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenance
import com.example.assetControl.data.room.dto.maintenance.AssetMaintenanceEntry
import com.example.assetControl.data.room.dto.maintenance.MaintenanceTypeEntry
import com.example.assetControl.data.room.entity.maintenance.AssetMaintenanceEntity

@Dao
interface AssetMaintenanceDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetEntry.TABLE_NAME}.${AssetEntry.ACTIVE} = 1 " +
                BASIC_ORDER
    )
    suspend fun selectActive(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.TRANSFERRED} = 0 " +
                BASIC_ORDER
    )
    suspend fun selectNoTransferred(): List<AssetMaintenance>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.TRANSFERRED} = 0 " +
                "AND ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.ASSET_ID} = :assetId " +
                BASIC_ORDER
    )
    suspend fun selectByAssetIdNotTransferred(assetId: Long): AssetMaintenance?

    @Query("SELECT MAX(${AssetMaintenanceEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenance: AssetMaintenanceEntity)


    @Query("UPDATE ${AssetMaintenanceEntry.TABLE_NAME} SET ${AssetMaintenanceEntry.TRANSFERRED} = 1 WHERE ${AssetMaintenanceEntry.ID} = :id")
    suspend fun updateTransferred(id: Long)

    @Update
    suspend fun update(maintenance: AssetMaintenanceEntity)


    /**
     * Get by formatted query
     *
     * @param query Ejemplo: [getMultiQuery]
     * @return Una lista de [AssetMaintenance]
     */
    @RawQuery
    suspend fun getByQuery(query: SupportSQLiteQuery): List<AssetMaintenance>

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
    suspend fun getMultiQuery(
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


    companion object {
        const val BASIC_SELECT = "SELECT ${AssetMaintenanceEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${AssetMaintenanceEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.ASSET_ID}, " +
                "${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.MAINTENANCE_TYPE_ID}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${AssetEntry.TABLE_NAME} ON ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.ASSET_ID} " +
                    "LEFT JOIN ${MaintenanceTypeEntry.TABLE_NAME} ON ${MaintenanceTypeEntry.TABLE_NAME}.${MaintenanceTypeEntry.ID} = ${AssetMaintenanceEntry.TABLE_NAME}.${AssetMaintenanceEntry.MAINTENANCE_TYPE_ID} "

        const val BASIC_JOIN_FIELDS =
            "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} AS ${AssetMaintenanceEntry.ASSET_STR}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} AS ${AssetMaintenanceEntry.ASSET_CODE}," +
                    "${MaintenanceTypeEntry.TABLE_NAME}.${MaintenanceTypeEntry.DESCRIPTION} AS ${AssetMaintenanceEntry.MAINTENANCE_TYPE_STR}"
    }
}
