package com.wedev.mygel.database.tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "firsttime")

public class TFirstTime {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "IsFirstTime")
    public int IsFirstTime;

    public TFirstTime() {
    }
    public int getIsFirstTime() {
        return IsFirstTime;
    }
    public void setIsFirstTime(int isFirstTime) {
        IsFirstTime = isFirstTime;
    }
}
