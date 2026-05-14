package com.aitool.plantid.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures // Thư viện để nhận diện vuốt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput // Thư viện để nhận diện cảm ứng
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.ui.Neutral300
import com.aitool.plantid.ui.Neutral900

// 1. Máy quét mạng thông minh (Đã sửa lỗi báo sai mạng)
@Composable
fun rememberNetworkState(context: Context): State<Boolean> {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isOnline = remember { mutableStateOf(checkIfOnline(connectivityManager)) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            // Không dùng onAvailable nữa vì có kết nối chưa chắc đã có internet

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // Phải có cả 2 cờ này mới chắc chắn 100% là vào được mạng
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                isOnline.value = hasInternet
            }

            override fun onLost(network: Network) {
                isOnline.value = false
            }
        }

        // Dùng registerDefaultNetworkCallback thay vì Builder cũ để chỉ theo dõi mạng đang được dùng chính
        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }
    return isOnline
}

private fun checkIfOnline(cm: ConnectivityManager): Boolean {
    val network = cm.activeNetwork ?: return false
    val cap = cm.getNetworkCapabilities(network) ?: return false
    // Kiểm tra cờ VALIDATED để tránh báo lỗi ảo khi mới bật app
    return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

// 2. Giao diện Banner có tính năng Vuốt lên để ẩn
@Composable
fun TopNoInternetBanner(isOffline: Boolean) {
    // Biến lưu trạng thái người dùng đã vuốt tắt hay chưa
    var userDismissed by remember { mutableStateOf(false) }

    // Khi có mạng lại (isOffline = false), reset biến userDismissed để lần mất mạng sau banner vẫn hiện ra
    LaunchedEffect(isOffline) {
        if (!isOffline) {
            userDismissed = false
        }
    }

    // Chỉ hiện Banner khi đang mất mạng VÀ người dùng chưa tự tay vuốt tắt
    val showBanner = isOffline && !userDismissed

    AnimatedVisibility(
        visible = showBanner,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCD4747)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                // BẮT SỰ KIỆN VUỐT Ở ĐÂY
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        // dragAmount < 0 nghĩa là ngón tay đang vuốt lên trên
                        if (dragAmount < -5f) {
                            userDismissed = true
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_disconect),
                    contentDescription = "No WiFi",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "No internet connection",
                        fontWeight = FontWeight.Medium,
                        color = Neutral900,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Connect to the internet and try again",
                        fontSize = 13.sp,
                        color = Neutral300
                    )
                }
            }
        }
    }
}