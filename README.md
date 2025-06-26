# 📦 RoomPortal Library & Sample Android App

## 🔍 Overview

**RoomPortal** is an Android library that lets you **browse and manage your Room database** through a lightweight local web portal served inside your app. It uses [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) to run a local HTTP server and deliver a responsive web interface accessible from any browser or WebView.

With RoomPortal you can:

- 📋 Browse all tables in your Room database dynamically  
- ✏️ Perform CRUD operations: Add, edit, delete rows  
- 📤 Export tables to CSV files  
- 📥 Import CSV files as temporary tables for quick viewing/manipulation  
- 🔄 Seamlessly switch between native Room tables and imported CSV data  
- 🌐 Access a modern web UI with column filters, dynamic forms, and real-time updates  

---

## ⚙️ Key Features

### Dynamic Table Browsing  
Lists all Room database tables automatically. Select any to view schema and data.

### Full CRUD Support  
Add new rows, update existing ones, and delete rows with immediate DB updates.

### CSV Export & Import  
Export any table as CSV and import CSV files as temporary UI tables without modifying the DB.

### Lightweight Embedded Server  
NanoHTTPD serves API and static assets on a configurable local port inside your app.

### Modern Web Interface  
Vanilla JS + HTML with dynamic checkboxes, forms, and data tables.

### Easy Integration  
Plug your existing RoomDatabase instance into the library to get started quickly.

---

## 📚 Library Components

| Class Name         | Description                                                        |
|--------------------|--------------------------------------------------------------------|
| `RoomPortalServer` | Embedded HTTP server managing API endpoints and assets            |
| `RawQueryExecutor` | Executes dynamic SQL queries (SELECT, INSERT, UPDATE, DELETE)     |
| `JsonUtils`        | JSON serialization/deserialization with Gson                      |
| `SchemaExtractor`  | Extracts table schema info (column names/types)                    |
| `TableListProvider`| Lists all available tables in Room database                        |
| `WebAssetHandler`  | Loads HTML/CSS/JS assets for the portal UI                         |
| `RoomPortal`       | Facade class to start the server easily                            |

---

## 🌐 Sample Web Portal

The portal includes:

- Dropdown to select tables  
- Column filter checkboxes  
- Data table with inline Add/Edit/Delete actions  
- Export to CSV button  
- Import CSV button & file input  
- Dynamic add/edit row forms  

Works with both native Room tables and CSV-loaded tables dynamically.

---

## 📱 Sample Android App Structure

| Class         | Purpose                                          |
|---------------|-------------------------------------------------|
| `User`, `Animal`  | Entity classes representing Room tables         |
| `UserDao`, `AnimalDao` | Data access interfaces for the entities         |
| `AppDatabase` | Room database holding entities                    |
| `MainActivity`| Initializes and starts the RoomPortalServer and opens the web portal |

---

## 🚀 Getting Started

1. Add the RoomPortal library to your project.  
2. Define your Room entities and DAOs as usual.  
3. Initialize `RoomPortalServer` with your Room database instance:  
   ```java
   RoomPortalServer server = new RoomPortalServer(this, 9090, appDatabase);
   server.start();
