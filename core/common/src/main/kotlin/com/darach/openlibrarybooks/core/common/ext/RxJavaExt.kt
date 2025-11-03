package com.darach.openlibrarybooks.core.common.ext

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * Extension functions for converting RxJava types to Kotlin Flow and suspend functions.
 */

/**
 * Converts an RxJava Observable to a Kotlin Flow.
 *
 * The Flow will emit all values from the Observable and complete when the Observable completes.
 *
 * @return Flow that emits the same values as the Observable
 */
fun <T : Any> Observable<T>.toFlow(): Flow<T> = this.asFlow()

/**
 * Converts an RxJava Single to a suspend function that returns the value.
 *
 * Suspends until the Single emits a value or an error occurs.
 *
 * @return The value emitted by the Single
 * @throws Exception if the Single emits an error
 */
suspend fun <T : Any> Single<T>.awaitValue(): T = this.await()

/**
 * Converts an RxJava Completable to a suspend function.
 *
 * Suspends until the Completable completes or an error occurs.
 *
 * @throws Exception if the Completable emits an error
 */
suspend fun Completable.awaitCompletion() = this.await()

/**
 * Maps an Observable's errors to a default value.
 *
 * If the Observable emits an error, it will emit the default value instead.
 *
 * @param defaultValue The value to emit on error
 * @return Observable that emits the default value on error
 */
fun <T : Any> Observable<T>.onErrorReturnDefault(defaultValue: T): Observable<T> = this.onErrorReturn { defaultValue }

/**
 * Maps a Single's errors to a default value.
 *
 * If the Single emits an error, it will emit the default value instead.
 *
 * @param defaultValue The value to emit on error
 * @return Single that emits the default value on error
 */
fun <T : Any> Single<T>.onErrorReturnDefault(defaultValue: T): Single<T> = this.onErrorReturn { defaultValue }
