package exercise

/**
 * Exercise 4 - scope functions.
 *
 * As in exercise 3 of block 1: **the tests are green from the start.**
 * The code works, it just does not use a scope function where one would
 * fit.
 *
 * Rewrite each function so that it uses the scope function named above it.
 * The tests must stay green throughout.
 *
 *     ./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E4*"
 *
 * The decision table is in `demo/D08ScopeFunctions.kt`.
 */

class DeviceConfiguration {
    var target: String = ""
    var port: Int = 9000
    var timeoutSeconds: Int = 30
    var tls: Boolean = false

    fun asText(): String =
        "${if (tls) "tls" else "tcp"}://$target:$port (timeout ${timeoutSeconds}s)"
}

data class DeviceEntry(
    val id: String,
    val name: String,
    val location: String,
    val utilisation: Int,
)

val sampleEntries = listOf(
    DeviceEntry("cam-04", "Camera Studio B", "Studio B", 42),
    DeviceEntry("rtr-01", "Router Control Room 1", "Control Room 1", 95),
    DeviceEntry("int-07", "Intercom Desk", "Control Room 1", 5),
    DeviceEntry("sen-12", "Temperature Sensor", "Plant Room", 91),
)

/**
 * 4a - **let**
 *
 * The null check with an intermediate variable can be pulled together into
 * a single chain.
 */
fun message(entry: DeviceEntry?): String {
    return entry?.run { "$name is running at $utilisation %" } ?: "device not found"
}

/**
 * 4b - **apply**
 *
 * Create an object, configure it, return it. The intermediate variable and
 * the four repetitions of its name are unnecessary.
 */
fun standardConfiguration(target: String): DeviceConfiguration {
    return DeviceConfiguration().apply {
        this.target = target
        port = 9100
        tls = true
        timeoutSeconds = 5
    }
}

/**
 * 4c - **with**
 *
 * The same receiver in front of the dot, five times over.
 */
fun profile(entry: DeviceEntry): String {
    return with(entry) {
        "id: $id\n" +
        "name: $name\n" +
        "location: $location\n" +
        "utilisation: $utilisation %"
    }
}

/**
 * 4d - **also**
 *
 * The chain is broken up only to log intermediate results. With `also` it
 * stays a chain.
 *
 * Careful: the order and the wording of the log lines must stay the same -
 * the test checks them.
 */
fun criticalNames(entries: List<DeviceEntry>, log: MutableList<String>): List<String> {
    return entries
        .also { log.add("incoming: ${it.size}") }
        .filter { it.utilisation >= 90 }
        .also { log.add("critical: ${it.size}")}
        .map { it.name }
        .also { log.add("names: ${it.size}") }
}

/**
 * 4e - **run**
 *
 * A computation on an object whose result is returned.
 */
fun rating(entry: DeviceEntry): String {
    val level = entry.run {
        when {
            utilisation < 10 -> "idle"
            utilisation < 60 -> "normal"
            utilisation < 90 -> "high"
            else -> "critical"
        }
    }
    return "${entry.id} is $level"
}

fun main() {
    println(message(sampleEntries.first()))
    println(message(null))
    println(standardConfiguration("control-1").asText())
    println(profile(sampleEntries.first()))

    val log = mutableListOf<String>()
    println(criticalNames(sampleEntries, log))
    log.forEach { println("  $it") }

    println(rating(sampleEntries[1]))
}
