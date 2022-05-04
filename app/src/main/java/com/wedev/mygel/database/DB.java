package com.wedev.mygel.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.wedev.mygel.database.daos.TFirstTimeDao;
import com.wedev.mygel.database.daos.TMainDao;
import com.wedev.mygel.database.tables.TFirstTime;
import com.wedev.mygel.database.tables.TMain;

@Database(entities = {TMain.class, TFirstTime.class},version = 1,exportSchema = false)
public abstract class DB extends RoomDatabase {
    private static final String DB_NAME = "oppov1_db";

    private static DB instance;

    // Sync db
    public static synchronized DB getInstance(Context context) {
        if(instance==null){
            // Genera db
            instance= Room.databaseBuilder(context.getApplicationContext(),DB.class,DB_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build();
        }
        return instance;
    }

    // interfaces
    public abstract TMainDao tMainDao();
    public abstract TFirstTimeDao tFirstTimeDao();
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };


}