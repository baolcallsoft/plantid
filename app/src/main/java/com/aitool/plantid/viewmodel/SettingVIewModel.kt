package com.aitool.plantid.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel

class SettingViewModel : ViewModel() {

    val privacyPolicyUrl = "https://allsoftvietnam.com/privacy-policy"
    val developerMoreAppsUrl = "https://play.google.com/store/apps/developer?id=ALLSOFT&hl=en"

    fun createFeedbackMailtoUrl(feedback: String): String? {
        if (feedback.isBlank()) {
            return null // Trả về null nếu feedback trống
        }

        val subject = "Feedback Plant Identifier AI App"
        return "mailto:baolc.allsoft@gmail.com" +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(feedback)
    }

    fun getAppPlayStoreUrl(packageName: String): String {
        return "https://play.google.com/store/apps/details?id=$packageName"
    }
}