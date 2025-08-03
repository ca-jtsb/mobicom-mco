package com.mobicom.s16.mco.util

import kotlinx.coroutines.delay

suspend fun <T> retryIO(
    times: Int = 3,
    delayTime: Long = 1000L,
    block: suspend () -> T
): T {
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            println("🔁 Retry attempt ${attempt + 1} failed: ${e.message}")
            delay(delayTime)
        }
    }
    return block() // final attempt
}
