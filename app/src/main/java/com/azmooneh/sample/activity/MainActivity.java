package com.azmooneh.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.azmooneh.sample.R;
import com.azmooneh.sample.adapter.StageAdapter;
import com.azmooneh.sample.struct.StructStep;
import com.azmooneh.sample.struct.StructWord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.LANG_MISSING_DATA;
import static android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED;

public class MainActivity extends AppCompatActivity {

    // static value
    public static final String API_SERVICE  = "http://azmooneh.com/api/v1/";
    public static final String API_KEY      = "[apiKey]"; // To get the code, please contact Support.
    public static final String LANGUAGE_KEY = "en"; // en | ko | fr | tr | ar | de | ru | es | it | jp | zh
    public static final String USER_ID      = "ABCD";
    public static TextToSpeech textToSpeech;
    //
    private             int    continueStage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize AndroidNetworking
        AndroidNetworking.initialize(getApplicationContext());
        // initialize TextToSpeech
        initTTS();
        // download stage list
        AndroidNetworking.post(API_SERVICE + "stage")
                .addHeaders("apiKey", API_KEY)
                .addBodyParameter("userId", USER_ID)
                .addBodyParameter("languageKey", LANGUAGE_KEY)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        try {
                            JSONObject objReturn = response.getJSONObject("return");
                            if (objReturn.getInt("state") != 200) {
                                Toast.makeText(MainActivity.this, objReturn.getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ArrayList<StructStep> listStage = new ArrayList<>();
                            JSONArray             listStep  = response.getJSONObject("data").getJSONArray("steps");
                            for (int i = 0; i < listStep.length(); i++) {
                                StructStep structStep = new StructStep();
                                JSONObject objStep    = listStep.getJSONObject(i);
                                // id | A key value to get information about each lesson. (id == stepId)
                                structStep.id = objStep.getInt("id");
                                // title |Displays the title of each step.
                                structStep.title = objStep.getString("title");
                                // state | Report the status of each step. ( 0 == lock, 1 == complete, 2 == continue)
                                structStep.state = objStep.getInt("state");

                                listStage.add(structStep);

                                // Find the last step of the user to continue the stages.
                                if (structStep.state == 2) {
                                    continueStage = structStep.id;
                                }
                            }

                            // show stages list with RecyclerView.
                            RecyclerView               rvStages       = findViewById(R.id.rvStages);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                            RecyclerView.Adapter mAdapter = new StageAdapter(listStage, new StageAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(StructStep item) {
                                    if (item.state > 0) {
                                        // go to CourseActivity and download the course info.
                                        Intent intent = new Intent(MainActivity.this, CourseActivity.class);
                                        intent.putExtra("stageId", item.id);
                                        MainActivity.this.startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "this step is lock!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                            rvStages.setHasFixedSize(true);
                            rvStages.setLayoutManager(mLayoutManager);
                            rvStages.setAdapter(mAdapter);
                        } catch (JSONException e) {
                            Log.e("tag", e.toString());
                            Toast.makeText(MainActivity.this, "JSONObject response has error!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.e("tag", error.toString());
                        Toast.makeText(MainActivity.this, "There was an error downloading on the network!", Toast.LENGTH_SHORT).show();
                    }
                });

        //
        findViewById(R.id.bStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CourseActivity.class);
                intent.putExtra("userId", USER_ID);
                intent.putExtra("stageId", continueStage);
                MainActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.bWrongs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                AndroidNetworking.post(API_SERVICE + "remember")
                        .addHeaders("apiKey", API_KEY)
                        .addBodyParameter("userId", USER_ID)
                        .addBodyParameter("languageKey", LANGUAGE_KEY)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                try {
                                    JSONObject objReturn = response.getJSONObject("return");
                                    if (objReturn.getInt("state") != 200) {
                                        Toast.makeText(MainActivity.this, objReturn.getString("message"), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    JSONArray             list      = response.getJSONObject("data").getJSONArray("list");

                                    if (list.length() == 0){
                                        Toast.makeText(MainActivity.this, "There is nothing wrong word to remember.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    ArrayList<StructWord> listWrongWord = new ArrayList<>();
                                    for (int i = 0; i < list.length(); i++) {
                                        StructWord structWord = new StructWord();
                                        JSONObject objWord    = list.getJSONObject(i);
                                        structWord.id = objWord.getInt("id");
                                        structWord.word = objWord.getString("word");
                                        structWord.mean = objWord.getString("mean");
                                        listWrongWord.add(structWord);
                                    }

                                    Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                                    intent.putExtra("dataList", listWrongWord);
                                    intent.putExtra("remember", true);
                                    MainActivity.this.startActivity(intent);

                                } catch (JSONException e) {
                                    Log.e("tag", e.toString());
                                    Toast.makeText(MainActivity.this, "JSONObject response has error!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                Log.e("tag", error.toString());
                                Toast.makeText(MainActivity.this, "There was an error downloading on the network!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public void initTTS() {
        TextToSpeech.OnInitListener listener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.UK);
                        switch (result) {
                            case LANG_MISSING_DATA:
                                Log.e("tag", "TextToSpeech -> lang missing data (" + LANG_MISSING_DATA + ")");
                                break;
                            case LANG_NOT_SUPPORTED:
                                Log.e("tag", "TextToSpeech -> lang not supported (" + LANG_NOT_SUPPORTED + ")");
                                break;
                            default:
                                Log.e("tag", "TextToSpeech -> supported (" + result + ")");
                                break;
                        }
                    } else {
                        Log.e("tag", "TextToSpeech -> Initialization TTS Failed!");
                    }
                } catch (Exception e) {
                    Log.e("tag", "TextToSpeech -> " + e.toString());
                }
            }
        };
        textToSpeech = new TextToSpeech(MainActivity.this, listener, "com.google.android.tts");
        textToSpeech.setSpeechRate((float) 0.8);
    }
}
