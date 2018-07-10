package cs496.second.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import cs496.second.R;

public class InstaAdapter extends RecyclerView.Adapter<InstaAdapter.CvViewHolder> {
    private Context context;
    static List<PhotosPair> photosPairArrayList = new ArrayList<>();
    private LayoutInflater inflater;

    public InstaAdapter(Context context){
        this.context = context;
    //    this.photosPairArrayList = photosPairArrayList;

        inflater = LayoutInflater.from(context);

    }

    public void setData(List<PhotosPair> data){
        InstaAdapter.photosPairArrayList = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return photosPairArrayList.size(); }


    @Override
    public CvViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View myView = inflater.inflate(R.layout.fragment_second_cardview,parent, false);

        return new CvViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(final CvViewHolder holder, final int position){
        final PhotosPair model = photosPairArrayList.get(position);
        Log.d("TestTag","instaAdapter position " +position);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] imageBytes = baos.toByteArray();
        imageBytes = Base64.decode(model.getBaseString(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        holder.cv_image.setImageBitmap(decodedImage);

        View itemView = holder.itemView;


    }

    public class CvViewHolder extends RecyclerView.ViewHolder{
        View row;
        ImageView cv_image;

        public CvViewHolder(View itemView){
            super(itemView);
            row = itemView;
            cv_image = (ImageView) row.findViewById(R.id.ivcv_image);
        }
    }



}
