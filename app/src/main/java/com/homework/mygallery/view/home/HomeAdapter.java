package com.homework.mygallery.view.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.homework.mygallery.R;
import com.homework.mygallery.model.Bucket;

import java.io.File;
import java.util.List;

/**
 * @introduction： 相册列表适配器
 * @author： 林锦焜
 * @time： 2022/7/27 15:43
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.BucketHolder> {

    private final List<Bucket> buckets;

    private final OnToPhotoListener onToPhotoListener;

    public HomeAdapter(List<Bucket> buckets, OnToPhotoListener onToPhotoListener) {
        this.buckets = buckets;
        this.onToPhotoListener = onToPhotoListener;
    }

    @NonNull
    @Override
    public BucketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_bucket_view, parent,false);
        return new BucketHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BucketHolder holder, int position) {
        Bucket bucket = buckets.get(position);
        // 设置相册名称
        holder.bucketName.setText(bucket.getBucketName());
        // 加载图片
        Glide.with(holder.coverView.getContext()).load(new File(bucket.getCover())).into(holder.coverView);
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onToPhotoListener != null) {
                onToPhotoListener.onClick(bucket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buckets.size();
    }

    public static class BucketHolder extends RecyclerView.ViewHolder {

        ImageView coverView;

        TextView bucketName;

        public BucketHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.coverView);
            bucketName = itemView.findViewById(R.id.bucketName);
        }
    }


    public interface OnToPhotoListener {
        void onClick(Bucket bucket);
    }

}
