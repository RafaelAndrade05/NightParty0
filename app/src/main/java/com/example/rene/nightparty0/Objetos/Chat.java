package com.example.rene.nightparty0.Objetos;

import java.util.Date;

public class Chat {

    public static final String COLLECTION_CHAT = "chats";
    public static final String COLLECTION_MENSAJES = "messages";

    public static final String CAMPO_DATE = "timestamp";

    public static final String TEXT = "TEXT";
    public static final String IMAGE = "IMAGE";

    private String message;
    private String userId;
    private String lugarId;
    private Date timestamp;
    private String recibidoId; //variable para las notificaciones id del que recibe mensaje
    private String nombreRemitente; //variable para las notificaciones nombre de quien mando el mensaje
    private String type = TEXT; //texto o imagen

    public Chat() {
    }

    public Chat(String message, String userId, String lugarId, Date timestamp, String recibidoId, String nombreRemitente, String type) {
        this.message = message;
        this.userId = userId;
        this.lugarId = lugarId;
        this.timestamp = timestamp;
        this.recibidoId = recibidoId;
        this.nombreRemitente = nombreRemitente;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLugarId() {
        return lugarId;
    }

    public void setLugarId(String lugarId) {
        this.lugarId = lugarId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecibidoId() {
        return recibidoId;
    }

    public void setRecibidoId(String recibidoId) {
        this.recibidoId = recibidoId;
    }

    public String getNombreRemitente() {
        return nombreRemitente;
    }

    public void setNombreRemitente(String nombreRemitente) {
        this.nombreRemitente = nombreRemitente;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
