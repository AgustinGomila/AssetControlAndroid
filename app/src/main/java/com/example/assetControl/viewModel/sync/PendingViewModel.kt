package com.example.assetControl.viewModel.sync

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assetControl.network.sync.GetPending
import kotlinx.coroutines.launch

class PendingViewModel : ViewModel() {
    val pendingLiveData: MutableLiveData<ArrayList<Any>> = MutableLiveData()

    fun refreshPending() {
        viewModelScope.launch {
            GetPending { setPending(it) }
        }
    }

    @Suppress("unused")
    fun getPending(): ArrayList<Any> {
        return pendingLiveData.value ?: ArrayList()
    }

    fun setPending(it: ArrayList<Any>) {
        pendingLiveData.postValue(it)
    }
}