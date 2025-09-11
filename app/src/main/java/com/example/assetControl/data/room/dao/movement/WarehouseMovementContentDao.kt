package com.example.assetControl.data.room.dao.movement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.assetControl.data.room.dto.asset.AssetEntry
import com.example.assetControl.data.room.dto.category.ItemCategoryEntry
import com.example.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.example.assetControl.data.room.dto.location.WarehouseEntry
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContent
import com.example.assetControl.data.room.dto.movement.WarehouseMovementContentEntry
import com.example.assetControl.data.room.dto.movement.WarehouseMovementEntry
import com.example.assetControl.data.room.entity.movement.WarehouseMovementContentEntity

@Dao
interface WarehouseMovementContentDao {
    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseMovementContentEntry.TABLE_NAME}.${WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID} = :id " +
                BASIC_ORDER
    )
    suspend fun selectByWarehouseMovementId(id: Long): List<WarehouseMovementContent>

    @Query("SELECT MAX(${WarehouseMovementContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: WarehouseMovementContentEntity): Long

    @Transaction
    suspend fun insert(entities: List<WarehouseMovementContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(WarehouseMovementContentEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(content: WarehouseMovementContentEntity)

    @Query("UPDATE ${WarehouseMovementContentEntry.TABLE_NAME} SET ${WarehouseMovementContentEntry.ASSET_ID} = :newValue WHERE ${WarehouseMovementContentEntry.ASSET_ID} = :oldValue")
    suspend fun updateAssetId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${WarehouseMovementContentEntry.TABLE_NAME} SET ${WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID} = :newValue " +
                "WHERE ${WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID} = :oldValue"
    )
    suspend fun updateMovementId(newValue: Long, oldValue: Long)


    @Delete
    suspend fun delete(content: WarehouseMovementContentEntity)

    @Query(
        "DELETE $BASIC_FROM WHERE ${WarehouseMovementContentEntry.WAREHOUSE_MOVEMENT_ID} " +
                "IN (SELECT ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.ID} " +
                "FROM ${WarehouseMovementEntry.TABLE_NAME} " +
                "WHERE ${WarehouseMovementEntry.TABLE_NAME}.${WarehouseMovementEntry.TRANSFERRED_DATE} IS NOT NULL )"
    )
    suspend fun deleteTransferred()


    companion object {
        const val BASIC_SELECT = "SELECT ${WarehouseMovementContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${WarehouseMovementContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${WarehouseMovementContentEntry.TABLE_NAME}.${WarehouseMovementContentEntry.ID}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${AssetEntry.TABLE_NAME} ON ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} = ${WarehouseMovementContentEntry.TABLE_NAME}.${WarehouseMovementContentEntry.ASSET_ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${ItemCategoryEntry.TABLE_NAME} ON ${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID}"

        const val BASIC_JOIN_FIELDS =
            " 0 AS ${WarehouseMovementContentEntry.CONTENT_STATUS_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} AS ${WarehouseMovementContentEntry.DESCRIPTION}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} AS ${WarehouseMovementContentEntry.CODE}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.STATUS} AS ${WarehouseMovementContentEntry.ASSET_STATUS_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.PARENT_ID} AS ${WarehouseMovementContentEntry.PARENT_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} AS ${WarehouseMovementContentEntry.WAREHOUSE_AREA_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.LABEL_NUMBER} AS ${WarehouseMovementContentEntry.LABEL_NUMBER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID} AS ${WarehouseMovementContentEntry.ITEM_CATEGORY_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.OWNERSHIP_STATUS} AS ${WarehouseMovementContentEntry.OWNERSHIP_STATUS_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.MANUFACTURER} AS ${WarehouseMovementContentEntry.MANUFACTURER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.MODEL} AS ${WarehouseMovementContentEntry.MODEL}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.SERIAL_NUMBER} AS ${WarehouseMovementContentEntry.SERIAL_NUMBER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.EAN} AS ${WarehouseMovementContentEntry.EAN}," +
                    "${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.DESCRIPTION} AS ${WarehouseMovementContentEntry.ITEM_CATEGORY_STR}," +
                    "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${WarehouseMovementContentEntry.WAREHOUSE_AREA_STR}," +
                    "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${WarehouseMovementContentEntry.WAREHOUSE_STR}"
    }
}