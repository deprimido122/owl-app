package com.example.mentoringapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface TranslationService {
    @POST("/translate?api-version=3.0&to=ko")
    @Headers({
            "Content-Type:application/json",
            "charset:UTF-8"
    })
    Call<List<TranslateResponse>> translate(
            @Header("Ocp-Apim-Subscription-Key")String key,
            @Header("X-ClientTraceId")String uuid,
            @Body List<TranslateRequest> data
    );
}