package com.aitool.plantid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // 1. Lưu lịch sử mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    // 2. Lấy toàn bộ lịch sử (Mới nhất xếp trên cùng)
    @Query("SELECT * FROM scan_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    // 3. Chức năng cho thanh Search: Tìm kiếm theo tên cây (Không phân biệt hoa thường)
    @Query("SELECT * FROM scan_history WHERE plantName LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchHistory(searchQuery: String): Flow<List<HistoryEntity>>

    // 4. 🔥 AUTO-CLEANUP: Câu lệnh phép thuật để giữ đúng 50 mục mới nhất
    // Bạn nên gọi hàm này ngay sau khi gọi hàm insertHistory()
    @Query("""
        DELETE FROM scan_history 
        WHERE id NOT IN (
            SELECT id FROM scan_history 
            ORDER BY createdAt DESC 
            LIMIT 50
        )
    """)
    suspend fun deleteOldHistoryExceedingLimit()

    // 5. Xóa 1 lịch sử cụ thể (Khi người dùng bấm vào dấu 3 chấm -> Delete)
    @Query("DELETE FROM scan_history WHERE id = :historyId")
    suspend fun deleteHistoryById(historyId: String)

    // 6. Xóa toàn bộ lịch sử (Clear all)
    @Query("DELETE FROM scan_history")
    suspend fun clearAllHistory()
}