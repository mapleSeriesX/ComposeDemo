package com.example.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    fun fetchData(): Flow<Int> =
        flow {
            repeat(3) {
                delay(100.milliseconds)
                emit(it) // 发射数据
            }
        }

    @Test
    fun test() {
        runBlocking {
            delay(20 * 1000)
            withContext(Dispatchers.IO) {
                fetchData()
                    .map { it * 2 }
                    .filter { it > 0 }
                    .collect { println(it) }
            }
        }
    }
}
