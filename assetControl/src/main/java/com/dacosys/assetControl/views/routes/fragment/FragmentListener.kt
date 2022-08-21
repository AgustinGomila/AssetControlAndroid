package com.dacosys.assetControl.views.routes.fragment

interface DccFragmentListener {
    // Define data you like to return from AysncTask
    fun onFragmentStarted()
    fun onFragmentDestroy()
    fun onFragmentOk()
}

interface OnEnabledChangeListener {
    // Define data you like to return from AysncTask
    fun onEnabledChange(isEnabled: Boolean)
}