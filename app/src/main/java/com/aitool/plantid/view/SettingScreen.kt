package com.aitool.plantid.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aitool.plantid.R
import com.aitool.plantid.components.AppBottomSheet
import com.aitool.plantid.ui.Neutral400
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral200
import com.aitool.plantid.viewmodel.SettingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onNavigateToLanguage: () -> Unit
) {
    val context = LocalContext.current
    val settingViewModel: SettingViewModel = viewModel()

    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    // --- TRẠNG THÁI CHO BOTTOM SHEET ---
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("") }

    var showRateSheet by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(5) }

    // Chống click đúp
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val safeClick: (() -> Unit) -> Unit = { action ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > 500L) {
            lastClickTime = currentTime
            action()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text("Setting", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            MinimalSettingOption(
                iconId = R.drawable.ic_language,
                title = "Languages",
                onClick = { safeClick { onNavigateToLanguage() } }
            )

            MinimalSettingOption(
                iconId = R.drawable.ic_privacy,
                title = "Privacy Policy",
                onClick = {
                    safeClick {
                        val url = settingViewModel.privacyPolicyUrl
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            )

            MinimalSettingOption(
                iconId = R.drawable.ic_star,
                title = "Rate Us",
                onClick = {
                    safeClick {
                        selectedRating = 5
                        showRateSheet = true
                    }
                }
            )

            MinimalSettingOption(
                iconId = R.drawable.ic_feedback,
                title = "Feedback",
                onClick = {
                    safeClick {
                        showFeedbackSheet = true
                    }
                }
            )

            MinimalSettingOption(
                iconId = R.drawable.ic_share,
                title = "Share App",
                onClick = {
                    safeClick {
                        val appUrl = settingViewModel.getAppPlayStoreUrl(context.packageName)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, appUrl)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                }
            )

            MinimalSettingOption(
                iconId = R.drawable.ic_more_app,
                title = "More App",
                onClick = {
                    safeClick {
                        val url = settingViewModel.developerMoreAppsUrl
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version $versionName",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
            )
        }

        // ==========================================
        // 1. BOTTOM SHEET: FEEDBACK (Dùng AppBottomSheet)
        // ==========================================
        AppBottomSheet(
            showSheet = showFeedbackSheet,
            onDismiss = { showFeedbackSheet = false },
            showDragHandle = true,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 5.dp, bottom = 20.dp)
            ) {
                Text(
                    text = "Feedback",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Typing your feedback here", color = Color.Gray) },
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Button(
                        onClick = { showFeedbackSheet = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0), contentColor = Color.Black)
                    ) {
                        Text(text = "Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val mailtoUrl = settingViewModel.createFeedbackMailtoUrl(feedbackText)

                            if (mailtoUrl != null) {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse(mailtoUrl)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Send feedback via..."))
                                    Toast.makeText(context, "Feedback sent!", Toast.LENGTH_SHORT).show()
                                    showFeedbackSheet = false
                                    feedbackText = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green900)
                    ) {
                        Text(text = "Submit", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }

        // ==========================================
        // 2. BOTTOM SHEET: RATE US (Dùng AppBottomSheet)
        // ==========================================
        AppBottomSheet(
            showSheet = showRateSheet,
            onDismiss = { showRateSheet = false },
            containerColor = Color.White,
            showDragHandle = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding( bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rating),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Rate your experience",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Rate your experience and help us\nmake it even better!",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..5).forEach { index ->
                        IconButton(onClick = { selectedRating = index }, modifier = Modifier.size(46.dp)) {
                            Icon(
                                painter = painterResource(id = if (index <= selectedRating) R.drawable.ic_star_gold else R.drawable.ic_star_gray),
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showRateSheet = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
                    ) {
                        Text(text = "Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (selectedRating >= 4) {
                                val appId = context.packageName
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId"))
                                intent.setPackage("com.android.vending")

                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Thanks for the review!", Toast.LENGTH_SHORT).show()
                            }
                            showRateSheet = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green900) // Đổi sang màu Plant ID
                    ) {
                        Text("Send Review", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// Khối Option giao diện tối giản
@Composable
fun MinimalSettingOption(iconId: Int, title: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                tint = Neutral400,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Neutral200,
                modifier = Modifier.size(24.dp)
            )
        }

        HorizontalDivider(
            color = Color(0xFFF5F5F5),
            thickness = 1.dp
        )
    }
}