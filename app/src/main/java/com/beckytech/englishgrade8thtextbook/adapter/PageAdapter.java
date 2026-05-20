package com.beckytech.englishgrade8thtextbook.adapter;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.englishgrade8thtextbook.R;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PAGE = 0;
    private static final int TYPE_AD = 1;
    private static final int AD_INTERVAL = 6; // Ad after every 5 pages (index 5, 11, ...)

    private final List<Object> items = new ArrayList<>();
    private final PdfRenderer renderer;

    private static final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);

    public PageAdapter(PdfRenderer renderer, int startPage, int endPage, List<NativeAd> nativeAds) {
        this.renderer = renderer;
        int adIndex = 0;
        int currentPageCount = 0;
        for (int i = startPage; i <= endPage; i++) {
            items.add(i);
            currentPageCount++;
            if (currentPageCount % 5 == 0 && nativeAds != null && adIndex < nativeAds.size()) {
                items.add(nativeAds.get(adIndex++));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Integer ? TYPE_PAGE : TYPE_AD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf_page, parent, false);
            return new PageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad, parent, false);
            return new AdViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PAGE) {
            int pageIndex = (int) items.get(position);
            ((PageViewHolder) holder).bind(pageIndex, renderer);
        } else {
            NativeAd nativeAd = (NativeAd) items.get(position);
            ((AdViewHolder) holder).bind(nativeAd);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        com.github.chrisbanes.photoview.PhotoView imageView;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pdf_page_image);
            imageView.setMinimumHeight(500); // Initial height
        }

        void bind(int pageIndex, PdfRenderer renderer) {
            imageView.setImageBitmap(null);
            if (renderer == null) return;
            executor.submit(() -> {
                try {
                    Bitmap bitmap;
                    synchronized (renderer) {
                        PdfRenderer.Page page = renderer.openPage(pageIndex);
                        int width = page.getWidth() * 2;
                        int height = page.getHeight() * 2;
                        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(android.graphics.Color.WHITE);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        page.close();
                    }
                    imageView.post(() -> imageView.setImageBitmap(bitmap));
                } catch (Exception e) {
                    android.util.Log.e("PageAdapter", "Error rendering page " + pageIndex, e);
                }
            });
        }
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        NativeAdView adView;

        AdViewHolder(@NonNull View itemView) {
            super(itemView);
            adView = (NativeAdView) itemView;
        }

        void bind(NativeAd nativeAd) {
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));
            adView.setMediaView(adView.findViewById(R.id.ad_media));

            ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
            if (nativeAd.getBody() == null) {
                Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }

            if (nativeAd.getCallToAction() == null) {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
                ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }

            if (nativeAd.getIcon() == null) {
                Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
            } else {
                ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }

            adView.setNativeAd(nativeAd);
        }
    }
}