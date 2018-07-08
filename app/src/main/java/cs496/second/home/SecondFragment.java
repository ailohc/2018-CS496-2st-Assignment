package cs496.second.home;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cs496.second.R;
import cs496.second.photo.GalleryPickerAdapter;
import cs496.second.photo.PhotosData;
import cs496.second.photo.PhotosModel;

public class SecondFragment extends Fragment {
    public SecondFragment(){
        Log.d("TestTag","start SecondFragment");
    }

    RecyclerView mRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private static final int URL_LOADER = 0;
    public GalleryPickerAdapter galleryPickerAdapter;
    String permissions= new String (Manifest.permission.READ_EXTERNAL_STORAGE);
    private int PERMISSION_REQUEST_CODE = 200;

    AlbumView albumView = new AlbumView();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //final View rootview = inflater.inflate(R.layout.fragment_second, container, false);
        Log.d("TestTag","onCreateView start");

        View rootview = inflater.inflate(R.layout.fragment_second,null);
        mRecyclerView = (RecyclerView) rootview.findViewById(R.id.secondFragmentRecycler_view);
        gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(),2);  // 이거는 gallery를 보여주는 것과는 상관없음
        mRecyclerView.setLayoutManager(gridLayoutManager);

        Log.d("TestTag","onCreateView after inflate");

        if (galleryPickerAdapter == null) {
            galleryPickerAdapter = new GalleryPickerAdapter(getActivity().getApplicationContext());
            mRecyclerView.setAdapter(galleryPickerAdapter);
        }

        Log.d("TestTag","onCreateView before return");

        return rootview;
    }



    @Override
    public void onResume() {
        super.onResume();
        if(checkPermission()) {
            Log.d("TestTag","onResume start");
            albumView.loadAlbum();
            Log.d("TestTag","onResume finish");
        }
    }

    private  boolean checkPermission() {
        Log.d("TestTag","checkPermission start");
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        result = ContextCompat.checkSelfPermission(getActivity(),permissions);
        if (result != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(permissions);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        if(permsRequestCode==200){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                albumView.loadAlbum();
            } else {
                Toast.makeText(getActivity(),  "Please give permission to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public class AlbumView implements LoaderManager.LoaderCallbacks<Cursor>{

        public void loadAlbum(){
            getActivity().getLoaderManager().restartLoader(URL_LOADER, null, this);//restart the loader manager by invoking getSupportLoaderManager
            Log.d("TestTag", "loadAlbum");

        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            Log.d("TestTag", "onCreateLoader");

            return new CursorLoader(getActivity().getApplicationContext(),
                    GalleryPickerAdapter.uri,
                    GalleryPickerAdapter.projections,
                    null,
                    null,
                    GalleryPickerAdapter.sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // AsyncTask here?
            Log.d("TestTag", "onLoadFinished");
            GetDataTask getDataTask = new GetDataTask(true, cursor);
            getDataTask.execute();

            // galleryPickerAdapter.setData(PhotosData.getData(true, cursor));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.d("TestTag", "onLoaderReset");
        }
    }


    private class GetDataTask extends AsyncTask<Object, Integer, List<PhotosModel>> {
        private Boolean home;
        private Cursor cursor;

        public GetDataTask(Boolean home, Cursor cursor){
            Log.d("TestTag", "AsyncTask constructor");
            this.home = home;
            this.cursor = cursor;
        }

        @Override
        protected List<PhotosModel> doInBackground(Object[] objects){
            Log.d("TestTag", "AsyncTask boInBackground start");
            List<PhotosModel> photos = new ArrayList<>();
            photos = PhotosData.getData(home,cursor);

            return photos;
        }

        @Override
        protected void onPostExecute(List<PhotosModel> photosModelList){
            // Actually
            Log.d("TestTag", "AsyncTask onPostExecute start");
            List<PhotosModel> photos = photosModelList;

            galleryPickerAdapter.setData(photos);
        }

    }



}
