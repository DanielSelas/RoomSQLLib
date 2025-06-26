package com.example.roomlibrary;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jspecify.annotations.NonNull;

@Entity
public class Animal {
    @PrimaryKey
    @NonNull
    public Long id;

    public String name;
    public String kind;
    public int age;
    public double weight;

    public Animal(@NonNull Long id, String name, String kind, int age, double weight) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.age = age;
        this.weight = weight;
    }
}
