package com.dacosys.assetControl.data.room.entity.category

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.dacosys.assetControl.data.room.entity.category.ItemCategory.Entry
import com.dacosys.assetControl.data.webservice.category.ItemCategoryObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        ),
        Index(
            value = [Entry.PARENT_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.PARENT_ID}"
        )
    ]
)
data class ItemCategory(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) var id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) var description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) var active: Int = 0,
    @ColumnInfo(name = Entry.PARENT_ID) var parentId: Long = 0L,
    @ColumnInfo(name = Entry.TRANSFERRED) var transferred: Int? = null,
    @Ignore var parentStr: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        parentId = parcel.readLong(),
        transferred = parcel.readValue(Int::class.java.classLoader) as? Int,
        parentStr = parcel.readString().orEmpty()
    )

    constructor(ic: ItemCategoryObject) : this(
        id = ic.item_category_id,
        description = ic.description,
        active = ic.active,
        parentId = ic.parent_id,
        transferred = 1
    )

    object Entry {
        const val TABLE_NAME = "item_category"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val PARENT_ID = "parent_id"
        const val TRANSFERRED = "transferred"

        const val PARENT_STR = "parent_str"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(parentId)
        parcel.writeValue(transferred)
        parcel.writeString(parentStr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemCategory> {
        override fun createFromParcel(parcel: Parcel): ItemCategory {
            return ItemCategory(parcel)
        }

        override fun newArray(size: Int): Array<ItemCategory?> {
            return arrayOfNulls(size)
        }

        /**
         * Migration zero
         * Migración desde la base de datos SQLite (version 0) a la primera versión de Room.
         * No utilizar constantes para la definición de nombres para evitar incoherencias en el futuro.
         * @return
         */
        fun migrationZero(): List<String> {
            val r: ArrayList<String> = arrayListOf()
            r.add("ALTER TABLE item_category RENAME TO item_category_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `item_category`
            (
                `_id`         INTEGER NOT NULL,
                `description` TEXT    NOT NULL,
                `active`      INTEGER NOT NULL,
                `parent_id`   INTEGER NOT NULL,
                `transferred` INTEGER,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO item_category (
                `_id`, `description`, `active`,
                `parent_id`, `transferred`
            )
            SELECT
                `_id`, `description`, `active`,
                `parent_id`, `transferred`
            FROM item_category_temp
        """.trimIndent()
            )
            r.add("DROP TABLE item_category_temp")
            r.add("DROP INDEX IF EXISTS `IDX_item_category_description`;")
            r.add("DROP INDEX IF EXISTS `IDX_item_category_parent_id`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_item_category_description` ON `item_category` (`description`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_item_category_parent_id` ON `item_category` (`parent_id`);")
            return r
        }
    }
}
