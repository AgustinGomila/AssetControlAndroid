package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.data.enums.review.AssetReviewStatus
import com.dacosys.assetControl.data.room.dao.review.StatusDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import kotlinx.coroutines.runBlocking
import com.dacosys.assetControl.data.room.entity.review.AssetReviewStatus as AssetReviewStatusRoom

class AssetReviewStatusRepository {
    private val dao: StatusDao
        get() = database.statusDao()

    fun sync() = runBlocking {
        val status = AssetReviewStatus.getAll().map { AssetReviewStatusRoom(it) }.toList()
        dao.deleteAll()
        dao.insert(status)
    }
}