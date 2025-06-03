package com.example.roomportallib;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawQueryExecutor {

    public static List<Map<String, Object>> getTableData(RoomDatabase db, String tableName, List<String> columns) {
        List<Map<String, Object>> rows = new ArrayList<>();

        SupportSQLiteDatabase sqlite = db.getOpenHelper().getReadableDatabase();

        String columnString = (columns == null || columns.isEmpty())
                ? "*"
                : joinColumns(columns);

        String queryStr = "SELECT " + columnString + " FROM " + tableName;
        SupportSQLiteQuery query = new SimpleSQLiteQuery(queryStr);

        try (android.database.Cursor cursor = sqlite.query(query)) {
            while (cursor.moveToNext()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    int type = cursor.getType(i);
                    String colName = cursor.getColumnName(i);

                    switch (type) {
                        case android.database.Cursor.FIELD_TYPE_INTEGER:
                            row.put(colName, cursor.getLong(i));
                            break;
                        case android.database.Cursor.FIELD_TYPE_FLOAT:
                            row.put(colName, cursor.getDouble(i));
                            break;
                        case android.database.Cursor.FIELD_TYPE_STRING:
                            row.put(colName, cursor.getString(i));
                            break;
                        case android.database.Cursor.FIELD_TYPE_BLOB:
                            row.put(colName, "[BLOB]");
                            break;
                        case android.database.Cursor.FIELD_TYPE_NULL:
                        default:
                            row.put(colName, null);
                            break;
                    }
                }
                rows.add(row);
            }
        }

        return rows;
    }

    private static String joinColumns(List<String> columns) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            builder.append("`").append(columns.get(i)).append("`");
            if (i < columns.size() - 1) builder.append(", ");
        }
        return builder.toString();
    }

    public static void insertRow(RoomDatabase db, String tableName, Map<String, Object> row) {
        SupportSQLiteDatabase sqlite = db.getOpenHelper().getWritableDatabase();

        StringBuilder keys = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> args = new ArrayList<>();

        for (String key : row.keySet()) {
            if ("id".equalsIgnoreCase(key)) continue;
            if (keys.length() > 0) {
                keys.append(", ");
                placeholders.append(", ");
            }
            keys.append("`").append(key).append("`");
            placeholders.append("?");
            args.add(row.get(key));
        }

        String sql = "INSERT INTO " + tableName + " (" + keys + ") VALUES (" + placeholders + ")";
        sqlite.execSQL(sql, args.toArray());
    }

    public static void updateRow(RoomDatabase db, String tableName, Map<String, Object> row) {
        SupportSQLiteDatabase sqlite = db.getOpenHelper().getWritableDatabase();

        if (!row.containsKey("id")) {
            throw new IllegalArgumentException("Row must contain 'id' for update.");
        }

        Object idValue = row.get("id");

        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<Object> args = new ArrayList<>();

        boolean first = true;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if ("id".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            if (!first) sql.append(", ");
            sql.append(entry.getKey()).append(" = ?");
            args.add(entry.getValue());
            first = false;
        }

        sql.append(" WHERE id = ?");
        args.add(idValue);

        sqlite.execSQL(sql.toString(), args.toArray());
    }

    public static void deleteRowByRow(RoomDatabase db, String tableName, Map<String, Object> row) {
        SupportSQLiteDatabase sqlite = db.getOpenHelper().getWritableDatabase();
        StringBuilder sql = new StringBuilder("DELETE FROM " + tableName + " WHERE ");
        List<Object> args = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (i++ > 0) sql.append(" AND ");
            sql.append(entry.getKey()).append(" = ?");
            args.add(entry.getValue());
        }

        sqlite.execSQL(sql.toString(), args.toArray());
    }
}