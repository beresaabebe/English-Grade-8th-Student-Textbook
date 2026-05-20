package com.beckytech.englishgrade8thtextbook.activity;

import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.englishgrade8thtextbook.AdManager;
import com.beckytech.englishgrade8thtextbook.R;
import com.beckytech.englishgrade8thtextbook.adapter.PageAdapter;
import com.beckytech.englishgrade8thtextbook.contents.ContentEndPage;
import com.beckytech.englishgrade8thtextbook.contents.ContentStartPage;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChapterFragment extends Fragment {

    private static final String ARG_CHAPTER_INDEX = "chapter_index";
    private int chapterIndex;
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private final List<NativeAd> nativeAds = new ArrayList<>();

    public static ChapterFragment newInstance(int chapterIndex) {
        ChapterFragment fragment = new ChapterFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAPTER_INDEX, chapterIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chapterIndex = getArguments().getInt(ARG_CHAPTER_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_chapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        try {
            initPdfRenderer();
            int startPage = ContentStartPage.pageStart[chapterIndex];
            int endPage = ContentEndPage.pageEnd[chapterIndex];

            if (getContext() != null && AdManager.getInstance(getContext()).isAdsEnabled()) {
                loadNativeAds(recyclerView, startPage, endPage);
            } else {
                PageAdapter adapter = new PageAdapter(pdfRenderer, startPage, endPage, null);
                recyclerView.setAdapter(adapter);
            }

        } catch (Exception e) {
            android.util.Log.e("ChapterFragment", "Error initializing PDF", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Error loading PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            }
        }

        return view;
    }

    private void initPdfRenderer() throws IOException {
        if (getContext() == null) return;
        File file = new File(getContext().getCacheDir(), "eng8.pdf");
        if (!file.exists()) {
            try (InputStream is = getContext().getAssets().open("eng8.pdf");
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    private void loadNativeAds(RecyclerView recyclerView, int startPage, int endPage) {
        if (getContext() == null) return;
        com.google.android.gms.ads.AdLoader.Builder builder = new com.google.android.gms.ads.AdLoader.Builder(getContext(), getString(R.string.google_native_ads_unit_id));
        builder.forNativeAd(nativeAd -> {
            nativeAds.add(nativeAd);
            if (recyclerView.getAdapter() == null) {
                PageAdapter adapter = new PageAdapter(pdfRenderer, startPage, endPage, nativeAds);
                recyclerView.setAdapter(adapter);
            }
        });
        builder.withAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                if (recyclerView.getAdapter() == null) {
                    PageAdapter adapter = new PageAdapter(pdfRenderer, startPage, endPage, null);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // Ensure adapter is set if not already
                if (recyclerView.getAdapter() == null) {
                    PageAdapter adapter = new PageAdapter(pdfRenderer, startPage, endPage, nativeAds);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
        com.google.android.gms.ads.AdLoader adLoader = builder.build();
        adLoader.loadAds(new com.google.android.gms.ads.AdRequest.Builder().build(), 3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (pdfRenderer != null) pdfRenderer.close();
            if (parcelFileDescriptor != null) parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (NativeAd ad : nativeAds) {
            ad.destroy();
        }
    }
}