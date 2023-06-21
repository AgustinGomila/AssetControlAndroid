package com.dacosys.assetControl.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.preferences.Preferences.Companion.prefsGetBoolean
import com.dacosys.assetControl.utils.settings.Preference
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import kotlin.math.min
import kotlin.math.roundToInt

class Screen {

    companion object {
        @SuppressLint("SourceLockedOrientationActivity")
        fun setScreenRotation(activity: AppCompatActivity) {
            val rotation: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = activity.display
                display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION") val display = activity.windowManager.defaultDisplay
                display.rotation
            }
            val height: Int
            val width: Int

            val displayMetrics = Resources.getSystem().displayMetrics
            height = displayMetrics.heightPixels
            width = displayMetrics.widthPixels

            if (prefsGetBoolean(Preference.allowScreenRotation)) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                when (rotation) {
                    Surface.ROTATION_90 -> when {
                        width > height -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                        else -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    }

                    Surface.ROTATION_180 -> when {
                        height > width -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT

                        else -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    }

                    Surface.ROTATION_270 -> when {
                        width > height -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE

                        else -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }

                    Surface.ROTATION_0 -> activity.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                    else -> when {
                        height > width -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                        else -> activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            }
        }

        fun getScreenWidth(activity: Activity): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                val insets: Insets =
                    windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && activity.resources.configuration.smallestScreenWidthDp < 600) { // landscape and phone
                    val navigationBarSize: Int = insets.right + insets.left
                    bounds.width() - navigationBarSize
                } else { // portrait or tablet
                    bounds.width()
                }
            } else {
                val outMetrics = DisplayMetrics()
                @Suppress("DEPRECATION") activity.windowManager.defaultDisplay.getMetrics(outMetrics)
                outMetrics.widthPixels
            }
        }

        fun getScreenHeight(activity: Activity): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                val insets: Insets =
                    windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && activity.resources.configuration.smallestScreenWidthDp < 600) { // landscape and phone
                    bounds.height()
                } else { // portrait or tablet
                    val navigationBarSize: Int = insets.bottom
                    bounds.height() - navigationBarSize
                }
            } else {
                val outMetrics = DisplayMetrics()
                @Suppress("DEPRECATION") activity.windowManager.defaultDisplay.getMetrics(outMetrics)
                outMetrics.heightPixels
            }
        }

        fun getSystemBarsHeight(activity: AppCompatActivity): Int {
            val insets: WindowInsetsCompat = ViewCompat.getRootWindowInsets(activity.window.decorView) ?: return 0

            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val actionBarHeight = activity.supportActionBar?.height ?: 0
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            return statusBarHeight + actionBarHeight + navBarHeight
        }

        fun isTablet(): Boolean {
            return getContext().resources.getBoolean(R.bool.isTab)
        }

        fun isKeyboardVisible(): Boolean {
            val imm =
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            return imm != null && imm.isActive
        }

        fun showKeyboard(activity: AppCompatActivity) {
            if (!KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(activity.window.decorView.rootView, 0)
            }
        }

        fun closeKeyboard(activity: AppCompatActivity) {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                val imm =
                    activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                val cf = activity.currentFocus
                if (cf != null) {
                    imm.hideSoftInputFromWindow(cf.windowToken, 0)
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setupUI(view: View, activity: AppCompatActivity) {
            // Set up touch checkedChangedListener for non-text box views to hide keyboard.
            if (view !is EditText && view !is AppCompatTextView) {
                view.setOnTouchListener { _, motionEvent ->
                    closeKeyboard(activity)
                    if (view is Button && view !is Switch && view !is CheckBox) {
                        touchButton(motionEvent, view)
                        true
                    } else {
                        false
                    }
                }
            }

            //If a layout container, iterate over children and seed recursion.
            if (view is ViewGroup) {
                (0 until view.childCount).map { view.getChildAt(it) }
                    .forEach { setupUI(it, activity) }
            }
        }

        fun touchButton(motionEvent: MotionEvent, button: Button) {
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    button.isPressed = false
                    button.performClick()
                }

                MotionEvent.ACTION_DOWN -> {
                    button.isPressed = true
                }
            }
        }

        fun getBestContrastColor(color: String): Int {
            val backColor = Color.parseColor(color)
            val l =
                0.2126 * Color.red(backColor) + 0.7152 * Color.green(backColor) + 0.0722 * Color.blue(
                    backColor
                )
            return if (l <= 128) textLightColor()
            else textDarkColor()
        }

        fun getBestContrastColor(color: Int): Int {
            return getBestContrastColor("#" + Integer.toHexString(color))
        }

        fun toStringColorToInt(color: String): Int {
            val backColor = Color.parseColor(color)
            return Color.red(backColor) + Color.green(backColor) + Color.blue(backColor)
        }

        @ColorInt
        fun textLightColor(): Int {
            return ResourcesCompat.getColor(getContext().resources, R.color.text_light, null)
        }

        @ColorInt
        fun textDarkColor(): Int {
            return ResourcesCompat.getColor(getContext().resources, R.color.text_dark, null)
        }

        fun manipulateColor(color: Int, factor: Float): Int {
            val a = Color.alpha(color)
            val r = (Color.red(color) * factor).roundToInt()
            val g = (Color.green(color) * factor).roundToInt()
            val b = (Color.blue(color) * factor).roundToInt()
            return Color.argb(
                a, min(r, 255), min(g, 255), min(b, 255)
            )
        }

        fun getColorWithAlpha(colorId: Int, alpha: Int): Int {
            val color = ResourcesCompat.getColor(getContext().resources, colorId, null)

            val red = Color.red(color)
            val blue = Color.blue(color)
            val green = Color.green(color)

            return Color.argb(alpha, red, green, blue)
        }
    }
}