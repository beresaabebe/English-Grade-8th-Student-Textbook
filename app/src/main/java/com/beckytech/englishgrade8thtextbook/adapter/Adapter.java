package com.beckytech.englishgrade8thtextbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.englishgrade8thtextbook.R;
import com.beckytech.englishgrade8thtextbook.model.Model;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_AD = 1;

    private final List<Object> list = new ArrayList<>();
    private final onBookClicked bookClicked;

    public Adapter(List<Model> models, List<NativeAd> nativeAds, onBookClicked bookClicked) {
        this.bookClicked = bookClicked;
        int adIndex = 0;
        for (int i = 0; i < models.size(); i++) {
            list.add(models.get(i));
            if ((i + 1) % 5 == 0 && nativeAds != null && adIndex < nativeAds.size()) {
                list.add(nativeAds.get(adIndex++));
            }
        }
    }

    public interface onBookClicked {
        void clickedBook(Model model);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) instanceof Model ? TYPE_ITEM : TYPE_AD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        } else {
            return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_native_ad, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            Model model = (Model) list.get(position);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.title.setText(model.getTitle());
            itemHolder.subTitle.setText(model.getSubTitle());
            itemHolder.itemView.setOnClickListener(v -> bookClicked.clickedBook(model));
        } else {
            NativeAd nativeAd = (NativeAd) list.get(position);
            ((AdViewHolder) holder).bind(nativeAd);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView title, subTitle;
        ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            title.setSelected(true);
            subTitle = itemView.findViewById(R.id.subTitle);
            imageView = itemView.findViewById(R.id.image);
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
            adView.setPriceView(adView.findViewById(R.id.ad_price));
            adView.setStoreView(adView.findViewById(R.id.ad_store));

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

            if (nativeAd.getPrice() == null) {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }

            if (nativeAd.getStore() == null) {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }

            adView.setNativeAd(nativeAd);
        }
    }
}