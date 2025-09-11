package com.example.assetControl.data.room.dao.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.assetControl.data.enums.review.AssetReviewStatus
import com.example.assetControl.data.room.dto.location.WarehouseAreaEntry
import com.example.assetControl.data.room.dto.location.WarehouseEntry
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewEntry
import com.example.assetControl.data.room.dto.user.UserEntry
import com.example.assetControl.data.room.dto.user.UserWarehouseAreaEntry
import com.example.assetControl.data.room.entity.review.AssetReviewEntity
import java.util.*

@Dao
interface AssetReviewDao {
    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN WHERE ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.ID} = :id")
    suspend fun selectById(id: Long): AssetReview?

    @Query("SELECT MAX(${AssetReviewEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByDescription(wDescription: String, waDescription: String, userId: Long): List<AssetReview>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ACTIVE} == 1 " +
                "AND ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ACTIVE} == 1 " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByDescriptionActive(wDescription: String, waDescription: String, userId: Long): List<AssetReview>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.STATUS_ID} = :statusId " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByCompleted(statusId: Int = AssetReviewStatus.completed.id, userId: Long): List<AssetReview>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assetReview: AssetReviewEntity): Long


    @Update
    suspend fun update(review: AssetReviewEntity)

    @Query("UPDATE ${AssetReviewEntry.TABLE_NAME} SET ${AssetReviewEntry.WAREHOUSE_AREA_ID} = :newValue WHERE ${AssetReviewEntry.WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateWarehouseAreaId(newValue: Long, oldValue: Long)

    @Query("UPDATE ${AssetReviewEntry.TABLE_NAME} SET ${AssetReviewEntry.WAREHOUSE_ID} = :newValue WHERE ${AssetReviewEntry.WAREHOUSE_ID} = :oldValue")
    suspend fun updateWarehouseId(newValue: Long, oldValue: Long)

    @Query(
        "UPDATE ${AssetReviewEntry.TABLE_NAME} SET ${AssetReviewEntry.ID} = :newValue, " +
                "${AssetReviewEntry.MODIFICATION_DATE} = :date, " +
                "${AssetReviewEntry.STATUS_ID} = :status " +
                "WHERE ${AssetReviewEntry.ID} = :oldValue"
    )
    suspend fun updateId(newValue: Long, oldValue: Long, date: Date, status: Int = AssetReviewStatus.transferred.id)


    @Query("DELETE $BASIC_FROM WHERE ${AssetReviewEntry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${AssetReviewEntry.STATUS_ID} = :status")
    suspend fun deleteTransferred(status: Int = AssetReviewStatus.transferred.id)

    companion object {
        const val BASIC_SELECT = "SELECT ${AssetReviewEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${AssetReviewEntry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.ID}"

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${WarehouseEntry.TABLE_NAME} ON ${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.ID} = ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${WarehouseAreaEntry.TABLE_NAME} ON ${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.ID} = ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${UserEntry.TABLE_NAME} ON ${UserEntry.TABLE_NAME}.${UserEntry.ID} = ${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.USER_ID} "

        const val USER_WA_FILTER =
            "${AssetReviewEntry.TABLE_NAME}.${AssetReviewEntry.WAREHOUSE_AREA_ID} " +
                    "IN (SELECT ${UserWarehouseAreaEntry.TABLE_NAME}.${UserWarehouseAreaEntry.WAREHOUSE_AREA_ID} " +
                    "FROM ${UserWarehouseAreaEntry.TABLE_NAME} " +
                    "WHERE ( ${UserWarehouseAreaEntry.TABLE_NAME}.${UserWarehouseAreaEntry.USER_ID} = :userId " +
                    "AND ${UserWarehouseAreaEntry.TABLE_NAME}.${UserWarehouseAreaEntry.SEE} = 1 ))"

        const val BASIC_JOIN_FIELDS =
            "${WarehouseAreaEntry.TABLE_NAME}.${WarehouseAreaEntry.DESCRIPTION} AS ${AssetReviewEntry.WAREHOUSE_STR}," +
                    "${WarehouseEntry.TABLE_NAME}.${WarehouseEntry.DESCRIPTION} AS ${AssetReviewEntry.WAREHOUSE_AREA_STR}, " +
                    "${UserEntry.TABLE_NAME}.${UserEntry.NAME} AS ${AssetReviewEntry.USER_STR}"
    }
}