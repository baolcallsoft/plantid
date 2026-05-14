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
import com.aitool.plantid.data.HistoryDao
import com.aitool.plantid.data.HistoryEntity
import com.aitool.plantid.view.CameraEntryMode
import com.aitool.plantid.view.ResultType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

// Trạng thái của màn hình Scan
sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    data class Success(val jsonResult: String, val resultType: ResultType, val imageUri: String? = null) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

@HiltViewModel
class SharedScannerViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {
    private val plantChatAI = PlantChatAI()

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Hàm này được gọi khi ScannerScreen mở lên
    fun processImage(context: Context, imageUriString: String, modeStr: String) {
        // Tránh gọi lại AI nếu xoay màn hình
        if (_uiState.value is ScannerUiState.Loading || _uiState.value is ScannerUiState.Success) return

        _uiState.value = ScannerUiState.Loading

        viewModelScope.launch {
            try {
                val uri = Uri.parse(imageUriString)
                val bitmap = getBitmapFromUri(context, uri)

                if (bitmap == null) {
                    _uiState.value = ScannerUiState.Error("Could not load image.")
                    return@launch
                }

                // Kiểm tra mode để gọi đúng hàm AI
                val jsonResult = when (modeStr) {
                    CameraEntryMode.DIAGNOSE_PLANT.name -> {
                        plantChatAI.diagnosePlantImage(bitmap)
                    }
                    else -> {
                        // Các mode còn lại (Identify, Flower, Mushroom)
                        plantChatAI.analyzePlantImage(bitmap)
                    }
                }

                // 🔥 CẬP NHẬT 1: Map modeStr sang ResultType chuẩn
                val resultType = when (modeStr) {
                    CameraEntryMode.MUSHROOM.name -> ResultType.MUSHROOM
                    CameraEntryMode.FLOWER.name -> ResultType.FLOWER
                    CameraEntryMode.DIAGNOSE_PLANT.name -> ResultType.DIAGNOSE_PLANT
                    else -> ResultType.IDENTIFY_PLANT
                }

                // LƯU VÀO LỊCH SỬ
                saveToHistory(jsonResult, imageUriString, resultType)

                _uiState.value = ScannerUiState.Success(jsonResult, resultType, imageUriString)
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun saveToHistory(json: String, uri: String, type: ResultType) {
        try {
            val root = JSONObject(json)
            // Tên cây nằm ở các vị trí khác nhau tùy JSON (Plant vs Diagnose)
            val plantName = if (type == ResultType.DIAGNOSE_PLANT) {
                root.optString("diseaseName", "Unknown Issue")
            } else {
                root.optJSONObject("basicInfo")?.optString("name", "Unknown Plant") ?: "Unknown Plant"
            }

            // Map mode sang chuỗi hiển thị đẹp cho Tag
            val scanTypeDisplay = type.displayName

            val historyEntry = HistoryEntity(
                plantName = plantName,
                imageUri = uri,
                scanType = scanTypeDisplay,
                aiJsonData = json
            )

            historyDao.insertHistory(historyEntry)
            historyDao.deleteOldHistoryExceedingLimit() // Giữ tối đa 50 mục
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetState() {
        _uiState.value = ScannerUiState.Idle
    }

    // Hàm này dùng để nạp dữ liệu từ lịch sử vào để hiển thị lại ở ResultScreen
    fun loadFromHistory(json: String, type: ResultType, uri: String) {
        _uiState.value = ScannerUiState.Success(json, type, uri)
    }

    // Hàm phụ trợ biến Uri ảnh thành Bitmap (Gemini bắt buộc dùng cấu hình ARGB_8888)
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // Ép dùng Software để tránh lỗi phần cứng với Gemini
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}