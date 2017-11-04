package com.example.jaimejahuey.photogallerybnr.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jaimejahuey on 11/3/17.
 */

public class Photo {
    @SerializedName("id") private String mId;
    @SerializedName("title") private String mTitle;
    @SerializedName("url_s") private String mUrl;

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
