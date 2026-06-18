package io.github.saifullah.nurani.ads.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd as GmsAppOpenAd
import io.github.saifullah.nurani.ads.core.AdConfig
import io.github.saifullah.nurani.ads.core.AppOpenAd
import io.github.saifullah.nurani.ads.core.Scheduler
import io.github.saifullah.nurani.ads.core.utils.ContextUtils.Companion.findActivity

class AdmobAppOpenAd(
    val context: Context,
    adUnitId: String,
    adConfig: AdConfig?,
    adRequest: AdRequest?,
    handler: Handler?
) : FullScreenAdState(context, Scheduler(handler), adConfig, TAG), AppOpenAd {
    private var mAppOpenAd: GmsAppOpenAd? = null
    private val adUnitId: String = if (this.adConfig.isTestModeEnabled) TEST_AD_UNIT_ID else adUnitId
    private val adRequest: AdRequest = adRequest ?: AdRequest.Builder().build()

    private val appOpenAdLoadCallback = object : GmsAppOpenAd.AppOpenAdLoadCallback() {
        override fun onAdLoaded(appOpenAd: GmsAppOpenAd) {
            adStateManager.onAdLoaded()
            adLoadListener?.onAdLoaded()
            mAppOpenAd = appOpenAd
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            val adError = AdmobUtils.adErrorFrom(loadAdError)
            adStateManager.onAdFailedToLoad(adError)
            adLoadListener?.onAdFailedToLoad(adError)
            if (adStateManager.shouldPreserveOnFailure) {
                mAppOpenAd = null
            }
        }
    }

    override val isAdAvailable: Boolean get() = mAppOpenAd != null

    override fun loadAd() {
        if (mAppOpenAd != null) return
        reloadAd()
    }

    override fun onAdLoad() {
        GmsAppOpenAd.load(
            context,
            adUnitId,
            adRequest,
            appOpenAdLoadCallback
        )
    }

    override fun clean() {
        mAppOpenAd = null
    }

    override fun showAd(owner: Activity) {
        if (isAdAvailable) {
            mAppOpenAd!!.fullScreenContentCallback = FullScreenContentCallback(adStateManager, adScreenContentCallback, ::clean)
            mAppOpenAd!!.show(owner)
        }
    }

    @Throws(IllegalStateException::class)
    fun showAdOrThrow() {
        val activity = findActivity(context)
        checkNotNull(activity) { "$TAG: Context is not an Activity." }
        check(isAdAvailable) { "$TAG: is not loaded." }
        showAd(activity)
    }

    override fun tryShowAd(): Boolean {
        return try {
            showAdOrThrow()
            true
        } catch (e: IllegalStateException) {
            adLogger?.e(e.message)
            false
        }
    }

    override fun addLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(adStateManager)
    }

    companion object {
        @JvmStatic
        fun with(context: Context, adUnitId: String): AdmobAppOpenAd {
            return AdmobAppOpenAd(context, adUnitId, null, null, null)
        }

        @JvmStatic
        fun with(context: Context, @StringRes adUnitId: Int): AdmobAppOpenAd {
            return with(context, context.getString(adUnitId))
        }

        const val TAG: String = "AdmobAppOpenAd"
        const val TEST_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/9257395921"
    }
}
