package com.example.final_project_dictionary_app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    TextView more_info;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        more_info = findViewById(R.id.moreInfo);

        String hardcodedWord = "river";


        // Retrieve data from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("WORD")) {
            String word = intent.getStringExtra("WORD");

            // Make an API call using the word
            try {
                queryData(hardcodedWord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void queryData(String word) throws IOException {
        URL url = Network.buildUrl(word);
        new DataTask().execute(url);
    }

    public class DataTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String data = null;
            try {
                data = Network.getDatafromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            // Handle the API response and update UI accordingly
            Log.d("DetailActivity", "Response: " + s);
            try {
                // Parse the JSON response
                JSONArray jsonArray = new JSONArray(s);

                if (jsonArray.length() > 0) {
                    JSONObject wordObject = jsonArray.getJSONObject(0);

                    // Extract origin, phonetics, and audio
                    String origin = wordObject.optString("origin");

                    JSONArray phoneticsArray = wordObject.getJSONArray("phonetics");
                    StringBuilder phoneticStringBuilder = new StringBuilder("Phonetics:\n");

                    for (int i = 0; i < phoneticsArray.length(); i++) {
                        JSONObject phoneticObject = phoneticsArray.getJSONObject(i);
                        String text = phoneticObject.optString("text");

                        phoneticStringBuilder.append("Text: ").append(text);

                        // Check if audio is available
                        if (phoneticObject.has("audio")) {
                            String audio = phoneticObject.optString("audio");
                            phoneticStringBuilder.append(", Audio: ").append(audio);
                        }

                        phoneticStringBuilder.append("\n");
                    }

                    // Display origin, phonetics, and audio information in the TextView
                    more_info.setText("Origin: " + origin + "\n" + phoneticStringBuilder.toString());
                } else {
                    more_info.setText("Word not found");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                more_info.setText("Error parsing JSON");
            }
        }



    }
}