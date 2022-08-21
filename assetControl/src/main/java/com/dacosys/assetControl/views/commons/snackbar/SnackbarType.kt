package com.dacosys.assetControl.views.commons.snackbar

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.dacosys.assetControl.R
import com.dacosys.assetControl.utils.Statics
import java.util.*

class SnackbarType(
    snackbarTypeId: Long,
    var description: String,
    var duration: Int,
    var backColor: Drawable?,
    var foreColor: Int,
) {
    var id: Long = snackbarTypeId

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is SnackbarType) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    companion object CREATOR {
        var ERROR = SnackbarType(
            snackbarTypeId = 0,
            description = Statics.AssetControl.getContext().getString(R.string.error),
            duration = 3500,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_error, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.firebrick)
                )
            )
        )
        var INFO = SnackbarType(
            snackbarTypeId = 1,
            description = Statics.AssetControl.getContext().getString(R.string.information),
            duration = 1500,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_info, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.goldenrod)
                )
            )
        )
        var RUNNING = SnackbarType(
            snackbarTypeId = 2,
            description = Statics.AssetControl.getContext().getString(R.string.running),
            duration = 750,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_running, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.lightskyblue)
                )
            )
        )
        var SUCCESS = SnackbarType(
            3,
            Statics.AssetControl.getContext().getString(R.string.success),
            duration = 1500,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_success, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.seagreen)
                )
            )
        )
        var ADD = SnackbarType(
            snackbarTypeId = 4,
            description = Statics.AssetControl.getContext().getString(R.string.add),
            duration = 1000,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_add, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.cadetblue)
                )
            )
        )
        var UPDATE = SnackbarType(
            snackbarTypeId = 5,
            description = Statics.AssetControl.getContext().getString(R.string.update),
            duration = 1000,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_update, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.steelblue)
                )
            )
        )
        var REMOVE = SnackbarType(
            snackbarTypeId = 6,
            description = Statics.AssetControl.getContext().getString(R.string.remove),
            duration = 1000,
            backColor = ResourcesCompat.getDrawable(
                Statics.AssetControl.getContext().resources, R.drawable.snackbar_remove, null
            ),
            foreColor = Statics.getBestContrastColor(
                "#" + Integer.toHexString(
                    Statics.AssetControl.getContext().getColor(R.color.orangered)
                )
            )
        )

        fun getAll(): ArrayList<SnackbarType> {
            val allSections = ArrayList<SnackbarType>()
            Collections.addAll(
                allSections,
                ERROR,
                INFO,
                RUNNING,
                SUCCESS,
                ADD,
                UPDATE,
                REMOVE
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getById(snackbarTypeId: Long): SnackbarType {
            return getAll().firstOrNull { it.id == snackbarTypeId } ?: INFO
        }
    }
}