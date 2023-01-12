package com.dacosys.assetControl.model.location.async

import com.dacosys.assetControl.model.location.Warehouse
import com.dacosys.assetControl.model.location.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}