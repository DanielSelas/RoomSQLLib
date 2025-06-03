package com.example.roomportallib;

import android.content.Context;
import androidx.room.RoomDatabase;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomPortalServer extends NanoHTTPD {

    private final Context context;
    private final RoomDatabase database;
    private final WebAssetHandler webAssetHandler;

    public RoomPortalServer(Context context, int port, RoomDatabase db) {
        super("0.0.0.0", port);
        this.context = context;
        this.database = db;
        this.webAssetHandler = new WebAssetHandler(context);
    }

    private String getRequestBody(IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        System.out.println("Headers: " + session.getHeaders());
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        System.out.println("Parsed body keys: " + files.keySet());
        String body = files.get("postData");
        if (body == null || body.isEmpty()) {
            if (!files.isEmpty()) {
                body = files.values().iterator().next();
            }
        }
        System.out.println("Request body: " + body);
        return body;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        String body = null;
        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
            body = files.get("postData");
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
        }

        Map<String, Object> jsonBody = null;
        String _methodOverride = null;

        if (body != null && !body.isEmpty()) {
            try {
                jsonBody = JsonUtils.fromJsonToMap(body);
                if (jsonBody != null && jsonBody.containsKey("_method")) {
                    _methodOverride = (String) jsonBody.get("_method");
                    jsonBody.remove("_method");
                    if (_methodOverride.equalsIgnoreCase("PUT")) method = Method.PUT;
                    else if (_methodOverride.equalsIgnoreCase("DELETE")) method = Method.DELETE;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Handling URI: " + uri + " with HTTP method: " + method);
        System.out.println("Request body: " + body);

        if (uri.equals("/") || uri.endsWith(".html") || uri.endsWith(".js") || uri.endsWith(".css")) {
            String fileName = uri.equals("/") ? "index.html" : uri.substring(1);
            String content = webAssetHandler.loadAsset(fileName);

            if (content != null) {
                String mimeType = "text/plain";
                if (fileName.endsWith(".html")) mimeType = "text/html";
                else if (fileName.endsWith(".js")) mimeType = "application/javascript";
                else if (fileName.endsWith(".css")) mimeType = "text/css";

                return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, mimeType, content);
            } else {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", fileName + " not found");
            }
        }

        if (uri.equals("/ping")) {
            return NanoHTTPD.newFixedLengthResponse("pong");
        }

        if (uri.equals("/tables")) {
            List<String> tableNames = TableListProvider.getTableNames(database);
            String json = JsonUtils.toJson(tableNames);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/json", json);
        }

        if (uri.startsWith("/table/") && method == Method.GET) {
            String tableName = uri.replace("/table/", "");
            String queryParams = session.getQueryParameterString();
            List<String> cols = null;

            if (queryParams != null && queryParams.contains("cols=")) {
                String[] parts = queryParams.split("cols=");
                if (parts.length > 1) {
                    String colString = parts[1].split("&")[0];
                    cols = List.of(colString.split(","));
                }
            }

            try {
                List<Map<String, Object>> tableData = RawQueryExecutor.getTableData(database, tableName, cols);
                String json = JsonUtils.toJson(tableData);
                return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/json", json);
            } catch (Exception e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to query table");
            }
        }

        if (uri.startsWith("/table/") && (method == Method.POST || method == Method.PUT)) {
            String tableName = uri.replace("/table/", "");
            if (jsonBody == null) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing body data");
            }
            try {
                if (method == Method.POST) {
                    RawQueryExecutor.insertRow(database, tableName, jsonBody);
                    return NanoHTTPD.newFixedLengthResponse("Row inserted successfully");
                } else {
                    RawQueryExecutor.updateRow(database, tableName, jsonBody);
                    return NanoHTTPD.newFixedLengthResponse("Row updated successfully");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to insert/update row: " + e.getMessage());
            }
        }

        if (uri.startsWith("/table/") && method == Method.DELETE) {
            String tableName = uri.replace("/table/", "");
            if (jsonBody == null) {
                return NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing body data");
            }
            try {
                RawQueryExecutor.deleteRowByRow(database, tableName, jsonBody);
                return NanoHTTPD.newFixedLengthResponse("Row deleted successfully");
            } catch (Exception e) {
                e.printStackTrace();
                return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to delete row: " + e.getMessage());
            }
        }

        if (uri.startsWith("/schema/")) {
            String tableName = uri.replace("/schema/", "");
            List<Map<String, Object>> schema = SchemaExtractor.getTableSchema(database, tableName);
            String json = JsonUtils.toJson(schema);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "application/json", json);
        }

        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
    }
}