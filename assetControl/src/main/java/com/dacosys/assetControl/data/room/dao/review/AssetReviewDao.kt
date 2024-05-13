package com.dacosys.assetControl.data.room.dao.review

import androidx.room.*
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.data.room.entity.review.AssetReview.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetReviewDao {
    @Query("SELECT * FROM ${Entry.TABLE_NAME}")
    fun getAllAssetReviews(): Flow<List<AssetReview>>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    fun getById(id: Long): Flow<AssetReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetReview(assetReview: AssetReview)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assetReviews: List<AssetReview>)

    @Update
    suspend fun updateAssetReview(assetReview: AssetReview)

    @Query("DELETE FROM ${Entry.TABLE_NAME} WHERE ${Entry.ID} = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ${Entry.TABLE_NAME}")
    suspend fun deleteAll()
}
