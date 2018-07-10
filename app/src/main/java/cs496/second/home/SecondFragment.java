package cs496.second.home;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

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
    public Button server_button;
    public ImageView server_image;

    private Animation fab_open, fab_close;
    private FloatingActionButton fab1_instagram;
    private FloatingActionButton fab2_instagram;
    private FloatingActionButton fab3_instagram;
    private Boolean isFabOpen = false;

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

        server_button = (Button) rootview.findViewById(R.id.server_button);

        server_button.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view){
                Log.d("TestTag","button click ");
                ImageGetTask imageGetTask = new ImageGetTask("key");
                imageGetTask.execute();
            }
        });

        server_image = (ImageView) rootview.findViewById(R.id.server_image);

        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);

        fab1_instagram = (FloatingActionButton) rootview.findViewById(R.id.fab1_instagram);
        fab2_instagram = (FloatingActionButton) rootview.findViewById(R.id.fab2_instagram);
        fab3_instagram = (FloatingActionButton) rootview.findViewById(R.id.fab3_instagram);

        fab1_instagram.setOnClickListener(mClickListener);



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

    FloatingActionButton.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View view){
            int id = view.getId();
            switch (id){
                case R.id.fab1_instagram:
                    anim();
                    Toast.makeText(getActivity(), "Floating Action Button", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.fab2_instagram:
                    anim();
                    Toast.makeText(getActivity(), "Button1", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.fab3_instagram:
                    anim();
                    Toast.makeText(getActivity(), "Button2", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public void anim() {
        if (isFabOpen) {
            fab2_instagram.startAnimation(fab_close);
            fab3_instagram.startAnimation(fab_close);
            fab2_instagram.setClickable(false);
            fab3_instagram.setClickable(false);
            isFabOpen = false;
        } else {
            fab2_instagram.startAnimation(fab_open);
            fab3_instagram.startAnimation(fab_open);
            fab2_instagram.setClickable(true);
            fab3_instagram.setClickable(true);
            isFabOpen = true;
        }
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


    public class GetDataTask extends AsyncTask<Object, Integer, List<PhotosModel>> {
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

    public class ImageGetTask extends AsyncTask<Void, Void, String> {
        private String key;

        public ImageGetTask(String key) {
            this.key = key;
        }

        @Override
        protected String doInBackground(Void... params) {
            String jsonResponse = "";
            String urlStr = "http://52.231.70.3:3000/get/image";
            String jsonResponose = "";
            JSONObject json = new JSONObject();
            InputStream inputStream = null;
            Bitmap bitmap;

            try {
                HttpURLConnection httpURLConnection = null;
                URL url = new URL(urlStr);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                //httpURLConnection.setRequestProperty("User-Agent",USER_AGENT);

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                    String data = readData(in);
                    JSONObject jsonObject = new JSONObject(data);
                    Log.d("TestTag", jsonObject.getString("id"));
                    Log.d("TestTag", jsonObject.getString("image"));

                    String imagebase64 = jsonObject.getString("image");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] imageBytes = baos.toByteArray();
                    imageBytes = Base64.decode(imagebase64, Base64.DEFAULT);
                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    server_image.setImageBitmap(decodedImage);


                    //readStream(in);
                    //Log.d("TestTag",data);
                    httpURLConnection.disconnect();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "에러발생", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "AsyncString";
        }
    }


    public void readStream(InputStream in){
        final String data = readData(in);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // server_button.set
            }
        });
    }

    public String readData(InputStream is){
        String data = "";
        Scanner s = new Scanner(is);
        while(s.hasNext()) data += s.nextLine() + "\n";
        s.close();
        return data;
    }

    Handler mHandler = new Handler();



}
