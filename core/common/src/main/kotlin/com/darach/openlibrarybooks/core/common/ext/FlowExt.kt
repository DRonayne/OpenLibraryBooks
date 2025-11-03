package com.darach.openlibrarybooks.core.common.ext

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.rx3.asObservable

/**
 * Extension functions for converting Kotlin Flow to RxJava types.
 *
 * These helpers facilitate interoperability between coroutines Flow and RxJava,
 * which is useful when working with libraries that use RxJava (like Room's RxJava support).
 */

/**
 * Converts a Flow to an RxJava Observable.
 *
 * The Observable will emit all values from the Flow and complete when the Flow completes.
 *
 * @return Observable that emits the same values as the Flow
 */
fun <T : Any> Flow<T>.toObservable(): Observable<T> = this.asObservable()

/**
 * Converts a Flow to an RxJava Single by taking the first emitted value.
 *
 * The Single will emit the first value from the Flow, or error if the Flow is empty.
 *
 * @return Single that emits the first value from the Flow
 */
suspend fun <T : Any> Flow<T>.toSingle(): Single<T> = Single.create { emitter ->
    kotlinx.coroutines.runBlocking {
        val value = this@toSingle.firstOrNull()
        if (value != null) {
            emitter.onSuccess(value)
        } else {
            emitter.onError(NoSuchElementException("Flow is empty"))
        }
    }
}

/**
 * Converts a Flow to a nullable value by collecting the first emission.
 *
 * Useful for converting Flow to a one-time result in RxJava chains.
 *
 * @return The first value from the Flow, or null if empty
 */
suspend fun <T> Flow<T>.firstOrNullSuspend(): T? = this.firstOrNull()
