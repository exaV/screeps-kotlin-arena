package sourcemaps

import screeps.api.Record

// this function must not be inline and and must be in a different file that SourceMapResolver
fun <T> runWithSourceMapSupport(rethrow: Boolean = false, block: () -> T): T? {
    try {
        return block()
    } catch (t: Throwable){
        SourceMapResolver.registry = SourceMapRegistryRuntimeLocator
        println(SourceMapResolver.printMappedStacktrace(t))
        if (rethrow){
            throw t
        }
        return null
    }
}

//./node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/season1/constructandcontrol/ConstructAndControl.export.mjs
//module not found: /user/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/season1/spawnstrike/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/sourcemaps/SourceMapRegistry.mjs
//@JsModule("./node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/sourcemaps/SourceMapRegistry.mjs")
@JsModule("./SourceMapRegistry.mjs")
external object SourceMapRegistryRuntimeLocator : SourceMapRegistry {
    override val rawSourceMaps: Record<String, String>
    override val sourceRoot: String
}