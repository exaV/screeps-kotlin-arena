@file:Suppress("UnsafeCastFromDynamic")

package sourcemaps

import screeps.api.Record
import screeps.api.get

external interface SourceMapRegistry {
    val rawSourceMaps: Record<String, String>
    val sourceRoot: String
}

object SourceMapResolver {

    var registry: SourceMapRegistry? = null

    /** Cache of parsed SourceMap per generated file key (e.g. "SpawnStrike.mjs") */
    private val mapCache = mutableMapOf<String, SourceMap>()

    fun printMappedStacktrace(t: Throwable): String {
        try {
            val jsError = t.asDynamic()
            val nativeStack = jsError?.stack as String
            return mappedStacktrace(nativeStack)
        } catch (inner: Throwable) {
            println(t.stackTraceToString())
            println("original exception above")
            throw RuntimeException("failed to resolve stacktrace", inner)
        }
    }

    fun mappedStacktrace(stack: String): String {
        val lines = stack.lines()
        if (lines.isEmpty()) return stack

        val out = ArrayList<String>(lines.size)
        out += lines.first()

        for (i in 1 until lines.size) {
            val line = lines[i].replace("file:///game", "game")
            val frame = parseFrame(line)
            if (frame == null) {
                out += line
                continue
            }

            val key = basename(frame.file)
            val sm = getOrLoadSourceMap(key) ?: run {
                out += line
                continue
            }

            val orig = sm.originalPositionFor(frame.line, frame.column)
            if (orig?.source == null || orig.line == null || orig.column == null) {
                out += line
                continue
            }

            requireNotNull(registry) { "configure SourceMapResolver.registry" }
            val trimmedSource = orig.source.trim('.', '/')
            val source = if (trimmedSource.startsWith("src/jsMain/kotlin")) {
                 "file://" + registry!!.sourceRoot + "/" + orig.source.trim('.', '/')
            } else {
                orig.source
            }
            val fnPrefix = frame.function?.let { "at $it " } ?: "at "
            var mappedLine =  "    $fnPrefix(${source}:${orig.line}:${orig.column})"
            out += mappedLine
        }

        return out.joinToString("\n")
    }

    private fun getOrLoadSourceMap(key: String): SourceMap? {
        mapCache[key]?.let { return it }
        val reg = registry
        requireNotNull(reg) { "configure SourceMapResolver.registry" }
        val raw = reg.rawSourceMaps[key] ?: return null
        val obj = JSON.parse<dynamic>(raw)
        val sm = SourceMap.fromDynamic(obj)
        mapCache[key] = sm
        return sm
    }

    // -----------------------------
    // V8 stack parsing
    // -----------------------------

    private data class Frame(val function: String?, val file: String, val line: Int, val column: Int)

    private fun parseFrame(line: String): Frame? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("at ")) return null
        var rest = trimmed.removePrefix("at ").trim()

        // "fn (file:line:col)"
        if (rest.contains(" (") && rest.endsWith(")")) {
            val fn = rest.substringBefore(" (").trim().takeIf { it.isNotEmpty() }
            val loc = rest.substringAfter(" (").removeSuffix(")")
            val (file, ln, col) = splitLocation(loc) ?: return null
            return Frame(fn, file, ln, col)
        }

        // "file:line:col"
        val (file, ln, col) = splitLocation(rest) ?: return null
        return Frame(null, file, ln, col)
    }

    private fun splitLocation(raw: String): Triple<String, Int, Int>? {
        var loc = raw
        if (loc.startsWith("file://")) loc = loc.removePrefix("file://")

        // Parse from end: "...:line:col" (works with paths containing ':' less well on Windows,
        // but Screeps is linux-y + file:// so OK)
        val last = loc.lastIndexOf(':')
        val second = loc.lastIndexOf(':', last - 1)
        if (last < 0 || second < 0) return null

        val file = loc.substring(0, second)
        val line = loc.substring(second + 1, last).toIntOrNull() ?: return null
        val col = loc.substring(last + 1).toIntOrNull() ?: return null
        return Triple(file, line, col)
    }

    private fun basename(path: String): String {
        val clean = if (path.startsWith("file://")) path.removePrefix("file://") else path
        val idx = maxOf(clean.lastIndexOf('/'), clean.lastIndexOf('\\'))
        return if (idx >= 0) clean.substring(idx + 1) else clean
    }
}

