package com.aitool.plantid.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitool.plantid.ai.PlantChatAI
import com.aitool.plantid.data.ChatDao
import com.aitool.plantid.data.ChatMessageEntity
import com.aitool.plantid.data.ChatSessionEntity
import com.aitool.plantid.view.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 1. Gắn mác VIP cho Hilt nhận diện, yêu cầu bơm ChatDao vào
@HiltViewModel
class PlantChatViewModel @Inject constructor(
    private val chatDao: ChatDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val plantChatBot = PlantChatAI()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedChatIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedChatIds: StateFlow<Set<Int>> = _selectedChatIds.asStateFlow()

    // 2. Lấy dữ liệu lịch sử từ Dao (tự động cập nhật bằng Flow)
    val chatHistorySessions: StateFlow<List<ChatSessionEntity>> = chatDao.getAllChatSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 🔥 Cập nhật 1: Thêm tham số imageUri (mặc định là null nếu chỉ gửi text)
    fun sendMessage(userMsg: String, imageUri: android.net.Uri? = null) {
        android.util.Log.d("KIEM_TRA_API", "Hàm sendMessage vừa được gọi 1 lần!")
        // 1. Cập nhật UI ngay lập tức
        val currentList = _chatMessages.value.toMutableList()
        // Nhét thêm imageUri vào bóng chat của User
        currentList.add(ChatMessage(text = userMsg, isFromUser = true, imageUri = imageUri?.toString()))
        currentList.add(ChatMessage(text = "Typing...", isFromUser = false, isLoading = true))
        _chatMessages.value = currentList

        // 2. Xử lý logic ngầm (Lưu DB + Gọi AI)
        viewModelScope.launch {
            if (_currentSessionId.value == null) {
                // Nếu người dùng chỉ gửi ảnh mà không gõ chữ, lấy chữ "Image Analysis" làm tiêu đề
                val title = if (userMsg.isNotBlank()) userMsg else "Image Analysis"
                val newSession = ChatSessionEntity(title = title)
                _currentSessionId.value = chatDao.insertSession(newSession).toInt()
            }

            // Lưu tin nhắn của User kèm đường dẫn ảnh (ép về dạng String)
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = _currentSessionId.value!!,
                    sender = "USER",
                    content = userMsg,
                    imageUri = imageUri?.toString()
                )
            )

            val aiResponse = if (imageUri != null) {
                val bitmap = uriToBitmap(imageUri.toString())
                if (bitmap != null) {
                    plantChatBot.sendMessageWithImageToBot(userMsg, bitmap)
                } else {
                    "Lỗi: Không thể đọc được ảnh từ thiết bị."
                }
            } else {
                plantChatBot.sendMessageToBot(userMsg)
            }

            val updatedList = _chatMessages.value.toMutableList()
            updatedList[updatedList.lastIndex] = ChatMessage(text = aiResponse, isFromUser = false)
            _chatMessages.value = updatedList

            // Lưu tin nhắn của Bot (Bot thì không có ảnh nên để null)
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = _currentSessionId.value!!,
                    sender = "BOT",
                    content = aiResponse,
                    imageUri = null
                )
            )
        }
    }

    // 🔥 Cập nhật 2: Phục hồi ảnh khi mở lại lịch sử chat
    fun loadChatSession(sessionId: Int) {
        _currentSessionId.value = sessionId
        _chatMessages.value = emptyList()
        viewModelScope.launch {
            val historyEntities = chatDao.getMessagesBySession(sessionId).first()
            if (historyEntities.isNotEmpty()) {
                val uiMessages = historyEntities.map { entity ->
                    ChatMessage(
                        text = entity.content,
                        isFromUser = entity.sender == "USER",
                        isLoading = false,
                        imageUri = entity.imageUri
                    )
                }
                _chatMessages.value = uiMessages
            }
        }
    }

    fun renameChatSession(sessionId: Int, newTitle: String) {
        viewModelScope.launch {
            chatDao.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun deleteChatSession(sessionId: Int) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                chatDao.deleteSession(sessionId)
            }
        }
    }

    // Hàm này giúp chuyển đường dẫn Uri thành một tấm ảnh Bitmap thực thụ
    private fun uriToBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedChatIds.value = emptySet() // Reset khi tắt chế độ chọn
        }
    }

    fun toggleChatSelection(chatId: Int) {
        val currentSelected = _selectedChatIds.value
        _selectedChatIds.value = if (currentSelected.contains(chatId)) {
            currentSelected - chatId
        } else {
            currentSelected + chatId
        }
    }

    fun selectAllChats(chatIds: Set<Int>) {
        _selectedChatIds.value = chatIds
    }

    fun deleteSelectedChats() {
        val idsToDelete = _selectedChatIds.value
        idsToDelete.forEach { id ->
            deleteChatSession(id)
        }
        setSelectionMode(false) // Thoát chế độ chọn sau khi xóa xong
    }
}

