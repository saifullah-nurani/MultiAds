package io.github.saifullah.nurani.ads.admob;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import io.github.saifullah.nurani.ads.core.AdConfig;
import io.github.saifullah.nurani.ads.core.AdLogger;
import io.github.saifullah.nurani.ads.core.OnUserRewardedListener;

public class AdmobAds {

    private static final String TAG = "AdmobAds";

    private static WeakReference<Context> applicationContext;
    private static Config currentConfig;

    // Use Maps with WeakReferences to prevent memory leaks if objects are destroyed
    private static final ConcurrentHashMap<String, WeakReference<AdmobInterstitialAd>> mInterstitialAds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WeakReference<AdmobRewardedAd>> mRewardedAds = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WeakReference<AdmobRewardedInterstitialAd>> mRewardedInterstitialAds = new ConcurrentHashMap<>();

    private AdmobAds() {
        // Private constructor to prevent instantiation
    }

    public final static AdRequest DEFAULT_AD_REQUEST = new AdRequest.Builder().build();

    /**
     * Configuration class to set custom AdConfigs for each ad format.
     */
    public static class Config {
        private final AdConfig interstitialConfig;
        private final AdConfig rewardedConfig;

        private final AdLogger adLogger;
        private final AdConfig rewardedInterstitialConfig;

        private final AdRequest interstitialAdRequest;
        private final AdRequest rewardedAdRequest;
        private final AdRequest rewardedInterstitialAdRequest;

        private Config(Builder builder) {
            AdConfig fallbackConfig = builder.defaultConfig != null ? builder.defaultConfig : AdConfig.getDefault();
            AdRequest fallbackRequest = builder.defaultAdRequest != null ? builder.defaultAdRequest
                    : DEFAULT_AD_REQUEST;

            this.interstitialConfig = builder.interstitialConfig != null ? builder.interstitialConfig : fallbackConfig;
            this.rewardedConfig = builder.rewardedConfig != null ? builder.rewardedConfig : fallbackConfig;
            this.adLogger = builder.adLogger;
            this.rewardedInterstitialConfig = builder.rewardedInterstitialConfig != null
                    ? builder.rewardedInterstitialConfig
                    : fallbackConfig;

            this.interstitialAdRequest = builder.interstitialAdRequest != null ? builder.interstitialAdRequest
                    : fallbackRequest;
            this.rewardedAdRequest = builder.rewardedAdRequest != null ? builder.rewardedAdRequest : fallbackRequest;
            this.rewardedInterstitialAdRequest = builder.rewardedInterstitialAdRequest != null
                    ? builder.rewardedInterstitialAdRequest
                    : fallbackRequest;
        }

        public AdConfig getInterstitialConfig() {
            return interstitialConfig;
        }

        public AdConfig getRewardedConfig() {
            return rewardedConfig;
        }

        public AdConfig getRewardedInterstitialConfig() {
            return rewardedInterstitialConfig;
        }

        public AdRequest getInterstitialAdRequest() {
            return interstitialAdRequest;
        }

        public AdRequest getRewardedAdRequest() {
            return rewardedAdRequest;
        }

        public AdRequest getRewardedInterstitialAdRequest() {
            return rewardedInterstitialAdRequest;
        }

        public static class Builder {
            private AdConfig defaultConfig;
            private AdRequest defaultAdRequest;

            private AdLogger adLogger;

            private AdConfig interstitialConfig;
            private AdConfig rewardedConfig;
            private AdConfig rewardedInterstitialConfig;

            private AdRequest interstitialAdRequest;
            private AdRequest rewardedAdRequest;
            private AdRequest rewardedInterstitialAdRequest;

            /**
             * Sets the default AdConfig applied to all ad formats.
             * This will be used if a specific format config is not provided.
             */
            public Builder setDefaultConfig(@NonNull AdConfig config) {
                this.defaultConfig = config;
                return this;
            }

            /**
             * Sets the default AdRequest applied to all ad formats.
             * This will be used if a specific format ad request is not provided.
             */
            public Builder setDefaultAdRequest(@NonNull AdRequest adRequest) {
                this.defaultAdRequest = adRequest;
                return this;
            }

