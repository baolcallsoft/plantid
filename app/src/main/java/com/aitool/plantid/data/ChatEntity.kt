package com.aitool.plantid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Bảng lưu trữ "Đoạn chat" (Dùng cho ChatHistoryScreen)
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

// 2. Bảng lưu trữ "Chi tiết tin nhắn" (Dùng cho giao diện nhắn tin)
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val sessionId: Int,         // Tin nhắn này thuộc về đoạn chat nào?
    val sender: String,         // "USER" hoặc "BOT"
    val content: String,        // Nội dung chữ
    val imageUri: String?,      // Đường dẫn ảnh (nếu người dùng có gửi ảnh cây)
    val timestamp: Long = System.currentTimeMillis()
)