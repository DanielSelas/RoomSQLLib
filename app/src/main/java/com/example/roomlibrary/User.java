package com.example.roomlibrary;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    public Long id;

    public String name;
    public String email;

    public User(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

}