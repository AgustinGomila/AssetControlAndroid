package com.dacosys.assetControl.data.room.dao.review

import androidx.room.*
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.entity.asset.Asset
import com.dacosys.assetControl.data.room.entity.category.ItemCategory
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent.Entry


@Dao
interface AssetReviewContentDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN $BASIC_ORDER")
    fun select(): List<AssetReviewContent>

    @Query("SELECT MIN(${Entry.ID}) $BASIC_FROM")
    fun selectMinId(): Long?

    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    fun selectById(id: Long): AssetReviewContent?

    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN WHERE ${Entry.TABLE_NAME}.${Entry.ASSET_REVIEW_ID} = :id")
    fun selectByAssetReviewId(id: Long): List<AssetReviewContent>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: AssetReviewContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contents: List<AssetReviewContent>)

    @Transaction
    suspend fun insert(entities: List<AssetReviewContent>, completedTask: (Int) -> Unit) {
        entities.forEachIndexed { index, entity ->
            insert(entity)
            completedTask(index + 1)
        }
    }


    @Update
    suspend fun update(content: AssetReviewContent)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ASSET_ID} = :newValue WHERE ${Entry.ASSET_ID} = :oldValue")
    suspend fun updateAssetId(oldValue: Long, newValue: Long)


    @Delete
    suspend fun delete(content: AssetReviewContent)

    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    @Query("DELETE $BASIC_FROM WHERE ${Entry.ASSET_REVIEW_ID} = :id")
    suspend fun deleteByAssetReviewId(id: Long)

    @Query(
        "DELETE $BASIC_FROM WHERE ${Entry.ASSET_REVIEW_ID} " +
                "IN (SELECT ${arEntry.TABLE_NAME}.${arEntry.ID} " +
                "FROM ${arEntry.TABLE_NAME} " +
                "WHERE ${arEntry.TABLE_NAME}.${arEntry.STATUS_ID} = :status )"
    )
    suspend fun deleteTransferred(status: Int = AssetReviewStatus.transferred.id)


    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ASSET_REVIEW_ID}, " +
                "${Entry.TABLE_NAME}.${Entry.DESCRIPTION}, " +
                "${Entry.TABLE_NAME}.${Entry.CODE}"

        private val arEntry = AssetReview.Entry
        private val aEntry = Asset.Entry
        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry
        private val icEntry = ItemCategory.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${aEntry.TABLE_NAME} ON ${aEntry.TABLE_NAME}.${aEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ASSET_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} ON ${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.ORIGIN_WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${waEntry.TABLE_NAME}.${waEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${icEntry.TABLE_NAME} ON ${icEntry.TABLE_NAME}.${icEntry.ID} = ${aEntry.TABLE_NAME}.${aEntry.ITEM_CATEGORY_ID}"

        const val BASIC_JOIN_FIELDS = "${aEntry.TABLE_NAME}.${aEntry.DESCRIPTION} AS ${Entry.DESCRIPTION}," +
                "${aEntry.TABLE_NAME}.${aEntry.CODE} AS ${Entry.CODE}," +
                "${aEntry.TABLE_NAME}.${aEntry.PARENT_ID} AS ${Entry.PARENT_ID}," +
                "${aEntry.TABLE_NAME}.${aEntry.WAREHOUSE_AREA_ID} AS ${Entry.WAREHOUSE_AREA_ID}," +
                "${aEntry.TABLE_NAME}.${aEntry.LABEL_NUMBER} AS ${Entry.LABEL_NUMBER}," +
                "${aEntry.TABLE_NAME}.${aEntry.ITEM_CATEGORY_ID} AS ${Entry.ITEM_CATEGORY_ID}," +
                "${aEntry.TABLE_NAME}.${aEntry.OWNERSHIP_STATUS} AS ${Entry.OWNERSHIP_STATUS_ID}," +
                "${aEntry.TABLE_NAME}.${aEntry.MANUFACTURER} AS ${Entry.MANUFACTURER}," +
                "${aEntry.TABLE_NAME}.${aEntry.MODEL} AS ${Entry.MODEL}," +
                "${aEntry.TABLE_NAME}.${aEntry.SERIAL_NUMBER} AS ${Entry.SERIAL_NUMBER}," +
                "${aEntry.TABLE_NAME}.${aEntry.EAN} AS ${Entry.EAN}," +
                "${icEntry.TABLE_NAME}.${icEntry.DESCRIPTION} AS ${Entry.ITEM_CATEGORY_STR}," +
                "${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_AREA_STR}," +
                "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}"
    }
}