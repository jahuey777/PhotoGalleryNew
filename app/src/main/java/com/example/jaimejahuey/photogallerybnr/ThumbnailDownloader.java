package com.example.jaimejahuey.photogallerybnr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.lang.annotation.Target;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jaimejahuey on 11/5/17.
 */

//Gave the class a single generic argument, T
    //makes the implementation more flexble, T is the Photoholder object in this case
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    //Handler responsible for queueing download requests as messages onto the ThumbnailDownloader background thread
    //This handler will also be in charge of processing download request messages when they are pulled off the queue
    private Handler mRequestHandler;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;


    private LruCache<T, Bitmap> mMemoryCache;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    //Handler that is passed in from the main thread.
    //This handler is associated with the main threads looper since it was created in the main thread
    //So all messages this handler handles will be done on the main thread
    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);

        mResponseHandler = responseHandler;
    }

    //called before the looper checks the queue for the first time.
    //Called right when getlooper method is called.
    @Override
    protected void onLooperPrepared(){
        Log.v(TAG, " onLooperPrepared called.");

        //Creating cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize = maxMemory/8;

        mMemoryCache = new LruCache<T, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(T key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };

        mRequestHandler = new Handler(){
            //called when a download message is pulled off the queue and is ready to be processed.
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, " Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG, "Got a URL: " + url);

        if(url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }

    }

    public void clearQueue(){
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target){

        try{
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

            //check to see if bitmap already exists in cache
            final Bitmap bitmapCheck = getBitmapFromMemCache(target);

            //IF it exists then set the image and return.
            if(bitmapCheck!=null){
                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmapCheck);
                Log.v(TAG, "Bitmap grabbed from cache");
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            //Since this handler is associated with the main threads looper, then all of this gets run on the main thread
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url){
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                    addBitmapToMemoryCache(target, bitmap);
                }
            });

        }catch (IOException ioe){
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void addBitmapToMemoryCache(T key, Bitmap bitmap){
        if(getBitmapFromMemCache(key)==null)
            mMemoryCache.put(key,bitmap);
    }

    public Bitmap getBitmapFromMemCache(T key){
        return mMemoryCache.get(key);
    }

}
