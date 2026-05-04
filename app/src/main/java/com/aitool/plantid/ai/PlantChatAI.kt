package com.aitool.plantid.ai


import com.aitool.plantid.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlantChatAI(val languageCode: String = "vi") {

    // 1. Cấu hình AI với "Lệnh bài" (System Instruction)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text("""
                You are PlantBot, an expert botanist. 
                Current language: $languageCode. 
                
                RULES:
                1. ALWAYS reply in the language matching the code: $languageCode.
                2. If the language is 'vi', reply in Vietnamese. If 'en', reply in English, etc.
                3. Only answer questions about plants and gardening. 
                4. If the user asks unrelated questions, politely refuse in $languageCode.
            """.trimIndent())
        }
    )

    // 2. Khởi tạo một phiên Chat (Để AI nhớ được ngữ cảnh các câu trước đó)
    private val chat = generativeModel.startChat()

    // 3. Hàm gửi tin nhắn và nhận câu trả lời
    suspend fun sendMessageToBot(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = chat.sendMessage(userMessage)
                response.text ?: "Xin lỗi, tôi không thể trả lời lúc này."
            } catch (e: Exception) {
                // Kiểm tra xem có phải lỗi quá tải 503 không
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("503") || errorMsg.contains("high demand")) {
                    "Máy chủ AI hiện đang quá tải do có quá nhiều người hỏi thăm cây cối! 😅 Bạn vui lòng chờ một lát rồi hỏi lại mình nhé."
                } else {
                    // Các lỗi khác như mất mạng, sai API key...
                    "Ui da, có vẻ như kết nối mạng bị lỗi rồi. Vui lòng kiểm tra lại Internet nhé!"
                }
            }
        }
    }
}