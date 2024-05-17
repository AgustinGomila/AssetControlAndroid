package com.dacosys.assetControl.data.room.repository.fragment

import com.dacosys.assetControl.data.room.dao.fragment.FragmentDataDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData
import kotlinx.coroutines.runBlocking

class FragmentDataRepository {
    private val dao: FragmentDataDao
        get() = database.fragmentDataDao()

    fun select() = dao.select()

    fun insert(fragments: List<FragmentData>) = runBlocking {
        dao.insert(fragments)
    }
}