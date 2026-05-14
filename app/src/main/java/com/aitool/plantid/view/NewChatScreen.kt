package com.aitool.plantid.view

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.camera.CameraPermissionSheet
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.ui.Green100
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral100

@Composable
fun NewChatScreen(
    navController: NavController, // BẮT BUỘC PHẢI CÓ để lấy ảnh từ Camera về
    onNavigateToChatDetail: (String?, String?) -> Unit,
    onOpenCamera: () -> Unit
) {
    var chatText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Hứng ảnh từ Camera
    val cameraResult by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("camera_result", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    var showAttachmentSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showPermissionSheet by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenCamera() // ĐÃ SỬA: Dùng hành động thay vì navigate thẳng
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

    LaunchedEffect(cameraResult) {
        cameraResult?.let { uriString ->
            selectedImageUri = android.net.Uri.parse(uriString)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("camera_result")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bot),
                contentDescription = "Bot Mascot",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Hi there! Got questions about your plant?\nI've got answers!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // ==========================================
        // KHUNG NHẬP LIỆU & ẢNH ĐÍNH KÈM NỔI BÊN NGOÀI
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 8.dp)
        ) {
            selectedImageUri?.let { uri ->
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_remove),
                            contentDescription = "Remove",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 140.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, Neutral100, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_image),
                        contentDescription = "Gallery",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showAttachmentSheet = true }
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(1.dp)
                            .height(24.dp)
                            .background(Neutral100)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (chatText.isEmpty()) Text("Typing...", color = Color.Gray, fontSize = 15.sp)
                        BasicTextField(
                            value = chatText,
                            onValueChange = { chatText = it },
                            maxLines = 3,
                            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (chatText.isNotBlank() || selectedImageUri != null) Color(0xFF00C853) else Color(0xFFF2F2F2),
                            shape = CircleShape
                        )
                        .clickable(enabled = chatText.isNotBlank() || selectedImageUri != null) {
                            if (chatText.isNotBlank() || selectedImageUri != null) {
                                val msgToSend = chatText.ifBlank { "Please analyze this plant image." }
                                // ĐÃ SỬA: Dùng hàm onNavigateToChatDetail thay vì tự encode Uri
                                onNavigateToChatDetail(msgToSend, selectedImageUri?.toString())

                                chatText = ""
                                selectedImageUri = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_up),
                        contentDescription = "Send",
                        tint = if (chatText.isNotBlank() || selectedImageUri != null) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    AppBottomSheet(
        showSheet = showAttachmentSheet,
        onDismiss = { showAttachmentSheet = false },
        showDragHandle = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add a photo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(
                    onClick = { showAttachmentSheet = false },
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            HorizontalDivider(color = Neutral100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green900)
                    .clickable {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.ic_photo), contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose from Gallery", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green100)
                    .clickable {
                        showAttachmentSheet = false
                        val permission = android.Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            onOpenCamera()
                        } else {
                            showPermissionSheet = true
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(id = R.drawable.ic_camera), contentDescription = "Camera", tint = Green900, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take a Photo", color = Green900, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    CameraPermissionSheet(
        showSheet = showPermissionSheet,
        onDismiss = { showPermissionSheet = false },
        onAllow = {
            showPermissionSheet = false
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    )
}