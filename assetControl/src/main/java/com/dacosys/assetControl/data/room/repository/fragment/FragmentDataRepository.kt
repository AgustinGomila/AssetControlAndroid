package com.dacosys.assetControl.data.room.repository.fragment

import com.dacosys.assetControl.data.room.dao.fragment.FragmentDataDao
import com.dacosys.assetControl.data.room.database.AcTempDatabase.Companion.database
import com.dacosys.assetControl.data.room.entity.fragment.FragmentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FragmentDataRepository {
    private val dao: FragmentDataDao by lazy {
        database.fragmentDataDao()
    }

    suspend fun insert(fragmentData: FragmentData) {
        withContext(Dispatchers.IO) {
            dao.insert(fragmentData)
        }
    }

    suspend fun delete(fragmentData: FragmentData) {
        withContext(Dispatchers.IO) {
            dao.delete(fragmentData)
        }
    }

}