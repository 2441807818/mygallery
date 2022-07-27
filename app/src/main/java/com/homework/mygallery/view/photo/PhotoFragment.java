package com.homework.mygallery.view.photo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homework.mygallery.databinding.FragmentPhotoBinding;

import java.util.ArrayList;


public class PhotoFragment extends Fragment {

    private FragmentPhotoBinding binding;

    private ArrayList<String> photos;

    private int cur;

    public PhotoFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 更新顶部导航栏标题
        requireActivity().setTitle("图片详情");
        binding = FragmentPhotoBinding.inflate(inflater,container,false);
        if (getArguments() != null) {
            photos = getArguments().getStringArrayList("photos");
            cur = getArguments().getInt("cur");
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(requireActivity().getSupportFragmentManager(), getLifecycle(), photos);
        // 给viewPager2控件设置适配器
        binding.viewPager2.setAdapter(viewPagerAdapter);
        binding.viewPager2.setPageTransformer(new ZoomOutPageTransformer());
        new Handler().post(() -> {
            binding.viewPager2.setCurrentItem(cur);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}