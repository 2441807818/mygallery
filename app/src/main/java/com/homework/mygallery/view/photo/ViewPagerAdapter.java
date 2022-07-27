package com.homework.mygallery.view.photo;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * @introduction：
 * @author： 林锦焜
 * @time： 2022/7/27 16:52
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<String> photos;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, ArrayList<String> photos) {
        super(fragmentManager, lifecycle);
        this.photos = photos;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PictureFragment(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

}
