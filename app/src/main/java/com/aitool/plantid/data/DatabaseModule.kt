package com.aitool.plantid.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton // Đảm bảo Hilt chỉ tạo 1 Database duy nhất
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {

        // Mẹo: Khai báo biến trễ để có thể gọi database.collectionDao() ở bên trong callback
        lateinit var database: AppDatabase

        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "plant_bot_database" // Bạn có thể giữ nguyên tên này
        )
            // 🔥 THÊM CALLBACK ĐỂ BƠM DỮ LIỆU TẠI ĐÂY
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Chạy ngầm để không đơ màn hình
                    CoroutineScope(Dispatchers.IO).launch {
                        // Bơm 2 thư mục mặc định khi app vừa cài đặt
                        database.collectionDao().insertCollection(CollectionEntity(name = "Indoor"))
                        database.collectionDao().insertCollection(CollectionEntity(name = "Outdoor"))
                    }
                }
            })
            .build()

        return database
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    // 🔥 CUNG CẤP THÊM HistoryDao CHO HILT
    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    // 🔥 CUNG CẤP THÊM CollectionDao CHO HILT
    @Provides
    fun provideCollectionDao(database: AppDatabase): CollectionDao {
        return database.collectionDao()
    }
}