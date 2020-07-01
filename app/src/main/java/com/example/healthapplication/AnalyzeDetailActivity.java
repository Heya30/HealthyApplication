package com.example.healthapplication;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

public class AnalyzeDetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;

    private TextView answertv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_analyze_detail);


        imageView = findViewById(R.id.analyze_img);
        textView = findViewById(R.id.choice_tv);
        answertv = findViewById(R.id.analyze_answer);

        TextView backButon;
        backButon= findViewById(R.id.back);
        backButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Intent intent = getIntent();
        String choice = intent.getStringExtra("choice");
        String url = intent.getStringExtra("img");
        String answer = intent.getStringExtra("answer");

        answertv.setText("检测结果："+ answer);

        textView.setText(choice);
        //将照片显示在 ivImage上
        Glide.with(this).load("http://47.100.32.161:8080"+url).into(imageView);



    }

}
