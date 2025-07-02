package com.dacosys.assetControl.data.room.dao.review

import androidx.room.*
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.dto.asset.AssetEntry
import com.dacosys.assetControl.data.room.dto.category.ItemCategoryEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.dacosys.assetControl.data.room.dto.location.WarehouseEntry
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContent
import com.dacosys.assetControl.data.room.dto.review.AssetReviewContentEntry
import com.dacosys.assetControl.data.room.dto.review.AssetReviewEntry
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContentEntity

@Dao
interface AssetReviewContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    suspend fun select(): List<AssetReviewContent>

    @Query("SELECT MAX(${AssetReviewContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.ID} = :id"
    )
    suspend fun selectById(id: Long): AssetReviewContent?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.ASSET_REVIEW_ID} = :id"
    )
    suspend fun selectByAssetReviewId(id: Long): List<AssetReviewContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: AssetReviewContentEntity): Long

    @Transaction
    suspend fun insert(entities: List<AssetReviewContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(AssetReviewContentEntity(entity))
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(content: AssetReviewContentEntity)

    @Query("UPDATE ${AssetReviewContentEntry.TABLE_NAME} SET ${AssetReviewContentEntry.ASSET_ID} = :newValue WHERE ${AssetReviewContentEntry.ASSET_ID} = :oldValue")
    suspend fun updateAssetId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetReviewContentEntry.TABLE_NAME} SET ${AssetReviewContentEntry.ASSET_REVIEW_ID} = :newValue " +
                "WHERE ${AssetReviewContentEntry.ASSET_REVIEW_ID} = :oldValue"
    )
    suspend fun updateAssetReviewId(newValue: Long, oldValue: Long)


    @Delete
    suspend fun delete(content: AssetReviewContentEntity)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${AssetReviewContentEntry.ASSET_REVIEW_ID} = :id")
    suspend fun deleteByAssetReviewId(id: Long)

    @Query(
        "DELETE $BASIC_FROM WHERE ${AssetReviewContentEntry.ASSET_REVIEW_ID} " +
                "IN (SELECT ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.ID} " +
                "FROM ${AssetReviewEntry.TABLE_NAME} " +
                "WHERE ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.STATUS_ID} = :status )"
    )
    suspend fun deleteTransferred(status: Int = AssetReviewStatus.transferred.id)


    companion object {
        const val BASIC_SELECT = "SELECT ${AssetReviewContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${AssetReviewContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.ASSET_REVIEW_ID}, " +
                    "${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.DESCRIPTION}, " +
                    "${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.CODE}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${AssetEntry.TABLE_NAME} ON ${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.ASSET_ID} = ${AssetEntry.TABLE_NAME}.${AssetEntry.ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${AssetReviewContentEntry.TABLE_NAME}.${AssetReviewContentEntry.ORIGIN_WAREHOUSE_AREA_ID} = ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} " +
                    "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.WAREHOUSE_ID} = ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} " +
                    "LEFT JOIN ${ItemCategoryEntry.TABLE_NAME} ON ${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID} = ${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.ID}"

        const val BASIC_JOIN_FIELDS =
            "${AssetEntry.TABLE_NAME}.${AssetEntry.DESCRIPTION} AS ${AssetReviewContentEntry.DESCRIPTION}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.CODE} AS ${AssetReviewContentEntry.CODE}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.STATUS} AS ${AssetReviewContentEntry.ASSET_STATUS_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.PARENT_ID} AS ${AssetReviewContentEntry.PARENT_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.WAREHOUSE_AREA_ID} AS ${AssetReviewContentEntry.WAREHOUSE_AREA_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.LABEL_NUMBER} AS ${AssetReviewContentEntry.LABEL_NUMBER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.ITEM_CATEGORY_ID} AS ${AssetReviewContentEntry.ITEM_CATEGORY_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.OWNERSHIP_STATUS} AS ${AssetReviewContentEntry.OWNERSHIP_STATUS_ID}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.MANUFACTURER} AS ${AssetReviewContentEntry.MANUFACTURER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.MODEL} AS ${AssetReviewContentEntry.MODEL}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.SERIAL_NUMBER} AS ${AssetReviewContentEntry.SERIAL_NUMBER}," +
                    "${AssetEntry.TABLE_NAME}.${AssetEntry.EAN} AS ${AssetReviewContentEntry.EAN}," +
                    "${ItemCategoryEntry.TABLE_NAME}.${ItemCategoryEntry.DESCRIPTION} AS ${AssetReviewContentEntry.ITEM_CATEGORY_STR}," +
                    "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${AssetReviewContentEntry.WAREHOUSE_AREA_STR}," +
                    "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${AssetReviewContentEntry.WAREHOUSE_STR}"
    }
}