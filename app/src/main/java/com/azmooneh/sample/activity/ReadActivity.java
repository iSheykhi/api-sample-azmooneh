package com.azmooneh.sample.activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.azmooneh.sample.R;
import com.azmooneh.sample.struct.StructWord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azmooneh.sample.activity.MainActivity.API_KEY;
import static com.azmooneh.sample.activity.MainActivity.LANGUAGE_KEY;
import static com.azmooneh.sample.activity.MainActivity.USER_ID;
import static com.azmooneh.sample.activity.MainActivity.API_SERVICE;
import static com.azmooneh.sample.activity.MainActivity.textToSpeech;

public class ReadActivity extends AppCompatActivity {
    private ArrayList<StructWord> dataList = new ArrayList<>();
    private TextView tvCounter, tvWord, tvMean;
    private int index = 0, wordId;
    private boolean isRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        setTitle("Read course content");

        // initialize AndroidNetworking
        AndroidNetworking.initialize(getApplicationContext());

        // init UI
        tvCounter = findViewById(R.id.tvCounter);
        tvWord = findViewById(R.id.tvWord);
        tvMean = findViewById(R.id.tvMeans);

        // get data list
        Bundle bundle = getIntent().getExtras();
        dataList = bundle.getParcelableArrayList("dataList");
        isRemember = bundle.getBoolean("remember");
        if (isRemember) findViewById(R.id.llChallenge).setVisibility(View.VISIBLE);

        // first index data set
        dataSet(index);

        // listener
        findViewById(R.id.bNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index >= dataList.size() - 1) return;
                dataSet(++index);
            }
        });

        findViewById(R.id.bBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index <= 0) return;
                dataSet(--index);
            }
        });

        findViewById(R.id.bSpeech).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech(tvWord.getText().toString());
            }
        });

        findViewById(R.id.bShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMean.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.bYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidNetworking.post(API_SERVICE + "remember")
                        .addHeaders("apiKey", API_KEY)
                        .addBodyParameter("userId", USER_ID)
                        .addBodyParameter("languageKey", LANGUAGE_KEY)
                        // add wordId to remove.
                        .addBodyParameter("wordId", String.valueOf(wordId))
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject objReturn = response.getJSONObject("return");
                                    Toast.makeText(ReadActivity.this, objReturn.getString("message"), Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    Log.e("tag", e.toString());
                                    Toast.makeText(ReadActivity.this, "JSONObject response has error!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                Log.e("tag", error.toString());
                                Toast.makeText(ReadActivity.this, "There was an error downloading on the network!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        findViewById(R.id.bNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReadActivity.this, "OK, More Try.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void speech(String text) {
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void dataSet(int pos) {
        tvCounter.setText(String.valueOf((pos + 1) + "/" + dataList.size()));
        StructWord structWord = dataList.get(pos);
        wordId = structWord.id;
        tvWord.setText(structWord.word);
        tvMean.setText(structWord.mean);
        speech(structWord.word);

        // just for remember word
        if (isRemember) {
            tvMean.setVisibility(View.INVISIBLE);
        }
    }

}
