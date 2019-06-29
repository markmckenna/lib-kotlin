package org.lantopia.kstream

fun Any.unit() = Unit

fun expr(value: Any): Boolean = true

operator fun Boolean.and(action: () -> Unit) = if (this) action() else unit()
infix fun Boolean.or(action: () -> Unit) = if (this) unit() else action()
