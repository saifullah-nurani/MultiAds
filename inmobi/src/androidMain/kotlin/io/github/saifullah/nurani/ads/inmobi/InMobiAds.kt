package io.github.saifullah.nurani.ads.inmobi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.inmobi.sdk.InMobiSdk
import com.inmobi.sdk.SdkInitializationListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object InMobiAds {

    private const val TAG = "InMobiAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<Long, WeakReference<InMobiInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<Long, WeakReference<InMobiRewardedAd>>()

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
     * configuration and Account ID.
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, accountId: String, config: Config, onComplete: ((Error?) -> Unit)?) {
        applicationContext = WeakReference(appContext)
        currentConfig = config

        InMobiSdk.init(appContext, accountId, null, object : SdkInitializationListener {
            override fun onInitializationComplete(error: Error?) {
                if (error != null) {
                    logError("InMobi SDK Init failed: " + error.message)
                } else {
                    logDebug("InMobi SDK Initialization Complete")
                }
                onComplete?.invoke(error)
            }
        })
    }

    @JvmStatic
    fun init(appContext: Context, accountId: String, config: Config) {
        init(appContext, accountId, config, null)
    }

    @JvmStatic
    @JvmName("initWithError")
    fun init(appContext: Context, accountId: String, onComplete: ((Error?) -> Unit)?) {
        init(appContext, accountId, Config.Builder().build(), onComplete)
    }

    /**
     * MUST be called in Application class or at start of the app with standard configurations.
     */
    @JvmStatic
    fun init(appContext: Context, accountId: String) {
        init(appContext, accountId, Config.Builder().build(), null)
    }

    @JvmStatic
    actual fun init(context: io.github.saifullah.nurani.ads.core.compose.PlatformContext, accountId: String, onComplete: ((Boolean) -> Unit)?) {
        init(context as Context, accountId, Config.Builder().build()) { error ->
            onComplete?.invoke(error == null)
        }
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = applicationContext?.get() != null

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("InMobiAds is not initialized. Call InMobiAds.init(appContext, accountId) first.")
        }
    }

    private val context: Context
        get() {
            val ctx = applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize InMobiAds.")
            return ctx
        }

    // ---- INTERSTITIAL AD ----

    @JvmStatic
    fun loadInterstitialAd(placementId: Long) {
        checkInitialized()
        val ref = mInterstitialAds[placementId]
        var ad = ref?.get()

        if (ad == null) {
            ad = InMobiInterstitialAd(context, placementId, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, placementId: Long) {
        checkInitialized()
        val ref = mInterstitialAds[placementId]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity)
        } else {
            logError("InterstitialAd not ready to show for placement ID: $placementId")
        }
    }

    // ---- REWARDED AD ----

    @JvmStatic
    fun loadRewardedAd(placementId: Long) {
        checkInitialized()
        val ref = mRewardedAds[placementId]
        var ad = ref?.get()

        if (ad == null) {
            ad = InMobiRewardedAd(context, placementId, currentConfig!!.rewardedConfig, null)
            mRewardedAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: Long) {
        showRewardedAd(activity, placementId, object : OnUserRewardedListener {
            override fun onUserRewarded() {}
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: Long, listener: OnUserRewardedListener) {
        checkInitialized()
        val ref = mRewardedAds[placementId]
        val ad = ref?.get()

        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for placement ID: $placementId")
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
