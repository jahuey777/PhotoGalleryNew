package com.example.jaimejahuey.photogallerybnr.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jaimejahuey on 11/3/17.
 */

    //This response is what is in within the "photos":{page,... photo[]}
    //photo is the array that has all the photos within in
public class PhotosResponse {

    @SerializedName("page") String mPage;
    @SerializedName("pages") String mPages;
    @SerializedName("perpage") String mPerPage;
    @SerializedName("total") String mTotal;

    @SerializedName("photo") List<Photo> mPhotos;

    //Public constructor is necessary for collections.
    public PhotosResponse(){
        mPhotos = new ArrayList<>();
    }

    public List<Photo> getPhotos(){
        return mPhotos;
    }
}
