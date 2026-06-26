package io.github.saifullah.nurani.ads.multi.models

import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.adConfig

data class MultiAdsConfig(
    val adConfig: AdConfig,
    val waterfallConfig: WaterfallConfig? = null
)

@DslMarker
annotation class MultiAdsDsl

@MultiAdsDsl
class MultiAdsConfigBuilder {
    var waterfallConfig: WaterfallConfig? = null
    var adLogger = null as io.github.saifullah.nurani.ads.core.AdLogger?
    var adReloadPolicies = setOf<io.github.saifullah.nurani.ads.core.AdReloadPolicy>()
    var adFailedRetryRule: io.github.saifullah.nurani.ads.core.AdFailedRetryRule = io.github.saifullah.nurani.ads.core.exponentialRetry()
    var adRefreshStrategy: io.github.saifullah.nurani.ads.core.AdRefreshStrategy = io.github.saifullah.nurani.ads.core.periodicRefresh()
    var isTestModeEnabled: Boolean = false
    var tag: String? = null

    fun waterfall(block: WaterfallConfigBuilder.() -> Unit) {
        waterfallConfig = waterfallConfig(block)
    }

    internal fun build(): MultiAdsConfig {
        return MultiAdsConfig(
            adConfig = adConfig {
                this.adLogger = this@MultiAdsConfigBuilder.adLogger
                this.adReloadPolicies = if (this@MultiAdsConfigBuilder.adReloadPolicies.isEmpty()) this.adReloadPolicies else this@MultiAdsConfigBuilder.adReloadPolicies
                this.adFailedRetryRule = this@MultiAdsConfigBuilder.adFailedRetryRule
                this.adRefreshStrategy = this@MultiAdsConfigBuilder.adRefreshStrategy
                this.isTestModeEnabled = this@MultiAdsConfigBuilder.isTestModeEnabled
                this.tag = this@MultiAdsConfigBuilder.tag
            },
            waterfallConfig = waterfallConfig
        )
    }
}

fun multiAdsConfig(block: MultiAdsConfigBuilder.() -> Unit): MultiAdsConfig {
    return MultiAdsConfigBuilder().apply(block).build()
}
