/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.igalia.wolvic.browser.components

import com.igalia.wolvic.browser.api.IResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.CancellableOperation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Wait for a GeckoResult to be complete in a co-routine.
 */
suspend fun <T> IResult<T>.await() = suspendCoroutine<T?> { continuation ->
    then({
        continuation.resume(it)
        IResult.create<Void>()
    }, {
        continuation.resumeWithException(it)
        IResult.create<Void>()
    })
}

/**
 * Converts a [GeckoResult] to a [CancellableOperation].
 */
fun <T> IResult<T>.asCancellableOperation(): CancellableOperation {
    val res = this
    return object : CancellableOperation {
        override fun cancel(): Deferred<Boolean> {
            val result = CompletableDeferred<Boolean>()
            res.cancel().then({
                result.complete(it ?: false)
                IResult.create<Void>()
            }, { throwable ->
                result.completeExceptionally(throwable)
                IResult.create<Void>()
            })
            return result
        }
    }
}

/**
 * Create a GeckoResult from a co-routine.
 */
@Suppress("TooGenericExceptionCaught")
fun <T> CoroutineScope.launchGeckoResult(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
) = IResult.create<T>().apply {
    launch(context, start) {
        try {
            val value = block()
            complete(value)
        } catch (exception: Throwable) {
            completeExceptionally(exception)
        }
    }
}
