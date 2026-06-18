package io.github.saifullah.nurani.ads.applovin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object AppLovinAds {
    private const val TAG = "AppLovinAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<AppLovinInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<AppLovinRewardedAd>>()

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
    fun init(appContext: Context, sdkKey: String, config: Config, onComplete: ((Boolean) -> Unit)? = null) {
        applicationContext = WeakReference(appContext.applicationContext)
        currentConfig = config

        val initConfig = AppLovinSdkInitializationConfiguration.builder(sdkKey, appContext)
            .setMediationProvider(AppLovinMediationProvider.MAX)
            .build()

        AppLovinSdk.getInstance(appContext).initialize(initConfig) {
            logDebug("AppLovin SDK MAX initialization complete")
            onComplete?.invoke(true)
        }
    }

    @JvmStatic
    fun init(appContext: Context, sdkKey: String) {
        init(appContext, sdkKey, Config.Builder().build())
    }

    actual fun init(context: PlatformContext, sdkKey: String, onComplete: ((Boolean) -> Unit)?) {
        init(context, sdkKey, Config.Builder().build(), onComplete)
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = applicationContext?.get() != null

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("AppLovinAds is not initialized. Call AppLovinAds.init(appContext, sdkKey) first.")
        }
    }

    private fun getContext(): Context {
        return applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize AppLovinAds.")
    }

    @JvmStatic
    fun loadInterstitialAd(adUnitId: String) {
        checkInitialized()
        val ref = mInterstitialAds[adUnitId]
        var ad = ref?.get()
        if (ad == null) {
            ad = AppLovinInterstitialAd(getContext(), adUnitId, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, adUnitId: String) {
        checkInitialized()
        val ad = mInterstitialAds[adUnitId]?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity)
        } else {
            logError("InterstitialAd not ready to show for ad unit ID: $adUnitId")
        }
    }

    @JvmStatic
    fun loadRewardedAd(adUnitId: String) {
        checkInitialized()
        val ref = mRewardedAds[adUnitId]
        var ad = ref?.get()
        if (ad == null) {
            ad = AppLovinRewardedAd(getContext(), adUnitId, currentConfig!!.rewardedConfig, null)
            mRewardedAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String) {
        showRewardedAd(activity, adUnitId, object : OnUserRewardedListener {
            override fun onUserRewarded() = Unit
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String, listener: OnUserRewardedListener) {
        checkInitialized()
        val ad = mRewardedAds[adUnitId]?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for ad unit ID: $adUnitId")
        }
    }

    private fun logDebug(message: String) {
        currentConfig?.adLogger?.d("$TAG: $message")
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message")
    }
}
