package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.data.room.dao.review.AssetReviewDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetReviewRepository {
    private val dao: AssetReviewDao by lazy {
        database.assetReviewDao()
    }

    fun getAllAssetReviews() = dao.getAllAssetReviews()

    fun getById(id: Long) = dao.getById(id)

    suspend fun insertAssetReview(assetReview: AssetReview) {
        withContext(Dispatchers.IO) {
            dao.insertAssetReview(assetReview)
        }
    }

    suspend fun insertAll(assetReviews: List<AssetReview>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(assetReviews)
        }
    }

    suspend fun updateAssetReview(assetReview: AssetReview) {
        withContext(Dispatchers.IO) {
            dao.updateAssetReview(assetReview)
        }
    }

    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}
