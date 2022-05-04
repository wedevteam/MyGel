package com.wedev.mygel.database.tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "main")

public class TMain {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "IdServer")
    public String IdServer;
    @ColumnInfo(name = "Token")
    public String Token;

    public TMain() {
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getIdServer() {
        return IdServer;
    }
    public void setIdServer(String idServer) {
        IdServer = idServer;
    }
    public String getToken() {
        return Token;
    }
    public void setToken(String token) {
        Token = token;
    }
}
