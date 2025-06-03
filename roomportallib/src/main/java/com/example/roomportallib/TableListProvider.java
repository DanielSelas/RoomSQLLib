package com.example.roomportallib;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

public class TableListProvider {

    public static List<String> getTableNames(RoomDatabase db) {
        List<String> tables = new ArrayList<>();

        if (db.getClass().getSimpleName().equals("AppDatabase")) {
            try {
                Class<?> appDatabaseClass = Class.forName("com.example.roomlibrary.AppDatabase");
                java.lang.reflect.Method method = appDatabaseClass.getDeclaredMethod("getEntityTableNames");
                Object result = method.invoke(null);
                if (result instanceof List) {
                    return (List<String>) result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SupportSQLiteDatabase sqlite = db.getOpenHelper().getReadableDatabase();
        String query = "SELECT name FROM sqlite_master WHERE type='table'";

        try (android.database.Cursor cursor = sqlite.query(new SimpleSQLiteQuery(query))) {
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                if (tableName.equalsIgnoreCase("sqlite_master") ||
                        tableName.equalsIgnoreCase("android_metadata") ||
                        tableName.startsWith("room_master_table")) {
                    continue;
                }
                tables.add(tableName);
            }
        }

        return tables;
    }
}