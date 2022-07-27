package com.homework.mygallery.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * @introduction： 图片工具类
 * @author： 林锦焜
 * @time： 2022/7/27 16:20
 */
public class PhotoUtil {
    /**
     * 获取相册的图片列表
     */
    public static ArrayList<String> getPhotos(Context context,int BucketId) {
        ArrayList<String> photos = new ArrayList<>();
        // 内容接收者
        ContentResolver resolver = context.getContentResolver();
        // 目标Uri
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 筛选列值
        String[] projection = new String[]{
                MediaStore.Images.Media.DATA
        };
        // 结果游标
        Cursor cursor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 适配android11
            Bundle bundle = new Bundle();
            // 设置查询条件
            bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "bucket_id=?");
            bundle.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[]{String.valueOf(BucketId)});
            cursor = resolver.query(uri, projection, bundle, null);
        } else {
            cursor = resolver.query(uri, projection, "bucket_id=?", new String[]{String.valueOf(BucketId)}, null);
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                photos.add(cursor.getString(0));
            }
            cursor.close();
        }
        return photos;
    }

}
