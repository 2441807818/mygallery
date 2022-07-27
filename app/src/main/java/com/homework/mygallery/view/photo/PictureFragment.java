package com.homework.mygallery.view.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.homework.mygallery.databinding.FragmentPictureBinding;

import java.io.File;

/**
 * @introduction：
 * @author： 林锦焜
 * @time： 2022/7/27 16:50
 */
public class PictureFragment extends Fragment {

    private final String path;

    private FragmentPictureBinding binding;

    public PictureFragment(String path) {
        this.path = path;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPictureBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Glide.with(requireActivity()).load(new File(path)).into(binding.imageView);
    }


}
