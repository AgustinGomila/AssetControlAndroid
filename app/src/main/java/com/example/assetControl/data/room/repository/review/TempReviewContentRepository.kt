package com.example.assetControl.data.room.repository.review

import com.example.assetControl.data.room.dao.review.TempReviewContentDao
import com.example.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.data.room.entity.review.TempReviewContentEntity
import kotlinx.coroutines.runBlocking

class TempReviewContentRepository {
    private val dao: TempReviewContentDao
        get() = database.tempReviewContentDao()

    private val nextId: Long
        get() = runBlocking { (dao.selectMaxId() ?: 0) + 1 }

    fun selectByTempId(arId: Long): List<AssetReviewContent> = runBlocking {
        val tempContent = dao.selectByTempIds(arId)

        val content: ArrayList<AssetReviewContent> = arrayListOf()
        tempContent.mapTo(content) { AssetReviewContent(it) }

        content
    }


    fun insert(content: TempReviewContentEntity) = runBlocking {
        dao.insert(content)
    }

    fun insert(arId: Long, contents: List<AssetReviewContent>) = runBlocking {
        contents.forEach { it.assetReviewId = arId }

        val tempContents: ArrayList<TempReviewContentEntity> = arrayListOf()
        contents.mapTo(tempContents) { TempReviewContentEntity(it) }

        tempContents.forEach {
            it.id = nextId
            dao.insert(it)
        }
    }

    fun deleteAll() = runBlocking {
        dao.deleteAll()
    }
}
