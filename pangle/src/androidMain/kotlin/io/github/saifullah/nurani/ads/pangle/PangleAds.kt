package io.github.saifullah.nurani.ads.pangle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import io.github.saifullah.nurani.ads.core.AdInitResult
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object PangleAds {

    private const val TAG = "PangleAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null
    private var isInitialized = false

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<PangleInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<PangleRewardedAd>>()

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
     * configuration and App ID.
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, appId: String, config: Config, onComplete: ((Boolean) -> Unit)?) {
        applicationContext = WeakReference(appContext)
        currentConfig = config
        isInitialized = false

        val pagConfig = PAGConfig.Builder()
            .appId(appId)
            .debugLog(config.adLogger != null)
            .build()

        PAGSdk.init(appContext, pagConfig, object : PAGSdk.PAGInitCallback {
            override fun success() {
                logDebug("Pangle SDK Initialization Complete")
                isInitialized = true
                onComplete?.invoke(true)
            }

            override fun fail(code: Int, msg: String) {
                logError("Pangle SDK Init failed: code: $code, msg: $msg")
                isInitialized = false
                onComplete?.invoke(false)
            }
        })
    }

    @JvmStatic
    fun init(appContext: Context, appId: String, config: Config) {
        init(appContext, appId, config, null)
    }

    @JvmStatic
    actual fun init(
        context: io.github.saifullah.nurani.ads.core.compose.PlatformContext,
        androidAppId: String,
        iosAppId: String,
        onComplete: ((AdInitResult) -> Unit)?
    ) {
        init(context as Context, androidAppId, Config.Builder().build()) { success ->
            onComplete?.invoke(AdInitResult(success))
        }
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = isInitialized

    /**
     * MUST be called in Application class or at start of the app with standard configurations.
     */
    @JvmStatic
    fun init(appContext: Context, appId: String) {
        init(appContext, appId, Config.Builder().build(), null)
    }

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("PangleAds is not initialized. Call PangleAds.init(appContext, appId) first.")
        }
    }

    private val context: Context
        get() {
            val ctx = applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize PangleAds.")
            return ctx
        }

    // ---- INTERSTITIAL AD ----

    @JvmStatic
    fun loadInterstitialAd(adUnitId: String) {
        checkInitialized()
        val ref = mInterstitialAds[adUnitId]
        var ad = ref?.get()

        if (ad == null) {
            ad = PangleInterstitialAd(context, adUnitId, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, adUnitId: String) {
        checkInitialized()
        val ref = mInterstitialAds[adUnitId]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity)
        } else {
            logError("InterstitialAd not ready to show for adUnitId: $adUnitId")
        }
    }

    // ---- REWARDED AD ----

    @JvmStatic
    fun loadRewardedAd(adUnitId: String) {
        checkInitialized()
        val ref = mRewardedAds[adUnitId]
        var ad = ref?.get()

        if (ad == null) {
            ad = PangleRewardedAd(context, adUnitId, currentConfig!!.rewardedConfig, null)
            mRewardedAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String) {
        showRewardedAd(activity, adUnitId, object : OnUserRewardedListener {
            override fun onUserRewarded() {}
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String, listener: OnUserRewardedListener) {
        checkInitialized()
        val ref = mRewardedAds[adUnitId]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for adUnitId: $adUnitId")
        }
    }

    // --------------------------------------------------------
    // Logging Helpers
    // --------------------------------------------------------

    private fun logDebug(message: String) {
        currentConfig?.adLogger?.d("$TAG: $message") ?: android.util.Log.d(TAG, message)
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message") ?: android.util.Log.e(TAG, message)
    }
}
