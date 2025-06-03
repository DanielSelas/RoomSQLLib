package com.example.roomportallib;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaExtractor {

    public static List<Map<String, Object>> getTableSchema(RoomDatabase db, String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();

        SupportSQLiteDatabase sqlite = db.getOpenHelper().getReadableDatabase();
        String query = "PRAGMA table_info(" + tableName + ")";
        try (android.database.Cursor cursor = sqlite.query(new SimpleSQLiteQuery(query))) {
            while (cursor.moveToNext()) {
                Map<String, Object> column = new HashMap<>();
                column.put("cid", cursor.getInt(cursor.getColumnIndexOrThrow("cid")));
                column.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")));
                column.put("type", cursor.getString(cursor.getColumnIndexOrThrow("type")));
                column.put("notnull", cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1);
                column.put("dflt_value", cursor.getString(cursor.getColumnIndexOrThrow("dflt_value")));
                column.put("pk", cursor.getInt(cursor.getColumnIndexOrThrow("pk")) == 1);
                columns.add(column);
            }
        }

        return columns;
    }
}