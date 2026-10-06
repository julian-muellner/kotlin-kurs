package exercise

/**
 * Exercise 2 - Data classes and value classes.
 *
 * The goal: a small reading model that does not let invalid values come
 * into existence in the first place.
 *
 * The declarations are already there - you fill in the TODOs.
 *
 * Verify with:
 *     ./gradlew :block1-grundlagen-1:exerciseTest
 */

/**
 * Exercise 2a
 *
 * An id must never be blank. Add the check in the init block so that a
 * blank or whitespace-only value raises an IllegalArgumentException.
 *
 * Useful: require(...) { "message" }, isBlank()
 */
@JvmInline
value class SensorId(val value: String) {
    init {
        require(value.isNotBlank())
    }

    override fun toString(): String = value
}

/**
 * Exercise 2b + 2c
 *
 * A percentage is always between 0 and 100 inclusive. Anything else is a
 * programming error and should surface immediately.
 *
 * 2b: the check in the init block.
 * 2c: `isCritical` is true from 90 percent upwards (inclusive).
 */
@JvmInline
value class Percent(val value: Int) {
    init {
        require(value in 0..100)
    }

    val isCritical: Boolean
        get() = (value >= 90)

    override fun toString(): String = "$value %"
}

/**
 * The data class is already complete - nothing to do here, but two things
 * worth seeing:
 *
 *  - `source` has a default argument. That is why a reading can be created
 *    with two or with three arguments, without needing a second overload.
 *  - From the three constructor properties the compiler generates equals(),
 *    hashCode(), toString(), copy() and destructuring.
 */
data class Reading(
    val sensor: SensorId,
    val utilisation: Percent,
    val source: String = "device",
) {
    /**
     * A property in the body, NOT in the primary constructor. Remember for
     * the test further down what that means for equals().
     */
    var note: String = ""

    /**
     * Exercise 2d
     *
     * Returns a NEW reading with a changed utilisation. The existing object
     * must not be modified.
     *
     * Useful: the generated copy() function.
     */
    fun withUtilisation(newValue: Percent): Reading {
        return this.copy(utilisation = newValue)
    }

    /**
     * Exercise 2e
     *
     * Returns a label of this shape:
     *
     *     cam-04: 42 % (source: device)
     *
     * When the utilisation is critical, " [critical]" is appended:
     *
     *     cam-04: 95 % (source: device) [critical]
     */
    fun label(): String {
        val str = "$sensor: $utilisation (source: $source)"
        val retval = if (utilisation.isCritical) "$str [critical]" else str
        return retval

    }
}

fun main() {
    val reading = Reading(SensorId("cam-04"), Percent(42))
    println(reading.label())
    println(reading.withUtilisation(Percent(95)).label())
}
