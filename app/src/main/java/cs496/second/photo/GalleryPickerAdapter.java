package cs496.second.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
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

        Bitmap thumb = BitmapDecoder.decodeBitmapFromFile(model.getImagePath(), 180, 180);

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
}