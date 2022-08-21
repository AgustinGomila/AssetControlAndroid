package com.dacosys.assetControl.model.locations.async

import com.dacosys.assetControl.model.locations.warehouse.`object`.Warehouse
import com.dacosys.assetControl.model.locations.warehouseArea.`object`.WarehouseArea

interface WarehouseChangedObserver {
    fun onWarehouseChanged(w: Warehouse?)
}

interface WarehouseAreaChangedObserver {
    fun onWarehouseAreaChanged(w: WarehouseArea?)
}