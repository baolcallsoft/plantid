package com.aitool.plantid.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        HistoryEntity::class,
        CollectionEntity::class,
        SavedPlantEntity::class
    ],
    version = 1, // Nếu bạn chạy báo lỗi migration, hãy đổi số này thành 2, hoặc xóa app cài lại
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 1. DAO của Chat (Cũ)
    abstract fun chatDao(): ChatDao

    // 2. DAO của My Plants & History (Mới)
    abstract fun historyDao(): HistoryDao
    abstract fun collectionDao(): CollectionDao
}