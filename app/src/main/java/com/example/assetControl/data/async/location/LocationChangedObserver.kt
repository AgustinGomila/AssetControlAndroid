package com.example.assetControl.data.async.location

import com.example.assetControl.data.room.dto.location.Warehouse
import com.example.assetControl.data.room.dto.location.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}