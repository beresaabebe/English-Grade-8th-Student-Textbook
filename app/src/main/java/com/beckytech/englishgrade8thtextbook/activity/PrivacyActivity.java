package com.beckytech.englishgrade8thtextbook.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.beckytech.englishgrade8thtextbook.R;

public class PrivacyActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.privacy_title);

        WebView activity_privacy = findViewById(R.id.webView_privacy);
        activity_privacy.loadUrl("https://yoosaad.com/beresa-android-website-privacy-policy/");
        activity_privacy.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        activity_privacy.getSettings().getLoadsImagesAutomatically();
        activity_privacy.getSettings().setJavaScriptEnabled(true);
        activity_privacy.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                activity_privacy.loadUrl("file:///android_asset/error.html");
            }
        });
    }
}