package exercise

/**
 * Exercise 3 - functional programming: evaluating telemetry.
 *
 * Every task can be solved without a single loop and without `var`. If you
 * find yourself needing a loop, there is almost certainly a matching
 * collections operation - a look at `D06Functional.kt` helps.
 *
 * Verify with:
 *     ./gradlew :block2-grundlagen-2:exerciseTest --tests "exercise.E3*"
 */

/** Reused from exercise 2: data class Measurement(deviceId, value) */

val sampleTelemetry: List<Measurement> = listOf(
    Measurement("sen-12", 21.4),
    Measurement("sen-12", 22.8),
    Measurement("sen-12", 71.2),
    Measurement("cam-04", 38.0),
    Measurement("cam-04", 39.5),
    Measurement("rtr-01", 88.0),
    Measurement("rtr-01", 92.5),
    Measurement("rtr-01", 95.0),
)

/**
 * Exercise 3a
 *
 * The average value per device.
 *
 * For the sample data:
 *     sen-12 -> 38.466…, cam-04 -> 38.75, rtr-01 -> 91.833…
 *
 * Useful: groupBy, mapValues, average()
 */
fun averagePerDevice(measurements: List<Measurement>): Map<String, Double> {
    return measurements
        .groupBy { it.deviceId }
        .mapValues { (_, list) -> list.map { it.value }.average() }
}

/**
 * Exercise 3b
 *
 * The highest value per device.
 *
 * Useful: groupBy, mapValues, maxOf
 */
fun peakPerDevice(measurements: List<Measurement>): Map<String, Double> {
    return measurements
        .groupBy { it.deviceId }
        .mapValues { (_, list) -> list.maxOf { it.value } }
}

/**
 * Exercise 3c
 *
 * All measurements above the given limit, in the order they arrived.
 */
fun outliers(measurements: List<Measurement>, limit: Double): List<Measurement> {
    return measurements.filter { it.value > limit }
}

/**
 * Exercise 3d
 *
 * The `count` devices with the highest average value, in descending order.
 * For the sample data with count = 2: ["rtr-01", "cam-04"].
 *
 * Useful: the result of 3a, sortedByDescending, take, keys/toList
 */
fun topDevices(measurements: List<Measurement>, count: Int): List<String> {
    return averagePerDevice(measurements)
        .toList()
        .sortedBy { -it.second }
        .map { it.first }
        .take(count)
}

/**
 * Exercise 3e
 *
 * An extension property: a measurement counts as notable from 70.0 upwards.
 *
 * Remember: an extension property cannot have a backing field, so it needs
 * a getter.
 */
val Measurement.isNotable: Boolean
    get() = (value >= 70.0)

/**
 * Exercise 3f
 *
 * An extension function on List<Measurement> that yields a summary in
 * exactly this format:
 *
 *     8 measurements from 3 devices, 4 notable
 *
 * For an empty list:
 *
 *     no measurements
 *
 * Useful: size, distinctBy or map+distinct, count, isNotable from 3e
 */
fun List<Measurement>.summarise(): String {
    return when(size) {
        0 -> "no measurements"
        else -> "$size measurements from ${distinctBy { it.deviceId }.size} devices, ${count { it.isNotable }} notable"
    }
}

fun main() {
    println(averagePerDevice(sampleTelemetry))
    println(peakPerDevice(sampleTelemetry))
    println(outliers(sampleTelemetry, 70.0))
    println(topDevices(sampleTelemetry, 2))
    println(sampleTelemetry.summarise())
}
