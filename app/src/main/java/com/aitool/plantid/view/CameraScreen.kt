package com.aitool.plantid.view

import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral900
import com.aitool.plantid.viewmodel.CameraViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

enum class CameraEntryMode(val title: String) {
    MUSHROOM("Mushroom"),
    FLOWER("Flower"),
    IDENTIFY_PLANT("Identify plant"),
    DIAGNOSE_PLANT("Diagnose plant"),
    CHAT_ATTACHMENT("Chat attachment")
}

@Composable
fun CameraScreen(
    navController: NavController,
    entryMode: CameraEntryMode = CameraEntryMode.IDENTIFY_PLANT,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(entryMode) {
        viewModel.setActiveMode(entryMode)
    }

    val activeModeStr by viewModel.activeModeStr.collectAsState()
    val activeMode = try { CameraEntryMode.valueOf(activeModeStr) } catch (e: Exception) { CameraEntryMode.IDENTIFY_PLANT }
    val isFlashEnabled by viewModel.isFlashEnabled.collectAsState()
    val currentZoomRatio by viewModel.currentZoomRatio.collectAsState()
    val capturedImageUriStr by viewModel.capturedImageUriStr.collectAsState()
    val capturedImageUri = capturedImageUriStr?.let { android.net.Uri.parse(it) }
    val showSnapTipsSheet by viewModel.showSnapTipsSheet.collectAsState()

    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    val imageCapture = remember { ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build() }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.setCapturedImageUri(uri)
    }

    LaunchedEffect(isFlashEnabled, cameraControl) {
        cameraControl?.enableTorch(isFlashEnabled)
        imageCapture.flashMode = if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    fun capturePhoto() {
        val photoFile = File(context.cacheDir, "plant_img_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.setCapturedImageUri(android.net.Uri.fromFile(photoFile))
                }
                override fun onError(exc: ImageCaptureException) { exc.printStackTrace() }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // --- THANH CÔNG CỤ TRÊN CÙNG ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.toggleFlash() }) {
                val flashIcon = if (isFlashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                Icon(painterResource(flashIcon), contentDescription = "Flash", tint = Color.Black)
            }
        }

        // --- KHU VỰC PREVIEW CAMERA ---
        if (capturedImageUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            val newZoom = currentZoomRatio * zoom
                            viewModel.setZoomRatio(newZoom)
                            cameraControl?.setZoomRatio(currentZoomRatio)
                        }
                    }
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                                cameraControl = camera.cameraControl
                                cameraControl?.setZoomRatio(currentZoomRatio)
                                cameraControl?.enableTorch(isFlashEnabled)
                            } catch (exc: Exception) { exc.printStackTrace() }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )

                if (activeMode != CameraEntryMode.CHAT_ATTACHMENT) {
                    ScannerOverlayFrame()
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${currentZoomRatio.roundToInt()}x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            AsyncImage(
                model = capturedImageUri, contentDescription = "Captured Photo",
                modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f),
                contentScale = ContentScale.Crop
            )
        }

        // --- KHU VỰC ĐIỀU KHIỂN DƯỚI CÙNG ---
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            if (capturedImageUri == null) {
                if (activeMode == CameraEntryMode.CHAT_ATTACHMENT) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_capture),
                        contentDescription = "Capture Photo",
                        modifier = Modifier.size(70.dp).clip(CircleShape).clickable { capturePhoto() }
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val modes = listOf(CameraEntryMode.MUSHROOM, CameraEntryMode.FLOWER, CameraEntryMode.IDENTIFY_PLANT, CameraEntryMode.DIAGNOSE_PLANT)
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        val dynamicEdgePadding = (screenWidth / 2) - 45.dp

                        val initialIndex = modes.indexOf(activeMode).takeIf { it >= 0 } ?: 2
                        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
                        val coroutineScope = rememberCoroutineScope()

                        // TÍNH TOÁN INDEX GIỮA MÀN HÌNH NGAY TRONG KHI TRƯỢT (Dùng derivedStateOf để mượt mà, không bị khựng)
                        val centerIndex by remember {
                            derivedStateOf {
                                val layoutInfo = listState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) -1
                                else {
                                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                                    visibleItems.minByOrNull { item ->
                                        kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                                    }?.index ?: -1
                                }
                            }
                        }

                        // Cập nhật chế độ vào ViewModel ngay khi trượt qua giữa
                        LaunchedEffect(centerIndex) {
                            if (centerIndex != -1 && centerIndex in modes.indices) {
                                viewModel.setActiveMode(modes[centerIndex])
                            }
                        }

                        // Cuộn về đúng vị trí khi vừa vào màn hình dựa theo entryMode từ Home
                        LaunchedEffect(Unit) {
                            val targetIndex = modes.indexOf(entryMode)
                            if (targetIndex >= 0) {
                                listState.scrollToItem(targetIndex)
                            }
                        }

                        LazyRow(
                            state = listState,
                            flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = listState),
                            contentPadding = PaddingValues(horizontal = dynamicEdgePadding),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            itemsIndexed(modes) { index, mode ->
                                val isSelected = activeMode == mode
                                Text(
                                    text = mode.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Neutral900 else Color.Gray,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            // 🔥 FIX LỖI: CẬP NHẬT TRẠNG THÁI NGAY LẬP TỨC KHI CLICK
                                            viewModel.setActiveMode(mode)
                                            coroutineScope.launch { listState.animateScrollToItem(index) }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                ) {
                                    Icon(painterResource(R.drawable.ic_photo), null, modifier = Modifier.size(24.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Photo", fontSize = 14.sp, color = Color.Black)
                                }
                            }

                            Image(
                                painter = painterResource(id = R.drawable.ic_capture),
                                contentDescription = "Capture Photo",
                                modifier = Modifier.size(70.dp).clip(CircleShape).clickable { capturePhoto() }
                            )

                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { viewModel.toggleSnapTipsSheet(true) }
                                ) {
                                    Icon(painterResource(R.drawable.ic_tips), null, modifier = Modifier.size(24.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Snap tips", fontSize = 14.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setCapturedImageUri(null) },
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFD1D1D1)), modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Retake photo", color = Color(0xFF4A4A4A), fontSize = 16.sp, fontWeight = FontWeight.Medium) }

                    OutlinedButton(
                        onClick = {
                            if (activeMode == CameraEntryMode.CHAT_ATTACHMENT) {
                                navController.previousBackStackEntry?.savedStateHandle?.set("camera_result", capturedImageUri.toString())
                                navController.popBackStack()
                            } else {
                                val encodedUri = android.net.Uri.encode(capturedImageUri.toString())
                                navController.navigate("scanner_screen?imageUri=$encodedUri&mode=${activeMode.name}")
                            }
                        },
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Green900), modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Use photo", color = Green900, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
                }
            }
        }
    }

    AppBottomSheet(
        showSheet = showSnapTipsSheet,
        onDismiss = { viewModel.toggleSnapTipsSheet(false) },
        showDragHandle = false
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp).navigationBarsPadding()
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), contentAlignment = Alignment.Center) {
                Text("Snap tips", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                IconButton(onClick = { viewModel.toggleSnapTipsSheet(false) }, modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
            }

            Text("Do:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { TipItemCard(R.drawable.single_plant, "Single plant") }
                item { TipItemCard(R.drawable.leaf_issue, "Leaf issue") }
                item { TipItemCard(R.drawable.singer_flower, "Single flower") }
                item { TipItemCard(R.drawable.single_mushroom, "Single mushroom") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Don't:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item { TipItemCard(R.drawable.too_far, "Too far") }
                item { TipItemCard(R.drawable.too_close, "Too close") }
                item { TipItemCard(R.drawable.multiple_plants, "Multiple plants") }
                item { TipItemCard(R.drawable.too_dark, "Too dark") }
                item { TipItemCard(R.drawable.blurry, "Blurry") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.toggleSnapTipsSheet(false) },
                colors = ButtonDefaults.buttonColors(containerColor = Green900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Got it", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ScannerOverlayFrame() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cornerLength = 40.dp.toPx()
        val strokeWidth = 3.dp.toPx()
        val cornerRadius = 12.dp.toPx()
        val horizontalPadding = 40.dp.toPx()
        val verticalPadding = (size.height - (size.width - horizontalPadding * 2)) / 2
        val left = horizontalPadding; val top = verticalPadding; val right = size.width - horizontalPadding; val bottom = size.height - verticalPadding
        val path = Path()

        path.moveTo(left, top + cornerLength); path.lineTo(left, top + cornerRadius); path.quadraticBezierTo(left, top, left + cornerRadius, top); path.lineTo(left + cornerLength, top)
        path.moveTo(right - cornerLength, top); path.lineTo(right - cornerRadius, top); path.quadraticBezierTo(right, top, right, top + cornerRadius); path.lineTo(right, top + cornerLength)
        path.moveTo(right, bottom - cornerLength); path.lineTo(right, bottom - cornerRadius); path.quadraticBezierTo(right, bottom, right - cornerRadius, bottom); path.lineTo(right - cornerLength, bottom)
        path.moveTo(left + cornerLength, bottom); path.lineTo(left + cornerRadius, bottom); path.quadraticBezierTo(left, bottom, left, bottom - cornerRadius); path.lineTo(left, bottom - cornerLength)

        drawPath(path = path, color = Color.White, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

@Composable
fun TipItemCard(imageRes: Int, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = text,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}