package com.aitool.plantid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    // --- THAO TÁC VỚI THƯ MỤC ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: String)

    // Lấy tất cả Thư mục kèm danh sách cây (Dùng cho UI màn hình My Plants)
    @Transaction
    @Query("SELECT * FROM collections ORDER BY createdAt ASC")
    fun getCollectionsWithPlants(): Flow<List<CollectionWithPlants>>


    // --- THAO TÁC VỚI CÂY ĐÃ LƯU ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlant(plant: SavedPlantEntity)

    // Lấy danh sách cây thuộc một thư mục cụ thể (Dùng khi bấm vào 1 thư mục)
    @Query("SELECT * FROM saved_plants WHERE collectionId = :collectionId ORDER BY createdAt DESC")
    fun getPlantsInCollection(collectionId: String): Flow<List<SavedPlantEntity>>

    @Query("DELETE FROM saved_plants WHERE id = :plantId")
    suspend fun deleteSavedPlant(plantId: String)

    // Kiểm tra trùng lặp cây trong cùng 1 thư mục
    @Query("SELECT EXISTS(SELECT 1 FROM saved_plants WHERE collectionId = :collectionId AND scientificName = :sciName LIMIT 1)")
    suspend fun isPlantAlreadySaved(collectionId: String, sciName: String): Boolean
}