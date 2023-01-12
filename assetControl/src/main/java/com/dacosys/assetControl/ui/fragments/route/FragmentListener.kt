package com.dacosys.assetControl.ui.fragments.route

interface DccFragmentListener {
    fun onFragmentStarted()
    fun onFragmentDestroy()
    fun onFragmentOk()
}

interface OnEnabledChangeListener {
    fun onEnabledChange(isEnabled: Boolean)
}