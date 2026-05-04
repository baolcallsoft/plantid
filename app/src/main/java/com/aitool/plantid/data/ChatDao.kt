package com.aitool.plantid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // 1. Lấy toàn bộ danh sách lịch sử chat (Mới nhất lên đầu)
    // Trả về dạng Flow để giao diện tự động cập nhật khi có chat mới
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>

    // 2. Tạo một phiên chat mới
    @Insert
    suspend fun insertSession(session: ChatSessionEntity): Long

    // 3. Xóa một đoạn chat
    @Query("DELETE FROM chat_sessions WHERE sessionId = :id")
    suspend fun deleteSession(id: Int)

    // --- Lệnh cho tin nhắn ---

    // 4. Lấy toàn bộ tin nhắn của một đoạn chat cụ thể
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: Int): Flow<List<ChatMessageEntity>>

    // 5. Lưu tin nhắn mới vào db
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)
}