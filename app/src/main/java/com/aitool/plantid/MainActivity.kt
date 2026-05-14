package com.aitool.plantid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.aitool.plantid.components.AppNavigation
import com.aitool.plantid.components.TopNoInternetBanner
import com.aitool.plantid.components.rememberNetworkState
import com.aitool.plantid.ui.PlantIDTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )

        setContent {
            PlantIDTheme {
                val context = LocalContext.current
                val isOnline by rememberNetworkState(context)

                val rootNavController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize()) {


                    AppNavigation(rootNavController = rootNavController)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .systemBarsPadding()
                    ) {
                        TopNoInternetBanner(isOffline = !isOnline)
                    }
                }
            }
        }
    }
}