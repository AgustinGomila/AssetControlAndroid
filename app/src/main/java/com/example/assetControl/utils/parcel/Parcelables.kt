package com.example.assetControl.utils.parcel

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

object Parcelables {
    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = getParcelableExtra(key, T::class.java)

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = getParcelable(key, T::class.java)

    inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = getSerializable(key, T::class.java)

    inline fun <reified K : Serializable, reified V : Serializable> Bundle.serializableMap(key: String): MutableMap<K, V>? {
        @Suppress("UNCHECKED_CAST")
        return getSerializable(key, HashMap::class.java) as? MutableMap<K, V>
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String, default: T): T =
        getParcelable(key, T::class.java) ?: default

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String, default: () -> T): T =
        getParcelable(key, T::class.java) ?: default()

    inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? =
        getParcelableArrayList(key, T::class.java)

    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? =
        getParcelableArrayListExtra(key, T::class.java)
}