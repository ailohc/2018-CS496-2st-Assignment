package cs496.second.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cs496.second.R;

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

 //       String urlStr = "http://52.231.70.3:3000/post/image";

        holder.iv_grid.setImageBitmap(thumb);

        //if true display bucket name or image name
        if (PhotosData.dir) {
            holder.tv_grid.setText(model.getImageName() != null ? model.getImageBucket() : model.getImageName());
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

                        ImagePostTask imagePostTask = new ImagePostTask(thumb);
                        imagePostTask.execute();

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Shared!... ", Toast.LENGTH_SHORT).show();
                                Log.d("TestTag","time is : "+testToday());
                            }
                        }, 1500);
                    }
                }).show();

                return true;
            }
        });

    }

    public String testToday(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Calendar c1 = Calendar.getInstance();
            String strToday = sdf.format(c1.getTime());
            return strToday;
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

        public ImagePostTask(Bitmap bitmap){
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(Void... params){
            String jsonResponse = "";
            String urlStr = "http://52.231.70.3:3000/ins/post";

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOS);

            String encodedString = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
            //Log.d("TestTag",encodedString);
            //Log.d("TestTag2",photosModel.getImageName());
            JSONObject json = new JSONObject();
            InputStream inputStream = null;

            Log.d("TestTag","gallerypicadapter, flag should be used only once");
            try {
                URL url = new URL(urlStr);
                // HttpURLConnection 객체 생성.
                HttpURLConnection httpURLConnection = null;
                // URL 연결 (웹페이지 URL 연결.)
                httpURLConnection = (HttpURLConnection) url.openConnection();
                // 요청 방식 선택 (GET, POST)
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");


                json.accumulate("time",testToday());
                json.accumulate("image",encodedString);

                OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                writer.write(json.toString());
                writer.flush();
                writer.close();

                int responseCode = httpURLConnection.getResponseCode();

                Log.d("TestTag","responsecode is : "+ responseCode);

                httpURLConnection.disconnect();

                return Integer.toString(responseCode);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                Log.d("TestTag","gallerypicadapter ALL ! exception ");
                e.printStackTrace();
            }


            return "-1";
        }


    }
}