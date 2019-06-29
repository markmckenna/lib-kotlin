package org.lantopia.kstream

import java.util.concurrent.atomic.AtomicBoolean

fun atomic(bool: Boolean) = AtomicBoolean(bool)

var AtomicBoolean.value
    get() = get()
    set(value) = set(value)

fun AtomicBoolean.setAndDo(old: Boolean, new: Boolean, action: (new: Boolean) -> Unit) =
        compareAndSet(old, new) and { action(new) }