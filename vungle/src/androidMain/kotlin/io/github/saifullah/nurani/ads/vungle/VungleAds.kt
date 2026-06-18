package io.github.saifullah.nurani.ads.vungle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.vungle.ads.InitializationListener
import com.vungle.ads.VungleError
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object VungleAds {

    private const val TAG = "VungleAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<VungleInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<VungleRewardedAd>>()

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
     * configuration and Vungle App ID.
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, appId: String, config: Config, onComplete: ((Boolean) -> Unit)?) {
        applicationContext = WeakReference(appContext)
        currentConfig = config

        com.vungle.ads.VungleAds.init(appContext, appId, object : InitializationListener {
            override fun onSuccess() {
                logDebug("Vungle SDK Initialization Complete")
                onComplete?.invoke(true)
            }

            override fun onError(vungleError: VungleError) {
                logError("Vungle SDK Init failed: " + vungleError.localizedMessage)
                onComplete?.invoke(false)
            }
        })
    }

    @JvmStatic
    fun init(appContext: Context, appId: String, config: Config) {
        init(appContext, appId, config, null)
    }

    @JvmStatic
    actual fun init(context: io.github.saifullah.nurani.ads.core.compose.PlatformContext, appId: String, onComplete: ((Boolean) -> Unit)?) {
        init(context as Context, appId, Config.Builder().build(), onComplete)
    }

    @JvmStatic
    actual fun isInitialized(): Boolean = applicationContext?.get() != null

    /**
     * MUST be called in Application class or at start of the app with standard configurations.
     */
    @JvmStatic
    fun init(appContext: Context, appId: String) {
        init(appContext, appId, Config.Builder().build(), null)
    }

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("VungleAds is not initialized. Call VungleAds.init(appContext, appId) first.")
        }
    }

    private val context: Context
        get() {
            val ctx = applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize VungleAds.")
            return ctx
        }

    // ---- INTERSTITIAL AD ----

    @JvmStatic
    fun loadInterstitialAd(placementId: String) {
        checkInitialized()
        val ref = mInterstitialAds[placementId]
        var ad = ref?.get()

        if (ad == null) {
            ad = VungleInterstitialAd(context, placementId, currentConfig!!.interstitialConfig, null)
            mInterstitialAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showInterstitialAd(activity: Activity, placementId: String) {
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
    fun loadRewardedAd(placementId: String) {
        checkInitialized()
        val ref = mRewardedAds[placementId]
        var ad = ref?.get()

        if (ad == null) {
            ad = VungleRewardedAd(context, placementId, currentConfig!!.rewardedConfig, null)
            mRewardedAds[placementId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: String) {
        showRewardedAd(activity, placementId, object : OnUserRewardedListener {
            override fun onUserRewarded() {}
        })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, placementId: String, listener: OnUserRewardedListener) {
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
        currentConfig?.adLogger?.d("$TAG: $message") ?: android.util.Log.d(TAG, message)
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message") ?: android.util.Log.e(TAG, message)
    }
}
