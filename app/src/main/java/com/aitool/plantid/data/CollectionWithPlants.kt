package com.aitool.plantid.data

import androidx.room.Embedded
import androidx.room.Relation

data class CollectionWithPlants(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "collectionId"
    )
    val savedPlants: List<SavedPlantEntity>
)