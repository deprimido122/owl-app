package com.example.mentoringapp;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    CameraSurfaceView cameraView;
    // https://mentoring.cognitiveservices.azure.com
    // vision/v3.2/read/analyze
    // Translation-key (Key2) : a7fa222d5e8a4c81bd2177f7b7f27c4f
    // OCR-key (key2) : 6a4551b6afd04307924b8b0b1001ddd3

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        cameraView = new CameraSurfaceView(this);
        previewFrame.addView(cameraView);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();
            }
        });

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.CAMERA,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        Toast.makeText(getApplicationContext(), "허용된 권한 갯수 : " + permissions.size(), Toast.LENGTH_LONG);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        Toast.makeText(getApplicationContext(), "거부된 권한 갯수 : " + permissions.size(), Toast.LENGTH_LONG);
                    }
                })
                .start();

    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://mentoring.cognitiveservices.azure.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    OCRService ocrService = retrofit.create(OCRService.class);
                    RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), data);
                    Call<OCRResponse> response = ocrService.recognize("6a4551b6afd04307924b8b0b1001ddd3", body);
                    response.enqueue(new Callback<OCRResponse>() {
                        @Override
                        public void onResponse(Call<OCRResponse> call, Response<OCRResponse> response) {
                            OCRResponse body = response.body();

                            ArrayList<String> texts = new ArrayList<>();
                            ArrayList<String> boundingBoxes = new ArrayList<>();
                            if(body == null){
                                return;
                            }

                            List<OCRResponse.Region> regions = body.regions;
                            for (int i=0; i<regions.size();  i++) {
                                List<OCRResponse.Line> lines = regions.get(i).lines;
                                for (int j=0; j<lines.size(); j++) {
                                    List<OCRResponse.Word> words = lines.get(j).words;
                                    for (int k=0; k< words.size(); k++) {
                                        String originalText = words.get(k).text;
                                        String boundingBox = words.get(k).boundingBox;
                                        // String translateResult = translate(originalText);
                                        texts.add(originalText);
                                        boundingBoxes.add(boundingBox);
                                    }
                                }
                            }
                            translate(texts, boundingBoxes);
                        }

                        @Override
                        public void onFailure(Call<OCRResponse> call, Throwable t) {
                            Log.e("failure", t.getMessage());

                        }
                    });

                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void translate(ArrayList<String> recognizedTexts, ArrayList<String> boundingBoxes){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cognitive.microsofttranslator.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TranslationService translateService = retrofit.create(TranslationService.class);

        List<TranslateRequest> data = new ArrayList<TranslateRequest>();
        for(int i=0; i<recognizedTexts.size(); i++){
            data.add(new TranslateRequest(recognizedTexts.get(i)));
        }
        Call<List<TranslateResponse>> translateCall = translateService.translate(
                "c141a4cbc83b41b7b488f4b2410ce0a3",
                UUID.randomUUID().toString(),
                data);
        translateCall.enqueue(new Callback<List<TranslateResponse>>() {
            @Override
            public void onResponse(Call<List<TranslateResponse>> call, Response<List<TranslateResponse>> response) {

                List<TranslateResponse> body = response.body();
                /*
                Analyzed Results
                {origianlText}가 {boundingBox}위치에서 발견되어 {translateResult}로 번역되었습니다
                {origianlText}가 {boundingBox}위치에서 발견되어 {translateResult}로 번역되었습니다
                {origianlText}가 {boundingBox}위치에서 발견되어 {translateResult}로 번역되었습니다
                {origianlText}가 {boundingBox}위치에서 발견되어 {translateResult}로 번역되었습니다
                {origianlText}가 {boundingBox}위치에서 발견되어 {translateResult}로 번역되었습니다
                */
                // "
                String message = "Analyzed Results\n";
                if(body==null){
                    message = "not found";
                }
                else {
                    for (int i = 0; i < body.size(); i++) {
                        message += recognizedTexts.get(i) + " 가 " + boundingBoxes.get(i) + " 위치에서 발견되어 " + body.get(i).translations.get(0).text + "로 번역되었습니다\n";
                    }
                }
                Snackbar snackbar = Snackbar.make(findViewById(R.id.layout_main), message, Snackbar.LENGTH_LONG).setDuration(20000);
                TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setMaxLines(100);
                snackbar.show();
            }

            @Override
            public void onFailure(Call<List<TranslateResponse>> call, Throwable t) {

            }
        });
    }
}

