package com.example.assetControl.data.room.repository.asset

import com.example.assetControl.data.room.dao.asset.TempAssetDao
import com.example.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.example.assetControl.data.room.entity.asset.TempAssetEntity
import kotlinx.coroutines.runBlocking

class TempAssetRepository {
    private val dao: TempAssetDao
        get() = database.tempAssetDao()

    fun select() = runBlocking { dao.select() }

    fun insert(assets: List<TempAssetEntity>) = runBlocking {
        dao.insert(assets)
    }

    fun deleteAll() = runBlocking {
        dao.deleteAll()
    }
}