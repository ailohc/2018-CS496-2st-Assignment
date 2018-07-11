package cs496.second.home;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cs496.second.R;
import cs496.second.photo.InstaAdapter;
import cs496.second.photo.PhotosPair;


public class Second_sub_Activity extends AppCompatActivity {
    private CardView cardView;
    private ArrayList<PhotosPair> photosPairArrayList;
    public ImageView insImage;
    public InstaAdapter instaAdapter;
    private LinearLayoutManager linearLayoutManager;

    RecyclerView myRecyclerView;

    List<PhotosPair> result;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_second_subactivity);
        photosPairArrayList = new ArrayList<>();

        ImageGetTask imageGetTask = new ImageGetTask("key");
        try{
            result = imageGetTask.execute().get();
            Log.d("TestTag","synch imageGEtTask start");
        } catch (Exception e){
            e.printStackTrace();
            Log.d("TestTag","synch imageGEtTask error!");
        }


        cardView = (CardView)findViewById(R.id.cv);
        myRecyclerView = (RecyclerView) findViewById(R.id.secondFragmentRecycler_viewCardView);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        myRecyclerView.setLayoutManager(linearLayoutManager);

        if (instaAdapter == null) {

//            instaAdapter.setData(result);

            instaAdapter = new InstaAdapter(getApplicationContext());
            myRecyclerView.setAdapter(instaAdapter);
        }


    }

    public class ImageGetTask extends AsyncTask<Object, Integer, List<PhotosPair>> {
        private String key;

        public ImageGetTask(String key) {
            this.key = key;
        }

        @Override
        protected List<PhotosPair> doInBackground(Object[] objects) {
            String jsonResponse = "";
            //String urlStr = "http://52.231.70.3:3000/ins/findOne";
            String urlStr = "http://52.231.70.3:3000/ins/get";
            JSONObject json = new JSONObject();

            InputStream inputStream = null;
            Bitmap bitmap;


            try {

                Log.d("TestTag", "imageGet Asynch try01 ");

                HttpURLConnection httpURLConnection = null;
                URL url = new URL(urlStr);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                //httpURLConnection.setRequestProperty("User-Agent",USER_AGENT);

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("TestTag", "imageGet Asynch try02 ");

                    InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

                    Log.d("TestTag", "imageGet Asynch try03 ");

                    String data = readData(in);

                    Log.d("TestTag", "imageGet Asynch try04 ");
                    //Log.d("TestTag", "imageGet Asynch try04-2 "+data);


                    /*
                    JSONObject jsonObject = new JSONObject(data);
                    Log.d("TestTag", "imageGet Asynch json"+jsonObject.toString());

                    String base64 = jsonObject.getString("image");
                    String timestamp = jsonObject.getString("time");

                    Log.d("TestTag", "imageGet Asynch try04-3 "+timestamp);

                    PhotosPair photosPair = new PhotosPair(base64,timestamp);
                    photosPairArrayList.add(photosPair);
                    Log.d("TestTag", "imageGet Asynch try04-4 ");
                    Log.d("TestTag", "imageGet Asynch try04-5 "+photosPairArrayList.size());
                    */

                    JSONArray jsonArray = new JSONArray(data);
                    Log.d("TestTag", "after get request, length is " + jsonArray.length());
                    for(int i=0; i< jsonArray.length(); i++){
                        PhotosPair photosPair;
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String base64 = jsonObject1.getString("image");
                        String timestamp = jsonObject1.getString("time");

                        photosPair = new PhotosPair(base64,timestamp);

                        // TODO : Decide to decode location
                        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        //byte[] imageBytes = baos.toByteArray();
                        //imageBytes = Base64.decode(base64, Base64.DEFAULT);
                        //Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        //insImage.setImageBitmap(decodedImage);

                        photosPairArrayList.add(photosPair);
                    }


                    Log.d("TestTag", "imageGet Asynch try05 ");
                    //readStream(in);
                    //Log.d("TestTag",data);
                    httpURLConnection.disconnect();
                } else {
                    Toast.makeText(getApplicationContext(), "에러발생", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d("TestTag", "imageGet Asynch try06 ");
                e.printStackTrace();
            }

            return photosPairArrayList;
        }

        @Override
        protected void onPostExecute(List<PhotosPair> photosPairList){
            Log.d("TestTag", "Get Image AsynchTask onPostExecuete start");
            instaAdapter.setData(photosPairList);
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
