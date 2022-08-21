package com.dacosys.assetControl.views.commons.snackbar

import android.R
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.dacosys.assetControl.utils.Statics
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

class MakeText : AppCompatActivity() {
    companion object {
        fun makeText(activity: AppCompatActivity, text: String, snackbarType: SnackbarType) {
            makeText(
                WeakReference(activity.window.decorView.findViewById(R.id.content)),
                text,
                snackbarType
            )
        }

        fun makeText(v: View, text: String, snackbarType: SnackbarType) {
            makeText(WeakReference(v), text, snackbarType)
        }

        private fun makeText(v: WeakReference<View>, text: String, snackbarType: SnackbarType) {
            if (snackbarType == SnackbarType.ERROR) {
                Log.e(Statics.AssetControl.getContext().toString(), text)
            }

            val snackbar = Snackbar.make(v.get() ?: return, text, snackbarType.duration)
            val snackbarView = snackbar.view

            val params = snackbar.view.layoutParams
            if (params is CoordinatorLayout.LayoutParams) {
                params.gravity = Gravity.CENTER
            } else {
                (params as FrameLayout.LayoutParams).gravity = Gravity.CENTER
            }
            snackbar.view.layoutParams = params

            snackbarView.background = snackbarType.backColor
            snackbarView.elevation = 6f

            snackbar.animationMode = ANIMATION_MODE_FADE
            snackbar.setTextColor(snackbarType.foreColor)

            val textView =
                snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            textView.maxLines = 4 // show multiple line

            snackbar.show()
        }
    }
}