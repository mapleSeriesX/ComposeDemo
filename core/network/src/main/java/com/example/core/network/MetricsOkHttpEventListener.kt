package com.example.core.network

import android.util.Log
import okhttp3.Call
import okhttp3.EventListener
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetricsOkHttpEventListener @Inject constructor() : EventListener() {

    private data class CallMetrics(
        val url: String,
        val startTimeMs: Long = System.currentTimeMillis(),
        var dnsStartMs: Long = 0,
        var dnsDurationMs: Long = 0,
        var connectStartMs: Long = 0,
        var connectDurationMs: Long = 0,
        var totalDurationMs: Long = 0
    )

    private val metricsMap = ConcurrentHashMap<Call, CallMetrics>()

    override fun callStart(call: Call) {
        val url = call.request().url.toString()
        metricsMap[call] = CallMetrics(url = url)
    }

    override fun dnsStart(call: Call, domainName: String) {
        metricsMap[call]?.dnsStartMs = System.currentTimeMillis()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        metricsMap[call]?.let { metrics ->
            if (metrics.dnsStartMs > 0) {
                metrics.dnsDurationMs = System.currentTimeMillis() - metrics.dnsStartMs
            }
        }
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        metricsMap[call]?.connectStartMs = System.currentTimeMillis()
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: okhttp3.Protocol?
    ) {
        metricsMap[call]?.let { metrics ->
            if (metrics.connectStartMs > 0) {
                metrics.connectDurationMs = System.currentTimeMillis() - metrics.connectStartMs
            }
        }
    }

    override fun callEnd(call: Call) {
        metricsMap.remove(call)?.let { metrics ->
            val totalTime = System.currentTimeMillis() - metrics.startTimeMs
            Log.d(
                TAG,
                "[APM Network] URL: ${metrics.url} | Total: ${totalTime}ms | DNS: ${metrics.dnsDurationMs}ms | Connect: ${metrics.connectDurationMs}ms"
            )
        }
    }

    override fun callFailed(call: Call, ioe: java.io.IOException) {
        metricsMap.remove(call)?.let { metrics ->
            val totalTime = System.currentTimeMillis() - metrics.startTimeMs
            Log.e(
                TAG,
                "[APM Network FAIL] URL: ${metrics.url} | Total: ${totalTime}ms | Error: ${ioe.localizedMessage}"
            )
        }
    }

    companion object {
        private const val TAG = "OkHttpAPM"
    }
}
