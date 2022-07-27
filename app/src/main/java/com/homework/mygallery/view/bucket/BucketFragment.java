package com.homework.mygallery.view.bucket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homework.mygallery.R;
import com.homework.mygallery.databinding.FragmentBucketBinding;
import com.homework.mygallery.util.PhotoUtil;

import java.util.ArrayList;

/**
 * 相册列表界面
 */
public class BucketFragment extends Fragment {

    private int BucketId;

    private FragmentBucketBinding binding;

    public BucketFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBucketBinding.inflate(inflater,container,false);
        if (getArguments() != null) {
            BucketId = getArguments().getInt("BucketId");
            // 更新顶部导航栏标题
            requireActivity().setTitle(getArguments().getString("BucketName"));
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<String> photos = PhotoUtil.getPhotos(requireActivity(), BucketId);
        if (photos.size() > 0) {
            // 实例化适配器
            BucketAdapter bucketAdapter = new BucketAdapter(photos, (cur) -> {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("photos", photos);
                bundle.putInt("cur",cur);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_bucketFragment_to_photoFragment,bundle);
            });
            // 设置布局管理器
            binding.recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(),4));
            // 设置适配器
            binding.recyclerView.setAdapter(bucketAdapter);
        }
    }
}