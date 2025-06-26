package com.example.roomlibrary;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class,  Animal.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract AnimalDao animalDao();
}