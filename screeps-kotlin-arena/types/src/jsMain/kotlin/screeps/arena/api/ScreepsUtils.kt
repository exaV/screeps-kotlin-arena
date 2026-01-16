package screeps.arena.api

inline val <T> Constant<T>.value: T get() = this.asDynamic().unsafeCast<T>()
