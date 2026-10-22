package com.example.core.common.analytics

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 结构化埋点分析接口
 */
interface AnalyticsHelper {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

/**
 * 默认空实现，防止未注入时崩溃
 */
object NoOpAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        // No-op
    }
}

/**
 * Compose 上下文 CompositionLocal
 */
val LocalAnalyticsHelper = staticCompositionLocalOf<AnalyticsHelper> { NoOpAnalyticsHelper }
