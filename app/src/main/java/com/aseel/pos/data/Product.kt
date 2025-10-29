package com.aseel.pos.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("sku")]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val nameAr: String,
    val nameEn: String? = null,
    val priceBase: Double, // Base price in YER
    val stockQty: Int = 0,
    val imagePath: String? = null,
    val categoryId: Long? = null
)
