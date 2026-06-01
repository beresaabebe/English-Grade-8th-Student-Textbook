package com.beckytech.englishgrade8thtextbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.englishgrade8thtextbook.activity.AboutActivity;
import com.beckytech.englishgrade8thtextbook.activity.BookDetailActivity;
import com.beckytech.englishgrade8thtextbook.activity.PrivacyActivity;
import com.beckytech.englishgrade8thtextbook.adapter.Adapter;
import com.beckytech.englishgrade8thtextbook.contents.ContentEndPage;
import com.beckytech.englishgrade8thtextbook.contents.ContentStartPage;
import com.beckytech.englishgrade8thtextbook.contents.SubTitleContents;
import com.beckytech.englishgrade8thtextbook.contents.TitleContents;
import com.beckytech.englishgrade8thtextbook.model.Model;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Adapter.onBookClicked {
    private List<Model> modelList;
    private AdView adView;
    private DrawerLayout drawerLayout;

    private final List<com.google.android.gms.ads.nativead.NativeAd> mainNativeAds = new ArrayList<>();

    private static final int UPDATE_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        checkUpdate(false);
        AppRate.app_launched(this);
        AdManager.getInstance(this).loadInterstitialAd(this);
        AdManager.getInstance(this).loadRewardedAd(this);
        adsContent();
        loadBanner();
        toolbarDrawer();
        navigationView();
        loadNativeAds();
    }

    private void mainRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_main_item);
        getData();
        Adapter adapter = new Adapter(modelList, mainNativeAds, this);
        recyclerView.setAdapter(adapter);
    }

    private void checkUpdate(boolean manual) {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (manual) {
                    Toast.makeText(this, "No update available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails, you can request to start the update again.
                Log.e("Error", "Update flow failed! Result code: " + resultCode);
            }
        }
    }

    private void loadNativeAds() {
        if (!AdManager.getInstance(this).isAdsEnabled()) {
            mainRecyclerView();
            return;
        }

        com.google.android.gms.ads.AdLoader.Builder builder = new com.google.android.gms.ads.AdLoader.Builder(this, getString(R.string.google_native_ads_unit_id));
        builder.forNativeAd(nativeAd -> {
            mainNativeAds.add(nativeAd);
            mainRecyclerView();
        });
        builder.withAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                mainRecyclerView();
            }
        });
        builder.build().loadAds(new com.google.android.gms.ads.AdRequest.Builder().build(), 3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (com.google.android.gms.ads.nativead.NativeAd ad : mainNativeAds) {
            ad.destroy();
        }
    }

    private void navigationView() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        updateMenu(navigationView.getMenu());
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = MenuOptions(item);
            if (handled) {
                updateMenu(navigationView.getMenu());
            }
            return handled;
        });
    }

    private void updateMenu(android.view.Menu menu) {
        boolean adsEnabled = AdManager.getInstance(this).isAdsEnabled();
        MenuItem adItem = menu.findItem(R.id.action_ad_toggle);
        if (adItem != null) {
            if (adsEnabled) {
                adItem.setTitle("Turn off Ads (5 min)");
                adItem.setIcon(R.drawable.ic_baseline_close_24);
            } else {
                adItem.setTitle("Turn on Ads");
                adItem.setIcon(R.drawable.ic_baseline_update_24);
            }
        }
    }

    private void toolbarDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getColor(R.color.white));

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
        drawerLayout.addDrawerListener(toggle);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(android.view.View drawerView) {
                super.onDrawerOpened(drawerView);
                NavigationView navigationView = findViewById(R.id.navigationView);
                updateMenu(navigationView.getMenu());
            }
        });
    }

    private void adsContent() {
        if (!AdManager.getInstance(this).isAdsEnabled()) return;
        FrameLayout adContainerView = findViewById(R.id.adView_container);
        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId(getString(R.string.google_banner_ad_unit_id));
    }

    private void getData() {
        modelList = new ArrayList<>();
        for (int j = 0; j < TitleContents.title.length; j++) {
            modelList.add(new Model(TitleContents.title[j].substring(0, 1).toUpperCase() +
                    TitleContents.title[j].substring(1).toLowerCase(),
                    SubTitleContents.subTitle[j],
                    ContentStartPage.pageStart[j],
                    ContentEndPage.pageEnd[j]));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private boolean MenuOptions(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        if (id == R.id.action_privacy) {
            startActivity(new Intent(this, PrivacyActivity.class));
            return true;
        }
        if (id == R.id.action_about_us) {
            AdManager.getInstance(this).showInterstitialAd(this, () ->
                    startActivity(new Intent(MainActivity.this, AboutActivity.class)));
            return true;
        }

        if (id == R.id.action_rate) {
            AppRate.showRateDialog(this, null);
            return true;
        }

        if (id == R.id.action_more_apps) {
            AdManager.getInstance(this).showInterstitialAd(this, () ->
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/dev?id=6669279757479011928"))));
            return true;
        }

        if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Download this app from Play store \n" + url);
            startActivity(Intent.createChooser(intent, "Choose to send"));
            return true;
        }

        if (id == R.id.action_update) {
            checkUpdate(true);
            return true;
        }
        if (id == R.id.action_ad_toggle) {
            if (AdManager.getInstance(this).isAdsEnabled()) {
                AdManager.getInstance(this).showRewardedAd(this, () -> {
                    AdManager.getInstance(this).turnOffAdsForFiveMinutes();
                    Toast.makeText(this, "Reward Earned: Ads turned off for 5 minutes!", Toast.LENGTH_LONG).show();
                    if (adView != null) adView.setVisibility(android.view.View.GONE);
                    NavigationView nv = findViewById(R.id.navigationView);
                    if (nv != null) updateMenu(nv.getMenu());
                });
            } else {
                AdManager.getInstance(this).turnOnAdsManually();
                Toast.makeText(this, "Ads turned back on!", Toast.LENGTH_SHORT).show();
                recreate();
            }
            return true;
        }
        if (id == R.id.action_exit) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage("Do you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        System.exit(0);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setBackground(getResources().getDrawable(R.drawable.nav_header_bg, null))
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void clickedBook(Model model) {
        AdManager.getInstance(this).showInterstitialAd(this, () ->
                startActivity(new Intent(MainActivity.this, BookDetailActivity.class).putExtra("data", model)));
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadBanner() {
        if (!AdManager.getInstance(this).isAdsEnabled() || adView == null) return;
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }
}