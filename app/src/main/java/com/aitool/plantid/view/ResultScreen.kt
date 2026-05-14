package com.aitool.plantid.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aitool.plantid.R
import com.aitool.plantid.ui.Green900
import com.aitool.plantid.ui.Neutral100
import org.json.JSONObject

enum class ResultType(val displayName: String) {
    IDENTIFY_PLANT("Identify plant"),
    MUSHROOM("Mushroom"),
    FLOWER("Flower"),
    DIAGNOSE_PLANT("Diagnose plant")
}

data class PlantModel(
    val name: String = "Unknown", val scientificName: String = "-", val family: String = "-",
    val plantType: String = "-", val origin: String = "-", val toxicity: String = "-", val description: String = "-",
    val toxicityDetails: String = "-", val commonProblems: String = "-", val specialUses: String = "-",
    val growthRate: String = "-", val matureSize: String = "-", val flowering: String = "-",
    val propagation: String = "-", val lifespan: String = "-", val light: String = "-",
    val watering: String = "-", val soil: String = "-", val temperature: String = "-",
    val humidity: String = "-", val fertilizer: String = "-"
)

data class DiagnoseModel(
    val diseaseName: String = "Healthy", val scientificName: String = "-", val symptoms: String = "-",
    val causes: String = "-", val affectedPlants: String = "-", val treatmentSteps: List<String> = emptyList(),
    val monitoring: String = "-", val tips: String = "-"
)

fun parsePlantJson(json: String): PlantModel {
    return try {
        val root = JSONObject(json)
        val basic = root.optJSONObject("basicInfo") ?: JSONObject()
        val warn = root.optJSONObject("warnings") ?: JSONObject()
        val growth = root.optJSONObject("growth") ?: JSONObject()
        val care = root.optJSONObject("care") ?: JSONObject()

        fun getBulletList(obj: JSONObject, key: String): String {
            val arr = obj.optJSONArray(key) ?: return "-"
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.optString(i))
            return list.joinToString("\n• ", prefix = "• ")
        }

        fun getCommaList(obj: JSONObject, key: String): String {
            val arr = obj.optJSONArray(key) ?: return "-"
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.optString(i))
            return list.joinToString(", ")
        }

        PlantModel(
            name = basic.optString("name", "Unknown Plant"), scientificName = basic.optString("scientificName", "-"),
            family = basic.optString("family", "-"), plantType = basic.optString("plantType", "-"),
            origin = basic.optString("origin", "-"), toxicity = basic.optString("toxicity", "-"),
            description = basic.optString("description", "-"), toxicityDetails = warn.optString("toxicityDetails", "-"),
            commonProblems = getBulletList(warn, "commonProblems"), specialUses = getBulletList(warn, "specialUses"),
            growthRate = growth.optString("growthRate", "-"), matureSize = growth.optString("matureSize", "-"),
            flowering = growth.optString("flowering", "-"), propagation = getCommaList(growth, "propagation"),
            lifespan = growth.optString("lifespan", "-"), light = care.optString("light", "-"),
            watering = care.optString("watering", "-"), soil = care.optString("soil", "-"),
            temperature = care.optString("temperature", "-"), humidity = care.optString("humidity", "-"),
            fertilizer = care.optString("fertilizer", "-")
        )
    } catch (e: Exception) { PlantModel(name = "Parsing Error") }
}

fun parseDiagnoseJson(json: String): DiagnoseModel {
    return try {
        val root = JSONObject(json)
        val stepsArr = root.optJSONArray("treatmentSteps")
        val steps = mutableListOf<String>()
        if (stepsArr != null) {
            for (i in 0 until stepsArr.length()) steps.add(stepsArr.optString(i))
        }
        DiagnoseModel(
            diseaseName = root.optString("diseaseName", "Unknown"), scientificName = root.optString("scientificName", "-"),
            symptoms = root.optString("symptoms", "-"), causes = root.optString("causes", "-"),
            affectedPlants = root.optString("affectedPlants", "-"), treatmentSteps = steps,
            monitoring = root.optString("monitoring", "-"), tips = root.optString("tips", "-")
        )
    } catch (e: Exception) { DiagnoseModel(diseaseName = "Parsing Error") }
}

// ==========================================
// 2. GIAO DIỆN CHÍNH
// ==========================================
@Composable
fun ResultScreen(
    type: ResultType,
    jsonString: String,
    imageUri: String,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // 1. ẢNH HEADER
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(380.dp)
        )

        // Nút Close
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(28.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // 2. NỘI DUNG CHI TIẾT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 260.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            // 🔥 CẬP NHẬT 2: Phân luồng hiển thị giao diện theo Type
            when (type) {
                // Hoa, Nấm, Cây cối đều dùng chung giao diện Thông tin cây
                ResultType.IDENTIFY_PLANT, ResultType.MUSHROOM, ResultType.FLOWER -> {
                    val data = parsePlantJson(jsonString)
                    PlantInfoContent(data)
                }
                // Chỉ riêng Khám bệnh mới dùng giao diện Điều trị
                ResultType.DIAGNOSE_PLANT -> {
                    val data = parseDiagnoseJson(jsonString)
                    DiagnoseContent(data)
                }
            }
        }

        // 3. BOTTOM BAR
        ResultBottomBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ResultBottomBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Quét lại
            OutlinedButton(
                onClick = { /* Re-scan logic */ },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                contentPadding = PaddingValues(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Icon(painterResource(R.drawable.ic_scan), contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }

            // Nút Save
            Button(
                onClick = { /* Save logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Green900),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Save to My Plants", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}