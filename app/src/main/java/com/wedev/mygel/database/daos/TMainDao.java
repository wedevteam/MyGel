package com.wedev.mygel.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wedev.mygel.database.tables.TMain;

import java.util.List;

@Dao
public interface TMainDao {
    @Query("select * from main order by id desc")
    List<TMain> getAll();

    @Query("select * from main order by id desc")
    LiveData<List<TMain>> getAllLiveData();

    @Query("select COUNT(*) from main")
    int getNumber();

    @Insert
    void insert(TMain tMain);

    @Update
    void update(TMain tMain);

    @Delete
    void delete(TMain tMain);

    @Query("delete from main")
    void deleteAll();
}