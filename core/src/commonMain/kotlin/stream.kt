package org.lantopia.libkotlin

import kotlinx.coroutines.*

/* Given that Kotlin already has Result<T> with map, fold, etc. methods on it; what we probably want here
 is a higher-order enclosure that helps us do more with that result type.

 step 1: deferred result processing.  Instead of getting the result now, we get the result later, but we hook
 up the result processing now.  So we have a 'future' style wrapper type that can do all of the things that Result
 can do but it keeps them within the Future space.  Anywhere you'd do Result<T>.blah() and get Result<?> back, you
 can do Future<T>.blah() and get Future<?> back instead.  Anywhere you'd do Result<T>.blah() and get <?> back, you
 can do Future<T>.blah() and pass in a deferred call which receives <?>.  The dynamics are the same except that
 everything happens 'later'.  Since Result wraps up thrown errors into a no-throw concept, this simplifies future
 handling quite a bit.  We should use coroutines under the hood, and so we should have the deferred calls here be
 able to be declared as 'suspend' and do coroutine things.

 Q: Can we write an extension method that applies only to a suspend function returning Result?

 step 2: repeated result processing.  Instead of dealing with individual results, you can deal with batches of results
 in positionally stable ways.  We have a 'batch' wrapper that matches the Result API, just like above--calls that
 would return Result instead return Batch; calls that would return <?> instead accept a deferred call that receives <?>
 which will be invoked multiple times.

 One useful thing is to represent a stream of zero or more results.  Downstream consumers act as adapters around the
 stream, applying a modifier to each eventual
 */

// 1. someone defines a coroutine as a suspend function
// 2. someone launches a coroutine using a coroutine primitive, which returns Deferred<T>
// 3. we want to operate on the coroutine in progress, so our fun extends Deferred

/** Allow the platform to define an application-wide threading model that we adhere to */
expect fun <T> async(operation: suspend CoroutineScope.() -> T): Deferred<T>

suspend fun echoLater(): String {
    delay(1000)
    return "hello"
}

fun main() {
    val deferredEcho = async { echoLater() }
    val deferredFirstEchoedCharacter = deferredEcho.map { it.substring(1) }
}

/** Define [map] for deferred calls. This allows deferred calls to be followed in a promise-like way. */
fun <T, U> Deferred<T>.map(operation: (T) -> U): Deferred<U> = async { operation(this@map.await()) }


typealias EventSink<T> = (T) -> Unit

/** A subscription token that can be used to cancel a subscription */
interface Subscription : Disposable {
    /** @return true iff this subscription is already cancelled */
    val closed: Boolean
}

/** Represents a typed stream of zero or more events. */
interface Stream<T> {
    /** Start receiving events from this stream. */
    fun pull(sink: EventSink<T>): Subscription
}

/** A passive, unbuffered stream whose source is a simple function */
fun <T> Stream(source: (EventSink<T>) -> Unit) = object : Stream<T> {
    override fun pull(sink: EventSink<T>) = ProxySubscription(sink).also { source(it::invoke) }
}

fun <T> Stream(source: EventSink<T>.() -> Unit) = object : Stream<T> {
    override fun pull(sink: EventSink<T>) = ProxySubscription(sink).also { source(it::invoke) }
}

/** Subscription handler that works by proxying away listeners */
internal class ProxySubscription<T>(val sink: EventSink<T>) : Subscription {
    override var closed = false

    operator fun invoke(event: T) {
        if (!closed) sink(event)
    }

    override fun close() {
        closed = true
    }
}

