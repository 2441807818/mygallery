package com.homework.mygallery.model;

/**
 * @introduction： 相册类
 * @author： 林锦焜
 * @time： 2022/7/27 15:18
 */
public class Bucket {

    // 相册名称
    private String BucketName;

    // 相册编号
    private int BucketId;

    // 相册封面
    private String cover;


    public Bucket() {
    }

    public String getBucketName() {
        return BucketName;
    }

    public void setBucketName(String bucketName) {
        BucketName = bucketName;
    }

    public int getBucketId() {
        return BucketId;
    }

    public void setBucketId(int bucketId) {
        BucketId = bucketId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "BucketName='" + BucketName + '\'' +
                ", BucketId=" + BucketId +
                ", cover='" + cover + '\'' +
                '}';
    }
}
