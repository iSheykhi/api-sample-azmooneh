package com.azmooneh.sample.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.azmooneh.sample.R;
import com.azmooneh.sample.struct.StructAnswer;
import com.azmooneh.sample.struct.StructQuiz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static com.azmooneh.sample.activity.MainActivity.API_KEY;
import static com.azmooneh.sample.activity.MainActivity.LANGUAGE_KEY;
import static com.azmooneh.sample.activity.MainActivity.USER_ID;
import static com.azmooneh.sample.activity.MainActivity.API_SERVICE;
import static com.azmooneh.sample.activity.MainActivity.textToSpeech;

public class ExamActivity extends AppCompatActivity {
    private ArrayList<StructQuiz>   dataList    = new ArrayList<>();
    private ArrayList<StructAnswer> answersList = new ArrayList<>();
    private ArrayList<Integer>      wrongList   = new ArrayList<>();
    private TextView tvCounter, tvQuestion;
    private Button bSpeech;
    private ArrayList<Button> listButtonAnswer = new ArrayList<>();
    private int index = 0, stageId, wordId;
    private boolean gameOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        setTitle("Exam");

        // init
        tvCounter = findViewById(R.id.tvCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        bSpeech = findViewById(R.id.bSpeech);
        listButtonAnswer.add((Button) findViewById(R.id.bAnswer1));
        listButtonAnswer.add((Button) findViewById(R.id.bAnswer2));
        listButtonAnswer.add((Button) findViewById(R.id.bAnswer3));
        listButtonAnswer.add((Button) findViewById(R.id.bAnswer4));

        // get data list
        Bundle bundle = getIntent().getExtras();
        dataList = bundle.getParcelableArrayList("dataList");
        stageId = bundle.getInt("stageId");

        // set ClickListener
        bSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech(tvQuestion.getText().toString());
            }
        });

        View.OnClickListener answerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int btnPosition = 0;
                for (int i = 0; i < listButtonAnswer.size(); i++) {
                    if (listButtonAnswer.get(i).getId() == v.getId()) {
                        btnPosition = i;
                    }
                }
                StructAnswer structAnswer = answersList.get(btnPosition);
                if (structAnswer.state) {
                    if (gameOver) {
                        wrongList.add(wordId);
                        gameOver = false;
                        index = 0;
                        dataSet(index);
                        Toast.makeText(ExamActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (index >= dataList.size() - 1) {
                        saveRecord();
                        return;
                    }
                    dataSet(++index);
                } else {
                    Toast.makeText(ExamActivity.this, "): You Are LOSE :(", Toast.LENGTH_SHORT).show();
                    gameOver = true;
                    // Show the correct answer
                    for (int i = 0; i < listButtonAnswer.size(); i++) {
                        listButtonAnswer.get(i).setTextColor(answersList.get(i).state ? Color.BLUE : Color.RED);
                    }
                }
            }
        };

        for (int i = 0; i < listButtonAnswer.size(); i++) {
            listButtonAnswer.get(i).setOnClickListener(answerListener);
        }

        // first index data set
        dataSet(index);
    }

    private void saveRecord() {
        stageId++;
        Toast.makeText(this, "(: You Are WIN :)", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < listButtonAnswer.size(); i++) {
            listButtonAnswer.get(i).setEnabled(false);
        }
        bSpeech.setEnabled(false);

        AndroidNetworking.post(API_SERVICE + "save")
                .addHeaders("apiKey", API_KEY)
                .addBodyParameter("userId", USER_ID)
                .addBodyParameter("languageKey", LANGUAGE_KEY)
                .addBodyParameter("stageId", String.valueOf(stageId))
                .addBodyParameter("wrongList", String.valueOf(new JSONArray(wrongList)))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("tag", response.toString());
                        try {
                            JSONObject objReturn = response.getJSONObject("return");
                            if (objReturn.getInt("state") != 200) {
                                Toast.makeText(ExamActivity.this, objReturn.getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            new MaterialDialog.Builder(ExamActivity.this)
                                    .title("You win!")
                                    .content("Do you want to go to the next step(" + stageId + ")?")
                                    .positiveText("yes")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent intent = new Intent(ExamActivity.this, CourseActivity.class);
                                            intent.putExtra("stageId", stageId);
                                            ExamActivity.this.startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .negativeText("no")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog dialog, DialogAction which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (JSONException e) {
                            Log.e("tag", e.toString());
                            Toast.makeText(ExamActivity.this, "JSONObject response has error!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.e("tag", error.toString());
                        Toast.makeText(ExamActivity.this, "There was an error downloading on the network!", Toast.LENGTH_SHORT).show();
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
        StructQuiz structQuiz = dataList.get(pos);
        wordId = structQuiz.id;
        tvQuestion.setText(structQuiz.quiz);
        speech(structQuiz.quiz);
        answersList = structQuiz.answers;
        Collections.shuffle(answersList);
        for (int i = 0; i < answersList.size(); i++) {
            StructAnswer structAnswer = answersList.get(i);
            // set text value
            listButtonAnswer.get(i).setText(structAnswer.text);
            Log.e("tag", "state: " + structAnswer.state + " - text: " + structAnswer.text);
        }

        // reset color
        for (int i = 0; i < listButtonAnswer.size(); i++) {
            listButtonAnswer.get(i).setTextColor(Color.BLACK);
        }
    }

}
