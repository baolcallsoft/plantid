package com.aitool.plantid.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "saved_plants",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE // Xóa Thư mục -> Xóa hết cây bên trong
        )
    ],
    indices = [Index("collectionId")]
)
data class SavedPlantEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val collectionId: String,
    val plantName: String,
    val scientificName: String,
    val imageUri: String,
    val aiJsonData: String,
    val createdAt: Long = System.currentTimeMillis()
)