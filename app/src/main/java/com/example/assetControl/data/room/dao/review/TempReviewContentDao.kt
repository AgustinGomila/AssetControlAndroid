package com.example.assetControl.data.room.dao.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.assetControl.data.room.entity.review.TempReviewContentEntity
import com.example.assetControl.data.room.entity.review.TempReviewContentEntry

@Dao
interface TempReviewContentDao {
    @Query("SELECT MAX(${TempReviewContentEntry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${TempReviewContentEntry.TABLE_NAME}.${TempReviewContentEntry.ASSET_REVIEW_ID} = :arId $BASIC_ORDER")
    suspend fun selectByTempIds(arId: Long): List<TempReviewContentEntity>


    @Insert
    suspend fun insert(content: TempReviewContentEntity)

    @Insert
    suspend fun insert(contents: List<TempReviewContentEntity>)

    @Query("DELETE FROM ${TempReviewContentEntry.TABLE_NAME}")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${TempReviewContentEntry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${TempReviewContentEntry.TABLE_NAME}"
        const val BASIC_ORDER =
            "ORDER BY ${TempReviewContentEntry.TABLE_NAME}.${TempReviewContentEntry.DESCRIPTION}, " +
                    "${TempReviewContentEntry.TABLE_NAME}.${TempReviewContentEntry.CODE}, " +
                    "${TempReviewContentEntry.TABLE_NAME}.${TempReviewContentEntry.ASSET_ID}"
    }
}
