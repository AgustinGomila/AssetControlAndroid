package com.dacosys.assetControl.data.async.location

import com.dacosys.assetControl.data.room.entity.location.Warehouse
import com.dacosys.assetControl.data.room.entity.location.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}