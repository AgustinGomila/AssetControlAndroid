package com.example.assetControl.utils.misc

import java.util.*
import kotlin.math.min

fun <T> splitList(originalArray: Array<T>, partitionSize: Int): Array<List<T>> {
    val originalList = originalArray.toList()

    val partitions: LinkedList<List<T>> = LinkedList()
    for (i in originalList.indices step partitionSize) {
        val p = originalList.subList(i, min(i + partitionSize, originalList.size))
        partitions.add(p)
    }

    return partitions.toTypedArray()
}