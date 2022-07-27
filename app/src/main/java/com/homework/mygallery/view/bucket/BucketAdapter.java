package com.homework.mygallery.view.bucket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.homework.mygallery.R;

import java.io.File;
import java.util.ArrayList;

/**
 * @introduction：
 * @author： 林锦焜
 * @time： 2022/7/27 15:43
 */
public class BucketAdapter extends RecyclerView.Adapter<BucketAdapter.BucketHolder> {

    private final ArrayList<String> photos;

    private final OnToBigPhotoListener onToBigPhotoListener;

    public BucketAdapter(ArrayList<String> photos, OnToBigPhotoListener onToBigPhotoListener) {
        this.photos = photos;
        this.onToBigPhotoListener = onToBigPhotoListener;
    }

    @NonNull
    @Override
    public BucketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_photo_view, parent,false);
        return new BucketHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BucketHolder holder, int position) {
        String path = photos.get(position);
        Glide.with(holder.coverView.getContext()).load(new File(path)).into(holder.coverView);
        holder.itemView.setOnClickListener(v -> {
            if (onToBigPhotoListener != null) {
                onToBigPhotoListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class BucketHolder extends RecyclerView.ViewHolder {

        ImageView coverView;

        public BucketHolder(@NonNull View itemView) {
            super(itemView);
            coverView = itemView.findViewById(R.id.coverView);
        }
    }

    public interface OnToBigPhotoListener {
        void onClick(int cur);
    }

}
