package cs496.second.photo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cs496.second.R;
import cs496.second.home.HomeActivity;

public class GalleryPickerAdapter extends RecyclerView.Adapter<GalleryPickerAdapter.MyViewHolder> {

    //define source of MediaStore.Images.Media, internal or external storage
    public static final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final String[] projections =
            {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DISPLAY_NAME};
    private Context context;
    private LayoutInflater inflater;
    public static String sortOrder = MediaStore.Images.Media.DATA + " DESC";
    int flag = 0;

    static List<PhotosModel> data = new ArrayList<>();

    public GalleryPickerAdapter(Context context) {
        this.context = context;

        inflater = LayoutInflater.from(context);
    }


    public void setData(List<PhotosModel> data) {
        GalleryPickerAdapter.data = data;
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View myView = inflater.inflate(R.layout.grid_item, parent, false);
        return new MyViewHolder(myView);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final PhotosModel model = data.get(position);
        Log.d("TestTag", "gallerypick adapter position : "+ position);
//        Log.d("TestTag","gallery pick adapter name "+model.getImageName());

        Bitmap thumb = BitmapDecoder.decodeBitmapFromFile(model.getImagePath(), 180, 180);

        String urlStr = "http://52.231.70.3:3000/post/image";

        //ImagePostTask imagePostTask = new ImagePostTask(thumb, urlStr, model);
        //imagePostTask.execute();

        holder.iv_grid.setImageBitmap(thumb);

        //if true display bucket name or image name
        if (PhotosData.dir) {
            holder.tv_grid.setText(model.getImageName() == null ? model.getImageBucket() : model.getImageName());
        } else {
            holder.tv_grid.setVisibility(View.GONE);
        }

        View itemView = holder.itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if true view image as gridview
                if (PhotosData.dir) {
                    Log.d("TestTag","thas would show all images");
                    Intent intent = new Intent(context,ImagesGridActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("POS",position);
                    context.startActivity(intent);

                } else {
                    Log.d("MyTag","else0");
                    //add all images to new array list,used to view image as slide show
                    ArrayList<String> paths = new ArrayList<>();
                    Log.d("MyTag","else1");
                    for (int i = 0; i < data.size(); i++) {
                        paths.add(data.get(i).getImagePath());
                        Log.d("Paths", paths.get(i));
                    }
                    Log.d("MyTag","else2");

                    Intent intent = new Intent(context, ImageViewActivity.class);
                    Log.d("MyTag","else3");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("MyTag","else4");
                    intent.putStringArrayListExtra("paths", paths);
                    Log.d("MyTag","else5");
                    intent.putExtra("Position",position);
                    Log.d("MyTag","else6");
                    context.startActivity(intent);
                    Log.d("MyTag","else7    ");
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(context, "Long Click", Toast.LENGTH_SHORT).show();
                Log.d("TestTag","LongClick");

                Snackbar.make(v, "Wanna Share?~", Snackbar.LENGTH_SHORT).setAction("Yeap", new View.OnClickListener(){

                    @Override
                    public void onClick(View view){
                        Toast.makeText(context, "Uploading... "+model.getImageName(), Toast.LENGTH_SHORT).show();

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Shared!... "+position, Toast.LENGTH_SHORT).show();
                            }
                        }, 1500);


                        Runnable mToast = new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Shared!...", Toast.LENGTH_SHORT).show();
                            }
                        };

                    }

                }).show();


                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View row;
        ImageView iv_grid;
        TextView tv_grid;

        public MyViewHolder(View itemView) {
            super(itemView);
            row = itemView;
            iv_grid = (ImageView) row.findViewById(R.id.gv_image);
            tv_grid = (TextView) row.findViewById(R.id.gv_title);

        }
    }


    public class ImagePostTask extends AsyncTask<Void, Void, String>{
        private Bitmap bitmap;
        private String urlString;
        private PhotosModel photosModel;

        public ImagePostTask(Bitmap bitmap, String urlString, PhotosModel photosModel){
            this.bitmap = bitmap;
            this.urlString = urlString;
            this.photosModel = photosModel;

        }

        @Override
        protected String doInBackground(Void... params){
            String jsonResponse = "";
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10 , byteArrayOS);

            String encodedString = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);

            /*try{
                FileWriter fileWriter = new FileWriter("testimageoutput.txt");
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(encodedString);
                bufferedWriter.close();
                Log.d("TestTag", "file save excepttion!");
            } catch (IOException e){
                Log.d("TestTag", "file save excepttion!");
            }*/

            //Log.d("TestTag",encodedString);
            //Log.d("TestTag2",photosModel.getImageName());

            String jsonResponose = "";

            JSONObject json = new JSONObject();

            InputStream inputStream = null;

            if(flag==0){
                Log.d("TestTag","gallerypicadapter, flag should be used only once");
                flag++;
                try {

                    // HttpURLConnection 객체 생성.
                    HttpURLConnection httpURLConnection = null;
                    URL url = new URL(urlString);
                    // URL 연결 (웹페이지 URL 연결.)
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    // 요청 방식 선택 (GET, POST)
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    //httpURLConnection.setRequestProperty("Name", "testname");
                    //httpURLConnection.setRequestProperty("Image", "testimage");
                    //httpURLConnection.setRequestProperty("ApiId","testAppid");
                    //httpURLConnection.setRequestProperty("photo","testphoto");

                    //json.accumulate("id",photosModel.getImageName());
                    json.accumulate("id","1234");
                    json.accumulate("image",encodedString);

                    OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                    writer.write(json.toString());
                    writer.flush();
                    writer.close();
                    //Log.d("TestTag2",json.toString());
//                    Log.d("TestTag2",photosModel.getImageName());
                    /*
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(encodedString.getBytes("euc-kr"));
                    os.flush();
                    os.close();*/

                    int responseCode = httpURLConnection.getResponseCode();

                    Log.d("TestTag","responsecode is : "+ responseCode);

                    httpURLConnection.disconnect();

                    return Integer.toString(responseCode);

                /*
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    readStream(in);
                    urlConnection.disconnect();
                }else{
                    Toast.makeText(getApplicationContext(), "에러발생", Toast.LENGTH_SHORT).show();
                }
                */

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    Log.d("TestTag","gallerypicadapter ALL ! exception ");
                    e.printStackTrace();
                } finally {
                    //if(inputStream != null)
                        //inputStream.close();
                }

                // Log.d("TestTag","gallerypicadapter, flag should be used only once");

            }



            return "-1";
        }


    }
}