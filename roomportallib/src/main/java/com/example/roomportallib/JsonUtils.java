package com.example.roomportallib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public class JsonUtils {


    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();


    public static String toJson(Object data) {
        if (data == null) return "{}";
        return gson.toJson(data);
    }


    public static <T> String toJsonList(List<T> list) {
        if (list == null || list.isEmpty()) return "[]";
        return gson.toJson(list);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }


    public static <T> List<T> fromJsonList(String json, Class<T> type) {
        Type listType = TypeToken.getParameterized(List.class, type).getType();
        return gson.fromJson(json, listType);
    }

    public static Map<String, Object> fromJsonToMap(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
