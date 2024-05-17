package com.dacosys.assetControl.data.room.repository.review

import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.enums.common.SaveProgress
import com.dacosys.assetControl.data.room.dao.review.AssetReviewContentDao
import com.dacosys.assetControl.data.room.database.AcDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.review.AssetReview
import com.dacosys.assetControl.data.room.entity.review.AssetReviewContent
import com.dacosys.assetControl.network.utils.ProgressStatus
import kotlinx.coroutines.runBlocking


class AssetReviewContentRepository {
    private val dao: AssetReviewContentDao
        get() = database.assetReviewContentDao()

    fun select() = dao.select()

    fun selectByAssetReviewId(id: Long) = dao.selectByAssetReviewId(id)

    val minId get() = dao.selectMinId() ?: -1


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
                            getContext().getString(R.string.adding_asset_),
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

    fun insert(content: AssetReviewContent) = runBlocking {
        dao.insert(content)
    }

    fun insert(contents: List<AssetReviewContent>) = runBlocking {
        dao.insert(contents)
    }

    fun update(content: AssetReviewContent): Boolean {
        val r = runBlocking {
            dao.update(content)
            true
        }
        return r
    }

    fun updateAssetId(newValue: Long, oldValue: Long) = runBlocking {
        dao.updateAssetId(newValue, oldValue)
    }

    fun deleteByAssetReviewId(id: Long) = runBlocking {
        dao.deleteByAssetReviewId(id)
    }

    fun deleteTransferred() = runBlocking {
        dao.deleteTransferred()
    }
}