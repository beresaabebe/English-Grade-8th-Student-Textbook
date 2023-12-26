package com.beckytech.englishgrade8thtextbook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.beckytech.englishgrade8thtextbook.R;
import com.beckytech.englishgrade8thtextbook.contents.ContentEndPage;
import com.beckytech.englishgrade8thtextbook.contents.ContentStartPage;
import com.beckytech.englishgrade8thtextbook.contents.SubTitleContents;
import com.beckytech.englishgrade8thtextbook.contents.TitleContents;
import com.beckytech.englishgrade8thtextbook.model.Model;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    private AdView adView;
    private TextView subTitle;
    private TextView title;
    private PDFView pdfView;
    private int currentIndex;

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
        pdfView = findViewById(R.id.pdfView);
        subTitle.setSelected(true);
        title.setSelected(true);

        new Handler().postDelayed(()-> {
            adsContent();
            loadBanner();
        }, 3000);

        assert model != null;
        currentIndex = getIndex(model.getTitle().toLowerCase());
        mainContent(currentIndex);

        ImageButton prevButton = findViewById(R.id.prevButton);
        prevButton.setVisibility(View.INVISIBLE);
        ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setVisibility(View.INVISIBLE);

        prevButton.setOnClickListener(v -> {
            if (currentIndex < TitleContents.title.length && currentIndex > 0) {
                currentIndex = getIndex(TitleContents.title[currentIndex - 1].toLowerCase());
                mainContent(currentIndex);
                nextButton.setVisibility(View.VISIBLE);
            }
            else {
                if (prevButton.getVisibility() == View.VISIBLE)
                    prevButton.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Thi is the first chapter!", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < TitleContents.title.length - 1 && currentIndex >= 0) {
                currentIndex = getIndex(TitleContents.title[currentIndex + 1].toLowerCase());
                mainContent(currentIndex);
                prevButton.setVisibility(View.VISIBLE);
            }
            else {
                if (nextButton.getVisibility() == View.VISIBLE)
                    nextButton.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Thi is the first chapter!", Toast.LENGTH_SHORT).show();
            }
        });

        new Handler().postDelayed(() -> {
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        }, 3000);
    }

    private int getIndex(String lowerCase) {
        for (int i = 0; i < TitleContents.title.length; i++) {
            if (TitleContents.title[i].toLowerCase().equals(lowerCase)) return i;
        }
        return -1;
    }

    private void mainContent(int currentIndex) {

        title.setText(String.valueOf(TitleContents.title[currentIndex]));
        subTitle.setText(String.valueOf(SubTitleContents.subTitle[currentIndex]));

        int start = ContentStartPage.pageStart[currentIndex];
        int end = ContentEndPage.pageEnd[currentIndex];

        List<Integer> list = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            list.add(i);
        }

        int[] array = new int[list.size()];

        for (int j = 1; j < array.length; j++) {
            array[j] = list.get(j);
        }

        pdfView.fromAsset("eng8.pdf")
                .pages(array)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .spacing(10)
                .enableDoubletap(true)
                .fitEachPage(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }
    private void adsContent() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        //get the reference to your FrameLayout
        FrameLayout adContainerView = findViewById(R.id.adView_container);
        //Create an AdView and put it into your FrameLayout
        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId(getString(R.string.banner_detail_unit_id));
    }

    private AdSize getAdSize() {
        //Determine the screen width to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        //you can also pass your selected width here in dp
        int adWidth = (int) (widthPixels / density);

        //return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        AdSize adSize = getAdSize();
        // Set the adaptive ad size to the ad view.
        adView.setAdSize(adSize);

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }
}