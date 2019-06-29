package org.lantopia.kstream

import java.util.concurrent.atomic.AtomicBoolean

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

