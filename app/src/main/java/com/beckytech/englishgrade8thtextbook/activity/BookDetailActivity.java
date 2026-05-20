package com.beckytech.englishgrade8thtextbook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.beckytech.englishgrade8thtextbook.AdManager;
import com.beckytech.englishgrade8thtextbook.R;
import com.beckytech.englishgrade8thtextbook.adapter.ChapterPagerAdapter;
import com.beckytech.englishgrade8thtextbook.contents.SubTitleContents;
import com.beckytech.englishgrade8thtextbook.contents.TitleContents;
import com.beckytech.englishgrade8thtextbook.model.Model;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class BookDetailActivity extends AppCompatActivity {

    private TextView subTitle;
    private TextView title;
    private ViewPager2 viewPager;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        ImageButton back_btn = findViewById(R.id.back_book_detail);
        back_btn.setColorFilter(Color.WHITE);
        back_btn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Intent intent = getIntent();
        Model model = (Model) intent.getSerializableExtra("data");

        title = findViewById(R.id.title_book_detail);
        subTitle = findViewById(R.id.sub_title_book_detail);
        viewPager = findViewById(R.id.viewPager_chapters);
        subTitle.setSelected(true);
        title.setSelected(true);

        adsContent();
        loadBanner();

        ChapterPagerAdapter adapter = new ChapterPagerAdapter(this);
        viewPager.setAdapter(adapter);

        if (model != null) {
            int currentIndex = getIndex(model.getTitle().toLowerCase());
            if (currentIndex != -1) {
                viewPager.setCurrentItem(currentIndex, false);
                updateToolbar(currentIndex);
            }
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateToolbar(position);
            }
        });

        ImageButton prevButton = findViewById(R.id.prevButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        prevButton.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(current + 1);
            }
        });
    }

    private void adsContent() {
        if (!AdManager.getInstance(this).isAdsEnabled()) return;
        FrameLayout adContainerView = findViewById(R.id.adView_container);
        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId(getString(R.string.google_banner_detail_unit_id));
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

    private void updateToolbar(int position) {
        title.setText(TitleContents.title[position]);
        subTitle.setText(SubTitleContents.subTitle[position]);
    }

    private int getIndex(String lowerCase) {
        for (int i = 0; i < TitleContents.title.length; i++) {
            if (TitleContents.title[i].toLowerCase().equals(lowerCase)) return i;
        }
        return -1;
    }
}