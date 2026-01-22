@file:Suppress("UnsafeCastFromDynamic")

package sourcemaps

import screeps.api.Record
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class SourceMapResolverTest {

    init {
        SourceMapResolver.registry = SourceMapRegistryTestHelper
    }

    @Test
    fun maps_kotlin_frames_and_leaves_game_frames() {
        // Arrange: exact JS-native t.stack
        val nativeStack = """
            $: whoopsie
                at captureStack (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/kotlin-kotlin-stdlib/kotlin/js/coreRuntime.mjs:207:11)
                at init_kotlin_RuntimeException (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/kotlin-kotlin-stdlib/kotlin/exceptions.mjs:84:3)
                at $.t5 (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/kotlin-kotlin-stdlib/kotlin/exceptions.mjs:97:9)
                at $.wd (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/tutorial/Tutorials.mjs:199:40)
                at loop${'$'}lambda (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/tutorial/Main.mjs:37:33)
                at runWithSourceMapSupport (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/sourcemaps/SourceMapSupport.mjs:12:12)
                at loop (file:///user/node_modules/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/tutorial/Main.mjs:13:3)
                at <isolated-vm>:14:13
        """.trimIndent()


        // Act
        val mapped = try {
            SourceMapResolver.mappedStacktrace(nativeStack)
        } catch (e: Throwable) {
            fail("Mapping threw unexpectedly: ${e.message}")
        }

        assertTrue(
            mapped.contains("Tutorials.kt:"),
            "Expected a mapped Kotlin source reference (Tutorials.kt) in:\n$mapped"
        )

        println(mapped)
    }
}

// hacky way to import the correct module
@JsModule("../../../../../../../build/js/packages/screeps-kotlin-arena-starter/kotlin/screeps-kotlin-arena-starter/sourcemaps/SourceMapRegistry.mjs")
@JsNonModule
external object SourceMapRegistryTestHelper: SourceMapRegistry {
    override val rawSourceMaps: Record<String, String>
    override val sourceRoot: String
}