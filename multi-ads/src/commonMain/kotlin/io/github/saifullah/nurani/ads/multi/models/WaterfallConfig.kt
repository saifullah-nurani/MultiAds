package io.github.saifullah.nurani.ads.multi.models

enum class AdNetwork {
    ADMOB,
    APPLOVIN,
    META,
    VUNGLE,
    INMOBI,
    PANGLE,
    IRONSOURCE
}

data class AdNetworkConfig(
    val network: AdNetwork,
    val adUnitId: String,
    val priority: Int
)

data class WaterfallConfig(
    val networks: List<AdNetworkConfig>,
    val maxConcurrentLoads: Int = 1
)

@DslMarker
annotation class WaterfallDsl

@WaterfallDsl
class WaterfallConfigBuilder {
    private val networks = mutableListOf<AdNetworkConfig>()
    private var maxConcurrentLoads: Int = 1

    fun maxConcurrentLoads(value: Int) {
        maxConcurrentLoads = value.coerceAtLeast(1)
    }

    fun network(
        network: AdNetwork,
        adUnitId: String,
        priority: Int
    ) {
        networks += AdNetworkConfig(
            network = network,
            adUnitId = adUnitId,
            priority = priority
        )
    }

    fun admob(adUnitId: String, priority: Int) = network(AdNetwork.ADMOB, adUnitId, priority)
    fun applovin(adUnitId: String, priority: Int) = network(AdNetwork.APPLOVIN, adUnitId, priority)
    fun meta(placementId: String, priority: Int) = network(AdNetwork.META, placementId, priority)
    fun vungle(placementId: String, priority: Int) = network(AdNetwork.VUNGLE, placementId, priority)
    fun inmobi(placementId: Long, priority: Int) = network(AdNetwork.INMOBI, placementId.toString(), priority)
    fun pangle(adUnitId: String, priority: Int) = network(AdNetwork.PANGLE, adUnitId, priority)
    fun ironsource(placementName: String, priority: Int) = network(AdNetwork.IRONSOURCE, placementName, priority)

    internal fun build(): WaterfallConfig {
        return WaterfallConfig(
            networks = networks
                .sortedWith(compareBy<AdNetworkConfig> { it.priority }.thenBy { it.network.name }),
            maxConcurrentLoads = maxConcurrentLoads
        )
    }
}

fun waterfallConfig(block: WaterfallConfigBuilder.() -> Unit): WaterfallConfig {
    return WaterfallConfigBuilder().apply(block).build()
}

fun waterfallConfig(
    vararg networks: AdNetworkConfig,
    maxConcurrentLoads: Int = 1
): WaterfallConfig {
    return WaterfallConfig(
        networks = networks
            .asList()
            .sortedWith(compareBy<AdNetworkConfig> { it.priority }.thenBy { it.network.name }),
        maxConcurrentLoads = maxConcurrentLoads.coerceAtLeast(1)
    )
}
