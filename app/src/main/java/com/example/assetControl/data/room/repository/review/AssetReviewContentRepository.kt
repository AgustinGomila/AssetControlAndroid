package com.example.assetControl.data.room.repository.review

import com.example.assetControl.AssetControlApp.Companion.context
import com.example.assetControl.R
import com.example.assetControl.data.enums.common.SaveProgress
import com.example.assetControl.data.room.dao.review.AssetReviewContentDao
import com.example.assetControl.data.room.database.AcDatabase.Companion.database
import com.example.assetControl.data.room.dto.review.AssetReview
import com.example.assetControl.data.room.dto.review.AssetReviewContent
import com.example.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.runBlocking


class AssetReviewContentRepository {
    private val dao: AssetReviewContentDao
        get() = database.assetReviewContentDao()

    fun select() = runBlocking { dao.select() }

    fun selectByAssetReviewId(id: Long) = runBlocking { dao.selectByAssetReviewId(id) }

    val maxId get() = runBlocking { dao.selectMaxId() ?: -1 }

    val nextId = maxId + 1


    fun insert(review: AssetReview, contents: List<AssetReviewContent>, progress: (SaveProgress) -> Unit) =
        insert(review.id, contents, progress)

    fun insert(id: Long, contents: List<AssetReviewContent>, progress: (SaveProgress) -> Unit) {
        runBlocking {
            // Set new ID
            contents.forEach { it.assetReviewId = id }

            val total = contents.size
            dao.insert(contents) {
                val asset = contents[it - 1]
                progress.invoke(
                    SaveProgress(
                        msg = String.format(
                            context.getString(R.string.adding_asset_),
                            asset.code
                        ),
                        taskStatus = ProgressStatus.running.id,
                        progress = it,
                        total = total
                    )
                )
            }
        }
    }


    fun updateAssetId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateAssetId(newValue, oldValue)
    }

    fun updateAssetReviewId(newValue: Long, oldValue: Long) {
        runBlocking {
            dao.updateAssetReviewId(newValue, oldValue)
        }
    }


    fun deleteByAssetReviewId(id: Long) = runBlocking {
        dao.deleteByAssetReviewId(id)
    }

    fun deleteTransferred() = runBlocking {
        dao.deleteTransferred()
    }
}