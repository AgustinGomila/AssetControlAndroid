package com.dacosys.assetControl.ui.fragments.settings

import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dacosys.assetControl.R

class ProgressViewLayout(context: Context) : ConstraintLayout(context) {
    private var syncText: TextView
    private var progressText: TextView
    private var progressBar: ProgressBar

    fun setText(text: String) {
        syncText.text = text
    }

    fun setProgress(progress: Int) {
        val value = "${progress}%"
        progressBar.progress = progress
        progressText.text = value
    }

    init {
        // Infla el layout personalizado
        val view = LayoutInflater.from(context).inflate(R.layout.progress_view, this, true)

        // Obtiene las vistas que necesitas actualizar
        syncText = view.findViewById(R.id.syncStatusTextView)
        progressText = view.findViewById(R.id.syncPercentTextView)
        progressBar = view.findViewById(R.id.progressBar)
    }
}