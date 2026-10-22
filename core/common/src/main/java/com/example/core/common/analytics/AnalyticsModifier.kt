package com.example.core.common.analytics

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * 声明式无侵入点击埋点扩展
 */
fun Modifier.trackClick(
    eventName: String,
    params: Map<String, Any> = emptyMap(),
    onClick: () -> Unit
): Modifier = composed {
    val analyticsHelper = LocalAnalyticsHelper.current
    this.clickable {
        analyticsHelper.logEvent(eventName, params)
        onClick()
    }
}
