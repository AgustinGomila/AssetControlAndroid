package com.example.assetControl.data.room.repository.review

import com.example.assetControl.data.enums.review.AssetReviewStatus
import com.example.assetControl.data.room.dao.review.StatusDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.entity.review.AssetReviewStatusEntity
import kotlinx.coroutines.runBlocking

class AssetReviewStatusRepository {
    private val dao: StatusDao
        get() = database.statusDao()

    fun sync() = runBlocking {
        val status = AssetReviewStatus.getAll().map { AssetReviewStatusEntity(it) }.toList()
        dao.deleteAll()
        dao.insert(status)
    }
}