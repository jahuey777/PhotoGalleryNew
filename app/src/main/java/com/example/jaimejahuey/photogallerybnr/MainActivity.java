package com.example.jaimejahuey.photogallerybnr;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity {

    //Api Key 1888392a573dd1ebcf08ab8b8f9aa480
    //Secret 71987592e9d4766e

    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

}