            /**
             * Sets a custom {@link AdLogger} implementation to receive internal ad lifecycle logs.
             */
            public Builder setAdLogger(@NonNull AdLogger adLogger) {
                this.adLogger = adLogger;
                return this;
            }

            /**
             * Sets a custom AdConfig specifically for Interstitial Ads.
             */
            public Builder setInterstitialConfig(@NonNull AdConfig config) {
                this.interstitialConfig = config;
                return this;
            }

            /**
             * Sets a custom AdConfig specifically for Rewarded Ads.
             */
            public Builder setRewardedConfig(@NonNull AdConfig config) {
                this.rewardedConfig = config;
                return this;
            }

            /**
             * Sets a custom AdConfig specifically for Rewarded Interstitial Ads.
             */
            public Builder setRewardedInterstitialConfig(@NonNull AdConfig config) {
                this.rewardedInterstitialConfig = config;
                return this;
            }

            /**
             * Sets a custom AdRequest specifically for Interstitial Ads.
             */
            public Builder setInterstitialAdRequest(@NonNull AdRequest adRequest) {
                this.interstitialAdRequest = adRequest;
                return this;
            }

            /**
             * Sets a custom AdRequest specifically for Rewarded Ads.
             */
            public Builder setRewardedAdRequest(@NonNull AdRequest adRequest) {
                this.rewardedAdRequest = adRequest;
                return this;
            }

            /**
             * Sets a custom AdRequest specifically for Rewarded Interstitial Ads.
             */
            public Builder setRewardedInterstitialAdRequest(@NonNull AdRequest adRequest) {
                this.rewardedInterstitialAdRequest = adRequest;
                return this;
            }

            public Config build() {
                return new Config(this);
            }
        }
    }

    /**
     * MUST be called in Application class or at start of the app with a specific
     * configuration.
     *
     * @param appContext The Application context
     * @param config     Custom configurations for different ad formats
     */
    @SuppressLint("MissingPermission")
    public static void init(@NonNull Context appContext, @NonNull Config config) {
        applicationContext = new WeakReference<>(appContext);
        currentConfig = config;
        MobileAds.initialize(appContext);
    }

    /**
     * MUST be called in Application class or at start of the app. Uses default
     * configurations.
     *
     * @param appContext The Application context
     */
    public static void init(@NonNull Context appContext) {
        init(appContext, new Config.Builder().build());
    }

    private static void checkInitialized() {
        if (applicationContext == null) {
            throw new IllegalStateException("AdmobAds is not initialized. Call AdmobAds.init(appContext) first.");
        }
    }

    private static Context getContext() {
        Context context = applicationContext != null ? applicationContext.get() : null;

        if (context == null) {
            throw new IllegalStateException("Context lost. Reinitialize AdmobAds.");
        }

        return context;
    }

    // ---- INTERSTITIAL AD ----

    public static void loadInterstitialAd(@NonNull String adUnitId) {
        checkInitialized();
        WeakReference<AdmobInterstitialAd> ref = mInterstitialAds.get(adUnitId);
        AdmobInterstitialAd ad = ref != null ? ref.get() : null;

        if (ad == null) {
            ad = new AdmobInterstitialAd(getContext(), adUnitId, currentConfig.getInterstitialConfig(),
                    currentConfig.getInterstitialAdRequest(), null);
            mInterstitialAds.put(adUnitId, new WeakReference<>(ad));
        }
        if (!ad.isAdLoading()) {
            ad.loadAd();
        }
    }

    public static void showInterstitialAd(@NonNull Activity activity, @NonNull String adUnitId) {
        checkInitialized();
        WeakReference<AdmobInterstitialAd> ref = mInterstitialAds.get(adUnitId);
        AdmobInterstitialAd ad = ref != null ? ref.get() : null;

        if (ad != null && ad.isAdAvailable()) {
            ad.showAd(activity);
        } else {
            logError("InterstitialAd not ready to show for ad unit: " + adUnitId);
        }
    }

