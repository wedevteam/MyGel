package com.wedev.mygel.models;

public class ModelDevices {
    String nome;
    String mcaddress;
    int stato;

    public ModelDevices() {
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getMcaddress() {
        return mcaddress;
    }
    public void setMcaddress(String mcaddress) {
        this.mcaddress = mcaddress;
    }
    public int getStato() {
        return stato;
    }
    public void setStato(int stato) {
        this.stato = stato;
    }
}
