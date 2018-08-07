package com.azmooneh.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.azmooneh.sample.R;
import com.azmooneh.sample.struct.StructAnswer;
import com.azmooneh.sample.struct.StructQuiz;
import com.azmooneh.sample.struct.StructWord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.azmooneh.sample.activity.MainActivity.API_KEY;
import static com.azmooneh.sample.activity.MainActivity.LANGUAGE_KEY;
import static com.azmooneh.sample.activity.MainActivity.USER_ID;
import static com.azmooneh.sample.activity.MainActivity.API_SERVICE;

public class CourseActivity extends AppCompatActivity {

    ArrayList<StructWord> courseContents  = new ArrayList<>();
    ArrayList<StructQuiz> courseQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        // get bundle stageId value
        Bundle    bundle  = getIntent().getExtras();
        final int stageId = bundle.getInt("stageId");
        setTitle("Course of step " + stageId);
        // initialize AndroidNetworking.
        AndroidNetworking.initialize(getApplicationContext());
        // download course info.
        courseContents.clear();
        courseQuestions.clear();
        AndroidNetworking.post(API_SERVICE + "course")
                .addHeaders("apiKey", API_KEY)
                .addBodyParameter("userId", USER_ID)
                .addBodyParameter("languageKey", LANGUAGE_KEY)
                .addBodyParameter("stageId", String.valueOf(stageId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        try {
                            JSONObject objReturn = response.getJSONObject("return");
                            if (objReturn.getInt("state") != 200) {
                                Toast.makeText(CourseActivity.this, objReturn.getString("message"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Get the content of the educational section
                            JSONArray contents = response.getJSONObject("data").getJSONArray("contents");
                            for (int i = 0; i < contents.length(); i++) {
                                StructWord structWord = new StructWord();
                                JSONObject objWord    = contents.getJSONObject(i);
                                structWord.id = objWord.getInt("id");
                                structWord.word = objWord.getString("word");
                                structWord.mean = objWord.getString("mean");
                                courseContents.add(structWord);
                            }

                            // Get the content of the questions section
                            JSONArray questions = response.getJSONObject("data").getJSONArray("questions");
                            for (int i = 0; i < questions.length(); i++) {
                                StructQuiz structQuiz = new StructQuiz();
                                JSONObject objQuiz    = questions.getJSONObject(i);
                                structQuiz.id = objQuiz.getInt("id");
                                structQuiz.quiz = objQuiz.getString("quiz");

                                ArrayList<StructAnswer> questionAnswers = new ArrayList<>();
                                JSONArray               answers         = objQuiz.getJSONArray("answers");
                                for (int j = 0; j < answers.length(); j++) {
                                    StructAnswer structAnswer = new StructAnswer();
                                    JSONObject   objAnswer    = answers.getJSONObject(j);
                                    structAnswer.state = objAnswer.getInt("state") == 1;
                                    structAnswer.text = objAnswer.getString("text");
                                    questionAnswers.add(structAnswer);
                                }
                                structQuiz.answers = questionAnswers;
                                courseQuestions.add(structQuiz);
                            }
                        } catch (JSONException e) {
                            Log.e("tag", e.toString());
                            Toast.makeText(CourseActivity.this, "JSONObject response has error!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.e("tag", error.toString());
                        Toast.makeText(CourseActivity.this, "There was an error downloading on the network!", Toast.LENGTH_SHORT).show();
                    }
                });

        // Study the educational content
        findViewById(R.id.bContents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseContents.isEmpty()) {
                    Toast.makeText(CourseActivity.this, "Course Contents isEmpty! Please wait or try again to download.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CourseActivity.this, ReadActivity.class);
                intent.putExtra("dataList", courseContents);
                intent.putExtra("remember", false);
                CourseActivity.this.startActivity(intent);
            }
        });

        // Answer the questions
        findViewById(R.id.bQuestions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseQuestions.isEmpty()) {
                    Toast.makeText(CourseActivity.this, "Course Questions isEmpty! Please wait or try again to download.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CourseActivity.this, ExamActivity.class);
                intent.putExtra("dataList", courseQuestions);
                intent.putExtra("stageId", stageId);
                CourseActivity.this.startActivity(intent);
            }
        });
    }
}
