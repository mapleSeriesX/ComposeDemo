package com.example.core.common.util

import android.util.Log
import com.example.core.common.BuildConfig

/**
 * 统一日志治理工具类：隔绝对 android.util.Log 的硬编码依赖，
 * 自动拦截 Release 环境日志输出并支持扩展 APM 上报。
 */
object AppLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        // Release 环境集中上报至 APM (如 Firebase Crashlytics / Sentry / 自研平台)
    }
}
