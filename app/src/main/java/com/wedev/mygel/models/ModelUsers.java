package com.wedev.mygel.models;

import java.util.ArrayList;

public class ModelUsers {
    public ModelUsers() {
    }
    String Id ;
    String Nome ;
    String Cognome ;
    String Indirizzo ;
    String Citta ;
    String Email ;
    String Password ;
    String Status ;
    String Note ;
    String TipoProfilo ;
    String IPAddress;
    String Token ;
    String ForgotHash ;
    String DataCreazione ;
    String DataModifica ;
    public String getId() {
        return Id;
    }
    public void setId(String id) {
        Id = id;
    }
    public String getNome() {
        return Nome;
    }
    public void setNome(String nome) {
        Nome = nome;
    }
    public String getCognome() {
        return Cognome;
    }
    public void setCognome(String cognome) {
        Cognome = cognome;
    }
    public String getIndirizzo() {
        return Indirizzo;
    }
    public void setIndirizzo(String indirizzo) {
        Indirizzo = indirizzo;
    }
    public String getCitta() {
        return Citta;
    }
    public void setCitta(String citta) {
        Citta = citta;
    }
    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) {
        Email = email;
    }
    public String getPassword() {
        return Password;
    }
    public void setPassword(String password) {
        Password = password;
    }
    public String getStatus() {
        return Status;
    }
    public void setStatus(String status) {
        Status = status;
    }
    public String getNote() {
        return Note;
    }
    public void setNote(String note) {
        Note = note;
    }
    public String getTipoProfilo() {
        return TipoProfilo;
    }
    public void setTipoProfilo(String tipoProfilo) {
        TipoProfilo = tipoProfilo;
    }
    public String getIPAddress() {
        return IPAddress;
    }
    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }
    public String getToken() {
        return Token;
    }
    public void setToken(String token) {
        Token = token;
    }
    public String getForgotHash() {
        return ForgotHash;
    }
    public void setForgotHash(String forgotHash) {
        ForgotHash = forgotHash;
    }
    public String getDataCreazione() {
        return DataCreazione;
    }
    public void setDataCreazione(String dataCreazione) {
        DataCreazione = dataCreazione;
    }
    public String getDataModifica() {
        return DataModifica;
    }
    public void setDataModifica(String dataModifica) {
        DataModifica = dataModifica;
    }
}
