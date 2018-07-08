package cs496.second.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cs496.second.R;

public class ImageViewActivity extends AppCompatActivity {
    TextView totalCount,currentCount;
    ViewPager viewPager;
    ViewPagerAdapter pageradapter;
    int position;
    ArrayList<String> list;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);
        context = getApplicationContext();
        totalCount = (TextView) findViewById(R.id.total_images);
        currentCount = (TextView) findViewById(R.id.current_image);
        viewPager = (ViewPager) findViewById(R.id.pager);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("Position");
        list = intent.getStringArrayListExtra("paths");


        pageradapter = new ViewPagerAdapter(context,list);
        viewPager.setAdapter(pageradapter);
        viewPager.setCurrentItem(position);

        currentCount.setText((position+1) +"");
        totalCount.setText(list.size()+"");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentCount.setText((position+1)+"");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater mLayoutInflater;
        ArrayList<String> list;

        public ViewPagerAdapter(Context context, ArrayList<String> list) {
            this.list = list;
            this.context = context;
            mLayoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.pager_image);

            imageView.setImageBitmap(BitmapFactory.decodeFile(list.get(position)));
            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }

    }
}
