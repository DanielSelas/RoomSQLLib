package com.example.roomportallib;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.room.RoomDatabase;

import java.io.IOException;

public class RoomPortal {

    private static RoomPortalServer server;
    private static RoomDatabase database;

    public static void init(Context context, RoomDatabase db) {
        if (server != null) return;
        database = db;
        try {
            server = new RoomPortalServer(context.getApplicationContext(), 9090, db);
            server.start();
            Log.d("RoomPortal", "Server started on port 9090");
        } catch (IOException e) {
            Log.e("RoomPortal", "Failed to start server: " + e.getMessage(), e);
            Toast.makeText(context, "Server failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
            server = null;
            database = null;
        }
    }

    public static RoomDatabase getDatabase() {
        return database;
    }
}