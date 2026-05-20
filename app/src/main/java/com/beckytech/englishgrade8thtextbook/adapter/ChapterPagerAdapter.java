package com.beckytech.englishgrade8thtextbook.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.beckytech.englishgrade8thtextbook.activity.ChapterFragment;
import com.beckytech.englishgrade8thtextbook.contents.TitleContents;

public class ChapterPagerAdapter extends FragmentStateAdapter {

    public ChapterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ChapterFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return TitleContents.title.length;
    }
}