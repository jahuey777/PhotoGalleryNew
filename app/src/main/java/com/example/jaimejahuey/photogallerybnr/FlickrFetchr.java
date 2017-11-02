package com.example.jaimejahuey.photogallerybnr;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaimejahuey on 4/5/16.
 */
public class FlickrFetchr
{
    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "1888392a573dd1ebcf08ab8b8f9aa480";

    public byte [] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        //Creates the connection with the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try
        {
            //Storing all the bytes into the out ByteArray
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            //Making sure the connection is ok.
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            int bytesRead = 0;
            byte [] buffer = new byte[1024];

            //Reading in the data until the connections runs out of data.
            while ((bytesRead = in.read(buffer))>0)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    //this converts the bytes fetched by getUrlBytes into a String.
    //
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems() {

        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/").buildUpon().appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY).appendQueryParameter("format", "json").appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s").build().toString();
            //extras, url_s tells flickR to include the URL for the small version of he picture if avaiable

            String jsonString = null;
            jsonString = getUrlString(url);

            Log.i(TAG, "received JSON: " + jsonString);

            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Failed to fetch items ", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSon",e);
        }

        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {

        //JsonBody is the top most level.
        //Grabbing photos and then will graby jsonArray inside photosJsonObject
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");

        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();

            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));

            //checking for url
            if(!photoJsonObject.has("url_s"))
                continue;
            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }

    }
}