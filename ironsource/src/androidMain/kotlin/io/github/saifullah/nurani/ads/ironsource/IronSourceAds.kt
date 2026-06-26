package io.github.saifullah.nurani.ads.ironsource

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.ironsource.mediationsdk.IronSource
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object IronSourceAds {

    private const val TAG = "IronSourceAds"
    const val TEST_APP_KEY: String = "2636d0095"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<IronSourceInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<IronSourceRewardedAd>>()

    /**
     * Configuration class to set custom AdConfigs for each ad format.
     */
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

            /**
             * Sets the default AdConfig applied to all ad formats.
             * This will be used if a specific format config is not provided.
             */
            fun setDefaultConfig(config: AdConfig): Builder {
                this.defaultConfig = config
                return this
            }

            /**
             * Sets a custom [AdLogger] implementation to receive internal ad lifecycle logs.
             */
            fun setAdLogger(adLogger: AdLogger): Builder {
                this.adLogger = adLogger
                return this
            }

            /**
             * Sets a custom AdConfig specifically for Interstitial Ads.
             */
            fun setInterstitialConfig(config: AdConfig): Builder {
                this.interstitialConfig = config
                return this
            }

            /**
             * Sets a custom AdConfig specifically for Rewarded Ads.
             */
            fun setRewardedConfig(config: AdConfig): Builder {
                this.rewardedConfig = config
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

    /**
     * MUST be called in Application class or at start of the app with a specific
     * configuration and App Key.
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, appKey: String, config: Config) {
        applicationContext = WeakReference(appContext)
        currentConfig = config
        IronSource.init(appContext, appKey)
    }

    /**
     * MUST be called with Activity context (recommended for lifecycle binding)
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(activity: Activity, appKey: String, config: Config) {
        applicationContext = WeakReference(activity.applicationContext)
        currentConfig = config
        IronSource.init(activity, appKey)
    }

    /**
     * MUST be called in Application class or at start of the app with standard configurations.
     */
    @JvmStatic
    fun init(appContext: Context, appKey: String) {
        init(appContext, appKey, Config.Builder().build())
    }

    @JvmStatic
    fun init(activity: Activity, appKey: String) {
        init(activity, appKey, Config.Builder().build())
    }

    @JvmStatic
    actual fun init(
        context: io.github.saifullah.nurani.ads.core.compose.PlatformContext,
        androidAppKey: String,
        iosAppKey: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(context as Context, androidAppKey)
        onComplete?.invoke(AdInitResult(true))
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = applicationContext?.get() != null

    @JvmStatic
    fun onResume(activity: Activity) {
        IronSource.onResume(activity)
    }

    @JvmStatic
    fun onPause(activity: Activity) {
        IronSource.onPause(activity)
    }

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("IronSourceAds is not initialized. Call IronSourceAds.init(appContext, appKey) first.")
        }
    }

    private val context: Context
        get() {
            val ctx = applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize IronSourceAds.")
            return ctx
        }

    // ---- INTERSTITIAL AD ----

    @JvmStatic
    fun loadInterstitialAd(placementName: String) {
        checkInitialized()
        val ref = mInterstitialAds[placementName]
        var ad = ref?.get()

        if (ad == null) {
            ad = IronSourceInterstitialAd(context, placementName, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[placementName] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, placementName: String) {
        checkInitialized()
        val ref = mInterstitialAds[placementName]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity)
        } else {
            logError("InterstitialAd not ready to show for placement: $placementName")
        }
    }

    // ---- REWARDED AD ----

    @JvmStatic
    fun loadRewardedAd(placementName: String) {
        checkInitialized()
        val ref = mRewardedAds[placementName]
        var ad = ref?.get()

        if (ad == null) {
            ad = IronSourceRewardedAd(context, placementName, currentConfig!!.rewardedConfig, null)
            mRewardedAds[placementName] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementName: String) {
        showRewardedAd(activity, placementName, object : OnUserRewardedListener {
            override fun onUserRewarded() {}
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementName: String, listener: OnUserRewardedListener) {
        checkInitialized()
        val ref = mRewardedAds[placementName]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for placement: $placementName")
        }
    }

    // --------------------------------------------------------
    // Logging Helpers
    // --------------------------------------------------------

    private fun logDebug(message: String) {
        currentConfig?.adLogger?.d("$TAG: $message")
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message")
    }
}
