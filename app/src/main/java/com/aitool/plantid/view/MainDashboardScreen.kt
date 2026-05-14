package com.aitool.plantid.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aitool.plantid.R
import com.aitool.plantid.camera.CameraPermissionSheet
import com.aitool.plantid.components.AnimatedScanButton

@Composable
fun MainDashboardScreen(
    rootNavController: NavController,
    onNavigateToSetting: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onOpenCamera: (CameraEntryMode) -> Unit,
    onNavigateToHistory: () -> Unit // 🔥 THÊM THAM SỐ NÀY Ở ĐÂY ĐỂ FIX LỖI
) {
    val nestedNavController = rememberNavController()
    val context = LocalContext.current

    // 1. Quản lý trạng thái xin quyền cho nút Scan ở BottomBar
    var showPermissionSheet by remember { mutableStateOf(false) }
    var pendingMode by remember { mutableStateOf<CameraEntryMode?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingMode?.let { onOpenCamera(it) }
        }
        showPermissionSheet = false
    }

    val handleScanClick: (CameraEntryMode) -> Unit = { mode ->
        val permission = Manifest.permission.CAMERA
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            onOpenCamera(mode)
        } else {
            pendingMode = mode
            showPermissionSheet = true
        }
    }

    Box(modifier = Modifier.Companion.fillMaxSize()) {
        Scaffold(
            topBar = {
                MyTopBar(
                    navController = nestedNavController,
                    onNavigateToSetting = onNavigateToSetting,
                    onNavigateToChatbot = onNavigateToChatbot,
                    onNavigateToHistory = onNavigateToHistory // 🔥 TRUYỀN XUỐNG TOP BAR
                )
            },
            bottomBar = {
                MyBottomBar(nestedNavController, onScanClick = {
                    handleScanClick(CameraEntryMode.IDENTIFY_PLANT)
                })
            }
        ) { innerpadding ->
            NavHost(
                navController = nestedNavController,
                startDestination = "home",
                modifier = Modifier.Companion
                    .padding(innerpadding)
                    .fillMaxSize(),
                enterTransition = {
                    val isGoingToMyPlants = targetState.destination.route == "my_plants"
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> if (isGoingToMyPlants) fullWidth else -fullWidth }
                    )
                },
                exitTransition = {
                    val isGoingToMyPlants = targetState.destination.route == "my_plants"
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> if (isGoingToMyPlants) -fullWidth else fullWidth }
                    )
                },
                popEnterTransition = {
                    val isGoingToMyPlants = targetState.destination.route == "my_plants"
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> if (isGoingToMyPlants) fullWidth else -fullWidth }
                    )
                },
                popExitTransition = {
                    val isGoingToMyPlants = targetState.destination.route == "my_plants"
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> if (isGoingToMyPlants) -fullWidth else fullWidth }
                    )
                }
            ) {
                composable("home") {
                    HomeScreen(onFeatureClick = { mode -> onOpenCamera(mode) })
                }
                composable("my_plants") {
                    MyPlantsScreen()
                }
            }
        }

        CameraPermissionSheet(
            showSheet = showPermissionSheet,
            onDismiss = { showPermissionSheet = false },
            onAllow = {
                showPermissionSheet = false
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

// ... (MyBottomBar giữ nguyên)
@Composable
fun MyBottomBar(navController: NavController, onScanClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color.Companion.White,
        shadowElevation = 16.dp,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Box(
                modifier = Modifier.Companion.weight(1f),
                contentAlignment = Alignment.Companion.Center
            ) {
                CustomNavItem(
                    iconRes = R.drawable.ic_home,
                    label = "Home",
                    isSelected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            AnimatedScanButton(onClick = onScanClick)

            Box(
                modifier = Modifier.Companion.weight(1f),
                contentAlignment = Alignment.Companion.Center
            ) {
                CustomNavItem(
                    iconRes = R.drawable.ic_plantpot,
                    label = "My Plants",
                    isSelected = currentRoute == "my_plants",
                    onClick = {
                        navController.navigate("my_plants") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    navController: NavController,
    onNavigateToSetting: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToHistory: () -> Unit // 🔥 THÊM THAM SỐ NÀY CHO TOP BAR
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TopAppBar(
        title = {
            Text(
                text = when (currentRoute) {
                    "home" -> "Plant Identifier AI"
                    "my_plants" -> "My Plants"
                    else -> "Plant ID"
                },
                fontWeight = FontWeight.Companion.Bold
            )
        },
        actions = {
            if (currentRoute == "home") {
                IconButton(onClick = onNavigateToChatbot) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chatbot),
                        contentDescription = null
                    )
                }
                IconButton(onClick = onNavigateToSetting) {
                    Icon(
                        painter = painterResource(R.drawable.ic_setting),
                        contentDescription = null
                    )
                }
            } else {
                // 🔥 KHI Ở MÀN HÌNH "MY PLANTS", BẤM VÀO ĐÂY SẼ MỞ HISTORY
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null
                    )
                }
                IconButton(onClick = { /* Mở more */ }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_circle),
                        contentDescription = null
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = Color.Companion.Black,
            actionIconContentColor = Color.Companion.Black
        )
    )
}

@Composable
fun CustomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Companion.Gray

    Column(
        modifier = Modifier.Companion
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.Companion.size(26.dp)
        )
        Spacer(modifier = Modifier.Companion.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Medium,
            maxLines = 1,
            overflow = TextOverflow.Companion.Ellipsis
        )
    }
}