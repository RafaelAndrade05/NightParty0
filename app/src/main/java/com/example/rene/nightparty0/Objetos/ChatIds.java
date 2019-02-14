package com.example.rene.nightparty0.Objetos;

public class ChatIds {

    public static String CAMPO_IDUSUARIO = "idUsuario";
    public static String CAMPO_IDLUGAR= "idLugar";
    public static String CAMPO_IDCHAT= "idChat";


    private String idLugar;
    private String idUsuario;
    private String idChat;

    public ChatIds() {
    }

    public ChatIds(String idLugar, String idUsuario, String idChat) {
        this.idLugar = idLugar;
        this.idUsuario = idUsuario;
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

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }
}
