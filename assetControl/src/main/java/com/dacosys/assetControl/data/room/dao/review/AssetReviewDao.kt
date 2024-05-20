package com.dacosys.assetControl.data.room.dao.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.data.room.entity.review.AssetReview.Entry
import com.dacosys.assetControl.data.room.entity.user.User
import com.dacosys.assetControl.data.room.entity.user.UserWarehouseArea
import java.util.*

@Dao
interface AssetReviewDao {

    @Query("$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN WHERE ${Entry.TABLE_NAME}.${Entry.ID} = :id")
    suspend fun selectById(id: Long): AssetReview?

    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByDescription(wDescription: String, waDescription: String, userId: Long): List<AssetReview>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} LIKE '%' || :wDescription || '%'  " +
                "AND ${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} LIKE '%' || :waDescription || '%'  " +
                "AND ${wEntry.TABLE_NAME}.${wEntry.ACTIVE} == 1 " +
                "AND ${waEntry.TABLE_NAME}.${waEntry.ACTIVE} == 1 " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByDescriptionActive(wDescription: String, waDescription: String, userId: Long): List<AssetReview>

    @Query(
        "$BASIC_SELECT, $BASIC_JOIN_FIELDS $BASIC_FROM $BASIC_LEFT_JOIN " +
                "WHERE ${Entry.TABLE_NAME}.${Entry.STATUS_ID} = :statusId " +
                "AND $USER_WA_FILTER" +
                BASIC_ORDER
    )
    suspend fun selectByCompleted(statusId: Int = AssetReviewStatus.completed.id, userId: Long): List<AssetReview>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assetReview: AssetReview)


    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_AREA_ID} = :newValue WHERE ${Entry.WAREHOUSE_AREA_ID} = :oldValue")
    suspend fun updateWarehouseAreaId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.WAREHOUSE_ID} = :newValue WHERE ${Entry.WAREHOUSE_ID} = :oldValue")
    suspend fun updateWarehouseId(oldValue: Long, newValue: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.ID} = :newValue, ${Entry.MODIFICATION_DATE} = :date, ${Entry.STATUS_ID} = :status WHERE ${Entry.ID} = :oldValue")
    suspend fun updateId(
        oldValue: Long,
        newValue: Long,
        date: Date = Date(),
        status: Int = AssetReviewStatus.transferred.id
    )


    @Query("DELETE $BASIC_FROM WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE $BASIC_FROM WHERE ${Entry.STATUS_ID} = :status")
    suspend fun deleteTransferred(status: Int = AssetReviewStatus.transferred.id)

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.ID}"

        private val uEntry = User.Entry
        private val wEntry = Warehouse.Entry
        private val waEntry = WarehouseArea.Entry
        private val uwaEntry = UserWarehouseArea.Entry

        const val BASIC_LEFT_JOIN =
            "LEFT JOIN ${wEntry.TABLE_NAME} ON ${wEntry.TABLE_NAME}.${wEntry.ID} = ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_ID} " +
                    "LEFT JOIN ${waEntry.TABLE_NAME} ON ${waEntry.TABLE_NAME}.${waEntry.ID} = ${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} " +
                    "LEFT JOIN ${uEntry.TABLE_NAME} ON ${uEntry.TABLE_NAME}.${uEntry.ID} = ${Entry.TABLE_NAME}.${Entry.USER_ID} "

        const val USER_WA_FILTER =
            "${Entry.TABLE_NAME}.${Entry.WAREHOUSE_AREA_ID} " +
                    "IN (SELECT ${uwaEntry.TABLE_NAME}.${uwaEntry.WAREHOUSE_AREA_ID} " +
                    "FROM ${uwaEntry.TABLE_NAME} " +
                    "WHERE ( ${uwaEntry.TABLE_NAME}.${uwaEntry.USER_ID} = :userId " +
                    "AND ${uwaEntry.TABLE_NAME}.${uwaEntry.SEE} = 1 ))"

        const val BASIC_JOIN_FIELDS =
            "${waEntry.TABLE_NAME}.${waEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_STR}," +
                    "${wEntry.TABLE_NAME}.${wEntry.DESCRIPTION} AS ${Entry.WAREHOUSE_AREA_STR}, " +
                    "${uEntry.TABLE_NAME}.${uEntry.NAME} AS ${Entry.USER_STR}"
    }
}