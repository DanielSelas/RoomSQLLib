package com.example.roomportallib;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WebAssetHandler {

    private final Context context;

    public WebAssetHandler(Context context) {
        this.context = context;
    }

    public String loadAsset(String filename) {
        StringBuilder content = new StringBuilder();
        try (InputStream is = context.getAssets().open("web/" + filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return content.toString();
    }
}