package com.example.final_project_dictionary_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.final_project_dictionary_app.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class SearchActivity extends AppCompatActivity {

    TextView tvResult;
    Button clear_btn, show_Detail_Btn;
    ImageView volume_Icon;
    MediaPlayer mediaPlayer;
    String audioUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        tvResult = findViewById(R.id.result);
        clear_btn = findViewById(R.id.clearBtn);
        volume_Icon = findViewById(R.id.volumeIcon);

        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("");
            }
        });

        show_Detail_Btn = findViewById(R.id.showDetailBtn);
//        show_Detail_Btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MenuItem searchMenuItem = findViewById(R.id.searchbar_menu_item);
//
//                // Get the SearchView from the MenuItem
//                SearchView searchView = (SearchView) searchMenuItem.getActionView();
//
//                String enteredWord = searchView.getQuery().toString();
//
//                // Open the new activity when the button is clicked
//                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
//                intent.putExtra("WORD", enteredWord);
//                startActivity(intent);
//            }
//        });

        show_Detail_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the new activity when the button is clicked
                Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        volume_Icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUrl != null && !audioUrl.isEmpty()) {
                    Log.d("MediaPlayer", "audioUrl: " + audioUrl); // Log the audio URL for debugging

                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }

                    mediaPlayer = new MediaPlayer();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build());
                    } else {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // Use correct constant
                    }

                    try {
                        mediaPlayer.setDataSource(audioUrl);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });

                        // Set the OnCompletionListener
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.d("MediaPlayer", "Playback completed successfully");

                                // Check if the MediaPlayer is not null before releasing it
                                if (mediaPlayer != null) {
                                    mediaPlayer.release();
                                    mediaPlayer = null; // Set to null after release
                                }
                            }
                        });
                    } catch (IOException e) {
                        Log.e("MediaPlayer", "Error setting data source or preparing: " + e.getMessage());
                    }
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        androidx.appcompat.widget.SearchView menuSearchItem = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.searchbar_menu_item).getActionView();
        menuSearchItem.setQueryHint("Search for a Word");
        menuSearchItem.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Handle search submission if needed
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Handle search text changes
                return false;
            }
        });

        menuSearchItem.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission
                try {
                    queryData(query);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle query text changes
                return false;
            }
        });

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //New code
    public void queryData(String word) throws IOException {
        URL url = Network.buildUrl(word);
        new DataTask().execute(url);
    }
// New code
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
        setcityData(s);
    }

    // New code
    public void setcityData(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);

            // Check if the array is empty
            if (jsonArray.length() == 0) {
                tvResult.setText("Word not found");
                volume_Icon.setVisibility(View.GONE); // Hide the volume icon if no definitions found
            } else {
                JSONObject responseObject = jsonArray.getJSONObject(0);

                // Extracting word and phonetics
                String word = responseObject.optString("word");
                JSONArray phoneticsArray = responseObject.optJSONArray("phonetics");

                StringBuilder phoneticStringBuilder = new StringBuilder();

                // Iterate through phonetics and append to StringBuilder
                if (phoneticsArray != null) {
                    for (int i = 0; i < phoneticsArray.length(); i++) {
                        JSONObject phoneticObject = phoneticsArray.getJSONObject(i);
                        String text = phoneticObject.optString("text");
                        phoneticStringBuilder.append("Phonetic ").append(i + 1).append(": ").append(text).append("\n");

                        // Check if audio URL is available and store it
                        if (phoneticObject.has("audio")) {
                            String completeAudioUrl = phoneticObject.getString("audio");
                            Log.d("AudioURL", "Complete Audio URL: " + completeAudioUrl);

                            // Assign the audioUrl only if it's not already assigned
                            if (audioUrl == null || audioUrl.isEmpty()) {
                                audioUrl = completeAudioUrl; // Use the full URL provided in the JSON
                            }
                        }
                    }
                }

                // Set visibility after checking all phonetics
                if (audioUrl != null && !audioUrl.isEmpty()) {
                    volume_Icon.setVisibility(View.VISIBLE); // Make the volume icon visible
                } else {
                    volume_Icon.setVisibility(View.GONE); // Hide the volume icon if no audio URL
                    Log.d("AudioURL", "No valid audio URL found.");
                }



                // Extract and append meanings
                JSONArray meaningsArray = responseObject.optJSONArray("meanings");
                StringBuilder meaningsStringBuilder = new StringBuilder("\nMeanings:\n");

                if (meaningsArray != null) {
                    for (int i = 0; i < meaningsArray.length(); i++) {
                        JSONObject meaningObject = meaningsArray.getJSONObject(i);
                        String partOfSpeech = meaningObject.optString("partOfSpeech");
                        meaningsStringBuilder.append(partOfSpeech).append(": ");

                        JSONArray definitionsArray = meaningObject.optJSONArray("definitions");
                        if (definitionsArray != null) {
                            for (int j = 0; j < definitionsArray.length(); j++) {
                                JSONObject definitionObject = definitionsArray.getJSONObject(j);
                                String definition = definitionObject.optString("definition");
                                meaningsStringBuilder.append(definition).append("\n");
                            }
                        }
                    }
                }

                // Display word, phonetic, and meanings information in the TextView
                tvResult.setText("Word: " + word + "\n" + phoneticStringBuilder.toString() + meaningsStringBuilder.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

//    public class DataTask extends AsyncTask<URL, Void, String> {
//
//        @Override
//        protected String doInBackground(URL... urls) {
//            URL url = urls[0];
//            String data = null;
//            try {
//                data = Network.getDatafromHttpUrl(url);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return data;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            setcityData(s);
//        }
//
//
//        // New code
//
//
//        public void setcityData(String data) {
//            try {
//                JSONArray jsonArray = new JSONArray(data);
//
//                if (jsonArray.length() > 0) {
//                    JSONObject wordObject = jsonArray.getJSONObject(0);
//
//                    // Extracting word and phonetics
//                    String word = wordObject.optString("word");
//                    JSONArray phoneticsArray = wordObject.getJSONArray("phonetics");
//
//                    StringBuilder phoneticStringBuilder = new StringBuilder();
//
//                    // Iterate through phonetics and append to StringBuilder
//                    for (int i = 0; i < phoneticsArray.length(); i++) {
//                        JSONObject phoneticObject = phoneticsArray.getJSONObject(i);
//                        String text = phoneticObject.optString("text");
//                        phoneticStringBuilder.append("Phonetic ").append(i + 1).append(": ").append(text).append("\n");
//                    }
//
//                    // Extract and append meanings
//                    JSONArray meaningsArray = wordObject.getJSONArray("meanings");
//                    StringBuilder meaningsStringBuilder = new StringBuilder("\nMeanings:\n");
//
//                    for (int i = 0; i < meaningsArray.length(); i++) {
//                        JSONObject meaningObject = meaningsArray.getJSONObject(i);
//                        String partOfSpeech = meaningObject.optString("partOfSpeech");
//                        meaningsStringBuilder.append(partOfSpeech).append(": ");
//
//                        JSONArray definitionsArray = meaningObject.getJSONArray("definitions");
//                        for (int j = 0; j < definitionsArray.length(); j++) {
//                            JSONObject definitionObject = definitionsArray.getJSONObject(j);
//                            String definition = definitionObject.optString("definition");
//                            meaningsStringBuilder.append(definition).append("\n");
//                        }
//                    }
//
//                    // Display word, phonetic, and meanings information in the TextView
//                    tvResult.setText("Word: " + word + "\n" + phoneticStringBuilder.toString() + meaningsStringBuilder.toString());
//                } else {
//                    tvResult.setText("Word not found");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}