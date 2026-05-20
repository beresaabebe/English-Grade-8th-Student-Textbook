package com.beckytech.englishgrade8thtextbook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
        MobileAds.initialize(this, initializationStatus -> {});
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    private class AppOpenAdManager {
        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;
        private long loadTime = 0;

        public AppOpenAdManager() {}

        private void loadAd(Context context) {
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(
                    context, context.getString(R.string.google_app_open_ads_unit_id), request,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd ad) {
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = (new Date()).getTime();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            isLoadingAd = false;
                        }
                    });
        }

        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - this.loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }

        private boolean isAdAvailable() {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }

        public void showAdIfAvailable(@NonNull final Activity activity) {
            showAdIfAvailable(activity, () -> {});
        }

        public void showAdIfAvailable(@NonNull final Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
            if (isShowingAd) {
                return;
            }

            if (!isAdAvailable()) {
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            appOpenAd = null;
                            isShowingAd = false;
                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            appOpenAd = null;
                            isShowingAd = false;
                            onShowAdCompleteListener.onShowAdComplete();
                            loadAd(activity);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    });
            appOpenAd.show(activity);
        }
    }
}