package com.homework.mygallery.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.homework.mygallery.model.Bucket;

import java.util.ArrayList;
import java.util.List;

/**
 * @introduction： 相册工具类
 * @author： 林锦焜
 * @time： 2022/7/27 15:23
 */
public class BucketUtil {

    public static List<Bucket> getBuckets(Context context) {
        List<Bucket> buckets = new ArrayList<>();
        // 内容接收者
        ContentResolver resolver = context.getContentResolver();
        // 目标Uri
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        // 筛选列值
        String[] projection;
        // 结果游标
        Cursor cursor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 适配android11
            projection = new String[]{
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATA};
            Bundle bundle = new Bundle();
            // 给BUCKET_DISPLAY_NAME分组
            bundle.putString(ContentResolver.QUERY_ARG_SQL_GROUP_BY,MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            // 设置排序
            bundle.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER,"date_added desc");
            cursor = resolver.query(uri, projection, bundle, null);
        } else {
            projection = new String[]{
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATA
            };
            cursor = resolver.query(uri, projection, "0=0) group by (bucket_display_name", null, "date_added desc");
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Bucket bucket = new Bucket();
                bucket.setBucketName(cursor.getString(0));
                bucket.setBucketId(cursor.getInt(1));
                bucket.setCover(cursor.getString(2));
                buckets.add(bucket);
            }
            cursor.close();
        }
        return buckets;
    }

}
