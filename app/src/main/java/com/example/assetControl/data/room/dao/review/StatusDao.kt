package com.example.assetControl.data.room.dao.review

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.assetControl.data.room.dto.review.AssetReviewStatusEntry
import com.example.assetControl.data.room.entity.review.AssetReviewStatusEntity

@Dao
interface StatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statuses: List<AssetReviewStatusEntity>)


    @Query("DELETE $BASIC_FROM")
    suspend fun deleteAll()

    companion object {
        const val BASIC_FROM = "FROM ${AssetReviewStatusEntry.TABLE_NAME}"
    }
}