    // ---- REWARDED AD ----

    public static void loadRewardedAd(@NonNull String adUnitId) {
        checkInitialized();
        WeakReference<AdmobRewardedAd> ref = mRewardedAds.get(adUnitId);
        AdmobRewardedAd ad = ref != null ? ref.get() : null;

        if (ad == null) {
            ad = new AdmobRewardedAd(getContext(), adUnitId, currentConfig.getRewardedConfig(),
                    currentConfig.getRewardedAdRequest()
                    , null);
            mRewardedAds.put(adUnitId, new WeakReference<>(ad));
        }
        if (!ad.isAdLoading()) {
            ad.loadAd();
        }
    }

    public static void showRewardedAd(@NonNull Activity activity, @NonNull String adUnitId) {
        showRewardedAd(activity, adUnitId, rewardItem -> {
        });
    }

    public static void showRewardedAd(@NonNull Activity activity, @NonNull String adUnitId,
                                      @NonNull OnUserEarnedRewardListener listener) {
        checkInitialized();
        WeakReference<AdmobRewardedAd> ref = mRewardedAds.get(adUnitId);
        AdmobRewardedAd ad = ref != null ? ref.get() : null;

        if (ad != null && ad.isAdAvailable()) {
            ad.showAd(activity, listener);
        } else {
            logError("RewardedAd not ready to show for ad unit: " + adUnitId);
        }
    }

    public static void showRewardedAd(@NonNull Activity activity, @NonNull String adUnitId,
                                      @NonNull OnUserRewardedListener listener) {
        showRewardedAd(activity, adUnitId, rewardItem -> listener.onUserRewarded());
    }

    // ---- REWARDED INTERSTITIAL AD ----

    public static void loadRewardedInterstitialAd(@NonNull String adUnitId) {
        checkInitialized();
        WeakReference<AdmobRewardedInterstitialAd> ref = mRewardedInterstitialAds.get(adUnitId);
        AdmobRewardedInterstitialAd ad = ref != null ? ref.get() : null;

        if (ad == null) {
            ad = new AdmobRewardedInterstitialAd(getContext(),
                    adUnitId,
                    currentConfig.getRewardedInterstitialConfig(),
                    currentConfig.getRewardedInterstitialAdRequest(), null);
            mRewardedInterstitialAds.put(adUnitId, new WeakReference<>(ad));
        }
        if (!ad.isAdLoading()) {
            ad.loadAd();
        }
    }

    public static void showRewardedInterstitialAd(@NonNull Activity activity, @NonNull String adUnitId) {
        showRewardedInterstitialAd(activity, adUnitId, rewardItem -> {
        });
    }

    public static void showRewardedInterstitialAd(@NonNull Activity activity, @NonNull String adUnitId,
                                                  @NonNull OnUserEarnedRewardListener listener) {
        checkInitialized();
        WeakReference<AdmobRewardedInterstitialAd> ref = mRewardedInterstitialAds.get(adUnitId);
        AdmobRewardedInterstitialAd ad = ref != null ? ref.get() : null;

        if (ad != null && ad.isAdAvailable()) {
            ad.showAd(activity, listener);
        } else {
            logError("RewardedInterstitialAd not ready to show for ad unit: " + adUnitId);
        }
    }

    public static void showRewardedInterstitialAd(@NonNull Activity activity, @NonNull String adUnitId,
                                                  @NonNull OnUserRewardedListener listener) {
        showRewardedInterstitialAd(activity, adUnitId, rewardItem -> {
            listener.onUserRewarded();
        });
    }

    // --------------------------------------------------------
    // Logging Helpers
    // --------------------------------------------------------

    private static void logDebug(String message) {
        if (currentConfig.adLogger != null) {
            currentConfig.adLogger.d(TAG + ": " + message);
        }
    }

    private static void logError(String message) {
        if (currentConfig.adLogger != null) {
            currentConfig.adLogger.e(TAG + ": " + message);
        }
    }
}
