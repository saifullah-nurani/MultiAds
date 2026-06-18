package io.github.saifullah.nurani.ads.man

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.facebook.ads.AudienceNetworkAds
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object MetaAds {
    private const val TAG = "MetaAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<MetaInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<MetaRewardedAd>>()

    class Config private constructor(
        val interstitialConfig: AdConfig,
        val rewardedConfig: AdConfig,
        val adLogger: AdLogger?
    ) {
        class Builder {
            private var defaultConfig: AdConfig? = null
            private var adLogger: AdLogger? = null
            private var interstitialConfig: AdConfig? = null
            private var rewardedConfig: AdConfig? = null

            fun setDefaultConfig(config: AdConfig): Builder {
                defaultConfig = config
                return this
            }

            fun setAdLogger(adLogger: AdLogger): Builder {
                this.adLogger = adLogger
                return this
            }

            fun setInterstitialConfig(config: AdConfig): Builder {
                interstitialConfig = config
                return this
            }

            fun setRewardedConfig(config: AdConfig): Builder {
                rewardedConfig = config
                return this
            }

            fun build(): Config {
                val fallbackConfig = defaultConfig ?: AdConfig.default
                return Config(
                    interstitialConfig = interstitialConfig ?: fallbackConfig,
                    rewardedConfig = rewardedConfig ?: fallbackConfig,
                    adLogger = adLogger
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, config: Config, onComplete: ((Boolean) -> Unit)? = null) {
        applicationContext = WeakReference(appContext.applicationContext)
        currentConfig = config
        AudienceNetworkAds.initialize(appContext)
        onComplete?.invoke(true)
    }

    @JvmStatic
    fun init(appContext: Context) {
        init(appContext, Config.Builder().build())
    }

    actual fun init(context: PlatformContext, onComplete: ((Boolean) -> Unit)?) {
        init(context, Config.Builder().build(), onComplete)
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = applicationContext?.get() != null

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("MetaAds is not initialized. Call MetaAds.init(appContext) first.")
        }
    }

    private fun getContext(): Context {
        return applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize MetaAds.")
    }

    @JvmStatic
    fun loadInterstitialAd(placementId: String) {
        checkInitialized()
        val ref = mInterstitialAds[placementId]
        var ad = ref?.get()
        if (ad == null) {
            ad = MetaInterstitialAd(getContext(), placementId, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, placementId: String) {
        checkInitialized()
        val ad = mInterstitialAds[placementId]?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity)
        } else {
            logError("InterstitialAd not ready to show for placement ID: $placementId")
        }
    }

    @JvmStatic
    fun loadRewardedAd(placementId: String) {
        checkInitialized()
        val ref = mRewardedAds[placementId]
        var ad = ref?.get()
        if (ad == null) {
            ad = MetaRewardedAd(getContext(), placementId, currentConfig!!.rewardedConfig, null)
            mRewardedAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: String) {
        showRewardedAd(activity, placementId, object : OnUserRewardedListener {
            override fun onUserRewarded() = Unit
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: String, listener: OnUserRewardedListener) {
        checkInitialized()
        val ad = mRewardedAds[placementId]?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for placement ID: $placementId")
        }
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message")
    }
}
