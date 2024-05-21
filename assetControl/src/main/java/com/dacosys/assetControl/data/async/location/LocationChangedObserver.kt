package com.dacosys.assetControl.data.async.location

import com.dacosys.assetControl.data.room.dto.location.Warehouse
import com.dacosys.assetControl.data.room.dto.location.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}