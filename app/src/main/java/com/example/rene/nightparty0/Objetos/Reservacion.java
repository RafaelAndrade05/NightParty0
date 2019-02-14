package com.example.rene.nightparty0.Objetos;

import android.support.annotation.NonNull;

import java.util.Date;

public class Reservacion implements Comparable<Reservacion> {

    public static final String COLLECTION_RESERVACIONES = "reservaciones";
    public static final String CAMPO_ID = "id";
    public static final String CAMPO_DIA = "dia";
    public static final String CAMPO_HORA = "hora";
    public static final String CAMPO_PERSONAS = "numPersonas";
    public static final String CAMPO_USERID = "uid";
    public static final String CAMPO_LUGARID = "lugarId";
    public static final String CAMPO_COMENTARIO = "comentario";
    public static final String CAMPO_CONFIRMACION = "confirmacionReservacion";
    public static final String CAMPO_ESTATUS = "estatusReservacion";
    public static final String CAMPO_DATE = "timestamp";

    private String id;
    private String dia;
    private String hora;
    private String nombreLugar;
    private int numPersonas;
    private String uid;
    private String lugarId;
    private String comentarioU;
    private String comentarioN;
    private boolean confirmacionReservacion = false;
    private String estatusReservacion; //Pendiente,Rechazada y Aceptada
    private Date timestamp;


    public Reservacion() {

    }


    public Reservacion(String id, String dia, String hora, String nombreLugar, int numPersonas, String uid, String lugarId, String comentarioU, String comentarioN, boolean confirmacionReservacion, String estatusReservacion, Date timestamp) {
        this.id = id;
        this.dia = dia;
        this.hora = hora;
        this.nombreLugar = nombreLugar;
        this.numPersonas = numPersonas;
        this.uid = uid;
        this.lugarId = lugarId;
        this.comentarioU = comentarioU;
        this.comentarioN = comentarioN;
        this.confirmacionReservacion = confirmacionReservacion;
        this.estatusReservacion = estatusReservacion;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getNombreLugar() {
        return nombreLugar;
    }

    public void setNombreLugar(String nombreLugar) {
        this.nombreLugar = nombreLugar;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLugarId() {
        return lugarId;
    }

    public void setLugarId(String lugarId) {
        this.lugarId = lugarId;
    }

    public String getComentarioU() {
        return comentarioU;
    }

    public void setComentarioU(String comentarioU) {
        this.comentarioU = comentarioU;
    }

    public String getComentarioN() {
        return comentarioN;
    }

    public void setComentarioN(String comentarioN) {
        this.comentarioN = comentarioN;
    }

    public boolean isConfirmacionReservacion() {
        return confirmacionReservacion;
    }

    public void setConfirmacionReservacion(boolean confirmacionReservacion) {
        this.confirmacionReservacion = confirmacionReservacion;
    }

    public String getEstatusReservacion() {
        return estatusReservacion;
    }

    public void setEstatusReservacion(String estatusReservacion) {
        this.estatusReservacion = estatusReservacion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(@NonNull Reservacion reservacion) {
        return reservacion.getTimestamp().compareTo(getTimestamp());
    }
}