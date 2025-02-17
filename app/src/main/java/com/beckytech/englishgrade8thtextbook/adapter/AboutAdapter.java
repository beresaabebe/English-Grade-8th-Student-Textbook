package com.beckytech.englishgrade8thtextbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.englishgrade8thtextbook.model.AboutModel;
import com.beckytech.englishgrade8thtextbook.R;

import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.AboutViewHolder> {
    private List<AboutModel> modelList;
    private OnLinkClicked linkClicked;

    public AboutAdapter(List<AboutModel> modelList, OnLinkClicked linkClicked) {
        this.modelList = modelList;
        this.linkClicked = linkClicked;
    }

    public interface OnLinkClicked {
        public void linkClicked(AboutModel model);
    }

    @NonNull
    @Override
    public AboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AboutViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.about_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AboutViewHolder holder, int position) {
        AboutModel model = modelList.get(position);
        holder.name.setText(model.getName());
        holder.imageView.setImageResource(model.getImage());
        holder.itemView.setOnClickListener(v -> linkClicked.linkClicked(model));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public static class AboutViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;
        public AboutViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_about);
            name = itemView.findViewById(R.id.about_name);

        }
    }

}
