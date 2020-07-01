package com.example.healthapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthapplication.model.User;
import com.example.healthapplication.util.FileUtil;

import org.json.JSONObject;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalyzeFragment extends Fragment {

    private Button ctButton;
    private ImageView imageView;
    private CardView cardViewCT;
    private CardView cardViewCancer;
    private CardView bmiCard;
    private CardView bloodCard;
    private String choice;
    private final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    public AnalyzeFragment() {
        // Required empty public constructor
    }

    public static AnalyzeFragment newInstance() {
        AnalyzeFragment fragment = new AnalyzeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static final int RC_CHOOSE_PHOTO = 2;

    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_CHOOSE_PHOTO:   //相册选择照片权限申请返回
                choosePhoto();
                break;
        }
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_analyze, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("智能分析");

        imageView = view.findViewById(R.id.img);
        cardViewCancer = view.findViewById(R.id.cancerCard);
        cardViewCancer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "淋巴切片诊断结果";
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CHOOSE_PHOTO);
                } else {
                    //已授权，获取照片
                    choosePhoto();
                }
            }
        });
        cardViewCT = view.findViewById(R.id.ctCard);
        cardViewCT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "CT诊断结果";
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CHOOSE_PHOTO);
                } else {
                    //已授权，获取照片
                    choosePhoto();
                }
            }
        });
        bmiCard = view.findViewById(R.id.BmiCard);
        bmiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),BMIActivity.class);
                startActivity(intent);
            }
        });
        bloodCard = view.findViewById(R.id.BPCard);
        bloodCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BPActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                Uri uri = data.getData();
                String image_path  = FileUtil.getFilePathByUri(getContext(), uri);
                Log.d("logaa",image_path);


                if (!TextUtils.isEmpty(image_path )) {


                    final File file = new File(image_path );
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            OkHttpClient mOkHttpClient = new OkHttpClient();
                            MultipartBody.Builder builder = new MultipartBody.Builder();
                            builder.setType(MultipartBody.FORM)
                                    .addFormDataPart("picture", "img" + "_" + System.currentTimeMillis() + ".jpg",
                                            RequestBody.create(MEDIA_TYPE_PNG, file));



                            RequestBody requestBody = builder.build();
                            Request.Builder reqBuilder = new Request.Builder();

                            Request request = reqBuilder
                                    .url("http://47.100.32.161:8080/POST/CTRecord")
                                    .addHeader("Authorization", User.getInstance().getToken())
                                    .post(requestBody)
                                    .build();

                            try{

                                Response response = mOkHttpClient.newCall(request).execute();
                                Log.d("logaa", "响应码 " + response.code());
                                String resultValue = response.body().string();
                                JSONObject jsonObject = new JSONObject(resultValue);

                                Object dataObject = jsonObject.getJSONObject("data");
                                String s2=dataObject.toString();
                                JSONObject userDataJson = new JSONObject(s2);

                                String url = userDataJson.getString("picture_url");
                                String answer =userDataJson.getString("answer");


                                Log.d("logaa", "响应体 " + resultValue);

                                Intent intent = new Intent(getActivity(),AnalyzeDetailActivity.class);
                                intent.putExtra("choice",choice);
                                intent.putExtra("img",url );
                                intent.putExtra("answer",answer);
                                startActivity(intent);


                            } catch (Exception e) {

                                e.printStackTrace();

                            }

                        }
                    }).start();


                }
                break;
        }




    }






































}
