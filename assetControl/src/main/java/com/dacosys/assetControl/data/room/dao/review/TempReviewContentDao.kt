package com.dacosys.assetControl.data.room.dao.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dacosys.assetControl.data.room.entity.review.TempReviewContentEntity
import com.dacosys.assetControl.data.room.entity.review.TempReviewContentEntity.Entry

@Dao
interface TempReviewContentDao {
    @Query("SELECT MAX(${Entry.ID}) $BASIC_FROM")
    suspend fun selectMaxId(): Long?

    @Query("$BASIC_SELECT $BASIC_FROM WHERE ${Entry.TABLE_NAME}.${Entry.ASSET_REVIEW_ID} = :arId $BASIC_ORDER")
    suspend fun selectByTempIds(arId: Long): List<TempReviewContentEntity>


    @Insert
    suspend fun insert(content: TempReviewContentEntity)

    @Insert
    suspend fun insert(contents: List<TempReviewContentEntity>)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()

    companion object {
        const val BASIC_SELECT = "SELECT ${Entry.TABLE_NAME}.*"
        const val BASIC_FROM = "FROM ${Entry.TABLE_NAME}"
        const val BASIC_ORDER = "ORDER BY ${Entry.TABLE_NAME}.${Entry.DESCRIPTION}, " +
                "${Entry.TABLE_NAME}.${Entry.CODE}, " +
                "${Entry.TABLE_NAME}.${Entry.ASSET_ID}"
    }
}
