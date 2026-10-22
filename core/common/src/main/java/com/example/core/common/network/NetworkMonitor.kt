package com.example.core.common.network

import kotlinx.coroutines.flow.Flow

/**
 * 实时监控设备的网络连接状态
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
