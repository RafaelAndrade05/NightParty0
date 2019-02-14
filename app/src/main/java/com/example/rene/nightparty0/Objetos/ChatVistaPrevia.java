package com.example.rene.nightparty0.Objetos;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

public class ChatVistaPrevia implements Comparable<ChatVistaPrevia> {


    private String idChat;
    private String idLugar;
    private String idUsuario;
    private String nombre;
    private Date fecha;
    private String mensaje;
    private String urlImagen;

    public ChatVistaPrevia() {
    }

    public ChatVistaPrevia(String idChat, String idLugar, String idUsuario, String nombre, Date fecha, String mensaje, String urlImagen) {
        this.idChat = idChat;
        this.idLugar = idLugar;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.urlImagen = urlImagen;
    }

    public String getData(){
        String data = "";
        data += "IdChat => " + idChat + "\n";
        data += "IdLugar => " + idLugar + "\n";
        data += "nombre => " + nombre + "\n";
        data += "fecha => " + fecha + "\n";
        data += "mensaje => " + mensaje + "\n";
        data += "imagen => " + urlImagen + "\n";
        return data;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public String getIdLugar() {
        return idLugar;
    }

    public void setIdLugar(String idLugar) {
        this.idLugar = idLugar;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    @Override
    public int compareTo(@NonNull ChatVistaPrevia chatVistaPrevia) {
        return chatVistaPrevia.getFecha().compareTo(getFecha());
    }
}