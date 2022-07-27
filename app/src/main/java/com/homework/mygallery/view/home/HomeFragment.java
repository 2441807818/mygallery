package com.homework.mygallery.view.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.homework.mygallery.R;
import com.homework.mygallery.databinding.FragmentHomeBinding;
import com.homework.mygallery.model.Bucket;
import com.homework.mygallery.util.BucketUtil;

import java.util.List;

public class HomeFragment extends PermissionFragment {

    private FragmentHomeBinding binding;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 更新顶部导航栏标题
        requireActivity().setTitle("首页");
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 检查读取手机存储权限
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 没有权限，去申请
            ActivityCompat.requestPermissions(requireActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }
        List<Bucket> buckets = BucketUtil.getBuckets(requireActivity());
        if (buckets.size() > 0) {
            // 实例化适配器
            HomeAdapter homeAdapter = new HomeAdapter(buckets, bucket -> {
                Bundle bundle = new Bundle();
                bundle.putInt("BucketId",bucket.getBucketId());
                bundle.putString("BucketName",bucket.getBucketName());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_homeFragment_to_bucketFragment,bundle);
            });
            // 设置布局管理器
            binding.recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(),2));
            // 设置适配器
            binding.recyclerView.setAdapter(homeAdapter);
        }
    }


    // 权限申请回调事件
    @Override
    public void onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireActivity(), "授权成功", Toast.LENGTH_SHORT).show();
            onStart();
        } else {
            Toast.makeText(requireActivity(),"授权失败，软件无法正常使用",Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }
    }
}
