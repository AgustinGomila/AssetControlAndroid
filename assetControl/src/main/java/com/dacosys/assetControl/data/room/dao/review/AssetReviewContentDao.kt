package com.dacosys.assetControl.data.room.dao.review

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetReviewContentDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAssetReviewContents(): Flow<List<AssetReviewContent>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getById(id: Long): Flow<AssetReviewContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetReviewContent(assetReviewContent: AssetReviewContent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assetReviewContents: List<AssetReviewContent>)

    @Update
    suspend fun updateAssetReviewContent(assetReviewContent: AssetReviewContent)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
