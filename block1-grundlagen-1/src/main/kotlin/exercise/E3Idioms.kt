package exercise

/**
 * Exercise 3 - Idioms: a refactoring.
 *
 * This code WORKS. The tests are green from the start.
 *
 * It is, however, written without using what Kotlin offers for these
 * six cases.
 *
 * The task: rewrite each of the six functions idiomatically. The tests
 * must stay green THROUGHOUT - they are the safety net.
 *
 *     ./gradlew :block1-grundlagen-1:exerciseTest
 *
 * The comment above each function says what it is about. Afterwards no
 * function should need a `return` in the middle of its body, and none
 * should use a StringBuilder any more.
 */

/**
 * 3a - if/else cascade -> `when` as an expression.
 */
fun level(utilisation: Int): String {
    return when {
        utilisation < 10 -> "idle"
        utilisation < 60 -> "normal"
        utilisation < 90 -> "high"
        else -> "critical"
    }
}

/**
 * 3b - StringBuilder -> string template.
 */
fun format(name: String, port: Int) = "$name (port $port)"

/**
 * 3c - three overloads -> one function with default arguments.
 *
 * Careful: after the rewrite there must be only ONE function
 * `connectionUrl`. The tests call it with one, two and three arguments.
 */
fun connectionUrl(host: String, port: Int = 9000, protocol: String = "tcp") = "$protocol://$host:$port"


/**
 * 3d - a chain of comparisons with && -> a range with `in`.
 */
fun isWithinNormalBand(utilisation: Int) = utilisation in 10..90

/**
 * 3e - nested null checks -> safe call, takeIf and Elvis.
 */
fun cleanName(input: String?): String {
    return input?.trim()?.takeIf { it.isNotEmpty() } ?: "unknown"
}

/**
 * 3f - `var` with a later assignment -> `if` as an expression and `val`.
 */
fun installationSize(channels: Int): String {
    return "${(if (channels > 8) "large" else "small")} installation"
}

fun main() {
    println(level(85))
    println(format("control-1", 9100))
    println(connectionUrl("control-1"))
    println(isWithinNormalBand(73))
    println(cleanName("  Camera Studio B  "))
    println(installationSize(16))
}
