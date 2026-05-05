package com.aitool.plantid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitool.plantid.ai.PlantChatAI
import com.aitool.plantid.data.ChatDao
import com.aitool.plantid.data.ChatSessionEntity
import com.aitool.plantid.view.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Gắn mác VIP cho Hilt nhận diện, yêu cầu bơm ChatDao vào
@HiltViewModel
class PlantChatViewModel @Inject constructor(
    private val chatDao: ChatDao
) : ViewModel() {

    private val plantChatBot = PlantChatAI()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var currentSessionId: Int? = null

    // 2. Lấy dữ liệu lịch sử từ Dao (tự động cập nhật bằng Flow)
    val chatHistorySessions: StateFlow<List<ChatSessionEntity>> = chatDao.getAllChatSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun sendMessage(userMsg: String) {
        // 1. Cập nhật UI ngay lập tức cho mượt
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(text = userMsg, isFromUser = true))
        currentList.add(ChatMessage(text = "Typing...", isFromUser = false, isLoading = true))
        _chatMessages.value = currentList

        // 2. Xử lý logic ngầm (Lưu DB + Gọi AI)
        viewModelScope.launch {

            if (currentSessionId == null) {
                val newSession = ChatSessionEntity(title = userMsg)

                currentSessionId = chatDao.insertSession(newSession).toInt()
            }

            val aiResponse = plantChatBot.sendMessageToBot(userMsg)

            val updatedList = _chatMessages.value.toMutableList()
            updatedList[updatedList.lastIndex] = ChatMessage(text = aiResponse, isFromUser = false)
            _chatMessages.value = updatedList
        }
    }

    fun loadChatSession(sessionId: Int) {
        currentSessionId = sessionId
        viewModelScope.launch {
            val historyEntities = chatDao.getMessagesBySession(sessionId).first()
            val uiMessages = historyEntities.map { entity ->
                ChatMessage(
                    text = entity.content,
                    isFromUser = entity.sender == "USER",
                    isLoading = false
                )
            }
            _chatMessages.value = uiMessages
        }
    }
}

