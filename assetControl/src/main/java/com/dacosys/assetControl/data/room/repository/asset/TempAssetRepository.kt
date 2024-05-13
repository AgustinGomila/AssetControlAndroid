package com.dacosys.assetControl.data.room.repository.asset

import com.dacosys.assetControl.data.room.dao.asset.TempAssetDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.asset.TempAsset

class TempAssetRepository {
    private val dao: TempAssetDao by lazy {
        database.tempAssetDao()
    }

    suspend fun insert(tempAsset: TempAsset) {
        dao.insert(tempAsset)
    }

    suspend fun delete(tempAsset: TempAsset) {
        dao.delete(tempAsset)
    }

}