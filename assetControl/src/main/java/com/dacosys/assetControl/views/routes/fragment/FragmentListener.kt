package com.dacosys.assetControl.views.routes.fragment

interface DccFragmentListener {
    fun onFragmentStarted()
    fun onFragmentDestroy()
    fun onFragmentOk()
}

interface OnEnabledChangeListener {
    fun onEnabledChange(isEnabled: Boolean)
}