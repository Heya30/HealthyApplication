package com.example.healthapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthapplication.Adapter.AnswerAdapter;
import com.example.healthapplication.model.AnswerReport;
import com.example.healthapplication.model.User;
import com.example.healthapplication.MyView.LoadMoreListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QandAFragment extends Fragment {


    private LoadMoreListView answerLv;
    private AnswerAdapter adapter;
    private CardView questioncard;
    private List<AnswerReport> reportList = new ArrayList<>();
    private int page = 0;
    public  static  final int ADDRECORD = 1;

    public QandAFragment() {}


    public static QandAFragment newInstance() {
        QandAFragment fragment = new QandAFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qand, container, false);
        Toolbar toolbar = view.findViewById(R.id.qa_tip_toolbar);
        toolbar.setTitle("智能问答");

        answerLv = view.findViewById(R.id.lvTrace);

        questioncard = view.findViewById(R.id.question_card);

        askForOkhttp();

        adapter = new AnswerAdapter(getContext(), reportList);
        answerLv.setAdapter(adapter);

        answerLv.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onloadMore() {
                loadMore();
            }
        });

        questioncard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }



    private void askForOkhttp(){
        //{{base_url}}/GET/QARecord?pageNum=1&pageSize=10
        page = page+1;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    String url = "http://47.100.32.161:8080/GET/QARecord?pageNum=" + page;
                    Request request = new Request.Builder().url(url).addHeader("Authorization", User.getInstance().getToken()).build();
                    Response response = client.newCall(request).execute();
                    String data = response.body().string();
                    Log.d("logaa",response.code()+"data:"+data);
                    JSONObject jsonObject = new JSONObject(data);

                    //Object dataObject = jsonObject.getJSONObject("data");
                    JSONArray array = jsonObject.getJSONArray( "data" );
                    String s2=array.toString();
                    Log.d("logaa",s2);

                    Gson gson = new Gson();
                    List<AnswerReport> answerReports = gson.fromJson(s2, new TypeToken<List<AnswerReport>>(){}.getType());

                    if(answerReports.size() >0){
                        Message message = new Message();
                        message.what = ADDRECORD;
                        handler.sendMessage(message);
                    }

                    for(AnswerReport answerReport: answerReports){
                        reportList.add(answerReport);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case ADDRECORD:
                    adapter.notifyDataSetChanged();
                    answerLv.setLoadCompleted();
                    break;
                    default:
                        break;
            }
        }
    };

    private void loadMore() {
        askForOkhttp();
    }

}
