package com.example.final_project_dictionary_app;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
public class Network {
    final static String USADARA_BASE_URL="https://api.dictionaryapi.dev/api/v2/entries/en/";

    public static URL buildUrl(String word) {
        if (word == null || word.isEmpty()) {
            // Handle the case where the word is null or empty
            return null;
        }

        Uri uri = Uri.parse(USADARA_BASE_URL)
                .buildUpon()
                .appendPath(word)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }



    public static String getDatafromHttpUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            InputStream stream = connection.getInputStream();
            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");
            boolean hasnext = scanner.hasNext();
            if (hasnext) {
                return scanner.next();
            } else
                return null;
        }
        finally {
            connection.disconnect();
        }

    }
}
