package com.example.core.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkResultCallAdapterTest {
    @Test
    fun `NetworkResult Success map transforms data correctly`() {
        val result: NetworkResult<Int> = NetworkResult.Success(10)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is NetworkResult.Success)
        assertEquals(20, (mapped as NetworkResult.Success).data)
        assertEquals(10, result.getOrNull())
        assertEquals(10, result.getOrDefault(0))
    }

    @Test
    fun `NetworkResult Error map returns Error unchanged`() {
        val result: NetworkResult<String> = NetworkResult.Error(404, "Not Found")
        val mapped = result.map { "transformed" }

        assertTrue(mapped is NetworkResult.Error)
        assertEquals(404, (mapped as NetworkResult.Error).code)
        assertEquals("Not Found", mapped.message)
        assertNull(result.getOrNull())
        assertEquals("default", result.getOrDefault("default"))
    }

    @Test
    fun `NetworkResult Exception onFailure triggers callback`() =
        runBlocking {
            val exception = IOException("Connection reset")
            val result: NetworkResult<String> = NetworkResult.Exception(exception)

            var errorMsg: String? = null
            result.onFailure { code, message ->
                assertEquals(-1, code)
                errorMsg = message
            }

            assertEquals("Connection reset", errorMsg)
        }
}
