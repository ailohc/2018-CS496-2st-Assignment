package cs496.second.photo;

import android.util.Log;

public class PhotosModel {
    String bucketId, imageBucket, imagePath, imageName;

    public PhotosModel(String bucketId, String imageBucket, String imagePath, String imageName) {
        this.bucketId = bucketId;
        this.imageBucket = imageBucket;
        this.imageName = imageName;
        this.imagePath = imagePath;
        Log.d("MyTag", "PhotoModel");
    }

    public String getBucketId() {
        return bucketId;
    }

    public String getImageBucket() {
        return imageBucket;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImagePath() {
        return imagePath;
    }

}