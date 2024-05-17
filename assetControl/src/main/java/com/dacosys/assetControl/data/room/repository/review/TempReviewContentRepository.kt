package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.data.room.dao.review.TempReviewContentDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.data.room.entity.review.TempReviewContent
import kotlinx.coroutines.runBlocking

class TempReviewContentRepository {
    private val dao: TempReviewContentDao
        get() = database.tempReviewContentDao()

    private val nextId: Long
        get() = (dao.selectMaxId() ?: 0) + 1

    fun selectByTempId(arId: Long): List<AssetReviewContent> {
        val tempContent = dao.selectByTempIds(arId)

        val content: ArrayList<AssetReviewContent> = arrayListOf()
        tempContent.mapTo(content) { AssetReviewContent(it) }

        return content
    }


    fun insert(content: TempReviewContent) = runBlocking {
        dao.insert(content)
    }

    fun insert(arId: Long, contents: List<AssetReviewContent>) = runBlocking {
        contents.forEach { it.assetReviewId = arId }

        val tempContents: ArrayList<TempReviewContent> = arrayListOf()
        contents.mapTo(tempContents) { TempReviewContent(it) }

        tempContents.forEach {
            it.assetReviewContentId = nextId
            dao.insert(it)
        }
    }

    fun deleteAll() = runBlocking {
        dao.deleteAll()
    }
}
