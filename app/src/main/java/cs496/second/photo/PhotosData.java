package cs496.second.photo;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PhotosData {
    public static boolean dir;


    public static List<PhotosModel> getData(boolean home, Cursor cursor) {
        Log.d("TestTag","photosData, getData start / "+ String.valueOf(home) );

        dir = home;
        List<PhotosModel> photos = new ArrayList<>();
        List<String> bucketIds = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();

        String[] projections = GalleryPickerAdapter.projections;
        while (cursor.moveToNext()) {
            String imageName = cursor.getString(cursor.getColumnIndex(projections[4]));
            String imageBucket = cursor.getString(cursor.getColumnIndex(projections[2]));
            String bucketId = cursor.getString(cursor.getColumnIndex(projections[3]));
            String imagePath = cursor.getString(cursor.getColumnIndex(projections[1]));
            PhotosModel model;

            if (home) {
                model = new PhotosModel(bucketId, imageBucket, imagePath, null);
                if (!bucketIds.contains(bucketId)) {
                    photos.add(model);
                    bucketIds.add(bucketId);
                }
            } else {
                model = new PhotosModel(bucketId, imageBucket, imagePath, imageName);

                if (!imagePaths.contains(imagePath)) {
                    photos.add(model);
                    imagePaths.add(imagePath);
                }
            }
        }
        Log.d("TestTag","photosData, getData finished" );
        return photos;
    }
}