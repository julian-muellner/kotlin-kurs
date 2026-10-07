package exercise

import demo.Device
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Exercise 1 - exceptions, retry and cancellation.
 *
 * [FlakyDeviceClient] is the kind of dependency you get handed in real
 * projects: it fails now and then, and sometimes it simply does not answer.
 *
 * Your job is to wrap it so the rest of the system can rely on it.
 *
 * Verify with:
 *     ./gradlew :block4-coroutines-2:exerciseTest --tests "exercise.E1*"
 *
 * The tests use `runTest`, so all the waiting happens in virtual time -
 * the suite runs in well under a second despite the delays.
 */

class DeviceUnreachableException(deviceId: String) :
    IllegalStateException("device $deviceId is unreachable")

/**
 * Fails the first [failuresBeforeSuccess] calls, then succeeds.
 * A call takes [latencyMillis].
 *
 * Do not change this class; it represents the behaviour to be handled.
 */
class FlakyDeviceClient(
    private val failuresBeforeSuccess: Int = 2,
    private val latencyMillis: Long = 100,
) {
    var callCount = 0
        private set

    suspend fun readStatus(deviceId: String): String {
        callCount++
        delay(latencyMillis.milliseconds)

        if (callCount <= failuresBeforeSuccess) {
            throw DeviceUnreachableException(deviceId)
        }
        return "$deviceId: online"
    }
}

/**
 * Exercise 1a
 *
 * Calls `readStatus` and retries on [DeviceUnreachableException].
 *
 *  - at most [maxAttempts] attempts in total
 *  - wait [backoffMillis] between attempts, doubling each time
 *    (100, 200, 400, ...)
 *  - if the last attempt also fails, let the exception through
 *
 * Careful with the trap from demo 2: do NOT catch `Exception` broadly.
 * `CancellationException` is an Exception too, and swallowing it makes
 * the function uncancellable. Catch [DeviceUnreachableException].
 */
suspend fun readStatusWithRetry(
    client: FlakyDeviceClient,
    deviceId: String,
    maxAttempts: Int = 3,
    backoffMillis: Long = 100,
): String {
    var waitTime = backoffMillis
    repeat(maxAttempts - 1) {
        try {
            return client.readStatus(deviceId)
        } catch (e: DeviceUnreachableException) {
            delay(waitTime.milliseconds)
            waitTime *= 2
        }
    }
    return client.readStatus(deviceId)
}

/**
 * Exercise 1b
 *
 * Like 1a, but gives up after [timeoutMillis] overall and returns null
 * instead of throwing.
 *
 * "Overall" means the whole retry sequence, not a single attempt.
 *
 * Useful: withTimeoutOrNull
 */
suspend fun readStatusOrNull(
    client: FlakyDeviceClient,
    deviceId: String,
    maxAttempts: Int = 5,
    backoffMillis: Long = 100,
    timeoutMillis: Long = 1_000,
): String? {
    return try {
        withTimeoutOrNull(timeoutMillis.milliseconds) {
            readStatusWithRetry(client, deviceId, maxAttempts, backoffMillis)
        }
    } catch (e: DeviceUnreachableException) {
        null
    }
}

/**
 * Exercise 1c
 *
 * Reads the status of several devices concurrently, and is explicitly
 * ALLOWED TO FAIL PARTIALLY: one unreachable device must not spoil the
 * results of the others.
 *
 * Returns a map from device id to result, where a failure is recorded as
 * null.
 *
 * Think about which scope is right here. `coroutineScope` cancels all
 * siblings when one child fails - that is the opposite of what is wanted.
 *
 * Useful: supervisorScope, async, try/catch around await()
 */
suspend fun readAllStatuses(
    clients: Map<String, FlakyDeviceClient>,
    maxAttempts: Int = 3,
    backoffMillis: Long = 100,
): Map<String, String?> {
    return supervisorScope {
        clients
            .mapValues { (id, client) -> async { readStatusWithRetry(client, id, maxAttempts, backoffMillis) } }
            .mapValues { (_, job) -> try { job.await() } catch (e: DeviceUnreachableException) { null } }
    }
}

/**
 * Exercise 1d
 *
 * A long-running cleanup that has to survive cancellation.
 *
 * The function polls until it is cancelled. When that happens, it must
 * still append "closed" to [log] - and the append happens after a
 * suspending call, which is the whole difficulty.
 *
 * Useful: try/finally, NonCancellable, withContext
 */
suspend fun pollUntilCancelled(log: MutableList<String>, intervalMillis: Long = 100) {
    try {
        while (true) {
            delay(intervalMillis.milliseconds)
            log += "poll"
        }
    } finally {
        withContext(NonCancellable) {
            delay(intervalMillis.milliseconds)
            log.addLast("closed")
        }
    }
}
