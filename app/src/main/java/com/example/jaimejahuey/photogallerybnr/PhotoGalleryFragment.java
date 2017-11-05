package com.example.jaimejahuey.photogallerybnr;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaimejahuey on 4/4/16.
 */
public class PhotoGalleryFragment extends Fragment
{
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mpageNum = 0;

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container,false);

        mPhotoRecyclerView = v.findViewById(R.id.fragment_photo_gallery_recyclerView);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setUpAdater();

        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destoyed");
    }

    private void setUpAdater(){

        //Confirms that the fragment has been attached to an activity so that getActivyt is not null in the adapter
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if(!recyclerView.canScrollVertically(1)){
                        new FetchItemsTask().execute();
                    }
                }
            });
        }

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>
    {

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

               return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            //Add items since we fetch new items whenever the user hits the bottom of the list
//            mItems = items;
            mItems.addAll(items);
            setUpAdater();

        }
    }

    //viewHolder
    private class PhotoHolder extends RecyclerView.ViewHolder
    {
//        private TextView mTitleTextView;
        private ImageView mItemImageView;

        private PhotoHolder(View itemView){
            super(itemView);

//            mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

//        public void bindGalleryItem(GalleryItem item){
//            mTitleTextView.setText(item.toString());
//        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    //adapter
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems = galleryItems;
        }


        //Inflate the view.
        //Passing the view to the photoHolder so that we can get references to the id of the things in the view
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
        {
//            TextView textView = new TextView(getActivity());
//            return new PhotoHolder(textView);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup,false);
            return new PhotoHolder(view);
        }

        //Set the data, now that view has been created. View we get is the photoHolder.
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int pos)
        {
            GalleryItem galleryItem = mGalleryItems.get(pos);
//            photoHolder.bindGalleryItem(galleryItem);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getmUrl());
        }

        @Override
        public int getItemCount(){
            return mGalleryItems.size();
        }
    }
}
