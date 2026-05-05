package com.aitool.plantid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 2. Giao chìa khóa cho anh thủ kho (DAO)
    abstract fun chatDao(): ChatDao
}