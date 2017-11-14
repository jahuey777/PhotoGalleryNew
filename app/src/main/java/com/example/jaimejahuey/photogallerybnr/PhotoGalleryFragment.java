package com.example.jaimejahuey.photogallerybnr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaimejahuey on 4/4/16.
 */
public class PhotoGalleryFragment extends Fragment
{
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mpageNum = 0;

//    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
//        new FetchItemsTask().execute();
        updateItems();

//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);

//        PollService.setServiceAlarm(getActivity(), true);

//        Handler responseHandler = new Handler();
//
//        //passing the main threads handler to the other thread so that we can update the UI whenever the photo is downloaded
//        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
//        //Setting the lister.
//
//        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
//            @Override
//            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
//
//                if(isAdded()){
//                    Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
//                    target.bindDrawable(drawable);
//                }
//            }
//        });
//
//        mThumbnailDownloader.start();
//        mThumbnailDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container,false);

        mPhotoRecyclerView = v.findViewById(R.id.fragment_photo_gallery_recyclerView);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setUpAdater(false);

        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

//        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();

//        mThumbnailDownloader.clearQueue();
        Log.i(TAG, "onDestroyView called");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v(TAG, " QueryTextSubmit "+ query);
                QueryPreferences.setStoreQuery(getActivity(), query);
                new FetchItemsTask(query, true).execute();
                hideKeyboard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.v(TAG, " QueryTextChange "+ newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(!PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoreQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                //If service alarm is on, then we don't want it to create anothe PendingIntent
                boolean shoudStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shoudStartAlarm);

                //Update the toolbar
                getActivity().invalidateOptionsMenu();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
        hideKeyboard();
    }

    private void hideKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void setUpAdater(boolean createNewAdapter){

        //Confirms that the fragment has been attached to an activity so that getActivyt is not null in the adapter
        if(isAdded()){
            if(mPhotoAdapter == null||createNewAdapter){
                mPhotoAdapter= new PhotoAdapter(mItems);
                mPhotoRecyclerView.setAdapter(mPhotoAdapter);
            }else{
                mPhotoAdapter.notifyDataSetChanged();
            }

//            mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                    super.onScrollStateChanged(recyclerView, newState);
//
//                    if(!recyclerView.canScrollVertically(1)){
//                    }
//                }
//            });
        }

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>>
    {
        private String mQuery;
        private  boolean mNewSearch;
        ProgressDialog mProgressDialog;

        public FetchItemsTask(String query){
            mQuery = query;
            mNewSearch = false;
        }

        public FetchItemsTask(String query, boolean newSearch){
            mQuery = query;
            mNewSearch = newSearch;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(getActivity());

            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);

            if(mProgressDialog!=null)
            mProgressDialog.show();
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

//               return new FlickrFetchr().fetchItems();
            if (mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else{
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            //Add items since we fetch new items whenever the user hits the bottom of the list
//            mItems = items;
            if(mProgressDialog!=null &&mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }

            if (mNewSearch){
                mItems = new ArrayList<>();
                mItems.addAll(items);
                setUpAdater(true);

            }else{
                mItems.addAll(items);
                setUpAdater(false);
            }

            Log.v("After network size"," " + mItems.size());
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
//            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getmUrl());
            Picasso.with(getActivity()).load(galleryItem.getmUrl()).into(photoHolder.mItemImageView);

            if(pos == mItems.size()-1){
                updateItems();
            }

        }

        @Override
        public int getItemCount(){
            return mGalleryItems.size();
        }
    }
}
