package com.dacosys.assetControl.network.utils

import android.os.Parcel
import android.os.Parcelable
import com.dacosys.assetControl.AssetControlApp.Companion.getContext
import com.dacosys.assetControl.R
import java.util.*

class ProgressStatus : Parcelable {
    var id: Int = 0
    var description: String = ""

    constructor(ProgressStatusId: Int, description: String) {
        this.description = description
        this.id = ProgressStatusId
    }

    override fun toString(): String {
        return description
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is ProgressStatus) {
            false
        } else this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    constructor(parcel: Parcel) {
        id = parcel.readInt()
        description = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProgressStatus> {
        override fun createFromParcel(parcel: Parcel): ProgressStatus {
            return ProgressStatus(parcel)
        }

        override fun newArray(size: Int): Array<ProgressStatus?> {
            return arrayOfNulls(size)
        }

        var unknown =
            ProgressStatus(0, getContext().getString(R.string.unknown))
        var starting = ProgressStatus(
            1,
            getContext().getString(R.string.progress_status_starting)
        )
        var running = ProgressStatus(
            2,
            getContext().getString(R.string.progress_status_running)
        )
        var success = ProgressStatus(
            3,
            getContext().getString(R.string.progress_status_success)
        )
        var canceled = ProgressStatus(
            4,
            getContext().getString(R.string.progress_status_canceled)
        )
        var crashed = ProgressStatus(
            5,
            getContext().getString(R.string.progress_status_crashed)
        )
        var finished = ProgressStatus(
            6,
            getContext().getString(R.string.progress_status_finished)
        )
        var bigStarting = ProgressStatus(
            7,
            getContext().getString(R.string.progress_status_starting)
        )
        var bigFinished = ProgressStatus(
            8,
            getContext().getString(R.string.progress_status_finished)
        )
        var bigCrashed = ProgressStatus(
            9,
            getContext().getString(R.string.progress_status_crashed)
        )

        fun getAllFinish(): ArrayList<ProgressStatus> {
            val allSections = ArrayList<ProgressStatus>()
            Collections.addAll(
                allSections,
                canceled,
                crashed,
                finished,
                bigCrashed,
                bigFinished
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun getAll(): ArrayList<ProgressStatus> {
            val allSections = ArrayList<ProgressStatus>()
            Collections.addAll(
                allSections,
                unknown,
                starting,
                running,
                success,
                canceled,
                crashed,
                finished
            )

            return ArrayList(allSections.sortedWith(compareBy { it.id }))
        }

        fun isFinishStatus(id: Int): Boolean {
            return getAllFinish().contains(getById(id))
        }

        fun getById(ProgressStatusId: Int): ProgressStatus? {
            return getAll().firstOrNull { it.id == ProgressStatusId }
        }
    }
}