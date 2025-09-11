package com.example.assetControl.data.room.repository.fragment

import com.example.assetControl.data.room.dao.fragment.FragmentDataDao
import com.example.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.example.assetControl.data.room.entity.fragment.FragmentDataEntity
import kotlinx.coroutines.runBlocking

class FragmentDataRepository {
    private val dao: FragmentDataDao
        get() = database.fragmentDataDao()

    fun select() = runBlocking { dao.select() }

    fun insert(fragments: List<FragmentDataEntity>) = runBlocking {
        dao.insert(fragments)
    }
}