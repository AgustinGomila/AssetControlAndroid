package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetReviewContentRepository {
    private val dao: com.dacosys.assetControl.data.room.dao.review.AssetReviewContentDao by lazy {
        database.assetReviewContentDao()
    }

    fun getAllAssetReviewContents() = dao.getAllAssetReviewContents()

    fun getById(id: Long) = dao.getById(id)

    suspend fun insertAssetReviewContent(assetReviewContent: AssetReviewContent) {
        withContext(Dispatchers.IO) {
            dao.insertAssetReviewContent(assetReviewContent)
        }
    }

    suspend fun insertAll(assetReviewContents: List<AssetReviewContent>) {
        withContext(Dispatchers.IO) {
            dao.insertAll(assetReviewContents)
        }
    }

    suspend fun updateAssetReviewContent(assetReviewContent: AssetReviewContent) {
        withContext(Dispatchers.IO) {
            dao.updateAssetReviewContent(assetReviewContent)
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
