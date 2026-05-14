package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.data.CollectionWithPlants
import com.aitool.plantid.data.SavedPlantEntity
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.viewmodel.MyPlantsViewModel

@Composable
fun MyPlantsScreen(
    viewModel: MyPlantsViewModel = hiltViewModel()
) {
    val collections by viewModel.collectionsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // Nền xám cực nhẹ như thiết kế
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Collection (${collections.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (collections.isEmpty()) {
            EmptyCollectionContent(onCreateClick = { viewModel.createNewCollection("New Collection") })
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(collections) { item ->
                    CollectionCard(
                        item = item,
                        onDelete = { viewModel.deleteCollection(item.collection.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionCard(item: CollectionWithPlants, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // PHẦN ẢNH (HÌNH VUÔNG)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (item.savedPlants.isEmpty()) {
                // Trạng thái trống: Hiện icon mầm cây
                Icon(
                    painter = painterResource(id = R.drawable.ic_plantpot),
                    contentDescription = null,
                    tint = Color(0xFFE0E0E0),
                    modifier = Modifier.size(64.dp)
                )
            } else {
                // Trạng thái có cây: Hiện Collage (Ghép ảnh)
                CollectionCollage(plants = item.savedPlants)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PHẦN CHỮ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.collection.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${item.savedPlants.size} plants",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun CollectionCollage(plants: List<SavedPlantEntity>) {
    // Chỉ lấy tối đa 4 ảnh để làm Collage
    val images = plants.take(4)

    Box(modifier = Modifier.fillMaxSize()) {
        if (images.size == 1) {
            // 1 cây: Full 1 ảnh
            PlantImage(uri = images[0].imageUri, modifier = Modifier.fillMaxSize())
        } else {
            // Nhiều cây: Chia lưới 2x2
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    PlantImage(uri = images[0].imageUri, modifier = Modifier.weight(1f).fillMaxHeight())
                    if (images.size >= 2) {
                        PlantImage(uri = images[1].imageUri, modifier = Modifier.weight(1f).fillMaxHeight())
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F5F5)))
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    if (images.size >= 3) {
                        PlantImage(uri = images[2].imageUri, modifier = Modifier.weight(1f).fillMaxHeight())
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F5F5)))
                    }
                    if (images.size >= 4) {
                        PlantImage(uri = images[3].imageUri, modifier = Modifier.weight(1f).fillMaxHeight())
                    } else {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F5F5)))
                    }
                }
            }
        }
    }
}

@Composable
fun PlantImage(uri: String, modifier: Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier.padding(1.dp), // Tạo đường kẻ trắng giữa các ảnh
        contentScale = ContentScale.Crop
    )
}

@Composable
fun EmptyCollectionContent(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_image),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No collections yet", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Create collections to manage your\nplants better.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = Green900),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp).padding(horizontal = 20.dp)
        ) {
            Text("Create your first collection", fontWeight = FontWeight.Bold)
        }
    }
}