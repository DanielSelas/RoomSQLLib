package com.example.roomlibrary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnimalDao {
    @Query("SELECT * FROM Animal")
    List<Animal> getAll();

    @Insert
    void insert(Animal animal);
}
