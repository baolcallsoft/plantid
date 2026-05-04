package com.aitool.plantid.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aitool.plantid.R
import com.aitool.plantid.view.MyPlantsScreen
import com.aitool.plantid.view.HomeScreen
import com.google.android.material.color.MaterialColors

@Composable
fun MainDashboardScreen(rootNavController: NavController, onNavigateToSetting: () -> Unit, onNavigateToChatbot: () -> Unit) {
    val nestedNavController = rememberNavController()

    Scaffold(
        topBar = {
            MyTopBar(navController = nestedNavController,
                onNavigateToSetting = onNavigateToSetting,
                onNavigateToChatbot = onNavigateToChatbot)
        },
        bottomBar = {
            MyBottomBar(nestedNavController, onScanClick = {})
        }
    ) { innerpadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = "home",
            modifier = Modifier
                .padding(innerpadding)
                .fillMaxSize(),

            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("my_plants") {
                MyPlantsScreen()
            }
        }
    }
}

@Composable
fun MyBottomBar(navController: NavController, onScanClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color.White,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
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

            AnimatedScanButton(onClick = onScanClick)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(navController: NavController, onNavigateToSetting: () -> Unit, onNavigateToChatbot: () -> Unit) {
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
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (currentRoute == "home") {
                IconButton(onClick = onNavigateToChatbot) {
                    Icon(painter = painterResource(R.drawable.ic_chatbot), contentDescription = null)
                }
                IconButton(onClick = onNavigateToSetting) {
                    Icon(painter = painterResource(R.drawable.ic_setting), contentDescription = null)
                }
            } else {
                IconButton(onClick = { /* Mở history */ }) {
                    Icon(painter = painterResource(R.drawable.ic_history), contentDescription = null)
                }
                IconButton(onClick = { /* Mở more */ }) {
                    Icon(painter = painterResource(R.drawable.ic_more_circle), contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = Color.Black,
            actionIconContentColor = Color.Black
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
    val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}