package com.example.rene.nightparty0.Objetos;

import java.io.Serializable;

public class Promocion implements Serializable{

    public static final String COLLECTION_PROMOCIONES = "promociones";
    public static final String COLLECTION_IMAGENES = "imagenes";

    public static final String CAMPO_DESCRIPCION = "descripcion";

    private String descripcion;
    private String idLugar;

    public Promocion(){

    }

    public Promocion(String descripcion,String idLugar) {
        this.descripcion = descripcion;
        this.idLugar = idLugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(String idLugar) {
        this.idLugar = idLugar;
    }
}
