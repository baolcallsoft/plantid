package com.aitool.plantid.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "scan_history")
data class HistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    // Cột này cực kỳ quan trọng để làm chức năng thanh Search trên đỉnh màn hình
    val plantName: String,

    // Đường dẫn ảnh chụp lưu ở Cache hoặc Document của máy
    val imageUri: String,

    // Lưu loại quét (Ví dụ: "Identify plant", "Mushroom", "Flower", "Diagnose plant")
    // Ở UI bạn có thể dùng biến này để đổi màu cái Tag (Xanh lá, Cam, Đỏ...)
    val scanType: String,

    // Vẫn lưu toàn bộ JSON để khi bấm vào 1 item, ném thẳng qua ResultScreen là xong
    val aiJsonData: String,

    // Thời gian quét, dùng để sort mới nhất lên đầu và format ra chuỗi "09:30 12/04/2024"
    val createdAt: Long = System.currentTimeMillis()
)