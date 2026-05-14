package com.aitool.plantid.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.aitool.plantid.view.CameraEntryMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 1. Quản lý chế độ hiện tại (Lưu dưới dạng String để an toàn khi lưu trữ)
    val activeModeStr: StateFlow<String> = savedStateHandle.getStateFlow("active_mode", CameraEntryMode.IDENTIFY_PLANT.name)

    // 2. Trạng thái Flash
    val isFlashEnabled: StateFlow<Boolean> = savedStateHandle.getStateFlow("flash_enabled", false)

    // 3. Trạng thái Zoom
    val currentZoomRatio: StateFlow<Float> = savedStateHandle.getStateFlow("zoom_ratio", 1f)

    // 4. Ảnh đã chụp (Lưu đường dẫn dạng String)
    val capturedImageUriStr: StateFlow<String?> = savedStateHandle.getStateFlow("captured_uri", null)

    // 5. Trạng thái hiển thị Bottom Sheet Tips
    val showSnapTipsSheet: StateFlow<Boolean> = savedStateHandle.getStateFlow("show_tips", false)


    // --- CÁC HÀM CẬP NHẬT TRẠNG THÁI ---

    // Đặt chế độ ban đầu khi từ màn hình khác truyền vào (chỉ set 1 lần)
    fun setInitialModeIfNeeded(mode: CameraEntryMode) {
        if (!savedStateHandle.contains("active_mode_initialized")) {
            savedStateHandle["active_mode"] = mode.name
            savedStateHandle["active_mode_initialized"] = true
        }
    }

    fun setActiveMode(mode: CameraEntryMode) {
        savedStateHandle["active_mode"] = mode.name
    }

    fun toggleFlash() {
        savedStateHandle["flash_enabled"] = !(isFlashEnabled.value)
    }

    fun setZoomRatio(zoom: Float) {
        // Đảm bảo zoom luôn nằm trong khoảng an toàn từ 1x đến 5x
        savedStateHandle["zoom_ratio"] = zoom.coerceIn(1f, 5f)
    }

    fun setCapturedImageUri(uri: Uri?) {
        savedStateHandle["captured_uri"] = uri?.toString()
    }

    fun toggleSnapTipsSheet(show: Boolean) {
        savedStateHandle["show_tips"] = show
    }
}