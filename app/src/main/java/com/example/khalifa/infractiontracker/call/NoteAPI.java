package com.example.khalifa.infractiontracker.call;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NoteAPI {

    @POST("fcm/send")
    Call<NoteRes> sendNote(@Body NoteReq request);
}
