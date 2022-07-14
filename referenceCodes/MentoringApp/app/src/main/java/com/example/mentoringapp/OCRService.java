package com.example.mentoringapp;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface OCRService {
    @POST("/vision/v3.1/ocr?overload=stream&detectOrientation=True")
    @Headers({
            "Content-Type:application/octet-stream"
    })
    Call<OCRResponse> recognize(@Header("Ocp-Apim-Subscription-Key")String key, @Body RequestBody data);
}
