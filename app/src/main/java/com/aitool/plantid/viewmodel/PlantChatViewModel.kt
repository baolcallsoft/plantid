package com.aitool.plantid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitool.plantid.ai.PlantChatAI
import com.aitool.plantid.view.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantChatViewModel : ViewModel() {

    // 1. Khởi tạo AI
    private val plantChatBot = PlantChatAI()

    // 2. Danh sách tin nhắn (StateFlow để UI lắng nghe)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // 3. Hàm gửi tin nhắn
    fun sendMessage(userMsg: String) {
        // Copy list hiện tại để thêm tin nhắn mới
        val currentList = _chatMessages.value.toMutableList()

        // Thêm câu hỏi của User
        currentList.add(ChatMessage(text = userMsg, isFromUser = true))

        // Thêm bong bóng "Typing..." của Bot
        currentList.add(ChatMessage(text = "Typing...", isFromUser = false, isLoading = true))

        // Cập nhật lên UI ngay lập tức
        _chatMessages.value = currentList

        // 4. Chạy ngầm AI bằng viewModelScope (Tự động hủy nếu ViewModel chết)
        viewModelScope.launch {
            val aiResponse = plantChatBot.sendMessageToBot(userMsg)

            // Xóa chữ "Typing..." và thay bằng câu trả lời thật
            val updatedList = _chatMessages.value.toMutableList()
            updatedList[updatedList.lastIndex] = ChatMessage(text = aiResponse, isFromUser = false)

            // Cập nhật lại UI
            _chatMessages.value = updatedList
        }
    }
}