package com.beckytech.englishgrade8thtextbook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManager {
    private static final String PREF_NAME = "ad_prefs";
    private static final String KEY_ADS_OFF_UNTIL = "ads_off_until";
    private static AdManager instance;
    private final SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private boolean isAdLoading = false;
    private boolean isRewardedAdLoading = false;

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

    public void loadRewardedAd(Context context) {
        if (isRewardedAdLoading || mRewardedAd != null) return;
        isRewardedAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, context.getString(R.string.google_rewarded_ads_unit_id), adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mRewardedAd = null;
                        isRewardedAdLoading = false;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        isRewardedAdLoading = false;
                    }
                });
    }

    public void showRewardedAd(Activity activity, OnRewardEarnedListener listener) {
        if (mRewardedAd != null) {
            mRewardedAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    mRewardedAd = null;
                    loadRewardedAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    mRewardedAd = null;
                    // On failure, we might still want to grant the reward if it was a technical error to keep user happy
                    if (listener != null) listener.onRewardEarned();
                }
            });
            mRewardedAd.show(activity, rewardItem -> {
                if (listener != null) listener.onRewardEarned();
            });
        } else {
            // If ad not ready, just give the reward to not annoy user, but load for next time
            if (listener != null) listener.onRewardEarned();
            loadRewardedAd(activity);
        }
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

    public interface OnRewardEarnedListener {
        void onRewardEarned();
    }
}