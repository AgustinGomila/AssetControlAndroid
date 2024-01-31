package com.dacosys.assetControl.data.model.location.async

import com.dacosys.assetControl.data.model.location.Warehouse
import com.dacosys.assetControl.data.model.location.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}