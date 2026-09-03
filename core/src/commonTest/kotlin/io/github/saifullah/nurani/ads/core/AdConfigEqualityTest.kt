package io.github.saifullah.nurani.ads.core

import kotlin.test.Test
import kotlin.test.assertEquals

class AdConfigEqualityTest {

    @Test
    fun equivalentDefaultConfigsHaveStableValueEquality() {
        assertEquals(adConfig(), adConfig())
    }

    @Test
    fun equivalentCustomConfigsHaveStableValueEquality() {
        val first = adConfig {
            adFailedRetryRule = exponentialRetry {
                delayInMillis = 1_000
                maxAttempts = 4
                multiplier = 1.5f
            }
            adRefreshStrategy = periodicRefresh {
                intervalMillis = 60_000
                preserveOnFailure = false
            }
        }
        val second = adConfig {
            adFailedRetryRule = exponentialRetry {
                delayInMillis = 1_000
                maxAttempts = 4
                multiplier = 1.5f
            }
            adRefreshStrategy = periodicRefresh {
                intervalMillis = 60_000
                preserveOnFailure = false
            }
        }

        assertEquals(first, second)
    }
}
