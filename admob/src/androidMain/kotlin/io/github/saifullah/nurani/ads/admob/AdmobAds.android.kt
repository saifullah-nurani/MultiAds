package io.github.saifullah.nurani.ads.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AdLogger
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener
import io.github.saifullah.nurani.ads.core.compose.PlatformContext
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

actual object AdmobAds {
    private const val TAG = "AdmobAds"

    private var applicationContext: WeakReference<Context>? = null
    private var currentConfig: Config? = null

    private val mInterstitialAds = ConcurrentHashMap<String, WeakReference<AdmobInterstitialAd>>()
    private val mRewardedAds = ConcurrentHashMap<String, WeakReference<AdmobRewardedAd>>()
    private val mRewardedInterstitialAds = ConcurrentHashMap<String, WeakReference<AdmobRewardedInterstitialAd>>()

    val DEFAULT_AD_REQUEST: AdRequest = AdRequest.Builder().build()

    class Config private constructor(
        val interstitialConfig: AdConfig,
        val rewardedConfig: AdConfig,
        val rewardedInterstitialConfig: AdConfig,
        val adLogger: AdLogger?,
        val interstitialAdRequest: AdRequest,
        val rewardedAdRequest: AdRequest,
        val rewardedInterstitialAdRequest: AdRequest
    ) {
        class Builder {
            private var defaultConfig: AdConfig? = null
            private var defaultAdRequest: AdRequest? = null
            private var adLogger: AdLogger? = null
            private var interstitialConfig: AdConfig? = null
            private var rewardedConfig: AdConfig? = null
            private var rewardedInterstitialConfig: AdConfig? = null
            private var interstitialAdRequest: AdRequest? = null
            private var rewardedAdRequest: AdRequest? = null
            private var rewardedInterstitialAdRequest: AdRequest? = null

            fun setDefaultConfig(config: AdConfig): Builder {
                this.defaultConfig = config
                return this
            }

            fun setDefaultAdRequest(adRequest: AdRequest): Builder {
                this.defaultAdRequest = adRequest
                return this
            }

            fun setAdLogger(adLogger: AdLogger): Builder {
                this.adLogger = adLogger
                return this
            }

            fun setInterstitialConfig(config: AdConfig): Builder {
                this.interstitialConfig = config
                return this
            }

            fun setRewardedConfig(config: AdConfig): Builder {
                this.rewardedConfig = config
                return this
            }

            fun setRewardedInterstitialConfig(config: AdConfig): Builder {
                this.rewardedInterstitialConfig = config
                return this
            }

            fun setInterstitialAdRequest(adRequest: AdRequest): Builder {
                this.interstitialAdRequest = adRequest
                return this
            }

            fun setRewardedAdRequest(adRequest: AdRequest): Builder {
                this.rewardedAdRequest = adRequest
                return this
            }

            fun setRewardedInterstitialAdRequest(adRequest: AdRequest): Builder {
                this.rewardedInterstitialAdRequest = adRequest
                return this
            }

            fun build(): Config {
                val fallbackConfig = defaultConfig ?: AdConfig.default
                val fallbackRequest = defaultAdRequest ?: DEFAULT_AD_REQUEST
                return Config(
                    interstitialConfig = interstitialConfig ?: fallbackConfig,
                    rewardedConfig = rewardedConfig ?: fallbackConfig,
                    rewardedInterstitialConfig = rewardedInterstitialConfig ?: fallbackConfig,
                    adLogger = adLogger,
                    interstitialAdRequest = interstitialAdRequest ?: fallbackRequest,
                    rewardedAdRequest = rewardedAdRequest ?: fallbackRequest,
                    rewardedInterstitialAdRequest = rewardedInterstitialAdRequest ?: fallbackRequest
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun init(appContext: Context, config: Config) {
        applicationContext = WeakReference(appContext)
        currentConfig = config
        MobileAds.initialize(appContext)
    }

    @JvmStatic
    actual fun init(context: PlatformContext) {
        init(context as Context, Config.Builder().build())
    }

    private fun checkInitialized() {
        if (applicationContext == null) {
            throw IllegalStateException("AdmobAds is not initialized. Call AdmobAds.init(appContext) first.")
        }
    }

    private fun getContext(): Context {
        return applicationContext?.get() ?: throw IllegalStateException("Context lost. Reinitialize AdmobAds.")
    }

    @JvmStatic
    fun loadInterstitialAd(adUnitId: String) {
        checkInitialized()
        val ref = mInterstitialAds[adUnitId]
        var ad = ref?.get()
        if (ad == null) {
            ad = AdmobInterstitialAd(
                getContext(),
                adUnitId,
                currentConfig!!.interstitialConfig,
                currentConfig!!.interstitialAdRequest,
                null
            )
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
            logError("InterstitialAd not ready to show for ad unit: $adUnitId")
        }
    }

    @JvmStatic
    fun loadRewardedAd(adUnitId: String) {
        checkInitialized()
        val ref = mRewardedAds[adUnitId]
        var ad = ref?.get()
        if (ad == null) {
            ad = AdmobRewardedAd(
                getContext(),
                adUnitId,
                currentConfig!!.rewardedConfig,
                currentConfig!!.rewardedAdRequest,
                null
            )
            mRewardedAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String) {
        showRewardedAd(activity, adUnitId, OnUserEarnedRewardListener { })
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String, listener: OnUserEarnedRewardListener) {
        checkInitialized()
        val ref = mRewardedAds[adUnitId]
        val ad = ref?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedAd not ready to show for ad unit: $adUnitId")
        }
    }

    @JvmStatic
    fun showRewardedAd(activity: Activity, adUnitId: String, listener: OnUserRewardedListener) {
        showRewardedAd(activity, adUnitId, OnUserEarnedRewardListener { listener.onUserRewarded() })
    }

    @JvmStatic
    fun loadRewardedInterstitialAd(adUnitId: String) {
        checkInitialized()
        val ref = mRewardedInterstitialAds[adUnitId]
        var ad = ref?.get()
        if (ad == null) {
            ad = AdmobRewardedInterstitialAd(
                getContext(),
                adUnitId,
                currentConfig!!.rewardedInterstitialConfig,
                currentConfig!!.rewardedInterstitialAdRequest,
                null
            )
            mRewardedInterstitialAds[adUnitId] = WeakReference(ad)
        }
        if (!ad.isAdLoading) {
            ad.loadAd()
        }
    }

    @JvmStatic
    fun showRewardedInterstitialAd(activity: Activity, adUnitId: String) {
        showRewardedInterstitialAd(activity, adUnitId, OnUserEarnedRewardListener { })
    }

    @JvmStatic
    fun showRewardedInterstitialAd(activity: Activity, adUnitId: String, listener: OnUserEarnedRewardListener) {
        checkInitialized()
        val ref = mRewardedInterstitialAds[adUnitId]
        val ad = ref?.get()
        if (ad != null && ad.isAdAvailable) {
            ad.showAd(activity, listener)
        } else {
            logError("RewardedInterstitialAd not ready to show for ad unit: $adUnitId")
        }
    }

    @JvmStatic
    fun showRewardedInterstitialAd(activity: Activity, adUnitId: String, listener: OnUserRewardedListener) {
        showRewardedInterstitialAd(activity, adUnitId, OnUserEarnedRewardListener { listener.onUserRewarded() })
    }

    private fun logDebug(message: String) {
        currentConfig?.adLogger?.d("$TAG: $message")
    }

    private fun logError(message: String) {
        currentConfig?.adLogger?.e("$TAG: $message")
    }
}
