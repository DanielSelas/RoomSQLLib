package com.example.roomlibrary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomportallib.RoomPortal;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        Button openPortalButton = new Button(this);
        openPortalButton.setText("Open RoomPortal");
        layout.addView(openPortalButton);

        setContentView(layout);

        AppDatabase db = androidx.room.Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                AppDatabase.class
        ).allowMainThreadQueries().build();

        db.userDao().insert(new User(000000000L, "Alice", "alice@example.com"));
        db.animalDao().insert(new Animal(0L, "Gerry", "Cat", 4, 12.2));
        RoomPortal.init(getApplicationContext(), db);

        openPortalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:9090/"));
                startActivity(browserIntent);
            }
        });
    }
}
