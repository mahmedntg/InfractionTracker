package com.example.khalifa.infractiontracker.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.IOException;

/**
 * Created by mhamedsayed on 3/16/2019.
 */

public class SharedUtils {
    public static final String email = "m@m.com";
    public static final String PREF_FCM_TOKEN = "infraction_fcm_token";


    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
