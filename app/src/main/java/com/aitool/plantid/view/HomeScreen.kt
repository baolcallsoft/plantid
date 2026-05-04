package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aitool.plantid.R
import com.aitool.plantid.components.PlantBannerSlider
import com.aitool.plantid.ui.Diagnose
import com.aitool.plantid.ui.Flower
import com.aitool.plantid.ui.Mushroom
import com.aitool.plantid.ui.Neutral200
import com.aitool.plantid.ui.Neutral300
import com.aitool.plantid.ui.Neutral900

data class FeatureItem(
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color
)

@Composable
fun HomeScreen() {
    val featureList = listOf(
        FeatureItem("Identify plant", "Know plant name immediately", R.drawable.ic_brandtree, MaterialTheme.colorScheme.primary),
        FeatureItem("Diagnose", "Check your plant's health", R.drawable.ic_diagnose, Diagnose),
        FeatureItem("Mushroom", "Check type and safety", R.drawable.ic_mushroom, Mushroom),
        FeatureItem("Flower", "Discover flower name", R.drawable.ic_flower, Flower)
    )

    Column(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .fillMaxSize()
    ) {
        PlantBannerSlider()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(featureList) { feature ->
                FeatureCard(
                    title = feature.title,
                    description = feature.description,
                    iconRes = feature.iconRes,
                    tintColor = feature.color,
                    onClick = {
                        if (feature.title == "Identify plant") {

                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    iconRes: Int,
    tintColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tintColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = tintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral900
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = Neutral300,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}