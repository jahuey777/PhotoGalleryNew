package com.example.jaimejahuey.photogallerybnr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
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

    public ThumbnailDownloader() {
        super(TAG);
    }

    //called before the looper checks the queue for the first time.
    @Override
    protected void onLooperPrepared(){
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
            Log.v(TAG, " url is null in queue");
        }else{
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }

    }

    private void handleRequest(final T target){

        try{
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

        }catch (IOException ioe){
            Log.e(TAG, "Error downloading image", ioe);
        }

    }

}
