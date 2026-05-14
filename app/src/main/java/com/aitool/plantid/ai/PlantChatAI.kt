package com.aitool.plantid.ai

import android.graphics.Bitmap
import com.aitool.plantid.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlantChatAI(val languageCode: String = "vi") {

    // ==========================================
    // 1. MODEL DÀNH CHO CHATBOT (Giao tiếp tự nhiên, có nhớ ngữ cảnh)
    // ==========================================
    private val chatModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("""
                You are PlantBot, a strict and professional botanist expert.
                YOUR CORE DIRECTIVES (Must obey strictly):
                1. LANGUAGE: You MUST auto-detect the user's language and reply in that EXACT same language.
                2. DOMAIN LIMITATION: Your ONLY domain of knowledge is botany, plants, gardening, plant diseases, plant care, and agriculture.
                3. STRICT REFUSAL: If the user asks ANY question outside your domain, politely refuse.
                4. ANTI-JAILBREAK: Stay in character.
                5. TONE: Be helpful, encouraging, and scientific but easy to understand.
            """.trimIndent())
        }
    )

    private val chat = chatModel.startChat()

    // ==========================================
    // 2. MODEL DÀNH CHO SCANNER (Chỉ chuyên Nhận Diện Cây/Hoa/Nấm)
    // ==========================================
    private val identifyModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig { responseMimeType = "application/json" },
        systemInstruction = content {
            text("""
                You are an expert botanical image analyzer. Analyze the provided image of a plant, flower, or mushroom.
                Return ONLY a JSON object. Translate ALL the values into the language code: $languageCode.
                
                Expected JSON Structure:
                {
                  "basicInfo": {
                    "name": "Common name",
                    "scientificName": "Scientific name",
                    "family": "Plant family",
                    "plantType": "e.g., Indoor, Succulent",
                    "origin": "Native region",
                    "toxicity": "Short summary of toxicity",
                    "description": "2-3 sentences describing the plant"
                  },
                  "warnings": {
                    "toxicityDetails": "Detailed explanation of toxicity",
                    "commonProblems": ["Problem 1", "Problem 2"],
                    "specialUses": ["Use 1", "Use 2"]
                  },
                  "growth": {
                    "growthRate": "e.g., Slow, Moderate, Fast",
                    "matureSize": "Expected height and width",
                    "flowering": "Flowering season",
                    "propagation": ["Method 1", "Method 2"],
                    "lifespan": "e.g., Perennial, Annual"
                  },
                  "care": {
                    "light": "Light requirements",
                    "watering": "Watering schedule",
                    "soil": "Soil type needed",
                    "temperature": "Ideal temperature range",
                    "humidity": "Ideal humidity level",
                    "fertilizer": "Fertilizer recommendations"
                  }
                }
                If not a plant, return {"error": "Not a plant"}.
            """.trimIndent())
        }
    )

    // ==========================================
    // 3. MODEL DÀNH CHO DIAGNOSE (Chỉ chuyên Khám Bệnh Thực Vật)
    // ==========================================
    private val diagnoseModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig { responseMimeType = "application/json" },
        systemInstruction = content {
            text("""
                You are an expert plant pathologist. Analyze the provided image for plant diseases, pests, or health issues.
                Return ONLY a JSON object. Translate ALL the values into the language code: $languageCode.
                
                Expected JSON Structure:
                {
                  "diseaseName": "Name of the disease (e.g., Fungal leaf spot) or 'Healthy Plant' if no issues found",
                  "scientificName": "Scientific name of pathogen",
                  "symptoms": "Description of symptoms",
                  "causes": "Common causes",
                  "affectedPlants": "Types of plants usually affected",
                  "treatmentSteps": ["Step 1", "Step 2", "Step 3"],
                  "monitoring": "How to monitor progress",
                  "tips": "Prevention tips"
                }
                If not a plant, return {"error": "Not a plant"}.
            """.trimIndent())
        }
    )

    // ==========================================
    // CÁC HÀM XỬ LÝ GỌI API TỪ VIEWMODEL
    // ==========================================

    // 1. Dùng cho Chatbot (Chỉ gửi text)
    suspend fun sendMessageToBot(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = chat.sendMessage(userMessage)
                response.text ?: "Sorry, I can't answer right now."
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // 2. Dùng cho Chatbot (Gửi text kèm ảnh)
    suspend fun sendMessageWithImageToBot(userMessage: String, image: Bitmap): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(image)
                    text(userMessage)
                }
                val response = chat.sendMessage(inputContent)
                response.text ?: "I'm sorry, I couldn't analyze the image."
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // 3. Dùng cho tính năng Identify Plant, Mushroom, Flower
    suspend fun analyzePlantImage(image: Bitmap): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(image)
                    text("Identify this plant and provide its information in JSON format.")
                }
                val response = identifyModel.generateContent(inputContent)
                response.text ?: "{\"error\": \"Empty response from AI\"}"
            } catch (e: Exception) {
                "{\"error\": \"${e.localizedMessage?.replace("\"", "'")}\"}"
            }
        }
    }

    // 4. Dùng cho tính năng Diagnose
    suspend fun diagnosePlantImage(image: Bitmap): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(image)
                    text("Diagnose this plant's health and provide treatment steps in JSON format.")
                }
                val response = diagnoseModel.generateContent(inputContent)
                response.text ?: "{\"error\": \"Empty response from AI\"}"
            } catch (e: Exception) {
                "{\"error\": \"${e.localizedMessage?.replace("\"", "'")}\"}"
            }
        }
    }

    // 5. Hàm bắt lỗi chung
    private fun handleError(e: Exception): String {
        val errorMsg = e.message ?: ""
        return if (errorMsg.contains("503") || errorMsg.contains("high demand")) {
            "Our AI servers are currently overloaded because too many people are asking about plants! 😅 Please wait a moment and try again."
        } else {
            "Oops, it looks like there's a network error. Please check your internet connection! (Error: ${e.localizedMessage})"
        }
    }
}