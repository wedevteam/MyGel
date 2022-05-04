package com.wedev.mygel.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.wedev.mygel.database.tables.TFirstTime;

import java.util.List;

@Dao
public interface TFirstTimeDao {
    @Query("select * from firsttime order by id desc")
    List<TFirstTime> getAll();

    @Query("select * from firsttime order by id desc")
    LiveData<List<TFirstTime>> getAllLiveData();

    @Query("select COUNT(*) from firsttime")
    int getNumber();

    @Insert
    void insert(TFirstTime tFirstTime);

    @Update
    void update(TFirstTime tFirstTime);

    @Delete
    void delete(TFirstTime tFirstTime);

    @Query("delete from firsttime")
    void deleteAll();
}