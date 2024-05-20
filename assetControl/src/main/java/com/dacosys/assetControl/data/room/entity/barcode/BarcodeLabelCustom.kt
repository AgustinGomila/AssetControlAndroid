package com.dacosys.assetControl.data.room.entity.barcode

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dacosys.assetControl.data.room.entity.barcode.BarcodeLabelCustom.Entry
import com.dacosys.assetControl.data.webservice.barcode.BarcodeLabelCustomObject

@Entity(
    tableName = Entry.TABLE_NAME,
    indices = [
        Index(
            value = [Entry.BARCODE_LABEL_TARGET_ID],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.BARCODE_LABEL_TARGET_ID}"
        ),
        Index(
            value = [Entry.DESCRIPTION],
            name = "IDX_${Entry.TABLE_NAME}_${Entry.DESCRIPTION}"
        )
    ]
)
data class BarcodeLabelCustom(
    @PrimaryKey
    @ColumnInfo(name = Entry.ID) val id: Long = 0L,
    @ColumnInfo(name = Entry.DESCRIPTION) val description: String = "",
    @ColumnInfo(name = Entry.ACTIVE) val active: Int = 0,
    @ColumnInfo(name = Entry.BARCODE_LABEL_TARGET_ID) val barcodeLabelTargetId: Long = 0L,
    @ColumnInfo(name = Entry.TEMPLATE) val template: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        description = parcel.readString().orEmpty(),
        active = parcel.readInt(),
        barcodeLabelTargetId = parcel.readLong(),
        template = parcel.readString().orEmpty()
    )

    constructor(blcObject: BarcodeLabelCustomObject) : this(
        id = blcObject.barcode_label_custom_id,
        description = blcObject.description,
        active = blcObject.active,
        barcodeLabelTargetId = blcObject.barcode_label_target_id,
        template = blcObject.template
    )

    object Entry {
        const val TABLE_NAME = "barcode_label_custom"
        const val ID = "_id"
        const val DESCRIPTION = "description"
        const val ACTIVE = "active"
        const val BARCODE_LABEL_TARGET_ID = "barcode_label_target_id"
        const val TEMPLATE = "template"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeInt(active)
        parcel.writeLong(barcodeLabelTargetId)
        parcel.writeString(template)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BarcodeLabelCustom> {
        override fun createFromParcel(parcel: Parcel): BarcodeLabelCustom {
            return BarcodeLabelCustom(parcel)
        }

        override fun newArray(size: Int): Array<BarcodeLabelCustom?> {
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
            r.add("ALTER TABLE barcode_label_custom RENAME TO barcode_label_custom_temp")
            r.add(
                """
            CREATE TABLE IF NOT EXISTS `barcode_label_custom`
            (
                `_id`                     INTEGER NOT NULL,
                `description`             TEXT    NOT NULL,
                `active`                  INTEGER NOT NULL,
                `barcode_label_target_id` INTEGER NOT NULL,
                `template`                TEXT    NOT NULL,
                PRIMARY KEY (`_id`)
            );
        """.trimIndent()
            )
            r.add(
                """
            INSERT INTO barcode_label_custom (
                _id, description, active,
                barcode_label_target_id, template
            )
            SELECT
                _id, description, active,
                barcode_label_target_id, template
            FROM barcode_label_custom_temp
        """.trimIndent()
            )
            r.add("DROP TABLE barcode_label_custom_temp")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_custom_barcode_label_target_id`;")
            r.add("DROP INDEX IF EXISTS `IDX_barcode_label_custom_description`;")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_barcode_label_target_id` ON `barcode_label_custom` (`barcode_label_target_id`);")
            r.add("CREATE INDEX IF NOT EXISTS `IDX_barcode_label_custom_description` ON `barcode_label_custom` (`description`);")
            return r
        }
    }
}