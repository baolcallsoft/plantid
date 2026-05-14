package com.aitool.plantid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitool.plantid.data.HistoryDao
import com.aitool.plantid.data.HistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    // Trạng thái của thanh tìm kiếm
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Danh sách lịch sử tự động thay đổi theo từ khóa tìm kiếm
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyList: StateFlow<List<HistoryEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                historyDao.getAllHistory() // Lấy toàn bộ nếu không search gì
            } else {
                historyDao.searchHistory(query) // Tìm kiếm theo tên
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Hàm cập nhật từ khóa khi người dùng gõ
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Xóa 1 mục lịch sử
    fun deleteHistory(id: String) {
        viewModelScope.launch {
            historyDao.deleteHistoryById(id)
        }
    }

    // Xóa tất cả lịch sử (Cho menu 3 chấm)
    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAllHistory()
        }
    }
}