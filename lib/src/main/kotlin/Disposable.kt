package org.lantopia.kstream

interface Disposable {
    val disposed: Boolean
    fun dispose()
}

class Disposer(val handler: () -> Unit) : Disposable {
    private val _disposed = atomic(false)
    override val disposed = _disposed.value

    override fun dispose() {
        _disposed.compareAndSet(false, true)
        handler()
    }
}
