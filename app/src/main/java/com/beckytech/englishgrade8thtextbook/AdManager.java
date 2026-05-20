package com.beckytech.englishgrade8thtextbook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static final String PREF_NAME = "ad_prefs";
    private static final String KEY_ADS_OFF_UNTIL = "ads_off_until";
    private static AdManager instance;
    private final SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd;
    private boolean isAdLoading = false;

    private AdManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isAdsEnabled() {
        long offUntil = sharedPreferences.getLong(KEY_ADS_OFF_UNTIL, 0);
        return System.currentTimeMillis() > offUntil;
    }

    public void turnOffAdsForFiveMinutes() {
        long offUntil = System.currentTimeMillis() + (5 * 60 * 1000);
        sharedPreferences.edit().putLong(KEY_ADS_OFF_UNTIL, offUntil).apply();
    }

    public void turnOnAdsManually() {
        sharedPreferences.edit().putLong(KEY_ADS_OFF_UNTIL, 0).apply();
    }

    public void loadInterstitialAd(Context context) {
        if (!isAdsEnabled() || isAdLoading || mInterstitialAd != null) {
            return;
        }

        isAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, context.getString(R.string.google_interstitial_ads_unit_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        isAdLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        isAdLoading = false;
                    }
                });
    }

    public void showInterstitialAd(Activity activity, AdClosedListener listener) {
        if (mInterstitialAd != null && isAdsEnabled()) {
            mInterstitialAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mInterstitialAd = null;
                    loadInterstitialAd(activity);
                    if (listener != null) listener.onAdClosed();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    mInterstitialAd = null;
                    if (listener != null) listener.onAdClosed();
                }
            });
            mInterstitialAd.show(activity);
        } else {
            if (listener != null) listener.onAdClosed();
        }
    }

    public interface AdClosedListener {
        void onAdClosed();
    }
}