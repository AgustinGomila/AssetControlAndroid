package com.example.assetControl.utils.conversor

object IntConversor {
    fun Int?.orZero(): Int {
        return this ?: 0
    }
}