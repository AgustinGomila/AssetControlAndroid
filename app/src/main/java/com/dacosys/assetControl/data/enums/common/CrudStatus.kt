package com.dacosys.assetControl.data.enums.common

enum class CrudStatus {
    UPDATE_OK,
    ERROR_UPDATE,
    ERROR_OBJECT_NULL,
    INSERT_OK,
    ERROR_INSERT,
}

data class CrudResult<T>(var status: CrudStatus, var itemResult: T)

interface CrudCompleted {
    fun <T> onCompleted(result: CrudResult<T?>)
}