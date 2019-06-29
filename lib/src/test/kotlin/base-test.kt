package org.lantopia.kstream

class BaseTest {
    fun `function-based create should work`() {
        val stream = Stream<Unit> { emit -> emit(Unit) }

        var emitted = false
        stream.pull { emitted = true }

        assert(emitted)
    }
}