package cs496.second.photo;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cs496.second.R;

public class ImagesGridActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    RecyclerView mRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private static final int URL_LOADER = 0;
    GalleryPickerAdapter galleryPickerAdapter;
    static int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_grid);

        Intent intent = getIntent();
        position = intent.getIntExtra("POS",0);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(),2); // number of images in a row

        mRecyclerView.setLayoutManager(gridLayoutManager);
        int spacing = 0; // 50px
        boolean includeEdge = false;
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, includeEdge));

        getLoaderManager().initLoader(URL_LOADER, null, this);

        if (galleryPickerAdapter == null) {
            galleryPickerAdapter = new GalleryPickerAdapter(getApplicationContext());
            mRecyclerView.setAdapter(galleryPickerAdapter);
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == URL_LOADER) {
            return new CursorLoader(getApplicationContext(),
                    GalleryPickerAdapter.uri,
                    GalleryPickerAdapter.projections,
                    GalleryPickerAdapter.projections[3] + " = \"" + GalleryPickerAdapter.data.get(position).getBucketId() + "\"",
                    null,
                    GalleryPickerAdapter.sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Asynch here
        GetDataTaskImageGrid getDataTaskImageGrid = new GetDataTaskImageGrid(false, cursor);
        getDataTaskImageGrid.execute();
        //galleryPickerAdapter.setData(PhotosData.getData(false, cursor));
        //galleryPickerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        galleryPickerAdapter.notifyDataSetChanged();
    }

    private class GetDataTaskImageGrid extends AsyncTask<Object, Integer, List<PhotosModel>> {
        private Boolean home;
        private Cursor cursor;

        public GetDataTaskImageGrid(Boolean home, Cursor cursor){
            Log.d("TestTag", "AsyncTaskImageGrid constructor");
            this.home = home;
            this.cursor = cursor;
        }

        @Override
        protected List<PhotosModel> doInBackground(Object[] objects){
            Log.d("TestTag", "AsyncTaskImageGrid boInBackground start");
            List<PhotosModel> photos = new ArrayList<>();
            photos = PhotosData.getData(home,cursor);

            return photos;
        }

        @Override
        protected void onPostExecute(List<PhotosModel> photosModelList){
            // Actually
            Log.d("TestTag", "AsyncTaskImageGrid onPostExecute start");
            //List<PhotosModel> photos = photosModelList;

//            galleryPickerAdapter.setData(photos);
            galleryPickerAdapter.setData(photosModelList);
            galleryPickerAdapter.notifyDataSetChanged();
        }

    }





}
