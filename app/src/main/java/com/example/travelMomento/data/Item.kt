package com.example.travelMomento.data

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity data class represents a single row in the database.
 */
@Entity
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "title")
    val itemTitle: String,

    @ColumnInfo(name = "content")
    val itemContent: String,

    @ColumnInfo(name = "image")
    val itemImage: Bitmap? = null,

    @ColumnInfo(name = "createdDate")
    val itemCreatedDate: Date? = null,

    @ColumnInfo(name = "modifiedDate")
    val itemModifiedDate: Date? = null
)