private class SourceMap(
    private val sources: List<String>,
    private val names: List<String>,
    mappings: String,
) {
    data class OriginalPosition(val source: String?, val line: Int?, val column: Int?)

    private val parsed: Array<List<Segment>> = parseMappings(mappings)

    fun originalPositionFor(genLine1: Int, genColumn1: Int): OriginalPosition? {
        val genLine = genLine1 - 1
        val genCol = genColumn1 - 1
        if (genLine !in parsed.indices) return null

        val segs = parsed[genLine]
        if (segs.isEmpty()) return null

        var lo = 0
        var hi = segs.lastIndex
        var best: Segment? = null

        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val s = segs[mid]
            if (s.generatedColumn <= genCol) {
                best = s
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }

        val b = best ?: return null
        val src = sources.getOrNull(b.sourceIndex ?: return null) ?: return null
        val line = (b.originalLine ?: return null) + 1
        val col = (b.originalColumn ?: return null) + 1

        return OriginalPosition(src, line, col)
    }

    private data class Segment(
        val generatedColumn: Int,
        val sourceIndex: Int?,
        val originalLine: Int?,
        val originalColumn: Int?,
        val nameIndex: Int?,
    )

    companion object {
        fun fromDynamic(obj: dynamic): SourceMap {
            val sources = (obj.sources as Array<String>).toList()
            val names = (obj.names as Array<String>).toList()
            val mappings = obj.mappings as String
            return SourceMap(sources, names, mappings) // fully parsed here
        }

        private fun parseMappings(mappings: String): Array<List<Segment>> {
            val lines = mappings.split(';')
            val result = Array(lines.size) { emptyList<Segment>() }

            var prevSrc = 0
            var prevLine = 0
            var prevCol = 0
            var prevName = 0

            for (i in lines.indices) {
                val line = lines[i]
                if (line.isEmpty()) continue

                val segs = ArrayList<Segment>()
                var prevGenCol = 0

                for (part in line.split(',')) {
                    if (part.isEmpty()) continue
                    val dec = VlqDecoder(part)

                    val genCol = prevGenCol + dec.next()
                    prevGenCol = genCol

                    if (!dec.hasNext()) {
                        segs += Segment(genCol, null, null, null, null)
                        continue
                    }

                    prevSrc += dec.next()
                    prevLine += dec.next()
                    prevCol += dec.next()

                    var nameIdx: Int? = null
                    if (dec.hasNext()) {
                        prevName += dec.next()
                        nameIdx = prevName
                    }

                    segs += Segment(genCol, prevSrc, prevLine, prevCol, nameIdx)
                }

                result[i] = segs
            }

            return result
        }
    }
}

private class VlqDecoder(private val s: String) {
    private var i = 0
    fun hasNext() = i < s.length

    fun next(): Int {
        var result = 0
        var shift = 0
        var cont: Boolean
        do {
            val digit = base64(s[i++])
            cont = (digit and 32) != 0
            val value = digit and 31
            result = result or (value shl shift)
            shift += 5
        } while (cont)

        val neg = (result and 1) == 1
        val v = result shr 1
        return if (neg) -v else v
    }

    private fun base64(c: Char): Int = when (c) {
        in 'A'..'Z' -> c.code - 'A'.code
        in 'a'..'z' -> c.code - 'a'.code + 26
        in '0'..'9' -> c.code - '0'.code + 52
        '+' -> 62
        '/' -> 63
        else -> error("Invalid base64 VLQ char: $c")
    }
}

